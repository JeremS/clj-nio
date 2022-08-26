(ns fr.jeremyschoffen.java.nio.alpha.file
  (:refer-clojure :exclude [resolve find list])
  (:require
    [fr.jeremyschoffen.java.nio.alpha.internal :as i]
    [fr.jeremyschoffen.java.nio.alpha.file.paths :as paths]
    [fr.jeremyschoffen.java.nio.alpha.file.files :as files]
    [fr.jeremyschoffen.java.nio.alpha.file.file-systems :as fs]
    [fr.jeremyschoffen.java.nio.alpha.file.file-stores :as file-stores]
    [fr.jeremyschoffen.java.nio.alpha.file.attribute.posix-file-permissions :as posix-perms])
  (:import
    (java.nio.file Path)))


(i/def-clone path? i/path?)
(i/def-clone url? i/url?)
(i/def-clone file-system? i/file-system?)

;;----------------------------------------------------------------------------------------------------------------------
;; coercions
(i/def-clone charset i/charset)
(i/def-clone copy-option i/copy-option)
(i/def-clone file i/file)
(i/def-clone file-system i/file-system)
(i/def-clone file-time i/file-time)
(i/def-clone file-visit-option i/file-visit-option)
(i/def-clone file-visit-options i/file-visit-options)
(i/def-clone input-stream i/input-stream)
(i/def-clone link-option i/link-option)
(i/def-clone open-option i/open-option)
(i/def-clone open-options i/open-options)
(i/def-clone output-stream i/output-stream)
(i/def-clone path i/path)
(i/def-clone posix-file-permission i/posix-file-permission)
(i/def-clone posix-file-permissions i/posix-file-permissions)
(i/def-clone uri i/uri)
(i/def-clone url i/url)
(i/def-clone watch-event-kind i/watch-event-kind)

;;----------------------------------------------------------------------------------------------------------------------
;; Paths
(i/def-clone absolute-path paths/absolute-path)
(i/def-clone absolute? paths/absolute?)
(i/def-clone compare-to paths/compare-to)
(i/def-clone ends-with? paths/ends-with?)
(i/def-clone file-name paths/file-name)
(i/def-clone normalize paths/normalize)
(i/def-clone parent paths/parent)
(i/def-clone real-path paths/real-path)
(i/def-clone register! paths/register!)
(i/def-clone relativize paths/relativize)
(i/def-clone resolve paths/resolve)
(i/def-clone resolve-sibling paths/resolve-sibling)
(i/def-clone root paths/root)
(i/def-clone starts-with? paths/starts-with?)

