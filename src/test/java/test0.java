public class test0 {
    public static int main() {
        int a, b, c;
        a = 1;
        b = 2;
        c = a + b;
        if(a + c == 4) {
             c = c + a;
        }else {
            c = c + b;
        }
        return c;
    }
}
