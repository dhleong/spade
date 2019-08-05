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

(defn ensure-style! [mode style-name factory args]
  (let [prefix (str (case mode
                      (:class :opts) "."
                      :id "#")
                    style-name)
        css (apply factory prefix args)

        existing (get @*injected* prefix)]

    (if existing
      ; update existing style element
      (update! existing css)

      ; create a new element
      (inject! prefix css))

    (case mode
      :class style-name
      :opts {:class prefix}

      prefix)))
