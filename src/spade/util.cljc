(ns ^{:author "Daniel Leong"
      :doc "spade.util"}
  spade.util
  (:require [clojure.string :as str]))

(defn factory->name [factory]
  (-> (.-name factory)
      (str/replace "_factory$" "")
      (str/replace "$" "_")))

