import java.util.Random;
import java.util.function.*;

public class FI_CoreBuiltIns {

    //  Predicate<T>: boolean test(T)
    public  static final Predicate<String> containsMohamed =
            s -> s != null && s.contains("Mohamed");

    //  BiPredicate<T,U>: boolean test(T,U)
    public static final BiPredicate<String, String> contains =
            (s, part) -> s != null && part != null && s.contains(part);

    //  Supplier<T>: T get()
    public static final Supplier<Double> randomDouble =
            () -> new Random(42).nextDouble(); // fixed seed for repeatable output

    //  Consumer<T>: void accept(T)
    public static final Consumer<String> printer =
            System.out::println;

    //  BiConsumer<T,U>: void accept(T,U)
    public static final BiConsumer<String, String> joinPrinter =
            (a, b) -> System.out.println(a + " " + b);

    //  Function<T,R>: R apply(T)
    public static final Function<String, Integer> length =
            s -> (s == null) ? 0 : s.length();

    //  BiFunction<T,U,R>: R apply(T,U)
    public static final BiFunction<String, String, String> concatWithDash =
            (a, b) -> a + "-" + b;

    //  UnaryOperator<T> = Function<T,T>
    public static final UnaryOperator<String> trim =
            s -> s == null ? null : s.trim();

    //  BinaryOperator<T> = BiFunction<T,T,T>
    public static final BinaryOperator<Integer> max =
            Integer::max;

    //  Primitive specializations (avoid boxing) — exam likes these
    public static final IntPredicate isEven =
            n -> n % 2 == 0;

    public static final ToIntFunction<String> parseOrZero =
            s -> {
                try {
                    return Integer.parseInt(s);
                } catch (Exception e) {
                    return 0;
                }
            };

    public static final IntSupplier tenSupplier =
            () -> 10;

    public static final IntConsumer intPrinter =
            n -> System.out.println("int=" + n);

    
    public static void main(String[] args) {
        String name = "Mohamed Ali";
        String other = "Ali";

        // Predicate / BiPredicate
        System.out.println("containsMohamed? " + containsMohamed.test(name));
        System.out.println("contains(other)? " + contains.test(name, other));

        // Supplier
        System.out.println("randomDouble: " + randomDouble.get());

        // Consumer / BiConsumer
        printer.accept("Hello from Consumer");
        joinPrinter.accept("Hello", "World");

        // Function / BiFunction
        System.out.println("length(name): " + length.apply(name));
        System.out.println("concatWithDash: " + concatWithDash.apply("A", "B"));

        // Operators
        System.out.println("trim: '" + trim.apply("  hi  ") + "'");
        System.out.println("max: " + max.apply(10, 7));

        // Primitive specializations
        System.out.println("isEven(6): " + isEven.test(6));
        System.out.println("parseOrZero('123'): " + parseOrZero.applyAsInt("123"));
        System.out.println("parseOrZero('x'): " + parseOrZero.applyAsInt("x"));
        intPrinter.accept(tenSupplier.getAsInt());

        // -------------------------
        // ❌ Exam traps (commented)
        // -------------------------

        // 1) Supplier has NO parameters:
        // Supplier<String> s = x -> "nope"; // ❌ compile error

        // 2) Predicate returns boolean (not int/String):
        // Predicate<String> p = s -> 1; // ❌ compile error

        // 3) Consumer returns void:
        // Consumer<String> c = s -> s.length(); // ❌ compile error (returns int)

        // 4) Function must return a value:
        // Function<String, Integer> f = s -> { System.out.println(s); }; // ❌ missing return

        // 5) Primitive vs wrapper mismatch:
        // IntPredicate ip = (Integer x) -> x > 0; // ❌ parameter must be int (no Integer)
    }
}
