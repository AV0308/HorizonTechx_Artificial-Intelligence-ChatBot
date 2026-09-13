import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The brain of the chatbot. Given a raw line of user text it:
 *   1. checks for a "teach" command (runtime training),
 *   2. tries to extract the user's name (simple entity extraction),
 *   3. tries to evaluate a basic arithmetic expression,
 *   4. answers time/date questions dynamically,
 *   5. otherwise falls back to rule-based intent matching via KnowledgeBase.
 */
public class ChatEngine {

    private final KnowledgeBase kb = new KnowledgeBase();
    private final Random random = new Random();
    private String userName = null;

    private static final Pattern NAME_PATTERN =
            Pattern.compile("\\b(?:my name is|i am|i'm|call me)\\s+([a-zA-Z]{2,20})\\b");

    private static final Pattern MATH_PATTERN =
            Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s*([+\\-*/])\\s*(-?\\d+(?:\\.\\d+)?)");

    private static final Pattern TEACH_PATTERN =
            Pattern.compile("^(?:teach|learn)\\s*:\\s*(.+?)\\s*=>\\s*(.+)$", Pattern.CASE_INSENSITIVE);

    // Words that can follow "i am"/"i'm" without being a name (avoids "i am sad" -> name "sad").
    private static final Set<String> NON_NAME_WORDS = new HashSet<>(Arrays.asList(
            "fine", "good", "great", "okay", "ok", "sad", "tired", "stressed", "upset",
            "doing", "not", "also", "here", "back", "done", "ready", "happy", "bad",
            "well", "sorry", "sure", "alright", "busy", "hungry", "excited", "bored"
    ));

    private static final String[] FALLBACKS = {
            "I'm not sure I understand. Could you rephrase that?",
            "Interesting — can you tell me more, or try asking differently?",
            "I don't have an answer for that yet. You can teach me with: teach: <question> => <answer>",
            "Hmm, I didn't quite catch that."
    };

    public boolean isExit(String input) {
        String norm = NLPUtils.normalize(input);
        return norm.equals("exit") || norm.equals("quit") || norm.equals("bye") || norm.equals("goodbye");
    }

    public String process(String rawInput) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return "Please type something so I can help you.";
        }
        String trimmed = rawInput.trim();

        // 1. Runtime training command: "teach: <question> => <answer>"
        Matcher teachMatcher = TEACH_PATTERN.matcher(trimmed);
        if (teachMatcher.matches()) {
            String q = teachMatcher.group(1).trim();
            String a = teachMatcher.group(2).trim();
            kb.teach(q, a);
            return "Got it! I've learned how to respond to: \"" + q + "\"";
        }

        // 2. Name extraction (simple entity recognition)
        Matcher nameMatcher = NAME_PATTERN.matcher(trimmed.toLowerCase());
        if (nameMatcher.find()) {
            String candidate = nameMatcher.group(1);
            if (!NON_NAME_WORDS.contains(candidate)) {
                userName = capitalize(candidate);
                return "Nice to meet you, " + userName + "! How can I help you today?";
            }
        }

        // 3. Basic arithmetic ("what is 5 + 3", "12 * 4", "10 divided by 2")
        String mathReady = replaceMathWords(trimmed);
        Matcher mathMatcher = MATH_PATTERN.matcher(mathReady);
        if (mathMatcher.find()) {
            double a = Double.parseDouble(mathMatcher.group(1));
            String op = mathMatcher.group(2);
            double b = Double.parseDouble(mathMatcher.group(3));
            Double result = compute(a, op, b);
            if (result == null) {
                return "I can't divide by zero!";
            }
            return "That equals " + trimResult(result);
        }

        // 4. Time / date questions (answered dynamically, not from static text)
        String norm = NLPUtils.normalize(trimmed);
        if (norm.contains("time") && !norm.contains("sometime") && !norm.contains("timely")) {
            return "The current time is " +
                    LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + ".";
        }
        if (norm.contains("date") || norm.equals("today") || norm.contains("today s date")
                || norm.contains("what day")) {
            return "Today's date is " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")) + ".";
        }

        // 5. Rule-based intent matching (the core "trained" knowledge base)
        List<String> keywords = NLPUtils.extractKeywords(trimmed);
        Intent match = kb.findBestIntent(keywords);
        if (match != null) {
            String response = match.getResponses().get(random.nextInt(match.getResponses().size()));
            if (userName != null && match.getTag().equals("greeting")) {
                response = response.replaceFirst("!$", ", " + userName + "!");
            }
            return response;
        }

        // 6. Fallback
        return FALLBACKS[random.nextInt(FALLBACKS.length)];
    }

    private String replaceMathWords(String s) {
        String r = s.toLowerCase();
        r = r.replaceAll("\\bplus\\b", "+");
        r = r.replaceAll("\\bminus\\b", "-");
        r = r.replaceAll("\\btimes\\b|\\bmultiplied by\\b", "*");
        r = r.replaceAll("\\bdivided by\\b", "/");
        return r;
    }

    private Double compute(double a, String op, double b) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/": return b == 0 ? null : a / b;
            default: return null;
        }
    }

    private String trimResult(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d)) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    private String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public String getUserName() {
        return userName;
    }
}
