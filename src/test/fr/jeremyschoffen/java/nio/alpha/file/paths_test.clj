(ns fr.jeremyschoffen.java.nio.alpha.file.paths-test
  (:refer-clojure :exclude [resolve name])
  (:require
    [clojure.test :refer [deftest testing]]
    [testit.core :refer :all]
    [fr.jeremyschoffen.java.nio.alpha.file.paths :as paths :refer [path]])
  (:import
    (java.nio.file FileSystems)))


(def wd (-> "." paths/path paths/absolute-path))
(def resources-dir "resources")
(def example (path wd resources-dir))
(def relative-example (paths/relativize wd example))


(deftest compare-to
  (facts
    (paths/compare-to example (path [wd "resources"])) => 0))


(deftest ends-with
  (facts
    (paths/ends-with? example resources-dir) => truthy
    (paths/ends-with? example "other-dir") => falsey))


(deftest file-name
  (fact (paths/file-name example) => (path resources-dir)))


(deftest file-system
  (let [fs1 (FileSystems/getDefault)
        fs2 (paths/file-system example)]
    (facts
      (= fs1 fs2) => true
      ;; TODO: why?
      (paths/file-system (paths/uri example)) =throws=> Exception)))


(deftest parent
  (fact (= (paths/parent example) (path wd)) => true))


(deftest root
  (facts
    (paths/root example) => (path "/")
    (paths/root relative-example) => nil))


(deftest absolute?
  (facts
    (paths/absolute? (paths/relativize wd example )) => false
    (paths/absolute? example) => true))


(deftest nomalize
  (facts
     (paths/normalize example) => (paths/absolute-path (path resources-dir))))


(deftest relativize
  (facts
    (paths/relativize wd example ) => (path resources-dir)))


(deftest resolve
  (facts
    (paths/resolve (path wd) (path resources-dir)) => example))


(deftest starts-with
  (fact (paths/starts-with? (paths/absolute-path resources-dir)
                            (paths/normalize wd))
        => true))

(deftest real-path
  (facts
    (paths/real-path example) =throws=> Exception
    (paths/real-path wd) => truthy))


;; TODO: test watch service registration.