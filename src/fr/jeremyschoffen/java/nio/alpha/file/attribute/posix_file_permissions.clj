(ns fr.jeremyschoffen.java.nio.alpha.file.attribute.posix-file-permissions
  (:require
    [fr.jeremyschoffen.java.nio.alpha.internal :as i])
  (:import
    (java.nio.file.attribute FileAttribute PosixFilePermissions)))


(i/alias-fn posix-file-permissions i/posix-file-permissions)


(i/defn-wn as-file-attribute
  {:coercions '{permissions i/posix-file-permissions}
   :tag FileAttribute}
  [permissions]
  (->> permissions
       i/posix-file-permissions
       PosixFilePermissions/asFileAttribute))


(i/defn-wn to-string
  {:coercions '{perms i/posix-file-permissions}
   :tag String}
  [perms]
  (-> perms
      i/posix-file-permissions
      PosixFilePermissions/toString))