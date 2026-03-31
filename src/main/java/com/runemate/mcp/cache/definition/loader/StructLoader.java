package com.runemate.mcp.cache.definition.loader;

import  com.runemate.mcp.cache.*;
import com.runemate.mcp.cache.definition.*;
import com.runemate.mcp.cache.fs.*;
import com.runemate.mcp.cache.util.*;
import java.io.*;

public class StructLoader extends ConfigLoader<StructConfig> {

    public StructLoader(JagexCache storage) {
        super(ConfigType.STRUCT, storage);
    }


    @Override
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    protected StructConfig decode(final Archive.File file) throws IOException, UnhandledOpcodeException {
        StructConfig def = new StructConfig(file.getFileId());

        file.decode((stream, opcode) -> {
            switch (opcode) {
                case 249 -> def.setParams(stream.readParams());
                default -> throw new UnhandledOpcodeException(opcode, ConfigType.STRUCT);
            }
        });

        return def;
    }
}
