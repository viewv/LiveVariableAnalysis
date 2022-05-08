public class LoopUnreachable {
    public static int main(String[] args) {
        int x = 0;
        int y = 5;
        int z;
        for (int i = 0; i < 10; i++) {
            z = x + 1;
            int w = y + 1;
        }
        return x + y;
    }
}
