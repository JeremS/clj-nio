(ns com.jeremyschoffen.java.nio.file.files
  (:refer-clojure :exclude [list find])
  (:require
    [com.jeremyschoffen.java.nio.internal.coercions :as coerce]
    [com.jeremyschoffen.java.nio.internal.utils :as u]
    [com.jeremyschoffen.java.nio.internal.def-helpers :as h])
  (:import
    (java.io InputStream OutputStream BufferedReader BufferedWriter)
    (java.nio.channels SeekableByteChannel)
    (java.nio.charset StandardCharsets)
    (java.nio.file DirectoryStream FileStore Files Path)
    (java.nio.file.attribute UserPrincipal FileAttributeView FileTime BasicFileAttributes PosixFilePermission)
    (java.util Map Set List)
    (java.util.stream Stream)))




(set! *warn-on-reflection* true)


(defmulti ^:private -copy! (fn [source target & _]
                             [(type source) (type target)]))


(defmethod -copy! :default [source target & _]
  (throw (IllegalArgumentException. (format "Can't copy from %s to %s" (type source) (type target)))))


(defmethod -copy! [InputStream Path]
  [^InputStream source ^Path target & copy-opts]
  (Files/copy source target
              (coerce/copy-option-array copy-opts)))


(defmethod -copy! [Path OutputStream]
  [^Path source ^OutputStream target & _]
  (Files/copy source target))


(defmethod -copy! [Path Path]
  [^Path source ^Path target & copy-opts]
  (Files/copy source target
              (coerce/copy-option-array copy-opts)))


(defn copy!
  "Copy all bytes from a file to a file, file to an output stream, or
  input stream to a file. The return type depends on the form of
  copy. Copying to or from a stream will return a long of the number
  of bytes read or written. Copying a file to another file will return
  the path to the target. If the source or target are not streams,
  they will be coerced to paths. Copy options may be included for
  configuration when writing to a file."
  [source target & copy-opts]
  (let [source (coerce/some-coercion coerce/path coerce/input-stream)
        target (coerce/some-coercion coerce/path coerce/output-stream)]
    (assert source "source must be coercible into a path or an input stream")
    (assert target "target must be coercible into a path or an output stream")
    (apply -copy! source target copy-opts)))


(h/def-create-fn create-directories!
  "Creates a directory by creating all nonexistent parent directories
  first."
  Path Files/createDirectories)


(h/def-create-fn create-directory!
  "Creates a new directory."
  Path Files/createDirectory)


(h/def-create-fn create-file!
  "Creates a new empty file."
  Path Files/createFile)


(h/def-binary-path-fn create-link!
  "Creates a new link for an existing file."
  Path Files/createLink)


(h/def-path-fn create-symbolic-link!
  "Creates a symbolic link form path to target."
  Path Files/createSymbolicLink
  {:additional-params [target & file-attributes]
   :coercions {target coerce/path
               file-attributes coerce/file-attribute-array}})


(u/defn-wn create-temp-directory
  "Creates a temporary directory with the given prefix in the given
  directory or the default temporary directory if none is provided."
  {:coercions '{dir coerce/path
                file-attrs coerce/file-attribute-array}
   :tag Path}
  [prefix & {dir        :dir
             file-attrs :file-attrs
             :or {dir        nil
                  file-attrs []}}]
  (if dir
    (Files/createTempDirectory (coerce/path dir) prefix (coerce/file-attribute-array file-attrs))
    (Files/createTempDirectory prefix (coerce/file-attribute-array file-attrs))))


(u/defn-wn create-temp-file
  "Creates a temporary file with the given prefix and suffix in the
  given directory or the default temporary directory if none is
  provided."
  {:coercions '{dir coerce/path
                file-attrs coerce/file-attribute-array}
   :tag Path}
  [prefix suffix & {dir :dir file-attrs :file-attrs
                    :or {dir nil file-attrs []}}]
  (if dir
    (Files/createTempFile (coerce/path dir) prefix suffix (coerce/file-attribute-array file-attrs))
    (Files/createTempFile prefix suffix (coerce/file-attribute-array file-attrs))))


(h/def-path-fn delete!
  "Deletes the file at path."
  nil Files/delete)


(h/def-path-fn delete-if-exists!
  "Deletes the file at path if it exists. Returns true if the file was
  deleted, false otherwise."
  Boolean Files/deleteIfExists)


(h/def-link-fn exists?
  "Returns true if the file exists, false otherwise."
  Boolean Files/exists)


