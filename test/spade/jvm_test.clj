(ns spade.jvm-test
  (:require [clojure.test :refer [deftest is testing]]
            [spade.runtime.atom :refer [->AtomStyleContainer]]
            [spade.runtime]
            [spade.core :refer [defclass with-styles-container]]))

(defclass blue-class []
  {:color "blue"})

(deftest with-styles-container-test
  (testing "Render styles to dynamically-provided Atom container"
    (let [styles (atom nil)
          container (->AtomStyleContainer styles)
          style-name (with-styles-container container
                       (blue-class))]
      (is (= "blue-class" style-name))
      (is (= ".blue-class{color:blue}"
             (get @styles style-name))))))

