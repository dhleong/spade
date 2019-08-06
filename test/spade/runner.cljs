(ns spade.runner
  (:require [doo.runner :refer-macros [doo-tests doo-all-tests]]
            [spade.core-test]))

(doo-all-tests #"spade\..*-test")
