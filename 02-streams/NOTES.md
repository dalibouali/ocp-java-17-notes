# OCP Java 17 - Streams (Exam Notes)

Compact, exam-focused notes for **Java Streams** in **OCP Java SE 17 (1Z0-829)**.

---

## 1) Core rules (memorize)

**Pipeline**
`source -> 0..n intermediate ops -> terminal op`

**Laziness**
- Intermediate ops are **lazy**.
- Nothing runs until a **terminal op** executes.

**Single-use**
- A stream is **single-use**.
- Reusing after a terminal op -> `IllegalStateException` at runtime.

Example (single-use trap):
```java
var s = java.util.List.of(1, 2, 3).stream();
System.out.println(s.count());
// System.out.println(s.count()); // IllegalStateException (stream already consumed)
```

---

## 2) Creating streams (5 ways)

**1) From an array**
```java
import java.util.Arrays;
import java.util.stream.Stream;

Double[] numbers = {1.1, 2.2, 3.3};
Stream<Double> stream1 = Arrays.stream(numbers);
System.out.println(stream1.count()); // 3
```

**2) From a collection**
```java
import java.util.List;
import java.util.stream.Stream;

List<String> list = List.of("a", "b", "c");
Stream<String> stream2 = list.stream();
System.out.println(stream2.count()); // 3
```

Exam trap: `Map` is not a `Collection`.
```java
import java.util.Map;

Map<String, Integer> map = Map.of("a", 1, "b", 2);
System.out.println(map.entrySet().stream().count()); // 2
```
Use one of:
- `map.entrySet().stream()`
- `map.keySet().stream()`
- `map.values().stream()`

**3) From values**
```java
import java.util.stream.Stream;

Stream<String> stream3 = Stream.of("a", "b", "c");
System.out.println(stream3.count()); // 3
```

**4) From a file**
```java
import java.nio.file.Files;
import java.nio.file.Path;

Path p = Path.of("data.txt");
try (var stream4 = Files.lines(p)) { // must close
    System.out.println(stream4.count());
}
```

**5) Infinite streams**
```java
import java.util.stream.Stream;

Stream.iterate(1, n -> n + 1)
      .limit(5)
      .forEach(System.out::println); // 1 2 3 4 5

Stream.generate(() -> "x")
      .limit(3)
      .forEach(System.out::println); // x x x
```
Java 9+:
`Stream.iterate(seed, predicate, f)`

---

## 3) Intermediate operations

### A) `filter` / `map` / `flatMap`
**map**: 1 element -> 1 element
```java
import java.util.List;

List.of("a","bb").stream()
    .map(String::length) // Stream<Integer>
    .forEach(System.out::println);
```

**flatMap**: 1 element -> many elements (flattens)
```java
import java.util.List;

var list = List.of(List.of("a","b"), List.of("c"));
list.stream()
    .flatMap(x -> x.stream())
    .forEach(System.out::println); // a b c
```

Exam trap:
- `map(List::stream)` -> `Stream<Stream<String>>`
- `flatMap(List::stream)` -> `Stream<String>`

### B) `distinct` / `sorted` / `peek`
**distinct** uses `equals()` and `hashCode()`.
```java
import java.util.stream.Stream;

Stream.of("eagle", "eagle", "EAGLE")
    .peek(s -> System.out.println("1." + s))
    .distinct()
    .forEach(s -> System.out.println("2." + s));
```

**sorted** requires `Comparable` or a `Comparator`.
```java
import java.util.Comparator;
import java.util.stream.Stream;

record Person(String name, int age) {}

Stream.of(new Person("A", 30), new Person("B", 20))
    .sorted(Comparator.comparingInt(Person::age))
    .forEach(System.out::println);
```
Trap:
```java
// Stream.of(new Object()).sorted().count(); // ClassCastException at runtime
```

**peek** runs only if a terminal op exists.
```java
import java.util.List;

List.of("a", "bb").stream()
    .peek(System.out::println)
    .count(); // prints: a  bb
```

