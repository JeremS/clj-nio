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
    :versioning/scheme mbt-defaults/simple-scheme
    :project/author "Jeremy Schoffen"
    :versioning/major :alpha))

(def conf (-> specific-conf
              mbt-defaults/make-conf))



(comment
  (mbt-defaults/bump-tag! conf)
  (mbt-defaults/build-jar! conf)
  (mbt-defaults/install! conf)
  (mbt-core/clean! conf))
