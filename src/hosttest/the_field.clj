(ns hosttest.the-field
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [hosttest.gamestats :as gamestats]
            [hosttest.cmdmonitor :as monitor]
            [hosttest.server :as game]))






(defn read_command
  []
  (game)
  )


(defn setup
  []
  (q/frame-rate 120)
  (q/color-mode :hsb)
  {:ball 10}
  )


(defn update_state
  [state]
  state
  )

(defn draw
  [_]
  (q/fill 255 255 255)
  (q/rect 200 200 50 50))



  (q/defsketch kek
               :title "tadaaa"
               :size [800 600]
               :setup setup
               :draw draw
               :update update_state
               :features [:keep-on-top]
               :middleware [m/fun-mode])


