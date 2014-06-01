(ns owlapi-clj.core
  (:require clojure.java.io)
  (:import 
     (org.semanticweb.owlapi.apibinding OWLManager)
     (org.semanticweb.owlapi.model IRI)
  )
)

(defn owl-manager [] (OWLManager/createOWLOntologyManager))

(def owl-format
  ^{:doc "Known ontology formats supported by OWLAPI, ie. subclasses of OWLOntologyFormat" }
 {
  ;; Commented out entries fail to find a serializer (might need additional classpath bindings)
  
    ;:dl-html (uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxHTMLOntologyFormat.)
    ;:dl (uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxOntologyFormat.)
    :krss2 (de.uulm.ecs.ai.owlapi.krssparser.KRSS2OntologyFormat.)
    ;:krss (org.coode.owl.krssparser.KRSSOntologyFormat.)
    ;:latex-axioms (org.coode.owlapi.latex.LatexAxiomsListOntologyFormat.)
    :latex (org.coode.owlapi.latex.LatexOntologyFormat.)
    :obo (org.coode.owlapi.obo.parser.OBOOntologyFormat.)
    :manchester (org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat.)
    :functional (org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat.)
    :owlxml (org.semanticweb.owlapi.io.OWLXMLOntologyFormat.)
    :rdfxml (org.semanticweb.owlapi.io.RDFXMLOntologyFormat.)
    ;:tutorial (uk.ac.manchester.owl.owlapi.tutorialowled2011.OWLTutorialSyntaxOntologyFormat.)
    ;:labelfunctional (org.obolibrary.owl.LabelFunctionalFormat.)
    :default (org.semanticweb.owlapi.io.DefaultOntologyFormat.)
    :turtle (org.coode.owlapi.turtle.TurtleOntologyFormat.)
})
	
(defn load-ontology 
	([uri] (load-ontology uri (owl-manager)))
	([uri manager] [(.loadOntologyFromOntologyDocument manager (IRI/create uri)) manager]))

(defn remove-ontology! [[ontology manager]]
	(.removeOntology manager ontology))

(defn ontology-document-uri [[ontology manager]]
	(.getOntologyDocumentIRI manager ontology))

(defn as-iri [file]
	(IRI/create (.toURI (clojure.java.io/file file))))

(defn ontology-format [[ontology manager]]
	(.getOntologyFormat manager ontology))


(defn copy-prefixes [ontology-manager new-format]
	(let [old-format (ontology-format ontology-manager)]
		(if (and (.isPrefixOWLOntologyFormat new-format)
		         (.isPrefixOWLOntologyFormat old-format))
			(.copyPrefixesFrom new-format old-format))))	
			

(defn save-ontology 
	([[ontology manager] file] 
		(.saveOntology manager ontology (as-iri file)))
	([[ontology manager :as ont-man] file save-format]
		(let [save-format (owl-format save-format)]
			(copy-prefixes ont-man save-format)
			(.saveOntology manager ontology save-format (as-iri file)))))

(defn classes [[ontology & ignore]]
	(.getClassesInSignature ontology))

