package com.runemate.mcp.cache.definition.loader;

import  com.runemate.mcp.cache.*;
import com.runemate.mcp.cache.definition.*;
import com.runemate.mcp.cache.fs.*;
import com.runemate.mcp.cache.util.*;
import java.io.*;

public class InventoryLoader extends ConfigLoader<InventoryConfig> {

    public InventoryLoader(JagexCache storage) {
        super(ConfigType.INV, storage);
    }


    @Override
    protected InventoryConfig decode(final Archive.File file) throws IOException, UnhandledOpcodeException {
        InventoryConfig def = new InventoryConfig();
        def.setId(file.getFileId());

        file.decode((stream, opcode) -> {
            switch (opcode) {
                case 2 -> def.setCapacity(stream.readUnsignedShort());
                case 249 -> def.setParams(stream.readParams());
                default -> throw new UnhandledOpcodeException(opcode, ConfigType.INV);
            }
        });

        return def;
    }
}
