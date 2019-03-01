(ns hosttest.server
  (:require [ring.adapter.jetty :as srv]
            [compojure.core :refer :all]
            [hosttest.MyGame :as game]
            [compojure.route :as route]
            [selmer.parser :as selmer]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            )
  (:use [clojure.main :only (repl)]))

(defonce cmdlist (atom []))


(defn testroute
  [id]
  (selmer/render-file "firstpage.html" {:name id})
  )

(defn clearcommandqueue
  [_]
  (swap! cmdlist (fn [_] ([])))
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "noget andet \n"}
  )

(defn cmdqueue
  [_]
  {:status 200 :body (pr-str @cmdlist)})

(defn parse-cmd
  [{{:keys [player action] :as params} :params}]
  (println params "|" player action)
  (when (and player action)
    {:player player
     :action action}))

(defn cmdhandler
  [request]
  (when-let [cmd (parse-cmd request)]
    (swap! cmdlist conj cmd))
  (println
    (take 2 (reverse @cmdlist)))
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "noget andet \n"})

(defn showgame
  [_]
  {:status 200 :body (pr-str @game/gamestate)})



(defroutes myroutes
           (GET "/showgame" request (showgame request))
           (GET "/cmd" request (cmdhandler request))
           (GET "/cmdqueue" request (cmdqueue request))
           (GET "/test/:id" [id] (testroute id))
           (GET "/clearcmd" request (clearcommandqueue request))
           (route/not-found "Page not found"))


(defn ourhandler
  [x]
  (((comp wrap-params wrap-keyword-params) #'myroutes)
    x))

(defn -main
  []
  (future (srv/run-jetty #'ourhandler {:port 8082}))
  (future (game/gamestarter))
  (println "STARTED")
  )
