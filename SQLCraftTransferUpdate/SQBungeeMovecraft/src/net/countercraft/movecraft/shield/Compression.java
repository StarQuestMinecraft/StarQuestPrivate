package net.countercraft.movecraft.shield;

import java.util.Arrays;
import java.util.UUID;

public class Compression {
	public static String uuidToStr15(UUID uuid) {
	    long[] longs = new long[2];
	    longs[0] = uuid.getLeastSignificantBits();
	    longs[1] = uuid.getMostSignificantBits();
	    System.out.println("uuidToStr15: " + Arrays.toString(longs));

	    char[] chars = new char[15];
	    // 15 chars x 9 bits payload == 135 >=  128.
	    final int bitsPerChar = (128 + chars.length - 1) / chars.length;
	    final int char0 = 0x2000;
	    long mask = (1L << bitsPerChar) - 1;
	    for (int i = 0; i < chars.length; ++i) {
	        int payload = (int)(longs[0] & mask);
	        chars[i] = (char)(char0 + payload);
	        longs[0] >>>= bitsPerChar;
	        longs[0] |= (longs[1] & mask) << (64 - bitsPerChar);
	        longs[1] >>>= bitsPerChar;
	    }
	    return new String(chars);
	}

	public static UUID str15ToUuid(String s) {
	    char[] chars = s.toCharArray();
	    if (chars.length != 15) {
	        throw new IllegalArgumentException(
	                "String should have length 15, not " + chars.length);
	    }
	    final int bitsPerChar = (128 + chars.length - 1) / chars.length;
	    final int char0 = 0x2000;
	    long mask = (1L << bitsPerChar) - 1;
	    long[] longs = new long[2];
	    //for (int i = 0; i < chars.length; ++i) {
	    for (int i = chars.length - 1; i >= 0; --i) {
	        int payload = (int) chars[i];
	        if (payload < char0) {
	            throw new IllegalArgumentException(
	                     String.format("Char [%d] is wrong; U+%04X",
	                         i, payload));
	        }
	        payload -= char0;
	        longs[1] <<= bitsPerChar;
	        longs[1] |= (longs[0] >>> (64 - bitsPerChar)) & mask;
	        longs[0] <<= bitsPerChar;
	        longs[0] |= payload;
	    }
	    System.out.println("str15ToUuid: " + Arrays.toString(longs));
	    return new UUID(longs[1], longs[0]);
	}

	public static void main(String[] args) {
	    UUID uuid = UUID.randomUUID();
	    System.out.println("UUID; " + uuid.toString());
	    String s = uuidToStr15(uuid);
	    UUID uuid2 = str15ToUuid(s);
	    System.out.println("Success: " + uuid2.equals(uuid));
	}
}
