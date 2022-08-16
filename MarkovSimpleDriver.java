/**
 * A driver class to test the results of the simple Hidden Markov Model created for Problem Set 5.
 * @author Veronica Quidore, Dartmouth CS 10, Spring 2021
 * @author Nicholas Sugiarto, Dartmouth CS 10, Spring 2021
 */

public class MarkovSimpleDriver {
    public static void main(String[] args) throws Exception {
        MarkovModel markovTester = new MarkovModel("./PS5/texts/simple-train-sentences.txt", "./PS5/texts/simple-train-tags.txt");
        System.out.println(markovTester.testAccuracy("./PS5/texts/simple-test-sentences.txt", "./PS5/texts/simple-test-tags.txt"));
        markovTester.consoleInput();
    }
}
