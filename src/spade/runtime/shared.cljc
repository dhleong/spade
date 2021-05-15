(ns spade.runtime.shared)

(defprotocol IStyleContainer
  (mount-style!
    [this style-name css]
    "Ensure the style with the given name and CSS is available"))
