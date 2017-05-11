package com.dylanlaufenberg.portlandstate.cs300.gui;

import com.dylanlaufenberg.portlandstate.cs300.ChatApplication;
import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Dylan on 5/10/2017.
 */
public class LoginScreen {
    private JPasswordField passwordField;
    private JTextField hostnameField;
    private JTextField portField; // TODO Restrict port field to only accept numbers.
    private JTextField userNameField;
    private JButton registerButton;
    private JButton loginButton;
    private JLabel userNameLabel;
    private JLabel passwordLabel;
    private JLabel hostnameLabel;
    private JLabel portLabel;
    private JPanel rootPanel;

    public LoginScreen() {
        loginButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("LoginScreen");
        frame.setContentPane(new LoginScreen().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static LoginScreen createAndShow() {
        LoginScreen ls = new LoginScreen();
        JFrame frame = new JFrame("LoginScreen");
        frame.setContentPane(ls.rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        return ls;
    }

    private void doLogin() {
        // Get everything we need
        String hostname = hostnameField.getText();
        int port = 0;
        try {
            port = Integer.valueOf(portField.getText());
        } catch (NumberFormatException e) {
            // Detected error: port is not a number.
        }
        String userName = userNameField.getText();
        String password = String.valueOf(passwordField.getPassword());

        // If anything is missing, error and don't proceed
        // TODO Add an error label and use it!
        boolean valid = true;
        if (userName.length() == 0) {
            // Detected error.
            valid = false;
        } else if (password.length() == 0) {
            // Detected error.
            valid = false;
        } else if (hostname.length() == 0) {
            // Detected error.
            valid = false;
        } else if (port == 0) {
            valid = false;
        }

        if (valid) {
            // We have all the information we need, so log in (or try).
            ChatApplication.login(hostname, port, userName, password);
        }
    }
}