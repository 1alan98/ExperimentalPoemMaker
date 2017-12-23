import java.io.*;
import java.util.*;
import datamuse.*;

// Produces experimental poems with the words found in the given text file. 
// The poems have two stanzas, 8 lines, an abab rhyhiming pattern, and 
// lines alternate between 8 and 6 syllables

public class ExperimentalPoemMaker {

	ArrayList<String> dictionary;
	DatamuseQuery wordData;

	public ExperimentalPoemMaker() throws FileNotFoundException {
		wordData = new DatamuseQuery();
		Scanner input = new Scanner(System.in);
		System.out.print("Enter file name: ");
		File poemsFile = new File(input.next());
		// Runs until user gives a valid file name
		while (!poemsFile.isFile()) { 
			System.out.println(poemsFile.getName() + " is not a valid filename.");
			System.out.print("Enter file name: ");
			poemsFile = new File(input.next());
		}
		Scanner text = new Scanner(poemsFile);
		// a dictionary with the words found in poemsFile is made 
		dictionary = getWords(text);
	}

	// Returns an ArrayList of the words found in the given text
	// If a word is found multiple times in the given text,
	// then the returned list will contain the same number of 
	// instances of the said word
	public ArrayList<String> getWords(Scanner text) {
		ArrayList<String> dictionary = new ArrayList<String>();
		while (text.hasNext()) {
			dictionary.add(text.next());
		}
		return dictionary;
	}

	// Makes and prints an 8 line poem made with the given dictionary
	// The poem has 2 stanzas, an abab rhyming pattern, and alternates
	// between 8 and 6 syllables
	public void printPoem() {
		Random r = new Random();
		// Used to find the # of syllables in words and if words rhyme
		// the last words in each line
		Stack<String> lastWords = new Stack<String>();
		for (int i = 0; i < 8; i++) {
			// Number of syllables alternates between 8 and 6 for each line
			int syllables = 8 - i % 2 * 2;
			String line = "";
			// runs until the syllable max is met for the current line
			while (syllables > 0) {
				int curSyllables = 0;
				int wordIndex = 0;
				boolean wordFound = false;
				// runs until it finds a word whose syllables can fit into the line
				while (!wordFound) {
					// index for the randomly chosen word
					wordIndex = r.nextInt(dictionary.size());
					try {
						curSyllables = Integer.parseInt(wordData.syllables(dictionary.get(wordIndex)));
						wordFound = syllables - curSyllables < 0;
					} catch(NumberFormatException e) { // the word is not in the wordData database
						// do nothing, while loop will run again
					}
				}
				// every 3rd and 4th line for each stanza the last word has to rhyme with the word
				// two lines up
				// Test checks if the current word is the last word of the line and checks
				// if its the 3rd or 4th stanza
				if (syllables == 8 - i % 2 * 2 && i % 4 >= 2) {
					line = " " + findWordThatRhymes(lastWords, dictionary.get(wordIndex), curSyllables);
				} else if (syllables == 8 - i % 2 * 2) {
					// not the 3rd or 4th stanza, but this is the last word of the line
					// so it needs to be added to the stack for future reference
					lastWords.push(dictionary.get(wordIndex));
					line = " " + dictionary.get(wordIndex) + line;
				} else { // a non-last word
					line = " " + dictionary.get(wordIndex) + line;
				}
				// Updates the number of syllables left in the current line
				syllables -= curSyllables;
			}
			System.out.println(line.trim());
			// four lines have already been printed, its time for a new stanza
			if (i == 3) {
				System.out.println();
			}

		}
	}
	
	// Searches and returns for a word that rhymes with the second most recent word in lastWords
	// with the same amount of syllables as currentWord. If no such word is found, then 
	// currentWord is returned
	private String findWordThatRhymes(Stack<String> lastWords, String currentWord, int syllables) {
		// takes of the word from the previous line and stores it in temp variable
		String prevWord = lastWords.pop();
		// looks at the value of the last word two lines ago
		String rhymeWith = lastWords.peek();
		Set<String> rhymes = wordData.getRhymes(rhymeWith);
		// no true rhymes
		if (rhymes.contains(null)) { //NTS: fix word data so I can use .isEmpty() instead
			wordData.getNearRhymes(rhymeWith);
		}
		// no near rhymes either so the originally chosen word is going to 
		// be used even though it doesn't rhyme, OR the original word is one of
		// the rhyming words so it will be used
		if (rhymes.contains(null) || rhymes.contains(currentWord)) {
			lastWords.push(prevWord);
			lastWords.push(currentWord);
			return currentWord;
			//line = " " + currentWord + line;
		}
		// Looks through all the rhyming words to find a match
		String word = currentWord;
		for (String curWord : rhymes) {
			try {
				// if curWord has the correct number of syllables, and if curWord is found
				// in the dictionary then curWord will be the used word
				if (Integer.parseInt(wordData.syllables(curWord)) == syllables &&
						dictionary.contains(curWord)) {
					word = curWord;
				}
			} catch (NumberFormatException e) {} // cur word isn't in wordData, will be used anyways 
		}
		// puts prevWord back into the stack and adds the new "word"
		lastWords.push(prevWord);
		lastWords.push(word);
		return word;
	}
}
