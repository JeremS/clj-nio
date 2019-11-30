(ns com.jeremyschoffen.java.nio.file.files
  (:refer-clojure :exclude [list find])
  (:require
    [com.jeremyschoffen.java.nio.internal :as i])
  (:import
    (java.io InputStream OutputStream BufferedReader BufferedWriter)
    (java.nio.channels SeekableByteChannel)
    (java.nio.charset StandardCharsets)
    (java.nio.file DirectoryStream FileStore Files Path)
    (java.nio.file.attribute UserPrincipal FileAttributeView FileTime BasicFileAttributes PosixFilePermission)
    (java.util Map Set List)
    (java.util.stream Stream)))


(defmulti ^:private -copy! (fn [source target & _]
                             [(type source) (type target)]))


(defmethod -copy! :default [source target & _]
  (throw (IllegalArgumentException. (format "Can't copy from %s to %s" (type source) (type target)))))


(defmethod -copy! [InputStream Path]
  [^InputStream source ^Path target & copy-opts]
  (Files/copy source target
              (i/copy-option-array copy-opts)))


(defmethod -copy! [Path OutputStream]
  [^Path source ^OutputStream target & _]
  (Files/copy source target))


(defmethod -copy! [Path Path]
  [^Path source ^Path target & copy-opts]
  (Files/copy source target
              (i/copy-option-array copy-opts)))


(i/defn-wn copy!
  "Copy all bytes from a file to a file, file to an output stream, or
  input stream to a file. The return type depends on the form of
  copy. Copying to or from a stream will return a long of the number
  of bytes read or written. Copying a file to another file will return
  the path to the target. If the source or target are not streams,
  they will be coerced to paths. Copy options may be included for
  configuration when writing to a file."
  {:arglists '([path os] [is path & copy-opts] [path path & copy-opts])
   :coercions '{source (i/some-coercion i/path i/input-stream)
                target (i/some-coercion i/path i/output-stream)}}
  [source target & copy-opts]
  (let [source (i/some-coercion source i/path i/input-stream)
        target (i/some-coercion target i/path i/output-stream)]
    (assert source "source must be coercible into a path or an input stream")
    (assert target "target must be coercible into a path or an output stream")
    (apply -copy! source target copy-opts)))


(i/def-create-fn create-directories!
  "Creates a directory by creating all nonexistent parent directories
  first."
  Path Files/createDirectories)


(i/def-create-fn create-directory!
  "Creates a new directory."
  Path Files/createDirectory)


(i/def-create-fn create-file!
  "Creates a new empty file."
  Path Files/createFile)


(i/def-binary-path-fn create-link!
  "Creates a new link for an existing file."
  {:arglists '([link existing])}
  Path Files/createLink)


(i/def-path-fn create-symbolic-link!
  "Creates a symbolic link form path to target."
  Path Files/createSymbolicLink
  {:additional-params [target & file-attributes]
   :coercions {target i/path
               file-attributes i/file-attribute-array}})


(i/defn-wn create-temp-directory!
  "Creates a temporary directory with the given prefix in the given
  directory or the default temporary directory if none is provided."
  {:coercions '{dir i/path
                file-attrs i/file-attribute-array}
   :tag Path}
  [prefix & {dir :dir file-attrs :file-attrs
             :or {dir nil
                  file-attrs []}}]
  (if dir
    (Files/createTempDirectory (i/path dir) prefix (i/file-attribute-array file-attrs))
    (Files/createTempDirectory prefix (i/file-attribute-array file-attrs))))


(i/defn-wn create-temp-file!
  "Creates a temporary file with the given prefix and suffix in the
  given directory or the default temporary directory if none is
  provided."
  {:coercions '{dir i/path
                file-attrs i/file-attribute-array}
   :tag Path}
  [prefix suffix & {dir :dir file-attrs :file-attrs
                    :or {dir nil file-attrs []}}]
  (if dir
    (Files/createTempFile (i/path dir) prefix suffix (i/file-attribute-array file-attrs))
    (Files/createTempFile prefix suffix (i/file-attribute-array file-attrs))))


(i/def-path-fn delete!
  "Deletes the file at path."
  nil Files/delete)


(i/def-path-fn delete-if-exists!
  "Deletes the file at path if it exists. Returns true if the file was
  deleted, false otherwise."
  Boolean Files/deleteIfExists)


(i/def-link-fn exists?
  "Returns true if the file exists, false otherwise."
  Boolean Files/exists)


