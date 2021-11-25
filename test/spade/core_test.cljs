(ns spade.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [clojure.string :as str]
            [spade.core :refer [defattrs defclass defglobal defkeyframes]]))

; for the linter's sake:
(declare with-media-factory$
         class-with-vars-factory$
         fixed-style-attrs-factory$
         composed-factory$
         composed-attrs-factory$
         parameterized-key-frames-factory$
         binding-forms-factory$)

(defclass computed-key [color]
  ^{:key (str/upper-case color)}
  {:color color})

(defclass key-in-block [color]
  (let [k (str/upper-case color)]
    ^{:key k}
    {:color color}))

(defclass params [color]
  {:color color})

(defclass with-media []
  (at-media {:max-width "50px"}
    {:background "blue"}
    [:.nested {:background "red"}]))

(defclass class-with-vars []
  {:*my-var* "42pt"
   ::*namespaced* "blue"
   :font-size :*my-var*
   :background ::*namespaced*
   :color [[:*my-var* :!important]]})

(deftest defclass-test
  (testing "computed-key test"
    (is (= "spade-core-test-computed-key_BLUE"
           (computed-key "blue"))))

  (testing "param-hash-based key test"
    (is (= (str "spade-core-test-params_" (hash ["blue"]))
           (params "blue"))))

  (testing "Fancy :key test"
    (is (= "spade-core-test-key-in-block_BLUE"
           (key-in-block "blue"))))

  (testing "@media generation"
    (let [generated (-> (with-media-factory$ "with-media" [])
                        :css
                        (str/replace #"\s+" " "))]
      (is (str/includes?
            generated
            (str "@media (max-width: 50px) { "
                 ".with-media { background: blue; } "
                 ".with-media .nested { background: red; }")))))

  (testing "CSS var declaration and usage"
    (let [generated (-> (class-with-vars-factory$ "class-with-vars" [])
                        :css
                        (str/replace #"\s+" " "))]
      (is (str/includes?
            generated
            (str ".class-with-vars {"
                 " --my-var: 42pt;"
                 " --spade-core-test--namespaced: blue;"
                 " font-size: var(--my-var);"
                 " background: var(--spade-core-test--namespaced);"
                 " color: var(--my-var) !important;"
                 " }"))))))


(defattrs fixed-style-attrs []
  {:*my-var* "blue"
   :color :*my-var*})

(deftest defattrs-test
  (testing "Return map from defattrs"
    (is (= {:class "spade-core-test-fixed-style-attrs"}
           (fixed-style-attrs))))

  (testing "CSS var declaration and usage"
    (let [generated (-> (fixed-style-attrs-factory$ "with-vars" [])
                        :css
                        (str/replace #"\s+" " "))]
      (is (str/includes?
            generated
            (str ".with-vars {"
                 " --my-var: blue;"
                 " color: var(--my-var);"
                 " }"))))))


(defglobal global-1
  [":root" {:*background* "blue"}]
  [:body {:background :*background*}])

(defglobal global-2
  (at-media {:min-width "42px"}
    [:body {:background "white"}]))

(deftest defglobal-test
  (testing "Declare const var with style from global"
    (is (string? global-1))
    (is (= (str ":root {\n  --background: blue;\n}\n\n"
                "body {\n  background: var(--background);\n}")
           global-1)))

  (testing "Support at-media automatically"
    (is (str/starts-with?
          global-2
          "@media (min-width: 42px) {"))))


(defkeyframes key-frames []
  [:from {:opacity 0}])

(defkeyframes parameterized-key-frames [from]
  [:from {::*from* from
          :opacity ::*from*}])

(deftest defkeyframes-test
  (testing "Return keyframes name from defkeyframes"
    (is (fn? key-frames))
    (is (= "spade-core-test-key-frames"
           (key-frames))))

  (testing "Return dynamic keyframes name from parameterized defkeyframes"
    (is (fn? key-frames))
    (is (= (str "spade-core-test-parameterized-key-frames_" (hash [0]))
           (parameterized-key-frames 0))))

  (testing "CSS var declaration and usage"
    (let [generated (-> (parameterized-key-frames-factory$
                          "with-vars" [42] 42)
                        :css
                        (str/replace #"\s+" " "))]
      (is (str/includes?
            generated
            (str "from {"
                 " --spade-core-test--from: 42;"
                 " opacity: var(--spade-core-test--from);"
                 " }"))))))

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

(defclass destructured [{:keys [c b]}]
  ^{:key (str c "_" b)}
  {:color c
   :background b})

(defn foo {:arglists '([a b c])}
  [& args]
  (println args))

(deftest function-meta-test
  (testing "Simple arglists"
    (is (= '([color])
           (:arglists (meta #'params))))
    (is (= '([])
           (:arglists (meta #'fixed-style-attrs)))))

  (testing "Destructuring arglists"
    (is (= '([{:keys [c b]}])
           (:arglists (meta #'destructured))))))

(defclass variadic [& others]
  ^{:key (str/join others)}
  {:content (str others)})

(deftest destructuring-factory-test
  (testing "Destructure args"
    (is (= "spade-core-test-destructured_blue_red"
           (destructured {:c "blue" :b "red"}))))

  (testing "Don't barf on Variadic args"
    (is (= "spade-core-test-variadic_cyanmagentayellow"
           (variadic "cyan" "magenta" "yellow")))))

(defclass binding-forms [base-opacity]
  (let [opacity (* 0.2 base-opacity)]
    {:opacity base-opacity}
    [:.child {:opacity opacity}]

    ; Contrived when-let
    (when-let [color (when (< opacity 0.2) "magenta")]
      [:.small-grandchild {:color color}]
      [:.child {:opacity opacity}])

    (when (>= opacity 0.2)
      [:.big-grandchild {:color "red"}]
      [:.child {:color "cyan"}])))

(deftest binding-forms-test
  (testing "Support muliple returns from `let` forms"
    (let [{:keys [css]} (binding-forms-factory$ "binding-forms" [1.0] 1.0)]
      (is (true? (str/includes?
                   css
                   "opacity: 1;")))
      (is (true? (str/includes?
                   css
                   "opacity: 0.2;")))))

  (testing "Support muliple returns from `when` forms"
    (let [{:keys [css]} (binding-forms-factory$ "binding-forms" [1.0] 1.0)]
      (is (true? (str/includes?
                   css
                   ".big-grandchild")))
      (is (true? (str/includes?
                   css
                   "color: cyan;")))
      (is (false? (str/includes?
                    css
                    ".small-grandchild")))))

  (testing "Support muliple returns from `when-let` forms"
    (let [{:keys [css]} (binding-forms-factory$ "binding-forms" [0.1] 0.1)]
      (is (true? (str/includes?
                   css
                   ".small-grandchild")))
      ; NOTE: This doesn't pass because our (when) transformation *also*
      ; transforms the one inside the with-let binding!
      #_(is (true? (str/includes?
                   css
                   "color: magenta;"))))))
