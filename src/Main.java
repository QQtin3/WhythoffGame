/**
 * Représentation du jeu de Whythoff jouable dans un terminal en version joueur contre joueur
 *
 * @author LEGEAY Quentin
 * @version 1.0
 */
class Main {
    void principal() {
        globalTest();

        System.out.println("Bienvenue dans le jeu !");
        System.out.println("Règles : ");
        System.out.println("""
                Le pion est placé initialement au hasard sur le plateau. A chaque tour, un joueur a le droit a un
                seul de ces trois mouvements : déplacement d’un nombre quelconque de cases vers la gauche, le
                bas, ou le long d’une diagonale vers la gauche et le bas. Le joueur gagnant est celui qui parvient a
                mettre le pion sur la case inférieure gauche.\n""");

        String player1 = SimpleInput.getString("Nom du joueur1 : ");
        String player2 = SimpleInput.getString("Nom du joueur2 : ");

        // Choisir la taille du plateau
        int boardSize;
        do {
            boardSize = SimpleInput.getInt("Veuillez saisir la taille du plateau de jeu (min. 3, max.99) ");
        } while (boardSize < 3 || boardSize > 99);

        // Créer la matrice de jeu
        int[][] board = createBoard(boardSize);

        // Débute le jeu Joueur contre Joueur (JcJ ou PvP)
        gamePvP(board, player1, player2);

    }

    /**
     * Déroule une partie en Joueur contre Joueur (ou Player versus Player (PvP)) du jeu
     *
     * @param board   Matricce de jeu
     * @param player1 Nom du joueur1
     * @param player2 Nom du joueur2
     */
    void gamePvP(int[][] board, String player1, String player2) {

        // Le premier joueur est choisi "aléatoirement"
        int playerTurn = (int) (Math.random() * 2);

        int nbTour = 1;
        int playerDecision;
        boolean winCondition = false;

        // Déroulement du jeu
        while (!winCondition) {
            System.out.print("Tour n° " + nbTour + " - ");
            if (playerTurn == 0) {
                System.out.println("c'est à " + player1 + " de jouer");
            } else {
                System.out.println("c'est à " + player2 + " de jouer");
            }

            // Affiche la position du pion sur le plateau et le plateau
            int[] pawn = getPawnPosition(board);
            displayTab(board);

            // Gère les erreurs possibles du joueur lors de ses choix de déplacement.
            boolean isLegal;
            int nbCase;
            do {

                // Empêche le joueur de rentrer une valeur autre que 1/2/3
                do {
                    System.out.println("""
                            Vous disposez de 3 coups :
                             1. Déplacement vers la gauche
                             2. Déplacement vers le bas
                             3. Déplacement en diagonal (gauche-bas)""");
                    playerDecision = SimpleInput.getInt("Que décidez vous de jouer ? ");
                } while (playerDecision < 1 || playerDecision > 3);


                // Empêche le joueur de saisir une valeur négative
                do {
                    nbCase = SimpleInput.getInt("Veuillez saisir un nombre de case de déplacement ");
                    isLegal = legalMove(pawn, playerDecision, nbCase);
                } while (nbCase <= 0);


                if (!isLegal) {
                    System.out.println("""
                            ##################################
                            Veuillez rentrer une valeur valide
                            ##################################""");
                }
            } while (!isLegal);

            playerMove(board, nbCase, playerDecision);
            System.out.println();

            // Regarde si la partie est terminée
            winCondition = gameIsDone(board);

            // Si la partie n'est pas finie
            if (!winCondition) {
                nbTour++;

                // Permet de changer de joueur (0 ou 1)
                playerTurn = (playerTurn + 3) % 2;
            }
        }
        displayResult(playerTurn, player1, player2, nbTour);
    }