(i/defn-wn find
  "Same as `java.nio.Files/find`, with `p` as anything that can coerced int a Path and
  `predicate` a clojure function that will automatically be turned into a
  `java.util.function.BiPredicate<Path,BasicFileAttributes>`.
  "
  {:coercions '{path            i/path
                predicate       i/bi-predicate
                file-visit-opts i/file-visit-option-array}
   :return-generic Path
   :tag Stream}
  [path max-depth predicate & file-visit-opts]
  (Files/find (i/path path)
              max-depth
              (i/bi-predicate predicate)
              (i/file-visit-option-array file-visit-opts)))


(i/def-link-fn attribute
  "Returns the value of a file attribute."
  Object Files/getAttribute
  {:additional-params [attribute]})


(i/def-link-fn file-attribute-view
  "Returns a file attribute view of the given type."
  FileAttributeView Files/getFileAttributeView
  {:additional-params [attribute-view-type]})


(i/def-path-fn file-store
  "Returns the file store where the file is located."
  FileStore Files/getFileStore)


(i/def-link-fn last-modified-time
  "Returns the last modified time for the file."
  FileTime Files/getLastModifiedTime)


(i/def-link-fn owner
  "Returns the owner of the file."
  UserPrincipal Files/getOwner)


(i/def-link-fn posix-file-permisions
  "Returns a file's POSIX file permissions."
  {:return-generic PosixFilePermission}
  Set Files/getPosixFilePermissions)


(i/def-link-fn directory?
  "Returns true if the file is a directory, false otherwise."
  Boolean Files/isDirectory)


(i/def-path-fn executable?
  "Returns true if the file is executable, false otherwise."
  Boolean Files/isExecutable)


(i/def-path-fn hidden?
  "Returns true if the file is hidden, false otherwise."
  Boolean Files/isHidden)


(i/def-path-fn readable?
  "Returns true if the file is readable, false otherwise."
  Boolean Files/isReadable)


(i/def-link-fn regular-file?
  "Returns true if the file is a regular file, false otherwise."
  Boolean Files/isRegularFile)


(i/def-binary-path-fn same-file?
  "Returns true if the two paths are the same, false otherwise."
  Boolean Files/isSameFile)


(i/def-path-fn symbolic-link?
  "Returns true if the file is a symbolic link, false otherwise."
  Boolean Files/isSymbolicLink)


(i/def-path-fn writable?
  "Returns true if the file is a writable, false otherwise."
  Boolean Files/isWritable)


(i/def-java-call lines
  "Read all lines from a file as a Stream."
  {:return-generic String}
  Stream Files/lines
  {:arities [[p] [p cs]]
   :coercions {p i/path
               cs i/charset}})


(i/def-path-fn list
  "List files at a path."
  {:return-generic Path}
  Stream Files/list)


(i/def-path-fn move!
  "Move or rename a file to a target file."
  Path Files/move
  {:additional-params [target & copy-opts]
   :coercions {target i/path
               copy-opts i/copy-option-array}})


(i/def-java-call new-buffered-reader
  "Create a buffered reader from a file."
  {:since 8}
  BufferedReader Files/newBufferedReader
  {:arities [[path] [path cs]]
   :coercions {path i/path
               cs i/charset}})


(i/defn-wn new-buffered-writer
  "Creates a buffered writer"
  {:since 8
   :coercions '{p i/path
                cs i/charset
                open-opts i/open-option-array}
   :tag BufferedWriter}
  [p {cs        :char-set
      open-opts :open-opts
      :or       {cs        StandardCharsets/UTF_8
                 open-opts []}}]
  (Files/newBufferedWriter (i/path p)
                           (i/charset cs)
                           (i/open-option-array open-opts)))


(i/defn-wn new-byte-channel
  "Opens or creates a file, returning a seekable byte channel to access the file."
  {:coercions '{p i/path
                open-opts i/open-options
                file-attrs i/file-attribute-array}
   :tag SeekableByteChannel}
  [p {open-opts :open-options file-attrs :file-attrs
      :or {open-opts  #{}
           file-attrs []}}]
  (Files/newByteChannel (i/path p)
                        (i/open-options open-opts)
                        (i/file-attribute-array file-attrs)))


(i/defn-wn new-directory-Stream
  "Opens a directory, returning a DirectoryStream to iterate over all entries in the directory."
  {:arglists '([path]
               [path glob]
               [path dir-stream-filter])
   :return-generic Path
   :tag DirectoryStream
   :coercions '{path i/path
                dir-stream-filter i/dir-stream-filter}}
  ([p]
   (Files/newDirectoryStream (i/path p)))
  ([p glob-or-fn]
   (if (string? glob-or-fn)
     (Files/newDirectoryStream (i/path p) ^String glob-or-fn)
     (Files/newDirectoryStream (i/path p)  (i/dir-stream-filter glob-or-fn)))))


