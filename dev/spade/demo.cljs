(ns spade.demo
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [spade.core :refer [defclass]]))

(defclass serenity []
  {:padding "8px"}

  [:.title {:font-size "22pt"}])

(defclass colorized [color]
  ^{:key (str/upper-case color)}
  {:height "20px"
   :width "20px"
   :background-color color})

(defclass flex []
  {:display 'flex})

(defn view []
  [:<>
   [:div {:class (serenity)}
    [:div.title "Test"]]

   [:div {:class (flex)}
    [:div {:class (colorized "red")}]
    [:div {:class (colorized "blue")}]
    [:div {:class (colorized "green")}]]
   ])

(defn mount-root []
  (r/render [view] (.getElementById js/document "app")))

(defn init!  []
  (mount-root))

(init!)

