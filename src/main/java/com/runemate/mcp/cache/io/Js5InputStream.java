package com.runemate.mcp.cache.io;

import java.io.*;
import java.nio.*;
import java.util.*;

public class Js5InputStream extends InputStream {

    private static final char[] CP1252_REPLACEMENT_TABLE = {
        '€', ' ', '‚', 'ƒ', '„', '…', '†', '‡', 'ˆ', '‰', 'Š', '‹', 'Œ', ' ',
        'Ž', ' ', ' ', '‘', '’', '“', '”', '•', '–', '—', '˜', '™', 'š', '›', 'œ', ' ', 'ž', 'Ÿ'
    };

    private final ByteBuffer buffer;

    public Js5InputStream(byte[] buffer) {
        this.buffer = ByteBuffer.wrap(buffer);
    }

    public byte[] getArray() {
        assert buffer.hasArray();
        return buffer.array();
    }

    public void skip(int length) {
        int pos = buffer.position();
        pos += length;
        buffer.position(pos);
    }

    public int getOffset() {
        return buffer.position();
    }

    public void setOffset(int offset) {
        buffer.position(offset);
    }

    public int getLength() {
        return buffer.limit();
    }

    public int remaining() {
        return buffer.remaining();
    }

    public byte peek() {
        return buffer.get(buffer.position());
    }

    public byte readByte() {
        return buffer.get();
    }

    public void readBytes(byte[] buffer, int off, int len) {
        this.buffer.get(buffer, off, len);
    }

    public void readBytes(byte[] buffer) {
        this.buffer.get(buffer);
    }

    public int readUnsignedByte() {
        return this.readByte() & 0xFF;
    }

    public int readUnsignedShort() {
        return buffer.getShort() & 0xFFFF;
    }

    public short readShort() {
        return buffer.getShort();
    }

    public int read24BitInt() {
        return (this.readUnsignedByte() << 16) + (this.readUnsignedByte() << 8) + this.readUnsignedByte();
    }

    public int readInt() {
        return buffer.getInt();
    }

    public long readLong() {
        return buffer.getLong();
    }

    public int readBigSmart() {
        return peek() >= 0 ? (this.readUnsignedShort() & 0xFFFF) : (this.readInt() & Integer.MAX_VALUE);
    }

    public int readBigSmart2() {
        if (peek() < 0) {
            return readInt() & Integer.MAX_VALUE; // and off sign bit
        }
        int value = readUnsignedShort();
        return value == 32767 ? -1 : value;
    }

    public int readShortSmart() {
        int peek = this.peek() & 0xFF;
        return peek < 128 ? this.readUnsignedByte() - 64 : this.readUnsignedShort() - 0xc000;
    }

    public int readUnsignedShortSmartMinusOne() {
        int peek = this.peek() & 0xFF;
        return peek < 128 ? this.readUnsignedByte() - 1 : this.readUnsignedShort() - 0x8001;
    }

    public int readUnsignedShortSmart() {
        int peek = this.peek() & 0xFF;
        return peek < 128 ? this.readUnsignedByte() : this.readUnsignedShort() - 0x8000;
    }

    public int readUnsignedIntSmartShortCompat() {
        int var1 = 0;

        int var2;
        for (var2 = this.readUnsignedShortSmart(); var2 == 32767; var2 = this.readUnsignedShortSmart()) {
            var1 += 32767;
        }

        var1 += var2;
        return var1;
    }

    public String readString() {
        StringBuilder sb = new StringBuilder();

        for (; ; ) {
            int ch = this.readUnsignedByte();

            if (ch == 0) {
                break;
            }

            if (ch >= 128 && ch < 160) {
                char var7 = CP1252_REPLACEMENT_TABLE[ch - 128];
                if (0 == var7) {
                    var7 = '?';
                }

                ch = var7;
            }

            sb.append((char) ch);
        }
        return sb.toString();
    }

    public String readString2() {
        if (this.readByte() != 0) {
            throw new IllegalStateException("Invalid jstr2");
        } else {
            return readString();
        }
    }

    public String readStringOrNull() {
        if (this.peek() != 0) {
            return readString();
        } else {
            this.readByte(); // discard
            return null;
        }
    }

    public int readVarInt() {
        byte var1 = this.readByte();

        int var2;
        for (var2 = 0; var1 < 0; var1 = this.readByte()) {
            var2 = (var2 | var1 & 127) << 7;
        }

        return var2 | var1;
    }

    public int readVarInt2() {
        int value = 0;
        int bits = 0;
        int read;
        do {
            read = readUnsignedByte();
            value |= (read & 0x7F) << bits;
            bits += 7;
        } while (read > 127);
        return value;
    }

    public int readDefaultableUnsignedSmart() {
        if (peek() < 0) {
            //Clears the sign bit (most significant bit), forcing the value to be positive.
            //The possible values are between 0 and Integer.MAX_VALUE
            return readInt() & Integer.MAX_VALUE;
        }
        int value = readUnsignedShort();
        if (value == Short.MAX_VALUE) {
            return -1;
        }
        return value;
    }

    public byte[] getRemaining() {
        byte[] b = new byte[buffer.remaining()];
        buffer.get(b);
        return b;
    }

    public Map<Integer, Object> readParams() {
        HashMap<Integer, Object> out = new HashMap<>();
        int size = this.readUnsignedByte();

        for (int i = 0; i < size; ++i) {
            int type = this.readUnsignedByte();
            int key = this.read24BitInt();
            Object value;
            if (type == 1) {
                value = this.readString();
            } else if (type == 2) {
                value = this.readLong();
            } else {
                value = this.readInt();
            }
            out.put(key, value);
        }

        return out;
    }

    @Override
    public int read() throws IOException {
        return this.readUnsignedByte();
    }

}
