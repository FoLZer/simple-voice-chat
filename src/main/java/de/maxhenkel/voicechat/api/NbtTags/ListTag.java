package de.maxhenkel.voicechat.api.NbtTags;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ListTag extends CollectionTag<Tag> {
    public static final TagType<ListTag> TYPE = new TagType<ListTag>() {
        @Override
        public ListTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(296L);
            if (i > 512) {
                throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
            } else {
                byte b = dataInput.readByte();
                int j = dataInput.readInt();
                if (b == 0 && j > 0) {
                    throw new RuntimeException("Missing type on ListTag");
                } else {
                    nbtAccounter.accountBits(32L * (long) j);
                    TagType<?> tagType = TagTypes.getType(b);
                    List<Tag> list = Lists.newArrayListWithCapacity(j);

                    for (int k = 0; k < j; ++k) {
                        list.add(tagType.load(dataInput, i + 1, nbtAccounter));
                    }

                    return new ListTag(list, b);
                }
            }
        }

        @Override
        public String getName() {
            return "LIST";
        }

        @Override
        public String getPrettyName() {
            return "TAG_List";
        }
    };
    private static final ByteSet INLINE_ELEMENT_TYPES = new ByteOpenHashSet(new byte[]{1, 2, 3, 4, 5, 6});
    private final List<Tag> list;
    private byte type;

    private ListTag(List<Tag> list, byte b) {
        this.list = list;
        type = b;
    }

    public ListTag() {
        this(Lists.newArrayList(), (byte) 0);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        if (list.isEmpty()) {
            type = 0;
        } else {
            type = list.get(0).getId();
        }

        dataOutput.writeByte(type);
        dataOutput.writeInt(list.size());
        Iterator var2 = list.iterator();

        while (var2.hasNext()) {
            Tag tag = (Tag) var2.next();
            tag.write(dataOutput);
        }

    }

    @Override
    public byte getId() {
        return 9;
    }

    @Override
    public TagType<ListTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("[");

        for (int i = 0; i < list.size(); ++i) {
            if (i != 0) {
                stringBuilder.append(',');
            }

            stringBuilder.append(list.get(i));
        }

        return stringBuilder.append(']').toString();
    }

    private void updateTypeAfterRemove() {
        if (list.isEmpty()) {
            type = 0;
        }

    }

    @Override
    public Tag remove(int i) {
        Tag tag = list.remove(i);
        updateTypeAfterRemove();
        return tag;
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    public CompoundTag getCompound(int i) {
        if (i >= 0 && i < list.size()) {
            Tag tag = list.get(i);
            if (tag.getId() == 10) {
                return (CompoundTag) tag;
            }
        }

        return new CompoundTag();
    }

    public ListTag getList(int i) {
        if (i >= 0 && i < list.size()) {
            Tag tag = list.get(i);
            if (tag.getId() == 9) {
                return (ListTag) tag;
            }
        }

        return new ListTag();
    }

    public short getShort(int i) {
        if (i >= 0 && i < list.size()) {
            Tag tag = list.get(i);
            if (tag.getId() == 2) {
                return ((ShortTag) tag).getAsShort();
            }
        }

        return 0;
    }

    public int getInt(int i) {
        if (i >= 0 && i < list.size()) {
            Tag tag = list.get(i);
            if (tag.getId() == 3) {
                return ((IntTag) tag).getAsInt();
            }
        }

        return 0;
    }

    public int[] getIntArray(int i) {
        if (i >= 0 && i < list.size()) {
            Tag tag = list.get(i);
            if (tag.getId() == 11) {
                return ((IntArrayTag) tag).getAsIntArray();
            }
        }

        return new int[0];
    }

    public double getDouble(int i) {
        if (i >= 0 && i < list.size()) {
            Tag tag = list.get(i);
            if (tag.getId() == 6) {
                return ((DoubleTag) tag).getAsDouble();
            }
        }

        return 0.0D;
    }

    public float getFloat(int i) {
        if (i >= 0 && i < list.size()) {
            Tag tag = list.get(i);
            if (tag.getId() == 5) {
                return ((FloatTag) tag).getAsFloat();
            }
        }

        return 0.0F;
    }

    public String getString(int i) {
        if (i >= 0 && i < list.size()) {
            Tag tag = list.get(i);
            return tag.getId() == 8 ? tag.getAsString() : tag.toString();
        } else {
            return "";
        }
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public Tag get(int i) {
        return list.get(i);
    }

    @Override
    public Tag set(int i, Tag tag) {
        Tag tag2 = get(i);
        if (!setTag(i, tag)) {
            throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", tag.getId(), type));
        } else {
            return tag2;
        }
    }

    @Override
    public void add(int i, Tag tag) {
        if (!addTag(i, tag)) {
            throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", tag.getId(), type));
        }
    }

    @Override
    public boolean setTag(int i, Tag tag) {
        if (updateType(tag)) {
            list.set(i, tag);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addTag(int i, Tag tag) {
        if (updateType(tag)) {
            list.add(i, tag);
            return true;
        } else {
            return false;
        }
    }

    private boolean updateType(Tag tag) {
        if (tag.getId() == 0) {
            return false;
        } else if (type == 0) {
            type = tag.getId();
            return true;
        } else {
            return type == tag.getId();
        }
    }

    @Override
    public ListTag copy() {
        Iterable<Tag> iterable = TagTypes.getType(type).isValue() ? list : Iterables.transform(list, Tag::copy);
        List<Tag> list = Lists.newArrayList((Iterable) iterable);
        return new ListTag(list, type);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            return object instanceof ListTag && Objects.equals(list, ((ListTag) object).list);
        }
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        if (isEmpty()) {
            return Component.text("[]");
        } else {
            int k;
            if (INLINE_ELEMENT_TYPES.contains(type) && size() <= 8) {
                String string2 = ", ";
                TextComponent mutableComponent = Component.text("[");

                for (k = 0; k < list.size(); ++k) {
                    if (k != 0) {
                        mutableComponent = mutableComponent.append(Component.text(", "));
                    }

                    mutableComponent = mutableComponent.append(list.get(k).getPrettyDisplay());
                }

                mutableComponent = mutableComponent.append(Component.text("]"));
                return mutableComponent;
            } else {
                TextComponent mutableComponent2 = Component.text("[");
                if (!string.isEmpty()) {
                    mutableComponent2 = mutableComponent2.append(Component.text("\n"));
                }

                String string3 = String.valueOf(',');

                for (k = 0; k < list.size(); ++k) {
                    TextComponent mutableComponent3 = Component.text(Strings.repeat(string, i + 1));
                    mutableComponent3 = mutableComponent3.append(list.get(k).getPrettyDisplay(string, i + 1));
                    if (k != list.size() - 1) {
                        mutableComponent3 = mutableComponent3.append(Component.text(string3)).append(Component.text(string.isEmpty() ? " " : "\n"));
                    }

                    mutableComponent2 = mutableComponent2.append(mutableComponent3);
                }

                if (!string.isEmpty()) {
                    mutableComponent2 = mutableComponent2.append(Component.text("\n")).append(Component.text(Strings.repeat(string, i)));
                }

                mutableComponent2 = mutableComponent2.append(Component.text("]"));
                return mutableComponent2;
            }
        }
    }

    @Override
    public byte getElementType() {
        return type;
    }

    @Override
    public void clear() {
        list.clear();
        type = 0;
    }
}
