#!/usr/bin/env nbb
;; Live gate for `src/statute/facts.cljc`.
;;
;; Re-fetches the official eCFR APIs and checks five things:
;;
;;   1. POSITIVE (headings) -- for every catalog entry and every absence
;;      `see-instead`, walking `:statute/cfr-node` from the title root lands on
;;      a node whose `label_description` is byte-identical to
;;      `:statute/verified-label`.
;;
;;   2. POSITIVE (text) -- for every entry carrying `:statute/verified-quotes`,
;;      each span occurs byte-exactly in the section text returned by the
;;      versioner full-text API. A heading can survive a repeal of the sentence
;;      underneath it, so a heading-only gate reports `verified` for a catalog
;;      whose operative claims have been gutted. This half is what makes the
;;      notes in facts.cljc, not just its citations, falsifiable.
;;
;;   3. NEGATIVE (structure) -- for every absence:
;;        * `:absence/absent-part`  -- scanning the subtree under
;;          `:statute/under` finds NO part with that identifier, AND the paired
;;          `:absence/control-part` IS found in that same subtree;
;;        * `:absence/absent-label` -- scanning that subtree finds NO node whose
;;          label matches the pattern, AND the paired `:absence/control-label`
;;          pattern DOES match there.
;;
;;   4. NEGATIVE (text) -- for every `:absence/absent-text`, the pattern does
;;      NOT occur in the fetched regulation text, AND the paired
;;      `:absence/control-text` pattern DOES occur in that same document.
;;
;;      The controls are not decoration. A negative's failure mode is that it
;;      passes for free: fetch the wrong tree, or an empty one, and `nothing
;;      matched` looks exactly like `confirmed still absent`. A control that
;;      must match turns that silence into an explicit could-not-answer. Text
;;      absences need this most -- an empty HTTP body satisfies every claim of
;;      the form `these words do not appear`.
;;
;;   5. FLOOR -- a run that checked fewer than `--min` headings, fewer than
;;      `--min-quotes` quotes, fewer than `--min-absences` absences, or zero of
;;      any kind, is a could-not-answer.
;;
;; Why walk the tree rather than match the URL. Hierarchical CFR identifiers
;; nest as substrings of one another (part `1` is a prefix of part `1010`, and
;; section `10.3` of `10.30`), so a string match can succeed against the wrong
;; node. Walking explicit [type identifier] steps cannot pass by accident. A
;; step may be `"*"`, used only where eCFR generates the identifier rather than
;; the CFR citing it (the `subpart` and `subject_group` nodes inside 26 CFR
;; part 301 have identifiers like `ECFR1b5d05d4bfe19f9`); a wildcard that
;; resolves to more than one node is reported as a failure, not silently taken.
;;
;; EVIDENCE FLOOR. This script refuses to report a pass it did not earn:
;;   * exit 2 -- could not answer (network failure, unparseable catalog, a
;;               title whose API endpoint is not declared, a control that
;;               stopped matching, zero checks of some kind). NOT a pass, and
;;               deliberately neither 0 nor 1.
;;   * exit 1 -- answered, and at least one citation, quote or absence is wrong.
;;   * exit 0 -- answered, everything checked, and every floor was met.
;; "Nothing was checked" and "nothing was wrong" must not share an exit code.
;;
;; Usage:
;;   nbb tools/verify_citations.cljs [--min N] [--min-quotes N]
;;                                   [--min-absences N] [--gap-ms N] [--quiet]

(ns verify-citations
  (:require [kotoba.lang.text :as str]
            [clojure.edn :as edn]
            ["fs" :as fs]
            ["path" :as path]))

