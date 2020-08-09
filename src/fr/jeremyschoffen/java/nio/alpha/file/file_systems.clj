(ns fr.jeremyschoffen.java.nio.alpha.file.file-systems
  (:require
    [fr.jeremyschoffen.java.nio.alpha.internal :as i])
  (:import
    (java.nio.file WatchService FileStore PathMatcher Path)
    (java.nio.file.attribute UserPrincipalLookupService)
    (java.nio.file.spi FileSystemProvider)
    (java.util Set)))


(i/def-clone file-system i/file-system)


(i/def-fs-fn file-stores
  "Returns an object to iterate over the underlying file stores."
  {:return-generic FileStore}
  Iterable .getFileStores)


(i/defn-wn path-in-fs
  "Get a Path from a FileSystem and strings."
  {:coercions '{fs file-system}
   :tag Path}
  [fs & strs]
  (apply i/path (file-system fs) strs))


(i/def-fs-fn path-matcher
  "Returns a PathMatcher."
  PathMatcher .getPathMatcher
  {:additional-params [syntax-and-pattern]})


(i/def-fs-fn root-directories
  "Returns an object to iterate over the paths of the root directories."
  {:return-generic Path}
  Iterable .getRootDirectories)


(i/def-fs-fn separator
  "Returns the name separator, represented as a string."
  String .getSeparator)


(i/def-fs-fn user-principal-lookup-service
  "Returns the UserPrincipalLookupService for this file system"
  UserPrincipalLookupService .getUserPrincipalLookupService)


(i/def-fs-fn open?
  "Tells whether or not this file system is open."
  Boolean .isOpen)


(i/def-fs-fn read-only?
  "Tells whether or not this file system allows only read-only access to its file stores."
  Boolean .isReadOnly)


(i/def-fs-fn new-watch-service
  "Constructs a new WatchService."
  WatchService .newWatchService)


(i/def-fs-fn provider
  "Returns the provider that created this file system."
  FileSystemProvider .provider)


(i/def-fs-fn supported-file-attribute-views
  "Returns the set of the names of the file attribute views supported by this FileSystem."
  {:return-generic String}
  Set .supportedFileAttributeViews)
