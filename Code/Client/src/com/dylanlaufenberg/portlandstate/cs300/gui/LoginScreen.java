package com.dylanlaufenberg.portlandstate.cs300.gui;

import com.dylanlaufenberg.portlandstate.cs300.ClientController;

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
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doRegister();
            }
        });
        userNameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
        hostnameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
        portField.addActionListener(new ActionListener() {
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
        LoginData d = getLoginData();
        if(d != null) {
            ClientController.login(d.hostname, d.port, d.userName, d.password);
        } else {
            // TODO Add error message here or in getLoginData.
        }
    }

    private void doRegister() {
        LoginData d = getLoginData();
        if(d != null) {
            ClientController.register(d.hostname, d.port, d.userName, d.password);
        } else {
            // TODO Add error message here or in getLoginData.
        }
    }

    public LoginData getLoginData() {
        LoginData d = new LoginData();

        // Get everything we need
        d.hostname = hostnameField.getText();
        d.port = 0;
        try {
            d.port = Integer.valueOf(portField.getText());
        } catch (NumberFormatException e) {
            // Detected error: port is not a number.
        }
        d.userName = userNameField.getText();
        d.password = String.valueOf(passwordField.getPassword());

        // If anything is missing, error and don't proceed
        // TODO Add an error label and use it!
        boolean valid = true;
        if (d.userName.length() == 0) {
            // Detected error.
            valid = false;
        } else if (d.password.length() == 0) {
            // Detected error.
            valid = false;
        } else if (d.hostname.length() == 0) {
            // Detected error.
            valid = false;
        } else if (d.port == 0) {
            valid = false;
        }

        if (valid) {
            // We have all the information we need, so log in (or try).
            return d;
        } else {
            return null;
        }
    }

    private class LoginData {
        public String hostname, userName, password;
        public int port;
    }
}