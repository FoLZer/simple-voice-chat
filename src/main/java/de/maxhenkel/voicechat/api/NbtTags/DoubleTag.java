package de.maxhenkel.voicechat.api.NbtTags;

import de.maxhenkel.voicechat.api.Mth;
import net.kyori.adventure.text.Component;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DoubleTag extends NumericTag {
    public static final DoubleTag ZERO = new DoubleTag(0.0D);
    public static final TagType<DoubleTag> TYPE = new TagType<DoubleTag>() {
        @Override
        public DoubleTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(128L);
            return valueOf(dataInput.readDouble());
        }

        @Override
        public String getName() {
            return "DOUBLE";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Double";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    private final double data;

    private DoubleTag(double d) {
        data = d;
    }

    public static DoubleTag valueOf(double d) {
        return d == 0.0D ? ZERO : new DoubleTag(d);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeDouble(data);
    }

    @Override
    public byte getId() {
        return 6;
    }

    @Override
    public TagType<DoubleTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return data + "d";
    }

    @Override
    public DoubleTag copy() {
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            return object instanceof DoubleTag && data == ((DoubleTag) object).data;
        }
    }

    @Override
    public int hashCode() {
        long l = Double.doubleToLongBits(data);
        return (int) (l ^ l >>> 32);
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        Component component = Component.text("d").style(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        return Component.text(String.valueOf(data)).append(component).style(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public long getAsLong() {
        return (long) Math.floor(data);
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
        return (float) data;
    }

    @Override
    public Number getAsNumber() {
        return data;
    }
}
