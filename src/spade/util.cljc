(ns ^:no-doc spade.util
  (:require [clojure.string :as str]))

(defn factory->name [factory]
  (-> (.-name factory)
      (str/replace "_factory$" "")
      (str/replace #"[_$]" "-")
      (str/replace #"^-" "_")))

(defn sanitize [s]
  (-> s
      str
      (str/replace #"[^A-Za-z0-9-_]" "-")))

(defn params->key [p]
  (try
    (hash p)
    (catch #?(:cljs :default
              :clj Throwable) _
      nil)))

(defn build-style-name [base style-key params]
  (cond
    ; easy case: a key was provided
    style-key (str base "_" (sanitize style-key))

    (seq params) (if-let [pkey (params->key params)]
                   (str base "_" pkey)

                   (let [msg (str "WARNING: no key provided for " base)]
                     #?(:cljs (js/console.warn msg)
                        :clj (throw (Exception. msg)))
                     base))

    ; easiest case: no key is necessary
    :else base))
