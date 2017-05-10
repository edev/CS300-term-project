package com.dylanlaufenberg.portlandstate.cs300.gui;

import javax.swing.*;

/**
 * Created by Dylan on 5/10/2017.
 */
public class LoginScreen {
    private JPasswordField passwordField;
    private JTextField hostnameField;
    private JTextField portField;
    private JTextField userNameField;
    private JButton registerButton;
    private JButton loginButton;
    private JLabel userNameLabel;
    private JLabel passwordLabel;
    private JLabel hostnameLabel;
    private JLabel portLabel;
    private JPanel rootPanel;

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
}
