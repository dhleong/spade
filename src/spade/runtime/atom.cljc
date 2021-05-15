(ns spade.runtime.atom
  (:require [spade.runtime.shared :refer [IStyleContainer]]))

(deftype AtomStyleContainer [styles-atom]
  IStyleContainer
  (mount-style! [_ style-name css]
    (swap! styles-atom assoc style-name css)))

