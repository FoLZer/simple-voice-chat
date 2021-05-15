package de.maxhenkel.voicechat.api.NbtTags;

import de.maxhenkel.voicechat.api.Mth;
import net.kyori.adventure.text.Component;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FloatTag extends NumericTag {
    public static final FloatTag ZERO = new FloatTag(0.0F);
    public static final TagType<FloatTag> TYPE = new TagType<FloatTag>() {
        @Override
        public FloatTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(96L);
            return valueOf(dataInput.readFloat());
        }

        @Override
        public String getName() {
            return "FLOAT";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Float";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    private final float data;

    private FloatTag(float f) {
        data = f;
    }

    public static FloatTag valueOf(float f) {
        return f == 0.0F ? ZERO : new FloatTag(f);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeFloat(data);
    }

    @Override
    public byte getId() {
        return 5;
    }

    @Override
    public TagType<FloatTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return data + "f";
    }

    @Override
    public FloatTag copy() {
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            return object instanceof FloatTag && data == ((FloatTag) object).data;
        }
    }

    @Override
    public int hashCode() {
        return Float.floatToIntBits(data);
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        Component component = Component.text("f").style(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        return Component.text(String.valueOf(data)).append(component).style(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public long getAsLong() {
        return (long) data;
    }

    @Override
    public int getAsInt() {
        return Mth.floor(data);
    }

    @Override
    public short getAsShort() {
        return (short) (Mth.floor(data) & '\uffff');
    }

    @Override
    public byte getAsByte() {
        return (byte) (Mth.floor(data) & 255);
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
}
