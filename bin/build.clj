(ns build
  (:require
    [clojure.spec.test.alpha :as spec-test]
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [fr.jeremyschoffen.mbt.alpha.core :as mbt-core]
    [fr.jeremyschoffen.mbt.alpha.default :as mbt-defaults]
    [fr.jeremyschoffen.mbt.alpha.utils :as u]))


(spec-test/instrument)


(def specific-conf
  (sorted-map
    :project/author "Jeremy Schoffen"
    :maven/group-id 'fr.jeremyschoffen
    :versioning/scheme mbt-defaults/simple-scheme
    :versioning/major :alpha))


(def conf (-> specific-conf
              mbt-defaults/make-conf))



(comment
  (mbt-defaults/bump-tag! conf)
  (mbt-defaults/build-jar! conf)
  (mbt-defaults/install! conf)
  (mbt-core/clean! conf)

  (-> conf
      (assoc :project/version "0")
      (u/side-effect! mbt-defaults/build-jar!)
      mbt-defaults/install!))
