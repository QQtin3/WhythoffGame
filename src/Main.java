/**
 * Représentation du jeu de Whythoff jouable dans un terminal en version joueur contre joueur
 *
 * @author LEGEAY Quentin
 * @version 1.0
 */
class Main {
    void principal() {
        // Dé commenter ligne de dessous pour l'exécution des tests.
        //globalTest();

        System.out.println("Bienvenue dans le jeu !");
        System.out.println("Règles : ");
        System.out.println("""
                Le pion est placé initialement au hasard sur le plateau. A chaque tour, un joueur a le droit a un
                seul de ces trois mouvements : déplacement d’un nombre quelconque de cases vers la gauche, le
                bas, ou le long d’une diagonale vers la gauche et le bas. Le joueur gagnant est celui qui parvient a
                mettre le pion sur la case inférieure gauche.
                """);

        String player1 = SimpleInput.getString("Nom du joueur1 : ");
        String player2 = SimpleInput.getString("Nom du joueur2 : ");

        // Choisir la taille du plateau
        int boardSize;
        do {
            boardSize = SimpleInput.getInt("Veuillez saisir la taille du plateau de jeu (min. 3, max. 100) ");
        } while (boardSize < 3 || boardSize > 100);

        // Créer la matrice de jeu
        int[][] board = createBoard(boardSize);

        // Débute le jeu Joueur contre Joueur (JcJ ou PvP)
        gamePvP(board, player1, player2);
    }

    /**
     * Déroule une partie en Joueur contre Joueur (ou Player versus Player (PvP)) du jeu
     *
     * @param board   Matrices de jeu
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
     * Créer une matrice vide de taille n avec un pion positionné "aléatoirement" en x, y qui associe matrice[y][x] = 1
     * Le pion ne peut être positionné sur les deux premières lignes ou colonnes pour éviter les parties évidentes.
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
     * Permet de retourner un tableau contenant la position du pion dans un repaire x, y
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
            if (tab.length > 10 && i < 10) {
                System.out.print("0" + i + " ");
            } else {
                System.out.print(i + " ");
            }

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

        // Ajoute la ligne tout en bas et lisse l'affichage quand + d'un chiffre
        if (tab.length > 10) {
            System.out.print("  ");
        } else {
            System.out.print(" ");
        }
        for (int k = 0; k < tab.length; k++) {
            if (tab.length > 10) {
                if (k < 10) {
                    System.out.print("  0" + k);
                } else {
                    System.out.print("  " + k);
                }
            } else {
                System.out.print("   " + k);
            }
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
     * Méthode vérifiant la possibilité du coup entré par l'utilisateur (ne sort pas du tableau...)
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

        /* Si dans un des deux cas le pion se trouve en position inférieure à 0 alors il est en dehors du tableau et
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


    /* ######### TEST METHODS PART #########
     *  Cette partie permet le test de toutes les autres méthodes utilisées dans le programme.
     *  Elle utilise deux autres méthodes qui permettent d'afficher correctement
     *  les matrices et tableaux (displayMatriceTest et displayTabTest)
     *
     *  Tous les tests de méthodes sont regroupés dans globalTest() ce qui permet un rendu plus
     *  efficace dans principal() en cas de doute sur l'efficacité des méthodes (évite les appels 1 à 1)
     *  #####################################
     * */


    /**
     * Méthode de test qui affiche toutes les autres méthodes de test afin de s'assurer du bon fonctionnement
     * de toutes les autres méthodes du programme.
     */
    void globalTest() {
        System.out.println("### Test methods ###");

        //ATTENTION : Sur ces affichages les coordonnées (0,0) sont en haut à gauche contrairement à l'affichage du jeu
        int[][] matrice = {{0, 1, 0},
                {0, 0, 0},
                {0, 0, 0}};
        int[] pawnPosition = {0, 1};
        testGameIsDone(matrice, false);
        testGetPawnPosition(matrice, pawnPosition);
        testLegalMove(pawnPosition, 1, 1, true);
        testCreateBoard(4);
        int[][] newMatrice = {{0, 0, 0},
                {0, 0, 0},
                {0, 0, 1}};
        int[] coord1 = {0, 1};
        int[] coord2 = {2, 2};
        testSwitchTwoCase(matrice, coord1, coord2, newMatrice);

        System.out.println("####################\n\n\n");
    }

    /**
     * Retourne en String une matrice donnée en entrée.
     *
     * @param matrice: matrice (d'entiers)
     * @return String de l'affichage de la matrice
     */
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
     * Retourne en String un tableau donné en entrée
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

