package ma.fstm.ilisi.realtimechat.client;

import ma.fstm.ilisi.realtimechat.common.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ChatGUI extends JFrame {
    private final Map<User, JTextArea> chatAreas;
    private final Map<User, JPanel> chatPanels;
    private final JTextField messageInput;
    private final JList<User> userList;
    private final DefaultListModel<User> userListModel;
    private final CardLayout cardLayout;
    private final JPanel chatContainer;
    private final IChatController controller;
    private User selectedUser;

    public ChatGUI() {
        chatAreas = new HashMap<>();
        chatPanels = new HashMap<>();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.controller = new ChatController(this);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to initialize chat: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }

        setTitle("Secure Private Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 500));

        // Card layout for chat panels
        cardLayout = new CardLayout();
        chatContainer = new JPanel(cardLayout);

        // Welcome panel
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        JLabel welcomeLabel = new JLabel("Select a user to start chatting");
        welcomeLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        welcomePanel.add(welcomeLabel);
        chatContainer.add(welcomePanel, "welcome");

        messageInput = new JTextField();
        messageInput.setFont(new Font("Dialog", Font.PLAIN, 14));

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setCellRenderer(new UserListCellRenderer());

        setupLayout();
        setupEventHandlers();

        messageInput.setEnabled(false);
        setLocationRelativeTo(null);
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(248, 249, 250));

        // User list panel
        JPanel userListPanel = new JPanel(new BorderLayout(0, 0));
        userListPanel.setPreferredSize(new Dimension(250, 0));
        userListPanel.setBackground(Color.WHITE);
        userListPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(222, 226, 230)));

        JLabel userListLabel = new JLabel(" Users");
        userListLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        userListLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(222, 226, 230)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        userList.setBackground(Color.WHITE);
        userList.setBorder(null);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(null);

        userListPanel.add(userListLabel, BorderLayout.NORTH);
        userListPanel.add(userScrollPane, BorderLayout.CENTER);

        // Chat panel
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setBackground(new Color(248, 249, 250));

        // Message input area
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)),
                new EmptyBorder(15, 15, 15, 15)
        ));
        inputPanel.setBackground(new Color(248, 249, 250));

        messageInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JButton sendButton = new JButton("Send");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(new Color(0, 123, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> sendMessage());
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        rightPanel.add(chatContainer, BorderLayout.CENTER);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        mainPanel.add(userListPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createChatPanel(User user) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(248, 249, 250));

        // Chat area
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230), 1));

        // Add to maps
        chatAreas.put(user, chatArea);
        chatPanels.put(user, panel);

        // Chat header
        JPanel headerPanel = getHeaderPanel(user);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private static JPanel getHeaderPanel(User user) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 249, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(222, 226, 230)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel headerLabel = new JLabel("Chat with " + user.getUsername());
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerLabel.setForeground(new Color(33, 37, 41));

        JLabel statusLabel = new JLabel(user.isOnline() ? "Online" : "Offline");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(user.isOnline() ? new Color(40, 167, 69) : new Color(108, 117, 125));

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        headerRight.setBackground(headerPanel.getBackground());
        headerRight.add(statusLabel);

        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);
        return headerPanel;
    }

    private void setupEventHandlers() {
        messageInput.addActionListener(e -> sendMessage());

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                User selected = userList.getSelectedValue();
                if (selected != null) {
                    selectedUser = selected;
                    messageInput.setEnabled(true);
                    messageInput.requestFocus();

                    // Create a chat panel if it doesn't exist
                    if (!chatPanels.containsKey(selected)) {
                        JPanel chatPanel = createChatPanel(selected);
                        chatContainer.add(chatPanel, selected.getUsername());
                    }

                    // Show the selected user's chat panel
                    cardLayout.show(chatContainer, selected.getUsername());
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.disconnect();
            }
        });
    }

    private void sendMessage() {
        if (selectedUser != null && !messageInput.getText().trim().isEmpty()) {
            try {
                String message = messageInput.getText().trim();
                controller.sendMessage(message, selectedUser);
                displayMessage("You: " + message, selectedUser);
                messageInput.setText("");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to send message: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void displayMessage(String message, User chatPartner) {
        JTextArea chatArea = chatAreas.get(chatPartner);
        String timestamp = String.format("[%tT] ", new Date());

        if (chatArea != null) {
            chatArea.append(timestamp + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        } else {
            JPanel chatPanel = createChatPanel(chatPartner);
            chatContainer.add(chatPanel, chatPartner.getUsername());
            chatAreas.get(chatPartner).append(timestamp + message + "\n");
            cardLayout.show(chatContainer, chatPartner.getUsername());
        }
    }

    public void updateUserList(List<User> users) {
        userListModel.clear();
        users.forEach(userListModel::addElement);
    }

    public void initialize(String username) {
        try {
            controller.initialize(username);
            setTitle("Secure Chat - " + username);
            setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to connect to server: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private static class UserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setOpaque(true);

            if (value instanceof User user) {
                JLabel nameLabel = new JLabel(user.getUsername());
                nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

                JLabel statusLabel = new JLabel(user.isOnline() ? "● Online" : "○ Offline");
                statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                statusLabel.setForeground(user.isOnline() ?
                        new Color(40, 167, 69) : new Color(108, 117, 125));

                panel.add(nameLabel, BorderLayout.CENTER);
                panel.add(statusLabel, BorderLayout.EAST);

                if (isSelected) {
                    panel.setBackground(new Color(236, 240, 245));
                } else {
                    panel.setBackground(Color.WHITE);
                }

                panel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            }

            return panel;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String username = JOptionPane.showInputDialog(null,
                    "Enter your username:",
                    "Login",
                    JOptionPane.PLAIN_MESSAGE);

            if (username != null && !username.trim().isEmpty()) {
                ChatGUI chatGUI = new ChatGUI();
                chatGUI.initialize(username.trim());
            }
        });
    }
}