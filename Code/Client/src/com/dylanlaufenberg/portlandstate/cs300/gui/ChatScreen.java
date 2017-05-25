package com.dylanlaufenberg.portlandstate.cs300.gui;

import com.dylanlaufenberg.portlandstate.cs300.ClientController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * ChatScreen represents the main chat window visible to the user when the client is logged in.
 */
public class ChatScreen {

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

    // Swing labels
    private final String userListLabel = "Online users";
    private final String chatAreaLabel = "Chat area";
    private final String sendButtonText = "Send";
    // Text to be displayed on the label for the messageField (where the user types in messages)
    private final String publicMessageFieldLabel = "Public message"; // Message displayed verbatim
    private final String privateMessageFieldLabelPrefix = "Private message for "; // Message concatenated with user name

    // Window label. Format: prefix + " " + userName + " " + suffix. Use "" rather than null to blank out pre/suffix.
    private final String windowLabelPrefix = "Chat Application: logged in as";
    private final String windowLabelSuffix = "";

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
                super.mouseClicked(e);
                if(e.getClickCount() >= privateMessageClickCount) {
                    String selected = (String) userList.getSelectedValue();
                    setPrivateMessageMode(selected);
                    messageField.requestFocus();
                }
            }
        });
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyTyped(e);
                if(e.getKeyChar() == privateMessageCancelKey) {
                    setPublicMessageMode();
                }
            }
        });
        messageField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                messageField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
    }

    private void initComponents() {
        // Initialize frame.
        chatFrame = new JFrame("ChatScreen");
        chatFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
                ClientController.exit();
            }
        });
        chatFrame.setPreferredSize(new Dimension(800, 600));

        // Initialize menu
        initializeMenu();

        // Initialize elements.
        rootPanel = new JPanel();
        userListModel = new UserListModel();
        userList = new JList<>(userListModel);
        chatArea = new JTextArea();
        messageField = new JTextField();
        sendButton = new JButton(sendButtonText);
        messageFieldLabel = new JLabel(publicMessageFieldLabel); // Initialize to public send mode.

        // Set basic element properties.
        chatArea.setEditable(false);

        // GroupLayout (primarily to have decent gaps between things, which you'd think would be easy to achieve....)
        // First, create some locals we'll need here but don't need throughout the life of the frame.
        JLabel userListLabel, chatAreaLabel;
        JScrollPane userListPane, chatAreaPane;
        userListPane = new JScrollPane(userList);
        chatAreaPane = new JScrollPane(chatArea);
        userListLabel = new JLabel(this.userListLabel);
        chatAreaLabel = new JLabel(this.chatAreaLabel);
        Dimension userListPreferredSize = new Dimension(120, 560);
        Dimension userListMaximumSize = new Dimension(200, Integer.MAX_VALUE);
        Dimension chatAreaPreferredSize = new Dimension(560, 560);
        Dimension chatAreaMaximumSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

        // Initialize primary layout.
        GroupLayout mainLayout = new GroupLayout(rootPanel);
        rootPanel.setLayout(mainLayout);
        mainLayout.setAutoCreateGaps(true);
        mainLayout.setAutoCreateContainerGaps(true);

        // Now, create two panels, which will have their own layouts.
        JPanel topPane = new JPanel();
        JPanel bottomPane = new JPanel();
        GroupLayout topPaneLayout = new GroupLayout(topPane);
        GroupLayout bottomPaneLayout = new GroupLayout(bottomPane);
        topPane.setLayout(topPaneLayout);
        bottomPane.setLayout(bottomPaneLayout);
        topPaneLayout.setAutoCreateGaps(true);
//        topPaneLayout.setAutoCreateContainerGaps(true);
        bottomPaneLayout.setAutoCreateGaps(true);
