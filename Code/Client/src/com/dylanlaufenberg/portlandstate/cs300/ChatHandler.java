package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Defines the handler used to respond to state changes and data reads on the Netty Channel talking to the server.
 */
public class ChatHandler extends SimpleChannelInboundHandler<NetMessage.Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage.Message m) throws Exception {
        if(m != null && m.getMessageContentsCase() != NetMessage.Message.MessageContentsCase.MESSAGECONTENTS_NOT_SET) {
            System.out.println("Received message: ");
            System.out.println(m.toString());
            System.out.println();

            switch(m.getMessageContentsCase()) {
                case AUTHMESSAGE:
                    processAuthMessage(m.getAuthMessage());
                    break;

                case NOTICEMESSAGE:
                    // processNotice(m); // TODO Fix processNotice
                    break;

                case CHATMESSAGE:
                    // TODO Process Chat messages
                    break;
            }
        }
    }

    private void processAuthMessage(NetMessage.Message.AuthenticationMessage m) {
        if(m == null) {
            return;
        }

        switch(m.getAuthMessageType()) {
            case UNSET:
            case AUTH_LOGIN:
            case AUTH_REGISTER:
            case UNRECOGNIZED:
                break;

            case AUTH_SUCCESS:
                ClientController.goOnline();
                break;

            case AUTH_ERROR_USER:
                if(ClientController.loginScreen != null) {
                    ClientController.loginScreen.showUserErrorMessage();
                }
                break;

            case AUTH_ERROR_PASSWORD:
                if(ClientController.loginScreen != null) {
                    ClientController.loginScreen.showPasswordErrorMessage();
                }
                break;
        }
    }

    /*
    private void processNotice(NetMessage.Message m) {
        if(m != null
                && m.getMessageType() == NetMessage.Message.MessageType.NOTICE
                && m.hasNoticeData()) {
            NetMessage.Message.Notice notice = m.getNoticeData();
            if(notice.getType() != NetMessage.Message.Notice.NoticeType.UNSET
                    && !notice.getUserName().equals("")){

                // User ...
                String userName = notice.getUserName();
                boolean isMe = ClientController.userName.equals(userName); // Is this me whose status may have changed?

                // is now ...
                switch (notice.getType()) {
                    case ONLINE:
                        // First, go online if it's us.
                        if(isMe) {
                            ClientController.goOnline();
                        }

                        // Second, send the message to the ChatScreen to notify.
                        // TODO
                        break;

                    case OFFLINE:
                        if(isMe) {
                            ClientController.goOffline();
                        } else {
                            // TODO Send the message to the ChatScreen to notify.
                        }

                        break;

                        // TODO Implement other notice types!
                }
            }
        }
    }
    */
}
