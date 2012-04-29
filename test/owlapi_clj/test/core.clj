(ns owlapi-clj.test.core
  (:use [owlapi-clj.core])
  (:use [clojure.test])
  (:import (java.io File)))

(defn load-pizza 
	[] (load-ontology "http://www.co-ode.org/ontologies/pizza/pizza.owl"))

(deftest load-pizza-owl
  (is true (load-pizza)))

(deftest load-then-remove-pizza-owl
  (is true (remove-ontology! (load-pizza))))

(deftest save-pizza
  (def file (doto (File/createTempFile "pizza" ".owl") (.delete) (.deleteOnExit)))
  (is false (.exists file))
  (save-ontology (load-pizza) file)
  (is true (.exists file))
)

