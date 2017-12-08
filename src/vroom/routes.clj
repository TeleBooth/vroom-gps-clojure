(ns vroom.routes
	(require
		[compojure.core :refer [defroutes GET POST]]
		[compojure.route :refer [not-found]]
		[ring.handler.dump :refer [handle-dump]]
		[vroom.views :as v]
		))

(defroutes routes
	(GET "/" [] v/welcome)
	(GET "/goodbye" [] v/goodbye)
	(GET "/about" [] v/about)
	(GET "/request-info" [] handle-dump)
	(GET "/hello/:name" [] v/hello)
	(GET "/calculator/:op/:a/:b" [] v/calculator)
	(not-found "Sorry, page not found"))