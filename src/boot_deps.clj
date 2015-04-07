(ns boot-deps
  (:require
    [boot.core       :as core :refer (deftask)]
    [boot.file       :as file]
    [clojure.edn     :as edn]
    [clojure.java.io :as io]
    [boot-deps.git   :as git]
    [boot-deps.npm   :as npm]))

(declare fetch-deps)

(defmulti fetch (fn [_ [type _]] type))

(defmethod fetch :git [cache [_ deps]]
  (git/clone deps cache))

(defmethod fetch :npm [_ [_ deps]]
  (npm/install deps))

(defmethod fetch :default [_ _]
  ;; NOOP
  )

(defn- read-deps [source]
  (let [deps (io/file source "deps.edn")]
    (when (.exists deps)
      (-> deps slurp edn/read-string))))

(defn- fetch-deps [deps cache]
  (let [sources (mapcat (partial fetch cache) deps)
        trans   (mapcat #(-> % read-deps (fetch-deps cache)) sources)]
    (concat sources trans)))

(defn- add-sources [fileset sources]
  (reduce #(core/add-source %1 (io/file %2 "src"))
          fileset
          sources))

(defn- add-deps [deps]
  (when (not-empty deps)
    (println "Adding maven dependencies" deps))
  (core/set-env! :dependencies #(-> (concat % deps) vec)))

(defn load-env! []
  (let [cache   (core/cache-dir! :deps/sources)
        sources (.list cache)]
    (doseq [source sources]
      (let [dir (io/file cache source)]
        (when (.isDirectory dir)
          (add-deps (:jar (read-deps dir))))))))

(deftask install
  [g git VAL #{[sym str str]} "Git dependencies"]
  "Read a dependency description from the project and process it"
  (let [cache (core/cache-dir! :deps/sources)]
    (fn [next]
      (fn [fileset]
        (let [sources (fetch-deps {:git git} cache)
              fileset (add-sources fileset sources)]
          (next fileset))))))

(deftask source
  []
  "Put downloaded source depdnencies on the classpath"
  (let [cache (core/cache-dir! :deps/sources)]
    (fn [next]
      (fn [fileset]
        (->> (.list cache)
             (map #(io/file cache %))
             (add-sources fileset)
             (next))))))

(defn clear
  []
  "Clear the source download cache. This should hardly ever be needed
   but is provided as a nuclear option"
  (let [cache (core/cache-dir! :deps/sources)]
    (fn [next]
      (fn [fileset]
        ;; TODO: since boot might be buggy and we don't want to delete
        ;; anything critical by accident we present the task to the
        ;; user and ask him to confir deletion
        (core/empty-dir! cache)
        (next fileset)))))
