public class Dispatch {
    class A {
        public void foo() {
            System.out.println("A.foo");
        }
    }

    class B extends A {
    }

    class C extends B {
        public void foo() {
            System.out.println("C.foo");
        }
    }

    public static void test(){
        System.out.println("test");
    }

    public void main(String[] args) {
        Dispatch.test();

        B x = new B();
        x.foo();

        A y = new C();
        y.foo();
    }
}
