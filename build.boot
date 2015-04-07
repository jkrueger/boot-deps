
(set-env!
  :source-paths #{"src"}
  :dependencies '[[me.raynes/conch "0.8.0"]])

(task-options!
  pom {:project 'boot-deps
       :version "0.1.0-SNAPSHOT"})
