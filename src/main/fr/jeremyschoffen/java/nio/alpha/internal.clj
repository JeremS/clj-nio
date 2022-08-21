(ns fr.jeremyschoffen.java.nio.alpha.internal
  (:require
    [fr.jeremyschoffen.dolly.core :as dolly]
    [fr.jeremyschoffen.java.nio.alpha.internal.utils :as u]
    [fr.jeremyschoffen.java.nio.alpha.internal.coercions :as coerce]
    [fr.jeremyschoffen.java.nio.alpha.internal.def-helpers :as h])
  (:import (java.util.stream Stream)
           (java.lang AutoCloseable)
           (java.nio.file DirectoryStream)))


(dolly/add-keys-to-quote! :coercions/keywords)

(dolly/def-clone def-clone dolly/def-clone)


(def-clone defn-wn u/defn-wn)

(def-clone bi-predicate coerce/bi-predicate)
(def-clone charset coerce/charset)
(def-clone copy-option coerce/copy-option)
(def-clone copy-option-array coerce/copy-option-array)
(def-clone dir-stream-filter coerce/dir-stream-filter)
(def-clone file coerce/file)
(def-clone file-attribute-array coerce/file-attribute-array)
(def-clone file-store coerce/file-store)
(def-clone file-system coerce/file-system)
(def-clone file-system? coerce/file-system?)
(def-clone file-time coerce/file-time)
(def-clone file-visit-option coerce/file-visit-option)
(def-clone file-visit-option-array coerce/file-visit-option-array)
(def-clone file-visit-options coerce/file-visit-options)
(def-clone input-stream coerce/input-stream)
(def-clone link-option coerce/link-option)
(def-clone link-option-array coerce/link-option-array)
(def-clone open-option coerce/open-option)
(def-clone open-option-array coerce/open-option-array)
(def-clone open-options coerce/open-options)
(def-clone output-stream coerce/output-stream)
(def-clone path coerce/path)
(def-clone path? coerce/path?)
(def-clone posix-file-permission coerce/posix-file-permission)
(def-clone posix-file-permissions coerce/posix-file-permissions)
(def-clone some-coercion coerce/some-coercion)
(def-clone uri coerce/uri)
(def-clone url coerce/url)
(def-clone url? coerce/url?)
(def-clone watch-event-kind coerce/watch-event-kind)
(def-clone watch-event-kind-array coerce/watch-event-kind-array)
(def-clone watch-event-modifier-array coerce/watch-event-modifier-array)


(def-clone def-java-call h/def-java-call)
(def-clone def-path-fn h/def-path-fn)
(def-clone def-fs-fn h/def-fs-fn)
(def-clone def-file-store-fn h/def-file-store-fn)
(def-clone def-binary-path-fn h/def-binary-path-fn)
(def-clone def-create-fn h/def-create-fn)
(def-clone def-link-fn h/def-link-fn)
(def-clone def-open-fn h/def-open-fn)


;;----------------------------------------------------------------------------------------------------------------------
;; Streams shenanigans
;;----------------------------------------------------------------------------------------------------------------------
(defn- realize-stream [^Stream s]
  (let [res (into [] (-> s
                         .iterator
                         iterator-seq
                         seq))]
    (.close s)
    res))

(defn- realize-dir-stream [dir-s]
  (let [res (into [] (-> ^Iterable dir-s
                         .iterator
                         iterator-seq
                         seq))]
    (.close ^AutoCloseable dir-s)
    res))


(defprotocol StreamLike
  (realize [this]
    "Eagerly convert a java stream (or stream like thing) into a vector, closes the stream it and returns the vector."))


(extend-protocol StreamLike
  Stream
  (realize [this] (realize-stream this))

  DirectoryStream
  (realize [this] (realize-dir-stream this)))
