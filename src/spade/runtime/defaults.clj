(ns spade.runtime.defaults
  (:require [spade.container.atom :as atom-container]))

(defonce shared-styles-atom (atom nil))
(defonce shared-styles-info-atom (atom nil))

(defn create-container []
  (atom-container/create-container shared-styles-atom shared-styles-info-atom))
