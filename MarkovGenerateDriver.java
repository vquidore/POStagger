public class MarkovGenerateDriver {
    public static void main(String[] args) throws Exception{
        MarkovModel markovTester = new MarkovModel("./PS5/texts/brown-train-sentences.txt", "./PS5/texts/brown-train-tags.txt");
        System.out.println(markovTester.generateRandomSentence());
        System.out.println(markovTester.generatePredictiveSentence());
    }
}