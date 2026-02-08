
@FunctionalInterface
public interface MyValidFunctionalInterface{
    String myMethod();
    @Override
    String toString(); // This is allowed as it is a method from Object class
    
    static void myStaticMethod(){
        System.out.println("This is a static method in the functional interface");
    }

    default void myDefaultMethod(){
        System.out.println("This is a default method in the functional interface");
    }
    
}
interface MyInvalidFunctionalInterface{
    String myMethod1();
    String myMethod2(); // This will cause a compilation error as it has more than one abstract method
}


