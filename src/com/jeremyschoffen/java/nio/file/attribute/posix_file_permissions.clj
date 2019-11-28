(ns com.jeremyschoffen.java.nio.file.attribute.posix-file-permissions
  (:require
    [com.jeremyschoffen.java.nio.internal.coercions :as coerce]
    [com.jeremyschoffen.java.nio.internal.utils :as u])
  (:import
    (java.nio.file.attribute FileAttribute PosixFilePermissions)))



(u/alias-fn coerce/posix-file-permissions)


(defn as-file-attribute
  {:tag FileAttribute}
  [attrs]
  (->> attrs
       coerce/posix-file-permissions
       set
       PosixFilePermissions/asFileAttribute))


(defn to-string [perms]
  (PosixFilePermissions/toString perms))