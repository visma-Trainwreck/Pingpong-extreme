(ns hosttest.the-field
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [hosttest.gamestats :as gamestats]
    #_[hosttest.cmdmonitor :as monitor]))

(defn setup
  []
  (q/frame-rate 120)
  (q/color-mode :hsb)
  {:ball 10})

(defn fib
  [n]
  (if (< n 2)
    1
    (+ (fib (dec n)) (- n 2))))

(defn update_state
  [state]
  @hosttest.cmdmonitor/gamestate)

(defn draw
  [gamestate]
  (q/clear)
  (dorun
    (map
      (fn [player]
        (q/fill 255 255 255)
        (q/rect 100 (:position (second player)) 50 50))(:players gamestate))))

(defn start-game
  []
  (q/defsketch kek
               :title "tadaaa"
               :size [800 600]
               :setup setup
               :draw draw
               :update update_state
               :features [:keep-on-top]
               :middleware [m/fun-mode]))
