(ns com.jeremyschoffen.java.nio.internal.def-helpers
  (:require
    [clojure.spec.alpha :as s]
    [com.jeremyschoffen.java.nio.internal.coercions :as coerce]
    [com.jeremyschoffen.java.nio.internal.utils :as u]))


;;----------------------------------------------------------------------------------------------------------------------
;; Java call compiler
;;----------------------------------------------------------------------------------------------------------------------
(defn- resolve-tag-pass [ctxt]
  (update ctxt :tag #(cond
                       (symbol? %) (resolve %)
                       (string? %) %
                       (nil? %)    %
                       :else (throw (ex-info "The :tag metadata was wrong."
                                             {:tag %})))))


(defn- inject-this-pass [ctxt]
  (cond-> ctxt
          (:inject-this ctxt) (assoc :this-sym (gensym "this__"))))


(defn- make-gen-symm [param-name]
  (-> param-name
      name
      (str "__")
      (gensym)))


(defn- make-param-name->gen-sym [ctxt]
  (let [{:keys [arities]} ctxt
        param-names (into #{}
                          (comp
                            cat
                            (remove #(= '& %)))
                          arities)]
    (into '{& &}
          (map #(vector % (make-gen-symm %)))
          param-names)))



(defn- assoc-gen-syms-pass [ctxt]
  (assoc ctxt :syms (make-param-name->gen-sym ctxt)))


(defn- process-arity
  "Generate the vectors of symbols used to make gensym-ed param decls and potentialy coerced
  use of those params."
  [ctxt arity]
  (let [{:keys [this-sym syms this-coercion coercions]} ctxt
        constructed-param-names (volatile! (cond-> []
                                                   this-sym (conj this-sym)))
        constructed-param-usage (volatile! (if this-sym
                                             (if this-coercion
                                               [(list this-coercion this-sym)]
                                               [this-sym])
                                             []))]

    (reduce (fn [_ param-name]
              (let [sym (get syms param-name)]
                (vswap! constructed-param-names conj sym)
                (when-not (= sym '&)
                  (if-let [coercion (get coercions param-name)]
                    (vswap! constructed-param-usage conj (list coercion sym))
                    (vswap! constructed-param-usage conj sym)))))
            nil
            arity)
    [@constructed-param-names @constructed-param-usage]))


(defn- make-processed-arities [ctxt]
  (let [{:keys [arities]} ctxt]
    (into {}
          (map #(process-arity ctxt %))
          arities)))


(defn- assoc-processed-arities-pass [ctxt]
  (assoc ctxt :processed-arities (make-processed-arities ctxt)))


(defn- make-defn-body [ctxt]
  (let [{:keys [name method this-coercion processed-arities arity-0]} ctxt]
    (cond->> (for [[params-decl coerced-params] processed-arities]
               `([~@params-decl]
                 (~method ~@coerced-params)))

             (and arity-0 this-coercion)
             (cons `([]
                     (~name (~this-coercion)))))))


(defn- assoc-defn-body-pass [ctxt]
  (assoc ctxt :defn-body (make-defn-body ctxt)))


(defn- assoc-arglists-pass [ctxt]
  (let [{:keys [arities inject-this arity-0]} ctxt
        processed-arities (if inject-this
                            (into (if arity-0 [[]] [])
                                  (comp (map #(cons 'this %))
                                        (map vec))
                                  arities)
                            (if arity-0
                              (cons [] arities)
                              arities))]
    (assoc-in ctxt [:attr-map :arglists] (list 'quote (seq processed-arities)))))


(defn- ensure-arglists-pass [ctxt]
  (let [arglists (get-in ctxt [:attr-map :arglists])]
    (if arglists
      ctxt
      (assoc-arglists-pass ctxt))))


(defn add-coercion-table-to-attr-map-pass [ctxt]
  (let [{:keys [coercions inject-this this-coercion]} ctxt
        coercions (cond-> coercions
                          (and inject-this this-coercion)
                          (assoc 'this this-coercion))]
    (assoc-in ctxt [:attr-map :coercions] (list 'quote coercions))))


(defn- make-defn [ctxt]
  (let [{:keys [name docstring tag attr-map defn-body]} ctxt]
    `(u/defn-wn ~name
       ~docstring
       ~(assoc attr-map :tag tag)
       ~@(if (= 1 (count defn-body))
           (first defn-body)
           defn-body))))


(defn- assoc-defn-pass [ctxt]
  (assoc ctxt :defn (make-defn ctxt)))


(def ^:private compile-java-call-defaults
  {:docstring ""
   :attr-map {}
   :inject-this  false
   :this-coercion nil
   :arities '[[]]
   :coercions {}
   :arity-0 false})


(s/def ::name symbol?)
(s/def ::tag (s/nilable (some-fn symbol? string?)))
(s/def ::method symbol?)
(s/def ::docstring string?)
(s/def ::attr-map map?)
(s/def ::inject-this boolean?)
(s/def ::this-coercion any?)
(s/def ::arities (s/coll-of (s/coll-of symbol?)))
(s/def ::coercions map?)
(s/def ::arity-0 boolean?)

(s/def ::compile-java-call-ctxt
  (s/keys :req-un [::name ::tag ::method]
          :opt-un [::docstring
                   ::attr-map
                   ::inject-this
                   ::this-coercion
                   ::arities
                   ::coercions
                   ::arity-0]))

(defn- compile-java-call
  "Construct a function that will call a java method.

  context map keys:
  - `:name`: symbol name of the result function
  - `:tag`: symbol tag of the result function
  - `:method`: symbol used as first element in a clj call.
  - `:docstring`: a docstring for the generated fn.
  - `:attr-map`: metadata for the generated fn.
  - `:inject-this`: whether we inject a this first parameter to the generated function arities.
  - `:this-coercion`: a coercion function for the injected this param.
  - `:arities`: The arities we want for instance [[] [x] [x & args]]
  - `:coercions`: a map from parameter name coercion function
  - `:arity-0`: wether we generate a generate an arity 0 that will recurse on the result of the call `(this-coercion)`"
  [ctxt]
  (-> ctxt

      (->> (s/assert ::compile-java-call-ctxt)
           (merge compile-java-call-defaults))
      resolve-tag-pass
      inject-this-pass
      assoc-gen-syms-pass
      assoc-processed-arities-pass
      assoc-defn-body-pass
      ensure-arglists-pass
      add-coercion-table-to-attr-map-pass
      assoc-defn-pass
      :defn))

;;----------------------------------------------------------------------------------------------------------------------
;; Macros using the compiler.
;;----------------------------------------------------------------------------------------------------------------------



(s/def ::def-java-call-opts
  (s/keys :opt-un [::docstring
                   ::attr-map
                   ::inject-this
                   ::this-coercion
                   ::arities
                   ::coercions
                   ::arity-0]))


(s/def ::def-java-call (s/cat :name ::name
                              :docstring (s/? ::docstring)
                              :attr-map (s/? ::attr-map)
                              :tag ::tag
                              :method ::method
                              :opts (s/? ::def-java-call-opts)))

(defmacro def-java-call
  "Construct a function that will call a java method following the template
  using the compile-java-call function."
  {:arglists '([name docstring? attr-map? tag method opts?])}
  [& args]
  (->> args
       (u/parse-params ::def-java-call)
       (compile-java-call)))


(defn- compute-method-on-arity [ctxt]
  (assoc ctxt
    :arities (if-let [additional-params (get ctxt :additional-params)]
               [additional-params]
               '[[]])))


(defn- method-on-pass [ctxt this-coercion arity-0]
  (-> ctxt
      (assoc :inject-this true
             :arity-0 arity-0)
      (cond-> this-coercion
              (assoc :this-coercion this-coercion))
      compute-method-on-arity))


(s/def ::additional-params (s/coll-of symbol?))


(s/def ::def-method-on-opts
  (s/merge ::def-java-call-opts
           (s/keys :opt-un [::additional-params])))


(s/def ::def-method-on (s/cat :name ::name
                              :docstring (s/? ::docstring)
                              :attr-map (s/? ::attr-map)
                              :tag ::tag
                              :method ::method
                              :opts (s/? ::def-method-on-opts)))


(defmacro def-path-fn
  "Define function an a path.
  :inject-this is true, :this-coercion is set to `coercions/path`

  opts:
  - `:additional-params` : ex :[x y z]
  - `:coercions` : ex {x path y fs}"
  {:arglists '([name docstring? attr-map? tag method opts?])}
  [& args]
  (-> args
      (->> (u/parse-params ::def-method-on))
      (method-on-pass `coerce/path false)
      compile-java-call))


(defmacro def-fs-fn
  "Define a function on a file system with `def-java-call`.
  :inject-this is true, :this-coercion is set to `coercions/file-system`

  opts:
  - `:additional-params` : ex :[x y z]
  - `:coercions` : ex {x path y fs}"
  [& args]
  (-> args
      (->> (u/parse-params ::def-method-on))
      (method-on-pass `coerce/file-system false)
      compile-java-call))


(defmacro def-file-store-fn
  "Define a function on a file store with `def-java-call`.
  :inject-this is true.`

  opts:
  - `:additional-params` : ex :[x y z]
  - `:coercions` : ex {x path y fs}"
  [& args]
  (-> args
      (->> (u/parse-params ::def-method-on))
      (method-on-pass `coerce/file-store false)
      compile-java-call))


(defmacro def-binary-path-fn
  "Define a java method call on 2 paths defined with `def-java-call`."
  [& args]
  (-> args
      (->> (u/parse-params ::def-method-on))
      (assoc :additional-params '[other]
             :coercions {'other `coerce/path})
      (method-on-pass `coerce/path false)
      compile-java-call))


(s/def ::additional-params-variadic (s/coll-of (s/and symbol?
                                                      (complement #{'&}))))


(defn variadic-method-on-pass
  "Adds a variadic end to the additionnal params, throws if one already exists."
  [ctxt variadic-param coercion]
  (let [{:keys [additional-params]
         :or {additional-params []}} ctxt
        additional-params (s/assert* ::additional-params-variadic additional-params)
        additional-params (-> additional-params
                              vec
                              (conj '& variadic-param))]
    (-> ctxt
        (assoc :additional-params additional-params)
        (update :coercions assoc variadic-param coercion))))


(defmacro def-link-fn
  "Define a java method call on a path and variadic links-options"
  [& args]
  (-> args
      (->> (u/parse-params ::def-method-on))
      (variadic-method-on-pass 'links `coerce/link-option-array)
      (method-on-pass `coerce/path false)
      compile-java-call))


(defmacro def-create-fn
  "Defines a create function of a path and variadic file attributes."
  [& args]
  (-> args
      (->> (u/parse-params ::def-method-on))
      (variadic-method-on-pass 'file-attributes `coerce/file-attribute-array)
      (method-on-pass `coerce/path false)
      compile-java-call))


(defmacro def-open-fn
  "Define a java method call on a path and variadic open-opts"
  [& args]
  (-> args
      (->> (u/parse-params ::def-method-on))
      (variadic-method-on-pass 'open-opts `coerce/open-option-array)
      (method-on-pass `coerce/path false)
      compile-java-call))