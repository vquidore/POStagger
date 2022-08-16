import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 * A Hidden Markov Model that is generated using a test data set that, using the Viterbi algorithm,
 * predicts the parts of speech of previously unseen sentences.
 * @author Veronica Quidore, Dartmouth CS 10, Spring 2021
 * @author Nicholas Sugiarto, Dartmouth CS 10, Spring 2021
 */
public class MarkovModel {

    // Markov Model "Graph"
    private HashMap<String,HashMap<String,Double>> observationMap;
    private HashMap<String,HashMap<String,Double>> transitionsMap;

    // Penalty for having a word not contained in any of the training data
    private double unseenConstant;

    // The starting part of speech
    private String startPOS;

    /**
     * Default constructor for the Markov model
     * Makes a hard-coded graph provided by the PS-5 web page
     * Used for testing the Viterbi algorithm in the HardCodedMarkovDriver class
     */
    public MarkovModel(){
        startPOS = "#";
        unseenConstant = -100.0;
        transitionsMap = new HashMap<String,HashMap<String,Double>>();
        observationMap= new HashMap<String,HashMap<String,Double>>();

        // Transition map for the start part of speech
        HashMap<String, Double> startTransMap = new HashMap<>();
        startTransMap.put("MOD", -2.3);
        startTransMap.put("PRO", -1.2);
        startTransMap.put("DET", -0.9);
        startTransMap.put("NP", -1.6);
        transitionsMap.put(startPOS, startTransMap);

        // Transition map for the "MOD" part of speech
        HashMap<String, Double> modTransMap = new HashMap<>();
        modTransMap.put("PRO", -0.7);
        modTransMap.put("V", -0.7);
        transitionsMap.put("MOD", modTransMap);

        // Observation map for the "MOD" part of speech
        HashMap<String, Double> modObsMap = new HashMap<>();
        modObsMap.put("can", -0.7);
        modObsMap.put("will", -0.7);
        observationMap.put("MOD", modObsMap);

        // Transition map for the "PRO" part of speech
        HashMap<String, Double> proTransMap = new HashMap<>();
        proTransMap.put("VD", -1.6);
        proTransMap.put("MOD", -1.6);
        proTransMap.put("V", -0.5);
        transitionsMap.put("PRO", proTransMap);

        // Observation map for the "PRO" part of speech
        HashMap<String, Double> proObsMap = new HashMap<>();
        proObsMap.put("i", -1.9);
        proObsMap.put("many", -1.9);
        proObsMap.put("me", -1.9);
        proObsMap.put("mine", -1.9);
        proObsMap.put("you", -0.8);
        observationMap.put("PRO", proObsMap);

        // Transition map for the "V" part of speech
        HashMap<String, Double> verbTransMap = new HashMap<>();
        verbTransMap.put("PRO", -1.9);
        verbTransMap.put("DET", -0.2);
        transitionsMap.put("V", verbTransMap);

        // Observation map for the "V" part of speech
        HashMap<String, Double> verbObsMap = new HashMap<>();
        verbObsMap.put("color", -2.1);
        verbObsMap.put("cook", -1.4);
        verbObsMap.put("eats", -2.1);
        verbObsMap.put("fish", -2.1);
        verbObsMap.put("has", -1.4);
        verbObsMap.put("uses", -2.1);
        observationMap.put("V", verbObsMap);

        // Transition map for the "N" part of speech
        HashMap<String, Double> nounTransMap = new HashMap<>();
        nounTransMap.put("VD", -0.4);
        nounTransMap.put("V", -0.3);
        transitionsMap.put("N", nounTransMap);

        // Observation map for the "N" part of speech
        HashMap<String, Double> nounObsMap = new HashMap<>();
        nounObsMap.put("color", -2.4);
        nounObsMap.put("cook", -2.4);
        nounObsMap.put("fish", -1.0);
        nounObsMap.put("jobs", -2.4);
        nounObsMap.put("mine", -2.4);
        nounObsMap.put("saw", -1.7);
        nounObsMap.put("uses", -2.4);
        observationMap.put("N", nounObsMap);

        // Transition map for the "VD" part of speech
        HashMap<String, Double> vdTransMap = new HashMap<>();
        vdTransMap.put("PRO", -0.4);
        vdTransMap.put("DET", -1.1);
        transitionsMap.put("VD", vdTransMap);

        // Observation map for the "VD" part of speech
        HashMap<String, Double> vdObsMap = new HashMap<>();
        vdObsMap.put("saw", -1.1);
        vdObsMap.put("were", -1.1);
        vdObsMap.put("wore", -1.1);
        observationMap.put("VD", vdObsMap);

        // Transition map for the "DET" part of speech
        HashMap<String, Double> detTransMap = new HashMap<>();
        detTransMap.put("N", 0.0);
        transitionsMap.put("DET", detTransMap);

        // Observation map for the "DET" part of speech
        HashMap<String, Double> detObsMap = new HashMap<>();
        detObsMap.put("a", -1.3);
        detObsMap.put("many", -1.7);
        detObsMap.put("one", -1.7);
        detObsMap.put("the", -1.0);
        observationMap.put("DET", detObsMap);

        // Transition map for the "NP" part of speech
        HashMap<String, Double> npTransMap = new HashMap<>();
        npTransMap.put("VD", -0.7);
        npTransMap.put("V", -0.7);
        transitionsMap.put("NP", npTransMap);

        // Observation map for the "NP" part of speech
        HashMap<String, Double> npObsMap = new HashMap<>();
        npObsMap.put("jobs", -0.7);
        npObsMap.put("will", -0.7);
        observationMap.put("NP", npObsMap);
    }

