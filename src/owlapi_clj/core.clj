(ns owlapi-clj.core
  (:import 
     (org.semanticweb.owlapi.apibinding OWLManager)
     (org.semanticweb.owlapi.model IRI)
  )
)

(defn owl-manager [] (OWLManager/createOWLOntologyManager))
	
(defn load-ontology 
	[uri] (load-ontology uri (owl-manager))
	[uri manager] [(.loadOntologyFromOntologyDocument manager (IRI/create uri)) manager])

(defn remove-ontology! [[ontology manager]]
	(.removeOntology manager ontology))

(defn ontology-document-uri [[ontology manager]]
	(.getOntologyDocumentIRI manager ))

(defn save-ontology
      [[ontology manager] file] (.saveOntology manager ontology (IRI/create (.toURI file))))

