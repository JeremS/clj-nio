(ns com.jeremyschoffen.java.nio.file.files-test
  (:refer-clojure :exclude [find])
  (:require
    [clojure.test :refer [deftest testing]]
    [testit.core :refer :all]
    [com.jeremyschoffen.java.nio.file.paths :as paths :refer [path]]
    [com.jeremyschoffen.java.nio.file.files :as files]
    [com.jeremyschoffen.java.nio.file.file-systems :as fs]
    [com.jeremyschoffen.java.nio.internal :as i])
  (:import (java.io ByteArrayInputStream)
           (java.nio.file.attribute BasicFileAttributeView PosixFileAttributeView)))

(def on-posix (-> (i/file-system)
                  (fs/supported-file-attribute-views)
                  (contains? "posix")))


(deftest copy!
  (let [temp-dir (files/create-temp-directory! "temp")
        some-text "some text!!!"
        src (files/create-temp-file "src_" nil :dir temp-dir)]

    (testing "creating the source"
      (spit src some-text)
      (fact (slurp src) => some-text))

    (testing "Copy to another path."
      (testing "On non existing file."
        (let [target1 (path temp-dir "target1_")]
          (files/copy! src target1)
          (fact (slurp target1) => some-text)))

      (testing "On existing file"
        (let [target2 (files/create-temp-file "target2_" nil :dir temp-dir)]
          (fact (files/copy! src target2) =throws=> Exception)
          (files/copy! src target2 :replace-existing)
          (fact (slurp target2) => some-text))))

    (testing "Copy an is to a path"
      (let [target3 (path temp-dir "file_3")]
        (with-open [is (-> some-text
                           str
                           (.getBytes (i/charset :utf-8))
                           ByteArrayInputStream.)]
          (files/copy! is target3)
          (fact (slurp target3) => some-text))))

    (testing "Copy a path to an os"
      (let [target4 (path temp-dir "file_4")]
        (with-open [os (i/output-stream target4)]
          (files/copy! src os))
        (fact (slurp target4) => some-text)))))


(deftest create-dir!
  (let [temp-dir (files/create-temp-directory! "temp")
        dir-1 (paths/resolve temp-dir (path "1"))
        dir-2-1 (paths/resolve temp-dir (path "1" "2"))]

    (fact (every? files/not-exists? [dir-1 dir-2-1]) => true)

    (files/create-directory! dir-1)

    (facts
      (files/exists? dir-1) => true
      (files/exists? dir-2-1) => false)

    (files/create-directories! dir-2-1)

    (fact
      (files/exists? dir-2-1) => true)))


(deftest create-delete-files-&-links!
  (let [temp-dir (files/create-temp-directory! "temp")
        some-text "text 123"
        original-path (path temp-dir "original")
        link (path temp-dir "link")
        sym-link (path temp-dir "symlink")
        all-files [original-path link sym-link]]

    (fact
      (every? files/not-exists? all-files) => true)

    (files/create-file! original-path)
    (files/create-link! link original-path)
    (files/create-symbolic-link! sym-link original-path)

    (facts
      (every? files/exists? all-files) => true
      (files/symbolic-link? sym-link) => true
      (slurp original-path) => ""
      (slurp link) => ""
      (slurp sym-link) => "")


    (spit original-path some-text)

    (facts
      (slurp original-path) => some-text
      (slurp link) => some-text
      (slurp sym-link) => some-text)

    (files/delete! original-path)

    (facts
      (files/not-exists? original-path) => true

      (files/symbolic-link? sym-link) => true
      (files/exists? sym-link)                 => false
      (files/exists? sym-link :nofollow-links) => true

      (files/exists? link) => true)

    (facts
      (slurp original-path) =throws=> Exception
      (slurp sym-link) =throws=> Exception
      (slurp link) => some-text
      (files/delete-if-exists! sym-link) => true)))


(deftest find
  (let [temp-dir (files/create-temp-directory! "temp")
        file-to-find (path temp-dir "1" "2" "toto.txt")]

    (files/create-directories! (paths/parent file-to-find))
    (files/create-file! file-to-find)

    (with-open [stream (files/find temp-dir
                                   10
                                   (fn [p _]

                                     (= "toto.txt" (str (paths/file-name p)))))]
      (let [paths (-> stream .iterator iterator-seq seq)]
        (fact (first paths) => file-to-find)))

    (with-open [stream (files/find temp-dir
                                   1
                                   (fn [p _]

                                     (= "toto.txt" (str (paths/file-name p)))))]
      (let [paths (-> stream .iterator iterator-seq seq)]
        (fact (nil? paths) => true)))))


(deftest attributes
  (let [temp-dir (files/create-temp-directory! "temp")
        basic-file (files/create-file! (path temp-dir "basic.basic"))
        attributes (files/read-attributes-into-map basic-file "*")]
    (fact (get attributes "isDirectory") => false)))


(deftest modified-time
  (let [temp-dir (files/create-temp-directory! "temp")
        basic-file (files/create-file! (path temp-dir "basic.basic"))
        base-time (files/last-modified-time basic-file)
        new-time (i/file-time)]

    (fact (compare base-time new-time) => neg?)

    (files/set-last-modified-time! basic-file new-time)

    (fact (compare (files/last-modified-time basic-file)
                   base-time)
          => 0)))


(when on-posix
  (deftest posix-permissions
    (let [temp-dir (files/create-temp-directory! "temp")
          temp-file (files/create-temp-file "temp" nil :dir temp-dir)
          perms #(files/posix-file-permisions temp-file)]
      (facts (perms) => (i/posix-file-permissions :owner-read :owner-write))
      (files/set-posix-file-permissions! temp-file :group-read)
      (facts (contains? (perms) (i/posix-file-permission :group-read)) => true))))


(deftest bools
  (let [temp-dir (files/create-temp-directory! "temp_")
        temp-file (files/create-temp-file "temp_" nil :dir temp-dir)
        hidden (files/create-temp-file ".temp_" nil :dir temp-dir)]

    (facts
      (files/directory? temp-dir) => true
      (files/directory? temp-file) => false)

    (fact (files/executable? temp-file) => false)

    (when on-posix
      (files/set-posix-file-permissions! temp-file #{:owner-read :owner-write :owner-execute})
      (fact (files/executable? temp-file) => true))

    (fact (files/hidden? temp-file) => false)
    (when on-posix
      (fact (files/hidden? hidden) => true))


    (facts
      (files/readable? temp-dir) => true
      (files/readable? temp-file) => true
      (files/readable? hidden) => true)

    (when on-posix
      (files/set-posix-file-permissions! hidden #{})
      (fact (files/readable? hidden) => false))

    (fact (every? files/regular-file? [temp-file hidden]) => true)

    ;;...
    (fact (files/same-file? temp-file temp-file) => true)

    (facts
      (files/writable? temp-file) => true
      (files/writable? hidden) => false)))

(deftest lines [])

(clojure.test/run-tests)

;(clojure.repl/doc files/file-store)
;(clojure.repl/doc files/set-posix-file-permissions!)