    /**
     * The constructor that reads in data files and constructs the model based off of that
     * @param fileWords - The file containing all the sentences
     * @param fileTags - The file containing all the corresponding tags for the sentences
     * @throws Exception - if the files cannot be opened
     */
    public MarkovModel(String fileWords, String fileTags) throws Exception {

        // Stores sentences as inputs
        // Stored as an array list of string so that each sentence has its own list index,
        // And each sentences's tags are stored as an array within that index
        ArrayList<String[]> sentenceTags;
        ArrayList<String[]> sentences;

        unseenConstant = -100.0;
        startPOS = "#";

        sentences = readFile(fileWords);  // Reads in the words
        sentenceTags = readFile(fileTags);  // Reads in the corresponding tags

        this.training(sentences, sentenceTags);
    }

    /**
     * Build the transitions and observations maps and normalize them by the totals in the training data.
     * @param sentences - An ArrayList of each line in the file. Each word in the sentence line is an array of strings.
     * @param sentenceTags - An ArrayList of the corresponding tags for each word in each sentence
     */
    private void training(ArrayList<String[]> sentences, ArrayList<String[]> sentenceTags) {

        // Initializes map of correspondences between current parts of speech and their current words
        observationMap = new HashMap<String,HashMap<String,Double>>();
        // Initializes map of correspondences between current and next possible parts of speech
        transitionsMap = new HashMap<String,HashMap<String,Double>>();

        // The total observations and transitions for later normalization
        HashMap<String,Double> observationTotals = new HashMap<>();
        HashMap<String,Double> transitionsTotals = new HashMap<>();

        // Initializes the start transition total, insert the empty map of possible transitions from the start POS
        HashMap<String,Double> startMap = new HashMap<>();
        transitionsMap.put(startPOS,startMap);
        transitionsTotals.put(startPOS, 0.0);

        // for each line in sentence tags
        for (int j = 0; j < sentenceTags.size(); j++) {
            // Increment the starting POS in transition graph to direct to the first possible POS
            String currTag = sentenceTags.get(j)[0];
            incrementMap(startPOS, currTag, transitionsMap, transitionsTotals);
            // for each part of speech in a line in sentence tags
            for (int i = 0; i < (sentenceTags.get(j).length - 1); i++) {

                // Create the indexes
                // Messy, but allows us to to get both the transitions and observations in one pass
                currTag = sentenceTags.get(j)[i];
                String nextTag = sentenceTags.get(j)[i+1];
                String currWord = sentences.get(j)[i];

                // Builds the transition map
                if (nextTag != null) {
                    if (transitionsMap.containsKey(currTag)) {
                        incrementMap(currTag, nextTag, transitionsMap, transitionsTotals);
                    }
                    else { initializeMap(currTag, nextTag, transitionsMap, transitionsTotals); }
                }

                // Builds the observation map
                if (observationMap.containsKey(currTag)) {
                    incrementMap(currTag, currWord, observationMap, observationTotals);
                }
                else { initializeMap(currTag, currWord, observationMap, observationTotals); }
            }
            // add the very last elements in the arrays into the observation map
            // we don't want transitions because next element will be null, which is why this is a special case
            // initialize last indexes
            int lastIdx = sentenceTags.get(j).length-1;
            String lastWord = sentences.get(j)[lastIdx];
            String lastTag = sentenceTags.get(j)[lastIdx];

            if (observationMap.containsKey(lastTag)) {
                incrementMap(lastTag, lastWord, observationMap, observationTotals);
            }
            else { initializeMap(lastTag, lastWord, observationMap, observationTotals); }
        }

        normalizeData(transitionsMap, transitionsTotals);
        normalizeData(observationMap, observationTotals);
    }

