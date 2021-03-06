(ns lambdacd.steps.support
  "DEPRECATED, use the namespaces under `lambdacd.stepsupport.*` instead. See CHANGELOG.md for details."
  {:deprecated "0.13.1"}
  (:require [clojure.core.async :as async]
            [lambdacd.stepsupport.output :as output]
            [lambdacd.stepsupport.metadata :as metadata]
            [lambdacd.stepsupport.chaining :as chaining]
            [lambdacd.execution.internal.execute-steps :as execute-steps]
            [lambdacd.execution.internal.serial-step-result-producer :as serial-step-result-producer]
            [clojure.walk :as walk]
            [lambdacd.step-id :as step-id]
            [lambdacd.execution.internal.util :as execution-util]
            [lambdacd.stepresults.merge-resolvers :as merge-resolvers]
            [lambdacd.stepresults.merge :as merge]
            [lambdacd.stepstatus.unify :as unify])
  (:import (java.io Writer StringWriter)))

(defn- merge-step-results-with-joined-output [a b]
  (merge/merge-two-step-results a b :resolvers [merge-resolvers/status-resolver
                                                       merge-resolvers/merge-nested-maps-resolver
                                                       merge-resolvers/join-output-resolver
                                                       merge-resolvers/second-wins-resolver]))

(defn- wrap-step-to-allow-nil-values [step]
  (fn [args ctx]
    (let [result (step args ctx)]
      (if (nil? result)
        args
        result))))

(defn- step-results-sorted-by-id [outputs]
  (->> outputs
       (into (sorted-map-by step-id/before?))
       (vals)))

; Duplicated instead of delegating because of injected-args, injected-ctx
(defn unify-results
  "Deprecated, this was never meant to be public."
  {:deprecated "0.13.1"}
  [step-results]
  (-> step-results
      (step-results-sorted-by-id)
      (merge/merge-step-results merge-step-results-with-joined-output)))

; Duplicated instead of delegating because of injected-args, injected-ctx
(defn- do-chain-steps-with-execute-steps [args ctx steps step-result-producer]
  (let [execute-step-result (execute-steps/execute-steps steps args ctx
                                                     :step-result-producer step-result-producer
                                                     :unify-results-fn unify-results)]
    (merge (unify-results (:outputs execute-step-result)) execute-step-result)))


; Duplicated instead of delegating because of injected-args, injected-ctx
(defn chain-steps
  "Deprecated, use `lambdacd.stepsupport.chaining/chain-steps` instead."
  {:deprecated "0.13.1"}
  [args ctx & steps]
   (do-chain-steps-with-execute-steps args ctx
                                      (map wrap-step-to-allow-nil-values steps)
                                      (serial-step-result-producer/serial-step-result-producer)))

; Duplicated instead of delegating because of injected-args, injected-ctx
(defn always-chain-steps
  "Deprecated, use `lambdacd.stepsupport.chaining/always-chain-steps` instead."
  {:deprecated "0.13.1"}
  ([args ctx & steps]
   (do-chain-steps-with-execute-steps args ctx
                                      (map wrap-step-to-allow-nil-values steps)
                                      (serial-step-result-producer/serial-step-result-producer :stop-predicate (constantly false)))))

