/**
 * Représentation du jeu de Whythoff jouable dans un terminal en version joueur contre ordinateur
 *
 * @author LEGEAY Quentin
 * @version 1.0
 */
class Main2 {
    void principal() {
        System.out.println("Bienvenue dans le jeu !");
        System.out.println("Règles : ");
        System.out.println("""
                Le pion est placé initialement au hasard sur le plateau. A chaque tour, un joueur a le droit a un
                seul de ces trois mouvements : déplacement d’un nombre quelconque de cases vers la gauche, le
                bas, ou le long d’une diagonale vers la gauche et le bas. Le joueur gagnant est celui qui parvient a
                mettre le pion sur la case inférieure gauche.\n""");

        // Choisir le pseudonyme du joueur
        String player = SimpleInput.getString("Nom du joueur : ");

        // Choisir la taille du plateau
        int boardSize;
        do {
            boardSize = SimpleInput.getInt("Veuillez saisir la taille du plateau de jeu (min. 3, max.99) ");
        } while (boardSize < 3 || boardSize > 99);

        // Créer la matrice de jeu
        int[][] board = createBoard(boardSize);

        gamePvB(board, player, botDifficulty);
    }

    /**
     * Déroule une partie en Joueur contre Ordinateur (ou Player versus Bot (PvB)) du jeu
     *
     * @param board  Matrice de jeu
     * @param player Nom du joueur
     */
    void gamePvB(int[][] board, String player, int botDifficulty) {
        int nbTour = 1;
        boolean winCondition = false;

        String reponseStart;
        do {
            // /!\ Ne gère pas le case sensitive
            reponseStart = SimpleInput.getString("Voulez vous jouer le premier coup ? (Oui/Non)");
        } while (reponseStart != "Oui" && reponseStart != "Non");

        int playerTurn;
        if (reponseStart == "Oui") {
            playerTurn = 0;
        } else {
            playerTurn = 1;
        }

        // Déroulement du jeu
        while (!winCondition) {
            System.out.print("Tour n° " + nbTour + " - ");
            if (playerTurn == 0) {
                System.out.println("c'est à " + player + " de jouer");
            } else {
                System.out.println("c'est à l'ordinateur de jouer");
            }

            // Affiche la position du pion sur le plateau et le plateau
            int[] pawn = getPawnPosition(board);
            displayTab(board);

            // Le cas où c'est au joueur humain de jouer
            if (playerTurn == 0) {

                // Gère les erreurs possibles du joueur lors de ses choix de déplacement.
                boolean isLegal;
                int nbCase;
                int playerDecision;
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
        displayResult(playerTurn, player, nbTour);
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

    void botMove(int[][] tab) {
        int[] pos = getPawnPosition(tab);
        int y = pos[0];
        int x = pos[1];
        int[] newPos = {y, x};


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
     * @param player     Nom du joueur
     */
    void displayResult(int playerTurn, String player, int nbTour) {
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


    // ######### TEST METHODS PART #########

}
