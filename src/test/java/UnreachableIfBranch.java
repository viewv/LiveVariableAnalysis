class UnreachableIfBranch {

    int branch() {
        int x = 10;
        int y = 2;
        int z;
        if (x > y) {
            z = 100;
        } else {
            z = 200; // unreachable branch
        }
        switch (y) {
            case 1:
                z = 1000;
                break;
            case 2:
                z = 2000;
            case 3:
                z = 3000;
                break;
            case 6:
                z = 4000; // unreachable branch
                break;
            default:
                z = 7000;
                break;
        }
        int k = z++;
        int m = z;
        if (z > m) {
            k = z;
        }else {
            k = m;
        }
        return z;
    }
}