### C) `limit` / `skip` / `takeWhile` / `dropWhile`
```java
import java.util.stream.Stream;

Stream.of(1,2,3,4,5)
    .limit(3)
    .forEach(System.out::print); // 123

Stream.of(1,2,3,4,5)
    .skip(2)
    .forEach(System.out::print); // 345
```

Order-sensitive ops:
```java
import java.util.List;

var nums = List.of(1,2,3,0,4,5);
nums.stream()
    .takeWhile(n -> n > 0)
    .forEach(System.out::print); // 123

nums.stream()
    .dropWhile(n -> n > 0)
    .forEach(System.out::print); // 045
```
Exam trap: `takeWhile` does **not** filter all positives; it stops at first false.

---

## 4) Terminal operations (must know)

### A) `count` / `min` / `max`
```java
import java.util.List;

var list = List.of(3, 1, 9);
System.out.println(list.stream().count()); // 3
System.out.println(list.stream().min(Integer::compareTo)); // Optional[1]
System.out.println(list.stream().max(Integer::compareTo)); // Optional[9]
```

### B) `findFirst` / `findAny`
- Both return `Optional<T>`.
- On parallel streams:
  - `findAny` may return any element (often faster)
  - `findFirst` respects encounter order
```java
import java.util.List;

var list = List.of("a","b","c");
System.out.println(list.stream().findFirst()); // Optional[a]
System.out.println(list.stream().findAny());   // Optional[a] (sequential usually first)
```

### C) `anyMatch` / `allMatch` / `noneMatch` (short-circuit)
```java
import java.util.stream.Stream;

boolean r = Stream.of("a", "bb", "ccc")
    .peek(s -> System.out.println("peek: " + s))
    .anyMatch(s -> s.length() == 2);

System.out.println(r);
// peek prints "a", then "bb" and stops
```

### D) `forEach` / `forEachOrdered`
```java
import java.util.stream.IntStream;

IntStream.rangeClosed(1, 5)
    .parallel()
    .forEach(System.out::print); // order not guaranteed

System.out.println();

IntStream.rangeClosed(1, 5)
    .parallel()
    .forEachOrdered(System.out::print); // 12345
```

### E) `toArray`
```java
import java.util.Arrays;
import java.util.stream.Stream;

Object[] arr = Stream.of("a","b").toArray();
System.out.println(Arrays.toString(arr)); // [a, b]

String[] arr2 = Stream.of("a","b").toArray(String[]::new);
System.out.println(Arrays.toString(arr2)); // [a, b]
```

---

## 5) Optional + streams (exam favorite)

Streams produce `Optional` from:
- `min`, `max`, `findFirst`, `findAny`
- `reduce(BinaryOperator)` (no identity)
- primitive `average()` -> `OptionalDouble`, etc.

Key trap: `orElse(...)` is **eager**, `orElseGet(...)` is **lazy**.
```java
import java.util.Optional;

public class OptionalTrap {
    static String expensive() {
        System.out.println("expensive called");
        return "fallback";
    }

    public static void main(String[] args) {
        Optional<String> o = Optional.of("OK");

        System.out.println(o.orElse(expensive()));         // expensive called
        System.out.println(o.orElseGet(OptionalTrap::expensive)); // not called
    }
}
```

---

## 6) `reduce` (3 forms) + identity rules

**1) `reduce(identity, accumulator)`**
Always returns `T`.
```java
import java.util.List;

int sum = List.of(1,2,3).stream().reduce(0, Integer::sum);
System.out.println(sum); // 6
```
Trap: wrong identity changes results.
```java
int wrong = List.of(1,2,3).stream().reduce(10, Integer::sum);
System.out.println(wrong); // 16
```

**2) `reduce(accumulator)`**
Returns `Optional<T>`.
```java
import java.util.List;

var r = List.<Integer>of().stream().reduce(Integer::sum);
System.out.println(r); // Optional.empty
```

**3) `reduce(identity, accumulator, combiner)`**
Used in parallel. Accumulator and combiner must be **compatible** and **associative**.
```java
import java.util.List;

int v = List.of(1,2,3,4).parallelStream()
    .reduce(0, (a,b) -> a - b, (x,y) -> x - y);

System.out.println(v); // unpredictable / not what you expect
```

