(ns spade.core
  (:require [clojure.walk :refer [postwalk]]
            [spade.util :refer [factory->name build-style-name]]
            [garden.core :as garden]))

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

(defn- transform-style [style style-name-var params-var]
  (let [has-key-meta? (find-key-meta style)
        static-key (extract-key style)]
    (if (or static-key
            (not has-key-meta?))
      ; if we can extract the key statically, that's better
      (let [name-creator `(#'build-style-name
                            ~style-name-var
                            ~static-key
                            ~params-var)
            name-var (gensym "name")]
        `(let [~name-var ~name-creator
               style# ~(into [`(str "." ~name-var)] style)]
           {:css (garden/css style#)
            :elements style#
            :name ~name-var}))

      `(let [base-style# ~(vec style)
             key# (:key (meta (first base-style#)))
             style-name# (#'build-style-name
                           ~style-name-var
                           key#
                           ~params-var)
             full-style# (into [(str "." style-name#)] base-style#)]
         {:css (garden/css full-style#)
          :elements full-style#
          :name style-name#}))))

(defmacro defclass [class-name params & style]
  (let [factory-fn-name (symbol (str (name class-name) "-factory$"))
        style-name-var (gensym "style-name")
        params-var (gensym "params")
        factory-params (vec (concat [style-name-var params-var] params))]
    `(do
       (defn ~factory-fn-name ~factory-params
         ~(transform-style style style-name-var params-var))

       ; TODO *if* we accept params, they should probably modify the
       ; class names/elements in some way. CSS vars?
       (let [factory-name# (factory->name ~factory-fn-name)]
         (defn ~class-name [& params#]
           (spade.runtime/ensure-style!
             :class
             factory-name#
             ~factory-fn-name
             params#))))))
