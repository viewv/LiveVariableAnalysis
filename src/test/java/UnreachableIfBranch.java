class UnreachableIfBranch {

    int branch() {
        int x = 10;
        int y = 2;
        int z;
        if (x > y) {
            z = 5;
        } else {
            z = 5; // unreachable branch
        }
        switch (z) {
            case 1:
                z = 1000; // unreachable branch
                break;
            case 2:
                z = 2000; // unreachable branch
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
        int k = z++; // unreachable branch
        int m = z;
        if (z > m) {
            k = z;
        }else {
            k = m; // unreachable branch
        }
        return z;
    }
}
