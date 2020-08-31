(ns docs.core
  (:require
    [fr.jeremyschoffen.textp.alpha.doc.core :as tp-doc]
    [fr.jeremyschoffen.mbt.alpha.utils :as u]))

(u/pseudo-nss
  project)


(defn make-readme! [{wd ::project/working-dir
                     coords ::project/maven-coords}]
  (spit (u/safer-path wd "README.md")
        (tp-doc/make-document "docs/pages/README.md.tp"
                              {:project/maven-coords coords})))

