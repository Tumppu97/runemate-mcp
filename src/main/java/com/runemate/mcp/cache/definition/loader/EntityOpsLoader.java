package com.runemate.mcp.cache.definition.loader;

import com.runemate.mcp.cache.io.*;

public class EntityOpsLoader {

    public void decodeOp(Js5InputStream is, int index) {
        var text = is.readString();
    }

    public void decodeSubOp(Js5InputStream is) {
        int index = is.readUnsignedByte();
        int subID = is.readUnsignedByte();
        String text = is.readString();
    }

    public void decodeConditionalOp(Js5InputStream is) {
        int index = is.readUnsignedByte();
        int varp = is.readUnsignedShort();
        int varb = is.readUnsignedShort();
        int min = is.readInt();
        int max = is.readInt();
        String text = is.readString();
    }

    public void decodeConditionalSubOp(Js5InputStream is) {
        int index = is.readUnsignedByte();
        int subID = is.readUnsignedShort();
        int varp = is.readUnsignedShort();
        int varb = is.readUnsignedShort();
        int min = is.readInt();
        int max = is.readInt();
        String text = is.readString();
    }
}