    /**
     * Méthode de test de legalMove() qui renvoie vrai si le coup est jouable par un utilisateur ou non
     *
     * @param pawnPosition   Coordonnés du pion sur le plateau
     * @param playerDecision Le coup décidé par le joueur (1/2/3)
     * @param nbCase         Le nombre de cases de déplacement du joueur
     * @param result         Contient le résultat attendu à l'issue de la fonction qui doit être testée
     */
    void testLegalMove(int[] pawnPosition, int playerDecision, int nbCase, boolean result) {
        System.out.println("legalMove(" + displayTabTest(pawnPosition)+ ", " + playerDecision + ", " + nbCase + ") : ");
        boolean testResult = legalMove(pawnPosition, playerDecision, nbCase);
        if (result == testResult) {
            System.out.println("OK");
        } else {
            System.out.println("ERREUR");
        }
    }

    /**
     * Méthode de test de gameIsDone() qui test si la partie est finie ou non.
     *
     * @param matrice Matrice de jeu avec notre pion
     * @param result  Booléen qui contient le résultat attendu à l'issue de la fonction qui doit être testée
     */
    void testGameIsDone(int[][] matrice, boolean result) {
        System.out.println("gameIsDone(" + displayMatriceTest(matrice) + ") : ");
        boolean testResult = gameIsDone(matrice);
        if (result == testResult) {
            System.out.println("OK");
        } else {
            System.out.println("ERREUR");
        }
    }

    /**
     * Méthode de test de getPawnPosition() qui renvoie les coordonnées du pion sur le plateau
     *
     * @param matrice Matrice de jeu avec notre pion
     * @param result  Tableau qui contient le résultat attendu de la fonction getPawnPosition pour le tableau donné
     */
    void testGetPawnPosition(int[][] matrice, int[] result) {
        System.out.println("getPawnPosition(" + displayMatriceTest(matrice) + ") : ");
        int[] testResult = getPawnPosition(matrice);
        // Vérifie que les deux tableaux à deux nombres aient bien les mêmes valeurs
        if (result[0] == testResult[0] && result[1] == testResult[1]) {
            System.out.println("OK");
        } else {
            System.out.println("ERREUR");
        }
    }

    /**
     * Méthode de test de createBoard() qui créé une matrice de taille size, et place un pion "aléatoirement" dedans
     *
     * @param size Taille du plateau qui doit être créé
     */
    void testCreateBoard(int size) {
        System.out.println("createBoard(" + size + ") : ");
        int[][] testResult = createBoard(size);

        // Vérifie le nombre de pions sur le plateau + si notre plateau est carré
        boolean isSquare = true;
        int numberOfPawn = 0;
        for (int i = 0; i < testResult.length; i++) {
            for (int j = 0; j < testResult[i].length; j++) {
                if (testResult[i][j] == 1) {  // Détecte si on trouve notre pion
                    numberOfPawn++;
                }
                if (testResult.length != testResult[i].length) {
                    isSquare = false;
                }
            }
        }

        // Vérifie que le tableau soit bien carré, de longueur size,
        if ((size == testResult.length) && (isSquare) && (numberOfPawn == 1)) {
            System.out.println("OK");
        } else {
            System.out.println("ERREUR");
        }
    }

    /**
     * Méthode de test de switchTwoCase() qui échange deux cases d'une matrice
     *
     * @param coord1  Coordonnés de la première case à intervertir
     * @param coord2  Coordonnés de la deuxième case à intervertir
     * @param matrice Matrice à l'origine utilisée avant l'utilisation de la fonction
     * @param result  Matrice qui contient le résultat attendu à l'issue de la fonction qui doit être testée
     */
    void testSwitchTwoCase(int[][] matrice, int[] coord1, int[] coord2, int[][] result) {
        // Méthode non vue : copie le contenu d'un tableau/d'une matrice
        int[][] matriceCopy = matrice.clone();

        System.out.println("switchTwoCase(" + displayMatriceTest(matriceCopy) + ", "
                + displayTabTest(coord1) + ", "
                + displayTabTest(coord2) + ", "
                + displayMatriceTest(result) + ") : ");

        switchTwoCase(matriceCopy, coord1, coord2);

        /* Ici on va regarder l'ensemble des cases de notre matrice modifiée et de notre résultat attendu afin d'être
         * absolument sûr que notre fonction de départ n'ait pas d'effet inattendu sur d'autres cases du tableau */
        boolean isEqual = true;
        for (int i = 0; i < matriceCopy.length; i++) {
            for (int j = 0; j < matriceCopy[i].length; j++) {
                if (matriceCopy[i][j] != result[i][j]) { // Si on remarque une différence entre deux cases
                    isEqual = false;
                }
            }
        }

        if (isEqual) {
            System.out.println("OK");
        } else {
            System.out.println("ERREUR");
        }
    }
}