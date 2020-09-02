(ns fr.jeremyschoffen.java.nio.alpha.build
  (:require
    [clojure.spec.test.alpha :as st]
    [fr.jeremyschoffen.mbt.alpha.core :as mbt-core]
    [fr.jeremyschoffen.mbt.alpha.default :as mbt-defaults]
    [fr.jeremyschoffen.mbt.alpha.utils :as u]
    [fr.jeremyschoffen.java.nio.alpha.docs.core :as docs]
    [clojure.spec.alpha :as s]))

(u/pseudo-nss
  git
  maven
  project
  project.license
  versioning)


(st/instrument `[mbt-core/deps-make-coord
                 mbt-defaults/versioning-tag-new-version!
                 mbt-defaults/build-jar!
                 mbt-defaults/maven-install!
                 mbt-defaults/maven-deploy!])


(def conf {::project/author "Jeremy Schoffen"
           ::maven/group-id 'fr.jeremyschoffen
           ::versioning/scheme mbt-defaults/git-distance-scheme
           ::versioning/major :alpha

           ::project/licenses [{::project.license/name "Eclipse Public License - v 2.0"
                                ::project.license/url "https://www.eclipse.org/legal/epl-v20.html"
                                ::project.license/distribution :repo
                                ::project.license/file (u/safer-path "LICENSE")}]})


(defn generate-docs! [conf]
  (-> conf
      (u/assoc-computed ::project/maven-coords mbt-core/deps-make-coord)
      (u/do-side-effect! docs/make-readme!)))

(u/spec-op generate-docs!
           :deps [mbt-core/deps-make-coord]
           :param {:req [::maven/artefact-name
                         ::maven/group-id
                         ::project/version]
                   :opt [::maven/classifier]})


(def next-version+1 (mbt-defaults/versioning-make-next-version+x 1))

(u/spec-op next-version+1
           :deps [mbt-defaults/versioning-next-version]
           :param {:req [::git/repo
                         ::versioning/scheme]
                   :opt [::versioning/bump-level
                         ::versioning/tag-base-name]})


(defn merge-version+1 [conf]
  (-> conf
      (u/assoc-computed ::versioning/version next-version+1
                        ::project/version (comp str ::versioning/version))))

(u/spec-op merge-version+1
           :deps [next-version+1]
           :param {:req [::git/repo
                         ::versioning/scheme]
                   :opt [::versioning/bump-level
                         ::versioning/tag-base-name]}
           :ret (s/keys :req [::versioning/version
                              ::project/version]))


(defn new-milestone! [conf]
  (-> conf
    (mbt-defaults/build-before-bump! (u/do-side-effect! generate-docs!))
    (u/do-side-effect! mbt-defaults/versioning-tag-new-version!)))

(u/spec-op new-milestone!
           :deps [generate-docs!
                  mbt-defaults/versioning-tag-new-version!]
           :param {:req [::git/repo
                         ::maven/artefact-name
                         ::maven/group-id
                         ::project/version
                         ::project/working-dir
                         ::versioning/tag-base-name
                         ::versioning/version]
                   :opt [::maven/classifier]})




