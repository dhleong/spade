(ns spade.util-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [spade.util :refer [factory->name]]))

(deftest factory->name-test
  (testing "Never prefix with illegal characters"
    ; simulate a fn under advanced compilation having
    ; a name that starts with _
    (is (= "_advanced-compile"
           (factory->name #js {:name "_advanced_compile"}))))

  (testing "Never prefix with illegal characters"
    ; simulate a fn under simple compilation not
    ; having *any* name
    (is (seq (factory->name #js {:name ""})))))

