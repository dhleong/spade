(ns spade.runtime.defaults
  (:require [spade.container.dom :as dom]))

(defn create-container []
  (dom/create-container))
