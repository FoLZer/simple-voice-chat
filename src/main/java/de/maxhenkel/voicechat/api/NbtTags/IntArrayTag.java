package de.maxhenkel.voicechat.api.NbtTags;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang3.ArrayUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class IntArrayTag extends CollectionTag<IntTag> {
    public static final TagType<IntArrayTag> TYPE = new TagType<IntArrayTag>() {
        @Override
        public IntArrayTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(192L);
            int j = dataInput.readInt();
            nbtAccounter.accountBits(32L * j);
            int[] is = new int[j];

            for (int k = 0; k < j; ++k) {
                is[k] = dataInput.readInt();
            }

            return new IntArrayTag(is);
        }

        @Override
        public String getName() {
            return "INT[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Int_Array";
        }
    };
    public static final int[] DATA = new int[0];
    private int[] data;

    public IntArrayTag(int[] is) {
        data = is;
    }

    public IntArrayTag(List<Integer> list) {
        this(toArray(list));
    }

    private static int[] toArray(List<Integer> list) {
        int[] is = new int[list.size()];

        for (int i = 0; i < list.size(); ++i) {
            Integer integer = list.get(i);
            is[i] = integer == null ? 0 : integer;
        }

        return is;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(data.length);
        int[] var2 = data;

        for (int i : var2) {
            dataOutput.writeInt(i);
        }

    }

    @Override
    public byte getId() {
        return 11;
    }

    @Override
    public TagType<IntArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("[I;");

        for (int i = 0; i < data.length; ++i) {
            if (i != 0) {
                stringBuilder.append(',');
            }

            stringBuilder.append(data[i]);
        }

        return stringBuilder.append(']').toString();
    }

    @Override
    public IntArrayTag copy() {
        int[] is = new int[data.length];
        System.arraycopy(data, 0, is, 0, data.length);
        return new IntArrayTag(is);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            return object instanceof IntArrayTag && Arrays.equals(data, ((IntArrayTag) object).data);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    public int[] getAsIntArray() {
        return data;
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        Component component = Component.text("I").style(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        TextComponent mutableComponent = Component.text("[").append(component).append(Component.text(";"));

        for (int j = 0; j < data.length; ++j) {
            mutableComponent = mutableComponent.append(Component.text(" ")).append(Component.text(String.valueOf(data[j])).style(SYNTAX_HIGHLIGHTING_NUMBER));
            if (j != data.length - 1) {
                mutableComponent = mutableComponent.append(Component.text(","));
            }
        }

        mutableComponent = mutableComponent.append(Component.text("]"));
        return mutableComponent;
    }

    @Override
    public int size() {
        return data.length;
    }

    @Override
    public IntTag get(int i) {
        return IntTag.valueOf(data[i]);
    }

    @Override
    public IntTag set(int i, IntTag intTag) {
        int j = data[i];
        data[i] = intTag.getAsInt();
        return IntTag.valueOf(j);
    }

    @Override
    public void add(int i, IntTag intTag) {
        data = ArrayUtils.add(data, i, intTag.getAsInt());
    }

    @Override
    public boolean setTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            data[i] = ((NumericTag) tag).getAsInt();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            data = ArrayUtils.add(data, i, ((NumericTag) tag).getAsInt());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public IntTag remove(int i) {
        int j = data[i];
        data = ArrayUtils.remove(data, i);
        return IntTag.valueOf(j);
    }

    @Override
    public byte getElementType() {
        return 3;
    }

    @Override
    public void clear() {
        data = DATA;
    }
}