(def argv (vec (drop 3 (js->clj js/process.argv))))
(defn flag? [f] (boolean (some #{f} argv)))
(defn flag-val [f default]
  (let [i (.indexOf argv f)]
    (if (neg? i) default (get argv (inc i) default))))

(def quiet? (flag? "--quiet"))
(def min-citations (js/parseInt (flag-val "--min" "38") 10))
(def min-quotes    (js/parseInt (flag-val "--min-quotes" "15") 10))
(def min-absences  (js/parseInt (flag-val "--min-absences" "4") 10))

(defn say [& xs] (when-not quiet? (println (str/join " " xs))))
(defn die [code & xs]
  (binding [*print-fn* *print-err-fn*] (println (str/join " " xs)))
  (js/process.exit code))

;; ---------------------------------------------------------------- catalog ---
;; facts.cljc is Clojure source, not EDN. Rather than depend on a reader that
;; would have to evaluate `ns`/`defn` forms, pull the literal values out by
;; locating their `(def ...)` heads and reading the first EDN form after each.

(def facts-path
  (let [rel (path/join "src" "statute" "facts.cljc")]
    (loop [dir (js/process.cwd) hops 0]
      (let [cand (path/join dir rel)]
        (cond
          (fs/existsSync cand) cand
          (> hops 3) (die 2 "CANNOT-ANSWER: could not locate" rel
                          "from working directory" (js/process.cwd))
          :else (recur (path/dirname dir) (inc hops)))))))

(def ^:private ws #{" " "\t" "\n" "\r" ","})

(defn- skip-ws [s i]
  (let [n (count s)]
    (loop [i i] (if (and (< i n) (ws (nth s i))) (recur (inc i)) i))))

(defn- skip-string [s i]
  (let [n (count s)]
    (loop [i (inc i)]
      (cond
        (>= i n)            i
        (= (nth s i) "\\")  (recur (+ i 2))
        (= (nth s i) "\"")  (inc i)
        :else               (recur (inc i))))))

(defn- value-start
  "Given source starting just after a `(def name`, return the index of the first
  character of the value form, stepping over an optional docstring."
  [s i]
  (let [i (skip-ws s i)]
    (if (and (< i (count s)) (= (nth s i) "\""))
      (skip-ws s (skip-string s i))
      i)))

(defn- form-end
  "Index just past the balanced form starting at `i`."
  [s i]
  (let [n (count s)
        open (nth s i)
        close (case open "(" ")" "[" "]" "{" "}" nil)]
    (if-not close
      (die 2 "CANNOT-ANSWER: expected a collection at offset" i "of" facts-path)
      (loop [i i depth 0]
        (cond
          (>= i n) (die 2 "CANNOT-ANSWER: unbalanced form in" facts-path)
          (= (nth s i) "\"") (recur (skip-string s i) depth)
          (= (nth s i) ";")  (recur (loop [j i] (if (or (>= j n) (= (nth s j) "\n")) j (recur (inc j)))) depth)
          (= (nth s i) open)  (recur (inc i) (inc depth))
          (= (nth s i) close) (if (= depth 1) (inc i) (recur (inc i) (dec depth)))
          :else (recur (inc i) depth))))))

(def src
  (try (fs/readFileSync facts-path "utf8")
       (catch :default e
         (die 2 "CANNOT-ANSWER: cannot read" facts-path "--" (.-message e)))))

(defn read-form-after [s head]
  (let [i (.indexOf s head)]
    (when (neg? i)
      (die 2 "CANNOT-ANSWER:" (pr-str head) "not found in" facts-path))
    (let [vs (value-start s (+ i (count head)))]
      (try (edn/read-string (subs s vs (form-end s vs)))
           (catch :default e
             (die 2 "CANNOT-ANSWER: cannot read the form after" (pr-str head)
                  "--" (.-message e)))))))

(def api-endpoints (read-form-after src "(def ecfr-structure-api"))
(def full-text-api (read-form-after src "(def ecfr-full-text-api"))
(def catalog       (read-form-after src "(def catalog"))
(def absences      (read-form-after src "(def absences"))

;; ------------------------------------------------------------------ fetch ---

(def gap-ms (js/parseInt (flag-val "--gap-ms" "400") 10))

(defn- sleep [ms] (js/Promise. (fn [resolve] (js/setTimeout resolve ms))))

(def ^:private retryable #{429 500 502 503 504})

(defn- fetch-body
  "Fetch `url`, retrying a rate-limit or transient server error with a growing
  pause. A gate that reports CANNOT-ANSWER because the server asked it to slow
  down is a gate people learn to ignore, so this distinguishes `the server said
  no` from `the server said wait`."
  [url as]
  (letfn [(attempt [n]
            (-> (js/fetch url)
                (.then (fn [r]
                         (cond
                           (.-ok r) (if (= as :json) (.json r) (.text r))
                           (and (< n 4) (retryable (.-status r)))
                           (do (say "  (HTTP" (.-status r) "-- retry" n "of 3 for" (str url ")"))
                               (.then (sleep (* 2000 n)) (fn [_] (attempt (inc n)))))
                           :else (die 2 "CANNOT-ANSWER: HTTP" (.-status r) "from" url))))
                (.catch (fn [e]
                          (if (< n 4)
                            (.then (sleep (* 2000 n)) (fn [_] (attempt (inc n))))
                            (die 2 "CANNOT-ANSWER: fetch failed for" url "--"
                                 (.-message e)))))))]
    (attempt 1)))

(defn- fetch-sequential
  "Fetch `[{:k :url :as}]` one at a time with `gap-ms` between requests, and
  return a promise of {k body}. Issued in parallel these requests draw HTTP 429
  from the eCFR API -- measured, not assumed."
  [specs]
  (reduce (fn [p {:keys [k url as]}]
            (.then p (fn [acc]
                       (.then (fetch-body url as)
                              (fn [v] (.then (sleep gap-ms)
                                             (fn [_] (assoc acc k v))))))))
          (js/Promise.resolve {})
          specs))

;; -------------------------------------------------------------------- xml ---

(defn section-text
  "Strip tags and collapse whitespace, so a recorded quote can be compared
  against running text without depending on the XML's line wrapping.

  Numeric character references are decoded generically rather than from a
  hand-written table: the CFR's running text is full of them (section signs, em
  dashes, curly quotes), and a table only covers the ones somebody happened to
  hit. An undecoded entity does not throw -- it silently turns a quote that IS
  present into one that is not, which reads as drift. `&amp;` is unescaped last,
  otherwise `&amp;lt;` would decode twice."
  [xml]
  (-> xml
      (str/replace #"<[^>]*>" "")
      (str/replace #"&#x([0-9A-Fa-f]+);"
                   (fn [[_ h]] (js/String.fromCodePoint (js/parseInt h 16))))
      (str/replace #"&#([0-9]+);"
                   (fn [[_ d]] (js/String.fromCodePoint (js/parseInt d 10))))
      (str/replace "&lt;" "<")
      (str/replace "&gt;" ">")
      (str/replace "&quot;" "\"")
      (str/replace "&apos;" "'")
      (str/replace "&amp;" "&")
      (str/replace #"\s+" " ")
      (str/trim)))

;; ------------------------------------------------------------------- walk ---

(defn walk-node
  "Descend `tree` following [[type identifier] ...]. An identifier of `\"*\"`
  matches any child of that type; the walk branches and must converge on
  exactly one node. Returns {:node n} | {:missing path} | {:ambiguous n}."
  [tree node-path]
  (loop [frontier [tree] steps node-path]
    (if (empty? steps)
      (if (= 1 (count frontier))
        {:node (first frontier)}
        {:ambiguous (count frontier)})
      (let [[t i] (first steps)
            nxt (vec (for [n frontier
                           c (get n "children")
                           :when (and (= (get c "type") t)
                                      (or (= i "*") (= (get c "identifier") i)))]
                       c))]
        (if (empty? nxt)
          {:missing (first steps)}
          (recur nxt (rest steps)))))))

(defn- descendants [node]
  (tree-seq #(seq (get % "children")) #(get % "children") node))

(defn find-parts
  "Every descendant of `node` of type `part` whose identifier is `id`.
  Recursive on purpose: a part can be re-adopted under a different subchapter
  than the one it used to live in, and an absence that only checked the old
  address would keep passing."
  [node id]
  (filterv #(and (= "part" (get % "type")) (= id (get % "identifier")))
           (rest (descendants node))))

(defn find-labels
  "Every descendant of `node` whose `label_description` matches `re`."
  [node re]
  (filterv #(re-find re (or (get % "label_description") "")) (descendants node)))

;; ------------------------------------------------------------------- main ---

(defn positives
  "Every heading this run must confirm EXISTS."
  []
  (concat
   (for [[iso es] catalog, e es]
     {:what (str iso " " (:statute/id e))
      :title (:statute/cfr-title e) :node (:statute/cfr-node e)
      :label (:statute/verified-label e)})
   (for [a absences
         :let [s (:absence/see-instead a)]
         :when s]
     {:what (str "absence " (:absence/id a) " see-instead")
      :title (:statute/cfr-title s) :node (:statute/cfr-node s)
      :label (:statute/verified-label s)})))

(defn quotes
  "Every section span this run must confirm is still in the live text.

  `:statute/verified-quotes` is a VECTOR: one section routinely carries more
  than one load-bearing claim (2 CFR 25.100 names both the identifier and the
  system; 31 CFR 10.2 sets both the breadth of `practice` and the specific acts
  inside it). Each span becomes its own check, so losing one of them is a
  failure rather than a silently smaller gate."
  []
  (for [[iso es] catalog, e es
        :when (seq (:statute/verified-quotes e))
        [i q] (map-indexed vector (:statute/verified-quotes e))]
    {:what (str iso " " (:statute/id e) " quote#" (inc i))
     :cfr-title (:statute/cfr-title e)
     :url (when-let [base (get full-text-api (:statute/cfr-title e))]
            (str base "?part=" (:statute/quote-part e)
                 "&section=" (:statute/quote-section e)))
     :quote q}))

(defn text-absences
  "Every absence whose claim is about words inside a regulation's text."
  []
  (for [a absences :let [t (:absence/absent-text a)] :when t]
    {:what (str "absence " (:absence/id a))
     :cfr-title (:statute/cfr-title t)
     :url (when-let [base (get full-text-api (:statute/cfr-title t))]
            (str base "?part=" (:statute/part t)
                 (when-let [s (:statute/section t)] (str "&section=" s))))
     :pattern (:statute/pattern t)
     :control (get-in a [:absence/control-text :statute/pattern])}))

(defn structure-negatives
  "Every part, and every label pattern, this run must confirm is NOT there."
  []
  (concat
   (for [a absences :let [p (:absence/absent-part a)] :when p]
     {:kind :part :what (str "absence " (:absence/id a))
      :title (:statute/cfr-title p) :under (:statute/under p)
      :part (:statute/part p)
      :control (get-in a [:absence/control-part :statute/part])})
   (for [a absences :let [l (:absence/absent-label a)] :when l]
     {:kind :label :what (str "absence " (:absence/id a))
      :title (:statute/cfr-title l) :under (:statute/under l)
      :pattern (:statute/pattern l)
      :control (get-in a [:absence/control-label :statute/pattern])})))

(defn -main []
  (let [pos       (vec (positives))
        qs        (vec (quotes))
        tabs      (vec (text-absences))
        negs      (vec (structure-negatives))
        _ (when (empty? pos) (die 2 "CANNOT-ANSWER: catalog produced zero headings to check"))
        _ (when (empty? qs)  (die 2 "CANNOT-ANSWER: catalog produced zero quotes to check"))
        ;; Every title any check needs a structure tree for.
        need-struct (into (sorted-set) (concat (map :title pos) (map :title negs)))
        _ (doseq [t need-struct]
            (when-not (get api-endpoints t)
              (die 2 "CANNOT-ANSWER: no structure endpoint declared for CFR title" t)))
        _ (doseq [q qs]
            (when-not (:url q)
              (die 2 "CANNOT-ANSWER: no full-text endpoint declared for CFR title"
                   (:cfr-title q) "needed by" (:what q))))
        _ (doseq [t tabs]
            (when-not (:url t)
              (die 2 "CANNOT-ANSWER: no full-text endpoint declared for CFR title"
                   (:cfr-title t) "needed by" (:what t)))
            (when-not (:control t)
              (die 2 "CANNOT-ANSWER:" (:what t)
                   "has no :absence/control-text -- an empty document would confirm it for free")))
        _ (doseq [n negs]
            (when-not (:control n)
              (die 2 "CANNOT-ANSWER:" (:what n)
                   "has no control -- an empty subtree would confirm it for free")))
        struct-specs (for [t need-struct] {:k t :url (get api-endpoints t) :as :json})
        doc-urls     (distinct (concat (map :url qs) (map :url tabs)))
        doc-specs    (for [u doc-urls] {:k u :url u :as :text})]
    (say "Fetching" (count struct-specs) "structure tree(s) and"
         (count doc-specs) "document(s) from eCFR ...")
    (-> (fetch-sequential (concat struct-specs doc-specs))
        (.then
         (fn [bodies]
           (let [trees (into {} (for [t need-struct] [t (js->clj (get bodies t))]))
                 docs  (into {} (for [u doc-urls] [u (section-text (get bodies u))]))
                 fails (atom [])
                 fail! (fn [& xs] (swap! fails conj (str/join " " xs)))
                 ;; A control that stopped matching does not mean the catalog
                 ;; is wrong -- it means this run could not tell. That is exit
                 ;; 2, not exit 1, and keeping the two accumulators separate is
                 ;; what stops `we checked nothing` from being reported as
                 ;; `we found something wrong`.
                 cannot (atom [])
                 cannot! (fn [& xs] (swap! cannot conj (str/join " " xs)))]

             ;; 1. headings
             (doseq [{:keys [what title node label]} pos]
               (let [r (walk-node (get trees title) node)]
                 (cond
                   (:missing r)   (fail! "MISSING" what "-- node path did not resolve at"
                                         (pr-str (:missing r)))
                   (:ambiguous r) (fail! "AMBIGUOUS" what "-- wildcard resolved to"
                                         (:ambiguous r) "nodes")
                   :else
                   (let [actual (get (:node r) "label_description")]
                     (when-not (= actual label)
                       (fail! "DRIFT" what "-- recorded" (pr-str label)
                              "but eCFR now says" (pr-str actual)))))))
             (say "  headings checked:" (count pos))

             ;; 2. quotes
             (doseq [{:keys [what url quote]} qs]
               (let [t (get docs url)]
                 (cond
                   (str/blank? t) (cannot! "CANNOT-CHECK" what "-- empty document from" url)
                   (not (str/includes? t quote))
                   (fail! "QUOTE-DRIFT" what "-- span no longer present:"
                          (pr-str (subs quote 0 (min 60 (count quote))))))))
             (say "  quotes checked:" (count qs))

             ;; 3. structure negatives
             (doseq [{:keys [kind what title under part pattern control]} negs]
               (let [r (walk-node (get trees title) under)]
                 (if (or (:missing r) (:ambiguous r))
                   (cannot! "CANNOT-CHECK" what "-- the subtree it scans did not resolve:"
                          (pr-str (or (:missing r) (:ambiguous r))))
                   (let [root (:node r)]
                     (case kind
                       :part
                       (let [hits (find-parts root part)
                             ctrl (find-parts root control)]
                         (cond
                           (empty? ctrl)
                           (cannot! "CONTROL-FAILED" what "-- control part" (pr-str control)
                                  "was not found, so the subtree is wrong or empty and"
                                  "`no part" part "` was confirmed against nothing")
                           (seq hits)
                           (fail! "ABSENCE-BROKEN" what "-- part" (pr-str part)
                                  "now EXISTS at" (pr-str (get (first hits) "label_description")))))
                       :label
                       (let [re   (re-pattern pattern)
                             cre  (re-pattern control)
                             hits (find-labels root re)
                             ctrl (find-labels root cre)]
                         (cond
                           (empty? ctrl)
                           (cannot! "CONTROL-FAILED" what "-- control pattern" (pr-str control)
                                  "matched no label, so the scan read the wrong tree and the"
                                  "absence is vacuous")
                           (seq hits)
                           (fail! "ABSENCE-BROKEN" what "-- pattern" (pr-str pattern)
                                  "now matches" (count hits) "label(s), e.g."
                                  (pr-str (get (first hits) "label_description"))))))))))

             ;; 4. text negatives
             (doseq [{:keys [what url pattern control]} tabs]
               (let [t (get docs url)]
                 (if (str/blank? t)
                   (cannot! "CANNOT-CHECK" what "-- empty document from" url)
                   (let [re  (re-pattern pattern)
                         cre (re-pattern control)]
                     (cond
                       (not (re-find cre t))
                       (cannot! "CONTROL-FAILED" what "-- control pattern" (pr-str control)
                              "does not occur in the fetched text, so the document is not the"
                              "one this absence is about and `no match` proves nothing")
                       (re-find re t)
                       (fail! "ABSENCE-BROKEN" what "-- pattern" (pr-str pattern)
                              "now occurs in the live text of" url))))))
             (say "  absences checked:" (+ (count negs) (count tabs))
                  (str "(" (count negs) " structural, " (count tabs) " textual)"))

             ;; 5. floors
             (when (< (count pos) min-citations)
               (die 2 "CANNOT-ANSWER: only" (count pos) "headings checked, below --min"
                    min-citations "-- a catalog this small is not evidence"))
             (when (< (count qs) min-quotes)
               (die 2 "CANNOT-ANSWER: only" (count qs) "quotes checked, below --min-quotes"
                    min-quotes))
             (when (< (+ (count negs) (count tabs)) min-absences)
               (die 2 "CANNOT-ANSWER: only" (+ (count negs) (count tabs))
                    "absences checked, below --min-absences" min-absences))

             (when (seq @cannot)
               (binding [*print-fn* *print-err-fn*]
                 (println "\nCANNOT-ANSWER --" (count @cannot) "check(s) could not be made:")
                 (doseq [c @cannot] (println "  *" c)))
               (js/process.exit 2))

             (if (seq @fails)
               (do (binding [*print-fn* *print-err-fn*]
                     (println "\nFAILED --" (count @fails) "problem(s):")
                     (doseq [f @fails] (println "  *" f)))
                   (js/process.exit 1))
               (do (say "\nVERIFIED:" (count pos) "headings," (count qs) "quotes,"
                        (+ (count negs) (count tabs)) "absences -- all live against eCFR.")
                   (js/process.exit 0)))))))))

(-main)
