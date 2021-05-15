package de.maxhenkel.voicechat.api.NbtTags;

import net.kyori.adventure.text.Component;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongTag extends NumericTag {
    public static final TagType<LongTag> TYPE = new TagType<LongTag>() {
        @Override
        public LongTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(128L);
            return valueOf(dataInput.readLong());
        }

        @Override
        public String getName() {
            return "LONG";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Long";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    private final long data;

    private LongTag(long l) {
        data = l;
    }

    public static LongTag valueOf(long l) {
        return l >= -128L && l <= 1024L ? Cache.cache[(int) l + 128] : new LongTag(l);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeLong(data);
    }

    @Override
    public byte getId() {
        return 4;
    }

    @Override
    public TagType<LongTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return data + "L";
    }

    @Override
    public LongTag copy() {
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            return object instanceof LongTag && data == ((LongTag) object).data;
        }
    }

    @Override
    public int hashCode() {
        return (int) (data ^ data >>> 32);
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        Component component = Component.text("L").style(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        return Component.text(String.valueOf(data)).append(component).style(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public long getAsLong() {
        return data;
    }

    @Override
    public int getAsInt() {
        return (int) (data & -1L);
    }

    @Override
    public short getAsShort() {
        return (short) (int) (data & 65535L);
    }

    @Override
    public byte getAsByte() {
        return (byte) (int) (data & 255L);
    }

    @Override
    public double getAsDouble() {
        return (double) data;
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
        static final LongTag[] cache = new LongTag[1153];

        static {
            for (int i = 0; i < cache.length; ++i) {
                cache[i] = new LongTag(-128 + i);
            }

        }
    }
}
