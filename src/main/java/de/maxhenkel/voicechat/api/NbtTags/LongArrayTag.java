package de.maxhenkel.voicechat.api.NbtTags;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang3.ArrayUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LongArrayTag extends CollectionTag<LongTag> {
    public static final TagType<LongArrayTag> TYPE = new TagType<LongArrayTag>() {
        @Override
        public LongArrayTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(192L);
            int j = dataInput.readInt();
            nbtAccounter.accountBits(64L * (long) j);
            long[] ls = new long[j];

            for (int k = 0; k < j; ++k) {
                ls[k] = dataInput.readLong();
            }

            return new LongArrayTag(ls);
        }

        @Override
        public String getName() {
            return "LONG[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Long_Array";
        }
    };
    private long[] data;

    public LongArrayTag(long[] ls) {
        data = ls;
    }

    public LongArrayTag(LongSet longSet) {
        data = longSet.toLongArray();
    }

    public LongArrayTag(List<Long> list) {
        this(toArray(list));
    }

    private static long[] toArray(List<Long> list) {
        long[] ls = new long[list.size()];

        for (int i = 0; i < list.size(); ++i) {
            Long long_ = list.get(i);
            ls[i] = long_ == null ? 0L : long_;
        }

        return ls;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(data.length);
        long[] var2 = data;
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            long l = var2[var4];
            dataOutput.writeLong(l);
        }

    }

    @Override
    public byte getId() {
        return 12;
    }

    @Override
    public TagType<LongArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("[L;");

        for (int i = 0; i < data.length; ++i) {
            if (i != 0) {
                stringBuilder.append(',');
            }

            stringBuilder.append(data[i]).append('L');
        }

        return stringBuilder.append(']').toString();
    }

    @Override
    public LongArrayTag copy() {
        long[] ls = new long[data.length];
        System.arraycopy(data, 0, ls, 0, data.length);
        return new LongArrayTag(ls);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            return object instanceof LongArrayTag && Arrays.equals(data, ((LongArrayTag) object).data);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        Component component = Component.text("L").style(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        TextComponent mutableComponent = Component.text("[").append(component).append(Component.text(";"));

        for (int j = 0; j < data.length; ++j) {
            TextComponent mutableComponent2 = Component.text(String.valueOf(data[j])).style(SYNTAX_HIGHLIGHTING_NUMBER);
            mutableComponent = mutableComponent.append(Component.text(" ")).append(mutableComponent2).append(component);
            if (j != data.length - 1) {
                mutableComponent = mutableComponent.append(Component.text(","));
            }
        }

        mutableComponent = mutableComponent.append(Component.text("]"));
        return mutableComponent;
    }

    public long[] getAsLongArray() {
        return data;
    }

    @Override
    public int size() {
        return data.length;
    }

    @Override
    public LongTag get(int i) {
        return LongTag.valueOf(data[i]);
    }

    @Override
    public LongTag set(int i, LongTag longTag) {
        long l = data[i];
        data[i] = longTag.getAsLong();
        return LongTag.valueOf(l);
    }

    @Override
    public void add(int i, LongTag longTag) {
        data = ArrayUtils.add(data, i, longTag.getAsLong());
    }

    @Override
    public boolean setTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            data[i] = ((NumericTag) tag).getAsLong();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            data = ArrayUtils.add(data, i, ((NumericTag) tag).getAsLong());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public LongTag remove(int i) {
        long l = data[i];
        data = ArrayUtils.remove(data, i);
        return LongTag.valueOf(l);
    }

    @Override
    public byte getElementType() {
        return 4;
    }

    @Override
    public void clear() {
        data = new long[0];
    }
}
