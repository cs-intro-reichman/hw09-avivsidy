import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		In in = new In(fileName);
		String text = in.readAll();
		text = text.replace("\r", "");
		if (text.length() <= windowLength) {
			return;
		}

		for (int i = 0; i + windowLength < text.length(); i++) {
			String window = text.substring(i, i + windowLength);
			char nextChar = text.charAt(i + windowLength);
			List probs = CharDataMap.get(window);
			if (probs == null) {
				probs = new List();
				CharDataMap.put(window, probs);
			}
			probs.update(nextChar);
		}

		for (String key : CharDataMap.keySet()) {
			calculateProbabilities(CharDataMap.get(key));
		}
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	void calculateProbabilities(List probs) {				
		if (probs == null || probs.getSize() == 0) {
			return;
		}
		CharData[] arr = probs.toArray();
		int total = 0;
		for (int i = 0; i < arr.length; i++) {
			total += arr[i].count;
		}
		double cumulative = 0.0;
		for (int i = 0; i < arr.length; i++) {
			arr[i].p = (double) arr[i].count / total;
			cumulative += arr[i].p;
			arr[i].cp = cumulative;
		}
	}

    // Returns a random character from the given probabilities list.
	char getRandomChar(List probs) {
		if (probs == null || probs.getSize() == 0) {
			return ' ';
		}
		double r = randomGenerator.nextDouble();
		ListIterator it = probs.listIterator(0);
		while (it != null && it.hasNext()) {
			CharData cd = it.next();
			if (r < cd.cp) {
				return cd.chr;
			}
		}
		return probs.getFirst().chr;
	}

	public String generate(String initialText, int textLength) {
		if (initialText == null) {
			return null;
		}
		if (textLength <= 0 || initialText.length() < windowLength) {
			return initialText;
		}
		StringBuilder out = new StringBuilder(initialText);
		for (int i = 0; i < textLength; i++) {
			String window = out.substring(out.length() - windowLength, out.length());
			List probs = CharDataMap.get(window);
			if (probs == null) {
				break;
			}
			out.append(getRandomChar(probs));
		}
        return out.toString();
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		if (args.length < 4) {
			return;
		}
		int windowLength = Integer.parseInt(args[0]);
		String fileName = args[1];
		String initialText = args[2];
		int textLength = Integer.parseInt(args[3]);
		LanguageModel model = new LanguageModel(windowLength);
		model.train(fileName);
		System.out.print(model.generate(initialText, textLength));
    }
}