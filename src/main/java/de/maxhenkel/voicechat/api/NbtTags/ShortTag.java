package de.maxhenkel.voicechat.api.NbtTags;

import net.kyori.adventure.text.Component;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ShortTag extends NumericTag {
    public static final TagType<ShortTag> TYPE = new TagType<ShortTag>() {
        @Override
        public ShortTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(80L);
            return valueOf(dataInput.readShort());
        }

        @Override
        public String getName() {
            return "SHORT";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Short";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    private final short data;

    private ShortTag(short s) {
        data = s;
    }

    public static ShortTag valueOf(short s) {
        return s >= -128 && s <= 1024 ? Cache.cache[s + 128] : new ShortTag(s);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeShort(data);
    }

    @Override
    public byte getId() {
        return 2;
    }

    @Override
    public TagType<ShortTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return data + "s";
    }

    @Override
    public ShortTag copy() {
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            return object instanceof ShortTag && data == ((ShortTag) object).data;
        }
    }

    @Override
    public int hashCode() {
        return data;
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        Component component = Component.text("s").style(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
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
        return (byte) (data & 255);
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
        static final ShortTag[] cache = new ShortTag[1153];

        static {
            for (int i = 0; i < cache.length; ++i) {
                cache[i] = new ShortTag((short) (-128 + i));
            }

        }
    }
}
