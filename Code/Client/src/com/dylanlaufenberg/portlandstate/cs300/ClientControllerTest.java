package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.gui.ChatScreen;
import com.dylanlaufenberg.portlandstate.cs300.gui.LoginScreen;
import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for ClientController class.
 */
public class ClientControllerTest {

    @BeforeClass
    public static void stubGUI() {
        ClientController.loginScreen = LoginScreen.createAndShow();
        ClientController.chatScreen = new ChatScreen();
    }

    @Test
    public void processMessage() throws Exception {
        class TestCase {
            String description;
            NetMessage.Message message;
            boolean result;

            public TestCase(String description, NetMessage.Message message, boolean result) {
                this.description = description;
                this.message = message;
                this.result = result;
            }
        }

        TestCase[] tests = {
                // AUTH TESTS
                new TestCase(
                        "Auth Success",
                        ServerHelper.buildAuthResponseMessage(
                                NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_SUCCESS),
                        true
                ),
                new TestCase(
                        "Auth Error - User",
                        ServerHelper.buildAuthResponseMessage(
                                NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_ERROR_USER),
                        false
                ),
                new TestCase(
                        "Auth Error - Password",
                        ServerHelper.buildAuthResponseMessage(
                                NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_ERROR_PASSWORD),
                        false
                ),
                new TestCase(
                        "Auth Error - Other",
                        ServerHelper.buildAuthResponseMessage(
                                NetMessage.Message.AuthenticationMessage.AuthMessageType.UNSET),
                        false
                ),

                // NOTICE TESTS
                new TestCase(
                        "Notice - Online",
                        ServerHelper.buildNoticeMessage(
                                NetMessage.Message.NoticeMessage.NoticeMessageType.ONLINE,
                                "John"),
                        true
                ),
                new TestCase(
                        "Notice - Offline",
                        ServerHelper.buildNoticeMessage(
                                NetMessage.Message.NoticeMessage.NoticeMessageType.OFFLINE,
                                "John"),
                        true
                ),
                new TestCase(
                        "Notice - Other",
                        ServerHelper.buildNoticeMessage(
                                NetMessage.Message.NoticeMessage.NoticeMessageType.UNSET,
                                "John"),
                        false
                ),

                // CHAT MESSAGE TESTS
                new TestCase(
                        "Chat - Public",
                        ServerHelper.buildChatResponseMessage(
                                NetMessage.Message.ChatMessage.ChatMessageType.PUBLIC,
                                "Joe",
                                "Hello there"
                        ),
                        true
                ),
                new TestCase(
                        "Chat - Private",
                        ServerHelper.buildChatResponseMessage(
                                NetMessage.Message.ChatMessage.ChatMessageType.PRIVATE,
                                "Joe",
                                "Hello there"
                        ),
                        true
                ),
                new TestCase(
                        "Chat - Other",
                        ServerHelper.buildChatResponseMessage(
                                NetMessage.Message.ChatMessage.ChatMessageType.UNSET,
                                "Joe",
                                "Hello there"
                        ),
                        false
                ),

                // USER LIST TESTS
                new TestCase(
                        "User List",
                        NetMessage.Message.newBuilder()
                                .setUserList(
                                        NetMessage.Message.UserList.newBuilder()
                                                .addUser("John")
                                                .addUser("Jane")
                                                .build()
                                ).build(),
                        true
                ),

                // OTHER MESSAGE TYPE TESTS
                new TestCase(
                        "Other",
                        NetMessage.Message.newBuilder()
                                .build(),
                        false
                )
        };

        for(TestCase t : tests) {
            assertThat(t.description,
                    ClientController.processMessage(t.message),
                    equalTo(t.result));
        }
    }

    /*

    The methods below need to be tested by hand, because the setup required to create automated tests is prohibitively
    complex given the project's time constraints. These all depend on having a valid server to talk to, and that's just
    not viable to set up on this testing timeline.

    @Test
    public void login() throws Exception {
    }

    @Test
    public void register() throws Exception {
    }

    @Test
    public void sendPublicMessage() throws Exception {
    }

    @Test
    public void sendPrivateMessage() throws Exception {
    }
     */
}