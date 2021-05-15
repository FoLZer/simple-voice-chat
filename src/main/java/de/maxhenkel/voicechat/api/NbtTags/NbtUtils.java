package de.maxhenkel.voicechat.api.NbtTags;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.util.internal.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.UUID;

public final class NbtUtils {
    @Nullable
    public static GameProfile readGameProfile(CompoundTag compoundTag) {
        String string = null;
        UUID uUID = null;
        if (compoundTag.contains("Name", 8)) {
            string = compoundTag.getString("Name");
        }

        if (compoundTag.hasUUID("Id")) {
            uUID = compoundTag.getUUID("Id");
        }

        try {
            GameProfile gameProfile = new GameProfile(uUID, string);
            if (compoundTag.contains("Properties", 10)) {
                CompoundTag compoundTag2 = compoundTag.getCompound("Properties");
                Iterator<String> var5 = compoundTag2.getAllKeys().iterator();

                while (var5.hasNext()) {
                    String string2 = var5.next();
                    ListTag listTag = compoundTag2.getList(string2, 10);

                    for (int i = 0; i < listTag.size(); ++i) {
                        CompoundTag compoundTag3 = listTag.getCompound(i);
                        String string3 = compoundTag3.getString("Value");
                        if (compoundTag3.contains("Signature", 8)) {
                            gameProfile.getProperties().put(string2, new Property(string2, string3, compoundTag3.getString("Signature")));
                        } else {
                            gameProfile.getProperties().put(string2, new Property(string2, string3));
                        }
                    }
                }
            }

            return gameProfile;
        } catch (Throwable var11) {
            return null;
        }
    }

    public static CompoundTag writeGameProfile(CompoundTag compoundTag, GameProfile gameProfile) {
        if (!StringUtil.isNullOrEmpty(gameProfile.getName())) {
            compoundTag.putString("Name", gameProfile.getName());
        }

        if (gameProfile.getId() != null) {
            compoundTag.putUUID("Id", gameProfile.getId());
        }

        if (!gameProfile.getProperties().isEmpty()) {
            CompoundTag compoundTag2 = new CompoundTag();
            Iterator<String> var3 = gameProfile.getProperties().keySet().iterator();

            while (var3.hasNext()) {
                String string = var3.next();
                ListTag listTag = new ListTag();

                CompoundTag compoundTag3;
                for (Iterator<Property> var6 = gameProfile.getProperties().get(string).iterator(); var6.hasNext(); listTag.add(compoundTag3)) {
                    Property property = var6.next();
                    compoundTag3 = new CompoundTag();
                    compoundTag3.putString("Value", property.getValue());
                    if (property.hasSignature()) {
                        compoundTag3.putString("Signature", property.getSignature());
                    }
                }

                compoundTag2.put(string, listTag);
            }

            compoundTag.put("Properties", compoundTag2);
        }

        return compoundTag;
    }

    public static UUID uuidFromIntArray(int[] is) {
        return new UUID((long)is[0] << 32 | (long)is[1] & 4294967295L, (long)is[2] << 32 | (long)is[3] & 4294967295L);
    }

    public static int[] uuidToIntArray(UUID uUID) {
        long l = uUID.getMostSignificantBits();
        long m = uUID.getLeastSignificantBits();
        return leastMostToIntArray(l, m);
    }

    private static int[] leastMostToIntArray(long l, long m) {
        return new int[]{(int)(l >> 32), (int)l, (int)(m >> 32), (int)m};
    }


    public static IntArrayTag createUUID(UUID uUID) {
        return new IntArrayTag(uuidToIntArray(uUID));
    }

    public static UUID loadUUID(Tag tag) {
        if (tag.getType() != IntArrayTag.TYPE) {
            throw new IllegalArgumentException("Expected UUID-Tag to be of type " + IntArrayTag.TYPE.getName() + ", but found " + tag.getType().getName() + ".");
        } else {
            int[] is = ((IntArrayTag) tag).getAsIntArray();
            if (is.length != 4) {
                throw new IllegalArgumentException("Expected UUID-Array to be of length 4, but found " + is.length + ".");
            } else {
                return uuidFromIntArray(is);
            }
        }
    }
}

