package com.runemate.mcp.cache.definition.loader;

import  com.runemate.mcp.cache.*;
import com.runemate.mcp.cache.definition.*;
import com.runemate.mcp.cache.fs.*;
import com.runemate.mcp.cache.util.*;
import com.google.common.cache.*;
import java.io.*;
import java.time.*;
import java.util.*;
import org.jetbrains.annotations.*;

public class ItemLoader extends ConfigLoader<ItemConfig> {

    private static final Cache<Integer, ItemConfig> CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(5))
        .build();

    private final EntityOpsLoader entityOpsLoader = new EntityOpsLoader();

    public ItemLoader(JagexCache storage) {
        super(ConfigType.ITEM, storage, CACHE);
    }


    @Override
    @Nullable
    public ItemConfig load(final int id) {
        ItemConfig def = super.load(id);
        if (def != null) {
            if (def.getNotedTemplate() != -1) {
                def.linkNote(super.load(def.getNotedTemplate()), super.load(def.getNotedId()));
            }

            if (def.getCosmeticTemplateId() != -1) {
                def.linkBought(super.load(def.getCosmeticTemplateId()), super.load(def.getCosmeticId()));
            }

            if (def.getPlaceholderTemplateId() != -1) {
                def.linkPlaceholder(super.load(def.getPlaceholderTemplateId()), super.load(def.getPlaceholderId()));
            }
        }
        return def;
    }

    @Override
    protected ItemConfig decode(final Archive.File file) throws IOException, UnhandledOpcodeException {
        ItemConfig def = new ItemConfig(file.getFileId());

        file.decode((stream, opcode) -> {
            switch (opcode) {
                case 1 -> def.setInventoryModel(stream.readUnsignedShort());
                case 2 -> def.setName(stream.readString());
                case 3 -> def.setExamine(stream.readString());
                case 4 -> def.setZoom2d(stream.readUnsignedShort());
                case 5 -> def.setXan2d(stream.readUnsignedShort());
                case 6 -> def.setYan2d(stream.readUnsignedShort());
                case 7 -> {
                    int xOffset2d = stream.readUnsignedShort();
                    if (xOffset2d > 0x7fff) {
                        xOffset2d -= 0x10000;
                    }
                    def.setXOffset2d(xOffset2d);
                }
                case 8 -> {
                    int yOffset2d = stream.readUnsignedShort();
                    if (yOffset2d > 0x7fff) {
                        yOffset2d -= 0x10000;
                    }
                    def.setYOffset2d(yOffset2d);
                }
                case 9 -> def.setUnknown1(stream.readString());
                case 11 -> def.setStackable(1);
                case 12 -> def.setCost(stream.readInt());
                case 13 -> def.setWearPos1(stream.readByte());
                case 14 -> def.setWearPos2(stream.readByte());
                case 16 -> def.setMembers(true);
                case 23 -> {
                    def.setMaleModel0(stream.readUnsignedShort());
                    def.setMaleOffset(stream.readUnsignedByte());
                }
                case 24 -> def.setMaleModel1(stream.readUnsignedShort());
                case 25 -> {
                    def.setFemaleModel0(stream.readUnsignedShort());
                    def.setFemaleOffset(stream.readUnsignedByte());
                }
                case 26 -> def.setFemaleModel1(stream.readUnsignedShort());
                case 27 -> def.setWearPos3(stream.readByte());
                case 30, 31, 32, 33, 34 -> {
                    String option = stream.readString();
                    if (option.equalsIgnoreCase("Hidden")) {
                        option = null;
                    }
                    def.getActions()[opcode - 30] = option;
                }
                case 35, 36, 37, 38, 39 -> {
                    def.getInventoryActions()[opcode - 35] = stream.readString();
                }
                case 40 -> {
                    int length = stream.readUnsignedByte();
                    short[] colorFind = new short[length];
                    short[] colorReplace = new short[length];

                    for (int index = 0; index < length; ++index) {
                        colorFind[index] = (short) stream.readUnsignedShort();
                        colorReplace[index] = (short) stream.readUnsignedShort();
                    }

                    def.setColorFind(colorFind);
                    def.setColorReplace(colorReplace);
                }
                case 41 -> {
                    int length = stream.readUnsignedByte();
                    short[] textureFind = new short[length];
                    short[] textureReplace = new short[length];

                    for (int index = 0; index < length; ++index) {
                        textureFind[index] = (short) stream.readUnsignedShort();
                        textureReplace[index] = (short) stream.readUnsignedShort();
                    }

                    def.setTextureFind(textureFind);
                    def.setTextureReplace(textureReplace);
                }
                case 42 -> def.setShiftClickDropIndex(stream.readByte());
                case 44 -> def.setInventoryModel(stream.readInt());
                case 45 -> {
                    def.setMaleModel0(stream.readInt());
                    def.setMaleOffset(stream.readUnsignedByte());
                }
                case 46 -> def.setMaleModel1(stream.readInt());
                case 47 -> def.setMaleModel2(stream.readInt());
                case 48 -> {
                    def.setFemaleModel0(stream.readInt());
                    def.setFemaleOffset(stream.readUnsignedByte());
                }
                case 49 -> def.setFemaleModel1(stream.readInt());
                case 50 -> def.setFemaleModel2(stream.readInt());
                case 51 -> def.setMaleHeadModel(stream.readInt());
                case 52 -> def.setMaleHeadModel2(stream.readInt());
                case 53 -> def.setFemaleHeadModel(stream.readInt());
                case 54 -> def.setFemaleHeadModel2(stream.readInt());
                case 43 -> {
                    int opId = stream.readUnsignedByte();
                    if (def.getSubops() == null) {
                        def.setSubops(new String[5][]);
                    }

                    boolean valid = opId >= 0 && opId < 5;
                    if (valid && def.getSubops()[opId] == null) {
                        def.getSubops()[opId] = new String[20];
                    }

                    while (true) {
                        int subopId = stream.readUnsignedByte() - 1;
                        if (subopId == -1) {
                            break;
                        }

                        String op = stream.readString();
                        if (valid && subopId >= 0 && subopId < 20) {
                            def.getSubops()[opId][subopId] = op;
                        }
                    }
                }
                case 65 -> def.setTradeable(true);
                case 75 -> def.setWeight(stream.readShort());
                case 78 -> def.setMaleModel2(stream.readUnsignedShort());
                case 79 -> def.setFemaleModel2(stream.readUnsignedShort());
                case 90 -> def.setMaleHeadModel(stream.readUnsignedShort());
                case 91 -> def.setFemaleHeadModel(stream.readUnsignedShort());
                case 92 -> def.setMaleHeadModel2(stream.readUnsignedShort());
                case 93 -> def.setFemaleHeadModel2(stream.readUnsignedShort());
                case 94 -> def.setCategory(stream.readUnsignedShort());
                case 95 -> def.setZan2d(stream.readUnsignedShort());
                case 97 -> def.setNotedId(stream.readUnsignedShort());
                case 98 -> def.setNotedTemplate(stream.readUnsignedShort());
                case 100, 101, 102, 103, 104, 105, 106, 107, 108, 109 -> {
                    if (def.getCountObj() == null) {
                        def.setCountObj(new int[10]);
                        def.setCountCo(new int[10]);
                    }

                    def.getCountObj()[opcode - 100] = stream.readUnsignedShort();
                    def.getCountCo()[opcode - 100] = stream.readUnsignedShort();
                }
                case 110 -> def.setResizeX(stream.readUnsignedShort());
                case 111 -> def.setResizeY(stream.readUnsignedShort());
                case 112 -> def.setResizeZ(stream.readUnsignedShort());
                case 113 -> def.setAmbient(stream.readByte());
                case 114 -> def.setContrast(stream.readByte());
                case 115 -> def.setTeam(stream.readUnsignedByte());
                case 139 -> def.setCosmeticId(stream.readUnsignedShort());
                case 140 -> def.setCosmeticTemplateId(stream.readUnsignedShort());
                case 148 -> def.setPlaceholderId(stream.readUnsignedShort());
                case 149 -> def.setPlaceholderTemplateId(stream.readUnsignedShort());
                case 200 -> entityOpsLoader.decodeSubOp(stream);
                case 201 -> entityOpsLoader.decodeConditionalOp(stream);
                case 202 -> entityOpsLoader.decodeConditionalSubOp(stream);
                case 249 -> def.setParams(stream.readParams());
                default -> throw new UnhandledOpcodeException(opcode, ConfigType.ITEM);
            }
        });

        if (def.getStackable() == 1) {
            def.setWeight(0);
        }

        return def;
    }
}
