(defproject clj-owlapi "0.3.1-SNAPSHOT"
  :description "OWL API wrapper for Clojure"
  :url "https://github.com/stain/clj-owlapi"
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
;                 [org.clojure/clojure-contrib "1.6.0"]
                 [net.sourceforge.owlapi/owlapi-contract "3.5.0"]
                 [org.clojars.stain/owlapi-jsonld "0.1.0"]

                ]
  :profiles {
    :dev { :dependencies [
        [org.slf4j/slf4j-jdk14 "1.7.7"]
      ]
    }
  }

)