    /**
     * If a String key already exists or the transitions or observations maps, either insert a new HashMap value for the "from" key,
     * or increment the frequency value of the HashMap value by 1.
     * @param fromEle - String of key to greater transition or observation map
     * @param toEle - String of key to HashMap value of the greater transition or observation map
     * @param incrementMap - transition or observation HashMap<String, HashMap<String,Double>> to be incremented
     * @param totalsMap - cumulative total HashMap<String,Double> of each part of speech String key in the transitions and observations maps
     */
    private void incrementMap(String fromEle, String toEle, HashMap<String, HashMap<String,Double>> incrementMap, HashMap<String,Double> totalsMap){
        if (incrementMap.get(fromEle).containsKey(toEle)) {
            double newFreq = incrementMap.get(fromEle).get(toEle) + 1; //increase observation freq by 1
            incrementMap.get(fromEle).put(toEle, newFreq);     //add new values to transitions map
        }
        else { incrementMap.get(fromEle).put(toEle, 1.0); }  // instantiate the key and value pair in the HashMap

        //increment the total value for the given key in the totals map
        double newTotVal = totalsMap.get(fromEle) + 1;
        totalsMap.put(fromEle, newTotVal);
    }

    /**
     * If the string key part of speech is not contained in the transitions or observations map,
     * add both the key and a corresponding HashMap into the passed transition or observation map.
     * @param fromEle - String key of greater transition or observation map
     * @param toEle - String key of HashMap value of greater transition or observation map
     * @param incrementMap - passed through HashMap<String, HashMap<String,Double>> of transitions or observations
     * @param totalsMap - passed through HashMap<String,Double> of corresponding transitions or observations cumulative totals
     */
    private void initializeMap(String fromEle, String toEle, HashMap<String, HashMap<String,Double>> incrementMap, HashMap<String,Double> totalsMap){
        HashMap<String, Double> valueMap = new HashMap<>();  //instantiated HashMap value that corresponds with POS key
        valueMap.put(toEle, 1.0);
        incrementMap.put(fromEle, valueMap);
        totalsMap.put(fromEle, 1.0);
    }

