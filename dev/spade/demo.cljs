(ns spade.demo
  (:require [clojure.string :as str]
            [reagent.dom :as rdom]
            [spade.core :refer [defclass defattrs defglobal defkeyframes]]
            [spade.react :as spade]))

(defkeyframes anim-frames []
  ["0%" {:opacity 0}]
  ["100%" {:opacity 1}])

(defkeyframes parameterized-anim-frames [start end]
  ["0%" {:opacity start}]
  ["100%" {:opacity end}])

(defglobal background
  [:body {:*my-var* "22pt"
          :background "#333"}])

(defglobal text
  [:body {:color "#fff"}])

(defclass serenity []
  (at-media {:min-width "750px"}
    {:padding "80px"})
  {:padding "8px"}

  [:.title {:font-size :*my-var*
            :animation [[(parameterized-anim-frames 0 0.5) "560ms" 'ease-in-out]]}])

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

(defattrs composed-attrs []
  {:composes (flex)})

(defn demo []
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

   [:div (composed-attrs)
    [:div {:class (colorized "red")}]
    [:div {:class (colorized "blue")}]
    [:div {:class (colorized "green")}]]])

(defn view []
  [:div
   [:style#styles]
   [demo]])

(defn mount-root []
  (rdom/render
    [spade/with-dom #(.getElementById js/document "styles")
     [view]]
    (.getElementById js/document "app")))

(defn init!  []
  (mount-root))

(init!)

