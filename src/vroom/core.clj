(ns vroom.core
	(:require [org.httpkit.server :as http-kit]
				[ring.middleware.reload :refer [wrap-reload]]
				[clojure.walk :as walk]
				[ring.middleware.defaults]
				[hiccup.core :as hiccup]
				[ring.middleware.params]
				[ring.middleware.keyword-params]
				[compojure.core :as comp :refer [defroutes GET POST]]
				[clojure.core.async :as async :refer [<! <!! >! >!! put! chan go go-loop]]
				[taoensso.sente :as sente]
				[taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
				[vroom.routes :as r]
				[cognitect.transit :as transit]
				[taoensso.sente.packers.transit :as sente-transit]
				[taoensso.timbre    :as timbre :refer (tracef debugf infof warnf errorf)]
				'[serial.core :as serial])
	(:import [java.io ByteArrayInputStream ByteArrayOutputStream]
			[java.lang Integer]))



;;stuff for serial
(def buffers-atom (atom []))
(def n 1024) ; 1 KiB

(defn exhaust-stream
  ([stream n] (exhaust-stream stream n '()))
  ([stream n buf-so-far]
   (let [new-buf (byte-array n)
         read-len (.read stream new-buf)
         buf-with-new (concat buf-so-far (take read-len new-buf))]
     (if (< read-len n)
       buf-with-new
       (recur stream n buf-with-new)))))

(def serial-port (serial/open "COM3" :baud-rate 9600))
(serial/listen! serial-port (fn [stream] (swap! buffers-atom concat (exhaust-stream stream n))))

;;stuff for sente

;;initialize the transit packer
(def packer (sente-transit/->TransitPacker :json {} {}))

(let [chsk-server
      (sente/make-channel-socket-server!
       (get-sch-adapter) {:packer packer})

      {:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      chsk-server]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

  

;; transit stuff

;; Serial read handler
;; to-be-written

;; Write data to a stream
(def out (ByteArrayOutputStream. 4096))
(def writer (transit/writer out :json))
(defn write-string [string]
	(transit/write writer string))

;; Read data from a stream
(def in (ByteArrayInputStream. (.toByteArray out)))
(def reader (transit/reader in :json))




;; Broadcast to be used for gps transmissions	  
(defonce broadcast-enabled?_ (atom true))

(defn start-example-broadcaster!
  "As an example of server>user async pushes, setup a loop to broadcast an
  event to all connected users every 10 seconds"
  []
  (let [broadcast!
        (fn [i]
          (let [uids (:any @connected-uids)]
            (debugf "Broadcasting server>user: %s uids" (count uids))
            (debugf "Broadcasting serial data sent through transit: %s" ())
			(doseq [uid uids]
              (chsk-send! uid))))]

    (go-loop [i 0]
      (<! (async/timeout 10000))
      (when @broadcast-enabled?_ (broadcast! i))
      (recur (inc i)))))



  
(defroutes app-routes
	(GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
	(POST "/chsk" req (ring-ajax-post                req))
	r/routes)
	
(def app
	(-> app-routes
	  ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.params/wrap-params))

(defn login-handler
  "Here's where you'll add your server-side login/auth procedure (Friend, etc.).
  In our simplified example we'll just always successfully authenticate the user
  with whatever user-id they provided in the auth request."
  [ring-req]
  (let [{:keys [session params]} ring-req
        {:keys [user-id]} params]
    (debugf "Login request: %s" params)
    {:status 200 :session (assoc session :uid user-id)}))
	
(defonce    web-server_ (atom nil)) ; (fn stop [])
(defn  stop-web-server! [] (when-let [stop-fn @web-server_] (stop-fn)))	  
(defn start-web-server! [& [port]]
  (stop-web-server!)
  (let [port (or port 0) ; 0 => Choose any available port
        ring-handler (wrap-reload #'app)

        [port stop-fn]
        (let [stop-fn (http-kit/run-server ring-handler {:port port})]
          [(:local-port (meta stop-fn)) (fn [] (stop-fn :timeout 100))])

        uri (format "http://localhost:%s/" port)]

    (infof "Web server is running at `%s`" uri)
    (try
      (.browse (java.awt.Desktop/getDesktop) (java.net.URI. uri))
      (catch java.awt.HeadlessException _))

    (reset! web-server_ stop-fn)))
	
(defn -main
	"A very simple web server using Ring & Jetty"
	[port-number]
	(start-web-server! 8000))