(ns com.jeremyschoffen.java.nio.internal.utils
  (:require
    [clojure.spec.alpha :as s]
    [clojure.pprint :as pp])
  (:import
    (java.lang AutoCloseable)))


(defn conform-or-throw [spec v]
  (let [conformed (s/conform spec v)]
    (when (s/invalid? conformed)
      (let [data (s/explain-data spec v)]
        (throw (ex-info (str "Error conforming.\n" (with-out-str (s/explain-out data)))
                        data))))
    conformed))


(defn parse-params [spec args]
  (let [conformed (conform-or-throw spec args)
        ctxt (dissoc conformed :opts)
        opts (get conformed :opts)]
    (merge opts ctxt)))


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


(defn- make-accepted-keywords-notice [m]
  (when-let [kws (get m :coercions/keywords)]
    (let [rows (map (fn [[k v]] {"Keyword" k "Enum val" v})
                    kws)]
      (str "Accepted keywords:\n"
           (with-out-str
             (pp/print-table ["Keyword" "Enum val"] rows))))))


(def ^:private notice-makers
  [make-return-type-notice
   make-close-warning-notice
   make-coercion-notice
   make-accepted-keywords-notice])



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