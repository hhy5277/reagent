(ns reagenttest.testinterop
  (:require [cljs.test :as t :refer-macros [is deftest]]
            [reagent.debug :refer-macros [dbg]]
            [reagent.interop :refer-macros [.' .! obj]]))

(def is-adv (let [o #js{}]
              (set! (.-somethinglong o) true)
              (not= (aget (.keys js/Object o) 0) "somethinglong")))

(deftest iterop-quote
  (let [o (obj :foo "foo"
               :foobar (obj :bar "bar"
                            :bar-foo "bar-foo")
               :bar-foo "barfoo")]

    (is (= "foo" (.' o :foo)))
    (is (= "bar" (.' o :foobar.bar)))
    (is (= "barfoo" (.' o :bar-foo)))
    (when-not is-adv
      (is (= "barfoo" (.-bar-foo o))))

    (is (= "foo" (.' o -foo)))
    (is (= "bar" (.' o -foobar.bar)))
    (is (= "bar-foo" (.' o -foobar.bar-foo)))
    (is (= "bar-foo" (.' o :foobar.bar-foo)))
    (is (= "barfoo" (.' o -bar-foo)))

    (.! o :foo "foo1")
    (is (= "foo1" (.' o :foo)))

    (.! o -foo "foo2")
    (is (= "foo2" (.' o -foo)))

    (.! o :foobar.bar "bar1")
    (is (= "bar1" (.' o :foobar.bar)))

    (.! o -foobar.bar "bar1")
    (is (= "bar1" (.' o -foobar.bar)))))

(deftest interop-quote-call
  (let [o #js{:bar "bar1"
              :foo (fn [x]
                     (this-as this
                              (str x (.' this :bar))))}
        o2 #js{:o o}]
    (is (= "ybar1" (.' o foo "y")))
    (is (= "xxbar1" (.' o2 o.foo "xx")))
    (is (= "abar1" (-> o2
                       (.' :o)
                       (.' foo "a"))))

    (is (= "bar1" (.' o foo)))
    (is (= "bar1" (.' o2 o.foo)))

    (.! o :bar "bar2")
    (is (= "bar2" (.' o foo)))

    (is (= "1bar2" (.' (.' o :foo)
                       call o 1)))))

(deftest interop-munge
  (let [o (obj :foo-bar "foo-bar"
               :foo? "foo?"
               :foo$ "foo$"
               :foo! "foo!"
               :foo><*+% "foo><*+%")]
    (is (= (.' o :foo-bar) "foo-bar"))
    (is (= (.' o :foo?) "foo?"))
    (is (= (.' o :foo$) "foo$"))
    (is (= (.' o -foo!) "foo!"))
    (is (= (.' o :foo><*+%) "foo><*+%"))

    (when-not is-adv
      (is (= (.-foo-bar o) "foo-bar"))
      (is (= (.-foo? o) "foo?"))
      (is (= (.-foo$ o) "foo$"))
      (is (= (.-foo><*+% o) "foo><*+%"))
      (let [x (js-obj)]
        (.! x -foo-bar "foo-bar")
        (is (= (.-foo-bar x) "foo-bar"))
        (set! (.-foo? x) "foo?")
        (is (= (.' x :foo?) "foo?"))))))