;;----------------------------------------------------------------------------------------------------------------------
;; Files
(i/def-clone file-attribute files/attribute)
(i/def-clone copy! files/copy!)
(i/def-clone create-directories! files/create-directories!)
(i/def-clone create-directory! files/create-directory!)
(i/def-clone create-file! files/create-file!)
(i/def-clone create-link! files/create-link!)
(i/def-clone create-symbolic-link! files/create-symbolic-link!)
(i/def-clone create-temp-directory! files/create-temp-directory!)
(i/def-clone create-temp-file! files/create-temp-file!)
(i/def-clone delete! files/delete!)
(i/def-clone delete-if-exists! files/delete-if-exists!)
(i/def-clone directory? files/directory?)
(i/def-clone executable? files/executable?)
(i/def-clone exists? files/exists?)
(i/def-clone file-attribute-view files/file-attribute-view)
(i/def-clone file-store files/file-store)
(i/def-clone find files/find)
(i/def-clone hidden? files/hidden?)
(i/def-clone last-modified-time files/last-modified-time)
(i/def-clone lines files/lines)
(i/def-clone list files/list)
(i/def-clone move! files/move!)
(i/def-clone new-buffered-reader files/new-buffered-reader)
(i/def-clone new-buffered-writer files/new-buffered-writer)
(i/def-clone new-byte-channel files/new-byte-channel)
(i/def-clone new-directory-Stream files/new-directory-Stream)
(i/def-clone new-input-stream files/new-input-stream)
(i/def-clone new-ouput-stream files/new-ouput-stream)
(i/def-clone not-exists? files/not-exists?)
(i/def-clone owner files/owner)
(i/def-clone posix-file-permisions files/posix-file-permisions)
(i/def-clone probe-content-type files/probe-content-type)
(i/def-clone read-all-bytes files/read-all-bytes)
(i/def-clone read-all-lines files/read-all-lines)
(i/def-clone read-attributes-into-class files/read-attributes-into-class)
(i/def-clone read-attributes-into-map files/read-attributes-into-map)
(i/def-clone read-symbolic-link files/read-symbolic-link)
(i/def-clone readable? files/readable?)
(i/def-clone regular-file? files/regular-file?)
(i/def-clone same-file? files/same-file?)
(i/def-clone set-attribute! files/set-attribute!)
(i/def-clone set-last-modified-time! files/set-last-modified-time!)
(i/def-clone set-owner! files/set-owner!)
(i/def-clone set-posix-file-permissions! files/set-posix-file-permissions!)
(i/def-clone size files/size)
(i/def-clone symbolic-link? files/symbolic-link?)
(i/def-clone walk files/walk)
(i/def-clone walk-file-tree files/walk-file-tree)
(i/def-clone writable? files/writable?)
(i/def-clone write files/write)
(i/def-clone write-bytes files/write-bytes)

;;----------------------------------------------------------------------------------------------------------------------
;; FileSystems
(i/def-clone file-stores fs/file-stores)
(i/def-clone new-watch-service fs/new-watch-service)
(i/def-clone open? fs/open?)
(i/def-clone path-in-fs fs/path-in-fs)
(i/def-clone path-matcher fs/path-matcher)
(i/def-clone provider fs/provider)
(i/def-clone file-system-read-only? fs/read-only?)
(i/def-clone root-directories fs/root-directories)
(i/def-clone separator fs/separator)
(i/def-clone supported-file-attribute-views fs/supported-file-attribute-views)
(i/def-clone user-principal-lookup-service fs/user-principal-lookup-service)

;;----------------------------------------------------------------------------------------------------------------------
;; FileStores
(i/def-clone file-store-attribute file-stores/attribute)
(i/def-clone file-store-attribute-view file-stores/file-store-attribute-view)
(i/def-clone file-store-name file-stores/file-store-name)
(i/def-clone file-store-type file-stores/file-store-type)
(i/def-clone file-store-read-only? file-stores/read-only?)
(i/def-clone supports-file-attribute-view file-stores/supports-file-attribute-view)
(i/def-clone total-space file-stores/total-space)
(i/def-clone unallocated-space file-stores/unallocated-space)
(i/def-clone usable-space file-stores/usable-space)


;;----------------------------------------------------------------------------------------------------------------------
;; Posix-permissions

(i/def-clone as-file-attribute posix-perms/as-file-attribute)
(i/def-clone posix-file-permission-to-string posix-perms/to-string)

;;----------------------------------------------------------------------------------------------------------------------
;; Stream util
(i/def-clone realize i/realize)

;;----------------------------------------------------------------------------------------------------------------------
;; Goodies

(i/defn-wn canonical-path
  "Returns a canonical path."
  {:tag Path
   :coercions '{p path}}
  [p]
  (-> p path file .getCanonicalPath path))


(i/defn-wn ancestor?
  "Determines the first parameter is a parent (loose sense) path of the second."
  {:coercions '{parent path
                child path}}
  [parent child]
  (starts-with? (-> child absolute-path normalize)
                (-> parent absolute-path normalize)))

(i/defn-wn file-extention
  {:coercions '{parent path}}
  [p]
  (let [p (path p)
        n (str (file-name p))
        i (.lastIndexOf n ".")]
    (if (< i 0)
      nil
      (.substring n (inc i)))))


