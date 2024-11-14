package tv.anypoint.lineartv.sample.scte35;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class BitSetWriter {
    private final List<Byte> byteList = new ArrayList<>();
    private int currentByte = 0;
    private int bitCount = 0;

    public void write(BitSet bits, int length) {
        for (int i = 0; i < length; i++) {
            writeBit(bits.get(i) ? 1 : 0);
        }
    }

    public void write(byte b) {
        if (bitCount == 0) {
            byteList.add(b);
        } else {
            for (int i = 7; i >= 0; i--) {
                writeBit((b >> i) & 1);
            }
        }
    }

    public void write(byte[] bytes) {
        for (byte b : bytes) {
            write(b);
        }
    }

    public void write(int num) {
        for (int i = 0; i < 4; i++) {
            write((byte) (num >> (8 * (3 - i))));
        }
    }

    private void writeBit(int bit) {
        currentByte = (currentByte << 1) | bit;
        bitCount++;
        if (bitCount == 8) {
            flushByte();
        }
    }

    private void flushByte() {
        byteList.add((byte) currentByte);
        currentByte = 0;
        bitCount = 0;
    }

    public byte[] getBytes() {
        if (bitCount > 0) {
            currentByte <<= (8 - bitCount); // Padding remaining bits with 0s
            flushByte();
        }
        byte[] result = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            result[i] = byteList.get(i);
        }
        return result;
    }
}