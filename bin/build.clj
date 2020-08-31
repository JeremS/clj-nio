(ns build
  (:require
    [clojure.spec.test.alpha :as st]
    [fr.jeremyschoffen.mbt.alpha.core :as mbt-core]
    [fr.jeremyschoffen.mbt.alpha.default :as mbt-defaults]
    [fr.jeremyschoffen.mbt.alpha.utils :as u]
    [docs.core :as docs]))

(u/pseudo-nss
  maven
  project
  project.license
  versioning)


(st/instrument `[mbt-core/deps-make-coord
                 mbt-defaults/versioning-tag-new-version!
                 mbt-defaults/build-jar!
                 mbt-defaults/maven-install!])


(def conf (mbt-defaults/config-make
            {::project/author "Jeremy Schoffen"
             ::maven/group-id 'fr.jeremyschoffen
             ::versioning/scheme mbt-defaults/git-distance-scheme
             ::versioning/major :alpha

             ::project/licenses [{::project.license/name "Eclipse Public License - v 2.0"
                                  ::project.license/url "https://www.eclipse.org/legal/epl-v20.html"
                                  ::project.license/distribution :repo
                                  ::project.license/file (u/safer-path "LICENSE")}]}))



(defn next-version [conf]
  (let [initial (mbt-defaults/versioning-initial-version conf)
        next-v (mbt-defaults/versioning-next-version conf)]
    (-> next-v
        (cond-> (not= initial next-v)
                (update :distance inc))
        str)))


(defn generate-docs! [conf]
  (-> conf
      (u/assoc-computed ::project/maven-coords mbt-core/deps-make-coord)
      (u/do-side-effect! docs/make-readme!)))


(defn new-milestone! [conf]
  (-> conf
    (u/assoc-computed ::versioning/version next-version
                      ::project/version (comp str ::versioning/version))

    (mbt-defaults/build-before-bump! (u/do-side-effect! generate-docs!))
    (u/do-side-effect! mbt-defaults/versioning-tag-new-version!)
    (u/do-side-effect! mbt-defaults/build-jar!)
    (u/do-side-effect! mbt-defaults/maven-install!)))



(comment
  (u/record-build
    (new-milestone! conf))
  (mbt-core/clean! conf)

  (-> conf
      (assoc ::project/version "0")
      (u/side-effect! mbt-defaults/build-jar!)
      mbt-defaults/install!))
