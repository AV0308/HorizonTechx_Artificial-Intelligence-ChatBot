import java.util.*;

/**
 * Represents one "intent" the bot understands: a tag, a set of example
 * patterns (converted to keyword sets), and a pool of possible responses.
 */
public class Intent {
    private final String tag;
    private final List<Set<String>> patternKeywordSets = new ArrayList<>();
    private final List<String> responses;

    public Intent(String tag, String[] patterns, String[] responses) {
        this.tag = tag;
        this.responses = new ArrayList<>(Arrays.asList(responses));
        for (String p : patterns) {
            addPattern(p);
        }
    }

    public void addPattern(String pattern) {
        List<String> kws = NLPUtils.extractKeywords(pattern);
        if (!kws.isEmpty()) {
            patternKeywordSets.add(new HashSet<>(kws));
        }
    }

    public void addResponse(String response) {
        responses.add(response);
    }

    public String getTag() {
        return tag;
    }

    public List<String> getResponses() {
        return responses;
    }

    /**
     * Scores this intent against a user's extracted keywords.
     * For every stored pattern, computes the fraction of that pattern's
     * keywords which are matched (exactly or via a small fuzzy tolerance)
     * somewhere in the user's input, and returns the best such fraction.
     */
    public double scoreAgainst(List<String> inputKeywords) {
        if (inputKeywords.isEmpty()) return 0;
        double best = 0;
        int inputSize = inputKeywords.size();
        for (Set<String> pset : patternKeywordSets) {
            if (pset.isEmpty()) continue;
            int matches = 0;
            for (String pk : pset) {
                int maxDist = pk.length() > 5 ? 2 : 1;
                for (String ik : inputKeywords) {
                    if (NLPUtils.fuzzyEquals(pk, ik, maxDist)) {
                        matches++;
                        break;
                    }
                }
            }
            // precision: how much of the pattern was found in the input
            double precision = (double) matches / pset.size();
            // recall: how much of the input is explained by the pattern
            double recall = (double) matches / inputSize;
            // combined confidence - stops a short generic pattern (e.g. a single
            // common word) from "perfectly" matching a longer, unrelated query
            double score = precision * recall;
            if (score > best) best = score;
        }
        return best;
    }
}
