/*
    This file is part of BF3 Droid

    BF3 Droid is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BF3 Droid is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 */

package com.ninetwozero.bf3droid.datatype;

public class ChatMessage {

    // Attributes
    private long timestamp;
    private String sender;
    private String message;

    // Construct
    public ChatMessage(long t, String s, String m) {
        timestamp = t;
        sender = s;
        message = m;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

}
