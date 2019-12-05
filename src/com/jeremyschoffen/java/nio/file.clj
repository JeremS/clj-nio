(ns com.jeremyschoffen.java.nio.file
  (:refer-clojure :exclude [resolve find list])
  (:require
    [com.jeremyschoffen.java.nio.internal :as i]
    [com.jeremyschoffen.java.nio.file.paths :as paths]
    [com.jeremyschoffen.java.nio.file.files :as files]
    [com.jeremyschoffen.java.nio.file.file-systems :as fs]
    [com.jeremyschoffen.java.nio.file.file-stores :as file-stores]
    [com.jeremyschoffen.java.nio.file.attribute.posix-file-permissions :as posix-perms]
    [clojure.java.io :as io]
    [clojure.edn :as edn]))


(i/alias-fn path? i/path?)

;;----------------------------------------------------------------------------------------------------------------------
;; coercions
(i/alias-fn charset i/charset)
(i/alias-fn copy-option i/copy-option)
(i/alias-fn file i/file)
(i/alias-fn file-system i/file-system)
(i/alias-fn file-time i/file-time)
(i/alias-fn file-visit-option i/file-visit-option)
(i/alias-fn file-visit-options i/file-visit-options)
(i/alias-fn input-stream i/input-stream)
(i/alias-fn link-option i/link-option)
(i/alias-fn open-option i/open-option)
(i/alias-fn open-options i/open-options)
(i/alias-fn output-stream i/output-stream)
(i/alias-fn path i/path)
(i/alias-fn posix-file-permission i/posix-file-permission)
(i/alias-fn posix-file-permissions i/posix-file-permissions)
(i/alias-fn uri i/uri)
(i/alias-fn url i/url)
(i/alias-fn watch-event-kind i/watch-event-kind)

;;----------------------------------------------------------------------------------------------------------------------
;; Paths
(i/alias-fn absolute-path paths/absolute-path)
(i/alias-fn absolute? paths/absolute?)
(i/alias-fn compare-to paths/compare-to)
(i/alias-fn ends-with? paths/ends-with?)
(i/alias-fn file-name paths/file-name)
(i/alias-fn normalize paths/normalize)
(i/alias-fn parent paths/parent)
(i/alias-fn real-path paths/real-path)
(i/alias-fn register! paths/register!)
(i/alias-fn relativize paths/relativize)
(i/alias-fn resolve paths/resolve)
(i/alias-fn resolve-sibling paths/resolve-sibling)
(i/alias-fn root paths/root)
(i/alias-fn starts-with? paths/starts-with?)

;;----------------------------------------------------------------------------------------------------------------------
;; Files
(i/alias-fn file-attribute files/attribute)
(i/alias-fn copy! files/copy!)
(i/alias-fn create-directories! files/create-directories!)
(i/alias-fn create-directory! files/create-directory!)
(i/alias-fn create-file! files/create-file!)
(i/alias-fn create-link! files/create-link!)
(i/alias-fn create-symbolic-link! files/create-symbolic-link!)
(i/alias-fn create-temp-directory! files/create-temp-directory!)
(i/alias-fn create-temp-file! files/create-temp-file!)
(i/alias-fn delete! files/delete!)
(i/alias-fn delete-if-exists! files/delete-if-exists!)
(i/alias-fn directory? files/directory?)
(i/alias-fn executable? files/executable?)
(i/alias-fn exists? files/exists?)
(i/alias-fn file-attribute-view files/file-attribute-view)
(i/alias-fn file-store files/file-store)
(i/alias-fn find files/find)
(i/alias-fn hidden? files/hidden?)
(i/alias-fn last-modified-time files/last-modified-time)
(i/alias-fn lines files/lines)
(i/alias-fn list files/list)
(i/alias-fn move! files/move!)
(i/alias-fn new-buffered-reader files/new-buffered-reader)
(i/alias-fn new-buffered-writer files/new-buffered-writer)
(i/alias-fn new-byte-channel files/new-byte-channel)
(i/alias-fn new-directory-Stream files/new-directory-Stream)
(i/alias-fn new-input-stream files/new-input-stream)
(i/alias-fn new-ouput-stream files/new-ouput-stream)
(i/alias-fn not-exists? files/not-exists?)
(i/alias-fn owner files/owner)
(i/alias-fn posix-file-permisions files/posix-file-permisions)
(i/alias-fn probe-content-type files/probe-content-type)
(i/alias-fn read-all-bytes files/read-all-bytes)
(i/alias-fn read-all-lines files/read-all-lines)
(i/alias-fn read-attributes-into-class files/read-attributes-into-class)
(i/alias-fn read-attributes-into-map files/read-attributes-into-map)
(i/alias-fn read-symbolic-link files/read-symbolic-link)
(i/alias-fn readable? files/readable?)
(i/alias-fn regular-file? files/regular-file?)
(i/alias-fn same-file? files/same-file?)
(i/alias-fn set-attribute! files/set-attribute!)
(i/alias-fn set-last-modified-time! files/set-last-modified-time!)
(i/alias-fn set-owner! files/set-owner!)
(i/alias-fn set-posix-file-permissions! files/set-posix-file-permissions!)
(i/alias-fn size files/size)
(i/alias-fn symbolic-link? files/symbolic-link?)
(i/alias-fn walk files/walk)
(i/alias-fn walk-file-tree files/walk-file-tree)
(i/alias-fn writable? files/writable?)
(i/alias-fn write files/write)
(i/alias-fn write-bytes files/write-bytes)

