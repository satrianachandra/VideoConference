/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package message;

/**
 *
 * @author chandra
 */
public class MessageType {
    public static final int REGISTERING = 10;
    public static final int REGISTERED = 11;
    public static final int FETCHUSERS = 12;
    public static final int CALL_REQUEST = 13;
    public static final int CALL_ACCEPTED = 14;
    public static final int BYE = 15;
    public static final int JOIN_ROOM_REQUEST = 16;
    public static final int JOIN_ROOM_SUCCESS = 17;
    public static final int ROOM_PARTICIPANT_UPDATE = 18;
    public static final int QUIT_ROOM = 19;
}
