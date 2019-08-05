(ns spade.demo
  (:require [reagent.core :as r]
            [spade.core :refer [defclass]]))

(defclass serenity []
  {:padding "8px"}

  [:.title {:font-size "22pt"}])

(defn view []
  [:div {:class (serenity)}
   [:div.title "Test"]])

(defn mount-root []
  (r/render [view] (.getElementById js/document "app")))

(defn init!  []
  (mount-root))

(init!)