(defn to-fn
  "Deprecated, this was never meant to be public."
  {:deprecated "0.13.1"}
  [form]
  (let [f# (first form)
        r# (next form)]
    (if (map? form)
      `(fn [& _# ] ~form)
      `(fn [args# ctx#] (~f# args# ctx# ~@r#)))))

(def ^{:doc "Deprecated, use `lambdacd.stepsupport.chaining/injected-args` instead."
       :deprecated "0.13.1"} injected-args)
(def ^{:doc "Deprecated, use `lambdacd.stepsupport.chaining/injected-ctx` instead."
       :deprecated "0.13.1"} injected-ctx)

; Duplicated instead of delegating because of injected-args, injected-ctx
(defn replace-args-and-ctx
  "Deprecated, this was never meant to be public."
  {:deprecated "0.13.1"}
  [args ctx]
  (fn [x]
    (cond
      (and
        (symbol? x)
        (= (var injected-args) (resolve x))) args
      (and
        (symbol? x)
        (= (var injected-ctx) (resolve x))) ctx
      :else x)))

; Duplicated instead of delegating because of injected-args, injected-ctx
(defn to-fn-with-args
  "Deprecated, this was never meant to be public."
  {:deprecated "0.13.1"}
  [form]
  (let [f# (first form)
        ctx (gensym "ctx")
        args (gensym "args")
        r# (walk/postwalk
             (replace-args-and-ctx args ctx)
             (next form))]
    (if (map? form)
      `(fn [& _# ] ~form)
      `(fn [~args ~ctx] (~f# ~@r#)))))

; Duplicated instead of delegating because of injected-args, injected-ctx
(defn- do-chaining [chain-fn args ctx forms]
  (let [fns (vec (map to-fn-with-args forms))]
    `(apply ~chain-fn ~args ~ctx ~fns)))

(defmacro chaining
  "Deprecated, use `lambdacd.stepsupport.chaining/chaining` instead."
  {:deprecated "0.13.1"}
  [args ctx & forms]
  (do-chaining chain-steps args ctx forms))

(defmacro always-chaining
  "Deprecated, use `lambdacd.stepsupport.chaining/always-chaining` instead."
  {:deprecated "0.13.1"}
  [args ctx & forms]
  (do-chaining always-chain-steps args ctx forms))

(defn last-step-status-wins
  "Deprecated, use `lambdacd.stepsupport.chaining/last-step-status-wins` instead."
  {:deprecated "0.13.1"}
  [step-result]
  (chaining/last-step-status-wins step-result))

(defn new-printer
  "Deprecated, use `lambdacd.stepsupport.output/new-printer` instead."
  {:deprecated "0.13.1"}
  []
  (output/new-printer))

(defn set-output
  "Deprecated, use `lambdacd.stepsupport.output/set-output` instead."
  {:deprecated "0.13.1"}
  [ctx msg]
  (output/set-output ctx msg))

(defn print-to-output
  "Deprecated, use `lambdacd.stepsupport.output/printed-output` instead."
  {:deprecated "0.13.1"}
  [ctx printer msg]
  (output/print-to-output ctx printer msg))

(defn printed-output
  "Deprecated, use `lambdacd.stepsupport.output/printed-output` instead."
  {:deprecated "0.13.1"}
  [printer]
  (output/printed-output printer))

(defn killed?
  "Deprecated, use `lambdacd.stepsupport.killable/killed?` instead."
  {:deprecated "0.13.1"}
  [ctx]
  @(:is-killed ctx))

(defmacro if-not-killed
  "Deprecated, use `lambdacd.stepsupport.killable/if-not-killed` instead."
  {:deprecated "0.13.1"}
  [ctx & body]
  `(if (killed? ~ctx)
     (do
       (async/>!! (:result-channel ~ctx) [:status :killed])
       {:status :killed})
     ~@body))

(defn merge-globals
  "Deprecated, this should not be necessary, a normal call to `merge-step-results` should keep all the globals."
  {:deprecated "0.13.1"}
  [step-results]
  (or (:global (reduce execution-util/keep-globals {} step-results)) {}))

(defn merge-step-results
  "Deprecated, use `lambdacd.stepresults.merge/merge-step-results` instead."
  {:deprecated "0.13.1"}
  [step-results]
  (merge/merge-step-results step-results merge/merge-two-step-results))

; not part of the public interface, just public for the macro
(defn ^:no-doc writer-to-ctx
  {:deprecated "0.13.1"}
  [ctx]
  (output/writer-to-ctx ctx))

(defmacro capture-output
  "Deprecated, use `lambdacd.stepsupport.output/capture-output` instead."
  {:deprecated "0.13.1"}
  [ctx & body]
  ; CODE DUPLICATED because it's tricky delegating this to another macro. Get rid of this deprecated code the next time you feel like changing it
  `(let [{x#      :writer
          buffer# :buffer} (writer-to-ctx ~ctx)
         body-result# (binding [*out* x#]
                        (do ~@body))]
     (if (associative? body-result#)
       (update body-result# :out #(if (nil? %) (str buffer#) (str buffer# "\n" % ))))))

(defn unify-only-status
  "DEPRECATED, use `lambdacd.stepstatus.unify/unify-only-status` instead."
  {:deprecated "0.13.1"}
  [unify-status-fn]
  (unify/unify-only-status unify-status-fn))


(defn assoc-build-metadata!
  "DEPRECATED, use `lambdacd.stepsupport.metadata/assoc-build-metadata!` instead."
  {:deprecated "0.13.1"}
  [ctx & kvs]
  (apply metadata/assoc-build-metadata! ctx kvs))