    /**
     * Créer une matrice vide de taille n avec un pion positionné "aléatoirement" en x,y qui associe matrice[y][x] = 1
     * Le pion ne peut être positionné sur les deux premières lignes et/ou colonnes pour éviter les parties évidentes.
     *
     * @param size Taille du tableau
     * @return Matrice de jeu
     */
    int[][] createBoard(int size) {
        int[][] tab = new int[size][size];

        // Le +2 évite les cas de parties trop évidentes où le pion est sur les deux premières lignes et colonnes
        int pawnX = 2 + (int) (Math.random() * (size - 2));
        int pawnY = 2 + (int) (Math.random() * (size - 2));
        tab[pawnY][pawnX] = 1;  // Place le pion
        return tab;
    }

    /**
     * Effectue le déplacement du pion à travers le plateau selon les entrées direction et nbCase.
     *
     * @param tab       Matrice de jeu
     * @param nbCase    Nombre de cases de déplacement du pion
     * @param direction Direction du pion (gauche, bas, diagonal gauche-bas)
     */
    void playerMove(int[][] tab, int nbCase, int direction) {
        int[] pos = getPawnPosition(tab);
        int y = pos[0];
        int x = pos[1];
        int[] newPos = {y, x};

        if (direction != 1) {
            newPos[0] -= nbCase;

        }
        if (direction != 2) {
            newPos[1] -= nbCase;
        }
        switchTwoCase(tab, pos, newPos);
    }

    /**
     * Permet de retourner un tableau contenant la position du pion dans un repaire x,y
     *
     * @param tab Matrice de jeu
     * @return Tableau avec la position y,x du pion (tab[0] = y et tab[1] = x)
     */
    int[] getPawnPosition(int[][] tab) {
        int[] pos = new int[2];
        boolean found = false;
        int i = 0;
        while (i < tab.length && !found) {
            int j = 0;
            while (j < tab.length && !found) {
                if (tab[i][j] == 1) {  // Détecte si on trouve notre pion
                    pos[0] = i;
                    pos[1] = j;
                    found = true;
                }
                j++;
            }
            i++;
        }
        return pos;
    }

    /**
     * Permet d'échanger deux cases d'une matrice
     *
     * @param tab    Matrice où l'échange a lieu
     * @param coord1 Tableau contenant les coordonnées de la première case
     * @param coord2 Tableau contenant les coordonnées de la deuxième case
     */
    void switchTwoCase(int[][] tab, int[] coord1, int[] coord2) {
        int tmp = tab[coord1[0]][coord1[1]];
        tab[coord1[0]][coord1[1]] = tab[coord2[0]][coord2[1]];
        tab[coord2[0]][coord2[1]] = tmp;
    }

    /**
     * Affiche la matrice entrée sous la forme d'un tableau de jeu avec les coordonnées écrites sur la gauche et en bas
     *
     * @param tab Matrice de jeu
     */
    void displayTab(int[][] tab) {
        for (int i = tab.length - 1; i >= 0; i--) {
            // Affiche la ligne de gauche
            System.out.print(i + " ");
            for (int j = 0; j < tab[0].length; j++) {
                System.out.print("|");
                if (tab[i][j] == 0) {
                    System.out.print("   ");
                } else {
                    System.out.print(" O ");
                }
            }
            System.out.println("|");
        }

        // Ajoute la ligne tout en bas
        System.out.print(" ");
        for (int k = 0; k < tab.length; k++) {
            System.out.print("   " + k);
        }
        System.out.println();
    }

    /**
     * Renvoie l'information sur si le pion est en coordonnées (0,0) et donc si la partie est finie ou non.
     *
     * @param tab Matrice de jeu
     * @return true s'il y a un pion en (0,0), false sinon
     */
    boolean gameIsDone(int[][] tab) {
        return tab[0][0] == 1;
    }


