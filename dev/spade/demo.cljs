(ns spade.demo
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [spade.core :refer [defclass defglobal defkeyframes]]))

(defkeyframes anim-frames
  ["0%" {:opacity 0}]
  ["100%" {:opacity 1}])

(defglobal background
  [:body {:background "#333"}])

(defglobal text
  [:body {:color "#fff"}])

(defclass serenity []
  (at-media {:min-width "750px"}
    {:padding "80px"})
  {:padding "8px"}

  [:.title {:font-size "22pt"
            :animation [[(anim-frames) "560ms" 'ease-in-out]]}])

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

