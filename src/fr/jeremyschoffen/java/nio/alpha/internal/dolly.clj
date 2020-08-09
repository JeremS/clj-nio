(ns fr.jeremyschoffen.java.nio.alpha.internal.dolly)


(defn- resolve-cloned [cloned]
  (let [cloned-var (resolve cloned)]
    (if cloned-var
      cloned-var
      (throw (ex-info (str "Can't resolve `" cloned  "` while cloning var.") {})))))


(defn cloned-info
  [cloned]
  (let [cloned-var (resolve-cloned cloned)
        cloned-meta (meta cloned-var)]
    {:cloned-var cloned-var
     :cloned-sym (symbol cloned-var)
     :cloned-meta cloned-meta
     :type (cond
             (:macro cloned-meta) :macro
             (:arglists cloned-meta) :function
             :else :value)}))


(defn- make-added-meta [{:keys [cloned-sym cloned-meta]}]
  (-> cloned-meta
      (dissoc :line :column :file :name :ns)
      (assoc ::clone-of cloned-sym)))


(defmacro clone-value [new-name cloned]
  (let [{:keys [cloned-sym]
         :as info} (cloned-info cloned)
        added-meta (make-added-meta info)]
    (when (:macro added-meta)
      (throw (ex-info (str "Can't clone the macro `" cloned-sym "` as a value.") {})))

    `(do
       (def ~new-name ~cloned-sym)
       (alter-meta! (var ~new-name) merge '~added-meta))))


(defmacro clone-macro [new-name cloned]
  (let [{:keys [cloned-sym]
         :as info} (cloned-info cloned)
        added-meta (make-added-meta info)]
    (when-not (:macro added-meta)
      (throw (ex-info (str "Can't clone the value `" cloned-sym "` as a macro.") {})))

    `(do
       (defmacro ~new-name [& body#]
         (list* '~cloned-sym body#))
       (alter-meta! (var ~new-name) merge '~added-meta))))


(defmacro def-clone
  ([cloned]
   `(def-clone nil ~cloned))
  ([new-name cloned]
   (let [cloned-var (resolve-cloned cloned)
         new-name (or new-name
                      (-> cloned-var symbol name symbol))]
     (if (-> cloned-var meta :macro)
       `(clone-macro ~new-name ~cloned)
       `(clone-value ~new-name ~cloned)))))