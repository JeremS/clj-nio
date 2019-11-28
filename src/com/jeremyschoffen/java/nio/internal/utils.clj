(ns com.jeremyschoffen.java.nio.internal.utils
  (:require
    [clojure.pprint :as pp])
  (:import
    (java.lang AutoCloseable)))


(defmacro alias-fn
  ([aliased]
   (list `alias-fn nil aliased))
  ([alias-name aliased]
   (let [resolved (resolve aliased)
         {resolved-name :name
          :as resolved-meta} (meta resolved)

         aliased-name (symbol resolved)
         alias-name   (or alias-name resolved-name)

         {doc :doc :or {doc ""}
          :as alias-meta} (dissoc resolved-meta :line :column :file :name :ns)

         alias-meta (update alias-meta :doc #(str (format "Alias for: `%s` \n\n  " aliased-name) %))]
     `(do
        (def ~alias-name ~aliased-name)
        (alter-meta! (var ~alias-name) #(merge '~alias-meta %))
        (var ~alias-name)))))


(defn- make-return-type-notice [metadata-map]
  (when-let [type (:return-type metadata-map
                    (:tag metadata-map))]
    (format "Return type: %s" type)))


(defn- make-return-type-notice [metadata-map]
  (when-let [tag (:tag metadata-map)]
    (let [ret (format "Return type: %s" tag)]
      (if-let [generic (:return-generic metadata-map)]
        (str ret (format "<%s>" (cond-> generic
                                        (symbol? generic) (-> resolve symbol))))
        ret))))


(defn- fully-qualify-coercion [c]
  (if (symbol? c)
    (-> c resolve symbol)
    c))


(defn- fully-qualify-coercions [cs]
  (into {}
        (map (fn [[sym c]]
               [sym (fully-qualify-coercion c)]))
        cs))


(defn- make-coercion-notice [metadata-map]
  (when-let [coercions-map (:coercions metadata-map)]
    (let [coercions-map (fully-qualify-coercions coercions-map)
          this-coercion (get coercions-map 'this)
          coercions-map (cond-> coercions-map this-coercion (dissoc 'this))
          rows (cond-> [] this-coercion (conj {"param" 'this
                                               "coercion" this-coercion}))
          rows (into rows
                     (map (fn [[param coercion]]
                            {"param" param
                             "coercion" coercion}))
                     coercions-map)]
      (str "Coercion(s) table:"
           (with-out-str
             (pp/print-table ["param" "coercion"] rows))))))


(defn- make-close-warning-notice [metadata-map]
  (when (isa? (:tag metadata-map) AutoCloseable)
    "Remember to close the result of this function."))


(def ^:private notice-makers
  [make-return-type-notice
   make-coercion-notice
   make-close-warning-notice])


(def ^:private make-notices (apply juxt notice-makers))


(defn add-notices [metadata-map]
  (let [current-doc (:doc metadata-map "")
        notices (-> metadata-map
                    make-notices
                    (->> (into [current-doc] (filter identity)))
                    (interleave (repeat "\n\n")))]
    (-> metadata-map
        (dissoc :coercions)
        (assoc :doc (apply str notices)))))


(defmacro defn-wn [n & args]
  `(do
     (defn ~n ~@args)
     (alter-meta! (var ~n) add-notices)
     (var ~n)))