    /**
     * Normalize the frequencies for each instance in either the passed through transition or observation map.
     * Divide every frequency value in the HashMap corresponding to a POS key by the total frequency of that POS key
     * @param incrementMap - either transitions or observations (contains every frequency value) HashMap<String,HashMap<String,Double>>
     * @param totalsMap - transitions or observations HashMap<String,Double> containing for every corresponding frequency value
     */
    private void normalizeData( HashMap<String,HashMap<String,Double>> incrementMap, HashMap<String,Double> totalsMap){
        for (String fromEle : incrementMap.keySet()) {
            for (String toEle : incrementMap.get(fromEle).keySet()) {
                double freq = incrementMap.get(fromEle).get(toEle);
                incrementMap.get(fromEle).put(toEle, (Math.log(freq/totalsMap.get(fromEle))));  //use logs for preserve precision
            }
        }
    }

    /**
     * Read each file of Strings separated by a " ", and add each line at its own index
     * where each line is split into an array of strings
     * @param fileName - Name of the file to be read
     * @return - The ArrayList of all the lines split into individual arrays of Strings
     * @throws Exception if the file cannot be opened
     */
    private ArrayList<String[]> readFile(String fileName) throws Exception {
        BufferedReader input = null;
        ArrayList<String[]> allEle = new ArrayList<String[]>(); // The ArrayList of all the elements

        try {
            input = new BufferedReader(new FileReader(fileName));  // Open the input
            String line;

            // Read over all the lines, and split them into String arrays
            // Add each array into the ArrayList of all the elements
            while((line=input.readLine()) != null){
                String[] ele = line.split(" ");
                allEle.add(ele);
            }
            input.close();
        }
        catch(Exception e) { throw new Exception("File not found"); }

        finally { if (input != null) { input.close(); } }

        return allEle;
    }

    /**
     * Tests the accuracy of the model by predicting the tags of a sentence, and then seeing whether or not they align
     * with the "answer key" also provided
     * @param testSentenceFile - The list of sentences to test the model against
     * @param testTagsFile - The "answer key" of all the tags for each sentence
     * @return - A String message containing information of the model's accuracy
     * @throws Exception if the file cannot be read or opened
     */
    public String testAccuracy (String testSentenceFile, String testTagsFile) throws Exception {
        ArrayList<String[]> testTags;
        double correctTags = 0, falseTags = 0;

        testTags = readFile(testTagsFile);
        String[] tagResults;

        BufferedReader wordsInput = null;
        try{
            wordsInput = new BufferedReader(new FileReader(testSentenceFile));
            String line;

            int lineIndex = 0;  // Which line we are on

            while ((line=wordsInput.readLine()) != null) {
                tagResults = viterbiAlgo(line);  // Processes the sentence through the Viterbi algorithm
                // Iterate over each item in the line
                for (int resultIndex = 0; resultIndex < (tagResults.length); resultIndex++) {
                    // If the Viterbi's prediction for a given word aligns with the corresponding answer, increment correct tags by one
                    if (tagResults[resultIndex].equals(testTags.get(lineIndex)[resultIndex])) { correctTags += 1; }
                    // Otherwise increment false tags by one
                    else {falseTags += 1;}
                }
                lineIndex += 1;
            }

            // Return message about the model's accuracy
            return ("Correct Tags: " + correctTags +
                    "\nFalse Tags: " + falseTags +
                    "\nThe model is approximately " + correctTags/(correctTags + falseTags) * 100) + "% correct!";
        }

        catch(IOException e) { throw new IOException("File not found"); }

        // Close the input
        finally { if (wordsInput != null) { wordsInput.close(); } }
    }