(u/defn-wn find
  "Same as `java.nio.Files/find`, with `p` as anything that can coerced int a Path and
  `predicate` a clojure function that will automatically be turned into a `java.util.function.BiPredicate`."
  {:coercions '{path            coerce/path
                predicate       coerce/bi-predicate
                file-visit-opts coerce/file-visit-option-array}
   :return-generic Path
   :tag Stream}
  [path max-depth predicate & file-visit-opts]
  (Files/find (coerce/path path)
              max-depth
              (coerce/bi-predicate predicate)
              (coerce/file-visit-option-array file-visit-opts)))


(h/def-link-fn attribute
  "Returns the value of a file attribute."
  Object Files/getAttribute
  {:additional-params [attribute]})


(h/def-link-fn file-attribute-view
  "Returns a file attribute view of the given type."
  FileAttributeView Files/getFileAttributeView
  {:additional-params [attribute-view-type]})


(h/def-path-fn file-store
  "Returns the file store where the file is located."
  FileStore Files/getFileStore)


(h/def-link-fn last-modified-time
  "Returns the last modified time for the file."
  FileTime Files/getLastModifiedTime)


(h/def-link-fn owner
  "Returns the owner of the file."
  UserPrincipal Files/getOwner)


(h/def-link-fn posix-file-permisions
  "Returns a file's POSIX file permissions."
  {:return-generic PosixFilePermission}
  Set Files/getPosixFilePermissions)


(h/def-link-fn directory?
  "Returns true if the file is a directory, false otherwise."
  Boolean Files/isDirectory)


(h/def-path-fn executable?
  "Returns true if the file is executable, false otherwise."
  Boolean Files/isExecutable)


(h/def-path-fn hidden?
  "Returns true if the file is hidden, false otherwise."
  Boolean Files/isHidden)


(h/def-path-fn readable?
  "Returns true if the file is readable, false otherwise."
  Boolean Files/isReadable)


(h/def-link-fn regular-file?
  "Returns true if the file is a regular file, false otherwise."
  Boolean Files/isRegularFile)


(h/def-binary-path-fn same-file?
  "Returns true if the two paths are the same, false otherwise."
  Boolean Files/isSameFile)


(h/def-path-fn symbolic-link?
  "Returns true if the file is a symbolic link, false otherwise."
  Boolean Files/isSymbolicLink)


(h/def-path-fn writable?
  "Returns true if the file is a writable, false otherwise."
  Boolean Files/isWritable)


(h/def-java-call lines
  "Read all lines from a file as a Stream."
  {:return-generic String}
  Stream Files/lines
  {:arities [[p] [p cs]]
   :coercions {p coerce/path
               cs coerce/charset}})


(h/def-path-fn list
  "List files at a path."
  {:return-generic Path}
  Stream Files/list)


(h/def-path-fn move!
  "Move or rename a file to a target file."
  Path Files/move
  {:additional-params [target & copy-opts]
   :coercions {target coerce/path
               copy-opts coerce/copy-option-array}})


(h/def-java-call new-buffered-reader
  "Create a buffered reader from a file."
  BufferedReader Files/newBufferedReader
  {:arities [[path] [path cs]]
   :coercions {path coerce/path
               cs coerce/charset}})


(u/defn-wn new-buffered-writer
  "Creates a buffered writer"
  {:coercions '{p coerce/path
                cs coerce/charset
                open-opts coerce/open-option-array}
   :tag BufferedWriter}
  [p {cs        :char-set
      open-opts :open-opts
      :or       {cs        StandardCharsets/UTF_8
                 open-opts []}}]
  (Files/newBufferedWriter (coerce/path p)
                           (coerce/charset cs)
                           (coerce/open-option-array open-opts)))


(u/defn-wn new-byte-channel
  "Opens or creates a file, returning a seekable byte channel to access the file."
  {:coercions '{p coerce/path
                open-opts coerce/open-options
                file-attrs coerce/file-attribute-array}
   :tag SeekableByteChannel}
  [p {open-opts :open-options file-attrs :file-attrs
      :or {open-opts  #{}
           file-attrs []}}]
  (Files/newByteChannel (coerce/path p)
                        (coerce/open-options open-opts)
                        (coerce/file-attribute-array file-attrs)))


(u/defn-wn new-directory-Stream
  "Opens a directory, returning a DirectoryStream to iterate over all entries in the directory."
  {:arglists '([path]
               [path glob]
               [path dir-stream-filter])
   :return-generic Path
   :tag DirectoryStream
   :coercions '{path coerce/path
                dir-stream-filter coerce/dir-stream-filter}}
  ([p]
   (Files/newDirectoryStream (coerce/path p)))
  ([p glob-or-fn]
   (if (string? glob-or-fn)
     (Files/newDirectoryStream (coerce/path p) ^String glob-or-fn)
     (Files/newDirectoryStream (coerce/path p)  (coerce/dir-stream-filter glob-or-fn)))))


