class UnreachableIfBranch {

    int branch() {
        int x = 10;
        int y = 1;
        int z;
        if (x > y) {
            z = 100;
        } else {
            z = 200; // unreachable branch
        }
        switch (x + 3) {
            case 10:
                z = 1000;
                break;
            case 20:
                z = 2000; // unreachable branch
                break;
            default:
                z = 3000;
                break;
        }
        return z;
    }
}
