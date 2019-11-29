(ns com.jeremyschoffen.java.nio.internal.coercions
  (:require
    [clojure.spec.alpha :as s]
    [clojure.java.io :as io]
    [com.jeremyschoffen.java.nio.internal.utils :as u])
  (:import
    (clojure.lang Sequential Keyword)
    (java.io File InputStream OutputStream)
    (java.lang ClassLoader)
    (java.net URI URL)
    (java.nio.charset Charset StandardCharsets)
    (java.nio.file FileSystems FileSystem FileStore
                   Paths Path
                   DirectoryStream$Filter
                   CopyOption StandardCopyOption
                   OpenOption StandardOpenOption
                   FileVisitOption LinkOption
                   StandardWatchEventKinds WatchEvent$Kind WatchEvent$Modifier)
    (java.nio.file.attribute FileAttribute PosixFilePermissions PosixFilePermission FileTime)
    (java.time Instant)
    (java.util  Date Map Set)
    (java.util.function BiPredicate)))


(defn bi-predicate
  "Reifies a java.util.function.BiPredicate.
  `p` is a function that takes 2 arguments..."
  [p]
  (reify BiPredicate
    (test [_ u v] (p u v))

    (and [_ other-p]
      (bi-predicate
        (fn [u v]
          (and (p u v)
               (other-p u v)
               true))))

    (or [_ other-p]
      (bi-predicate
        (fn [u v]
          (or (p u v)
              (other-p u v)
              false))))

    (negate [_]
      (bi-predicate (complement p)))))


(defn dir-stream-filter [f]
  {:tag DirectoryStream$Filter}
  "Makes a DirectoryStreamFileter from a clojure fn."
  (reify DirectoryStream$Filter
    (accept [_ entry]
      (f entry))))


;;----------------------------------------------------------------------------------------------------------------------
;; Utils
;;----------------------------------------------------------------------------------------------------------------------
(s/def ::def-u-coercion-args (s/cat :name symbol?
                                    :tag symbol?
                                    :coerce-fn symbol?
                                    :opts (s/? map?)))


