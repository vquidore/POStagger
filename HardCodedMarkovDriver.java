/**
 * A driver class for the hard coded graph designed to test the Viterbi algorithm in isolation
 * @author Veronica Quidore, Dartmouth CS 10, Spring 2021
 * @author Nicholas Sugiarto, Dartmouth CS 10, Spring 2021
 */

public class HardCodedMarkovDriver {
    public static void main(String[] args) {
        MarkovModel defaultMarkov = new MarkovModel();  // MarkovModel using the default and therefore hard-coded graph

        // Iterates over the array returned by the Viterbi Algorithm method and prints each of the tags
        String line = "I fish";
        System.out.println("Line: " + line);
        for(String item : defaultMarkov.viterbiAlgo(line)){
            System.out.println(item);
        }

        System.out.println("-----------------------------------");
        System.out.println("Line: " + (line = "One cook uses a saw"));
        for(String item : defaultMarkov.viterbiAlgo(line)){
            System.out.println(item);
        }

        System.out.println("-----------------------------------");
        System.out.println("Line: " + (line = "The mine has many fish"));
        for(String item : defaultMarkov.viterbiAlgo(line)){
            System.out.println(item);
        }
    }
}
