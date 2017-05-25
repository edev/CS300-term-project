package com.dylanlaufenberg.portlandstate.cs300.gui;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Serves one and only one purpose: to show the chat history of a specified user.
 */
public class ChatHistoryScreen {
    private JFrame frame;
    private JPanel rootPanel;
    private JTextArea textArea;

    private ChatHistoryScreen(String userName) {
        frame = new JFrame("Local chat history for " + userName);
        rootPanel = new JPanel();

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(rootPanel);
        frame.setPreferredSize(new Dimension(600, 400));

        GroupLayout layout = new GroupLayout(rootPanel);
        rootPanel.setLayout(layout);

        textArea = new JTextArea();
        JScrollPane textAreaScrollPane = new JScrollPane(textArea);
        rootPanel.add(textAreaScrollPane);

        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(textAreaScrollPane));
        layout.setVerticalGroup(layout.createSequentialGroup().addComponent(textAreaScrollPane));

        frame.pack();
        frame.setVisible(true);
    }

    public static ChatHistoryScreen screenFor(File logFile, String userName) {
        ChatHistoryScreen chs = new ChatHistoryScreen(userName);

        try {
            FileReader reader = new FileReader(logFile);
            BufferedReader bufferedReader = new BufferedReader(reader);
            chs.textArea.read(bufferedReader, null);
            bufferedReader.close();
        } catch(IOException e) {
            System.err.println(e.toString());
            chs.textArea.setText(e.toString());
        }

        return chs;
    }
}
