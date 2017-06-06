package com.dylanlaufenberg.portlandstate.cs300;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the User class.
 */
public class UserTest {
    private static final String userName = "johndoe";
    private static final String password = "mynameisjohn";
    private static final String testUserFile = "test.dat";

    @BeforeClass
    public static void createUsers() throws Exception {
        User.fout = new FileOutputStream(testUserFile); // Stubbed. No file output.
        User.newUser(userName, password);
    }

    @AfterClass
    public static void cleanUpUsers() throws Exception {
        new File(testUserFile).delete();
    }

    @Test
    public void loadUser() throws Exception {
        User.AuthResult result;

        // Nonexistent user
        result = User.loadUser("john3459", "mynameisjohn");
        assertThat("Nonexistent user",
                result.result,
                equalTo(User.AuthResult.Result.BAD_USER));

        // Incorrect password
        result = User.loadUser(userName, "badpass");
        assertThat("Incorrect password",
                result.result,
                equalTo(User.AuthResult.Result.BAD_PASSWORD));

        // Correct credentials
        result = User.loadUser(userName, password);
        assertThat("Correct credentials (result)",
                result.result,
                equalTo(User.AuthResult.Result.SUCCESS));
        assertThat("Correct credentials (user)",
                result.user.name,
                equalTo(userName));
    }

    @Test
    public void newUser() throws Exception {
        User.AuthResult result;

        // Nonexistent user
        result = User.newUser("john8888", "mynameisjohn");
        assertThat("Nonexistent user (result)",
                result.result,
                equalTo(User.AuthResult.Result.SUCCESS));
        assertThat("Nonexistent user (user)",
                result.user.name,
                equalTo("john8888"));

        // Existing user
        result = User.newUser(userName, password);
        assertThat("Existing user",
                result.result,
                equalTo(User.AuthResult.Result.BAD_USER));
    }

}