(i/def-open-fn new-input-stream
  "Opens a file, returning an input stream to read from the file."
  InputStream Files/newInputStream)


(i/def-open-fn new-ouput-stream
  "Opens or creates a file, returning an output stream that may be used to write bytes to the file."
  OutputStream Files/newOutputStream)


(i/def-link-fn not-exists?
  "Returns true if the file does not exist, false otherwise."
  Boolean Files/notExists)


(i/def-path-fn probe-content-type
  "Probes the content type of a file. "
  String Files/probeContentType)


(i/def-path-fn read-all-bytes
  "Returns the bytes from the file."
  "[B" Files/readAllBytes)


(i/def-java-call read-all-lines
  "Returns the lines of a file."
  {:since 8
   :return-generic String}
  List Files/readAllLines
  {:arities [[p] [p cs]]
   :coercions {p i/path
               cs i/charset}})


(i/defn-wn read-attributes-into-class
  "Reads a set of file attributes as a bulk operation."
  {:coercions '{path i/path
                link-opts i/link-option-array}
   :tag BasicFileAttributes}
  [path ^Class class link-opts]
  (Files/readAttributes (i/path path) class (i/link-option-array link-opts)))


(i/defn-wn read-attributes-into-map
  "Reads a file's attributes as a bulk operation."
  {:coercions '{path i/path
                link-opts i/link-option-array}
   :return-generic [String Object]
   :tag Map}

  [path ^String s & link-opts]
  (Files/readAttributes (i/path path) s (i/link-option-array link-opts)))


(i/def-path-fn read-symbolic-link
  "Reads the target of a symbolic link (optional operation)."
  Path Files/readSymbolicLink)


(i/def-link-fn set-attribute!
  "Sets the value of a file attribute."
  Path Files/setAttribute
  {:additional-params [attribute value]})


(i/def-path-fn set-last-modified-time!
  "Sets the last modified time of the file."
  Path Files/setLastModifiedTime
  {:additional-params [time]
   :coercions {time i/file-time}})


(i/def-path-fn set-owner!
  "Sets the file's owner."
  Path Files/setOwner
  {:additional-params [owner]})


(i/def-path-fn set-posix-file-permissions!
  "Sets the file's POSIX permissions."
  Path Files/setPosixFilePermissions
  {:additional-params [permissions]
   :coercions {permissions i/posix-file-permissions}})


(i/def-path-fn size
  "Returns the size of the file in bytes."
  Long Files/size)


(i/defn-wn walk
  "Return a Stream that is lazily populated with Path by walking the file
  tree rooted at a given starting file."
  {:coercions '{p i/path
                file-visit-opts i/file-visit-option-array}
   :return-generic Path
   :tag Stream}
  ([p]
   (walk p {}))
  ([p {d :max-depth file-visit-opts :file-visit-opts
       :or {d Integer/MAX_VALUE
            file-visit-opts []}}]
   (Files/walk (i/path p) d (i/file-visit-option-array file-visit-opts))))


(i/defn-wn walk-file-tree
  "Walks a file tree."
  {:coercions '{p i/path
                file-visit-opts i/file-visit-options}
   :tag Path}
  ([p visitor]
   (Files/walkFileTree (i/path p) visitor))
  ([p visitor {d :max-depth file-visit-opts :file-visit-opts
               :or {d Integer/MAX_VALUE
                    file-visit-opts #{}}}]
   (Files/walkFileTree (i/path p) (i/file-visit-options file-visit-opts) d visitor)))


(i/defn-wn write-bytes
  "Writes bytes to a file."
  {:coercions '{p i/path
                open-opts i/open-option-array}
   :tag Path}
  [p ^"[B" bytes & open-opts]
  (Files/write (i/path p)
               bytes
               (i/open-option-array open-opts)))


(i/defn-wn write
  "Write lines of text to a file. `lines` must be a Iterable<? extends CharSequence>"
  {:since 8
   :coercions '{p i/path
                cs i/charset
                open-opts i/open-option-array}
   :tag Path}
  ([p lines]
   (write p lines {}))
  ([p lines {cs  :char-set open-opts :open-opts
             :or {cs StandardCharsets/UTF_8
                  open-opts []}}]
   (Files/write (i/path p)
                lines
                (i/charset cs)
                (i/open-option-array open-opts))))