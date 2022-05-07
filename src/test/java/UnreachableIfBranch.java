class UnreachableIfBranch {

    int branch() {
        int x = 10;
        int y = 4;
        int z;
        if (x > y) {
            z = 100;
        } else {
            z = 200; // unreachable branch
        }
        switch (z) {
            case 1:
                z = 1000;
                break;
            case 2:
                z = 2000; // unreachable branch
                break;
            case 3:
                z = 3000; // unreachable branch
                break;
            case 6:
                z = 4000; // unreachable branch
                break;
            default:
                z = 7000;
                break;
        }
        int k = z;
        int m = 10;
        if (z > m) {
            k = z;
        }else {
            k = m;
        }
        return z;
    }
}
