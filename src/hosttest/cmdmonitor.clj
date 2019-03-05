(ns hosttest.cmdmonitor)

(defonce gamestate (ref {}))
(defonce next-cmd-index (ref 0))

(defn process-action
  [{:keys [t0 players] :as state}
   {:keys [action player ts]}]
  (let [t0 (or t0 ts)
        t (- ts t0)]
    (assoc state :t0 t0
                 :players (update-in players [player]
                                     (fn [{:keys [banan position actionls] :as m}]
                                       (let [pos (or position 50)
                                             pos (+ pos (case action
                                                          "up" 20
                                                          "down" -20
                                                          0))
                                             pos (min pos 100)
                                             pos (max pos 0)]
                                         (assoc m :t t :banan 42
                                                  :position pos
                                                  :actionls (conj actionls action)))))
                 #_(conj (or players #{}) player)
                 :ts ts
                 :t t)))

(defn process-cmd
  [state {:keys [action player ts] :as cmd} i]
  (println "Processed cmd: " cmd ", with index: " i)
  (try
    (if (and action player)
      (process-action state cmd)
      state)
    (catch Throwable e
      (println "der er sket en eller anden fucking error " e)
      state)))

(defn has-unprocessed-cmds
  "true if the passed cmd list has unprocessed cmds, false otherwise (fujibong)"
  [cmdlist-ref]
  (> (count @cmdlist-ref) @next-cmd-index))

(defn ?process-cmds
  [cmdlist-ref]
  (while (has-unprocessed-cmds cmdlist-ref)
    ; There are new cmds that we didn't process
    #_(println @cmdlist-ref @next-cmd-index)
    ; Make sure that the state change is transactional
    (dosync
      (when (has-unprocessed-cmds cmdlist-ref)
        (alter gamestate
               (fn [state]
                 (process-cmd
                   state
                   (get @cmdlist-ref
                        @next-cmd-index)
                   @next-cmd-index)))
        (alter next-cmd-index inc)))))

(defn reset-game!
  []
  (dosync
    (ref-set gamestate {})
    (ref-set next-cmd-index 0)))

(defn re-run
  [cmdlist-ref]
  (reset-game!)
  (while (has-unprocessed-cmds cmdlist-ref)
    (Thread/sleep 100)
    )
  @gamestate)

(defn run-monitor!
  [cmdlist-ref]
  (while true
    (Thread/sleep 100)
    (?process-cmds cmdlist-ref)))

