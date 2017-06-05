package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;

import java.util.Map;

/**
 * Container for server-side static helper methods.
 */
class ServerHelper {
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
