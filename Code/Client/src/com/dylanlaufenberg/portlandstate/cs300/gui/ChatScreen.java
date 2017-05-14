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
    private JTextField messageField;
    private JButton sendButton;


    private ChatScreen() {
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    public static ChatScreen createAndShow() {
        ChatScreen cs = new ChatScreen();
        JFrame frame = new JFrame("ChatScreen");
        frame.setContentPane(new ChatScreen().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        cs.chatFrame = frame;

        cs.chatFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ClientController.shutdown();
                e.getWindow().dispose();
            }
        });

        return cs;
    }

    public void close() {
        chatFrame.setVisible(false);
        chatFrame.dispose();
    }

}
