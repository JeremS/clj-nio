(ns user)



(alter-var-root #'*warn-on-reflection* (constantly true))


(comment
  (def i 1)
  (def j "2")

  (println (.toString i))
  (println (.toString j)))
