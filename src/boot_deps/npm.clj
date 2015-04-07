(ns boot-deps.npm
  (:require
    [me.raynes.conch :refer (programs)]))

(programs npm)

(defn install [deps]
  (doseq [[dep version] deps]
    (println (npm "install" (str (name dep) "@" version)))))