(defmacro ^:private def-u-coercion [& args]
  (let [{:keys [name tag coerce-fn]
         kws->v :coercions/keywords} (u/parse-params ::def-u-coercion-args args)
        docstring (format "Coerce the value `x` into the type: %s." (resolve tag))
        attr-map (cond-> `{:arglists '([x])
                           :tag      ~tag}
                         kws->v (assoc :coercions/keywords kws->v))]
    `(u/defn-wn ~name
       ~docstring
       ~attr-map
       [x#]
       (~coerce-fn x#))))


(defn- throw-failed-coercion [type v]
  (-> (format "Cannot coerce %s into: %s" type v)
      IllegalArgumentException.
      throw))

(defn try-coerce [coerce & args]
  (try
    (apply coerce args)
    (catch Exception _ nil)))


(defn coerce [v coercion]
  (if (class? coercion)
    (when (isa? (type v) coercion)
      v)
    (try-coerce coercion v)))


(defn some-coercion [v & coercions]
  (some (fn [coercion]
          (coerce v coercion))
        coercions))

(defn- flatten-even-sets [x]
  (let [node? (some-fn sequential? set?)]
    (remove node?
            (rest (tree-seq node? seq x)))))


(defn coerce-many [coercion coll]
  (into #{} (map coercion) (flatten-even-sets coll)))

;;----------------------------------------------------------------------------------------------------------------------
;; Paths
;;----------------------------------------------------------------------------------------------------------------------
(defprotocol UnaryPathBuilder
  (-to-u-path [this]))


(defprotocol NaryPathBuilder
  (-to-n-path [this more]))


(defn- path? [x] (isa? x Path))

(declare path)

(extend-protocol UnaryPathBuilder
  Path
  (-to-u-path [this] this)

  String
  (-to-u-path [this] (Paths/get this (make-array String 0)))

  File
  (-to-u-path [this] (.toPath this))

  URI
  (-to-u-path [this] (Paths/get this))

  URL
  (-to-u-path [this] (-to-u-path (.toURI this)))

  Sequential
  (-to-u-path [this]
    (let [sanitized (into [] (comp (map -to-u-path) (map str)) this)]
      (apply path sanitized))))


(extend-protocol NaryPathBuilder
  Path
  (-to-n-path [this more]
    (.resolve ^Path this (str (-to-u-path more))))

  String
  (-to-n-path [this more]
    (assert (every? string? more))
    (Paths/get this (into-array String more)))

  FileSystem
  (-to-n-path [this more]
    (assert (every? string? more))
    (let [[s & r] more]
      (.getPath this s (into-array String r)))))


(u/defn-wn path
  "Returns a Path from a Path, URI, File, FileSystem and sequence of
  strings, or sequence of strings. This will not accept paths in place
  of strings for variadic usage because the behavior is not well
  defined. Consider using resolve-path."
  {:arglists '([path] [uri] [file] [[strings]] [filesystem & strings] [string & strings])
   :tag Path}
  ([p]
   (-to-u-path p))
  ([p & more]
   (-to-n-path p more)))

;;----------------------------------------------------------------------------------------------------------------------
;; URI
;;----------------------------------------------------------------------------------------------------------------------

(u/defn-wn uri
  "Transforms anything that can be a path into a uri"
  {:arglists '([path] [uri] [file] [[strings]] [filesystem & strings] [string & strings])
   :tag URI}
  ([x]
   (.toUri (path x)))
  ([x & xs]
   (.toUri ^Path (apply path x xs))))


;;----------------------------------------------------------------------------------------------------------------------
;; clojure.java.io
;;----------------------------------------------------------------------------------------------------------------------
(extend-protocol io/Coercions
  Path
  (as-file [this] (.toFile this))
  (as-url [this] (io/as-url (.toUri this))))


(def-u-coercion file File io/as-file)
(def-u-coercion url URL io/as-url)


(extend Path
  io/IOFactory
  (assoc io/default-streams-impl
    :make-input-stream (fn [^Path x opts] (io/make-input-stream (uri x) opts))
    :make-output-stream (fn [^Path x opts] (io/make-output-stream (uri x) opts))))


(u/defn-wn input-stream
  "Delegates to `clojure.java.io/input-stream`"
  {:tag InputStream}
  [x & opts]
  (apply io/input-stream x opts))


(u/defn-wn output-stream
  "Delegates to `clojure.java.io/output-stream`"
  {:tag OutputStream}
  [x & opts]
  (apply io/output-stream x opts))

;;----------------------------------------------------------------------------------------------------------------------
;; FileSystem
;;----------------------------------------------------------------------------------------------------------------------
(defprotocol FileSystemBuilder
  (-to-file-system [this]))


(extend-protocol FileSystemBuilder
  nil
  (-to-file-system [_] (FileSystems/getDefault))

  FileSystem
  (-to-file-system [this] this)

  Path
  (-to-file-system [this] (.getFileSystem this))

  URI
  (-to-file-system [this]
    (FileSystems/getFileSystem this))

  Object
  (-to-file-system [this]
    (if-let [res (try-coerce uri this)]
      res
      (throw-failed-coercion FileSystem this))))


(def-u-coercion -file-system FileSystem -to-file-system)


(defmulti ^:private -new-file-system (fn [arg1 arg2]
                                       [(type arg1) (type arg2)]))


(defmethod -new-file-system :default [arg1 arg2]
  (throw (IllegalArgumentException. (format "Can't create a filesystem with %s to %s" (type arg1) (type arg2)))))


(defmethod -new-file-system [URI Map]
  [^URI uri ^Map m]
  (FileSystems/newFileSystem uri m))


(defmethod -new-file-system [Path ClassLoader]
  [^Path p ^ClassLoader c]
  (FileSystems/newFileSystem p c))



(u/defn-wn file-system
  "This function recreates the whole java.nio.file.FileSystems functionnality in one function.

  The arity-0 or arity-1 with nil behave as FileSystems/getDefault.

  The other arity-1 variants behave as such:
   - [fs]: returns the file system
   - [path] : get the file file system with (.getFileSystem (coerce/path path))
   - [uri] : uses the FileSystems/new static method on the corced parameter with the uri coercion.

  The arity-2 and arity-3 variants call FileSystem/new.
  "
  {:coercions '{fs -file-system
                path -file-system
                uri -file-system}
   :arglists '([]
               [nil] [fs] [path] [uri]
               [uri env] [path class-loader]
               [uri env classloader])
   :tag FileSystem}
  ([]
   (FileSystems/getDefault))
  ([something]
   (-file-system something))
  ([uri-or-path env-or-classloader]
   (if (isa? (type env-or-classloader) ClassLoader)
     (-new-file-system (uri uri-or-path) env-or-classloader)
     (-new-file-system (path uri-or-path) env-or-classloader)))
  ([uri env classloader]
   (FileSystems/newFileSystem (uri uri) env classloader)))

;;----------------------------------------------------------------------------------------------------------------------
;; FileTime
;;----------------------------------------------------------------------------------------------------------------------
(defprotocol FileTimeBuilder
  (-to-file-time [this]))

(extend-protocol FileTimeBuilder
  Long
  (-to-file-time [this]
    (FileTime/from this))

  Instant
  (-to-file-time [this]
    (FileTime/from this))

  Date
  (-to-file-time [this]
    (-to-file-time (.toInstant this)))

  FileTime
  (-to-file-time [this] this))


(def-u-coercion file-time* FileTime -to-file-time)

(u/defn-wn file-time
  "Coerce the value `x` into the type: class java.nio.file.attribute.FileTime
  If called with no argument returns `(file-time (Date.))`"
  {:tag FileTime}
  ([]
   (file-time* (Date.)))
  ([x]
   (file-time* x)))

;;----------------------------------------------------------------------------------------------------------------------
;; FileStore
;;----------------------------------------------------------------------------------------------------------------------
(defprotocol FileStoreBuilder
  (-to-file-store [this]))

(extend-protocol FileStoreBuilder
  FileStore
  (-to-file-store [this] this))

(def-u-coercion file-store FileStore -to-file-store)
;;----------------------------------------------------------------------------------------------------------------------
;; Keyword coercion helpers
;;----------------------------------------------------------------------------------------------------------------------
(defn- throw-keyword-not-found [type kw]
  (-> (format "No %s for keyword: %s" type kw)
      IllegalArgumentException.
      throw))


(defn make-kw->something-coercion [type coercion-table]
  (fn [kw]
    (or (get coercion-table kw)
        (throw-keyword-not-found type kw))))


(defmacro def-kw-coercion [coercion-name result-type coercion-table]
  (let [protocol-name (-> result-type
                          name
                          (str "Builder")
                          symbol)
        protocol-fn-name (->> coercion-name
                              name
                              (str "-to-")
                              symbol)
        kw->type-name (symbol (str "kw->" coercion-name))]
    `(do
       (def ~kw->type-name
         (make-kw->something-coercion ~result-type ~coercion-table))


       (defprotocol ~protocol-name
         (~protocol-fn-name [this#]))

       (extend-protocol ~protocol-name
         ~result-type
         (~protocol-fn-name [this#] this#)

         Keyword
         (~protocol-fn-name [this#]
           (~kw->type-name this#)))

       (def-u-coercion ~coercion-name
         ~result-type
         ~protocol-fn-name
         {:coercions/keywords '~coercion-table}))))


;;----------------------------------------------------------------------------------------------------------------------
;; Posix Permissions
;;----------------------------------------------------------------------------------------------------------------------
(def-kw-coercion posix-file-permission PosixFilePermission
  {:owner-read    PosixFilePermission/OWNER_READ
   :owner-write   PosixFilePermission/OWNER_WRITE
   :owner-execute PosixFilePermission/OWNER_EXECUTE
   :group-read    PosixFilePermission/GROUP_READ
   :group-write   PosixFilePermission/GROUP_WRITE
   :group-execute PosixFilePermission/GROUP_EXECUTE
   :other-read    PosixFilePermission/OTHERS_READ
   :other-write   PosixFilePermission/OTHERS_WRITE
   :other-execute PosixFilePermission/OTHERS_EXECUTE})


(u/defn-wn posix-file-permissions
  "Coerce input into a set of PosixFilePermission."
  {:return-generic PosixFilePermission
   :tag Set}
  ([x]
   (cond
     (string? x) (PosixFilePermissions/fromString x)
     ((some-fn sequential? set?) x) (coerce-many posix-file-permission x)
     :else #{(posix-file-permission x)}))
  ([x & xs]
   (posix-file-permissions (cons x xs))))

;;----------------------------------------------------------------------------------------------------------------------
;; Watch event
;;----------------------------------------------------------------------------------------------------------------------
(def-kw-coercion watch-event-kind WatchEvent$Kind
  {:entry-create StandardWatchEventKinds/ENTRY_CREATE
    :entry-delete StandardWatchEventKinds/ENTRY_DELETE
    :entry-modify StandardWatchEventKinds/ENTRY_MODIFY})

;;----------------------------------------------------------------------------------------------------------------------
;; Charset
;;----------------------------------------------------------------------------------------------------------------------
(def-kw-coercion charset Charset
  {:iso-8859-1 StandardCharsets/ISO_8859_1,
   :us-ascii StandardCharsets/US_ASCII,
   :utf-8 StandardCharsets/UTF_8,
   :utf-16 StandardCharsets/UTF_16,
   :utf-16be StandardCharsets/UTF_16BE,
   :utf-16le StandardCharsets/UTF_16LE})

;;----------------------------------------------------------------------------------------------------------------------
;; Copy opts
;;----------------------------------------------------------------------------------------------------------------------
(def-kw-coercion copy-option CopyOption
  {:replace-existing StandardCopyOption/REPLACE_EXISTING,
   :copy-attributes StandardCopyOption/COPY_ATTRIBUTES,
   :atomic-move StandardCopyOption/ATOMIC_MOVE})

;;----------------------------------------------------------------------------------------------------------------------
;; Open opts
;;----------------------------------------------------------------------------------------------------------------------
(def-kw-coercion open-option OpenOption
  {:read StandardOpenOption/READ,
   :create StandardOpenOption/CREATE,
   :append StandardOpenOption/APPEND,
   :create-new StandardOpenOption/CREATE_NEW,
   :sync StandardOpenOption/SYNC,
   :write StandardOpenOption/WRITE,
   :dsync StandardOpenOption/DSYNC,
   :truncate-existing StandardOpenOption/TRUNCATE_EXISTING,
   :sparse StandardOpenOption/SPARSE,
   :delete-on-close StandardOpenOption/DELETE_ON_CLOSE})


(u/defn-wn open-options
  "Coerce input into a set of OpenOption."
  {:return-generic OpenOption
   :tag Set}
  ([x]
   (if ((some-fn sequential? set?) x)
     (coerce-many open-option x)
     #{(open-option x)}))
  ([x & xs]
   (open-options (cons x xs))))

;;----------------------------------------------------------------------------------------------------------------------
;; Link opts
;;----------------------------------------------------------------------------------------------------------------------
(def-kw-coercion link-option LinkOption
  {:nofollow-links LinkOption/NOFOLLOW_LINKS})


;;----------------------------------------------------------------------------------------------------------------------
;; File Visit opts
;;----------------------------------------------------------------------------------------------------------------------
(def-kw-coercion file-visit-option FileVisitOption
  {:follow_links FileVisitOption/FOLLOW_LINKS})


(u/defn-wn file-visit-options
  "Coerce input into a set of FileVisitOption."
  {:return-generic FileVisitOption
   :tag Set}
  ([x]
   (if ((some-fn sequential? set?) x)
     (coerce-many file-visit-option x)
     #{(file-visit-option x)}))
  ([x & xs]
   (file-visit-options (cons x xs))))


(comment
  (->> LinkOption
       .getEnumConstants
       seq
       (into {} (map #(vector (-> % str
                                  .toLowerCase
                                  keyword)
                              (-> % str
                                  (->> (symbol "LinkOption"))))))))


;;----------------------------------------------------------------------------------------------------------------------
;; Arrays coercions
;;----------------------------------------------------------------------------------------------------------------------
(defmacro make-array-cstr
  ([n t]
   `(make-array-cstr ~n ~t nil))
  ([n t coercion]
   (let [tag (type (make-array (resolve t) 0))
         docstring (cond->(format "Contruct a java array whose elements are of type: %s" t)
                          coercion (str (format "\nElements are coerced automatically with: %s" coercion)))
         seq-sym (gensym "seq_param__")]
     `(defn ~n
        ~docstring
        {:tag ~tag}
        ([]
         (make-array ~t 0))
        ([~seq-sym]
         ~(if-not coercion
            `(into-array ~t ~seq-sym)
            `(into-array ~t (map ~coercion ~seq-sym))))))))


(make-array-cstr copy-option-array          CopyOption          copy-option)
(make-array-cstr file-attribute-array       FileAttribute)
(make-array-cstr file-visit-option-array    FileVisitOption)
(make-array-cstr link-option-array          LinkOption          link-option)
(make-array-cstr open-option-array          OpenOption          open-option)
(make-array-cstr watch-event-kind-array     WatchEvent$Kind     watch-event-kind)
(make-array-cstr watch-event-modifier-array WatchEvent$Modifier)
