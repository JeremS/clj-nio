(ns com.jeremyschoffen.java.nio.alpha.file.paths
  (:refer-clojure :exclude [resolve name])
  (:require
    [com.jeremyschoffen.java.nio.alpha.internal :as i])
  (:import
    [java.nio.file FileSystem Path WatchKey]))

(i/alias-fn path i/path)
(i/alias-fn uri i/uri)


(i/def-binary-path-fn compare-to
  "Returns an integer comparing path to the other lexicographically."
  Integer .compareTo)


(i/def-binary-path-fn ends-with?
  "Returns true if the path ends with the other, false otherwise."
  Boolean .endsWith)


(i/def-path-fn file-name
  "Returns the name of the file or directory denoted by the path."
  Path .getFileName)


(i/defn-wn file-system
  "Returns the FileSystem located at the URI, the FileSystem used to
  create the Path, or the default FileSystem when called with no
  arguments. Passing in a FileSystem returns itself."
  {:arglists '([] [path] [uri] [fs])
   :tag FileSystem}
  ([] (i/file-system nil))
  ([this] (i/file-system this)))


(i/def-path-fn parent
  "Returns the parent of the path if it has one, nil otherwise."
  Path .getParent)


(i/def-path-fn root
  "Returns the root of the path if it has one, nil otherwise."
  Path .getRoot)


(i/def-path-fn absolute?
  "Returns if the path is absolute, false otherwise"
  Boolean .isAbsolute)


(i/def-path-fn normalize
  "Returns the path with redundant name elements eliminated."
  Path .normalize)


(i/def-path-fn register!
  "Registers the file located by the path with the watch service and
  returns a WatchKey."
  WatchKey .register
  {:additional-params [watcher events & modifiers]
   :coercions {events    i/watch-event-kind-array
               modifiers i/watch-event-modifier-array}})


(i/def-binary-path-fn relativize
  "Returns a relative path between the path and other."
  Path .relativize)


(i/def-binary-path-fn resolve
  "Resolves the other against the path."
  Path .resolve)


(i/def-binary-path-fn resolve-sibling
  "Resolves the other against the path's parent."
  Path .resolveSibling)


(i/def-binary-path-fn starts-with?
  "Returns true if the path starts with the other, false otherwise."
  Boolean .startsWith)


(i/defn-wn absolute-path
  "Returns an absolute path from a Path, URI, File, FileSystem and
  sequence of strings, or sequence of strings. See path for more
  details."
  {:arglists '([path] [uri] [file] [[strings]] [filesystem & strings] [string & strings])
   :tag Path}
  [& args]
  (.toAbsolutePath ^Path (apply path args)))


(i/def-link-fn real-path
  "Returns the real path of an existing file according to the
  link-options."
  Path .toRealPath)