;;----------------------------------------------------------------------------------------------------------------------
;; FileSystems
(i/alias-fn file-stores fs/file-stores)
(i/alias-fn new-watch-service fs/new-watch-service)
(i/alias-fn open? fs/open?)
(i/alias-fn path-in-fs fs/path-in-fs)
(i/alias-fn path-matcher fs/path-matcher)
(i/alias-fn provider fs/provider)
(i/alias-fn file-system-read-only? fs/read-only?)
(i/alias-fn root-directories fs/root-directories)
(i/alias-fn separator fs/separator)
(i/alias-fn supported-file-attribute-views fs/supported-file-attribute-views)
(i/alias-fn user-principal-lookup-service fs/user-principal-lookup-service)

;;----------------------------------------------------------------------------------------------------------------------
;; FileStores
(i/alias-fn file-store-attribute file-stores/attribute)
(i/alias-fn file-store-attribute-view file-stores/file-store-attribute-view)
(i/alias-fn file-store-name file-stores/file-store-name)
(i/alias-fn file-store-type file-stores/file-store-type)
(i/alias-fn file-store-read-only? file-stores/read-only?)
(i/alias-fn supports-file-attribute-view file-stores/supports-file-attribute-view)
(i/alias-fn total-space​ file-stores/total-space​)
(i/alias-fn unallocated-space​ file-stores/unallocated-space​)
(i/alias-fn usable-space​ file-stores/usable-space​)


;;----------------------------------------------------------------------------------------------------------------------
;; Posix-permissions

(i/alias-fn as-file-attribute posix-perms/as-file-attribute)
(i/alias-fn posix-file-permission-to-string posix-perms/to-string)

;;----------------------------------------------------------------------------------------------------------------------
;; Stream util
(i/alias-fn realize i/realize)

;;----------------------------------------------------------------------------------------------------------------------
;; Goodies

(i/defn-wn ancestor?
  "Determines the first parameter is a parent (loose sense) path of the second."
  {:coercions '{parent path
                child path}}
  [parent child]
  (starts-with? (-> child absolute-path normalize)
                (-> parent absolute-path normalize)))

(comment
  (do ;; look for duplicates
    (require  '[clojure.edn :as edn])
    (require  '[clojure.java.io :as io])

    (defn make-reader []
      (-> ["." ".." "clj-nio" "src" "com" "jeremyschoffen" "java" "nio"]
          (path  "file.clj")
          slurp
          java.io.StringReader.
          java.io.PushbackReader.))

    (defn read-all []
      (let [reader (make-reader)]
        (take-while identity (repeatedly (fn [] (edn/read {:eof nil} reader))))))

    (-> (read-all)
        (->> (filter (fn [x] (-> x first (= 'i/alias-fn))))
             (group-by second)
             (vals)
             (filter (fn [x] (> (count x) 1)))))))








