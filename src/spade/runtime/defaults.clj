(ns spade.runtime.defaults
  (:require [spade.container.atom :refer [->AtomStyleContainer]]))

(defonce shared-styles-atom (atom nil))

(defn create-container []
  (->AtomStyleContainer shared-styles-atom))
