;; Copyright 2014 Red Hat, Inc, and individual contributors.
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;; http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns immutant.transactions-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [immutant.transactions :refer :all]
            [immutant.util :refer [in-container? set-log-level! reset-fixture]]
            [immutant.messaging :as msg]
            [immutant.caching   :as csh]))

(set-log-level! (or (System/getenv "LOG_LEVEL") :OFF))
(def queue (msg/queue "/queue/test" :durable false))
(def cache (csh/cache "tx-test" :transactional true))

(use-fixtures :each
  (fn [f]
    (.clear cache)
    (f)))

(defn attempt-transaction-external [& [f]]
  (try
    (with-open [conn (msg/connection :xa true)]
      (required
        (msg/publish queue "kiwi" :connection conn)
        (.put cache :a 1)
        (if f (f))))
    (catch Exception e
      (-> e .getCause .getMessage))))

(defn attempt-transaction-internal [& [f]]
  (try
    (required
      (msg/publish queue "kiwi" :xa true)
      (.put cache :a 1)
      (if f (f)))
    (catch Exception e
      (-> e .getCause .getMessage))))

(deftest verify-transaction-success-external
  (is (nil? (attempt-transaction-external)))
  (is (= "kiwi" (msg/receive queue :timeout 1000)))
  (is (= 1 (:a cache))))

(deftest verify-transaction-failure-external
  (is (= "force rollback" (attempt-transaction-external #(throw (Exception. "force rollback")))))
  (is (nil? (msg/receive queue :timeout 1000)))
  (is (nil? (:a cache))))

(deftest verify-transaction-success-internal
  (is (nil? (attempt-transaction-internal)))
  (is (= "kiwi" (msg/receive queue :timeout 1000)))
  (is (= 1 (:a cache))))

(deftest verify-transaction-failure-internal
  (is (= "force rollback" (attempt-transaction-internal #(throw (Exception. "force rollback")))))
  (is (nil? (msg/receive queue :timeout 1000)))
  (is (nil? (:a cache))))