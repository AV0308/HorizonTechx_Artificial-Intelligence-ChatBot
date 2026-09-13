import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

/**
 * Simple, dependency-free Swing GUI for AvChatBot.
 * Run with: java AvChatBotGUI
 */
public class ChatBotGUI extends JFrame {

    private final ChatEngine engine = new ChatEngine();
    private JTextPane chatPane;
    private StyledDocument doc;
    private JTextField inputField;

    private static final Color PRIMARY = new Color(0x2E5AAC);
    private static final Color BOT_COLOR = new Color(0x1B7F3B);
    private static final Color BG_COLOR = new Color(0xF4F6FA);

    public ChatBotGUI() {
        super("JavaBot - AI Chatbot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 640);
        setMinimumSize(new Dimension(360, 420));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildChatArea(), BorderLayout.CENTER);
        add(buildInputArea(), BorderLayout.SOUTH);

        appendMessage("AvChatBot", "Hi! I'm AvChatBot. Ask me anything, or tell me your name!", false);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(0, 56));
        JLabel title = new JLabel("AvChatBot \u2014 AI Chatbot");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(title);
        return header;
    }

    private JScrollPane buildChatArea() {
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setBackground(BG_COLOR);
        chatPane.setMargin(new Insets(10, 10, 10, 10));
        chatPane.setFont(new Font("SansSerif", Font.PLAIN, 14));
        doc = chatPane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(chatPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel buildInputArea() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.addActionListener(e -> handleSend());

        JButton sendButton = new JButton("Send");
        sendButton.setBackground(PRIMARY);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> handleSend());

        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        return panel;
    }

    private void handleSend() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        appendMessage("You", text, true);
        inputField.setText("");

        boolean exiting = engine.isExit(text);
        String response = engine.process(text);
        appendMessage("AvChatBot", response, false);

        if (exiting) {
            inputField.setEnabled(false);
        }
    }

    private void appendMessage(String sender, String message, boolean isUser) {
        try {
            SimpleAttributeSet nameStyle = new SimpleAttributeSet();
            StyleConstants.setForeground(nameStyle, isUser ? PRIMARY : BOT_COLOR);
            StyleConstants.setBold(nameStyle, true);
            doc.insertString(doc.getLength(), sender + ": ", nameStyle);

            SimpleAttributeSet bodyStyle = new SimpleAttributeSet();
            StyleConstants.setForeground(bodyStyle, Color.DARK_GRAY);
            doc.insertString(doc.getLength(), message + "\n\n", bodyStyle);

            chatPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatBotGUI().setVisible(true));
    }
}
