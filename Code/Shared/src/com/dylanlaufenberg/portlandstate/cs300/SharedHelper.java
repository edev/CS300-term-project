package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;

import java.util.Map;

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

    public static NetMessage.Message buildAuthMessage(NetMessage.Message.AuthenticationMessage.AuthMessageType messageType, String userName, String password) {
        return NetMessage.Message.newBuilder()
                .setAuthMessage(
                        NetMessage.Message.AuthenticationMessage.newBuilder()
                                .setAuthMessageType(messageType)
                                .setUserName(userName)
                                .setPassword(password)
                                .build())
                .build();
    }

    static NetMessage.Message buildPublicMessage(String message) {
        return NetMessage.Message.newBuilder()
                .setChatMessage(
                        NetMessage.Message.ChatMessage.newBuilder()
                                .setChatMessageType(NetMessage.Message.ChatMessage.ChatMessageType.PUBLIC)
                                .setText(message)
                                .build()
                )
                .build();
    }

    static NetMessage.Message buildPrivateMessage(String receiver, String message) {
        return NetMessage.Message.newBuilder()
                .setChatMessage(
                        NetMessage.Message.ChatMessage.newBuilder()
                                .setChatMessageType(NetMessage.Message.ChatMessage.ChatMessageType.PRIVATE)
                                .setReceiver(receiver)
                                .setText(message)
                                .build()
                )
                .build();
    }

    /**
     * Generates a Message containing a UserList with the all the keys in users.
     * @param users map of users to include. Only keys will be used; values will be ignored.
     * @return A built NetMessage.Message containing the specified UserList.
     */
    static NetMessage.Message buildUserListMessage(Map<String, User> users) {
        NetMessage.Message.UserList.Builder userList = NetMessage.Message.UserList.newBuilder();

        userList.addAllUser(users.keySet());

        return NetMessage.Message.newBuilder()
                .setUserList(
                        userList.build()
                )
                .build();
    }

    static NetMessage.Message buildAuthResponseMessage(NetMessage.Message.AuthenticationMessage.AuthMessageType type) {
        return NetMessage.Message.newBuilder()
                .setAuthMessage(
                        NetMessage.Message.AuthenticationMessage.newBuilder()
                                .setAuthMessageType(type)
                                .build()
                )
                .build();
    }

    static NetMessage.Message buildNoticeMessage(NetMessage.Message.NoticeMessage.NoticeMessageType type,
                                                 String userName) {
        return NetMessage.Message.newBuilder()
                .setNoticeMessage(
                        NetMessage.Message.NoticeMessage.newBuilder()
                                .setNoticeMessageType(type)
                                .setUserName(userName)
                                .build()
                )
                .build();
    }

    static NetMessage.Message buildChatResponseMessage(NetMessage.Message.ChatMessage.ChatMessageType type,
                                                       String senderName,
                                                       String text) {
        return NetMessage.Message.newBuilder()
                .setChatMessage(
                        NetMessage.Message.ChatMessage.newBuilder()
                                .setChatMessageType(type)
                                .setSender(senderName)
                                .setText(text)
                                .build()
                )
                .build();
    }
}
