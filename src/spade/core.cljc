(ns spade.core
  (:require [spade.runtime :as runtime]
            [spade.util :refer [factory->name build-style-name]]
            [garden.core :as garden]))

(defn compile-style-data [style-name elements]
  {:css (garden/css elements)
   :name style-name})

(defn- transform-style [style style-name-var params-var]
  (let [name-creator `(#'build-style-name
                        ~style-name-var
                        ~(:key (meta (first style)))
                        ~params-var)
        name-var (gensym "name")]
    `(let [~name-var ~name-creator]
       (#'compile-style-data ~name-var ~(into [`(str "." ~name-var)] style)))))

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
           (runtime/ensure-style!
             :class
             factory-name#
             ~factory-fn-name
             params#))))))
