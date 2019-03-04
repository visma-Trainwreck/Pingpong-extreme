(ns hosttest.cmdmonitor)

(defonce gamestate (ref {}))
(defonce next-cmd-index (ref 0))

(defn process-action
  [{:keys [t0 players] :as state}
   {:keys [action player ts]}]
  (assoc state :t0 (or t0 ts)
               :players (conj (or players #{}) player)
               :ts ts))

(defn process-cmd
  [state {:keys [action player ts] :as cmd} i]
  (println "Processed cmd: " cmd ", with index: " i)
  (if (and action player)
    (process-action state cmd)
    state)) 



(defn ?process-cmds
  [cmdlist-ref]
  (while (> (count @cmdlist-ref) @next-cmd-index)
    ; There are new cmds that we didn't process
    (println @cmdlist-ref @next-cmd-index)
    ; Make sure that the state change is transactional
    (dosync
      (when (> (count @cmdlist-ref) @next-cmd-index)
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

(defn run-monitor!
  [cmdlist-ref]
  (while true
    (Thread/sleep 100)
    (?process-cmds cmdlist-ref)))