    /**
     * Run the Viterbi Algorithm on a given String of text to match each word to its most likely part of speech.
     * The part of speech is determined by calculating a next score (the sum of the observation, transition, and current scores)
     * and maintaining a back-tracer which will be used to trace back the path of its most likely parts of speech
     * from the final highest score to the start #.
     * @param line - String line of text that either a file or user inputs to be analyzed by algorithm
     * @return - return the correct array of String parts of speech in order from the start to the end of the sentence
     */
    protected String[] viterbiAlgo(String line) {
        //instantiate array list of observed words, back tracer, current scores, and current states
        String[] eachObservation = line.split(" ");
        ArrayList<HashMap<String, String>> backTrace = new ArrayList<>();
        HashMap<String, Double> currScores = new HashMap<>();
        HashMap<String, String> currStates = new HashMap<>();

        // initializing first key value pair to begin loop
        currStates.put(startPOS, null);
        currScores.put(startPOS, 0.0);

        double observedScore;
        double nextScore;

        // for each observed word in the given line...
        for (String observedWord : eachObservation) {
            //instantiate empty next states and next scores HashMap
            HashMap<String, Double> nextScores = new HashMap<>();
            HashMap<String, String> nextStates = new HashMap<>();
            observedWord = observedWord.toLowerCase(); // make sure that all words not separated based on capitalization

            // iterates over current states and all of their possible transitions
            for (String currState : currStates.keySet()) {
                if(transitionsMap.get(currState) != null) {
                    for (String possTrans : transitionsMap.get(currState).keySet()) {
                        // if the word is observed, set the observed score to that in the observation map, otherwise penalize that cowardly morphemic blob
                        if (observationMap.get(possTrans).containsKey(observedWord)) {
                            observedScore = observationMap.get(possTrans).get(observedWord);
                        }
                        else {
                            observedScore = unseenConstant;
                        }

                        nextScore = currScores.get(currState) + transitionsMap.get(currState).get(possTrans) + observedScore;

                        // update the next score and states if they are the most likely transitions for the current state
                        if (!nextScores.containsKey(possTrans) || nextScore > nextScores.get(possTrans)) {
                            nextStates.put(possTrans, currState);
                            nextScores.put(possTrans, nextScore);
                        }
                    }
                }
            }
            // update for iteration
            currScores = nextScores;
            backTrace.add(nextStates);
            currStates = nextStates;
        }

        // iterate over all possible final states. Identify greatest corresponding score
        // set the starting part of speech for the backtrace accordingly
        String probablePOS = null;
        double probableScore = Double.NEGATIVE_INFINITY;
        for (String finalPOS : currScores.keySet()) {
            double testScore = currScores.get(finalPOS);
            if (testScore > probableScore) {
                probablePOS = finalPOS;
                probableScore = testScore;
            }
        }

        // Initialize the final array of all the words to return to the caller
        String[] finalWords = new String[backTrace.size()];
        String iterPOS = probablePOS;  // Let the iteration begin at the most likely end state

        finalWords[finalWords.length-1] = iterPOS;  // Add the most likely end state to the end of the array

        // Add the rest of the items behind it all the way to the beginning
        for (int i = (finalWords.length - 2); i >= 0; i--) {
            finalWords[i] = backTrace.get(i + 1).get(iterPOS);  // Grab the likely end state from the corresponding index in the backtrace
            iterPOS = finalWords[i];  // Update the iteration
        }

        // Return the array. We chose a String array and not an ArrayList to make it easier to align
        // with other arrays in testAccuracy, as the tags are also formatted as a String array
        return finalWords;
    }

    /**
     * Console test method for the user to input screens using a Scanner Class.
     */
    public void consoleInput(){

        Scanner in = new Scanner(System.in);

        // Opening message for the user
        System.out.println("\nType any sentence below. Hit enter to see each part of speech. Press \">q\" to quit" );

        String userInput = in.nextLine().toLowerCase();  // Translate the user input to lowercase so that words are not separated by capitalization

        while(!userInput.equals(">q")) {  // >q quits the "game"

            // Error message if the user refuses to input text
            if (userInput.equals("")) { System.out.println("Please gimme some text!"); }

            else {
                String printPOS = "";
                // Iterate over all the results from the Viterbi algorithm
                String[] viterbiOutput = viterbiAlgo(userInput);
                for (String item : viterbiOutput) {
                    printPOS += item + " ";
                }
                System.out.println(printPOS);  // Print out all the parts of speech
            }
            userInput = in.nextLine();  // Await the user's next input
        }
    }

    /**
     * Generates a random sentence
     */

