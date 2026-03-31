package com.runemate.mcp.cache.definition.loader;

import  com.runemate.mcp.cache.*;
import com.runemate.mcp.cache.definition.*;
import com.runemate.mcp.cache.fs.*;
import com.runemate.mcp.cache.util.*;
import java.io.*;
import java.util.*;

public class NpcLoader extends ConfigLoader<NpcConfig> {

    private static final boolean REV_210_HEAD_ICONS = true;
    private static final boolean REV_233 = true;
    private static final int DEFAULT_HEAD_ICON_ARCHIVE = -1;

    public NpcLoader(JagexCache storage) {
        super(ConfigType.NPC, storage);
    }


    @Override
    protected NpcConfig decode(Archive.File file) throws IOException, UnhandledOpcodeException {
        NpcConfig def = new NpcConfig(file.getFileId());

        file.decode((stream, opcode) -> {
            switch (opcode) {
                case 1 -> {
                    int length = stream.readUnsignedByte();
                    def.setModels(new int[length]);
                    for (int index = 0; index < length; ++index) {
                        def.getModels()[index] = stream.readUnsignedShort();
                    }
                }
                case 2 -> def.setName(JagTags.remove(stream.readString()));
                case 12 -> def.setSize(stream.readUnsignedByte());
                case 13 -> def.setStandingAnimation(stream.readUnsignedShort());
                case 14 -> def.setWalkingAnimation(stream.readUnsignedShort());
                case 15 -> def.setIdleRotateLeftAnimation(stream.readUnsignedShort());
                case 16 -> def.setIdleRotateRightAnimation(stream.readUnsignedShort());
                case 17 -> {
                    def.setWalkingAnimation(stream.readUnsignedShort());
                    def.setRotate180Animation(stream.readUnsignedShort());
                    def.setRotateLeftAnimation(stream.readUnsignedShort());
                    def.setRotateRightAnimation(stream.readUnsignedShort());
                }
                case 18 -> def.setCategory(stream.readUnsignedShort());
                case 30, 31, 32, 33, 34 -> {
                    String action = stream.readString();
                    if (action.equalsIgnoreCase("Hidden")) {
                        action = null;
                    }
                    def.getActions()[opcode - 30] = action;
                }
                case 40 -> {
                    int length = stream.readUnsignedByte();
                    short[] recolorToFind = new short[length];
                    short[] recolorToReplace = new short[length];
                    for (int index = 0; index < length; ++index) {
                        recolorToFind[index] = (short) stream.readUnsignedShort();
                        recolorToReplace[index] = (short) stream.readUnsignedShort();
                    }
                    def.setRecolorToFind(recolorToFind);
                    def.setRecolorToReplace(recolorToReplace);
                }
                case 41 -> {
                    int length = stream.readUnsignedByte();
                    short[] retextureToFind = new short[length];
                    short[] retextureToReplace = new short[length];
                    for (int index = 0; index < length; ++index) {
                        retextureToFind[index] = (short) stream.readUnsignedShort();
                        retextureToReplace[index] = (short) stream.readUnsignedShort();
                    }
                    def.setRetextureToFind(retextureToFind);
                    def.setRetextureToReplace(retextureToReplace);
                }
                case 60 -> {
                    int length = stream.readUnsignedByte();
                    int[] chatheadModels = new int[length];
                    for (int index = 0; index < length; ++index) {
                        chatheadModels[index] = stream.readUnsignedShort();
                    }
                    def.setChatheadModels(chatheadModels);
                }
                case 61 -> {
                    // 32-bit model IDs (for models exceeding unsigned short range)
                    int length = stream.readUnsignedByte();
                    def.setModels(new int[length]);
                    for (int index = 0; index < length; ++index) {
                        def.getModels()[index] = stream.readInt();
                    }
                }
                case 62 -> {
                    // 32-bit chathead model IDs
                    int length = stream.readUnsignedByte();
                    int[] chatheadModels32 = new int[length];
                    for (int index = 0; index < length; ++index) {
                        chatheadModels32[index] = stream.readInt();
                    }
                    def.setChatheadModels(chatheadModels32);
                }
                case 74 -> def.getStats()[0] = stream.readUnsignedShort();
                case 75 -> def.getStats()[1] = stream.readUnsignedShort();
                case 76 -> def.getStats()[2] = stream.readUnsignedShort();
                case 77 -> def.getStats()[3] = stream.readUnsignedShort();
                case 78 -> def.getStats()[4] = stream.readUnsignedShort();
                case 79 -> def.getStats()[5] = stream.readUnsignedShort();
                case 93 -> def.setMinimapVisible(false);
                case 95 -> def.setCombatLevel(stream.readUnsignedShort());
                case 97 -> def.setWidthScale(stream.readUnsignedShort());
                case 98 -> def.setHeightScale(stream.readUnsignedShort());
                case 99 -> def.setRenderPriority(1);
                case 100 -> def.setAmbient(stream.readByte());
                case 101 -> def.setContrast(stream.readByte());
                case 102 -> {
                    if (!REV_210_HEAD_ICONS) {
                        def.setHeadIconArchiveIds(new int[] { DEFAULT_HEAD_ICON_ARCHIVE });
                        def.setHeadIconSpriteIndex(new short[] { (short) stream.readUnsignedShort() });
                    } else {
                        int bitfield = stream.readUnsignedByte();
                        int len = 0;
                        for (int var5 = bitfield; var5 != 0; var5 >>= 1) {
                            ++len;
                        }

                        int[] headIconArchiveIds = new int[len];
                        short[] headIconSpriteIndex = new short[len];

                        for (int i = 0; i < len; i++) {
                            if ((bitfield & 1 << i) == 0) {
                                headIconArchiveIds[i] = -1;
                                headIconSpriteIndex[i] = -1;
                            } else {
                                headIconArchiveIds[i] = stream.readBigSmart2();
                                headIconSpriteIndex[i] = (short) stream.readUnsignedShortSmartMinusOne();
                            }
                        }

                        def.setHeadIconArchiveIds(headIconArchiveIds);
                        def.setHeadIconSpriteIndex(headIconSpriteIndex);
                    }
                }
                case 103 -> def.setRotationSpeed(stream.readUnsignedShort());
                case 106 -> {
                    int varbitId = stream.readUnsignedShort();
                    if (varbitId == 0xFFFF) {
                        varbitId = -1;
                    }
                    def.setVarbitId(varbitId);

                    int varpIndex = stream.readUnsignedShort();
                    if (varpIndex == 0xFFFF) {
                        varpIndex = -1;
                    }
                    def.setVarpIndex(varpIndex);

                    int length = stream.readUnsignedByte();
                    int[] configs = new int[length + 2];

                    for (int index = 0; index <= length; ++index) {
                        configs[index] = stream.readUnsignedShort();
                        if (configs[index] == 0xFFFF) {
                            configs[index] = -1;
                        }
                    }

                    configs[length + 1] = -1;
                    def.setConfigs(configs);
                }
                case 107 -> def.setInteractable(false);
                case 109 -> def.setRotationFlag(false);
                case 111 -> {
                    if (!REV_233) {
                        // removed in 220
                        def.setFollower(true);
                        def.setLowPriorityFollowerOps(true);
                    } else {
                        def.setRenderPriority(2);
                    }
                }
                case 114 -> def.setRunAnimation(stream.readUnsignedShort());
                case 115 -> {
                    def.setRunAnimation(stream.readUnsignedShort());
                    def.setRunRotate180Animation(stream.readUnsignedShort());
                    def.setRunRotateLeftAnimation(stream.readUnsignedShort());
                    def.setRunRotateRightAnimation(stream.readUnsignedShort());
                }
                case 116 -> def.setCrawlAnimation(stream.readUnsignedShort());
                case 117 -> {
                    def.setCrawlAnimation(stream.readUnsignedShort());
                    def.setCrawlRotate180Animation(stream.readUnsignedShort());
                    def.setCrawlRotateLeftAnimation(stream.readUnsignedShort());
                    def.setCrawlRotateRightAnimation(stream.readUnsignedShort());
                }
                case 118 -> {
                    int varbitId = stream.readUnsignedShort();
                    if (varbitId == 65535) {
                        varbitId = -1;
                    }
                    def.setVarbitId(varbitId);

                    int varpIndex = stream.readUnsignedShort();
                    if (varpIndex == 65535) {
                        varpIndex = -1;
                    }
                    def.setVarpIndex(varpIndex);

                    int var = stream.readUnsignedShort();
                    if (var == 0xFFFF) {
                        var = -1;
                    }

                    int length = stream.readUnsignedByte();
                    int[] configs = new int[length + 2];

                    for (int index = 0; index <= length; ++index) {
                        configs[index] = stream.readUnsignedShort();
                        if (configs[index] == 0xFFFF) {
                            configs[index] = -1;
                        }
                    }

                    configs[length + 1] = var;
                    def.setConfigs(configs);
                }
                case 122 -> def.setFollower(true);
                case 123 -> def.setLowPriorityFollowerOps(true);
                case 124 -> def.setHeight(stream.readUnsignedShort());
                case 126 -> def.setFootprintSize(stream.readUnsignedShort());
                case 129 -> def.setUnknown1(true);
                case 130 -> { /* idleAnimRestart flag — no data bytes */ }
                case 145 -> def.setCanHideForOverlap(true);
                case 146 -> def.setOverlapTintHSL(stream.readUnsignedShort());
                case 147 -> def.setZbuf(false);
                case 249 -> {
                    int length = stream.readUnsignedByte();
                    Map<Integer, Object> params = new HashMap<>(length);

                    for (int i = 0; i < length; i++) {
                        boolean isString = stream.readUnsignedByte() == 1;
                        int key = stream.read24BitInt();
                        Object value;

                        if (isString) {
                            value = stream.readString();
                        } else {
                            value = stream.readInt();
                        }

                        params.put(key, value);
                    }

                    def.setParams(params);
                }
                default -> throw new UnhandledOpcodeException(opcode, ConfigType.NPC);
            }
        });

        if (def.getFootprintSize() == -1) {
            def.setFootprintSize((int) (0.4F * (float) (def.getSize() * 128)));
        }

        return def;
    }
}
