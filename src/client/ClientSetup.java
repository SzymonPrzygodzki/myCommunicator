package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ClientSetup extends JFrame {

    private static final int SCREEN_WIDTH = 300;
    private static final int SCREEN_HEIGHT = 450;

    private ChatPanel chatPanel;
    private InputPanel inputPanel;
    private String hostname;
    private Socket connection;
    private PrintWriter outStream;
    private BufferedReader inStream;
    private String message;
    private String username = "";

    public ClientSetup(String hostname) {
        this.hostname = hostname;

        while (username.equals("")) {
            username = (String) JOptionPane.showInputDialog(null, "Enter your name", "Log in", JOptionPane.QUESTION_MESSAGE, new ImageIcon("images" + File.separator + "userIcon.png"),null, null);
            if (username == null) {
                System.exit(0);
            }
            if (username.equals("")) {
                JOptionPane.showMessageDialog(null, "Username field cannot be empty. Try again.", "Warning", JOptionPane.WARNING_MESSAGE, new ImageIcon("images" + File.separator + "warningIcon.png"));
            }
        }

        this.setTitle("Logged as: " + username);
        this.setBounds(new Rectangle(SCREEN_WIDTH, SCREEN_HEIGHT));

        inputPanel = new InputPanel();
        this.add(inputPanel, BorderLayout.PAGE_END);

        chatPanel = new ChatPanel(SCREEN_WIDTH - 20, SCREEN_HEIGHT - 70);
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        chatPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                scrollPane.getVerticalScrollBar().setValue(chatPanel.getScreenHeight());
            }
        });
        this.add(scrollPane, BorderLayout.PAGE_START);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sendMessage("User wants to disconnect");
                System.exit(0);
            }
        });

        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);
    }

    public void startRunning() {
        try {
            connectToServer();
            setupStreams();
            receiveMessage();
        } catch (IOException e) {
            showSystemMessage("Error occured: " + e.getMessage());
        } finally {
            closeStreams();
        }
    }

    private void connectToServer() throws IOException {
        showSystemMessage("Trying to connect to server...");
        connection = new Socket(InetAddress.getByName(hostname), 12008);
        showSystemMessage("Connected to: " + connection.getInetAddress().getHostName());
    }

    private void setupStreams() throws IOException {
        outStream = new PrintWriter(connection.getOutputStream(), true);
        inStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        showSystemMessage("I/O Streams established.");
    }

    private void receiveMessage() {
        while (true) {
            try {
                message = inStream.readLine();
                if (message.startsWith("Server:")) {
                    message = message.substring(message.indexOf(":") + 1);
                    showSystemMessage(message);
                } else {
                    int separator = message.indexOf(">");
                    String name = message.substring(0, separator);
                    message = message.substring(separator + 1);
                    chatPanel.addNextMsg(name, message);
                }
            } catch (IOException e) {
                showSystemMessage("Error occured while receiving a message.");
            }
        }
    }

    private void sendMessage(String msg) {
        try {
            outStream.println(username + ">" + msg);
            chatPanel.addNextMsg(username, msg);
        } catch (Exception e) {
            showSystemMessage("Error occured while sending a message.");
        }
    }

    private void showSystemMessage(String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chatPanel.addNextMsg("System:", msg);
            }
        });
    }

    private void closeStreams() {
        try {
            inStream.close();
            outStream.close();
            connection.close();
        } catch (IOException e) {
            showSystemMessage("Error while closing the streams: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ClientSetup clientSetup = new ClientSetup("2.tcp.ngrok.io");
        clientSetup.startRunning();
    }

    class ChatPanel extends JPanel implements ActionListener {

        private final static int DELAY = 100;
        private final static int OFFSET = 10;

        private ArrayList<Message> messages = new ArrayList<Message>();
        private int screenWidth;
        private int screenHeight;
        private Timer timer;
        private boolean running = false;

        public ChatPanel(int screenWidth, int screenHeight) {
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.setPreferredSize(new Dimension(screenWidth, screenHeight));
            this.setBackground(new Color(105, 181, 238));

            startChat();
        }

        public void startChat() {
            running = true;
            timer = new Timer(DELAY, this);
            timer.start();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawConversation(g);
        }

        public void drawConversation(Graphics g) {
            for (int i = 0; i < messages.size(); i++) {
                Message message = messages.get(i);
                if (message.getAuthor().equals("System:")) {
                    drawSystemMessage(g, i, message.getMsgPosX() + OFFSET, message.getMsgPosY());
                } else {
                    if (message.getAuthor() == username) {
                        drawUserMessage(g, i, screenWidth - message.getTextAreaWidth() - OFFSET, message.getMsgPosY() + OFFSET, new Color(19, 70, 7));
                    } else {
                        drawUserMessage(g, i, message.getMsgPosX() + OFFSET, message.getMsgPosY() + OFFSET, new Color(100, 0, 0));
                    }
                }
            }
        }

        public void drawSystemMessage(Graphics g, int index, int msgPosX, int msgPosY) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Comics Sans MS", Font.PLAIN, 10));
            int fontHeight = g.getFontMetrics().getHeight();

            g.drawString(messages.get(index).getText(), msgPosX, msgPosY + fontHeight);
        }

        public void drawUserMessage(Graphics g, int index, int msgPosX, int msgPosY, Color color) {
            g.setColor(new Color(185, 213, 185));
            Message message = messages.get(index);
            g.fillRoundRect(msgPosX, msgPosY, message.getTextAreaWidth(), message.getTextAreaHeight(), 10, 10);

            g.setFont(new Font("Comics Sans MS", Font.PLAIN, 14));
            int fontHeight2 = g.getFontMetrics().getHeight();

            String text = messages.get(index).getText();
            String[] lines = text.split("\n");
            for (int j = 0; j < lines.length; j++) {
                if (j == 0) {
                    g.setColor(color);
                } else {
                    g.setColor(Color.BLACK);
                }
                int x = msgPosX + 3;
                int y = msgPosY + fontHeight2;
                g.drawString(lines[j], x, y += j * fontHeight2);
            }
        }

        public void addNextMsg(String author, String msg) {
            String message = msg;

            if (!author.equals("System:")) {
                message = textFormat(msg);
            }
            if (messages.isEmpty()) {
                messages.add(new Message(author, message, 0, 0));
            } else {
                Message lastMsg = messages.get(messages.size() - 1);
                Message newMsg = new Message(author, message, lastMsg.getNextMsgPosX(), lastMsg.getNextMsgPosY() + OFFSET);
                messages.add(newMsg);
                extendChatPanel(newMsg.getNextMsgPosY() + 2 * OFFSET);
            }
        }

        public String textFormat(String msg) {
            String[] words = msg.split(" "); //limit 100 wyrazów
            String text = "";
            int counter = 0;

            for (int i = 0; i < words.length; i++) {
                String s = words[i];
                if (s.length() > 12) {
                    while (s.length() > 12) {
                        text += s.substring(0, 12) + "-" + "\n";
                        s = s.substring(12);
                    }
                    text += s + " ";
                    counter += s.length();
                } else {
                    text += words[i] + " ";
                    counter += words[i].length();
                }
                if (counter > 12) {
                    text += "\n";
                    counter = 0;
                }
            }
            return text.trim();
        }

        public void extendChatPanel(int newScreenHeight) {
            if (newScreenHeight > screenHeight) {
                this.screenHeight = newScreenHeight;
                this.setPreferredSize(new Dimension(screenWidth, screenHeight));
                revalidate();
            }
        }

        public int getScreenHeight() {
            return screenHeight;
        }
    }

    class InputPanel extends JPanel{
        private final static int USER_INPUT_COLUMNS = 15;
        private JTextField textField;
        private JButton sendButton;
        private String text;

        public InputPanel() {
            textField = new JTextField(USER_INPUT_COLUMNS);
            sendButton = new JButton("Send");
            add(textField);
            add(sendButton);

            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    text = textField.getText();
                    if (!text.isEmpty()) {
                        sendMessage(text);
                        textField.setText("");
                    }
                }
            });
        }
    }
}