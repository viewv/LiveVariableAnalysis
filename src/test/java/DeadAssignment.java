class DeadAssignment {

    void deadAssign() {
        int x = 1;
        int y = x + 2;
        int z = x + 3;
        y = use(z);
        int a = x;
        if (x < 0) {
            y = 1000;
        }else {
            y = 2000;
        }
        System.out.println(y);
    }

    int use(int n) {
        n = n + 1;
        System.out.println(n);
        return n + 1;
    }
}
