/**
 * Représentation du jeu de Whythoff jouable dans un terminal en version joueur contre ordinateur
 *
 * @author LEGEAY Quentin
 * @version 1.0
 */
class Main2 {
    void principal() {
        //Dé commenter ligne de dessous pour l'exécution des tests.
        //globalTest();

        System.out.println("Bienvenue dans le jeu !");
        System.out.println("Règles : ");
        System.out.println("""
                Le pion est placé initialement au hasard sur le plateau. A chaque tour, un joueur a le droit a un
                seul de ces trois mouvements : déplacement d’un nombre quelconque de cases vers la gauche, le
                bas, ou le long d’une diagonale vers la gauche et le bas. Le joueur gagnant est celui qui parvient a
                mettre le pion sur la case inférieure gauche.
                """);

        // Choisir le pseudonyme du joueur
        String player = SimpleInput.getString("Nom du joueur : ");

        // Choisir la taille du plateau
        int boardSize;
        do {
            boardSize = SimpleInput.getInt("Veuillez saisir la taille du plateau de jeu (min. 3, max.99) ");
        } while (boardSize < 3 || boardSize > 99);

        // Créer la matrice de jeu
        int[][] board = createBoard(boardSize);

        gamePvB(board, player);
    }

    /**
     * Déroule une partie en Joueur contre Ordinateur (ou Player versus Bot (PvB)) du jeu
     *
     * @param board  Matrice de jeu
     * @param player Nom du joueur
     */
    void gamePvB(int[][] board, String player) {
        int nbTour = 1;
        boolean winCondition = false;

        int reponseStart;
        do {
            // /!\ Ne gère pas le case sensitive
            reponseStart = SimpleInput.getInt("Voulez vous jouer le premier coup ? (1: Oui/ 2: Non)");
        } while (reponseStart != 1 && reponseStart != 2);

        int playerTurn;
        if (reponseStart == 1) {
            playerTurn = 0;
        } else {
            playerTurn = 1;
        }

        // Permet de définir les positions gagnantes sur le plateau
        int[][] posGagnante = getPosGagnante(board);
        for (int i = 0; i < posGagnante.length; i++) {
            setPosGagnante(board, posGagnante[i]);
        }

        // Déroulement du jeu
        while (!winCondition) {
            System.out.print("Tour n° " + nbTour + " - ");
            if (playerTurn == 0) {
                System.out.println("c'est à " + player + " de jouer");
            } else {
                System.out.println("c'est à l'ordinateur de jouer");
            }
            displayMatrice(board);

            if (playerTurn == 0) {  // Le cas où c'est au joueur humain de jouer
                playerToPlay(board);
            } else {
                botMove(board);
            }

            // Détermine si la partie est finie
            winCondition = gameIsDone(board);

            // Si la partie n'est pas finie
            if (!winCondition) {
                nbTour++;

                // Permet de changer de joueur (0 ou 1)
                playerTurn = (playerTurn + 3) % 2;
            }
        }
        displayResult(board, playerTurn, player, nbTour);
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

        int pawnX;
        int pawnY;
        do {
            // Le +2 évite les cas de parties trop évidentes où le pion est sur les deux premières lignes et colonnes
            pawnX = 2 + (int) (Math.random() * (size - 2));
            pawnY = 2 + (int) (Math.random() * (size - 2));
        } while (pawnX == pawnY);
        tab[pawnY][pawnX] = 1;  // Place le pion
        return tab;
    }

    /**
     * Permet l'interaction et le mouvement avec l'utilisateur lors de son coup, textuel et déplacement du pion
     * sur le plateau selon les entrées saisies.
     *
     * @param board Matrice de jeu
     */
    void playerToPlay(int[][] board) {
        int[] pawnPosition = getPawnPosition(board);
        boolean isLegal;
        int nbCase;
        int playerDecision;

        // Gère les erreurs possibles du joueur lors de ses choix de déplacement.
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
                isLegal = legalMove(pawnPosition, playerDecision, nbCase);
            } while (nbCase <= 0);


