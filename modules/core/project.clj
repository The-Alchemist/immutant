(defproject org.immutant/immutant-core-module "1.0.3-SNAPSHOT"
  :parent [org.immutant/immutant-modules-parent _ :relative-path "../pom.xml"]
  :plugins [[lein-modules "0.1.1-SNAPSHOT"]
            [org.immutant/build-plugin "0.1.0-SNAPSHOT"]]
  :profiles {:provided
             {:dependencies [[org.immutant/immutant-common-module _]
                             [org.immutant/immutant-bootstrap-module _]
                             [org.projectodd/polyglot-core _]
                             [org.jboss.as/jboss-as-jmx _]]}}
  :dependencies [[org.projectodd.shimdandy/shimdandy-api _]
                 [org.projectodd.shimdandy/shimdandy-impl _]
                 [org.clojure/tools.nrepl "" :exclusions [[org.clojure/clojure]]]
                 [clj-stacktrace _ :exclusions [[org.clojure/clojure]]]
                 [clojure-complete _ :exclusions [[org.clojure/clojure]]]
                 [org.tcrawley/dynapath _]])
