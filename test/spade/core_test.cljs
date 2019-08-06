(ns spade.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [clojure.string :as str]
            [spade.core :refer [defattrs defclass]]))

(defclass computed-key [color]
  ^{:key (str/upper-case color)}
  {:color color})

(defclass key-in-block [color]
  (let [k (str/upper-case color)]
    ^{:key k}
    {:color color}))

(defclass params [color]
  {:color color})

(deftest defclass-test
  (testing "computed-key test"
    (is (= "spade-core-test-computed-key_BLUE"
           (computed-key "blue"))))

  (testing "param-hash-based key test"
    (is (= (str "spade-core-test-params_" (hash ["blue"]))
           (params "blue"))))

  (testing "Fancy :key test"
    (is (= "spade-core-test-key-in-block_BLUE"
           (key-in-block "blue")))))

(defattrs fixed-style-attrs []
  {:color "blue"})

(deftest defattrs-test
  (testing "Return map from defattrs"
    (is (= {:class "spade-core-test-fixed-style-attrs"}
           (fixed-style-attrs)))))