//        bottomPaneLayout.setAutoCreateContainerGaps(true);

        // Next, add all components to the panes where they belong, before we specify layout.
        chatFrame.setContentPane(rootPanel);
        rootPanel.add(topPane);
        rootPanel.add(bottomPane);
        topPane.add(userListLabel);
        topPane.add(userListPane);
        topPane.add(chatAreaLabel);
        topPane.add(chatAreaPane);
        bottomPane.add(messageFieldLabel);
        bottomPane.add(messageField);
        bottomPane.add(sendButton);

        // Add topPane and bottomPane to rootPanel.
        mainLayout.setVerticalGroup(
                mainLayout.createSequentialGroup()
                        .addComponent(topPane)
                        .addComponent(bottomPane)
        );
        mainLayout.setHorizontalGroup(
                mainLayout.createParallelGroup()
                        .addComponent(topPane)
                        .addComponent(bottomPane)
        );

        // Configure topPane, which holds the user list and the chat area, plus their labels.
        topPaneLayout.setVerticalGroup(
                topPaneLayout.createSequentialGroup()
                        .addGroup(topPaneLayout.createParallelGroup()
                                .addComponent(userListLabel)
                                .addComponent(chatAreaLabel))
                        .addGroup((topPaneLayout.createParallelGroup()
                                .addComponent(userListPane, 0, userListPreferredSize.height, userListMaximumSize.height)
                                .addComponent(chatAreaPane, 0, chatAreaPreferredSize.height, chatAreaMaximumSize.height)))
        );
        topPaneLayout.setHorizontalGroup(
                topPaneLayout.createSequentialGroup()
                        .addGroup(topPaneLayout.createParallelGroup()
                                .addComponent(userListLabel)
                                .addComponent(userListPane, userListPreferredSize.width, userListPreferredSize.width, userListMaximumSize.width))
                        .addGroup(topPaneLayout.createParallelGroup()
                                .addComponent(chatAreaLabel)
                                .addComponent(chatAreaPane, 0, chatAreaPreferredSize.width, chatAreaMaximumSize.width))
        );

        // Configure bottomPane, which holds message field and label plus send button.
        bottomPaneLayout.setVerticalGroup(
                bottomPaneLayout.createSequentialGroup()
                        .addComponent(messageFieldLabel)
                        .addGroup(bottomPaneLayout.createParallelGroup()
                                .addComponent(messageField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(sendButton))
        );
        bottomPaneLayout.setHorizontalGroup(
                bottomPaneLayout.createParallelGroup()
                        .addComponent(messageFieldLabel)
                        .addGroup(bottomPaneLayout.createSequentialGroup()
                                .addComponent(messageField)
                                .addComponent(sendButton))
        );



/*      // BoxLayout - again, seems to be basically useless. Gaps don't do a damn thing, at least not reliably.
        // First, create some panes to use.
        JPanel topPane, bottomPane,leftPane, rightPane, bottomLeftPane;
        topPane = new JPanel();
        bottomPane = new JPanel();
        leftPane = new JPanel();
        rightPane = new JPanel();
        bottomLeftPane = new JPanel();

        // Next, set the panes' layouts. (Can't do this in constructor because of BoxLayout constructor. Ugh.)
        rootPanel.setLayout(        new BoxLayout(rootPanel,        BoxLayout.PAGE_AXIS));
        topPane.setLayout(          new BoxLayout(topPane,          BoxLayout.LINE_AXIS));
        bottomPane.setLayout(       new BoxLayout(bottomPane,       BoxLayout.LINE_AXIS));
        leftPane.setLayout(         new BoxLayout(leftPane,         BoxLayout.PAGE_AXIS));
        rightPane.setLayout(        new BoxLayout(rightPane,        BoxLayout.PAGE_AXIS));
        bottomLeftPane.setLayout(   new BoxLayout(bottomLeftPane,   BoxLayout.PAGE_AXIS));

        // Now, to create some required local references to components we don't need after initialization....
        JLabel userListLabel, chatAreaLabel;
        JScrollPane userListPane, chatAreaPane;
        userListPane = new JScrollPane(userList);
        chatAreaPane = new JScrollPane(chatArea);
        userListLabel = new JLabel(this.userListLabel);
        chatAreaLabel = new JLabel(this.chatAreaLabel);
        Dimension outerGapDimension = new Dimension(5, 5); // Between components and frame
        Component outerGap = Box.createRigidArea(outerGapDimension);
        Dimension innerGapDimension = new Dimension(10, 10); // Between components
        Component innerGap = Box.createRigidArea(innerGapDimension);

        // Then, add the panes to one another. Apologies for the fillers. BoxLayout is REALLY stupid.
        rootPanel.add(topPane);
        rootPanel.add(innerGap);
        rootPanel.add(bottomPane);
        topPane.add(leftPane);
        topPane.add(innerGap);
        topPane.add(rightPane);
        bottomPane.add(bottomLeftPane);

        // Add components to panes - almost done! Apologies for the fillers - BoxLayout is REALLY stupid.
        chatFrame.setContentPane(rootPanel);
        leftPane.add(userListLabel);
        leftPane.add(userListPane);
        rightPane.add(chatAreaLabel);
        rightPane.add(chatAreaPane);
        bottomLeftPane.add(messageFieldLabel);
        bottomLeftPane.add(messageField);
        bottomPane.add(innerGap);
        bottomPane.add(sendButton);

        // Finally, size components.
        userListLabel.setAlignmentX((Component.LEFT_ALIGNMENT));
        userListPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        userListPane.setPreferredSize(new Dimension(120, 560));
        chatAreaPane.setPreferredSize(new Dimension(660, 560));
*/

/*      // GridBagLayout - not working adequately for my layout without hacks, it seems.
        // Set up constraints.
        final double x0weight = 0.2;
        final double x1weight = 0.8;
        final double y0weight = 0.9;
        final int inset = 8;
        final int gridWidth = 4;
        GridBagConstraints userListConstraints, chatAreaConstraints, messageFieldConstraints, sendButtonConstraints, spacerConstraints;
        userListConstraints = new GridBagConstraints();
        userListConstraints.gridx = 0;
        userListConstraints.gridy = 0;
        userListConstraints.fill = GridBagConstraints.BOTH;
        userListConstraints.weightx = x0weight;
        userListConstraints.weighty = y0weight;
        userListConstraints.insets = new Insets(inset, inset, inset/2, inset/2);
        chatAreaConstraints = new GridBagConstraints();
        chatAreaConstraints.gridx = 1;
        chatAreaConstraints.gridy = 0;
        chatAreaConstraints.gridwidth = gridWidth;
        chatAreaConstraints.fill = GridBagConstraints.BOTH;
        chatAreaConstraints.weightx = x1weight;
        chatAreaConstraints.weighty = y0weight;
        chatAreaConstraints.insets = new Insets(inset, inset/2, inset/2, inset);

        // If we use this, need to Set a span of some kind to achieve desired result.

        messageFieldConstraints = new GridBagConstraints();
        messageFieldConstraints.gridx = 0;
        messageFieldConstraints.gridy = 1;
        messageFieldConstraints.gridwidth = gridWidth;
        messageFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        messageFieldConstraints.weightx = 0.9;
        messageFieldConstraints.insets = new Insets(inset/2, inset, inset, inset/2);
        sendButtonConstraints = new GridBagConstraints();
        sendButtonConstraints.gridx = GridBagConstraints.RELATIVE;
        sendButtonConstraints.gridy = 1;
        sendButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
        sendButtonConstraints.insets = new Insets(inset/2, inset/2, inset, inset);

        // If we use this, need to add constraints for the labels, too, if we need them.

        // Add elements to frame/pane.
        chatFrame.setContentPane(rootPanel);
//        rootPanel.add(new JLabel(userListLabel), userListConstraints);
        rootPanel.add(userList, userListConstraints); // NEED TO Add userList inside a JScrollPane!
//        rootPanel.add(new JLabel(chatAreaLabel), chatAreaConstraints);
        rootPanel.add(chatArea, chatAreaConstraints); // NEED TO ADD chatArea inside a JScrollPane!
//        rootPanel.add(messageFieldLabel, messageFieldConstraints);
        rootPanel.add(messageField, messageFieldConstraints);
//        rootPanel.add(sendButton, sendButtonConstraints);
*/

        chatFrame.pack();
        messageField.requestFocus();
    }

    private void initializeMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu chatMenu = new JMenu("Chat");
        chatMenu.setMnemonic(KeyEvent.VK_C);
        menuBar.add(chatMenu);

        JMenuItem menuHistory = new JMenuItem("View History", KeyEvent.VK_H);
        chatMenu.add(menuHistory);
        chatMenu.add(new JSeparator());
        JMenuItem menuLogout = new JMenuItem("Log out", KeyEvent.VK_L);
        chatMenu.add(menuLogout);
        JMenuItem menuExit = new JMenuItem("Quit", KeyEvent.VK_Q);
        chatMenu.add(menuExit);

        chatFrame.setJMenuBar(menuBar);

        // Now add listeners.
        menuHistory.addActionListener(e -> ClientController.showLog());
        menuLogout.addActionListener(e -> ClientController.goOffline());
        menuExit.addActionListener(e -> ClientController.exit());
    }

    public void show() {
        // Update title.
        StringBuilder title = new StringBuilder();
        if(windowLabelPrefix != null) {
            title.append(windowLabelPrefix);
            title.append(" ");
        }
        title.append(ClientController.userName);
        if(windowLabelSuffix != null) {
            title.append(" ");
            title.append(windowLabelSuffix);
        }
        chatFrame.setTitle(title.toString());

        // Update visibility if needed.
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

    public void addPrivateMessage(String sender, String text, boolean outgoing) {
        if(outgoing) {
            addMessage("Private message to " + sender + ": " + text);
        } else {
            addMessage("Private message from " + sender + ": " + text);
        }
        if(!javax.swing.SwingUtilities.isEventDispatchThread()) {
            System.err.println("ChatScreen addPrivateMessage NOT on Swing Event Dispatch Thread.");
        }
    }

    /**
     * Adds the specified message to the chat area, plus a newline.
     * @param message The text to add (newline will be added automatically).
     */
    private void addMessage(String message) {
        chatArea.append(message + "\n");
        ClientController.log(message + "\n");
    }

    private void sendMessage(String message) {
        if(message == null || message.trim().equals("")) {
            return;
        }

        if(sendType == SendMessageType.PRIVATE) {
            if(privateMessageRecipient != null && !privateMessageRecipient.trim().equals("")) {
                // Send private message.
                ClientController.sendPrivateMessage(privateMessageRecipient, message);
                addPrivateMessage(ClientController.userName, message, true);
                messageField.setText("");
            } else {
                System.err.println("Tried to send a private message without specifying a recipient. Ignoring.");
            }
        } else if(sendType == SendMessageType.PUBLIC) {
            // Send public message.
            addPublicMessage(ClientController.userName, message);
            ClientController.sendPublicMessage(message);
            messageField.setText("");
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
            fireIntervalAdded(this, 0, users.size());
        }

        public void remove(String user) {
            if(user == null) {
                return;
            }

            users.remove(user);
            fireIntervalRemoved(this, 0, users.size());
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
