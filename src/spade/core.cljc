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

(defn- transform-named-style [style style-name-var params-var]
  (let [has-key-meta? (find-key-meta style)
        static-key (extract-key style)
        [composition style] (extract-composes style)
        name-var (gensym "name")
        style-var (gensym "style")]
    (if (or static-key
            (not has-key-meta?))
      ; if we can extract the key statically, that's better
      (let [name-creator `(#'build-style-name
                            ~style-name-var
                            ~static-key
                            ~params-var)]
        `(let [~name-var ~name-creator
               ~style-var ~(into [`(str "." ~name-var)] style)]
           ~(with-composition composition name-var style-var)))

      `(let [base-style# ~(vec style)
             key# (:key (meta (first base-style#)))
             ~name-var (#'build-style-name
                         ~style-name-var
                         key#
                         ~params-var)
             ~style-var (into [(str "." ~name-var)] base-style#)]
         ~(with-composition composition name-var style-var)))))

(defn- transform-style [mode style style-name-var params-var]
  (let [style (replace-at-forms style)]
    (cond
      (#{:global} mode)
      `{:css (spade.runtime/compile-css ~(vec style))
        :name ~style-name-var}

      (#{:keyframes} mode)
      `{:css (spade.runtime/compile-css
               (garden.stylesheet/at-keyframes
                 ~style-name-var
                 ~(vec style)))
        :name ~style-name-var}

      :else
      (transform-named-style style style-name-var params-var))))

(defmulti ^:private declare-style
  (fn [mode _class-name _factory-name-var _factory-fn-name]
    (case mode
      :global :static
      :keyframes :no-args
      :default)))
(defmethod declare-style :static
  [mode class-name factory-name-var factory-fn-name]
  `(def ~class-name (spade.runtime/ensure-style!
                      ~mode
                      ~factory-name-var
                      ~factory-fn-name
                      nil)))
(defmethod declare-style :no-args
  [mode class-name factory-name-var factory-fn-name]
  `(defn ~class-name []
     (spade.runtime/ensure-style!
       ~mode
       ~factory-name-var
       ~factory-fn-name
       nil)))
(defmethod declare-style :default
  [mode class-name factory-name-var factory-fn-name]
  `(defn ~class-name [& params#]
     (spade.runtime/ensure-style!
       ~mode
       ~factory-name-var
       ~factory-fn-name
       params#)))

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
         ~(transform-style mode style style-name-var params-var))

       (let [~factory-name-var (factory->name ~factory-fn-name)]
         ~(declare-style mode class-name factory-name-var factory-fn-name)))))

(defmacro defclass [class-name params & style]
  (declare-style-fns :class class-name params style))

(defmacro defattrs [class-name params & style]
  (declare-style-fns :attrs class-name params style))

(defmacro defglobal [group-name & style]
  (declare-style-fns :global group-name nil style))

(defmacro defkeyframes [keyframes-name & style]
  (declare-style-fns :keyframes keyframes-name nil style))
