(ns build
  (:require
    [clojure.spec.test.alpha :as spec-test]
    [com.jeremyschoffen.java.nio.alpha.file :as fs]
    [com.jeremyschoffen.mbt.alpha.core :as mbt-core]
    [com.jeremyschoffen.mbt.alpha.default :as mbt-defaults]
    [com.jeremyschoffen.mbt.alpha.utils :as u]))


(spec-test/instrument)

(def specific-conf
  (sorted-map
    :project/author "Jeremy Schoffen"
    :maven/group-id 'com.jeremyschoffen
    :versioning/scheme mbt-defaults/simple-scheme
    :versioning/major :alpha))

(def conf (-> specific-conf
              mbt-defaults/make-conf))



(comment
  (mbt-defaults/bump-tag! conf)

  (mbt-defaults/build-jar! conf)

  (-> conf
      (assoc :maven.install/dir (u/safer-path "target" "local"))
      mbt-defaults/install!)

  (mbt-core/clean! conf))
