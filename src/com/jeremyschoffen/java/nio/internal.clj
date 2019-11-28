(ns com.jeremyschoffen.java.nio.internal
  (:require
    [com.jeremyschoffen.java.nio.internal.potemkin.namespaces :as n]
    [com.jeremyschoffen.java.nio.internal.utils :as u]
    [com.jeremyschoffen.java.nio.internal.coercions :as coerce]
    [com.jeremyschoffen.java.nio.internal.def-helpers :as h]))


(defmacro alias-def [alias aliased-name]
  (let [aliased-name (-> aliased-name resolve symbol)]
    `(n/import-def ~aliased-name ~alias)))


(defmacro alias-fn [alias aliased-name]
  (let [aliased-name (-> aliased-name resolve symbol)]
    `(n/import-fn ~aliased-name ~alias)))


(defmacro alias-macro [alias aliased-name]
  (let [aliased-name (-> aliased-name resolve symbol)]
    `(n/import-macro ~aliased-name ~alias)))


(alias-macro defn-wn u/defn-wn)

(alias-fn bi-predicate coerce/bi-predicate)
(alias-fn charset coerce/charset)
(alias-fn copy-option coerce/copy-option)
(alias-fn copy-option-array coerce/copy-option-array)
(alias-fn dir-stream-filter coerce/dir-stream-filter)
(alias-fn file coerce/file)
(alias-fn file-attribute-array coerce/file-attribute-array)
(alias-fn file-store coerce/file-store)
(alias-fn file-system coerce/file-system)
(alias-fn file-time coerce/file-time)
(alias-fn file-visit-option coerce/file-visit-option)
(alias-fn file-visit-option-array coerce/file-visit-option-array)
(alias-fn file-visit-options coerce/file-visit-options)
(alias-fn input-stream coerce/input-stream)
(alias-fn link-option coerce/link-option)
(alias-fn link-option-array coerce/link-option-array)
(alias-fn open-option coerce/open-option)
(alias-fn open-option-array coerce/open-option-array)
(alias-fn open-options coerce/open-options)
(alias-fn output-stream coerce/output-stream)
(alias-fn path coerce/path)
(alias-fn posix-file-permission coerce/posix-file-permission)
(alias-fn posix-file-permissions coerce/posix-file-permissions)
(alias-fn some-coercion coerce/some-coercion)
(alias-fn uri coerce/uri)
(alias-fn url coerce/url)
(alias-fn watch-event-kind coerce/watch-event-kind)
(alias-fn watch-event-kind-array coerce/watch-event-kind-array)
(alias-fn watch-event-modifier-array coerce/watch-event-modifier-array)


(alias-macro def-java-call h/def-java-call)
(alias-macro def-path-fn h/def-path-fn)
(alias-macro def-fs-fn h/def-fs-fn)
(alias-macro def-file-store-fn h/def-file-store-fn)
(alias-macro def-binary-path-fn h/def-binary-path-fn)
(alias-macro def-create-fn h/def-create-fn)
(alias-macro def-link-fn h/def-link-fn)
(alias-macro def-open-fn h/def-open-fn)
