(ns spade.container.alternate
  (:require [spade.runtime.shared :as sc :refer [IStyleContainer]]))

(deftype AlternateStyleContainer [get-preferred fallback]
  IStyleContainer
  (mount-style!
    [_ style-name css]
    (or (when-let [preferred (get-preferred)]
          (sc/mount-style! preferred style-name css))
        (sc/mount-style! fallback style-name css))))
