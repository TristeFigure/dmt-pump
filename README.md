# dmt-pump

#### The destructuring power of `fn` with the brevity of `#(... %)`

![Mickey's inner palace](https://i.imgur.com/ecUyfVr.jpg)

### Abstract

> The state of consciousness induced by N,N-dimethyltryptamine (DMT) is one of the most extraordinary of any naturally-occurring psychedelic substance. Users consistently report the complete replacement of normal subjective experience with a novel “alternate universe,” often densely populated with a variety of strange objects and other highly complex visual content, including what appear to be sentient “beings.” The phenomenology of the DMT state is of great interest to psychology and calls for rigorous academic enquiry. The extremely short duration of DMT effects—less than 20 min—militates against single dose administration as the ideal model for such enquiry. Using pharmacokinetic modeling and DMT blood sampling data, we demonstrate that the unique pharmacological characteristics of DMT, which also include a rapid onset and lack of acute tolerance to its subjective effects, make it amenable to administration by target-controlled intravenous infusion. This is a technology developed to maintain a stable brain concentration of anesthetic drugs during surgery. Simulations of our model demonstrate that this approach will allow research subjects to be induced into a stable and prolonged DMT experience, making it possible to carefully observe its psychological contents, and provide more extensive accounts for subsequent analyses. This model would also be valuable in performing functional neuroimaging, where subjects are required to remain under the influence of the drug for extended periods. Finally, target-controlled intravenous infusion of DMT may aid the development of unique psychotherapeutic applications of this psychedelic agent.

[A Model for the Application of Target-Controlled Intravenous Infusion for a Prolonged Immersive DMT Psychedelic Experience](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4944667/)

## Usage

```clojure
[dmt-pump "0.1.0"]
```

```clojure
(ns my-ns
  (:require [dmt-pump.core :refer [%|]]))
```

## Visions

```clojure
;; Works like #(... %)
(= ((%| str %1 %2)        'a 'b)       "ab")
(= ((%| str %  %2)        'a 'b)       "ab")
(= ((%| apply str %&)     'a 'b)       "ab")

;; Can name args
(= ((%| str %1a %2b)      'a 'b)       "ab")
(= ((%| apply str %&ab)   'a 'b)       "ab")

;; And refer to these named args later
(= ((%| str %1a %a %1)    'a 'b)       "aaa")

;; Accepts args with no num, only a name. Indices are deduced incrementally.
(= ((%| str %a %b)        'a 'b)       "ab")
(= ((%| str %2b %c %1 %d) 'a 'b 'c 'd) "bcad")
(= ((%| apply str %&abcd) 'a 'b 'c 'd) "abcd")


;; Deep access.
;; Works like get-in. Each access symbol must follow this grammar:
;;   access-sym  => %access-sym+
;;   access-sym+ => key:access-sym+ || key
;;   key         => num || numname || name

;; To stay consistent with the way `#(... %)` indexes arguments, and unlike get-in,
;; indices start at 1, however deep.
(= ((%| str %:1)  '[a])   "a")

;; Works with maps
(= ((%| str %:a)  '{:a a}) "a")
(= ((%| str %:1)  '{1 a})  "a")

;; And lists
(= ((%| str %:1)  '(a))    "a")

;; Nice !
(= ((%| str %:1 %other-arg:2opts:value)  '[a] '[b {:value c}])
   "ac")

;                 _____
;              ,-"     "-.
;             / o       o \
;            /   \     /   \           DO NOT FORGET
;           /     )-"-(     \   THE INDICES ALWAYS START AT 1
;          /     ( 6 6 )     \               !
;         /       \ Œ /       \
;        /         )=(         \
;       /   o   .--"-"--.   o   \
;      /    I  /  -   -  \  I    \
;  .--(    (_}y/\       /\y{_)    )--.
; (    ".___l\/__\_____/__\/l___,"    )
;  \                                 /
;   "-._      o O o O o O o      _,-"
;       `--Y--.___________.--Y--'
;          |==.___________.==| hjw
;          `==.___________.==' `97
```

AhA ! I promise ! AH ahaha ! I will not forget ! AhaA!

## TODO

- Support nesting of `(%| ... %)` with itself and `#(... %)`.
- Turn `%|` into an extended version of the `#(` reader macro.

## License

Copyright © 2019 TristeFigure

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
