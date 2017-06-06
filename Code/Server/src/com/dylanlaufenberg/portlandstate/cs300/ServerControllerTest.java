package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage.Message.AuthenticationMessage.AuthMessageType;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for ServerController class
 */
public class ServerControllerTest {
    private static final String[] userName = { "johndoe", "janedoe" };
    private static final String password = "mynameisjohn";
    private static final String testUserFile = "test.dat";
    private static final EmbeddedChannel channel = new EmbeddedChannel();

    @BeforeClass
    public static void createUsers() throws Exception {
        User.fout = new FileOutputStream("test.dat"); // Stubbed. No file output.
        User.newUser(userName[1], password);
    }

    @AfterClass
    public static void cleanUpUsers() throws Exception {
        new File(testUserFile).delete();
    }

    @Test
    public void processAuthMessage() throws Exception {
        Object[][] testCases = {
                {
                    "Register - new user",
                    AuthMessageType.AUTH_REGISTER,
                    userName[0],
                    password,
                    true
                },
                {
                    "Register - existing user",
                    AuthMessageType.AUTH_REGISTER,
                    userName[0],
                    password,
                    false
                },
                {
                    "Login - existing user (not logged in)",
                    AuthMessageType.AUTH_LOGIN,
                    userName[1],
                    password,
                    true
                },
                {
                    "Login - nonexistent user",
                    AuthMessageType.AUTH_LOGIN,
                    "doesntexist",
                    password,
                    false
                },
                {
                    "Login - existing user (already logged in)",
                    AuthMessageType.AUTH_LOGIN,
                    userName[1],
                    password,
                    false
                },
                {
                    "Other",
                    AuthMessageType.UNSET,
                    userName[0],
                    password,
                    false
                }
        };

        for(Object[] testCase : testCases) {
            if((boolean)testCase[4]) {
                // Expect a user in response.
                assertThat(
                        (String)testCase[0],
                        ServerController.processAuthMessage(
                                SharedHelper.buildAuthMessage(
                                        (AuthMessageType)testCase[1],
                                        (String)testCase[2],
                                        (String)testCase[3])
                                        .getAuthMessage(),
                                channel),
                        instanceOf(User.class)
                );
            } else {
                // Expect null in response.
                assertThat(
                        (String)testCase[0],
                        ServerController.processAuthMessage(
                                SharedHelper.buildAuthMessage(
                                        (AuthMessageType)testCase[1],
                                        (String)testCase[2],
                                        (String)testCase[3])
                                        .getAuthMessage(),
                                channel),
                        equalTo(null)
                );
            }
        }
    }

    @Test
    public void processChatMessage() throws Exception {
    }

}