    public String generateRandomSentence(){
        String sentence = "";

        // Build a sentence until the sentence hits a punctuation mark
        String selectPOS = startPOS;  // Initialize the start of the sentence
        while(!selectPOS.equals(".")) {
            selectPOS = generateRandomEle(selectPOS, transitionsMap);  // Select a random part of speech
            String word = generateRandomEle(selectPOS, observationMap);  // From that part of speech, select a random word
            sentence += word + " ";  // Add the random word to the sentence
        }
        return sentence;
    }

    /**
     * Generates a random element by creating a probablity table and selecting a random element off of that
     * @param elementKey - The key with which to narrow down the random element search to
     * @param instanceMap - A map of elements that contains each item-to-item frequency, e.g. a transition or observation map
     * @return - the randomly generated element
     */
    private String generateRandomEle(String elementKey, HashMap<String,HashMap<String,Double>> instanceMap) {
        double totalTransProbability = 0;

        // Find the total probability of the entire instance map given the key
        for (String possNextSelect : instanceMap.get(elementKey).keySet()) { totalTransProbability += (1 + instanceMap.get(elementKey).get(possNextSelect)); }

        // Construct the probability table
        // Divides each log frequency by the total log frequency to generate values
        double upperBound = 0;
        HashMap<Double, String> probTable = new HashMap<>();
        for (String possNextSelect: instanceMap.get(elementKey).keySet()) {
            // The reciprocal is taken because log makes smaller elements have a bigger value than smaller elements
            // This ensures that more common elements are represented accordingly
            double probScore = (totalTransProbability / (1 + instanceMap.get(elementKey).get(possNextSelect)));
            // The upper bound for the probability table
            upperBound += probScore;
            // Inserts the item into the probability table
            probTable.put(upperBound, possNextSelect);
        }

        // Selects the random item from the probability table accounting for each of their weights
        int randIdx = (int) (Math.random() * upperBound);  // Generate the random index that is within the bounds of the table
        Double[] probArray = probTable.keySet().toArray(new Double[probTable.size()]); // Cast the keyset to an array
        Arrays.sort(probArray);  // Sort the array by upper bound
        Double randPOS = probArray[0];  // Initialize the first element as the lowest element in the probability table
        // Iterates over the array. If the index is above the upper bound, then kill the loop
        for(Double item: probArray) {
            if (randIdx < item) { break; }
            // Otherwise, then change the random item to the currently iterated item
            else {randPOS = item;}
        }
        return probTable.get(randPOS);
    }

    /**
     * Generates a sentence that predicts the most likely transition, and from that chooses a random word
     * @return - A predicatively generated sentence
     */
    public String generatePredictiveSentence() {
        String sentence = "";

        // Loops until 7 words are added to the sentence
        String selectPOS = startPOS;
        for (int i = 0; i < 7; i++) {  // Hard-capped at 7 because a period-to-period transition is never the most likely option
            selectPOS = mostLikelyTransition(selectPOS);  // Selects the most likely transition
            String word = generateRandomEle(selectPOS, observationMap);  // Selects the most likely word given that transition
            sentence += word + " ";  // Add the word to the sentence
        }
        sentence += ".";  // Add a period to end the sentence
        return sentence;
    }


    /**
     * Iterates over a given part of speech and selects the most likely transition from it
     * @param selectPOS - The part of speech to transition from
     * @return - The most likely transition
     */
    public String mostLikelyTransition(String selectPOS) {
        // Initialize the most likely transition and the highest probability
        double highestProbability = Double.NEGATIVE_INFINITY;
        String likelyTransition = null;

        // Loops over all the possible transitions, selecting the most probable one
        for (String nextPossEle : transitionsMap.get(selectPOS).keySet()) {
            double nextPossProb = transitionsMap.get(selectPOS).get(nextPossEle);
            if (nextPossProb > highestProbability) {
                highestProbability = nextPossProb;
                likelyTransition = nextPossEle;
            }
        }

        return likelyTransition;
    }


}
