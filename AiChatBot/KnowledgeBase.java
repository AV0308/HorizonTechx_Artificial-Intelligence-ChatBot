import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Holds every Intent the bot knows about.
 *
 * Two sources of knowledge:
 *   1. A hard-coded set of common intents (greetings, thanks, small talk,
 *      FAQs, etc.) - this is the bot's "default training".
 *   2. A plain-text file (chatbot_data/trained_qa.txt) of question/answer
 *      pairs taught at runtime via the "teach: <question> => <answer>"
 *      command. This lets the bot be trained further without recompiling.
 */
public class KnowledgeBase {

    private final List<Intent> intents = new ArrayList<>();
    private static final String DATA_DIR = "chatbot_data";
    private static final String TRAINED_FILE = DATA_DIR + File.separator + "trained_qa.txt";
    private static final double MATCH_THRESHOLD = 0.45;

    public KnowledgeBase() {
        loadDefaultIntents();
        loadTrainedIntents();
    }

    private void addIntent(String tag, String[] patterns, String[] responses) {
        intents.add(new Intent(tag, patterns, responses));
    }

    private void loadDefaultIntents() {
        addIntent("greeting",
                new String[]{"hello", "hi", "hey", "good morning", "good evening",
                        "good afternoon", "hiya", "greetings", "what is up"},
                new String[]{"Hello! How can I help you today?",
                        "Hi there! What can I do for you?",
                        "Hey! Great to see you."});

        addIntent("goodbye",
                new String[]{"bye", "goodbye", "see you later", "see you",
                        "talk to you later", "exit", "quit"},
                new String[]{"Goodbye! Have a great day!",
                        "See you later!",
                        "Bye! Feel free to come back anytime."});

        addIntent("thanks",
                new String[]{"thanks", "thank you", "that helps", "appreciate it", "thanks a lot"},
                new String[]{"You're welcome!", "Happy to help!", "Anytime!"});

        addIntent("bot_identity",
                new String[]{"what is your name", "who are you", "what should i call you"},
                new String[]{"I'm JavaBot, your friendly AI chatbot built in Java.",
                        "You can call me JavaBot!"});

        addIntent("creator",
                new String[]{"who made you", "who created you", "who built you", "who is your developer"},
                new String[]{"I was built as a Java-based AI chatbot project.",
                        "A developer built me using rule-based logic and simple NLP techniques in Java."});

        addIntent("capabilities",
                new String[]{"what can you do", "help", "what do you do",
                        "how can you help me", "commands", "options"},
                new String[]{"I can chat with you, answer common questions, tell the time and date, " +
                        "do basic math, tell jokes, and even learn new answers if you teach me! " +
                        "Try typing: teach: <question> => <answer>"});

        addIntent("how_are_you",
                new String[]{"how are you", "how are you doing", "how is it going", "how do you feel"},
                new String[]{"I'm just a program, but I'm running smoothly! How are you?",
                        "Doing great, thanks for asking!"});

        addIntent("user_wellbeing_good",
                new String[]{"i am fine", "i am good", "i am great", "doing well",
                        "i am okay", "not bad", "pretty good"},
                new String[]{"Glad to hear that!", "That's great!"});

        addIntent("user_wellbeing_bad",
                new String[]{"i am sad", "i am not okay", "feeling low", "i am tired",
                        "i am stressed", "i am upset", "having a bad day"},
                new String[]{"I'm sorry to hear that. I hope things get better soon.",
                        "That sounds tough. Take care of yourself."});

        addIntent("joke",
                new String[]{"tell me a joke", "make me laugh", "say something funny", "joke please"},
                new String[]{"Why do programmers prefer dark mode? Because light attracts bugs!",
                        "Why did the Java developer wear glasses? Because they couldn't C#!",
                        "I would tell you a UDP joke, but you might not get it."});

        addIntent("compliment",
                new String[]{"you are smart", "you are helpful", "good job", "well done", "you are amazing"},
                new String[]{"Thank you, that's kind of you to say!", "I appreciate that!"});

        addIntent("what_is_ai",
                new String[]{"what is artificial intelligence", "define ai", "what is ai"},
                new String[]{"Artificial Intelligence is the field of computer science focused on building " +
                        "systems that can perform tasks that typically require human intelligence, like " +
                        "understanding language, recognizing patterns, and making decisions."});

        addIntent("what_is_java",
                new String[]{"what is java", "tell me about java programming"},
                new String[]{"Java is a popular, object-oriented programming language known for its " +
                        "portability across platforms thanks to the JVM."});

        addIntent("what_is_chatbot",
                new String[]{"what is a chatbot", "define chatbot"},
                new String[]{"A chatbot is a software application designed to simulate conversation with " +
                        "human users, often using rule-based logic or machine learning."});

        addIntent("weather",
                new String[]{"what is the weather", "weather today", "is it raining", "how is the weather"},
                new String[]{"I don't have live weather access right now, but a weather app or website " +
                        "will give you accurate, up-to-date info!"});

        addIntent("age",
                new String[]{"how old are you", "what is your age"},
                new String[]{"I don't age like humans do — I was just compiled recently!"});

        addIntent("insult_response",
                new String[]{"you are stupid", "you are dumb", "you are useless", "i hate you"},
                new String[]{"I'm sorry you feel that way. I'll try to do better.",
                        "Let's keep things friendly — how can I help you?"});
    }

    /** Loads any previously-taught question/answer pairs from disk. */
    private void loadTrainedIntents() {
        File f = new File(TRAINED_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|\\|", 2);
                if (parts.length == 2) {
                    addIntent("trained_" + (i++), new String[]{parts[0]}, new String[]{parts[1]});
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load trained data: " + e.getMessage());
        }
    }

    /** Adds a new question/answer pair at runtime and persists it for future sessions. */
    public void teach(String question, String answer) {
        addIntent("trained_" + intents.size(), new String[]{question}, new String[]{answer});
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(TRAINED_FILE, true))) {
                bw.write(question.replace("|", "/") + "||" + answer.replace("|", "/"));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Could not save trained data: " + e.getMessage());
        }
    }

    /** Returns the best-matching intent for the given keywords, or null if nothing scores highly enough. */
    public Intent findBestIntent(List<String> keywords) {
        Intent best = null;
        double bestScore = 0;
        for (Intent intent : intents) {
            double score = intent.scoreAgainst(keywords);
            if (score > bestScore) {
                bestScore = score;
                best = intent;
            }
        }
        return bestScore >= MATCH_THRESHOLD ? best : null;
    }
}
