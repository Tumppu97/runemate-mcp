package com.runemate.mcp.cache.definition.loader;

import  com.runemate.mcp.cache.*;
import com.runemate.mcp.cache.definition.*;
import com.runemate.mcp.cache.fs.*;
import com.runemate.mcp.cache.util.*;
import java.io.*;
import java.util.*;

public class ObjectLoader extends ConfigLoader<ObjectConfig> {

    private static final boolean REV_220_SOUND_DATA = true;

    private final EntityOpsLoader entityOpsLoader = new EntityOpsLoader();

    public ObjectLoader(JagexCache storage) {
        super(ConfigType.OBJECT, storage);
    }


    @Override
    protected ObjectConfig decode(final Archive.File file) throws IOException, UnhandledOpcodeException {
        ObjectConfig def = new ObjectConfig(file.getFileId());
        file.decode((stream, opcode) -> {
            switch (opcode) {
                case 1 -> {
                    int length = stream.readUnsignedByte();
                    if (length > 0) {
                        int[] objectTypes = new int[length];
                        int[] objectModels = new int[length];

                        for (int index = 0; index < length; ++index) {
                            objectModels[index] = stream.readUnsignedShort();
                            objectTypes[index] = stream.readUnsignedByte();
                        }

                        def.setObjectTypes(objectTypes);
                        def.setObjectModels(objectModels);
                    }
                }
                case 2 -> def.setName(JagTags.remove(stream.readString()));
                case 5 -> {
                    int length5 = stream.readUnsignedByte();
                    if (length5 > 0) {
                        def.setObjectTypes(null);
                        int[] objectModels5 = new int[length5];

                        for (int index = 0; index < length5; ++index) {
                            objectModels5[index] = stream.readUnsignedShort();
                        }

                        def.setObjectModels(objectModels5);
                    }
                }
                case 6 -> {
                    int length6 = stream.readUnsignedByte();
                    if (length6 > 0) {
                        int[] objectTypes6 = new int[length6];
                        int[] objectModels6 = new int[length6];

                        for (int index = 0; index < length6; ++index) {
                            objectModels6[index] = stream.readInt();
                            objectTypes6[index] = stream.readUnsignedByte();
                        }

                        def.setObjectTypes(objectTypes6);
                        def.setObjectModels(objectModels6);
                    }
                }
                case 7 -> {
                    int length7 = stream.readUnsignedByte();
                    if (length7 > 0) {
                        def.setObjectTypes(null);
                        int[] objectModels7 = new int[length7];

                        for (int index = 0; index < length7; ++index) {
                            objectModels7[index] = stream.readInt();
                        }

                        def.setObjectModels(objectModels7);
                    }
                }
                case 14 -> def.setSizeX(stream.readUnsignedByte());
                case 15 -> def.setSizeY(stream.readUnsignedByte());
                case 17 -> {
                    def.setInteractType(0);
                    def.setBlocksProjectile(false);
                }
                case 18 -> def.setBlocksProjectile(false);
                case 19 -> def.setWallOrDoor(stream.readUnsignedByte());
                case 21 -> def.setContouredGround(0);
                case 22 -> def.setMergeNormals(true);
                case 23 -> def.setModelClipped(true);
                case 24 -> {
                    int animationID = stream.readUnsignedShort();
                    if (animationID == 0xFFFF) {
                        animationID = -1;
                    }
                    def.setAnimationID(animationID);
                }
                case 27 -> def.setInteractType(1);
                case 28 -> def.setDecorDisplacement(stream.readUnsignedByte());
                case 29 -> def.setAmbient(stream.readByte());
                case 39 -> def.setContrast(stream.readByte() * 25);
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
                        recolorToFind[index] = stream.readShort();
                        recolorToReplace[index] = stream.readShort();
                    }

                    def.setRecolorToFind(recolorToFind);
                    def.setRecolorToReplace(recolorToReplace);
                }
                case 41 -> {
                    int length = stream.readUnsignedByte();
                    short[] retextureToFind = new short[length];
                    short[] textureToReplace = new short[length];

                    for (int index = 0; index < length; ++index) {
                        retextureToFind[index] = stream.readShort();
                        textureToReplace[index] = stream.readShort();
                    }

                    def.setRetextureToFind(retextureToFind);
                    def.setRetextureToReplace(textureToReplace);
                }
                case 61 -> def.setCategory(stream.readUnsignedShort());
                case 62 -> def.setRotated(true);
                case 64 -> def.setShadow(false);
                case 65 -> def.setModelSizeX(stream.readUnsignedShort());
                case 66 -> def.setModelSizeHeight(stream.readUnsignedShort());
                case 67 -> def.setModelSizeY(stream.readUnsignedShort());
                case 68 -> def.setMapSceneID(stream.readUnsignedShort());
                case 69 -> def.setBlockingMask(stream.readByte());
                case 70 -> def.setOffsetX(stream.readUnsignedShort());
                case 71 -> def.setOffsetHeight(stream.readUnsignedShort());
                case 72 -> def.setOffsetY(stream.readUnsignedShort());
                case 73 -> def.setObstructsGround(true);
                case 74 -> def.setHollow(true);
                case 75 -> def.setSupportsItems(stream.readUnsignedByte());
                case 77 -> {
                    int varpID = stream.readUnsignedShort();
                    if (varpID == 0xFFFF) {
                        varpID = -1;
                    }
                    def.setVarbitID(varpID);

                    int configId = stream.readUnsignedShort();
                    if (configId == 0xFFFF) {
                        configId = -1;
                    }
                    def.setVarpID(configId);

                    int length = stream.readUnsignedByte();
                    int[] configChangeDest = new int[length + 2];

                    for (int index = 0; index <= length; ++index) {
                        configChangeDest[index] = stream.readUnsignedShort();
                        if (0xFFFF == configChangeDest[index]) {
                            configChangeDest[index] = -1;
                        }
                    }

                    configChangeDest[length + 1] = -1;
                    def.setConfigs(configChangeDest);
                }
                case 78 -> {
                    def.setAmbientSoundId(stream.readUnsignedShort());
                    def.setAmbientSoundDistance(stream.readUnsignedByte());
                    if (REV_220_SOUND_DATA) {
                        def.setAmbientSoundRetain(stream.readUnsignedByte());
                    }
                }
                case 79 -> {
                    def.setAmbientSoundChangeTicksMin(stream.readUnsignedShort());
                    def.setAmbientSoundChangeTicksMax(stream.readUnsignedShort());
                    def.setAmbientSoundDistance(stream.readUnsignedByte());
                    if (REV_220_SOUND_DATA) {
                        def.setAmbientSoundRetain(stream.readUnsignedByte());
                    }
                    int length = stream.readUnsignedByte();
                    int[] ambientSoundIds = new int[length];

                    for (int index = 0; index < length; ++index) {
                        ambientSoundIds[index] = stream.readUnsignedShort();
                    }

                    def.setAmbientSoundIds(ambientSoundIds);
                }
                case 81 -> def.setContouredGround(stream.readUnsignedByte() * 256);
                case 82 -> def.setMapAreaId(stream.readUnsignedShort());
                case 89 -> def.setRandomizeAnimStart(true);
                case 90 -> def.setDeferAnimChange(true);
                case 91 -> def.setSoundDistanceFadeCurve(stream.readUnsignedByte());
                case 92 -> {
                    int varpID = stream.readUnsignedShort();
                    if (varpID == 0xFFFF) {
                        varpID = -1;
                    }
                    def.setVarbitID(varpID);

                    int configId = stream.readUnsignedShort();
                    if (configId == 0xFFFF) {
                        configId = -1;
                    }
                    def.setVarpID(configId);

                    int var = stream.readUnsignedShort();
                    if (var == 0xFFFF) {
                        var = -1;
                    }

                    int length = stream.readUnsignedByte();
                    int[] configChangeDest = new int[length + 2];

                    for (int index = 0; index <= length; ++index) {
                        configChangeDest[index] = stream.readUnsignedShort();
                        if (0xFFFF == configChangeDest[index]) {
                            configChangeDest[index] = -1;
                        }
                    }

                    configChangeDest[length + 1] = var;
                    def.setConfigs(configChangeDest);
                }
                case 93 -> {
                    def.setSoundFadeInCurve(stream.readUnsignedByte());
                    def.setSoundFadeInDuration(stream.readUnsignedShort());
                    def.setSoundFadeOutCurve(stream.readUnsignedByte());
                    def.setSoundFadeOutDuration(stream.readUnsignedShort());
                }
                case 94 -> def.setUnknown1(true);
                case 95 -> def.setSoundVisibility(stream.readUnsignedByte());
                case 96 -> def.setRaise(stream.readUnsignedByte());
                case 100 -> entityOpsLoader.decodeSubOp(stream);
                case 101 -> entityOpsLoader.decodeConditionalOp(stream);
                case 102 -> entityOpsLoader.decodeConditionalSubOp(stream);
                case 249 -> def.setParams(stream.readParams());
                default -> throw new UnhandledOpcodeException(opcode, ConfigType.OBJECT);
            }
        });

        return def;
    }
}
