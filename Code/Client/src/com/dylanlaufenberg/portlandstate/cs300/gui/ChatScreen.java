package com.dylanlaufenberg.portlandstate.cs300.gui;

import javax.swing.*;

/**
 * Created by Dylan on 5/12/2017.
 */
public class ChatScreen {
    private JFrame chatFrame;
    private JPanel rootPanel;
    private JList userList;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;


    public static ChatScreen createAndShow() {
        ChatScreen cs = new ChatScreen();
        JFrame frame = new JFrame("ChatScreen");
        frame.setContentPane(new ChatScreen().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        cs.chatFrame = frame;
        return cs;
    }

    public void close() {
        chatFrame.setVisible(false);
        chatFrame.dispose();
    }

}
