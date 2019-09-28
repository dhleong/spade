(ns spade.runner
  (:require [doo.runner :refer-macros [doo-all-tests]]
            [spade.core-test]
            [spade.util-test]))

(doo-all-tests #"spade\..*-test")
