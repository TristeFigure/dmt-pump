(ns dmt-pump.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [dmt-pump.core :refer :all :reload true]))

(defmacro check [title & cases]
  `(testing ~title
    (are [f# args# res#] (= res# (apply f# args#))
         ~@cases)))

(deftest test-%|
  (check "superset of #(...)"
         (%| str %1 %2) '[a b]  "ab"
         (%| str %  %2) '[a b]  "ab"
         (%| str %2)    '[a b]  "b"
         (%| str)       '[]     ""
         (%| str %1 %1) '[a b]  "aa"
         (%| apply str           %1 (map str/upper-case %&))      '[a b c] "aBC"
         (%| apply str (flatten [%1 (map str/upper-case %&) %2])) '[a b c] "aCb")
  (check "named num-args"
         (%| str %2b %3c) '[a b c] "bc"
         (%| apply str (flatten [%1 (map str/upper-case %&x) %x])) '[a b] "aBb")
  (check "named args"
         (%| str %a %b)        '[a b]     "ab"
         (%| str %a %a)        '[a b]     "aa"
         (%| str %2b %c %1 %d) '[a b c d] "bcad")
  (check "a %NUM can refer to a previous %NAME"
         (%| str %abc %2 %1)                      '[a b]     "aba"
         (%| apply str (flatten [%&x %&]))        '[a]       "aa")
  (check "a %NAME never refer to a previous %NUM"
         (%| str %1 %2 %abc)                      '[a b c]   "abc"
         (%| apply str (flatten [%1 %2 %& %abc])) '[a b c d] "abdc")
  (check "a %NAME can refer to a previous %NUMNAME"
         (%| str %1abc %2 %abc)                   '[a b]     "aba"
         (%| apply str (flatten [%1 %&x %x]))     '[a b]     "abb")
  (check "a %NUMNAME can refer to a previous %NUM"
         (%| str %1 %2 %1abc)                     '[a b]     "aba")
  (check "deep access"
         (%| str %:1)                 '[[a]]         "a"
         (%| str %1:1 %2machin:2truc) '[[a] [b c]]   "ac"
         (%| str %1:a %1machin:b)     '[{:a a :b b}] "ab"
         (%| str %1:a %a)             '[{:a a}]      "aa"
         (%| str %1 %&:1)             '[a b]         "ab"
         (%| str %1 %&x:1truc:b)      '[a {:b b}]    "ab")
  (check "accessing maps having nums as keys"
         (%| str %1 %2:100b)          '[a {100 b}]   "ab")
  (check "Nesting with #(... %)"
         (%| str (#(str %1 'b) %1))  '[a]           "ab"
         ; #(str ((%| str %1 'b) %1))  '[a]           "ab"
         )
  ; (check "Nesting"
  ;        (%| str ((%| do %1) %1))     '[[a]]         "a")
  )
