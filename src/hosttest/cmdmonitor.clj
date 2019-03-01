(ns hosttest.cmdmonitor)

(defonce gamestate (atom {}))

(defonce next-cmd-index (atom 0))

(defn increment-state
  []
  (let [updatedstate @hosttest.server/cmdlist])
  (when (> (count updatedstate) next-cmd-index)
    ))

(defonce _thread-throwaway
         (future (while true
                   (Thread/sleep 100)
                   (increment-state))))



(defn apply-cmd
  [cmd state]
  state)