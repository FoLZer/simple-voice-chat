package de.maxhenkel.voicechat.api.NbtTags;

import java.io.DataInput;
import java.io.IOException;

public interface TagType<T extends Tag> {
    static TagType<EndTag> createInvalid(final int i) {
        return new TagType<EndTag>() {
            @Override
            public EndTag load(DataInput dataInput, int ix, NbtAccounter nbtAccounter) throws IOException {
                throw new IllegalArgumentException("Invalid tag id: " + i);
            }

            @Override
            public String getName() {
                return "INVALID[" + i + "]";
            }

            @Override
            public String getPrettyName() {
                return "UNKNOWN_" + i;
            }
        };
    }

    T load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException;

    default boolean isValue() {
        return false;
    }

    String getName();

    String getPrettyName();
}