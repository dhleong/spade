(ns spade.core
  (:require [clojure.walk :refer [postwalk]]
            [spade.util :refer [factory->name build-style-name]]))

(defn extract-key [style]
  (:key (meta (first style))))

(defn- find-key-meta [style]
  (postwalk
    (fn [form]
      (if (and (map? form)
               (::key form))
        form

        (if-let [k (:key (meta form))]
          {::key k}

          form)))
    style))

(def ^:private auto-imported-at-form?
  #{'at-font-face
    'at-import
    'at-media
    'at-supports})

(defn- replace-at-forms [style]
  (postwalk
    (fn [element]
      (if (and (symbol? element)
               (auto-imported-at-form? element))
        (symbol "garden.stylesheet" (name element))
        element))
    style))

(defn- extract-composes [style]
  (if-let [composes (when (map? (first style))
                      (:composes (first style)))]
    [composes

     (-> style
         vec
         (update 0 dissoc :composes))]

    [nil style]))

(defn- with-composition [composition name-var style-var]
  (let [base {:css `(spade.runtime/compile-css ~style-var)
              :name name-var}]
    (if composition
      (assoc base :composes composition)
      base)))

(defn- build-style-naming-let
  [style params original-style-name-var params-var]
  (let [has-key-meta? (find-key-meta style)
        static-key (extract-key style)
        name-var (gensym "name")]
    (cond
      ; easiest case: no params? no need to call build-style-name
      (nil? (seq params))
      [nil original-style-name-var nil]

      (or static-key
          (not has-key-meta?))
      ; if we can extract the key statically, that's better
      [nil name-var `[~name-var (#'build-style-name
                                  ~original-style-name-var
                                  ~static-key
                                  ~params-var)]]

      :else
      (let [base-style-var (gensym "base-style")]
        [base-style-var name-var `[~base-style-var ~(vec style)
                                   key# (:key (meta (first ~base-style-var)))
                                   ~name-var (#'build-style-name
                                               ~original-style-name-var
                                               key#
                                               ~params-var)]]))))

(defn- transform-named-style [style params style-name-var params-var]
  (let [[composition style] (extract-composes style)
        style-var (gensym "style")
        [base-style-var name-var name-let] (build-style-naming-let
                                             style params style-name-var
                                             params-var)
        style-decl (if base-style-var
                     `(into [(str "." ~name-var)] ~base-style-var)
                     (into [`(str "." ~name-var)] style))]
    `(let ~(vec (concat name-let
                        [style-var style-decl]))
       ~(with-composition composition name-var style-var))))

(defn- transform-keyframes-style [style params style-name-var params-var]
  (let [[style-var name-var style-naming-let] (build-style-naming-let
                                                style params style-name-var
                                                params-var)
        info-map `{:css (spade.runtime/compile-css
                          (garden.stylesheet/at-keyframes
                            ~name-var
                            ~(or style-var
                                 (vec style))))
                   :name ~name-var}]

    ; this (let) might get compiled out in advanced mode anyway, but
    ; let's just generate simpler code instead of having a redundant
    ; (let) if the keyframes take no params
    (if style-naming-let
      `(let ~style-naming-let ~info-map)
      info-map)))

(defn- transform-style [mode style params style-name-var params-var]
  (let [style (replace-at-forms style)]
    (cond
      (#{:global} mode)
      `{:css (spade.runtime/compile-css ~(vec style))
        :name ~style-name-var}

      ; keyframes are a bit of a special case
      (#{:keyframes} mode)
      (transform-keyframes-style style params style-name-var params-var)

      :else
      (transform-named-style style params style-name-var params-var))))

(defmulti ^:private declare-style
  (fn [mode _class-name params _factory-name-var _factory-fn-name]
    (case mode
      :global :static
      (cond
        (some #{'&} params) :variadic
        (every? symbol? params) :default
        :else :destructured))))
(defmethod declare-style :static
  [mode class-name _ factory-name-var factory-fn-name]
  `(def ~class-name (spade.runtime/ensure-style!
                      ~mode
                      ~factory-name-var
                      ~factory-fn-name
                      nil)))
(defmethod declare-style :no-args
  [mode class-name _ factory-name-var factory-fn-name]
  `(defn ~class-name []
     (spade.runtime/ensure-style!
       ~mode
       ~factory-name-var
       ~factory-fn-name
       nil)))
(defmethod declare-style :destructured
  [mode class-name params factory-name-var factory-fn-name]
  ; good case; since there's no variadic args, we can generate an :arglists
  ; meta and a simplified params list that we can forward simply
  (let [raw-params (->> (range (count params))
                        (map (fn [idx]
                               (gensym (str "param-" idx "-"))))
                        vec)]
    `(defn ~class-name
       {:arglists (quote ~(list params))}
       ~raw-params
       (spade.runtime/ensure-style!
         ~mode
         ~factory-name-var
         ~factory-fn-name
         ~raw-params))))
(defmethod declare-style :variadic
  [mode class-name _params factory-name-var factory-fn-name]
  ; dumb case; with a variadic params vector, any :arglists we
  ; provide gets ignored, so we just simply collect them all
  ; and pass the list as-is
  `(defn ~class-name [& params#]
     (spade.runtime/ensure-style!
       ~mode
       ~factory-name-var
       ~factory-fn-name
       params#)))
(defmethod declare-style :default
  [mode class-name params factory-name-var factory-fn-name]
  ; best case; simple params means we can use them directly
  `(defn ~class-name ~params
     (spade.runtime/ensure-style!
       ~mode
       ~factory-name-var
       ~factory-fn-name
       ~params)))

(defn- declare-style-fns [mode class-name params style]
  {:pre [(symbol? class-name)
         (or (vector? params)
             (nil? params))]}
  (let [factory-fn-name (symbol (str (name class-name) "-factory$"))
        style-name-var (gensym "style-name")
        params-var (gensym "params")
        factory-params (vec (concat [style-name-var params-var] params))
        factory-name-var (gensym "factory-name")]
    `(do
       (defn ~factory-fn-name ~factory-params
         ~(transform-style mode style params style-name-var params-var))

       (let [~factory-name-var (factory->name ~factory-fn-name)]
         ~(declare-style mode class-name params factory-name-var factory-fn-name)))))

(defmacro defclass [class-name params & style]
  (declare-style-fns :class class-name params style))

(defmacro defattrs [class-name params & style]
  (declare-style-fns :attrs class-name params style))

(defmacro defglobal [group-name & style]
  (declare-style-fns :global group-name nil style))

(defmacro defkeyframes [keyframes-name params & style]
  (declare-style-fns :keyframes keyframes-name params style))
