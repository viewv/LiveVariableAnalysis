public class Dispatch implements InterfaceDemo {
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

    @Override
    public void demo1() {
        System.out.println("Interface demo1");
    }


    @Override
    public void demo2() {
        System.out.println("Interface demo2");
    }

    public void main(String[] args) {
        Dispatch.test();

        B x = new B();
        x.foo();

        A y = new C();
        y.foo();

        InterfaceDemo z = new Dispatch();
        z.demo1();
        z.demo2();
    }
}
