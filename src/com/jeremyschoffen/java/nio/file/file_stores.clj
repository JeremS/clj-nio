(ns com.jeremyschoffen.java.nio.file.file-stores
  (:require [com.jeremyschoffen.java.nio.internal :as i])
  (:import
    (java.nio.file.attribute FileStoreAttributeView)))


(i/def-file-store-fn attribute
  "Reads the value of a file store attribute."
  Object .getAttribute
  {:additional-params [attribute]})


(i/def-file-store-fn file-store-attribute-view
  "Returns a FileStoreAttributeView of the given type."
  FileStoreAttributeView .getFileStoreAttributeView
  {:additional-params [type]})


(i/def-file-store-fn total-space​
  "Returns the size, in bytes, of the file store."
  Long .getTotalSpace)


(i/def-file-store-fn unallocated-space​
  "Returns the number of unallocated bytes in the file store."
  Long .getUnallocatedSpace)


(i/def-file-store-fn usable-space​
  "Returns the number of bytes available to this Java virtual machine on the file store."
  Long .getUsableSpace)


(i/def-file-store-fn read-only?
  "Tells whether this file store is read-only."
  Boolean .isReadOnly)


(i/def-file-store-fn file-store-name
  "Returns the name of this file store."
   String .name)


(i/defn-wn supports-file-attribute-view
  "Tells whether or not this file store supports the file attributes identified by the given file attribute view."
  {:tag Boolean}
  [store str-or-class]
  (if (string? str-or-class)
    (.supportsFileAttributeView (i/file-store store) ^String str-or-class)
    (.supportsFileAttributeView (i/file-store store) ^Class str-or-class)))


(i/def-file-store-fn file-store-type
  "Returns the name of this file store."
  String .type)