(ns numeric.expresso.test-core
  (:use numeric.expresso.core)
  (:use clojure.test)
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic.protocols]
        [clojure.core.logic :exclude [is] :as l]
        clojure.test)
  (:require [clojure.core.logic.fd :as fd])
  (:require [clojure.core.logic.unifier :as u]))


(deftest test-unify
  (let [ex1 (ex (+ 1 X))]
    ;;(is (= [(ex X)] (run* [q] (fresh [op p] (== [op p q] ex1)))))
    (is (= [(ex X)] (run* [q] (fresh [op p] (== ex1 [op p q])))))
    (is (= [(ex (+ 1 X))] (run* [q] (== ex1 q))))
    
    ))

(deftest test-constant
  (is (= 10 (ex 10)))
  (is (constant? (ex 1)))
  (is (not (constant? (ex (+ 1 X))))))

(deftest test-without-symbol
  (is (without-symbol? 'X (ex Y)))
  (is (without-symbol? 'X (ex (+ 1 Y))))
  (is (not (without-symbol? 'X (ex X))))
  (is (not (without-symbol? 'X (ex (+ 1 X))))))

(deftest test-lifto
  (is (= [3] (run* [q] ((lifto inc) 2 q)))))

(deftest test-lifto-with-inverse
  (let [inco (lifto-with-inverse inc dec)]
    (is (= [3] (run* [q] (inco 2 q))))
    (is (= [2] (run* [q] (inco q 3))))))

(deftest test-resolve-opo
  (is (= [clojure.core/+] (run* [q] (resolve-opo '+ q)))))

(deftest test-expo 
  (is (= [1] (run* [q] (expo '+ [q 2]  (ex (+ 1 2))))))
  (is (= [] (run* [q] (expo '- [q 2]  (ex (+ 1 2))))))
  (is (= [[1 2]] (run* [q] (fresh [ex op lhs rhs]
                                  (expo '+ [1 2] ex)
                                  (expo op [lhs rhs] ex)
                                  (== q [lhs rhs]))))))


(deftest test-mapo 
  (is (= [[2 3 4]] (run* [q] (mapo (lifto inc) [1 2 3] q))))
  (is (= [2] (run* [q] (mapo (lifto-with-inverse inc dec) [1 q 3] [2 3 4])))))

(deftest test-applyo 
  (is (= [[1 2 3 4]] (run* [q] (applyo conso [1 [2 3 4]] q))))
  (is (= [3] (run* [q] (applyo conso [1 [2 q 4]] [1 2 3 4])))))

(deftest test-resulto
  (is (= [2] (run* [q] (resulto (ex 2) q))))
  (is (= [6] (run* [q] (resulto (ex (+ 2 4)) q)))))

(deftest test-simplifico
  (is (= [3] (run* [q] (simplifico (ex (+ 1 2)) q))))
  (is (= [10] (run* [q] (simplifico (ex (+ 1 2 3 4)) q))))
  ;; (is (= [3] (run* [q] (simplifico (ex (+ 1 2 q 4)) 10)))) reverse simplification not impl.
  )

(deftest test-rearrangeo
  (is (= [['= 'X 3]] (run* [q] (rearrangeo (ex (= X (+ 1 2))) q))))
  )

(deftest test-expresso
  (is (= [3] (run* [q] (expresso 'X (ex (= X 3)) q))))
  (is (= [3] (run* [q] (expresso 'X (ex (= X (+ 1 2))) q)))))


(deftest test-apply-ruleo
  (is (= ['x] (run* [q] (apply-ruleo (rule ['+ 0 x] :=> x) '(+ 0 x) q))))
  (is (= [0] (run* [q] (apply-ruleo (rule ['* x 0] :=> 0) '(* x 0) q))))
  (is (= ['(+ 0 (+ 0 x))] (run* [q] (apply-ruleo (rule ['+ 0 x] :=> x) q '(+ 0 x)))))
  (is (= [7] (run* [q] (apply-ruleo calculo '(+ 1 (* 2 3)) q)))))

(deftest test-simplifyo
  (is (= [0] (run* [q] (simplifyo '(* x (+ 0 (* 3 (* x 0)))) q)))))

(deftest test-solveo
  (is (= ['(= x 7/3)] (run* [q] (solveo '(= (- (* x 3) 3) 4) 'x q))))
  (is (= ['(= x 4) ] (run* [q] (solveo '(= (+ (* 3 (+ (* x 4) (* x 3))) 5) 89) 'x  q)))))
