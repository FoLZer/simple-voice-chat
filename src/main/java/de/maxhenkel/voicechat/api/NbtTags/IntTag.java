package de.maxhenkel.voicechat.api.NbtTags;

import net.kyori.adventure.text.Component;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntTag extends NumericTag {
    public static final TagType<IntTag> TYPE = new TagType<IntTag>() {
        @Override
        public IntTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(96L);
            return valueOf(dataInput.readInt());
        }

        @Override
        public String getName() {
            return "INT";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Int";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    private final int data;

    private IntTag(int i) {
        data = i;
    }

    public static IntTag valueOf(int i) {
        return i >= -128 && i <= 1024 ? Cache.cache[i + 128] : new IntTag(i);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(data);
    }

    @Override
    public byte getId() {
        return 3;
    }

    @Override
    public TagType<IntTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return String.valueOf(data);
    }

    @Override
    public IntTag copy() {
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            return object instanceof IntTag && data == ((IntTag) object).data;
        }
    }

    @Override
    public int hashCode() {
        return data;
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        return Component.text(String.valueOf(data)).style(SYNTAX_HIGHLIGHTING_NUMBER);
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
        return (short) (data & '\uffff');
    }

    @Override
    public byte getAsByte() {
        return (byte) (data & 255);
    }

    @Override
    public double getAsDouble() {
        return data;
    }

    @Override
    public float getAsFloat() {
        return (float) data;
    }

    @Override
    public Number getAsNumber() {
        return data;
    }

    static class Cache {
        static final IntTag[] cache = new IntTag[1153];

        static {
            for (int i = 0; i < cache.length; ++i) {
                cache[i] = new IntTag(-128 + i);
            }

        }
    }
}
