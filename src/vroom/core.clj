(ns vroom.core
	(:require [org.httpkit.server :as server]
				[ring.middleware.reload :refer [wrap-reload]]
				[clojure.walk :as walk]
				[ring.middleware.params]
				[ring.middleware.keyword-params]
				[compojure.core :refer [defroutes GET POST]]
				[clojure.core.async :as async :refer [<! <!! >! >!! put! chan go go-loop]]
				[taoensso.sente :as sente]
				[taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
				[vroom.routes :as r]
				[cognitect.transit :as transit]
				[taoensso.sente.packers.transit :as sente-transit])
	(:import [org.joda.time DateTime ReadableInstant]))


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

(def joda-time-writer
  (transit/write-handler
    (constantly "m")
    (fn [v] (-> ^ReadableInstant v .getMillis))
    (fn [v] (-> ^ReadableInstant v .getMillis .toString))))

(def packer (sente-transit/->TransitPacker :json {:handlers {DateTime joda-time-writer}} {}))
  
(defroutes app-routes
	(GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
	(POST "/chsk" req (ring-ajax-post                req))
	r/routes)
	
(def app
	(-> app-routes
	  ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.params/wrap-params))

			
	
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