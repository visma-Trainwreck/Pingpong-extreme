(ns hosttest.server
  (:require [clojure.java.io :as io]
            [ring.adapter.jetty :as srv]
            [compojure.core :refer :all]
            [hosttest.MyGame :as game]
            [compojure.route :as route]
            [selmer.parser :as selmer])
  (:import (java.net InetAddress ServerSocket Socket SocketException)
           (java.io OutputStream OutputStreamWriter PrintWriter BufferedReader InputStreamReader)
           (clojure.lang LineNumberingPushbackReader))
  (:use [clojure.main :only (repl)]))

(defonce commandolist (atom []))

(def mock-state
  '({:role "player" :type "entity" :color 255 :x 20 :y 100 :velX 6 :velY 6}
     {:role "ball" :type "object" :color 255 :x 200 :y 200 :velX 5 :velY 5}
     {:role "enemy" :type "entity" :color 255 :x 760 :y 200 :velX 6 :velY 6}
     {:role "score" :type "logic" :player1 0 :player2 0}))



(defn- on-thread [f]
  (doto (Thread. ^Runnable f)
    (.start)))

(defn- close-socket [^Socket s]
  (when-not (.isClosed s)
    (doto s
      (.shutdownInput)
      (.shutdownOutput)
      (.close))))


(defn inserter
  [^Socket s ])


(defn- accept-fn [^Socket s connections fun]
  (let [ins (.getInputStream s)
        outs (.getOutputStream s)]

    (println "incoming!!")
    (on-thread #(do
                  #_(dosync (commute connections conj s))
                  (try
                    (fun ins outs s)
                    (catch SocketException e))
                  (close-socket s)
                  (dosync (commute connections disj s))))))

(defstruct server-def :server-socket :connections)

(defn- create-server-aux [fun ^ServerSocket ss]
  (let [connections (ref #{})]
    (on-thread #(when-not (.isClosed ss)
                  (try
                    (accept-fn (.accept ss) connections fun)
                    (catch SocketException e))
                  (recur)))
    (struct-map server-def :server-socket ss :connections connections)))

(defn create-server
  "Creates a server socket on port. Upon accept, a new thread is
  created which calls:
  (fun input-stream output-stream)
  Optional arguments support specifying a listen backlog and binding
  to a specific endpoint."
  ([port fun backlog ^InetAddress bind-addr]
   (create-server-aux fun (ServerSocket. port backlog bind-addr)))
  ([port fun backlog]
   (create-server-aux fun (ServerSocket. port backlog)))
  ([port fun]
   (println "server started")
   (create-server-aux fun (ServerSocket. port))))

(defn close-server [server]
  (doseq [s @(:connections server)]
    (close-socket s))
  (dosync (ref-set (:connections server) #{}))
  (.close ^ServerSocket (:server-socket server)))

(defn connection-count [server]
  (count @(:connections server)))

;;; REPL on a socket

(defn- socket-repl [ins outs]
  (binding [*in* (LineNumberingPushbackReader. (InputStreamReader. ins))
            *out* (OutputStreamWriter. outs)
            *err* (PrintWriter. ^OutputStream outs true)]
    (repl)))

(defn create-repl-server
  "create a repl on a socket"
  ([port backlog ^InetAddress bind-addr]
   (create-server port socket-repl backlog bind-addr))
  ([port backlog]
   (create-server port socket-repl backlog))
  ([port]
   (create-server port socket-repl)))


(defn startgame
  [s]

  )


(defn operator
  [ins outs s]
  (loop [socket s]
    (println "before")

    #_(.readLine (io/reader socket))

    (println "write to client")
    (let [writer (PrintWriter. (.getOutputStream socket))]
      (println "in the let!")
          (.println writer mock-state )
          (.flush writer)
          (Thread/sleep 1000)

    (recur socket))

    )




  )
(defn testroute
  [id]
    (selmer/render-file "firstpage.html" {:name id})
  )

(defn clearcommandqueue
  [_]
  (swap! commandolist (fn [_] ([])))
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "noget andet \n"}
  )


(defn commandqueue
  [_]
  {:status 200 :body (pr-str @commandolist)}
  )
(defn cmdhandler
  [request]
  #_(clojure.pprint/pprint (keys request))
  (swap! commandolist (fn [xs] (conj xs (:query-string request))))
  (println @commandolist)
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "noget andet \n"})

(defn showgame
  [_]
  {:status 200 :body (pr-str @game/gamestate)}
  )

(defn gamecommand
  [request]
  (let [player ]))

(defroutes myroutes
           (GET "/showgame" request (showgame request))
           (GET "/cmd" request (cmdhandler request))
           (GET "/cmdqueue" request (commandqueue request))
           (GET "/test/:id" [id] (testroute id))
           (GET "/clearcmd" request (clearcommandqueue request))
           (GET "/gamecommand" request ))



(defn ourhandler
  [x]
    (println "yderste handler")
    (#'myroutes x)
  )

(defn -main
  []
  (future (srv/run-jetty  #'ourhandler {:port 8082}))
  (future (game/gamestarter))
  (println "STARTED")

  #_(create-server 9000 operator))
