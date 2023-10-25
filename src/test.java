class test {
    void principal() {
        int i = 0;
        int cpt1 = 0;
        int cpt2 = 0;
        while (i < 1000) {
            int playerTurn = (int) (Math.random() * 2);
            if (playerTurn == 0) {
                cpt1++;
            }
            if (playerTurn == 1) {
                cpt2++;
            }
            i++;
        }
        System.out.println(cpt1 +" | " + cpt2);
    }
}
