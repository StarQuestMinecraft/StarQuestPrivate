package net.homeip.hall.sqnetevents.packet;

import net.homeip.hall.sqnetevents.SQNetEvents;
import net.homeip.hall.sqnetevents.networking.Sender;
import org.bukkit.event.Event;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.UUID;

public class EventPacket implements Packet {
    private final UUID uid;
    private final ReceivedDataEvent sendEvent;

    public EventPacket(UUID uid, ReceivedDataEvent sendEvent) {
        this.uid = uid;
        this.sendEvent = sendEvent;
    }
    //Called externally
    public EventPacket(ReceivedDataEvent sendEvent) {
        this(UUID.randomUUID(), sendEvent);
    }
    //returns the uid of this eventpacket
    public UUID getUid() {
        return uid;
    }
    //gets the event
    public Event getSendEvent() {
        return sendEvent;
    }
    //returns object read from bytebuffer
    public static EventPacket read(ByteBuffer byteBuffer) throws IOException {
    	byteBuffer.position(0);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.remaining()));
        final long mostSignificantBytes = ois.readLong();
        final long leastSignificantBytes = ois.readLong();
        final UUID uid = new UUID(mostSignificantBytes, leastSignificantBytes);

        Object o;
        try {
            o = ois.readObject();
        } catch (ClassNotFoundException e) {
            return null;
        }

        if (!(o instanceof Event)) {
            throw new IOException("Read object " + o + " is not an Event");
        }
        return new EventPacket(uid, (ReceivedDataEvent) o);
    }
    //fires the event
    @Override
    public void handle() {
    	System.out.println("[NetEvents] Firing event...");
        SQNetEvents.getInstance().getServer().getPluginManager().callEvent(this.getSendEvent());
    }
    //returns a bytebuffer representation of the object
    public ByteBuffer write() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeLong(uid.getMostSignificantBits());
        oos.writeLong(uid.getLeastSignificantBits());

        oos.writeObject(sendEvent);
        oos.flush();
        oos.close();

        ByteBuffer buf = ByteBuffer.wrap(baos.toByteArray());
        buf.position(0);
        return buf;
    }
    //don't know why you would ever use this
    @Override
    public String toString() {
        return "EventPacket{" +
                "uid=" + uid +
                ", sendEvent=" + sendEvent +
                '}';
    }
}
