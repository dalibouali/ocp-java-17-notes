# OCP Java 17 - Lambdas and Functional Interfaces (Exam Notes)

Compact, exam-focused notes for **Lambda Expressions** and **Functional Interfaces** in **OCP Java SE 17 (1Z0-829)**.

---

## 1) Functional Interfaces (FI): must know

**Definition (SAM)**
A functional interface has **exactly one abstract method** (SAM = Single Abstract Method).

It may also have:
- any number of `default` methods
- any number of `static` methods
- methods that match `Object` methods like `toString()`, `equals()`, `hashCode()` (do **not** count as extra abstract methods)

**@FunctionalInterface**
- Optional but useful.
- If annotated and the interface is not functional -> **compiler error**.
- Signals: "this must stay SAM".

Valid FI example:
```java
@FunctionalInterface
interface MyFn {
    int apply(int x);

    default int twice(int x) { return x * 2; }  // does not count
    static int add1(int x) { return x + 1; }    // does not count

    // Allowed: matches Object method signature
    String toString();
}
```

Not functional if a 2nd abstract method is added:
```java
@FunctionalInterface
interface BadFn {
    int a();
    int b(); // second abstract method => NOT functional
}
```

---

## 2) Built-in functional interfaces (memorize)

**Core four**
- `Predicate<T>` -> `boolean test(T t)`
- `Function<T,R>` -> `R apply(T t)`
- `Consumer<T>` -> `void accept(T t)`
- `Supplier<T>` -> `T get()`

**Primitive specializations (avoid boxing)**
- Predicates: `IntPredicate`, `LongPredicate`, `DoublePredicate`
- Functions: `IntFunction<R>`, `ToIntFunction<T>`, `IntToLongFunction`, `LongToDoubleFunction`, etc.
- Consumers: `IntConsumer`, `LongConsumer`, `DoubleConsumer`
- Suppliers: `IntSupplier`, `LongSupplier`, `DoubleSupplier`

**Operators**
- `UnaryOperator<T>` = `Function<T,T>`
- `BinaryOperator<T>` = `BiFunction<T,T,T>`

**Bi- versions (two inputs)**
- `BiPredicate<T,U>`
- `BiFunction<T,U,R>`
- `BiConsumer<T,U>`

**Fast exam mapping**
- `Predicate` returns **boolean**
- `Consumer` returns **void**
- `Supplier` takes **no args**
- `Function` returns a **value**

---

## 3) Lambda syntax (compilation rules)

**Parameters (valid forms)**
```java
s -> s.length()
(s) -> s.length()
(String s) -> s.length()
(var s) -> s.length()
```

Exam traps:
```java
String s -> s.length();              // invalid: type requires parentheses
(var a, String b) -> a.length() + b.length(); // invalid: cannot mix var with explicit types
(a, b) -> a + b;                      // valid
// a, b -> a + b;                     // invalid: missing parentheses for multiple params
```

**Body**
Expression body:
```java
x -> x + 1
```

Block body:
```java
x -> { return x + 1; }
```

Trap: a block that returns a value must use `return`.
```java
x -> { x + 1; } // invalid
```

**Void-compatible vs value-compatible**
Some lambdas can fit `Consumer` or `Function` depending on context.
```java
java.util.function.Consumer<String> c = s -> System.out.println(s);
java.util.function.Function<String, Void> f =
    s -> { System.out.println(s); return null; };
```

---

## 4) Type inference and target typing

A lambda has meaning only through its **target functional interface**.
```java
java.util.function.Predicate<String> p = s -> s.isEmpty(); // s inferred as String
```

Trap: lambda has no standalone type -> `var` cannot infer.
```java
// var x = s -> s.isEmpty(); // invalid
java.util.function.Predicate<String> x = s -> s.isEmpty(); // valid
```

---

## 5) Overload resolution with lambdas (ambiguity)

When multiple overloads could match, Java may fail to choose.
```java
import java.util.function.*;

class Overload {
    static void m(Predicate<String> p) {}
    static void m(Function<String, Boolean> f) {}

    public static void main(String[] args) {
        // m(s -> s.isEmpty()); // can be ambiguous

        m((Predicate<String>) s -> s.isEmpty()); // force Predicate overload

        Predicate<String> p = String::isEmpty;
        m(p); // no ambiguity
    }
}
```

Exam mindset: overload + lambda -> first ask "ambiguous?"

---

## 6) Method references (4 kinds)

1) Static method
```java
java.util.function.Function<String, Integer> f = Integer::parseInt;
```

2) Instance method on a particular object
```java
var sb = new StringBuilder("abc");
Runnable r = sb::reverse; // return value ignored (void-compatible)
```

3) Instance method on a parameter
```java
java.util.function.Predicate<String> p = String::isEmpty;          // s -> s.isEmpty()
java.util.function.Function<String, Integer> len = String::length; // s -> s.length()
```

4) Constructor reference
```java
java.util.function.Supplier<StringBuilder> sup = StringBuilder::new;
java.util.function.Function<String, StringBuilder> f2 = StringBuilder::new; // new StringBuilder(String)
```

---

## 7) Effectively final and variable capture

A lambda can capture local variables only if they are **final** or **effectively final**.
```java
int x = 10;
Runnable r = () -> System.out.println(x); // valid
// x = 11; // invalid: breaks effectively final
```

You can mutate the object, but you cannot reassign the reference.
```java
var list = new java.util.ArrayList<String>();
Runnable r2 = () -> list.add("x"); // valid
list.add("y");                     // valid
// list = new java.util.ArrayList<>(); // invalid: breaks effectively final
```

---

## 8) Checked exceptions and lambdas

A lambda can throw checked exceptions only if the FI method declares them.
```java
import java.util.concurrent.Callable;

Callable<String> c = () -> {
    if (Math.random() > 0.5) throw new Exception(); // allowed
    return "ok";
};

// Runnable r = () -> { throw new Exception(); }; // invalid: Runnable.run() doesn't declare checked exceptions
```

---

## 9) Generics and wildcards with lambdas (simple rules)

**Rule A: `List<?>` is read-only (except null)**
```java
java.util.List<?> xs = java.util.List.of("a", "b");
// xs.add("c"); // invalid: cannot add (unknown element type)
```

**Rule B: PECS (Producer Extends, Consumer Super)**
- `? extends T` -> you can read `T`, but cannot safely add `T`
- `? super T` -> you can add `T`, but reading gives `Object`

Example: `? super` accepts additions.
```java
java.util.List<? super Number> sink = new java.util.ArrayList<Object>();
sink.add(1);
sink.add(2.5); // adding Number subtypes is allowed

sink.forEach(x -> System.out.println(x)); // prints Objects
```

**Wildcard + lambda trap**
When you see wildcards + lambda:
- Identify `T` in the method signature.
- Check if it expects `Consumer<T>` or `Consumer<? super T>`.
- If types do not match, it is a compile error even if it feels correct.

---

## 10) Default method conflicts (interface rules)

If two interfaces provide the same default method, the implementing class must override it.
```java
interface A { default void hi() { System.out.println("A"); } }
interface B { default void hi() { System.out.println("B"); } }

class C implements A, B {
    @Override
    public void hi() {
        A.super.hi(); // choose one (or implement your own)
    }
}
```

Exam point: "two interface defaults conflict" -> class must resolve.
