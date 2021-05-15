package de.maxhenkel.voicechat.api.NbtTags;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang3.ArrayUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ByteArrayTag extends CollectionTag<ByteTag> {
    public static final TagType<ByteArrayTag> TYPE = new TagType<ByteArrayTag>() {
        @Override
        public ByteArrayTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(192L);
            int j = dataInput.readInt();
            nbtAccounter.accountBits(8L * (long) j);
            byte[] bs = new byte[j];
            dataInput.readFully(bs);
            return new ByteArrayTag(bs);
        }

        @Override
        public String getName() {
            return "BYTE[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Byte_Array";
        }
    };
    private byte[] data;

    public ByteArrayTag(byte[] bs) {
        data = bs;
    }

    public ByteArrayTag(List<Byte> list) {
        this(toArray(list));
    }

    private static byte[] toArray(List<Byte> list) {
        byte[] bs = new byte[list.size()];

        for (int i = 0; i < list.size(); ++i) {
            Byte byte_ = list.get(i);
            bs[i] = byte_ == null ? 0 : byte_;
        }

        return bs;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(data.length);
        dataOutput.write(data);
    }

    @Override
    public byte getId() {
        return 7;
    }

    @Override
    public TagType<ByteArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("[B;");

        for (int i = 0; i < data.length; ++i) {
            if (i != 0) {
                stringBuilder.append(',');
            }

            stringBuilder.append(data[i]).append('B');
        }

        return stringBuilder.append(']').toString();
    }

    @Override
    public Tag copy() {
        byte[] bs = new byte[data.length];
        System.arraycopy(data, 0, bs, 0, data.length);
        return new ByteArrayTag(bs);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            return object instanceof ByteArrayTag && Arrays.equals(data, ((ByteArrayTag) object).data);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        Component component = Component.text("B").style(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        TextComponent mutableComponent = Component.text("[").append(component).append(Component.text(";"));

        for (int j = 0; j < data.length; ++j) {
            TextComponent mutableComponent2 = Component.text(String.valueOf(data[j])).style(SYNTAX_HIGHLIGHTING_NUMBER);
            mutableComponent.append(Component.text(" ")).append(mutableComponent2).append(component);
            if (j != data.length - 1) {
                mutableComponent = mutableComponent.append(Component.text(","));
            }
        }

        mutableComponent = mutableComponent.append(Component.text("]"));
        return mutableComponent;
    }

    public byte[] getAsByteArray() {
        return data;
    }

    @Override
    public int size() {
        return data.length;
    }

    @Override
    public ByteTag get(int i) {
        return ByteTag.valueOf(data[i]);
    }

    @Override
    public ByteTag set(int i, ByteTag byteTag) {
        byte b = data[i];
        data[i] = byteTag.getAsByte();
        return ByteTag.valueOf(b);
    }

    @Override
    public void add(int i, ByteTag byteTag) {
        data = ArrayUtils.add(data, i, byteTag.getAsByte());
    }

    @Override
    public boolean setTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            data[i] = ((NumericTag) tag).getAsByte();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            data = ArrayUtils.add(data, i, ((NumericTag) tag).getAsByte());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ByteTag remove(int i) {
        byte b = data[i];
        data = ArrayUtils.remove(data, i);
        return ByteTag.valueOf(b);
    }

    @Override
    public byte getElementType() {
        return 1;
    }

    @Override
    public void clear() {
        data = new byte[0];
    }
}
