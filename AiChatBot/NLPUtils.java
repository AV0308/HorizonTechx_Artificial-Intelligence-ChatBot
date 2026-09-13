import java.util.*;

/**
 * NLPUtils centralises all the "basic NLP concepts" used by the chatbot:
 *   - text normalization (lower-casing, punctuation stripping)
 *   - tokenization
 *   - stop-word removal
 *   - a lightweight suffix-stripping stemmer
 *   - a small synonym/slang dictionary
 *   - Levenshtein distance for fuzzy (typo-tolerant) matching
 */
public class NLPUtils {

    // Common English stop-words that don't carry intent-matching value.
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "are", "was", "were", "am", "i", "you", "he", "she",
            "it", "we", "they", "to", "of", "in", "on", "at", "for", "with", "and", "or",
            "but", "do", "does", "did", "can", "could", "will", "would", "should", "my",
            "your", "his", "her", "its", "our", "their", "this", "that", "these", "those",
            "which", "who", "whom", "please", "me", "us", "them", "him", "be", "been",
            "so", "just", "there"
    ));

    // Slang / synonym normalisation so "hey", "hiya", "yo" all behave like "hello".
    private static final Map<String, String> SYNONYMS = new HashMap<>();
    static {
        SYNONYMS.put("hey", "hello");
        SYNONYMS.put("hiya", "hello");
        SYNONYMS.put("yo", "hello");
        SYNONYMS.put("sup", "hello");
        SYNONYMS.put("bye", "goodbye");
        SYNONYMS.put("cya", "goodbye");
        SYNONYMS.put("later", "goodbye");
        SYNONYMS.put("thx", "thanks");
        SYNONYMS.put("thankyou", "thanks");
        SYNONYMS.put("ty", "thanks");
        SYNONYMS.put("u", "you");
        SYNONYMS.put("r", "are");
        SYNONYMS.put("ur", "your");
        SYNONYMS.put("pls", "please");
        SYNONYMS.put("plz", "please");
        SYNONYMS.put("gr8", "great");
    }

    /** Lower-cases and strips punctuation (keeping letters, digits, +-*\/=?. and apostrophes). */
    public static String normalize(String input) {
        if (input == null) return "";
        String s = input.toLowerCase().trim();
        s = s.replaceAll("[^a-z0-9\\s+\\-*/=?.']", " ");
        s = s.replaceAll("\\s+", " ").trim();
        return s;
    }

    public static List<String> tokenize(String normalized) {
        if (normalized.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(normalized.split("\\s+")));
    }

    public static String applySynonym(String token) {
        return SYNONYMS.getOrDefault(token, token);
    }

    /** Very small suffix-stripping stemmer (Porter-lite): good enough for keyword matching. */
    public static String stem(String word) {
        String w = word;
        String[] suffixes = {"ing", "edly", "edness", "ed", "ies", "es", "s", "er", "ly"};
        for (String suf : suffixes) {
            if (w.length() - suf.length() >= 3 && w.endsWith(suf)) {
                return w.substring(0, w.length() - suf.length());
            }
        }
        return w;
    }

    /** Full pipeline: normalize -> tokenize -> synonym-map -> drop stop-words -> stem. */
    public static List<String> extractKeywords(String rawInput) {
        String normalized = normalize(rawInput);
        List<String> tokens = tokenize(normalized);
        List<String> keywords = new ArrayList<>();
        for (String t : tokens) {
            String syn = applySynonym(t);
            if (STOP_WORDS.contains(syn) || syn.isEmpty()) continue;
            keywords.add(stem(syn));
        }
        return keywords;
    }

    /** Classic edit-distance algorithm, used to tolerate small typos. */
    public static int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }

    public static boolean fuzzyEquals(String a, String b, int maxDist) {
        if (a.equals(b)) return true;
        if (Math.abs(a.length() - b.length()) > maxDist) return false;
        return levenshtein(a, b) <= maxDist;
    }
}
