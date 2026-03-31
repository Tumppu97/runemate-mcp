package com.runemate.mcp.cache.definition.loader;

import  com.runemate.mcp.cache.*;
import com.runemate.mcp.cache.definition.*;
import com.runemate.mcp.cache.fs.*;
import com.runemate.mcp.cache.util.*;
import java.io.*;

public class SpotAnimationLoader extends ConfigLoader<SpotAnimationConfig> {

    public SpotAnimationLoader(JagexCache storage) {
        super(ConfigType.SPOTANIM, storage);
    }


    @Override
    protected SpotAnimationConfig decode(final Archive.File file) throws IOException, UnhandledOpcodeException {
        SpotAnimationConfig def = new SpotAnimationConfig();
        def.setId(file.getFileId());

        file.decode((stream, opcode) -> {
            switch (opcode) {
                case 1 -> def.setModelId(stream.readUnsignedShort());
                case 2 -> def.setAnimationId(stream.readUnsignedShort());
                case 3 -> def.setModelId(stream.readInt());
                case 4 -> def.setResizeX(stream.readUnsignedShort());
                case 5 -> def.setResizeY(stream.readUnsignedShort());
                case 6 -> def.setRotation(stream.readUnsignedShort());
                case 7 -> def.setAmbient(stream.readUnsignedByte());
                case 8 -> def.setContrast(stream.readUnsignedByte());
                case 9 -> def.setDebugName(stream.readString());
                case 40 -> {
                    int count = stream.readUnsignedByte();
                    short[] recolorToFind = new short[count];
                    short[] recolorToReplace = new short[count];

                    for (int i = 0; i < count; ++i) {
                        recolorToFind[i] = (short) stream.readUnsignedShort();
                        recolorToReplace[i] = (short) stream.readUnsignedShort();
                    }

                    def.setRecolorToFind(recolorToFind);
                    def.setRecolorToReplace(recolorToReplace);
                }
                case 41 -> {
                    int count = stream.readUnsignedByte();
                    short[] textureToFind = new short[count];
                    short[] textureToReplace = new short[count];

                    for (int i = 0; i < count; ++i) {
                        textureToFind[i] = (short) stream.readUnsignedShort();
                        textureToReplace[i] = (short) stream.readUnsignedShort();
                    }

                    def.setTextureToFind(textureToFind);
                    def.setTextureToReplace(textureToReplace);
                }
                default -> throw new UnhandledOpcodeException(opcode, ConfigType.SPOTANIM);
            }
        });

        return def;
    }
}
