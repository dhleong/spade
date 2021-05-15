(ns spade.runtime.dom
  (:require [spade.runtime.shared :as container :refer [IStyleContainer]]))

(defonce ^:dynamic *injected-styles* (atom nil))
(defonce ^:dynamic *dom* nil)

(defn- perform-update! [obj css]
  (set! (.-innerHTML (:element obj)) css))

(defn update! [styles-container id css]
  (swap! styles-container update id
         (fn update-injected-style [obj]
           (when-not (= (:source obj) css)
             (perform-update! obj css))
           (assoc obj :source css))))

(defn inject! [target-dom styles-container id css]
  (let [destination (or (when (ifn? target-dom)
                          (target-dom))
                        target-dom
                        (.-head js/document))
        element (doto (js/document.createElement "style")
                  (.setAttribute "spade-id" (str id)))
        obj {:element element
             :source css
             :id id}]
    (assert (some? destination)
            "An <head> element or target *dom* is required to inject the style.")

    (.appendChild destination element)

    (swap! styles-container assoc id obj)
    (perform-update! obj css)))

(deftype DomStyleContainer [target-dom styles-container]
  IStyleContainer
  (mount-style! [_ style-name css]
    (let [resolved-container (or styles-container
                                 *injected-styles*)]
      (if (contains? @resolved-container style-name)
        (update! resolved-container style-name css)
        (inject! target-dom resolved-container style-name css)))))

(defn create-container
  ([] (create-container nil))
  ([target-dom] (create-container target-dom (when target-dom
                                               (atom nil))))
  ([target-dom styles-container]
   (->DomStyleContainer target-dom styles-container)))
