package de.maxhenkel.voicechat.api.NbtTags;

import net.kyori.adventure.text.Component;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteTag extends NumericTag {
    public static final TagType<ByteTag> TYPE = new TagType<ByteTag>() {
        @Override
        public ByteTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(72L);
            return valueOf(dataInput.readByte());
        }

        @Override
        public String getName() {
            return "BYTE";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Byte";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    public static final ByteTag ZERO = valueOf((byte) 0);
    public static final ByteTag ONE = valueOf((byte) 1);
    private final byte data;

    private ByteTag(byte b) {
        data = b;
    }

    public static ByteTag valueOf(byte b) {
        return Cache.cache[128 + b];
    }

    public static ByteTag valueOf(boolean bl) {
        return bl ? ONE : ZERO;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(data);
    }

    @Override
    public byte getId() {
        return 1;
    }

    @Override
    public TagType<ByteTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return data + "b";
    }

    @Override
    public ByteTag copy() {
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            return object instanceof ByteTag && data == ((ByteTag) object).data;
        }
    }

    @Override
    public int hashCode() {
        return data;
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        Component component = Component.text("b").style(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        return Component.text(String.valueOf(data)).append(component).style(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public long getAsLong() {
        return data;
    }

    @Override
    public int getAsInt() {
        return data;
    }

    @Override
    public short getAsShort() {
        return data;
    }

    @Override
    public byte getAsByte() {
        return data;
    }

    @Override
    public double getAsDouble() {
        return data;
    }

    @Override
    public float getAsFloat() {
        return data;
    }

    @Override
    public Number getAsNumber() {
        return data;
    }

    static class Cache {
        private static final ByteTag[] cache = new ByteTag[256];

        static {
            for (int i = 0; i < cache.length; ++i) {
                cache[i] = new ByteTag((byte) (i - 128));
            }

        }
    }
}
