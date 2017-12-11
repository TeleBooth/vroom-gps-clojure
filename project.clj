(defproject vroom "0.1.0-SNAPSHOT"
  :description "Vroom location server"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
				 [ring "1.6.2"]
				 [compojure "1.6.0"]
				 [serial-port "1.1.2"]
				 [http-kit "2.2.0"]
				 [com.cognitect/transit-clj "0.8.300"]
				 [com.taoensso/sente "1.11.0"]
				 [com.taoensso/timbre "4.7.4"]
				 [hiccup "1.0.5"]
				 [ring/ring-defaults "0.3.1"]
				 [clj-serial "2.0.3"]
				 [proto-repl "0.3.1"]
         [com.cemerick/pomegranate "1.0.0"]]

  :global-vars {*warn-on-reflection* true
                *assert* true}

  :main vroom.core
  :aot [vroom.core])
  (require 'cemerick.pomegranate.aether)
  (cemerick.pomegranate.aether/register-wagon-factory!
   "http" #(org.apache.maven.wagon.providers.http.HttpWagon.))
