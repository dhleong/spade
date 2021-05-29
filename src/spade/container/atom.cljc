(ns spade.container.atom
  "The AtomStyleContainer renders styles into an atom it is provided with."
  (:require [spade.container :refer [IStyleContainer]]))

(deftype AtomStyleContainer [styles-atom]
  IStyleContainer
  (mount-style! [_ style-name css]
    (swap! styles-atom assoc style-name css)))

