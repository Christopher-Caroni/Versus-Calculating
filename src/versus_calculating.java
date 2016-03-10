import extensions.CSVFile;

class versus_calculating extends Program {
	
	class Case {
		int valeur = 0;
		boolean trouve = false;
		boolean joueur = true; // joueur 1= false  joueur 2= true 
	}
	
	int resultat = 0;
	int scoreJ1 = 0;
	int scoreJ2 = 0;
	boolean joueurCourant = true; // J1 == true; J2 == false;
	

	boolean veutMenu = true, veutScores = false, veutJouer = false, quitter = false;
	long tempsInitiale = 0;
	
	void algorithm() {
		boolean difficulte = false, premiereFoisMenu = true, nouveauJeu = true, dansLesTemps = false;
		Case[] grille = new Case[36];
		int[] lesDes = new int[] {0,0,0};
		String[][] scores10Deb =  new String[10][2];
		String[][] scores10Exp =  new String[10][2];
		initialiserTab(scores10Deb);
		initialiserTab(scores10Exp);
		scores10Deb = importerScores(false);
		scores10Exp = importerScores(true);
		String[] noms = new String[]{"",""};
		
		do {
			if (veutMenu) {
				afficherMenus(premiereFoisMenu);
				premiereFoisMenu = false;
			} 
			if (veutScores) {
				afficherScores(scores10Deb, scores10Exp);
			}
			if (veutJouer) {
				difficulte = readChoixJeu();
				joueurCourant = true;
				do {
					if (nouveauJeu) {
					    scoreJ1=0;
					    scoreJ2=0;
					    noms = new String[]{"",""};
					    initGrille(grille, false);
					    readNoms(noms);
						effacerLignes(16,52, 24, 125);
						imprimerGrille(grille, difficulte, noms);
					}
					nouveauJeu = false; // pour ne pas "raffraichir" l'ecran entre les tours de joueurs. Ceci donne une meilleure impression de continuité
					rollDice(grille, lesDes, difficulte, noms);
					verifDice(lesDes, difficulte, grille); // ici entrer en parametre le nombre de combos minimum possibles qu'on veut
					dansLesTemps = calculatrice(grille, lesDes, difficulte); // fonction calculatrice et verifie si le joueur a depasse 30s
					validerDansGrille(grille, difficulte, noms, dansLesTemps); // valide le nombre dans la grille si pas deja trouve et qu'il a saisi en 30s
				} while(!grillePleine(grille,difficulte, noms, scores10Deb, scores10Exp, false));
				nouveauJeu = true;
			}
		} while (!quitter);
		
	}
	
	// LES GRANDES FONCTIONS DANS L'ORDRE D'APPEL

