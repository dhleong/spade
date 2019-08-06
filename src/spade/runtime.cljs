(ns ^{:author "Daniel Leong"
      :doc "spade.runtime"}
  spade.runtime)

(defonce
  ^{:private true
    :dynamic true}
  *injected* (atom {}))

(defn update! [obj css]
  (set! (.-innerHTML (:element obj)) css))

(defn inject! [id css]
  (let [head (.-head js/document)
        element (doto (js/document.createElement "style")
                  (.setAttribute "spade-id" id))
        obj {:element element
             :id id}]
    (assert (some? head)
            "An head element is required in the dom to inject the style.")

    (.appendChild head element)

    (swap! *injected* assoc id obj)
    (update! obj css)))

(defn ensure-style! [mode base-style-name factory params]
  (let [{css :css style-name :name} (apply factory base-style-name params params)
        existing (get @*injected* style-name)]

    (if existing
      ; update existing style element
      (update! existing css)

      ; create a new element
      (inject! style-name css))

    (case mode
      :class style-name
      :attrs {:class style-name})))
