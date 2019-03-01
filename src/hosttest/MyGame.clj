(ns hosttest.MyGame
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [hosttest.gamestats :as gamestats]
            [hosttest.colliders :as colliders]))

(defonce gamestate (atom '()))
(defonce currentactions (atom {:leftplayer 0 :rightplayer 0}))




(defn updategamestate
  [statelist]
  (swap! gamestate (fn [_] statelist))
  statelist
  )



(defn insertclientaction
  [keywords]

  (let [player (:player keywords)
        dir (:action keywords)]

    (if (= player "left")
      (cond
        (= dir "up")
        (swap! currentactions conj {:leftplayer (* -1 gamestats/playerspeed)})
        (= dir "down")
        (swap! currentactions conj {:leftplayer gamestats/playerspeed}))

      (cond
        (= dir "up")
        (swap! currentactions conj {:rightplayer (* -1 gamestats/playerspeed)})
        (= dir "down")
        (swap! currentactions conj {:rightplayer gamestats/playerspeed}))

      )
    )

  )





(defn scoreadd
  [statelist]

  (map (fn [state] (cond
                     (= "score" (:role state)) (let [ball (first (filter (fn [state] (if (= "ball" (:role state)) state nil)) statelist))]
                                                 (if (< (:x ball) -20)
                                                   (conj state {:player2 (+ 1 (:player2 state))})
                                                   (conj state {:player1 (+ 1 (:player1 state))})))
                     (= "player" (:role state)) (first gamestats/mock-state)
                     (= "enemy" (:role state)) (nth gamestats/mock-state 2)
                     (= "ball" (:role state)) (let [ball (second gamestats/mock-state)
                                                    ranvelX (+ 3 (rand-int 8))
                                                    ranvelY (+ 1 (rand-int 4))
                                                    dir (if (= 1 (- 1 (rand-int 2)))
                                                          1
                                                          -1)]
                                                (conj ball {:velX (* dir ranvelX) :velY ranvelY}))
                     :else state
                     )) statelist)
  )



(defn reset?
  [ball]
  (let [x (:x ball)]
    (if (or (< x -20) (> x 800))
      true
      false)))

(defn writescore
  [state]
  (let [player1score (:player1 state)
        player2score (:player2 state)
        player1 (if gamestats/aipower
                  "Nadal"
                  "Player")]
    (q/text (str player1 "   " player1score "        |        " player2score "   Mr AI") 350 50)
    )
  )

(defn writeballspeed
  [state]
  (let [velX (:velX state)
        velY (:velY state)
        rawspeed (Math/sqrt (+ (* velX velX) (* velY velY)))
        speed (int (Math/floor rawspeed))]
    (q/text (str "speed:  " speed) 400 90)
    ;(q/text (str "SpeedY: " velY "   speedX: " velX) 400 110)
    ))


(defn drawplayer
  [state]
  (let [x (:x state)
        y (:y state)
        color (:color state)]
    (q/fill color 255 255)
    (q/rect x y (first gamestats/playerSize) (second gamestats/playerSize))
    )
  )

(defn drawball
  [state]
  ;Draw baaaaaaalll
  (let [x (:x state)
        y (:y state)
        color (:color state)
        velX (Math/sqrt (Math/pow (:velX state) 2))
        velY (:velY state)]

    (dorun (cond
             (> 15 velX) (q/fill 0 255 255)
             (and (<= 15 velX) (> 10 velX)) (q/fill 0 125 255)
             (and (<= 10 velX) (> 4 velX)) (q/fill 0 0 255)
             :else (q/fill 0 0 0))
           (q/rect x y (first gamestats/ballsize) (second gamestats/ballsize))
           )))







(defn setup []
  ;tell quil what framerate / speed of the game and color mode, also gives the initialt state of the game.
  (q/frame-rate 120)
  (q/color-mode :hsb)
  gamestats/mock-state)


(defn drawIt
  [statelist]
  ;map through the list of states, and draw them. and returns the updated list. DOrun is there to make the lazy map do stuff
  (q/clear)
  (dorun (map (fn [state] (let [role (:role state)]
                            (cond (= "ball" role) (do (drawball state) (writeballspeed state))
                                  (= "player" role) (drawplayer state)
                                  (= "enemy" role) (drawplayer state)
                                  (= "score" role) (writescore state)))
                ) statelist))
  )


(defn player1
  [statelist]
  (q/background 240)
  (for [state statelist]
    (do (q/fill (:color state) 255 255)
        (q/with-translation
          [(/ (q/width) 2)
           (/ (q/height) 2)]
          (q/rect (:x state) (:y state) 20 20)))))

(defn newplayermower
  [state]

  (let [state (cond
                (= (:role state) "player")
                (conj state {:y (:leftplayer currentactions)})
                (= (:role state) "enemy")
                (conj state {:y (:rightplayer currentactions)}))]

    state
    )

  )



(defn playermover
  [state]
  ;keylistener! If a key is being pressed AND its the w or s key, then we do stuff, else we return the old state...and not move.
  ;update! it also sets the new velocity in the state depentant on which key is pressed!
  (let [state1 (cond
                 (and (q/key-pressed?) (= (q/raw-key) \w)) (conj state {:y (- (:y state) gamestats/playerspeed) :velY gamestats/playerspeed})
                 (and (q/key-pressed?) (= (q/raw-key) \s)) (conj state {:y (+ (:y state) gamestats/playerspeed) :velY (* -1 gamestats/playerspeed)})
                 :else (conj state {:velY 0}))]
    (colliders/outofbounds state1))
  )

(defn enemyMover
  [ball enemy]
  ;The enemy moves compared to the ball. If the ball is moving away from it, it will seek to move to the middle
  ;if the ball is moving towards the enemy it will match its own coordiants with the balls.
  (let [ballY (:y ball)
        ballv (:velX ball)
        enemyY (:y enemy)
        role (:role enemy)
        state (cond
                (and (= "enemy" role) (neg? ballv)) (cond
                                                      (< enemyY 200) (conj enemy {:y (+ (:y enemy) gamestats/playerspeed)})
                                                      (> enemyY 200) (conj enemy {:y (- (:y enemy) gamestats/playerspeed)})
                                                      :else enemy)
                (and (= "player" role) (pos-int? ballv)) (cond
                                                           (< enemyY 200) (conj enemy {:y (+ (:y enemy) gamestats/playerspeed)})
                                                           (> enemyY 200) (conj enemy {:y (- (:y enemy) gamestats/playerspeed)})
                                                           :else enemy)

                (< (- ballY 50) enemyY) (conj enemy {:y (- (:y enemy) gamestats/playerspeed)})
                (> (- ballY 50) enemyY) (conj enemy {:y (+ (:y enemy) gamestats/playerspeed)})
                :else enemy)]
    (colliders/outofbounds state)
    ))

(defn ballmover
  [statelist]
  ;the ball always moves, check if its above or below the top or bottom, and if it does we invert its Y velocity
  ;If the ball is hitting a player then we invert its X velocity
  ;if nothing is hiting the ball and we are within the game window the ball just continues
  (let [ball
        (if (colliders/checkBoundsY (second statelist))
          (colliders/bounce-hori (second statelist))
          (colliders/bounce-powerup statelist))
        x (:x ball)
        y (:y ball)
        velX (:velX ball)
        velY (:velY ball)]
    (conj ball {:x (+ x velX) :y (+ y velY)})
    ))

(defn update_main [statelist]
  ; check if the ball is out of bounds, and reset the state if it is!
  (let [ball (first (filter (fn [state] (if (= "ball" (:role state)) state nil)) statelist))]
    (if (reset? ball)
      (scoreadd statelist)
      ;map though the statelist and update the gamestats for outside to see
      (updategamestate (map (fn [state] (let [role (:role state)]
                                          (cond
                                            (and (= role "player") (not gamestats/aipower)) (newplayermower state)
                                            (and (= role "player") gamestats/aipower) (enemyMover (second statelist) (first statelist))
                                            (= role "ball") (ballmover statelist)
                                            (= role "enemy") (enemyMover (second statelist) (nth statelist 2))
                                            :else state))) statelist)))))

(defn gamestarter
  []
  (q/defsketch lala
               :title "tadaaa"
               :size [800 600]
               :setup setup
               :draw drawIt
               :update update_main
               :features [:keep-on-top]
               :middleware [m/fun-mode])
  )