	void rollDice(Case[] grille, int[] lesDes, boolean difficulte, String[] noms) {
		effacerLignes(25,26, 76, 25);
		cursor(25,59);
		text("yellow");
		if (joueurCourant) {
			println("C'est au tour de");
			cursor(25,76);
			text("cyan");
			println(noms[0]);
		} else {
			println("C'est au tour de");
			cursor(25,76);
			text("purple");
			println(noms[1]);
		}
		text("yellow");
		cursor(27,59);
		println("Appuyez sur entree pour rouler les des");
		cursor(27,71);
		text("green");
		println("entree");	
		text("yellow");	
		cursor(27,98);
		readString();
		effacerLignes(27,28, 59, 90);
		cursor(27,59);
		println("Voici vos des :");
		imprimerCadresDes();
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j<3;j++) {
				lesDes[j] = (int) ((random() * 6)+1); // de 0 a 5 puis de 1 a 6
				cursor(32,71+(14*j));
				text("green");
				println(lesDes[j]);
			}
			delay(75);
		}
		tempsInitiale = getTime();
		text("yellow");
	}

	void verifDice(int[] lesDes, boolean difficulte, Case[] grille) {
		// roule encore les des tant que le nombre d'operations valides est inferieure a 3
		int operationsPossibles = 0;
		int[] combinaison = new int[9];
		int combinaisonsParDifficulte = 0;
		
		for (int i = 0; i<length(combinaison);i++) {
			combinaison[i] = 0;
		}
		if (difficulte) {
			combinaisonsParDifficulte = 8;
		} else {
			combinaisonsParDifficulte = 3;
		}
		
		do {
			operationsPossibles = 0;
			
			for (int j = 0; j<3;j++) {
				lesDes[j] = (int) ((random() * 6)+1); // de 0 a 5 puis de 1 a 6
				cursor(32,71+(14*j));
				text("green");
				println(lesDes[j]);
			}
		
			combinaison[0] = lesDes[0] + lesDes[1] +  lesDes[2];
			combinaison[1] = lesDes[0] - lesDes[1] -  lesDes[2];
			combinaison[2] = lesDes[0] + lesDes[1] -  lesDes[2];
			combinaison[3] = lesDes[0] - lesDes[1] +  lesDes[2];
			combinaison[4] = lesDes[0] + (lesDes[1] *  lesDes[2]);
			combinaison[5] = (lesDes[0] - (lesDes[1]) *  lesDes[2]);
			combinaison[6] = lesDes[0] * lesDes[1] *  lesDes[2];
			combinaison[7] = (lesDes[0] * lesDes[1]) +  lesDes[2];
			combinaison[8] = (lesDes[0] * lesDes[1]) -  lesDes[2];
			
			for (int i = 0;i<=combinaisonsParDifficulte;i++) {
				if ( (((combinaison[i]>0 && combinaison[i]<37) && difficulte) || ((combinaison[i]>0 && combinaison[i]<19) && !difficulte)) && !grille[combinaison[i]-1].trouve) {
					operationsPossibles = operationsPossibles + 1;
				}
			}
			
		} while(operationsPossibles < 1);
		text("yellow");
	}

	boolean calculatrice(Case[] grille, int[] lesDes, boolean difficulte) {
		boolean bonneLongueur = true;
		String leCalcul = ""; // on stocke toute l'operation dans des string pour empecher les erreurs de saisie
		String leCalculOrig = "";
		String[] operationTotal = new String[3+2]; // Ensuite on va stocker les nombres et operateurs dans un tableau
		
		do {
			effacerLignes(39,43, 59, 65);
			bonneLongueur = true;
			leCalcul = "";
			cursor(36,59);
			println("Veuillez faire votre operation en une seule saisie avec ou sans espaces.");
			cursor(37,59);
			println("Exemple : \"3 + 2 - 1\" ");
			cursor(39,59);
			println("Votre operation");
			cursor(39,75);
			text("red");
			println("ici :  ");
			text("yellow");
			cursor(39,81);
			leCalculOrig = readString();
			leCalcul = sansEspaces(leCalculOrig);

			
			if (length(leCalcul) != 5) {
				bonneLongueur = false;
				cursor(41,59);
				println("Vous ne pouvez pas faire cette operation avec ces des");
				cursor(42,59);
				println("Appuyez sur entree pour reessayer");
				text("green");
				cursor(42,71);
				println("entree");
				text("yellow");
				cursor(42,95);
				readString();	
			} else {			
				for (int i = 0; i<5;i++) {
					operationTotal[i] = substring(leCalcul,i,i+1);
				}
			}
			// Si on a PAS bonneLongueur, le while est evalué directement a true, et on execute pas les deux fonctions de validation
		} while (!bonneLongueur || !entreeValide(operationTotal, leCalculOrig, difficulte, false) || !tousDesUtilises(lesDes, operationTotal, false) );

		if (difficulte) {
			return ( getTime() - tempsInitiale < (15*1000));
		}
		return ( getTime() - tempsInitiale < (30*1000));
	}

	boolean tousDesUtilises(int[] lesDes, String[] operationTotal, boolean debug) {
		// On stocke tous l'expression dans un string pour "supprimer" les caracteres utilises
		// Cette fonction supprime les nombres communs entre les operandes entres et les des que l'utilisateur doit utiliser
		// si apres suppresion, il reste des nombres, cest que l'utilisateur n'a pas utilises tous les des
		String tousDes = "", tousOperandes = "";
		tousOperandes = operationTotal[0] + operationTotal[2] + operationTotal[4];
		tousDes = tousDes + lesDes[0] + lesDes[1] + lesDes[2];
		int j = 0;
		
		for (int i =0;i<3;i++) {
			j = 0;
			while (j<length(tousOperandes)) {
				if (charAt(tousDes,0)==charAt(tousOperandes,j) ) {
					tousDes = substring(tousDes,1,length(tousDes));
					tousOperandes = substring(tousOperandes,0,j) + substring(tousOperandes,j+1,length(tousOperandes));
				} else {
					// on increment j seulement si le de nest pas trouve sinon, si elle est trouve et qu'on lincrement
					// on va comparer le de suivant au n+1 operateur, on en a donc sauté une
					j=j+1;
				}
			}
		}
		if (length(tousDes) != 0) { // pour les tests, on n'imprime pas
			if (!debug) {
				cursor(41,59);
				println("Vous n'avez pas utilise tous les des");
				cursor(42,59);
				println("Appuyez sur entree pour reessayer");
				text("green");
				cursor(42,71);
				println("entree");
				text("yellow");
				cursor(42,93);
				readString();
				effacerLignes(41,43,59,90);
			}
			return false;
		}
		return true;
	}

	boolean entreeValide(String[] operationTotal, String leCalculOrig, boolean difficulte, boolean debug) {
		// test si on peut vraiment faire un calcul, et execute le calcul
		// renvoie faux si l'operateur nest pas +/-/* ou si un des operateurs n'est pas un Int
		// ou qu'on est au niveau debutant et l'utilisateur rentre une multiplication
		boolean lesDeuxmulti = false, multiplication1 = false, multiplication2 = false;
		int calcul1 = 0,  total = 0, operande1 =0 , operande2 = 0, operande3 = 0;

		if (sontInt(operationTotal)) {
			// stringToInt ne reussit pas si le caractere est un +/-/* donc il faut d'abord s'assurer qu'ils ne sont pas
			operande1 = stringToInt(operationTotal[0]);
			operande2 = stringToInt(operationTotal[2]);
			operande3 = stringToInt(operationTotal[4]);
		} else {
			if (!debug) {
				cursor(41,59);
				println("Ce n'est pas posible avec vos des");
				cursor(42,59);
				println("Appuyez sur entree pour reessayer");
				cursor(42,71);
				text("green");
				println("entree");	
				text("yellow");	
				cursor(42,93);
				readString(); 
				effacerLignes(41,43,59,90);
			}
			return false;
		}
		
		if (!difficulte && (equals(operationTotal[1], "*") || equals(operationTotal[3], "*")) ) {
			if (!debug) {
				cursor(41,59);
				println("Vous ne pouvez pas utiliser les multiplications dans ce niveau");
				cursor(42,59);
				println("Appuyez sur entree pour reessayer");
				cursor(42,71);
				text("green");					
				println("entree");	
				text("yellow");	
				cursor(42,95);
				readString(); 
				effacerLignes(41,43,59,90);
			}
			return false;
		}
		
		if ( equals(operationTotal[1], "*") && equals(operationTotal[3], "*") && difficulte) {
			calcul1 = operande1 * operande2;
			total = calcul1 * operande3;
			lesDeuxmulti = true;
		} else if (equals(operationTotal[1], "*") && difficulte) {
			calcul1 = operande1 * operande2;
			multiplication1 = true;
		} else if (equals(operationTotal[3], "*") && difficulte) {
			calcul1 = operande2 * operande3;
			multiplication2 = true;
		}
		
		if (multiplication1 || multiplication2) {
			if (equals(operationTotal[1], "+")) {
				total = operande1 + calcul1;
			} else if (equals(operationTotal[1], "-")) {
				total = operande1 - calcul1;
			} else if (!multiplication1 && !multiplication2 && !lesDeuxmulti) {
				if (!debug) {
					cursor(41,59);
					println("Votre premier operateur n'existe pas");
					cursor(42,59);
					println("Appuyez sur entree pour reessayer");
					cursor(42,71);
					text("green");
					println("entree");	
					text("yellow");	
					cursor(42,95);
					readString(); 
					effacerLignes(41,43,59,90);
				}
				return false;
			}
			
		} else {	
			if (equals(operationTotal[1], "+")) {
				calcul1 = operande1 + operande2;
			} else if (equals(operationTotal[1], "-")) {
				calcul1 = operande1 - operande2;
			} else if (!multiplication1 && !multiplication2 && !lesDeuxmulti) {
				if (!debug) {
					cursor(41,59);
					println("Votre premier operateur n'existe pas");
					cursor(42,59);
					println("Appuyez sur entree pour reessayer");
					cursor(42,71);
					text("green");
					println("entree");	
					text("yellow");				
					readString();
					cursor(42,95);
					effacerLignes(41,43,59,90);
				}
				return false;
			}
		}
		
		if (equals(operationTotal[3], "+")) {
			total = calcul1 + operande3;
		} else if (equals(operationTotal[3], "-")) {
			total = calcul1 - operande3;
		} else if (!multiplication1 && !multiplication2 && !lesDeuxmulti) {
			if (!debug) {
				cursor(41,59);
				println("Votre deuxieme operateur n'existe pas");
				cursor(42,59);
				println("Appuyez sur entree pour reessayer");
				cursor(42,71);
				text("green");
				println("entree");	
				text("yellow");	
				cursor(42,95);
				readString();
				effacerLignes(41,43,59,90);
			}
			return false;
		}
		
		if (!debug) {
			cursor(41,59);
			println("Votre operation est donc " + leCalculOrig + " = " + total);
			resultat = total;
		}
		return true;
	}

	void validerDansGrille(Case[] grille, boolean difficulte, String[] noms, boolean dansLesTemps) {
		cursor(43,59);
		if ( (dansLesTemps && resultat>0 && resultat<37 && difficulte) || (dansLesTemps && resultat>0 && resultat<19 && !difficulte)) {
			if (!grille[resultat-1].trouve) {
				grille[resultat-1].trouve = true;
				if (joueurCourant) {
					scoreJ1 = scoreJ1 + 1;
					grille[resultat-1].joueur = true;
				} else {
					scoreJ2 = scoreJ2 + 1;
					grille[resultat-1].joueur = false;
				}
			} else {
				println("Desole, ce nombre a deja ete trouve");
			}
		} else if (!dansLesTemps) {
			println("Desole, vous avez pris trop de temps a rentrer votre expression");
		} else if ( ((resultat<1 || resultat>36) && difficulte) || ((resultat<1 || resultat>18) && !difficulte) ) {
			println("Desole, ce nombre n'est pas dans la grille");
		}
		
		imprimerGrille(grille, difficulte, noms);
		joueurCourant = !joueurCourant;
		
		cursor(45,59);
		println("Appuyez sur entree pour passer au joueur suivant ");
		cursor(45,71);
		text("green");
		println("entree");	
		text("yellow");	
		cursor(45,108);
		readString();
		effacerLignes(27,50, 40, 110);
	}

	boolean grillePleine(Case[] grille, boolean difficile, String[] noms, String[][] scores10Deb, String[][] scores10Exp, boolean debug) {
		int i = 0;
		if (difficile) {
			while (i<=length(grille)-1) {
				if (!grille[i].trouve) {
					return false;
				}
				i = i + 1;
			}
			// si on joue sur la petite grille, le jeu est fini lorsque seulement les 18 premiere cases (0 a 17) sont remplies
			// sinon, le fait que les cases de 18 a 35 ne sont pas decouverts pourrait faire renvoyer faux, et donc le jeu serait interminable
		} else {
			while (i<18) {
				if (!grille[i].trouve) {
					return false;
				}
				i = i + 1;
			}
		}
		
		if (!debug) {
			// dans le debuggage on n'execute pas le nouveau classement sinon il faudrait initialiser des scores pour les tests
			if (difficile)  {
				scores10Exp = nouveauxClassements(scores10Exp, noms, difficile);
			} else {
				scores10Deb = nouveauxClassements(scores10Deb, noms, difficile);
			}
			
			effacerLignes(25,50, 40, 90);
			effacerLignes(16,40, 28, 28);
			cursor(27,59);
			if (scoreJ1>scoreJ2) {
				println("Bravo ! Vous avez gagne M. " + noms[0]);
				text("cyan");
				cursor(27,86);
				println(noms[0]);
			} else if (scoreJ2>scoreJ1) {
				println("Bravo ! Vous avez gagne M. " + noms[1]);
				text("purple");
				cursor(27,86);
				println(noms[1]);
			} else if (scoreJ1==scoreJ2) {
				println("Vous etes a egalite !");
			}
			text("yellow");
			cursor(28,59);
			println("Les scores sont les suivants :");
			cursor(30,59);
			text("cyan");
			println(noms[0] + " : " + scoreJ1);
			cursor(32,59);
			text("purple");
			println(noms[1] + " : " + scoreJ2);
			text("yellow");
			
			cursor(34,59);
			lesChoix("Menu", "classements", "Quitter");
		}
		return true;
	}

	String[][] nouveauxClassements(String[][] initialScores, String[] noms, boolean difficulte) {
		int[] nouveauxValsScores = new int[12];

		// on stocke les valeurs des scores dans un tableau d'entier pour les trier
		for (int ligne = 0; ligne<length(initialScores,1);ligne++) {
			nouveauxValsScores[ligne] = stringToInt(initialScores[ligne][1]);
		}
		
		// les nouveux scores du jeu qui vient de terminer sont ajoutes a la fin
		nouveauxValsScores[10] = scoreJ1;
		nouveauxValsScores[11] = scoreJ2;
		
		// scores12 correspond a scores10 mais avec les noms et les scores des deux jouers du jeu qui vient de terminer
		String[][] scores12 = new String[12][2];
		scores12 = initialiserTab(scores12);
		echangerTab(initialScores,scores12); // les 10 prem lignes de scores10 sont stockes dans scores12
		
		// les nvx noms et scores sont stockes dans scores12
		scores12[10][0] = noms[0];
		scores12[10][1] = scores12[10][1] + scoreJ1;
		scores12[11][0] = noms[1];
		scores12[11][1] = scores12[11][1] + scoreJ2;

		int temp = 0, j = 0;
		String tempScoreString = "", tempNom = "";
		// on trie les scores, par rapport au tableau d'entiers, les scores du tableau d'entiers ET du tableau scores12
		for (int i = 0; i<length(nouveauxValsScores);i++) {
			j = i;
			while (j>0 && nouveauxValsScores[j]>nouveauxValsScores[j-1]) {

				tempNom = scores12[j-1][0];
				tempScoreString = scores12[j-1][1];
				temp = nouveauxValsScores[j-1];

				scores12[j-1][0] = scores12[j][0];
				scores12[j-1][1] = scores12[j][1];
				nouveauxValsScores[j-1] = nouveauxValsScores[j];

				scores12[j][0] = tempNom;
				scores12[j][1] = tempScoreString;
				nouveauxValsScores[j] = temp;
				
				j = j-1;	
			}
		}
		
		// une fois le scores12 est trie, on a donc integre les nvx scores dans le bon ordre. On peut maintenant stocker les 10 meilleurs joueurs dans le tableau initial
		echangerTab(scores12, initialScores); 
		if (difficulte) {
			saveCSV(initialScores,"../ressources/scoresExpert.csv");
		} else {
			saveCSV(initialScores,"../ressources/scoresDebutant.csv");
		}
		return initialScores;
	}
	
	// LES PETITES FONCTIONS
	// D'IMPRIMERIE ET VERIFICATIONS SIMPLES

	void readNoms(String[] leursNoms) {
		imprimerContenu();
		effacerLignes(41,52, 40, 100);
		cursor(41,74);
		text("red");
		println("Veuillez entrer vos noms :");
		cursor(43,40);
		text("yellow");
		println("Le nom doit avoir entre 1 et 24 caracteres et les deux joueurs doivent avoir des noms differents");
		cursor(47,55);
		println("Joueur 2 :");
		
		do {
			effacerLignes(45,47, 67, 60);
			cursor(45,55);
			text("green");
			println("Joueur 1 :");
			cursor(45,67);
			text("yellow");
			leursNoms[0] = readString();
		} while ( (length(leursNoms[0]))>24 || (length(leursNoms[0]))<1);
		
		cursor(45,55);
		text("yellow");
		println("Joueur 1 :");
		
		do {
			effacerLignes(47,48, 67, 60);
			cursor(47,55);
			text("green");
			println("Joueur 2 :");
			cursor(47,67);
			text("yellow");
			leursNoms[1] = readString();
		} while ( (length(leursNoms[1]))>24 || (length(leursNoms[1]))<1 || leursNoms[0].equalsIgnoreCase(leursNoms[1]) );
		
		cursor(47,55);
		text("yellow");
		println("Joueur 2 :");
		
		cursor(50,55);
		text("yellow");
		print("Appuyez maintenant sur        pour commencer le jeu");
		cursor(50,78);
		text("green");
		print("entree");
		text("yellow");
		cursor(50,107);
		readString();
	}
	
	void imprimerGrille(Case[] grille, boolean taille, String[] noms) {
		int colonneCase = 0;
		int width = 0, height = 0;
		boolean verticalLine = true;
		boolean horizontalLine = true;
		int ligneScore = 0;
		
		if (taille) {
			width = 19;
			height = 9;
			ligneScore = 17;
		} else {
			width = 13;
			height = 7;
			ligneScore = 16;
		}
		
		for (int ligne = 0; ligne<height;ligne++) {
			if (taille) {
				cursor(ligne+15,59);
			} else {
				cursor(ligne+15,70);
			}
			for (int colonne = 0; colonne<width;colonne++) {
				text("yellow");
				if (horizontalLine) { // print line
					if (verticalLine) {
						print("+");
					} else {
						print("-----");
					}
				} else { // print numbers
					if (verticalLine) {
						print("|");
					} else {
						if (grille[colonneCase].trouve) {
							if (grille[colonneCase].joueur) {
								text("cyan");
							} else {
								text("purple");
							}
							if (colonneCase<9) {
								print("  " + grille[colonneCase].valeur + "  ");
							} else {
								print("  " + grille[colonneCase].valeur + " ");
							}
						} else {
							print("     ");
						}
						colonneCase = colonneCase + 1;
					}
				}
				verticalLine = !verticalLine;
			}
			verticalLine = true;
			println();
			horizontalLine = !horizontalLine;
		}
		
		cursor(ligneScore,28);
		println("Scores :");
		cursor(ligneScore+2,28);
		text("cyan");
		println(noms[0] + " : " + scoreJ1);
		cursor(ligneScore+4,28);
		text("purple");
		println(noms[1] + " : " + scoreJ2);
		text("yellow");
	}
	
	String[][] importerScores(boolean expert) {
		CSVFile scoresCSVExp = loadCSV("../ressources/scoresExpert.csv");
		CSVFile scoresCSVDeb = loadCSV("../ressources/scoresDebutant.csv");
		String[][] scores = new String[10][2];
		if (expert) {
			for (int i = 0; i<length(scores,1);i++) {
				for (int j = 0;j<length(scores,2);j++) {
					scores[i][j] = getCell(scoresCSVExp,i,j);
				}
			}
		} else {
			for (int i = 0; i<length(scores,1);i++) {
				for (int j = 0;j<length(scores,2);j++) {
					scores[i][j] = getCell(scoresCSVDeb,i,j);
				}
			}
		}
		return scores;
	}
	
	boolean sontInt(String[] operation) {
		// Le boolean estInt faux de base. Si à un moment, l'operande en evaluation est égale à un string de valeur entre "1" et "6", il est mis a VRAI
		// Sinon, cela veut dire qu'a AUCUN moment il n'est egale à un nombre. On return directement false car on peut pas faire le calcul
		// Si on arrive a la fin de cette fonction, cela veut dire qu'aucune operande n'a ete FAUX. On return donc true
		String compInt = "";
		boolean estInt = false;
		for (int i = 0;i<5;i=i+2) { // increment de 2 car ce sont la ou se trouvent les nombres
			estInt = false;
			for (int compNombre = 1; compNombre<7; compNombre++) {
				compInt = "";
				compInt = compInt + compNombre;
				if (equals(operation[i],compInt)) {
					estInt = true;
				}
			}
			// on teste pour CHAQUE operande s'il etait un nombre entre 1 et 6
			if (!estInt) {
				return false;
			}
		}
		return true;
	}
	
	String sansEspaces(String mot) {
		String nouveauCalcul = "";
		for (int i = 0; i<length(mot);i++) {
			if ( !equals(substring(mot,i,i+1)," ") ) {
				nouveauCalcul = nouveauCalcul + charAt(mot,i);
			}
		}
		return nouveauCalcul;
	}

	void initGrille(Case[] grille, boolean debug) {
		for (int colonne = 0; colonne<length(grille);colonne++) {
			grille[colonne] = new Case();
			grille[colonne].valeur = colonne+1;
			grille[colonne].trouve = debug;
		}
	}

	String[][] initialiserTab(String[][] tab) {
		for (int i = 0; i<length(tab,1);i++) {
			for (int j = 0; j<length(tab,2);j++) {
				tab[i][j] = "";
			}
		}
		return tab;
	}

	void echangerTab(String[][] source, String[][] destination) {
		// echangerTab stocke les 10 premieres lignes de la source dans destination
		for (int ligne = 0; ligne<10;ligne++) {
			for (int colonne = 0 ; colonne<2;colonne++) {
				destination[ligne][colonne] = source[ligne][colonne];
			}
		}
	}

	void effacerLignes(int ligneDebut, int ligneFin,int colonneDebut, int cbColonnes) {
		for (int i = 0; i<ligneFin-ligneDebut;i++) {
			cursor(i+ligneDebut,colonneDebut);
			for (int j = 0; j<cbColonnes;j++) {
				print(" ");
			}
		}
	}

	void imprimerCadresDes() { // on imprime 3 cadres
		boolean etoile = true;
		text("green");
		for (int fois = 0; fois<3;fois++) {
			for (int ligne = 0; ligne<5;ligne++) {
				cursor(30+ligne,67+(fois*10)+fois*4);
				etoile = true;
				for (int colonne = 0; colonne<9;colonne++) {
					if ((ligne == 0 || ligne==4) && etoile) {
						print("*");
						etoile = !etoile;
					} else if (colonne==0 || colonne==8) {
						print("*");
					}else {
						print(" ");
						etoile = !etoile;
					}
				}
				println();
			}
		}
		text("yellow");
	}

	void imprimerCadre(int coinLigne, int coinColonne, int width, int height) {
		text("yellow");
		boolean verticalLine = false;
		for (int ligne = 0; ligne<height;ligne++) {
			cursor(coinLigne+ligne,coinColonne);
			verticalLine = false;
			for (int colonne = 0; colonne<width;colonne++) {
				if (colonne == 0 || colonne == width-1) {
					print("*");
				} else if (ligne == 0 || ligne == height-1){
					if (verticalLine) {
						print("*");
					} else {
						print(" ");
					}
					verticalLine = !verticalLine;
				} else {
					print(" ");
				}
			}
			println();
		}
	}

	void afficherMenus(boolean premiereFoisMenu){// Menu du jeu 
		String choix = "";
		effacerLignes(15,42, 24, 129);
		if (premiereFoisMenu) {
			clearScreen();
			imprimerCadre(5,23,131,50);
			imprimerTitre();
		}
		imprimerContenu();
		lesChoix("Jouer", "classements", "Quitter");
	}

	void afficherScores(String[][] scores10Deb, String[][] scores10Exp) {
		effacerLignes(15,42, 24, 129);
		cursor(15,80);
		text("cyan");
		println("Les classements :");
		text("yellow");
		cursor(18,50);
		println("Niveau Debutant");
		cursor(18,110);
		println("Niveau Expert");
		
		for (int ligne = 0; ligne<length(scores10Deb,1);ligne++) {
			for (int colonne = 0;colonne<length(scores10Deb,2);colonne++) {
				cursor(20+(ligne*2),35+(colonne*40));
				println(scores10Deb[ligne][colonne]);
			}
			println();
		}
		
		for (int ligne = 0; ligne<length(scores10Exp,1);ligne++) {
			for (int colonne = 0;colonne<length(scores10Exp,2);colonne++) {
				cursor(20+(ligne*2),95+(colonne*40));
				println(scores10Exp[ligne][colonne]);
			}
			println();
		}
		
		lesChoix("Menu","classements","Quitter");
	}
	
	boolean readChoixJeu() {
		String choix = "";
		effacerLignes(43,50, 40, 100);
		do {
			effacerLignes(45,47, 58, 60);
			cursor(41,72);
			text("red");
			println("Choississez votre difficulte :");
			cursor(43,41);
			text("yellow");
			println("Tapez un des choix de couleur");
			cursor(43,71);
			text("green");
			println("verte");
			cursor(32,64);
			println("1 : Debutant 			2 : Expert");
			cursor(34,43);
			println("Debutant");
			cursor(36,43);
			println("Expert");
			cursor(45,41);
			text("yellow");
			print("Saississez ici : ");
			cursor(45,52);
			text("red");
			print("ici");
			text("yellow");
			cursor(45,58);
			choix = readString();
		} while( !equals(choix,"1") && !equals(choix,"2") && !equals(choix,"debutant") && !equals(choix,"expert") );
		return (equals(choix,"2") || choix.equalsIgnoreCase("expert") );
	}
	
	void imprimerContenu() {
		cursor(16,80);
		text("cyan");
		println("Principe du jeu :");
		text("yellow");
		cursor(18,38);
		println("Le jeu est compose d'une grille de nombres allant de 1 a 18 ou 36. Il faut le remplir en roulant des  ");
		cursor(20,40);
		println("des et en ecrivant une expression mathematique permettant de retrouver un des nombres du tableau.");
		cursor(22,38);
		println("Vous devez retrouver un nombre qui n'est pas encore trouve et chaque nombre rapporte un point au joueur.");
		cursor(24,34);
		println("Attention : Vous devez utiliser TOUS les des et saisir votre calcul en moins de 15/30 secondes selon la difficulte");
		cursor(24,34);
		text("red");
		println("Attention");
		cursor(24,66);
		println("TOUS");
		cursor(24,105);
		println("moins");
		text("yellow");
		cursor(26,80);
		println("Bonne chance !");
		cursor(30,80);
		text("cyan");
		println("Les niveaux :");
		text("yellow");
		cursor(32,64);
		println("1 : Debutant 			2 : Expert");
		cursor(34,41);
		println("- Debutant : tableau de 1 a 18 avec des additions et soustractions");
		cursor(36,41);
		println("- Expert : tableau de 1 a 36 avec des additions, soustractions ET des multiplcations !");

	}

	void lesChoix(String choix1, String choix2, String choix3) {
		String choix = "";
		veutJouer = false;
		veutMenu = false;
		veutScores = false;
		
		effacerLignes(43,44, 55, 10);
		cursor(41,75);
		text("red");
		println("Que voulez-vous faire ?");
		text("yellow");
		cursor(43,76);
		text("green");
		println("2            " + choix2 + "                   3   " + choix3);
		cursor(43,49);
		println("1   "+ choix1);
		cursor(43,41);
		text("yellow");
		println("Choix N.");
		cursor(43,51);
		println(":");
		cursor(43,68);
		println("Choix N.");
		cursor(43,78);
		println(": Voir les");
		cursor(43,111);
		println("Choix N.");
		cursor(43,121);
		println(":");
		cursor(45,41);
		println("Tapez un des choix de couleur");
		cursor(45,71);
		text("green");
		println("verte");
		do {
			effacerLignes(47,48, 58, 60);
			text("yellow");
			cursor(47,41);
			print("Saississez ici : ");
			text("red");
			cursor(47,52);
			print("ici");
			text("yellow");
			cursor(47,58);
			choix = readString();
		} while( !equals(choix,"1") && !equals(choix,"2") && !equals(choix,"3")&& !choix.equalsIgnoreCase(choix1) && !choix.equalsIgnoreCase(choix2) && !choix.equalsIgnoreCase(choix3));
		
		if (choix1.equalsIgnoreCase("menu") && (choix.equalsIgnoreCase(choix1) || equals(choix,"1")) ) {
			veutMenu = true;
		} else if (choix.equalsIgnoreCase(choix1) || equals(choix,"1")) {
			veutJouer = true;
		} else if (choix.equalsIgnoreCase(choix2) || equals(choix,"2")) {
			veutScores = true;
		} else if (choix.equalsIgnoreCase(choix3) || equals(choix,"3")) {
			quitter = true;
		}
	}
	
	void imprimerTitre() {
		imprimerCadre(5,44,89,9);
		text("purple");
		cursor(6,48);
		println("                                   ___      _            _       _   _             ");
		cursor(7,48);
		println(" /\\   /\\___ _ __ ___ _   _ ___    / __\\__ _| | ___ _   _| | __ _| |_(_)_ __   __ _ ");
		cursor(8,48);
		println(" \\ \\ / / _ \\ '__/ __| | | / __|  / /  / _` | |/ __| | | | |/ _` | __| | '_ \\ / _` |");
		cursor(9,48);
		println("  \\ V /  __/ |  \\__ \\ |_| \\__ \\ / /__| (_| | | (__| |_| | | (_| | |_| | | | | (_| |");
		cursor(10,48);
		println("   \\_/ \\___|_|  |___/\\__,_|___/ \\____/\\__,_|_|\\___|\\__,_|_|\\__,_|\\__|_|_| |_|\\__, |");
		cursor(11,48);
		println("                                                                             |___/ ");
	}
	
	
	// Pour tous les tests, on appelle les fonctions avec en dernier parametre des booleans "debug" de valeur true
	// dans ces fonctions, si debug=FALSE, on n'imprime rien et on ne modifie pas le curseur
	
	void testSansEspaces() {
		assertEquals("abcdefg", sansEspaces("ab     cd ef       g"));
		assertEquals("bonjour", sansEspaces("     bon   jour   "));
	}
	
	void testTousDesUtilises() {
		int[] dice = new int[]{3,2,1};
		String[] calcul = new String[]{"3","+","1","-","2"};
		assertTrue(tousDesUtilises(dice, calcul, true));
		// le calcul utilise bien tous les des
		
		int[] dice2 = new int[]{3,2,1};
		String[] calcul2 = new String[]{"1","*","3","-","2"};
		assertTrue(tousDesUtilises(dice2, calcul2, true));
		
		int[] dice3 = new int[]{6,1,5};
		String[] calcul3 = new String[]{"5","+","1","-","2"};
		assertFalse(tousDesUtilises(dice3, calcul3, true));
		// le calcul n'utilise pas tous les des
		
		int[] dice4 = new int[]{3,5,1};
		String[] calcul4 = new String[]{"3","*","5","-","2"};
		assertFalse(tousDesUtilises(dice4, calcul4, true));
		// le calcul n'utilise pas tous les des
	}
	
	void testEntreeValide() {
		// test si la fonction accepte ou pas une lettre a la place d'une operande ou a la place d'un operateur
		String[] unCalcul = new String[]{"3","+","1","-","2"};
		assertTrue(entreeValide(unCalcul, "3+1-2", false, true));
		
		String[] unCalcul2 = new String[]{"1","*","3","-","2"};
		assertTrue(entreeValide(unCalcul2,"1*3-2", true, true) );
		
		String[] unCalcul3 = new String[]{"1","*","3","-","2"};
		assertFalse(entreeValide(unCalcul3,"1*3-2", false, true) );
		// false = niveau debutant, on ne peut pas utiliser des multiplications
		
		String[] unCalcul4 = new String[]{"5","5","1","-","2"};
		assertFalse(entreeValide(unCalcul4,"551-2",true, true) );
		// un "nombre" a la place d'un operateur
		
		String[] unCalcul5 = new String[]{"3","g","5","-","2"};
		assertFalse(entreeValide(unCalcul5,"3g5-2",true, true) );
		// une lettre a la place d'un operateur
		
		String[] unCalcul6 = new String[]{"h","-","g","-","2"};
		assertFalse(entreeValide(unCalcul6,"h-g-2",true, true) );
		// des lettres a la place d'operandes
		
		String[] unCalcul7 = new String[]{"h","f","g","-","2"};
		assertFalse(entreeValide(unCalcul7,"hfg-2",true, true) );
		// des lettre aux places des operateurs et operandes en meme temps
	}
	
	void testGrillePleine() {
		Case[] grilleTest = new Case[36];
		String[] nomsTest = new String[]{"nom1","nom2"};
		String[][] scoresTest = new String[10][2];
		initialiserTab(scoresTest);
		initGrille(grilleTest, true); //  true = on met grilleTest[x].trouve = true
		assertTrue(grillePleine(grilleTest, true, nomsTest, scoresTest, scoresTest, true) );
		
		Case[] grilleTest2 = new Case[36];
		initGrille(grilleTest2, true);
		grilleTest2[5].trouve = false;
		assertFalse(grillePleine(grilleTest2, true, nomsTest, scoresTest, scoresTest, true));
		// seulement las case 5 nest pas trouve, la grille nest donc pas pleine
		
		Case[] grilleTest3 = new Case[36];
		initGrille(grilleTest3, true);
		grilleTest3[20].trouve = false;
		assertTrue(grillePleine(grilleTest3, false, nomsTest, scoresTest, scoresTest, true));
		// false = niveau debutant, grillePleine est suppose verifier que les 18 premieres cases
		// meme avec la case 20 non trouve, la grille est pleine car on joue avec la grille de 1 a 18
	}
}
