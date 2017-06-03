package com.dylanlaufenberg.portlandstate.cs300;

/**
 * Contains static methods that are of use to both Client and Server.
 */
public class SharedHelper {
    public static void error(String message) {
        error(message, null);
    }

    public static void error(String message, Object object) {
        if(message != null) {
            System.err.println(message);
        }
        if(object != null) {
            System.err.println(object.toString());
        }
        System.err.println();
    } // TODO Refactor ALL errors to use these helpers.
}
