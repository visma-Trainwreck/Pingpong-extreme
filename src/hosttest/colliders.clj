(ns hosttest.colliders
  (:require [hosttest.gamestats :as gamestats]))


(defn bounce-hori
  [state]
  (conj state {:velY (* (:velY state) -1)})
  )
(defn bounce-vert
  [state]
  (conj state {:velX (* (:velX state) -1)})
  )
(defn outofbounds
  [state]
  (let [y (:y state)]
    (cond
      (> y 400) (conj state {:y 400})
      (< y 0) (conj state {:y 0})
      :else state
      )))


(defn collidecheck
  [ball obj]
  (let [ballX (:x ball)
        ballY (:y ball)
        objX (:x obj)
        objY (:y obj)
        objrole (:role obj)
        ballvelX (:velX ball)]

    ;first we check if the ball is going in the direction of a obj, if it isnt we prevent the hitbox from triggering, to avoid the ball being stuck in a hitbox
    ;then we check the 4 corners of the ball to see if its withing the 4 corners of a object.
    (cond
      (and (= "player" objrole) (pos-int? ballvelX)) false
      (and (= "enemy" objrole) (neg? ballvelX)) false
      (and (and (> ballX objX) (< ballX (+ objX (first gamestats/playerSize)))) (and (> ballY objY) (< ballY (+ objY (second gamestats/playerSize))))) true
      (and (and (> (+ ballX (first gamestats/ballsize)) objX) (< (+ ballX (first gamestats/ballsize)) (+ objX (first gamestats/playerSize)))) (and (> ballY objY) (< ballY (+ objY (second gamestats/playerSize))))) true
      (and (and (> ballX objX) (< ballX (+ objX (first gamestats/playerSize)))) (and (> (+ ballY (second gamestats/ballsize)) objY) (< (+ ballY (second gamestats/ballsize)) (+ objY (second gamestats/playerSize))))) true
      (and (and (> (+ ballX (first gamestats/ballsize)) objX) (< (+ ballX (first gamestats/ballsize)) (+ objX (first gamestats/playerSize)))) (and (> (+ ballY (second gamestats/ballsize)) objY) (< (+ ballY (second gamestats/ballsize)) (+ objY (second gamestats/playerSize))))) true
      :else false
      ))
  )

(defn outofbounds
  [state]
  (let [y (:y state)]
    (cond
      (> y 400) (conj state {:y 400})
      (< y 0) (conj state {:y 0})
      :else state)))

(defn checkBoundsY
  [state]
  ;returns true if the state are below or above the game window
  (cond
    (> (:y state) gamestats/boundsYHigh) true
    (< (:y state) gamestats/boundsYLow) true
    :else false)
  )

(defn check-object-collideX
  [ball objlist]
  (if (or (collidecheck ball (first objlist)) (collidecheck ball (nth objlist 2)))
    true
    false))

(defn samedirectioncheck
  [ball entity]

  (let [ballVely (:velY ball)
        entityVely (:velY entity)]

    (cond
      (and (pos-int? entityVely) (pos-int? ballVely)) (conj ball {:velY (+ gamestats/freewind (:velY ball)) })
      (and (neg? entityVely) (neg? ballVely)) (conj ball {:velY (- gamestats/freewind ballVely) })
      :else (conj ball {:velx (+ gamestats/freewind (:velX ball))}))

    )


  )


(defn bounce-powerup
  [statelist]
  (let [ball (first (filter (fn [state] (if (= "object" (:type state))
                                          state
                                          nil)) statelist))
        player (first (filter (fn [state] (if (= "player" (:role state))
                                            state
                                            nil)) statelist))
        enemy (first ( filter (fn [state] (if (= "enemy" (:role state))
                                            state)) statelist))]

    (cond
      (collidecheck ball player)
        (bounce-vert (samedirectioncheck ball player))
      (collidecheck ball enemy)
        (bounce-vert (samedirectioncheck ball enemy))
      :else ball
      )


    ))

