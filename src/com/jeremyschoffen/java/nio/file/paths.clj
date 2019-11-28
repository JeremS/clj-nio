(ns com.jeremyschoffen.java.nio.file.paths
  (:refer-clojure :exclude [resolve])
  (:require
    [com.jeremyschoffen.java.nio.internal.coercions :as coerce]
    [com.jeremyschoffen.java.nio.internal.utils :as u]
    [com.jeremyschoffen.java.nio.internal.def-helpers :as h])
  (:import
    [java.nio.file FileSystem Path WatchKey]))

(set! *warn-on-reflection* true)


(u/alias-fn coerce/path)
(u/alias-fn coerce/uri)


(h/def-binary-path-fn compare-to
  "Returns an integer comparing path to the other lexicographically."
  Integer .compareTo)


(h/def-binary-path-fn ends-with?
  "Returns true if the path ends with the other, false otherwise."
  Boolean .endsWith)


(h/def-path-fn file-name
  "Returns the name of the file or directory denoted by the path."
  Path .getFileName)


(u/defn-wn file-system
  "Returns the FileSystem located at the URI, the FileSystem used to
  create the Path, or the default FileSystem when called with no
  arguments. Passing in a FileSystem returns itself."
  {:arglists '([] [path] [uri] [fs])
   :tag FileSystem}
  ([] (coerce/file-system nil))
  ([this] (coerce/file-system this)))


(h/def-path-fn parent
  "Returns the parent of the path if it has one, nil otherwise."
  Path .getParent)


(h/def-path-fn root
  "Returns the root of the path if it has one, nil otherwise."
  Path .getRoot)


(h/def-path-fn absolute?
  "Returns if the path is absolute, false otherwise"
  Boolean .isAbsolute)


(h/def-path-fn normalize
  "Returns the path with redundant name elements eliminated."
  Path .normalize)


(h/def-path-fn register!
  "Registers the file located by the path with the watch service and
  returns a WatchKey."
  WatchKey .register
  {:additional-params [watcher events & modifiers]
   :coercions {events coerce/watch-event-kind-array
               modifiers coerce/watch-event-modifier-array}})


(h/def-binary-path-fn relativize
  "Returns a relative path between the path and other."
  Path .relativize)


(h/def-binary-path-fn resolve-path
  "Resolves the other against the path."
  Path .resolve)


(h/def-binary-path-fn resolve-sibling
  "Resolves the other against the path's parent."
  Path .resolveSibling)


(h/def-binary-path-fn starts-with?
  "Returns true if the path starts with the other, false otherwise."
  Boolean .startsWith)


(defn absolute-path
  "Returns an absolute path from a Path, URI, File, FileSystem and
  sequence of strings, or sequence of strings. See path for more
  details."
  {:arglists '([path] [uri] [file] [[strings]] [filesystem & strings] [string & strings])
   :tag Path}
  [& args]
  (.toAbsolutePath  ^Path (apply coerce/path args)))


(h/def-link-fn real-path
  "Returns the real path of an existing file according to the
  link-options."
  Path .toRealPath)



