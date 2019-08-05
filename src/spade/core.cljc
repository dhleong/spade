(ns spade.core
  (:require [spade.runtime :as runtime]
            [spade.util :refer [factory->name]]
            [garden.core :as garden]))

(defn- transform-style [style style-name-var]
  `(#'garden/css ~(into [style-name-var] style)))

(defmacro defclass [class-name params & style]
  (let [factory-fn-name (symbol (str (name class-name) "-factory$"))
        style-name-var (gensym "style-name")
        factory-params (vec (cons style-name-var params))]
    `(do
       (defn ~factory-fn-name ~factory-params
         ~(transform-style style style-name-var))

       ; TODO *if* we accept params, they should probably modify the
       ; class names/elements in some way. CSS vars?
       (let [factory-name# (factory->name ~factory-fn-name)]
         (defn ~class-name [& params#]
           (runtime/ensure-style!
             :class
             factory-name#
             ~factory-fn-name
             params#))))))
