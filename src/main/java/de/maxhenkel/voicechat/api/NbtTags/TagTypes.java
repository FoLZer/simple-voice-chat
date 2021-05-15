package de.maxhenkel.voicechat.api.NbtTags;

public class TagTypes {
    private static final TagType<?>[] TYPES;

    static {
        TYPES = new TagType[]{EndTag.TYPE, ByteTag.TYPE, ShortTag.TYPE, IntTag.TYPE, LongTag.TYPE, FloatTag.TYPE, DoubleTag.TYPE, ByteArrayTag.TYPE, StringTag.TYPE, ListTag.TYPE, CompoundTag.TYPE, IntArrayTag.TYPE, LongArrayTag.TYPE};
    }

    public static TagType<?> getType(int i) {
        return i >= 0 && i < TYPES.length ? TYPES[i] : TagType.createInvalid(i);
    }
}
