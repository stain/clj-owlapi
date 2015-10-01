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
  (with-owl
    (is (load-pizza))))

(deftest pizza-doc-uri
	(with-owl
	  (is (= pizza)
	     (ontology-document-uri (load-pizza)))))

(deftest formats
	(is (= (org.semanticweb.owlapi.io.RDFXMLOntologyFormat.) (owl-format :rdfxml)))
	(is (= (org.semanticweb.owlapi.io.OWLXMLOntologyFormat.) (owl-format :owlxml)))
  ; ...
)

(deftest test-mediatype
  (is (= "application/rdf+xml") (media-type :rdfxml))
  (is (= "text/turtle") (media-type :turtle))
  (is (= "application/owl+xml") (media-type :owlxml))
  (is (= "text/owl-manchester") (media-type :manchester))
  (is (= "text/owl-functional") (media-type :functional))
  (is (= "application/ld+json") (media-type :jsonld))
  (is (nil? (media-type :obo))) ; unknown
  (is (nil? (media-type :madeOneUp)))
)

(deftest mediatype-keys-in-formats
  (every? (set (keys owl-format)) (keys media-type)))

(deftest test-format-of-mediatype
  (is (= :turtle (format-of-media-type "text/turtle")))
  (is (= :rdfxml (format-of-media-type "application/rdf+xml")))
  ; ...
  (is (nil? (format-of-media-type "application/octet-stream")))
)

(deftest test-extensions
  (is (= ".rdf" (extension :rdfxml)))
  (is (= ".ttl" (extension :turtle)))
  (is (= ".owx" (extension :owlxml)))
  (is (= ".omn" (extension :manchester)))
  (is (= ".ofn" (extension :functional)))
  (is (= ".jsonld" (extension :jsonld)))
  (is (= ".tex" (extension :latex)))
  (is (= ".obo" (extension :obo))) ; TODO: Confirm
)

(deftest test-format-of-extension
  (is (= :rdfxml (format-of-extension ".rdf")))
  (is (= :turtle (format-of-extension ".ttl")))
  ; ...
  (is (nil? (format-of-extension ".txt")))
  )

(deftest extension-keys-in-formats
  (every? (set (keys owl-format)) (keys extension)))

(deftest load-then-remove-pizza-owl
	(with-owl
	  (is (not (remove-ontology! (load-pizza))))))

(deftest save-pizza
	(with-owl
	  (let [file (tempfile "pizza" ".owl")]
		  (is (not (.exists file)))
		  (save-ontology (load-pizza) file)
		  (is (.exists file)))))

(deftest save-pizza-turtle
	(with-owl
	  (let [file (tempfile "pizza" ".owl")]
		  (is (not (.exists file)))
		  (save-ontology (load-pizza) file :owlxml)
		  (is (.exists file)))))


(deftest copy-prefix-owl-turtle
	(with-owl
   (copy-prefixes (load-pizza) (owl-format :turtle))))

(deftest test-prefix-for-iri
  (with-owl
   (= "pizza" (prefix-for-iri
     (create-iri "http://www.co-ode.org/ontologies/pizza/pizza.owl#Pizza")
     (load-pizza)))))

(deftest all-formats
    ;; To avoid excessive logging from testing :obo serialization
  (load-logging-properties)
  (doseq [f (keys owl-format)]
	(with-owl
    (let [file (tempfile "pizza" ".owl")]
		   (is (not (.exists file)))
		   (save-ontology (load-pizza) file f)
		   (is (.exists file))))))

(deftest all-classes
	(with-owl
	  (is (= 100
		     (count (map str (classes (load-pizza))))))))

(deftest all-individuals
  (with-owl
    (is (= 5
          (count (map str (individuals (load-pizza))))))))

(deftest test-clear-all
  (with-owl
   (is (empty? (loaded-ontologies)))
   (load-pizza)
   (is (not-empty (loaded-ontologies)))
   (clear-ontologies!)
   (is (empty? (loaded-ontologies)))))

(deftest test-with-owl
    (is (=
	    (with-owl

	      (is (empty? (loaded-ontologies)))
	      (with-local-vars [manager nil]
	         (is (=
			        (with-owl ; even nested
                (var-set manager *owl-manager*)
			          (load-pizza)
			          (is (not-empty (loaded-ontologies)))
                (with-owl-manager @manager
                  (is (not-empty (loaded-ontologies))))
                ;; Ensure with-owl-manager did NOT clear the manager
                (is (not-empty (loaded-ontologies)))
			          :inner ; Ensure last value is returned from nested with-owl
			        ) :inner))
            (with-owl-manager @manager
                  ; Ensure that with-owl cleared the manager
                  (is (empty? (loaded-ontologies)))))
	      (is (empty? (loaded-ontologies)))
	       :outer ; Ensure last value is returned from with-owl
	      ) :outer)))

(deftest test-with-owl-manager
  (let [man (owl-manager)]
    (is (=
	    (with-owl-manager man
	      (is (empty? (loaded-ontologies)))
	      (load-pizza)
	      :return
      ) :return))
    (with-owl-manager man
        (is (not-empty (loaded-ontologies))))))
