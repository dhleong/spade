(ns ^{:author "Daniel Leong"
      :doc "spade.util"}
  spade.util
  (:require [clojure.string :as str]))

(defn factory->name [factory]
  (-> (.-name factory)
      (str/replace "_factory$" "")
      (str/replace "$" "_")))

(defn sanitize [s]
  (-> s
      str
      (str/replace #"[^A-Za-z0-9-_]" "_")))

(defn build-style-name [base style-key params]
  (cond
    style-key (str base "_" (sanitize style-key))
    (seq params) (throw (str "You must provide a key with params, for now"))
    :else base))
