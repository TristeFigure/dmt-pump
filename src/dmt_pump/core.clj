(ns dmt-pump.core
  (:require [shuriken.sequential :refer [slice get-nth]]))

(comment ;; TODO: unused, move to shuriken alongside lay & friends ?
  (defmacro earlet [bindings & body]
    `(let ~(mapv macroexpand bindings)
       ~@body)))

(defn parse-access-bit [x]
  (let [[_ n nme] (re-matches #"%?(\d+)?(.+)?" (name x))]
    (if (-> nme first (= \&))
      [\& (some->> (rest nme) seq (apply str))]
      [(when n (Integer/parseInt n))
         nme])))

(defn parse-access-sym [sym max-idx bindings idx-dict args-sym]
  (when-let [[_ sym-str] (when (simple-symbol? sym)
                           (re-matches #"%(.*)" (name sym)))]
     ;; converts % to %1
    (let [sym-str (if (or (empty? sym-str) (-> sym-str first (= \:)))
                    (str "1" sym-str)
                    sym-str)
          slices (->> (slice  #{\:}  sym-str)
                      (map (partial apply str)))
          parses (map parse-access-bit slices)]
      ;; [new-max-idx new-bindings new-idx-dict target-sym]
      (reduce
        (fn [[max-idx bindings idx-dict prev-sym] [n nme]]
          (if (= prev-sym args-sym) ;; if it's the head of the access sym
            (let [alias        (or (get idx-dict n) (get idx-dict nme))
                  idx          (or (:idx alias) n (inc max-idx))
                  new-sym      (or (:sym alias)
                                   (when n (symbol (str \% n nme)))
                                   (symbol (str \% (inc max-idx) nme)))
                  new-bind     (when-not alias
                                 [new-sym (if (= n \&)
                                            ::&
                                            `(nth ~prev-sym ~(dec idx)))])
                  new-bindings (concat bindings new-bind)
                  entry        {:idx idx :sym new-sym}
                  new-dict     (if-not alias
                                 (as-> (assoc idx-dict idx entry)
                                   $ (if nme (assoc $ nme entry) $))
                                 idx-dict)
                  new-max-idx (if (= idx \&) max-idx (max idx max-idx))]
              [new-max-idx new-bindings new-dict new-sym])
            (let [new-sym (symbol (if nme
                                    (str \% nme)
                                    (apply str \% n \- (rest (str prev-sym)))))
                  new-bind [new-sym (if n
                                      `(if (and (not (sequential? ~prev-sym))
                                                (associative? ~prev-sym))
                                         (get ~prev-sym ~n)
                                         (nth ~prev-sym ~(dec n)))
                                      `(get ~prev-sym ~(keyword nme)))]
                  new-bindings (concat bindings new-bind)
                  new-dict (if nme
                             (assoc idx-dict nme {:sym new-sym})
                             idx-dict)]
              [max-idx new-bindings new-dict new-sym])))
        [max-idx bindings idx-dict args-sym]
        parses))))

;; TODO: use a dance's context instead of a transient.
;; This will allows us to nest %| expression
(defmacro %| [& expr]
  (let [mem           (transient [0 [] {} false])
        get-max-idx  #(get mem 0)   set-max-idx!  #(assoc! mem 0 %)
        get-bindings #(get mem 1)   set-bindings! #(assoc! mem 1 %)
        get-idx-dict #(get mem 2)   set-idx-dict! #(assoc! mem 2 %)
        args-sym     (gensym "args-")
        new-expr     (clojure.walk/postwalk
                       (fn [form]
                         (let [max-idx (get-max-idx)
                               bds      (get-bindings)
                               idx-dict (get-idx-dict)]
                           (if-let [[new-idx new-bds new-idx-dict target-sym]
                                    (parse-access-sym
                                      form max-idx bds idx-dict args-sym)]
                             (do (set-max-idx! new-idx)
                                 (set-bindings! new-bds)
                                 (set-idx-dict! new-idx-dict)
                                 target-sym)
                             form)))
                       expr)
        bindings (if-not (contains? (get-idx-dict) \&)
                   (get-bindings)
                   (clojure.walk/postwalk (fn [form]
                                            (if (= form ::&)
                                              `(drop ~(get-max-idx) ~args-sym)
                                              form))
                                          (get-bindings)))]
    `(fn* [& ~args-sym]
       (let* ~(vec bindings)
         ~new-expr))))

(comment
  (def examples
    '[(%| str %1 %2)
      (%| str %  %2)
      (%| str %a %b)
      (%| str %1 %1)
      (%| str %a %a)
      (%| str %2b %3c)
      (%| str %abc %2 %1)
      (%| str %1 %2 %abc)
      (%| str %1abc %2 %abc)
      (%| str %1 %2 %1abc)
      (%| str %1:1 %2machin:2truc)
      (%| str %1:a %1machin:b)
      (%| str %1:a %a)
      (%| apply str (flatten [%1 (map str/upper-case %&x) %x]))
      (%| str %1 %2:100b)])

  (doseq [e examples]
    (println '=== e '===)
    (clojure.pprint/pprint (clojure.walk/macroexpand-all e))
    (newline)))
