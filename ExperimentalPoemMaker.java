import java.io.*;
import java.util.*;
import datamuse.*;

// Produces experimental poems with the words found in the given text file. 
// The poems have two stanzas, 8 lines, an abab rhyhiming pattern, and 
// lines alternate between 8 and 6 syllables

public class ExperimentalPoemMaker {

	public static void main(String[] args) throws FileNotFoundException {
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
		ArrayList<String> dictionary = getWords(text);
		System.out.println("How many poems would you like made?");
		int num = input.nextInt();
		// A num amount of experimental poems are printed
		for (int i = 0; i < num; i++) {
			printPoem(dictionary);
		}		
	}

	// Returns an ArrayList of the words found in the given text
	// If a word is found multiple times in the given text,
	// then the returned list will contain the same number of 
	// instances of the said word
	public static ArrayList<String> getWords(Scanner text) {
		ArrayList<String> dictionary = new ArrayList<String>();
		while (text.hasNext()) {
			dictionary.add(text.next());
		}
	    return dictionary;
	}
	
	// Makes and prints an 8 line poem made with the given dictionary
	// The poem has 2 stanzas, an abab rhyming pattern, and alternates
	// between 8 and 6 syllables
	public static void printPoem(ArrayList<String> dictionary) {
		Random r = new Random();
		// Used to find the # of syllables in words and if words rhyme
		DatamuseQuery wordData = new DatamuseQuery();
		// the last words in each line
		Stack<String> lastWords = new Stack<String>();
		for (int i = 0; i < 8; i++) {
			// Number of syllables alternates between 8 and 6 for each line
			int syllables = 8 - i % 2 * 2;
			// runs until the syllable max is met for the current line
			String line = "";
			while (syllables > 0) {
				// index for the randomly chosen word
				// runs until it finds a word whose syllables can fit into the line
				int curSyllables = syllables + 1;
				int wordIndex = 0;
				while (syllables - curSyllables < 0) {
					wordIndex = r.nextInt(dictionary.size());
					try {
						curSyllables = Integer.parseInt(wordData.syllables(dictionary.get(wordIndex)));
					} catch(NumberFormatException e) { // the word is not in the wordData database
						// makes it so the word at the current wordIndex isn't used for the poem
						curSyllables = syllables + 1;
					}
				}
				// every 3rd and 4th line for each stanza the last word has to rhyme with the word
				// two lines up
				// Test checks if the current word is the last word of the line and checks
				// its the 3rd or 4th stanza
				if (syllables == 8 - i % 2 * 2 && i % 4 >= 2) {
					// takes of the word from the previous line and stores it in temp variable
					String prevWord = lastWords.pop();
					// looks at the value of the last word two lines ago
					String rhymeWith = lastWords.peek();
					Set<String> rhymes = wordData.getRhymes(rhymeWith);
					// no true rhymes
					if (rhymes == null) {
						wordData.getNearRhymes(rhymeWith);
					}
					// no near rhymes either so the originally chosen word is going to 
					// be used even though it doesn't rhyme, OR the original word is one of
					// the rhyming words so it will be used
					if (rhymes == null || rhymes.contains(dictionary.get(wordIndex))) {
						lastWords.push(prevWord);
						lastWords.push(dictionary.get(wordIndex));
						line = " " + dictionary.get(wordIndex) + line;
					} else {
						// Looks through all the rhyming words to find a match
						String word = dictionary.get(wordIndex);
						for (String curWord : rhymes) {
							try {
								// if curWord has the correct number of syllables, and if curWord is found
								// in the dictionary then curWord will be the used word
								if (Integer.parseInt(wordData.syllables(curWord)) == curSyllables &&
									dictionary.contains(curWord)) {
									word = curWord;
								}
							} catch (NumberFormatException e) {} // cur word isn't in wordData, will be used anyways 
						}
						// puts prevWord back into the stack and adds the new "word"
						lastWords.push(prevWord);
						lastWords.push(word);
						line = " " + word + line;
					}
				} else if (syllables == 8 - i % 2 * 2) {
					// not the 3rd or 4th stanza, but this is the last word
					// so it needs to be taken track of for future reference
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
}
