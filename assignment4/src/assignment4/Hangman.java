package assignment4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

//Author: Andrew Johnson
//Date/Version: February 13, 2023
/*
* This is a Hangman game that uses the official Scrabble dictionary to pull
* words from as the mystery word.
* Please be warned that the Scrabble dictionary contains many racial slurs 
* and offensive words. Sorry about that.
* 
*/

public class Hangman {

	public static void main(String[] args) {
		/*
		 * the guessAlphabet is an int array that represents the count of each letter in
		 * the word for scoring and also allows guesses to be checked against it to rule
		 * out duplicate guesses.
		 */
		int[] guessAlphabet = new int[26];
		Scanner input = new Scanner(System.in);
		int badGuesses = 0;
		int goodGuesses = 0;
		String playerName = "";
		int score = 0;
		char guess = '.';
		ArrayList<HighScore> topScores = new ArrayList<HighScore>();
		/*
		 * game starts by picking a random word
		 */
		String word = "";
		try {
			word = wordFromDictionary("res/dictionary.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		/*
		 * when a letter is guessed the display updates
		 */
		while (badGuesses < 6) {
			// System.out.println("score: " + score); // this is for bug testing
			// System.out.println(word); // this is for bug testing
			boolean badInput = true;
			/*
			 * this prints the underscores and the letters of the word that have been
			 * correctly guessed.
			 */
			System.out.print("Hidden Word: ");
			for (int i = 0; i < word.length(); i++) {
				boolean printUnderscore = true;
				for (int j = 0; j < guessAlphabet.length; j++) {
					if (word.charAt(i) - 'a' == j && guessAlphabet[j] > 0) {
						System.out.print(word.charAt(i) + " ");
						printUnderscore = false;
					}
				}
				if (printUnderscore) {
					System.out.print("_ ");
				}
			}
			System.out.println();
			// this prints the list of already guessed letters.
			System.out.print("Incorrect Guesses: ");
			for (int i = 0; i < guessAlphabet.length; i++) {
				if (!word.contains(Character.toString((char) ('a' + i))) && guessAlphabet[i] > 0) {
					System.out.print((char) ('A' + i));
				}
			}
			System.out.println();
			System.out.println("Guesses Left: " + (6 - badGuesses));
			// this loop asks for input from the player.
			while (badInput) {
				try {
					System.out.println("Enter next guess: ");
					guess = input.next().charAt(0);
					if (guessAlphabet[guess - 'a'] == 0) {
						guessAlphabet[guess - 'a']++;
						badInput = false;
					} else {
						System.out.println("You already guessed that, try a different letter.");
					}
				} catch (Exception e) {
					System.out.println(e);
					input.nextLine();
				}
			}
			// here the end-game conditions are checked and score is handed out.
			if (word.contains(Character.toString(guess))) {
				for (int i = 0; i < word.length(); i++) {
					if (word.charAt(i) == guess) {
						guessAlphabet[guess - 'a']++;
						goodGuesses++;
						score += 10;
					}
				}
				if (goodGuesses == word.length()) {
					score += 100;
					score += 30 * (6 - badGuesses);
					// reset the guesses
					badGuesses = 0;
					goodGuesses = 0;
					for (int i = 0; i < guessAlphabet.length; i++) {
						guessAlphabet[i] = 0;
					}
					try {
						word = wordFromDictionary("res/dictionary.txt");
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						badGuesses = 99;
					}
				}
			} else {
				badGuesses++;
			}
		}
		if (badGuesses == 6) {
			System.out.println("GAME OVER");
			System.out.println("your score was : " + score);
		} else if (badGuesses == 99) {
			System.out.println("The game could not find the dictionary file.\n"
					+ "Make sure that the dictionary.txt file is located in the\n"
					+ "\"res\" folder inside of the Hangman project.");
		}

		try {
			readScores(topScores, "res/highScoreList.txt");
			Collections.sort(topScores, Collections.reverseOrder());
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		if (topScores.size() > 4) {
			if (score > topScores.get(4).getScore()) {
				System.out.println("Your score is in the top 5 all-time scores.");
				try {
					System.out.println("please enter your name :");
					playerName = input.next();
					topScores.add(new HighScore(playerName, score));
					Collections.sort(topScores, Collections.reverseOrder());
				} catch (Exception e) {
					System.out.println(e);
				}
				try {
					writeScores(topScores, "res/highScoreList.txt");
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		} else if (topScores.size() >= 0 && topScores.size() <= 4) {
			System.out.println("Your score is in the top 5 all-time scores.");
			try {
				System.out.println("please enter your name :");
				playerName = input.next();
				topScores.add(new HighScore(playerName, score));
				Collections.sort(topScores, Collections.reverseOrder());
			} catch (Exception e) {
				System.out.println(e);
			}
			try {
				writeScores(topScores, "res/highScoreList.txt");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		input.close();
		System.out.printf("%5S %10S %10S\n", "PLACE", "NAME", "SCORE");
		for (int i = 0; i < topScores.size(); i++) {
			int place = 1 + i;
			String name = topScores.get(i).getName();
			int endScore = topScores.get(i).getScore();
			System.out.printf("%5d : %10s %10d\n", place, name, endScore);
		}

	}

	/*
	 * the idea with this method is to grab text from a file and to populate an
	 * ArrayList with the scores so that the scores can be sorted to put the highest
	 * score at the top.
	 */
	public static ArrayList<HighScore> readScores(ArrayList<HighScore> scores, String filename)
			throws FileNotFoundException {
		Scanner fileIn = new Scanner(new File(filename));
		String name = "";
		int score = 0;
		while (fileIn.hasNext()) {
			name = fileIn.nextLine();
			score = Integer.parseInt(fileIn.nextLine());
			scores.add(new HighScore(name, score));
		}
		fileIn.close();
		return scores;
	}

	/*
	 * Once the game finishes this method should be used to save the ArrayList of
	 * scores into a text file that can be used for the next round of Hangman.
	 */
	public static void writeScores(ArrayList<HighScore> scores, String filename) throws FileNotFoundException {
		PrintWriter fileOut = new PrintWriter(new File(filename));
		for (int i = 0; i < scores.size(); i++) {
			fileOut.print(scores.get(i).toString() + "\n");
		}
		fileOut.close();
	}

	/*
	 * this method is supposed to grab a random word from the
	 * assignment4/res/dictionary.txt file.
	 */
	public static String wordFromDictionary(String filename) throws FileNotFoundException {
		int randLine = (int) (Math.random() * 127143);
		File file = new File(filename);
		Scanner fileIn = new Scanner(file);
		int counter = 0;
		String contents = "";
		while (counter < randLine) {
			contents = fileIn.nextLine();
			counter++;
		}
		fileIn.close();
		return contents;
	}
}

class HighScore implements Comparable<HighScore> {
	private String name;
	private int score;

	public HighScore(String name, int score) {
		this.name = name;
		this.score = score;
	}

	public int getScore() {
		return score;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name + "\n" + score;
	}

	@Override
	public int compareTo(HighScore o) {
		if (score > o.score) {
			return 1;
		} else if (score < o.score) {
			return -1;
		} else {
			return 0;
		}
	}
}
