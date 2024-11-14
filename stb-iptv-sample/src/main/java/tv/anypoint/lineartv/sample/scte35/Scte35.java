package tv.anypoint.lineartv.sample.scte35;

import java.util.BitSet;
import java.util.zip.CRC32;

public class Scte35 {
    private static void setByte(BitSet bitSet, int start, byte value) {
        for (int i = 0; i < 8; i++) {
            bitSet.set(start + 7 - i, (value & (1 << i)) != 0);
        }
    }

    public static byte[] make(int adBreakTimeInMs, int durationInMs) {
        BitSetWriter writer = new BitSetWriter();

        writer.write(/*tableId*/ (byte) 0xFC);
        BitSet sectionHeader = new BitSet(16);
        sectionHeader.set(/*section_syntax_indicator*/ 0, false);
        sectionHeader.set(/*private_indicator*/ 1, false);
        setByte(sectionHeader, /*section_length*/ 8, (byte) 0x25);
        writer.write(sectionHeader, 16);

        writer.write(/*protocolVersion*/ (byte) 0x00);
        BitSet encryptedInfo = new BitSet(8);
        encryptedInfo.set(/*encrypted_packet*/ 0, false);
        writer.write(encryptedInfo, 8);

        writer.write(/*ptsAdjustment*/ new byte[]{0x00, 0x00, 0x00, 0x00});
        writer.write(/*cwIndex*/ (byte) 0x00);
        writer.write(/*tier*/new BitSet(12), 12);
        BitSet spliceCommandLength = new BitSet(12);
        setByte(spliceCommandLength, /*splice_command_length*/ 4, (byte) 0x14);
        writer.write(spliceCommandLength, 12);

        writer.write(/*spliceCommandType*/ (byte) 0x05);

        // SpliceInsert command
        writer.write(/*spliceEventId*/ new byte[]{0x00, 0x00, 0x03, 0x54});
        writer.write(/*spliceEventCancelIndicator*/ (byte) 0x00);
        BitSet spliceInsertFlags = new BitSet(8);
        spliceInsertFlags.set(0, true); // out_of_network_indicator
        spliceInsertFlags.set(1, true); // program_splice_flag
        spliceInsertFlags.set(2, true); // duration_flag
        spliceInsertFlags.set(3, false); // splice_immediate_flag
        spliceInsertFlags.set(4, false); // event_id_compliance_flag
        writer.write(spliceInsertFlags, 8);

        // SpliceTime struct
        BitSet spliceTimeFlags = new BitSet(8);
        spliceTimeFlags.set(0, true); // time_specified_flag
        writer.write(spliceTimeFlags, 8);
        writer.write(/*ptsTime*/ adBreakTimeInMs * 90);

        // BreakDuration struct
        BitSet breakDurationFlags = new BitSet(8);
        breakDurationFlags.set(0, true); // auto_return
        writer.write(breakDurationFlags, 8);
        writer.write(/*breakDuration*/ durationInMs * 90);

        writer.write(/*uniqueProgramId*/ new byte[]{0x00, 0x01});
        writer.write(/*availNum*/ (byte) 0x00);
        writer.write(/*availsExpected*/ (byte) 0x00);

        writer.write(/*descriptorLoopLength*/ new byte[]{0x00, 0x00});

        byte[] rawBytes = writer.getBytes();
        CRC32 crc32 = new CRC32();
        crc32.update(rawBytes);
        writer.write((int) crc32.getValue());

        return writer.getBytes();
    }
}
