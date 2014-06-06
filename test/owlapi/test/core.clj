(ns owlapi.test.core
  (:use [owlapi.core])
  (:use [clojure.test])
  (:use [clojure.java.io])
  (:import (java.util.logging LogManager)
           (java.io File)))

; "http://www.co-ode.org/ontologies/pizza/pizza.owl"
(def pizza (clojure.java.io/resource "pizza.owl"))

(defn load-logging-properties []
  (with-open [props (input-stream (resource "logging.properties"))]    
    (.readConfiguration (LogManager/getLogManager) props)))

(defn load-pizza 
	[] 
  (is (not (nil? pizza)))
  (load-ontology pizza))

(defn tempfile [pre post]
  (doto (File/createTempFile pre post) 
	(.delete) 
	(.deleteOnExit)
))

(deftest load-pizza-owl
  (is (load-pizza)))

(deftest pizza-doc-uri
  (is (= pizza) 
     (ontology-document-uri (load-pizza))))
 
(deftest formats
	(is (= (org.semanticweb.owlapi.io.RDFXMLOntologyFormat.) (owl-format :rdfxml)))
	(is (= (org.semanticweb.owlapi.io.OWLXMLOntologyFormat.) (owl-format :owlxml)))
)

(deftest load-then-remove-pizza-owl
  (is (not (remove-ontology! (load-pizza)))))

(deftest save-pizza
  (let [file (tempfile "pizza" ".owl")]
	  (is (not (.exists file)))
	  (save-ontology (load-pizza) file)
	  (is (.exists file))
	)
)

(deftest save-pizza-turtle
  (let [file (tempfile "pizza" ".owl")]
	  (is (not (.exists file)))
	  (save-ontology (load-pizza) file :owlxml)
	  (is (.exists file))
  )
)


(deftest copy-prefix-owl-turtle
	(copy-prefixes (load-pizza) (owl-format :turtle)))

(deftest all-formats
    ;; To avoid excessive logging from testing :obo serialization
  (load-logging-properties)
  (doseq [f (keys owl-format)]
	  (let [file (tempfile "pizza" ".owl")]
		  (is (not (.exists file)))
		  (save-ontology (load-pizza) file f)
		  (is (.exists file)))))

(deftest all-classes
	(is (= 100
	    (count (map str (classes (load-pizza)))))))

(deftest test-with-owl
  (with-owl 
    (is (empty? (loaded-ontologies)))
    (with-owl ; even nested
      (load-pizza)
      (is (not-empty (loaded-ontologies)))
    )
    (is (empty? (loaded-ontologies)))))

(deftest test-with-owl-manager
  (let [man (owl-manager)]
    (with-owl-manager man 
      (is (empty? (loaded-ontologies)))
      (load-pizza))
    (with-owl-manager man
        (is (not-empty (loaded-ontologies))))))
    
    
  