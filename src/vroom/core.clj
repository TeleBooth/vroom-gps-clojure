(ns vroom.core
	(:require [org.httpkit.server :as server]
				[ring.middleware.reload :refer [wrap-reload]]
				[clojure.walk :as walk]
				[compojure.core :refer [defroutes GET POST]]
				[compojure.route :refer [not-found]]
				[ring.handler.dump :refer [handle-dump]]
				[taoensso.sente :as sente]
				[taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
				[vroom.routes :as r]))

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

;;stuff for sente
(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter) {})]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

(defroutes app
	(GET "/" [] welcome)
	(GET "/goodbye" [] goodbye)
	(GET "/about" [] about)
	(GET "/request-info" [] handle-dump)
	(GET "/hello/:name" [] hello)
	(GET "/calculator/:op/:a/:b" [] calculator)
	(GET "/transmit-gps" [] transmit-gps)
	(GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
	(POST "/chsk" req (ring-ajax-post                req))
	(not-found "Sorry, page not found"))
	
(defn welcome
	"A ring handler to process all requests sent to the webapp"
	[request]
	{:status 200
	:body "<h1> Hello, Clojure Worlds </h1>
	<p>I now use defroutes to manage incoming requests yay! I also use wrap-reload to make development easier! </p>"
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
	(server/run-server app
		{:port (Integer. port-number)}))
		
	  
(defn -dev-main
	"A very simple web server using Ring & Jetty"
	[port-number]
	(server/run-server (wrap-reload #'app)
		{:port (Integer. port-number)}))