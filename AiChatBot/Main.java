import java.util.Scanner;

/**
 * Console interface for JavaBot. Run with: java Main
 */
public class Main {
    public static void main(String[] args) {
        ChatEngine engine = new ChatEngine();
        Scanner scanner = new Scanner(System.in);

        System.out.println("==========================================");
        System.out.println("   AvChatBot - AI Chatbot (Console Mode)");
        System.out.println("==========================================");
        System.out.println("Type 'exit' or 'quit' to end the chat.");
        System.out.println("Tip: teach me with -> teach: <question> => <answer>");
        System.out.println();
        System.out.println("AvChatBot: Hi! I'm AvChatBot. What's your name?");

        while (true) {
            System.out.print("You: ");
            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine();

            boolean exiting = engine.isExit(input);
            String response = engine.process(input);
            System.out.println("AvChatBot: " + response);

            if (exiting) break;
        }
        scanner.close();
    }
}
