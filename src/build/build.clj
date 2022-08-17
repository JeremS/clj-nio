(ns build
  (:require
    [fr.jeremyschoffen.mbt2.core :as mbt]))


(def lib-name 'io.github.jerems/clj-nio)

(defn latest-git-coord []
  (mbt/latest-git-coord :lib-name lib-name))


(defn make-readme []
  (mbt/generate-md-doc "README.md.prose" {:git-coord (latest-git-coord)}))


(defn generate-readme! []
  (spit "README.md" (mbt/generate-md-doc "README.md.prose" {:lib-name lib-name})))


(defn generate-docs! []
  ;; generate stuff
  (generate-readme!)
  (mbt/git-add-all!)
  (mbt/git-commit! :commit-msg "Generated docs for new release"))



(defn release! []
  (mbt/assert-clean-repo)
  (mbt/tag-release!)
  (generate-docs!))


(comment
  (println (make-readme))
  (release!))
