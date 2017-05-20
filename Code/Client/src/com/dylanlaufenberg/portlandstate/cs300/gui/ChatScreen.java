package com.dylanlaufenberg.portlandstate.cs300.gui;

import com.dylanlaufenberg.portlandstate.cs300.ClientController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by Dylan on 5/12/2017.
 */
public class ChatScreen {
    private JFrame chatFrame;
    private JPanel rootPanel;
    private JList userList;
    private JTextArea chatArea;
    private JScrollPane chatAreaScrollPane;
    private JTextField messageField;
    private JButton sendButton;

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
        // chatAreaScrollPane = new JScrollPane(chatArea);
    }

    public void show() {
        JFrame frame = new JFrame("ChatScreen");
        frame.setContentPane(new ChatScreen().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame = frame;

        chatFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ClientController.shutdown();
                e.getWindow().dispose();
            }
        });

        frame.pack();
        frame.setVisible(true);
    }

    public void hide() {
        chatFrame.setVisible(false);
    }

    public void close() {
        chatFrame.setVisible(false);
        chatFrame.dispose();
    }

    /**
     * Adds the public message to the chat screen, i.e. displays it in the main chat area.
     * @param sender The user name of the message's sender.
     * @param text The text of the message.
     */
    public void addPublicMessage(String sender, String text) {
        chatArea.append(sender + ": " + text + "\n"); // FIXME Receiving messages doesn't work properly! Pretty sure it's because it happens in a Netty thread!
        if(javax.swing.SwingUtilities.isEventDispatchThread()) {
            System.out.println("addPublicMessage on Swing EDT.");
        } else {
            System.out.println("addPublicMessage NOT on Swing EDT.");
        }
        System.out.println("Added public message to chat area:\t\t" + sender + ": " + text);
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

}
