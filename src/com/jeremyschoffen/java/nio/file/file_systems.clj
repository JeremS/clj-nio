(ns com.jeremyschoffen.java.nio.file.file-systems
  (:require
    [com.jeremyschoffen.java.nio.internal.coercions :as coerce]
    [com.jeremyschoffen.java.nio.internal.utils :as u]
    [com.jeremyschoffen.java.nio.internal.def-helpers :as h])
  (:import
    (java.nio.file WatchService FileStore PathMatcher FileSystems Path)
    (java.nio.file.attribute UserPrincipalLookupService)
    (java.nio.file.spi FileSystemProvider)
    (java.util Set)))

(u/alias-fn file-system coerce/file-system)


(h/def-fs-fn file-stores
  "Returns an object to iterate over the underlying file stores."
  {:return-generic FileStore}
  Iterable .getFileStores)


(u/defn-wn path
  "Get a Path from a FileSystem and strings."
  {:coercions '{fs file-system}
   :tag Path}
  [fs & strs]
  (apply coerce/path (file-system fs) strs))


(h/def-fs-fn path-matcher
  "Returns a PathMatcher."
  PathMatcher .getPathMatcher
  {:additional-params [syntax-and-pattern]})


(h/def-fs-fn root-directories
  "Returns an object to iterate over the paths of the root directories."
  {:return-generic Path}
  Iterable .getRootDirectories)


(h/def-fs-fn separator
  "Returns the name separator, represented as a string."
  String .getSeparator)


(h/def-fs-fn user-principal-lookup-service
  "Returns the UserPrincipalLookupService for this file system"
  UserPrincipalLookupService .getUserPrincipalLookupService)


(h/def-fs-fn open?
  "Tells whether or not this file system is open."
  Boolean .isOpen)


(h/def-fs-fn read-only?
  "Tells whether or not this file system allows only read-only access to its file stores."
  Boolean .isReadOnly)


(h/def-fs-fn new-watch-service
  "Constructs a new WatchService."
  WatchService .newWatchService)


(h/def-fs-fn provider
  "Returns the provider that created this file system."
  FileSystemProvider .provider)


(h/def-fs-fn supported-file-attribute-views
  "Returns the set of the names of the file attribute views supported by this FileSystem."
  {:return-generic String}
  Set .supportedFileAttributeViews)
