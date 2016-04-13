package net.homeip.hall.sqnetevents.packet;

import java.io.IOException;
import java.nio.ByteBuffer;


public interface Packet {
    public void handle() throws IOException;
    public ByteBuffer write() throws IOException;
}