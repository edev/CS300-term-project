package com.dylanlaufenberg.portlandstate.cs300.gui;

import com.dylanlaufenberg.portlandstate.cs300.ClientController;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * ChatScreen represents the main chat window visible to the user when the client is logged in.
 */
public class ChatScreen {
    // Text to be displayed on the label for the messageField (where the user types in messages)
    private final String publicMessageFieldLabel = "Public message"; // Message displayed verbatim
    private final String privateMessageFieldLabelPrefix = "Private message for "; // Message concatenated with user name

    // Number of clicks in userList to activate a private message to a recipient.
    private final int privateMessageClickCount = 2;

    // Key that the user can press in messageField to cancel a private message
    private final int privateMessageCancelKey = KeyEvent.VK_ESCAPE;

    // Public/private message switching.
    private enum SendMessageType {
        PUBLIC,
        PRIVATE
    }
    private SendMessageType sendType = SendMessageType.PUBLIC;
    private String privateMessageRecipient; // Should be null when not in use.

    // Swing elements
    private JFrame chatFrame;
    private JPanel rootPanel;
    private JList userList;
    private UserListModel userListModel;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JLabel messageFieldLabel;

    public ChatScreen() {
        initComponents();

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                if(message != null && !message.trim().equals("")) {
                    sendMessage(message);
                }
            }
        });
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                if(message != null && !message.trim().equals("")) {
                    sendMessage(message);
                }
            }
        });
        userList.addMouseListener(new MouseAdapter() {
            /**
             * Triggers private message composition when double-clicking on a name in the list.
             * @param e Mouse event.
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                // FIXME userList resizes on click
                // FIXME Since adding this and KeyListener below, users don't get removed from userList properly on logout.
                super.mouseClicked(e);
                if(e.getClickCount() >= privateMessageClickCount) {
                    String selected = (String) userList.getSelectedValue();
                    setPrivateMessageMode(selected);
                }
            }
        });
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                if(e.getKeyCode() == privateMessageCancelKey) {
                    setPublicMessageMode();
                }
            }
        });
    }

    private void initComponents() {
        JFrame frame = new JFrame("ChatScreen");
        frame.setContentPane(rootPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        chatFrame = frame;

        chatFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ClientController.shutdown();
                e.getWindow().dispose();
            }
        });

        frame.pack();
    }

    /**
     * Automatically called through IDEA's generated UI code.
     * Custom creation code goes here, corresponding to components for which "custom create" is set in ChatScreen.form.
     */
    private void createUIComponents() {
        userListModel = new UserListModel();
        userList = new JList<>(userListModel);
    }

    public void show() {
        if(!chatFrame.isVisible()) {
            chatFrame.setVisible(true);
        }
    }

    public void hide() {
        chatFrame.setVisible(false);
    }

    /**
     * Adds the public message to the chat screen, i.e. displays it in the main chat area.
     * @param sender The user name of the message's sender.
     * @param text The text of the message.
     */
    public void addPublicMessage(String sender, String text) {
        addMessage(sender + ": " + text);
        if(!javax.swing.SwingUtilities.isEventDispatchThread()) {
            System.err.println("ChatScreen addPublicMessage NOT on Swing Event Dispatch Thread.");
        }
    }

    /**
     * Adds the specified message to the chat area, plus a newline.
     * @param message The text to add (newline will be added automatically).
     */
    private void addMessage(String message) {
        chatArea.append(message + "\n");
    }

    private void sendMessage(String message) {
        if(message == null || message.trim().equals("")) {
            return;
        }

        if(sendType == SendMessageType.PRIVATE) {
            if(privateMessageRecipient != null) {
                // Send private message.
                // TODO Send private message and add to chat area
            } else {
                System.err.println("Tried to send a private message without specifying a recipient. Ignoring.");
            }
        } else if(sendType == SendMessageType.PUBLIC) {
            // Send public message.
            addPublicMessage(ClientController.userName, message);
            ClientController.sendPublicMessage(message);
        } else {
            System.err.println("ChatScreen.sendType is invalid! (Neither PUBLIC nor PRIVATE.)");
        }
    }

    private void setPrivateMessageMode(String recipient) {
        if(recipient == null || recipient.equals("")) {
            return;
        }

        privateMessageRecipient = recipient;
        sendType = SendMessageType.PRIVATE;
        messageFieldLabel.setText(privateMessageFieldLabelPrefix + recipient);
    }

    private void setPublicMessageMode() {
        privateMessageRecipient = null;
        sendType = SendMessageType.PUBLIC;
        messageFieldLabel.setText(publicMessageFieldLabel);
    }

    /**
     * Called when the client receives a notice that a user has come online. Updates chat screen accordingly.
     * @param userName The name of the now-online user.
     */
    public void userAdded(String userName) {
        userListModel.add(userName);
        addMessage(userName + " is now online.");
    }

    /**
     * Called when the client receives a notice that a user has gone offline. Updates chat screen accordingly.
     * @param userName The name of the now-offline user.
     */
    public void userRemoved(String userName) {
        userListModel.remove(userName);
        addMessage(userName + " is now offline.");
    }

    private class UserListModel extends AbstractListModel<String> {
        private ArrayList<String> users;

        public UserListModel() {
            users = new ArrayList<>();
        }

        public void add(String user) {
            if(user == null) {
                return;
            }

            users.add(user);
            users.sort(Comparator.naturalOrder());
            fireIntervalAdded(this, 0, users.size() - 1);
        }

        public void remove(String user) {
            if(user == null) {
                return;
            }

            users.remove(user);
            fireIntervalRemoved(this, 0, users.size() - 1);
        }

        @Override
        public int getSize() {
            return users.size();
        }

        @Override
        public String getElementAt(int index) {
            if(index < users.size()) {
                return users.get(index);
            } else {
                return null;
            }
        }
    }
}
