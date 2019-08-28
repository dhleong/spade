(ns spade.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [clojure.string :as str]
            [spade.core :refer [defattrs defclass defglobal defkeyframes]]))

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


(defglobal global-1
  [:body {:background "blue"}])

(defglobal global-2
  (at-media {:min-width "42px"}
    [:body {:background "white"}]))

(deftest defglobal-test
  (testing "Declare const var with style from global"
    (is (string? global-1))
    (is (= "body {\n  background: blue;\n}"
           global-1)))

  (testing "Support at-media automatically"
    (is (str/starts-with?
          global-2
          "@media (min-width: 42px) {"))))


(defkeyframes key-frames
  [:from {:opacity 0}])

(deftest defkeyframes-test
  (testing "Return keyframes name from defkeyframes"
    (is (fn? key-frames))
    (is (= "spade-core-test-key-frames"
           (key-frames)))))

(defclass composed [color]
  ^{:key color}
  {:composes (computed-key color)
   :background color})

(deftest defclass-compose-test
  (testing "computed-key test"
    (is (= "spade-core-test-computed-key_BLUE spade-core-test-composed_blue"
           (composed "blue")))

    (let [generated (:css (composed-factory$ "" ["blue"] "blue"))]
      (is (false? (str/includes? generated
                                 "color:")))
      (is (false? (str/includes? generated
                                 "composes:")))
      (is (true? (str/includes? generated
                                "background:"))))))


(defattrs composed-attrs [color]
  ^{:key color}
  {:composes (computed-key color)
   :background color})

(defclass compose-ception []
  {:composes (composed-attrs "blue")
   :background "#333"})

(deftest defattrs-compose-test
  (testing "computed-key test"
    (is (= "spade-core-test-computed-key_BLUE spade-core-test-composed-attrs_blue"
           (:class (composed-attrs "blue"))))

    (let [generated (:css (composed-attrs-factory$ "" ["blue"] "blue"))]
      (is (false? (str/includes? generated
                                 "color:")))
      (is (false? (str/includes? generated
                                 "composes:")))
      (is (true? (str/includes? generated
                                "background:")))))

  (testing "compose a defattrs"
    (is (= ["spade-core-test-computed-key_BLUE"
            "spade-core-test-composed-attrs_blue"
            "spade-core-test-compose-ception"]
           (str/split (compose-ception) #" ")))

    (let [generated (:css (composed-attrs-factory$ "" ["blue"] "blue"))]
      (is (false? (str/includes? generated
                                 "color:")))
      (is (false? (str/includes? generated
                                 "composes:")))
      (is (true? (str/includes? generated
                                "background:"))))))


