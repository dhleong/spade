(ns hooks.defglobal)

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defmacro as-macro [the-name & body]
  `(def ~the-name ~@body))