    /**
     * Méthode vérifiant la possibilité du coup entré par l'utilisateur (ne sort pas du tableau..)
     *
     * @param pawnPosition   Tableau avec les coordonnées du pion
     * @param playerDecision Le coup décidé par le joueur (1/2/3)
     * @param nbCase         De combien de case le pion doit bouger
     * @return true si c'est un coup légal (possible) sinon false
     */
    boolean legalMove(int[] pawnPosition, int playerDecision, int nbCase) {
        int[] tempTab = {pawnPosition[0], pawnPosition[1]};
        boolean isLegal = true;
        if (playerDecision != 1) {
            tempTab[0] -= nbCase;

        }
        if (playerDecision != 2) {
            tempTab[1] -= nbCase;
        }

        /* Si dans un des deux cas le pion se trouve en position inférieur à 0 alors il est en dehors du tableau et
           c'est donc un coup illégal */
        if (tempTab[0] < 0 || tempTab[1] < 0) {
            isLegal = false;
        }

        return isLegal;
    }

    /**
     * @param playerTurn Qui joue le coup gagnant
     * @param nbTour     Nombre de tours
     * @param player1    Nom du joueur 1
     * @param player2    Nom du joueur 2
     */
    void displayResult(int playerTurn, String player1, String player2, int nbTour) {
        System.out.println("##### RESUME DE LA PARTIE #####");
        System.out.print("Le gagnant est : ");
        if (playerTurn == 0) {
            System.out.println(player1);
        } else if (playerTurn == 1) {
            System.out.println(player2);
        } else {
            System.out.println("ERREUR: Impossible d'obtenir de résultat, playerTurn est incorrect !");
        }
        System.out.println("Nombre de coups : " + nbTour);
        System.out.println("###############################");
    }


    // ######### TEST METHODS PART #########

    void globalTest() {
        System.out.println("### Test methods ###");

        int[][] matrice = {{0, 1, 0}, {0, 0, 0}, {0, 0, 0}};
        testGameIsDone(matrice, false);


        System.out.println("\n\n");
    }

    String displayMatriceTest(int[][] matrice) {
        String display = " {";
        for (int i = 0; i < matrice.length; i++) {
            display += "{";
            for (int j = 0; j < matrice[i].length; j++) {
                display += matrice[i][j];
                if (j != matrice[i].length - 1) {
                    display += ", ";
                }
            }
            display += "}";
            if (i != matrice.length - 1) {
                display += ", ";
            }
        }
        display += "} ";
        return display;
    }

    /**
     * Retourne en String un tableau donné en entrée.
     *
     * @param tab: tableau d'entiers
     * @return String de l'affichage du tableau
     */
    String displayTabTest(int[] tab) {
        int i = 0;
        String display = "{";
        while (i < tab.length - 1) {
            display += (tab[i] + ",");
            i = i + 1;
        }
        if (tab.length != 0) {
            display += (tab[i] + "}");
        } else {
            display += "}";
        }
        return display;
    }

    void testLegalMove(int[] pawnPosition, int playerDecision, int nbCase, boolean result) {
        System.out.println("legalMove(" + displayTabTest(pawnPosition) + ", " + playerDecision + ", " + nbCase + ") : ");
        boolean testResult = legalMove(pawnPosition, playerDecision, nbCase);
        if (result == testResult) {
            System.out.print("OK");
        } else {
            System.out.print("ERREUR");
        }
    }

    void testGameIsDone(int[][] tab, boolean result) {
        System.out.println("gameIsDone(" + displayTabTest(tab) + ") : ");
        boolean testResult = gameIsDone(tab);
        if (result == testResult) {
            System.out.print("OK");
        } else {
            System.out.print("ERREUR");
        }
    }

    void testGetPawnPositition(int[][] tab, boolean result) {
        System.out.println("gameIsDone(" + displayTabTest(tab) + ") : ");
        boolean testResult = gameIsDone(tab);
        if (result == testResult) {
            System.out.print("OK");
        } else {
            System.out.print("ERREUR");
        }
    }

    void testCreateBoard(int[][] tab, boolean result) {
        System.out.println("gameIsDone(" + displayTabTest(tab) + ") : ");
        boolean testResult = gameIsDone(tab);
        if (result == testResult) {
            System.out.print("OK");
        } else {
            System.out.print("ERREUR");
        }
    }
}
