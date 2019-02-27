(ns hosttest.gamestats)


(def boundsYLow 0)
(def boundsYHigh 575)
(def boundsXLow 0)
(def boundsXHigh 580)
(def ballspeed 4)
(def freewind 2)
(def aipower true)

(def playerspeed 5)
(def playerSize [20 200])
(def ballsize [20 20])




(def mock-state
  '({:role "player" :type "entity" :color 255 :x 20 :y 100 :velX 6 :velY 6}
     {:role "ball" :type "object" :color 255 :x 200 :y 200 :velX 5 :velY 5}
     {:role "enemy" :type "entity" :color 255 :x 760 :y 200 :velX 6 :velY 6}
     {:role "score" :type "logic" :player1 0 :player2 0}))

