(ns spade.demo
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [spade.core :refer [defclass]]))

(defclass serenity []
  {:padding "8px"}

  [:.title {:font-size "22pt"}])

(defclass colorized-with-key [color]
  ^{:key (str/upper-case color)}
  {:height "20px"
   :width "20px"
   :background-color color})

(defclass colorized-with-key-in-block [color]
  (let [k (str/upper-case color)]
    ^{:key k}
    {:height "20px"
     :width "20px"
     :background-color color}))

(defclass colorized [color]
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
    [:div {:class (colorized-with-key "red")}]
    [:div {:class (colorized-with-key "blue")}]
    [:div {:class (colorized-with-key "green")}]]

   [:div {:class (flex)}
    [:div {:class (colorized-with-key-in-block "red")}]
    [:div {:class (colorized-with-key-in-block "blue")}]
    [:div {:class (colorized-with-key-in-block "green")}]]

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

