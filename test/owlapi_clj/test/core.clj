(ns owlapi-clj.test.core
  (:use [owlapi-clj.core])
  (:use [clojure.test])
  (:import (java.io File)))

(defn load-pizza 
	[] (load-ontology "http://www.co-ode.org/ontologies/pizza/pizza.owl"))

(deftest load-pizza-owl
  (is (load-pizza)))

(deftest pizza-doc-uri
  (is (= "http://www.co-ode.org/ontologies/pizza/pizza.owl") 
     (ontology-document-uri (load-pizza))))
 
(deftest formats
	(is (= (org.semanticweb.owlapi.io.RDFXMLOntologyFormat.) (owl-format :rdfxml)))
	(is (= (org.semanticweb.owlapi.io.OWLXMLOntologyFormat.) (owl-format :owlxml)))
)

(deftest load-then-remove-pizza-owl
  (is (not (remove-ontology! (load-pizza)))))

(deftest save-pizza
  (def file (doto (File/createTempFile "pizza" ".owl") (.delete) (.deleteOnExit)))
  (is (not (.exists file)))
  (save-ontology (load-pizza) file)
  (is (.exists file))
)

(deftest save-pizza-turtle
  (def file (doto (File/createTempFile "pizza" ".owl") (.delete) ))
  (is (not (.exists file)))
  (save-ontology (load-pizza) file :turtle)
  (is (.exists file))
)


(deftest copy-prefix-owl-turtle
	(copy-prefixes (load-pizza) (owl-format :turtle)))


