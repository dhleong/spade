(ns spade.container.atom
  "The AtomStyleContainer renders styles into an atom it is provided with."
  (:require [spade.container :refer [IStyleContainer]]))

(deftype AtomStyleContainer [styles-atom info-atom]
  IStyleContainer
  (mounted-info [_ style-name]
    (get @info-atom style-name))
  (mount-style! [_ style-name css info]
    (swap! styles-atom assoc style-name css)
    (swap! info-atom assoc style-name info)))

(defn create-container
  ([] (create-container (atom nil)))
  ([styles-atom] (create-container styles-atom (atom nil)))
  ([styles-atom info-atom] (->AtomStyleContainer styles-atom info-atom)))
