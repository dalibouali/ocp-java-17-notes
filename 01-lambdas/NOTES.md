# OCP Java 17 — Lambdas & Functional Interfaces (Exam Notes)

This file is a compact, exam-focused notebook for **Lambda Expressions + Functional Interfaces** in **OCP Java SE 17 (1Z0-829)**.

---

## 1) Functional Interfaces (FI): what you must know

### Definition (SAM)
A **functional interface** has **exactly one abstract method** (SAM = Single Abstract Method).

It may also have:
- any number of **default** methods
- any number of **static** methods
- methods that match `Object` methods like `toString()`, `equals()`, `hashCode()`
  (these **do not count** as extra abstract methods)

### `@FunctionalInterface`
Optional but useful:
- If annotated and the interface is not functional → **compiler error**
- Good signal: “this must stay SAM”

✅ Valid FI example:
```java
@FunctionalInterface
interface MyFn {
    int apply(int x);

    default int twice(int x) { return x * 2; }  // does not count (default)
    static int add1(int x) { return x + 1; }    // does not count (static)

    // Allowed: matches Object method signature
    String toString();
}
❌ Not functional if a 2nd abstract method is added:

@FunctionalInterface
interface BadFn {
    int a();
    int b(); // ❌ second abstract method => NOT functional
}
2) Built-in functional interfaces you MUST memorize
Core Four
Predicate<T> → boolean test(T t) (returns boolean)

Function<T,R> → R apply(T t) (returns a value, takes 1 param)

Consumer<T> → void accept(T t) (returns void)

Supplier<T> → T get() (no params)

Primitive specializations (avoid boxing)
Predicates: IntPredicate , LongPredicate , DoublePredicate

Functions: IntFunction<R>, ToIntFunction<T>, IntToLongFunction, etc.

Consumers: IntConsumer, LongConsumer, DoubleConsumer

Suppliers: IntSupplier, LongSupplier, DoubleSupplier

Operators:

UnaryOperator<T> = Function<T,T>

BinaryOperator<T> = BiFunction<T,T,T>

Bi- versions (two inputs)
BiPredicate<T,U>

BiFunction<T,U,R>

BiConsumer<T,U>

Fast exam mapping

Predicate → returns boolean

Consumer → returns void

Supplier → takes no args

Function → returns a value

3) Lambda syntax (compilation rules)
Parameters (valid forms)
s -> s.length()
(s) -> s.length()
(String s) -> s.length()
(var s) -> s.length()
Exam traps

If you specify a type, parentheses are required:

String s -> s.length(); // ❌
You cant mix var with explicit types:

(var a, String b) -> a.length() + b.length(); // ❌
With multiple parameters, parentheses are required:

(a, b) -> a + b  // ✅
a, b -> a + b    // ❌
Body
Expression body:

x -> x + 1
Block body:

x -> { return x + 1; }
Trap: block that returns a value must use return

x -> { x + 1; } // ❌
Void-compatible vs value-compatible (common trick)
Some lambdas can fit Consumer or Function depending on context:

java.util.function.Consumer<String> c = s -> System.out.println(s); // ✅
java.util.function.Function<String, Void> f =
    s -> { System.out.println(s); return null; }; // ✅
4) Type inference & “target typing”
A lambda has meaning only through its target functional interface.

java.util.function.Predicate<String> p = s -> s.isEmpty(); // s inferred as String
Trap: lambda has no standalone type → var cannot infer

var x = s -> s.isEmpty(); // ❌
Correct:

java.util.function.Predicate<String> x = s -> s.isEmpty(); // ✅
5) Overload resolution with lambdas (AMBIGUITY)
When multiple overloads could match, Java may fail to choose.

import java.util.function.*;

class Overload {
    static void m(Predicate<String> p) {}
    static void m(Function<String, Boolean> f) {}

    public static void main(String[] args) {
        // m(s -> s.isEmpty()); // ❌ can be ambiguous

        m((Predicate<String>) s -> s.isEmpty()); // ✅ force Predicate overload

        Predicate<String> p = String::isEmpty;
        m(p); // ✅ no ambiguity
    }
}
Exam mindset: overload + lambda → first ask “ambiguous?”

6) Method references (4 kinds)
    1) Static method
    java.util.function.Function<String, Integer> f = Integer::parseInt;
    2) Instance method on a particular object
    var sb = new StringBuilder("abc");
    Runnable r = sb::reverse; // ✅ return value ignored (void-compatible)
    3) Instance method on a parameter (very common in exam)
    java.util.function.Predicate<String> p = String::isEmpty;         // s -> s.isEmpty()
    java.util.function.Function<String, Integer> len = String::length; // s -> s.length()
    4) Constructor reference
    java.util.function.Supplier<StringBuilder> sup = StringBuilder::new;
    java.util.function.Function<String, StringBuilder> f2 = StringBuilder::new; // new StringBuilder(String)
7) Effectively final & variable capture
A lambda can capture local variables only if they are final or effectively final.

int x = 10;
Runnable r = () -> System.out.println(x); // ✅
x = 11; // ❌ breaks effectively final
Important nuance:

You can mutate the object, but you cannot reassign the reference.

var list = new java.util.ArrayList<String>();
Runnable r2 = () -> list.add("x"); // ✅
list.add("y");                     // ✅
// list = new java.util.ArrayList<>(); // ❌ would break
8) Checked exceptions and lambdas
A lambda can throw checked exceptions only if the FI method declares them.

import java.util.concurrent.Callable;

Callable<String> c = () -> {
    if (Math.random() > 0.5) throw new Exception(); // ✅ allowed
    return "ok";
};
Trap:

Runnable r = () -> { throw new Exception(); }; // ❌ Runnable.run() doesn't declare checked exceptions
9) Generics + wildcards with lambdas (simple rules)
Rule A: List<?> is read-only (except null)
java.util.List<?> xs = java.util.List.of("a", "b");
// xs.add("c"); // ❌ cannot add (unknown element type)
Rule B: PECS (Producer Extends, Consumer Super)
? extends T → you can read T, but cannot safely add T

? super T → you can add T, but reading gives Object

Example: ? super accepts additions

java.util.List<? super Number> sink = new java.util.ArrayList<Object>();
sink.add(1);
sink.add(2.5); // ✅ adding Number subtypes is allowed

sink.forEach(x -> System.out.println(x)); // ✅ prints Objects
Wildcard + lambda trap (what to do in the exam)
When you see wildcards + lambda:

Identify T in the method signature.

Check if it expects Consumer<T> or Consumer<? super T>.

If types don’t match, it’s a compile error even if it “feels” correct.

10) Default methods conflicts (interface rules mixed with lambdas)
If two interfaces provide the same default method, the implementing class must override it.

interface A { default void hi() { System.out.println("A"); } }
interface B { default void hi() { System.out.println("B"); } }

class C implements A, B {
    @Override
    public void hi() {
        A.super.hi(); // ✅ choose one (or implement your own)
    }
}
Exam point: “two interface defaults conflict” → class must resolve.