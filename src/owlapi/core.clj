(ns owlapi.core
  (:require clojure.java.io)
  (:import 
     (org.semanticweb.owlapi.apibinding OWLManager)
     (org.semanticweb.owlapi.model IRI OWLOntology
              OWLOntologyAlreadyExistsException OWLOntologyRenameException))
)


(defn owl-manager [] 
   (OWLManager/createOWLOntologyManager))

(def ^:dynamic *owl-manager* (owl-manager))

(defmacro with-owl-manager [manager & body]
  `(binding [*owl-manager* ~manager]
     ~@body))

(defmacro with-owl [& body]
  `(with-owl-manager (owl-manager) ~@body))

(defn data-factory []
  (.getOWLDataFactory *owl-manager*))

(defn owl-types []
  (bean (data-factory)))

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
	
(defn load-ontology [uri] 
  (let [iri (IRI/create uri)]
    (or (first (.getOntologyIDsByVersion *owl-manager* iri))
        (first (.getOntology *owl-manager* iri))
        (try
          (.loadOntologyFromOntologyDocument *owl-manager* iri)
        (catch OWLOntologyAlreadyExistsException e
          (.getOntology *owl-manager* (.getOntologyID e)))
        (catch OWLOntologyRenameException e
          (.getOntology *owl-manager* (.getOntologyID e)))))))

(defn remove-ontology! [ontology]
	(.removeOntology *owl-manager* ontology))

(defn loaded-ontologies [] 
  (.getOntologies *owl-manager*))

(defn ontology-document-uri [ontology]
	(.getOntologyDocumentIRI *owl-manager* ontology))

(defn create-iri [iri]
  (IRI/create iri))

(defn as-iri [file]
	(IRI/create (.toURI (clojure.java.io/file file))))

(defn ontology-format [ontology]
	(.getOntologyFormat *owl-manager* ontology))

(defn prefixes [ontology]
  (let [format (ontology-format ontology)]
    (if (.isPrefixOWLOntologyFormat format)
      (.getPrefixName2PrefixMap format)
      {})))

(defn copy-prefixes [ontology new-format]
	(let [old-format (ontology-format ontology)]
		(if (and (.isPrefixOWLOntologyFormat new-format)
		         (.isPrefixOWLOntologyFormat old-format))
			(.copyPrefixesFrom new-format old-format))))	

(defn save-ontology 
	([ontology file] 
		(.saveOntology *owl-manager* ontology (as-iri file)))
	([ontology file save-format]
		(let [save-format (owl-format save-format)]
			(copy-prefixes ontology save-format)
			(.saveOntology *owl-manager* ontology save-format (as-iri file)))))

(defn classes [ontology]
	(.getClassesInSignature ontology))

(defn object-properties [ontology]
  (.getObjectPropertiesInSignature ontology))

(defn data-properties [ontology]
  (.getDataPropertiesInSignature ontology))

(defn annotation-properties [ontology]
  (.getAnnotationPropertiesInSignature ontology))

(defn ranges-of-property [property]
  (.getRanges property (loaded-ontologies)))
    
(defn annotations 
  ([entity]
    (set (mapcat #(.getAnnotations entity %) (loaded-ontologies))))
  ([entity annotation]
    (set (mapcat #(.getAnnotations entity % annotation) (loaded-ontologies)))))

