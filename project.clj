(defproject vroom "0.1.0-SNAPSHOT"
  :description "Vroom location server"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
				 [ring "1.6.2"]
				 [compojure "1.6.0"]
				 [serial-port "1.1.2"]]
  :main vroom.core
  :profiles {:dev
				{:source-paths ["src" "dev"]
				:main vroom.dev}})