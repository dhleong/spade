(ns spade.runtime
  (:require [clojure.string :as str]
            [garden.core :as garden]
            [garden.types :refer [->CSSFunction]]
            [spade.container :as sc]
            [spade.runtime.defaults :as defaults]))

(defonce ^:dynamic *css-compile-flags*
  {:pretty-print? #? (:cljs goog.DEBUG
                      :clj false)})

(defonce ^:dynamic *style-container* (defaults/create-container))

(defn ->css-var [n]
  (->CSSFunction "var" n))

(defn compile-css [elements]
  (garden/css *css-compile-flags* elements))

(defn- compose-names [{style-name :name composed :composes}]
  (if-not composed
    style-name
    (str/join " "
              (->>
                (if (seq? composed)
                  (into composed style-name)
                  [composed style-name])
                (map (fn [item]
                       (cond
                         (string? item) item

                         ; unpack a defattrs
                         (and (map? item)
                              (string? (:class item)))
                         (:class item)

                         :else
                         (throw (ex-info
                                  (str "Invalid argument to :composes key:"
                                       item)
                                  {})))))))))

(defn ensure-style! [mode base-style-name factory params]
  (let [{css :css style-name :name :as info} (apply factory base-style-name params params)]

    (sc/mount-style! *style-container* style-name css)

    (case mode
      :attrs {:class (compose-names info)}
      (:class :keyframes) (compose-names info)
      :global css)))
