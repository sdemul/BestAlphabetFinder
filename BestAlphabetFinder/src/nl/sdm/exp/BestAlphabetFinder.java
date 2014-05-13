package nl.sdm.exp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Sjaak de Mul
 *
 */
public class BestAlphabetFinder {

	private static final int MAX_NUMBER_OF_EVALUATION_LEVELS = 1;

	private String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	private List<String> words = new ArrayList<>();
	private File wordsFile = new File("resources/english_words.txt");
	private Random random = new Random(1);
	
	public static void main(String[] args) {
		
		BestAlphabetFinder bestAlphabetFinder = new BestAlphabetFinder();
		bestAlphabetFinder.start();
		
	}
	
	private void start()  {
		
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(this.wordsFile));
			System.out.println("Number of words: " + bufferedReader.lines().count());
			bufferedReader.close();
			 			
			bufferedReader = new BufferedReader(new FileReader(this.wordsFile));		
			bufferedReader.lines().forEach(s -> this.words.add(s));
			bufferedReader.close();
			
			System.out.println("Number of 'ascending' words: " + this.countAscendingWords(this.alphabet, this.words));
			
			this.determineBestAlphabet(this.alphabet);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private String determineBestAlphabet(String alphabet) {
		
		printScore("Base alphabet: ", alphabet, this.countAscendingWords(alphabet, this.words));
		System.out.println("30 tries:");
		String bestAlphabet = alphabet;
		long bestScore = 0;
		List<String> shuffledAlphabets = this.generateShuffledAlphabets(alphabet, 30);

		for (String startingAlphabet : shuffledAlphabets) {
			Map<String,Long> abcScores = new HashMap<>();
			int tries=0;
			long previousScore=0;
			long sameScoresInARow=1;
			String modifiedAlphabet = startingAlphabet;
			do {
				modifiedAlphabet = this.bestModification(modifiedAlphabet, abcScores.keySet(), 0);
				Long score = this.countAscendingWords(modifiedAlphabet, this.words);
				abcScores.put(modifiedAlphabet, score);
				if (score == previousScore) {
					sameScoresInARow++;
				} else {
					previousScore = score;
				}
				// printScore("After modification: ", modifiedAlphabet, score);
				tries++;
			} while(sameScoresInARow < 4 && tries<50);	
			Map.Entry<String,Long> maxEntry = abcScores.entrySet().stream()
					.max((entry1, entry2) -> Long.compare(entry1.getValue(), entry2.getValue()))
					.get();
			System.out.println("Best alphabet starting with " + startingAlphabet + ": " + maxEntry.getKey() + " Score = " + maxEntry.getValue());
			if (maxEntry.getValue() > bestScore) {
				bestScore = maxEntry.getValue();
				bestAlphabet = maxEntry.getKey();
			}

		}
		printScore("Best alphabet over all starting positions: ", bestAlphabet, this.countAscendingWords(bestAlphabet, this.words));
				
		return bestAlphabet;
	}
	
	private List<String> generateShuffledAlphabets(String baseAlphabet, int numberOfShuffledAlphabets) {
		
		List<String> alternativePositions = new ArrayList<>();
		StringBuffer sb = new StringBuffer(baseAlphabet);
		for (int i=0; i<numberOfShuffledAlphabets; i++) {
			for (int j=0; j<100; j++) {
				int x = Math.abs(random.nextInt() % baseAlphabet.length());
				int y = Math.abs(random.nextInt() % baseAlphabet.length());
				char c = sb.charAt(x);
				sb.deleteCharAt(x);
				sb.insert(y, c);
			}
			alternativePositions.add(sb.toString());				
		}
		return alternativePositions;
	}
	
	private void printScore(String caption, String betterAlphabet, Long score) {
		System.out.println(caption + " " + betterAlphabet 
				+ " Score = " + score 
				);
	}
	
	private String bestModification(String alphabet, Collection<String> toBeIgnored, int level) {
		
		Long maxScore = -1L;
		String bestAlphabet = alphabet;
		for (int i = 0; i < alphabet.length(); i++) {
			for (int j = 0; j < alphabet.length(); j
					++) {	
				if (i != j) {
					String modifiedAbc = this.modify(alphabet, i, j);
					
					// Ignore state that has been already visited
					if (toBeIgnored.contains(modifiedAbc)) {
						continue;
					}

					long score = this.score(modifiedAbc, toBeIgnored, level);
						
					if (score > maxScore) {
						maxScore = score;
						bestAlphabet = modifiedAbc;
					}
				}


			}
		}
		return bestAlphabet;
	}
	
	private String modify (String alphabet, int i, int j) {

		StringBuffer sb = new StringBuffer(alphabet);
		if (i != j) {
			// move
			char c = sb.charAt(i);
			sb.deleteCharAt(i);
			sb.insert(j, c);				
		}
		return sb.toString();
	}
	private long score(String alphabet, Collection<String> toBeIgnored, int level) {
		
		level++;
		if (level < MAX_NUMBER_OF_EVALUATION_LEVELS) {
			long score = this.countAscendingWords(alphabet, this.words);
			return Math.max(score, this.score(this.bestModification(alphabet, toBeIgnored, level), toBeIgnored, level));
		} else {
			return this.countAscendingWords(alphabet, this.words); // * 10000  + this.scoreOccurences(alphabet);
		}

	}
	
	private Long countAscendingWords(String alphabet, List<String> words)  {
		
		return words.stream().filter((String s) -> this.isAscendingWord(alphabet, s)).count();

	}

	private boolean isAscendingWord(String alphabet, String word) { //  String alphabet
		
		int currentIndex = -1;
		int newIndex;
		for (int i = 0; i < word.length(); i++) {
			newIndex = alphabet.indexOf(word.charAt(i));
			if (newIndex < currentIndex) {
				return false;
			} else { 
				currentIndex = newIndex;
			}
		}
		return true;
	}
}
