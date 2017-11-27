(ns vroom.core
	(:require [ring.adapter.jetty :as jetty]
				[ring.middleware.reload :refer [wrap-reload]]
				[clojure.walk :as walk]
				[compojure.core :refer [defroutes GET]]
				[compojure.route :refer [not-found]]
				[ring.handler.dump :refer [handle-dump]]))

;;partial declarations for compilation
(defn welcome [request])
(defn goodbye [request])
(defn about [request])
(defn request-info [request])
(defn hello [request])
(defn calculator [request])

;;stuff for vroom
(defn connect-serial [serial-number])
(defn receive-gps [serial-port])
(defn transmit-gps [out-stream])

(defroutes app
	(GET "/" [] welcome)
	(GET "/goodbye" [] goodbye)
	(GET "/about" [] about)
	(GET "/request-info" [] handle-dump)
	(GET "/hello/:name" [] hello)
	(GET "/calculator/:op/:a/:b" [] calculator)
	(GET "/transmit-gps" [] transmit-gps)
	(not-found "Sorry, page not found"))
	
(defn welcome
	"A ring handler to process all requests sent to the webapp"
	[request]
	{:status 200
	:body "<h1> Hello, Clojure Worlds </h1>
	<p>I now use defroutes to manage incoming requests yay! </p>"
	:headers {}})
	
(defn goodbye
	"A song to wish you goodbye"
	[request]
	{:status 200
	:body "<p> goodbye, dude</p>"
	:headers {}})
	
(defn about
	"Information about the webstie developer"
	[request]
	{:request 200
	:body "Clojure is a pretty dope language so far"
	:header {}})

(defn hello
	"A personalized greeting showing the use of variable path elements"
	[request]
	(let [name (get-in request [:route-params :name])]
		{:status 200
		:body (str "Hello" name ". I got your name from the web URL")
		:headers {}}))

(def operands {"+" + "-" - "*" * ":" /})

(defn calculator
	"A simple calculator using the clojure map generated from the request URL"
	[request]
	(let [a (Integer. (get-in request [:route-params :a]))
			b (Integer. (get-in request [:route-params :b]))
			op (get-in request [:route-params :op])
			f (get operands op)]
			(if f
				{:status 200
				:body (str "Calculated result: "(f a b))
				:headers {}}
				{:status 404
				:body "Sorry, unknown operator. I only recognise + - * : (: is for division)"
				:headers {}})))
				
(defn connect-serial [serial-number])

(defn receive-gps [serial-port])

(defn transmit-gps [out-stream])

			
	
(defn -main
	"A very simple web server using Ring & Jetty"
	[port-number]
	(jetty/run-jetty app
		{:port (Integer. port-number)}))