---

## 7) `collect` + `Collectors` (core scoring area)

### A) Low-level form
```java
import java.util.ArrayList;
import java.util.List;

var out = List.of("a","bb","ccc").stream()
    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

System.out.println(out);
```

### B) Collectors you must know
- `toList`, `toSet`, `toCollection`
- `joining`
- `counting`
- `averagingInt`, `summarizingInt`
- `mapping`, `filtering`, `flatMapping`
- `groupingBy`, `partitioningBy`
- `toMap`
- `collectingAndThen`
- sometimes `teeing`

Example: `joining`
```java
import java.util.List;
import java.util.stream.Collectors;

String joined = List.of("a","b","c").stream()
    .collect(Collectors.joining(","));
System.out.println(joined); // a,b,c
```

Example: `toCollection`
```java
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

var set = List.of("b","a","b").stream()
    .collect(Collectors.toCollection(TreeSet::new));
System.out.println(set); // [a, b]
```

**`toMap` duplicates (very common exam failure)**
Duplicate keys -> `IllegalStateException` unless merge function provided.
```java
import java.util.List;
import java.util.stream.Collectors;

var list = List.of("ab", "ac");

var map = list.stream().collect(Collectors.toMap(
    s -> s.substring(0, 1),
    s -> s,
    (oldV, newV) -> oldV + "," + newV // merge function
));

System.out.println(map); // {a=ab,ac}
```

4-arg `toMap` (with map supplier):
```java
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

var map2 = List.of("ab", "ac").stream().collect(Collectors.toMap(
    s -> s.substring(0, 1),
    s -> s,
    (oldV, newV) -> newV,
    TreeMap::new
));
System.out.println(map2);
```

### C) `groupingBy` / `partitioningBy`
**groupingBy** -> `Map<K, List<T>>`
```java
import java.util.List;
import java.util.stream.Collectors;

var map = List.of("a","bb","ccc","dd").stream()
    .collect(Collectors.groupingBy(String::length));
System.out.println(map); // {1=[a], 2=[bb, dd], 3=[ccc]}
```

Downstream example: `counting`
```java
import java.util.List;
import java.util.stream.Collectors;

var counts = List.of("a","bb","ccc","dd").stream()
    .collect(Collectors.groupingBy(String::length, Collectors.counting()));
System.out.println(counts); // {1=1, 2=2, 3=1}
```

**partitioningBy** -> `Map<Boolean, List<T>>`
```java
import java.util.List;
import java.util.stream.Collectors;

var parts = List.of("a","bb","ccc","dd").stream()
    .collect(Collectors.partitioningBy(s -> s.length() >= 2));
System.out.println(parts); // {false=[a], true=[bb, ccc, dd]}
```

---

## 8) Parallel streams (danger zone)

Rules:
- Do not rely on order unless `forEachOrdered`.
- Avoid shared mutable state.
- `reduce` must be associative.
- Prefer collectors for parallel collection.

Side-effect trap:
```java
import java.util.ArrayList;
import java.util.stream.IntStream;

var list = new ArrayList<Integer>();
IntStream.range(0, 1000).parallel().forEach(list::add);
System.out.println(list.size()); // can be < 1000
```

Fix:
```java
import java.util.stream.Collectors;
import java.util.stream.IntStream;

var safe = IntStream.range(0, 1000)
    .boxed()
    .collect(Collectors.toList());

System.out.println(safe.size()); // 1000
```

---

## 9) Quick exercises (exam drill)

- Show that `peek()` does not run without a terminal op.
- Show stream reuse throws at runtime.
- `map` vs `flatMap`: `List<List<Integer>>` -> sum all ints using `flatMap` + `mapToInt` + `sum`.
- `takeWhile`/`dropWhile` vs `filter`: compare results on `[1,2,0,3,4]`.
- `toMap` duplicates: key = length, merge by keeping the **longest** string.
- `groupingBy` with downstream counting.
- `partitioningBy` length >= 3.
- Parallel side-effect bug with `ArrayList`; fix with `collect(toList())`.
