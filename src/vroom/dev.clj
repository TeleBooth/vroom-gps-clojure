(ns vroom.dev
	(:require [ring.adapter.jetty :as jetty]
				[ring.middleware.reload :refer [wrap-reload]]
				[clojure.walk :as walk]
				[compojure.core :refer [defroutes GET]]
				[compojure.route :refer [not-found]]
				[ring.handler.dump :refer [handle-dump]]))
				
(defn -dev-main
	[port-number]
	(jetty/run-jetty (wrap-reload #'app)
		{:port (Integer. port-number)}))