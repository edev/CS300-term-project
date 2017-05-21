package com.dylanlaufenberg.portlandstate.cs300.gui;

import com.dylanlaufenberg.portlandstate.cs300.ClientController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * ChatScreen represents the main chat window visible to the user when the client is logged in.
 */
public class ChatScreen {
    private JFrame chatFrame;
    private JPanel rootPanel;
    private JList userList;
    private UserListModel userListModel;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    /**
     * Automatically called through IDEA's generated UI code.
     * Custom creation code goes here, corresponding to components for which "custom create" is set in ChatScreen.form.
     */
    private void createUIComponents() {
        userListModel = new UserListModel();
        userList = new JList<>(userListModel);
    }

    private enum SendMessageType {
        PUBLIC,
        PRIVATE
    }
    private SendMessageType sendType = SendMessageType.PUBLIC;
    private String privateMessageRecipient;

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
        chatArea.append(sender + ": " + text + "\n");
        if(!javax.swing.SwingUtilities.isEventDispatchThread()) {
            System.err.println("ChatScreen addPublicMessage NOT on Swing Event Dispatch Thread.");
        }
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

    /**
     * Called when the client receives a notice that a user has come online. Updates chat screen accordingly.
     * @param userName The name of the now-online user.
     */
    public void userAdded(String userName) {
        userListModel.add(userName);
    }

    /**
     * Called when the client receives a notice that a user has gone offline. Updates chat screen accordingly.
     * @param userName The name of the now-offline user.
     */
    public void userRemoved(String userName) { // FIXME Make userList remove correctly!
        userListModel.remove(userName);
    }

    private class UserListModel extends AbstractListModel<String> {
        private ArrayList<String> users;

        public UserListModel() {
            users = new ArrayList<>();
            users.add("Good morning, sunshine");
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
