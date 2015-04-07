(ns boot-deps.git
  (:require
    [boot.core :as core]
    [boot.file :as file]
    [clojure.java.io :as io]
    [me.raynes.conch :refer (programs)]))

(programs git)

(defn clone [deps cache]
  (doall
    (for [[dep repo version] deps]
      (let [out  (io/file cache (name dep))
            path (.getPath out)]
        ;; TODO: check if version still matches
        (when (not (.exists out))
          (println "Checking out:" (name dep) repo version)
          (git "clone" repo
               "--branch" version
               "--single-branch" path))
        out))))