            if (!isLegal) {
                System.out.println("""
                        ##################################
                        Veuillez rentrer une valeur valide
                        ##################################""");
            }
        } while (!isLegal);
        playerMove(board, nbCase, playerDecision);
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
     * Permet au bot (ordinateur) de jouer un coup lorsque c'est à son tour de jouer.
     * Joue légèrement intelligemment (aléatoire sauf s'il est possible d'atteindre une position gagnante en 1 coup)
     *
     * @param board Matrice de jeu
     */
    void botMove(int[][] board) {
        int[] pawnPosition = getPawnPosition(board);
        int y = pawnPosition[0];
        int x = pawnPosition[1];
        int[] newPos = {y, x};

        int y_limit = y - 1;
        int x_limit = x - 1;

        boolean isPossibleToWin = false;
        int[][] winningPosition = getPosGagnante(board);

        // Vérifie qu'on ne puisse pas atteindre une position gagnante avec la position actuelle
        int i = 0;
        while (i < winningPosition.length && !isPossibleToWin) {
            isPossibleToWin = isReachableFromPawn(pawnPosition, winningPosition[i]);
            i++;
        }

        boolean isSamePosition;
        if (isPossibleToWin) {
            isSamePosition = (pawnPosition[0] == winningPosition[i - 1][0]) && (pawnPosition[1] == winningPosition[i - 1][1]);
        } else {
            isSamePosition = true;
        }

        // Si la position est gagnante et n'est pas la position déjà occupée par le pion
        if (!isSamePosition) {
            newPos[0] = winningPosition[i - 1][0];
            newPos[1] = winningPosition[i - 1][1];
        } else {

            // Empêche le bot d'aller dans une direction impossible à jouer
            int direction;
            do {
                direction = (int) (Math.random() * 3);
            } while ((direction == 1 && x_limit < 0) || (direction == 2 && (y_limit < 0)) || (direction == 3 && (y_limit < 0 || x_limit < 0)));
            // TODO: Refact cette expression différemment.

            // Défini un nombre de cases de déplacement (dans la limite du possible) qui va être joué
            if (direction != 1) {
                newPos[0] -= (int) (1 + Math.random() * (newPos[0] - 1));
            }
            if (direction != 2) {
                newPos[1] -= (int) (1 + Math.random() * (newPos[1] - 1));
            }
        }
        switchTwoCase(board, pawnPosition, newPos);
    }

    /**
     * Détermine s'il est possible de passer des coordonnées du pion à celles de la deuxième position en 1 coup
     *
     * @param pawnPosition Position du pion sur le plateau sous forme de tableau à 2 valeurs (y, x)
     * @param coord        Position sur le plateau sous forme d'un tableau à deux valeurs (y, x)
     * @return Vrai si possible de passer des coordonnées du pion à celles de la deuxième position en 1 coup sinon faux
     */
    boolean isReachableFromPawn(int[] pawnPosition, int[] coord) {
        // Test s'ils se trouvent sur la même diagonale
        boolean sameDifference = pawnPosition[0] - coord[0] == pawnPosition[1] - coord[1];

        // Si on a le même x ou y pour les 2 positions alors la position est atteignable (+ diagonale avec sameDifference)
        boolean isReachable = pawnPosition[0] == coord[0] || pawnPosition[1] == coord[1] || sameDifference;
        return isReachable;
    }

    /**
     * Permet d'obtenir l'intégralité des positions gagnantes sur le plateau de jeu
     *
     * @param board Matrice de jeu
     * @return Matrice contenant les coordonnées des positions gagnantes sur le plateau
     */
    int[][] getPosGagnante(int[][] board) {
        String positionGagnante = "0-0";
        int i = 0;
        int rank = 0;
        while (i <= board.length) {
            int[][] matricePosGagnante = stringToMatrice(positionGagnante);

            // Sert à regarder si i est déjà en tant qu'abscisse ou ordonné
            boolean isNotInMatrice = true;
            int j = 0;
            while (j < matricePosGagnante.length && isNotInMatrice) {
                int k = 1;
                while (k <= 1 && isNotInMatrice) {
                    if (matricePosGagnante[j][k] == i) {
                        isNotInMatrice = false;
                    }
                    k++;
                }
                j++;
            }

            // Si i n'a pas été utilisé précédemment alors c'est une nouvelle position gagnante
            if (isNotInMatrice) {
                rank++;
                int x = i;
                int y = x + rank;
                if (x < board.length && y < board.length) {
                    positionGagnante += " " + x + "-" + y + " " + y + "-" + x;  // Ajoute (x,y) mais aussi (y,x)
                }
            }
            i++;
        }
        int[][] result = stringToMatrice(positionGagnante);
        return result;
    }

    /**
     * Change les valeurs de la matrice aux coordonnées données (x, y) à 2, mais également (y, x) à 2
     *
     * @param board Matrice de jeu
     * @param coord Tableau contenant 2 valeurs, les coordonnées à mettre comme valeur 2
     */
    void setPosGagnante(int[][] board, int[] coord) {
        board[coord[0]][coord[1]] = 2;
        board[coord[1]][coord[0]] = 2;
    }

    /**
     * Transforme une chaîne de caractère en une matrice lorsque possible, mettre un espace pour un nouvel élément du
     * tableau principal, mettre un - pour séparer plusieurs éléments dans un même tableau (max. 3 éléments)
     * "1-2-3 5-7-9 2-4-6" devient {{1, 2, 3}, {5, 7, 9}, {2, 4, 6}}
     *
     * @param sequence Chaîne de caractères
     * @return Matrice créée grâce à la chaîne de caractères
     */
    int[][] stringToMatrice(String sequence) {
        String[] pos = sequence.split(" ");
        int[][] matrice = new int[pos.length][2];

        for (int i = 0; i < pos.length; i++) {
            String[] coords = pos[i].split("-");
            for (int j = 0; j < matrice[i].length; j++) {
                matrice[i][j] = Integer.parseInt(coords[j]);
            }
        }
        return matrice;
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
     * Permet d'échanger deux cases d'une matrice et empêcher d'afficher une position gagnante à un autre endroit
     * à cause du déplacement de case
     *
     * @param tab    Matrice où l'échange a lieu
     * @param coord1 Tableau contenant les coordonnées de la première case
     * @param coord2 Tableau contenant les coordonnées de la deuxième case
     */
    void switchTwoCase(int[][] tab, int[] coord1, int[] coord2) {
        int tmp = tab[coord1[0]][coord1[1]];

        // Empêche d'afficher une position gagnante à un autre endroit à cause du déplacement de case
        if (tmp == 2) {
            tmp = 0;
        } else if (tab[coord2[0]][coord2[1]] == 2) {
            tab[coord2[0]][coord2[1]] = 0;
        }

        tab[coord1[0]][coord1[1]] = tab[coord2[0]][coord2[1]];
        tab[coord2[0]][coord2[1]] = tmp;
    }

    /**
     * Affiche la matrice entrée sous la forme d'un tableau de jeu avec les coordonnées écrites sur la gauche et en bas
     *
     * @param tab Matrice de jeu
     */
    void displayMatrice(int[][] tab) {
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
                } else if (tab[i][j] == 2) {
                    System.out.print(" X ");
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
     * Méthode vérifiant la possibilité du coup entré par l'utilisateur (ne sort pas du tableau…)
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
     * @param player     Nom du joueur
     */
    void displayResult(int[][] board, int playerTurn, String player, int nbTour) {
        displayMatrice(board);
        System.out.println("##### RESUME DE LA PARTIE #####");
        System.out.print("Le gagnant est : ");

        if (playerTurn == 0) {
            System.out.println(player);
        } else if (playerTurn == 1) {
            System.out.println("Ordinateur");
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
                {0, 2, 0},
                {0, 0, 2}};
        int[][] matrice2 = {{0, 1, 0},
                {0, 2, 0},
                {0, 0, 2}};
        int[] pawnPosition = {0, 1};
        testGameIsDone(matrice, false);
        testGetPawnPosition(matrice, pawnPosition);
        testLegalMove(pawnPosition, 1, 1, true);
        testLegalMove(pawnPosition, 2, 1, false);
        testCreateBoard(4);


        int[][] newMatrice = {{0, 0, 0},
                {0, 2, 0},
                {0, 0, 1}};
        int[][] newMatrice2 = {{0, 0, 0},
                {0, 1, 0},
                {0, 0, 2}};
        int[] coord1 = {0, 1};
        int[] coord2 = {2, 2};
        int[] coord3 = {1, 1};
        testSwitchTwoCase(matrice, coord1, coord2, newMatrice);
        testSwitchTwoCase(matrice2, coord1, coord3, newMatrice2);

        int[] coord4 = {0, 1};
        int[] coord5 = {0, 3};
        int[] coord6 = {2, 2};
        testIsReachableFromPawn(coord4, coord5, true);
        testIsReachableFromPawn(coord4, coord6, false);

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
        System.out.println("legalMove(" + displayTabTest(pawnPosition) + ", " + playerDecision + ", " + nbCase + ") : ");
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

    /**
     * Méthode de test de isReachableFromPawn()
     *
     * @param pawnPosition Coordonnés du pion sur le plateau
     * @param coord        coordonnées à tester
     * @param result       Matrice qui contient le résultat attendu à l'issue de la fonction qui doit être testée
     */
    void testIsReachableFromPawn(int[] pawnPosition, int[] coord, boolean result) {
        System.out.println("isReachableFromPawn(" + displayTabTest(pawnPosition) + ", "
                + displayTabTest(coord) + ", " + result);

        boolean testResult = isReachableFromPawn(pawnPosition, coord);
        if (testResult == result) {
            System.out.println("OK");
        } else {
            System.out.println("ERREUR");
        }
    }
}