(h/def-open-fn new-input-stream
  "Opens a file, returning an input stream to read from the file."
  InputStream Files/newInputStream)


(h/def-open-fn new-ouput-stream
  "Opens or creates a file, returning an output stream that may be used to write bytes to the file."
  OutputStream Files/newOutputStream)


(h/def-link-fn not-exists?
  "Returns true if the file does not exist, false otherwise."
  Boolean Files/notExists)


(h/def-path-fn probe-content-type
  "Probes the content type of a file. "
  String Files/probeContentType)


(h/def-path-fn read-all-bytes
  "Returns the bytes from the file."
  "[B" Files/readAllBytes)


(h/def-java-call read-all-lines
  "Returns the lines of a file."
  {:return-generic String}
  List Files/readAllLines
  {:arities [[p] [p cs]]
   :coercions {p coerce/path
               cs coerce/charset}})


(u/defn-wn read-attributes-into-class
  "Reads a set of file attributes as a bulk operation."
  {:coercions '{path coerce/path
                link-opts coerce/link-option-array}
   :tag BasicFileAttributes}
  [path ^Class class link-opts]
  (Files/readAttributes (coerce/path path) class (coerce/link-option-array link-opts)))


(u/defn-wn read-attributes-into-map
  "Reads a file's attributes as a bulk operation."
  {:coercions '{path coerce/path
                link-opts coerce/link-option-array}
   :return-generic [String Object]
   :tag Map}

  [path ^String s & link-opts]
  (Files/readAttributes (coerce/path path) s (coerce/link-option-array link-opts)))


(h/def-path-fn read-symbolic-link
  "Reads the target of a symbolic link (optional operation)."
  Path Files/readSymbolicLink)


(h/def-link-fn set-attribute!
  "Sets the value of a file attribute."
  Path Files/setAttribute
  {:additional-params [attribute value]})


(h/def-path-fn set-last-modified-time!
  "Sets the last modified time of the file."
  Path Files/setLastModifiedTime
  {:additional-params [time]
   :coercions {time coerce/file-time}})


(h/def-path-fn set-owner!
  "Sets the file's owner."
  Path Files/setOwner
  {:additional-params [owner]})


(h/def-path-fn set-posix-file-permissions!
  "Sets the file's POSIX permissions."
  Path Files/setPosixFilePermissions
  {:additional-params [permissions]
   :coercions {permissions coerce/posix-file-permissions}})


(h/def-path-fn size
  "Returns the size of the file in bytes."
  Long Files/size)


(u/defn-wn walk
  "Return a Stream that is lazily populated with Path by walking the file
  tree rooted at a given starting file."
  {:coercions '{p coerce/path
                file-visit-opts coerce/file-visit-option-array}
   :return-generic Path
   :tag Stream}
  ([p]
   (walk p {}))
  ([p {d :max-depth file-visit-opts :file-visit-opts
       :or {d Integer/MAX_VALUE
            file-visit-opts []}}]
   (Files/walk (coerce/path p) d (coerce/file-visit-option-array file-visit-opts))))


(u/defn-wn walk-file-tree
  "Walks a file tree."
  {:coercions '{p coerce/path
                file-visit-opts coerce/file-visit-options}
   :tag Path}
  ([p visitor]
   (Files/walkFileTree (coerce/path p) visitor))
  ([p visitor {d :max-depth file-visit-opts :file-visit-opts
               :or {d Integer/MAX_VALUE
                    file-visit-opts #{}}}]
   (Files/walkFileTree (coerce/path p) (coerce/file-visit-options file-visit-opts) d visitor)))


(u/defn-wn write-bytes
  "Writes bytes to a file."
  {:coercions '{p coerce/path
                open-opts coerce/open-option-array}
   :tag Path}
  [p ^"[B" bytes & open-opts]
  (Files/write (coerce/path p)
               bytes
               (coerce/open-option-array open-opts)))


(u/defn-wn write
  "Write lines of text to a file."
  {:coercions '{p coerce/path
                open-opts coerce/open-option-array}
   :tag Path}
  ([p lines]
   (write p lines {}))
  ([p lines {cs  :char-set open-opts :open-opts
             :or {cs StandardCharsets/UTF_8
                  open-opts []}}]
   (Files/write (coerce/path p)
                lines
                cs
                (coerce/open-option-array open-opts))))