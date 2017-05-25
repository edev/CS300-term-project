package com.dylanlaufenberg.portlandstate.cs300.gui;

import com.dylanlaufenberg.portlandstate.cs300.ClientController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Dylan on 5/10/2017.
 */
public class LoginScreen {
    // TODO Implement http://stackoverflow.com/questions/1313390/is-there-any-way-to-accept-only-numeric-values-in-a-jtextfield
    private JFrame loginFrame;
    private JPasswordField passwordField;
    private JTextField hostnameField;
    private JTextField portField; // TODO Restrict port field to only accept numbers.
    private JTextField userNameField;
    private JButton registerButton;
    private JButton loginButton;
    private JPanel rootPanel;
    private JLabel statusText;
    private Operation currentOperation;
    // TODO Add a focusListener to select all in all 4 fields.
    private enum Operation {
        NONE,
        LOGIN,
        REGISTER
    }

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
        userNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                routeLoginEvent(e);
            }
        });
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                routeLoginEvent(e);
            }
        });

        hostnameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                routeLoginEvent(e);
            }
        });
        portField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                routeLoginEvent(e);
            }
        });

    }

    private void routeLoginEvent(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            if ((e.getModifiers() & InputEvent.SHIFT_MASK) == 0) {
                // No shift. Login.
                doLogin();
            } else {
                // Shift. Register.
                doRegister();
            }
        }
    }

    public static LoginScreen createAndShow() {
        LoginScreen ls = new LoginScreen();
        JFrame frame = new JFrame("LoginScreen");
        frame.setContentPane(ls.rootPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        ls.loginFrame = frame;
        ls.currentOperation = Operation.NONE;
        return ls;
    }

    public void show() {
        if(!loginFrame.isVisible()){
            loginFrame.setVisible(true);
        }
    }

    private void doLogin() {
        LoginData d = getLoginData();
        if(d != null) {
            currentOperation = Operation.LOGIN;
            ClientController.login(d.hostname, d.port, d.userName, d.password);
        }
    }

    private void doRegister() {
        LoginData d = getLoginData();
        if(d != null) {
            currentOperation = Operation.REGISTER;
            ClientController.register(d.hostname, d.port, d.userName, d.password);
        }
    }

    private LoginData getLoginData() {
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
        boolean valid = true;
        if (d.userName.length() == 0) {
            // Detected error.
            setErrorText("Please specify your user name.");
            valid = false;
        } else if (d.password.length() == 0) {
            // Detected error.
            setErrorText("Please specify your password.");
            valid = false;
        } else if (d.hostname.length() == 0) {
            // Detected error.
            setErrorText("Please specify a hostname or IP address.");
            valid = false;
        } else if (d.port == 0 || d.port > 65535) {
            setErrorText("Port must be a number between 1 and 65535.");
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

    public void showUserErrorMessage() {
        switch(currentOperation) {
            case LOGIN:
                setErrorText("That user does not exist.");
                break;

            case REGISTER:
                setErrorText("That user already exists.");
                break;

            case NONE:
                break;
        }
        currentOperation = Operation.NONE;
    }

    public void showPasswordErrorMessage() {
        switch(currentOperation) {
            case LOGIN:
                setErrorText("Incorrect password.");
                break;

            case REGISTER:
                setErrorText("Invalid password.");
                break;

            case NONE:
                break;
        }
        currentOperation = Operation.NONE;
    }

    /**
     * Displays the given error message (presumably a network connection failure, etc.) and clears any interal record
     * of an in-progress operation.
     * @param text The (SHORT!) error text to display.
     */
    public void showErrorMessage(String text) {
        setErrorText(text);
        currentOperation = Operation.NONE;
    }

    public void clearErrorMessage() {
        setErrorText("");
    }

    private void setErrorText(String text) {
        statusText.setForeground(Color.red);
        statusText.setText("<html>" + text + "</html>");
    }

    public void hide() {
        currentOperation = Operation.NONE;
        loginFrame.setVisible(false);
    }
}