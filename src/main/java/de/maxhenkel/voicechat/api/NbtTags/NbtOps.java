package de.maxhenkel.voicechat.api.NbtTags;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.PeekingIterator;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractStringBuilder;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class NbtOps implements DynamicOps<Tag> {
    public static final NbtOps INSTANCE = new NbtOps();
    public static final long[] LS = new long[0];
    public static final byte[] BS = new byte[0];
    public static final int[] IS = new int[0];

    protected NbtOps() {
    }

    private static CollectionTag<?> createGenericList(byte b, byte c) {
        if (typesMatch(b, c, (byte) 4)) {
            return new LongArrayTag(LS);
        } else if (typesMatch(b, c, (byte) 1)) {
            return new ByteArrayTag(BS);
        } else {
            return typesMatch(b, c, (byte) 3) ? new IntArrayTag(IS) : new ListTag();
        }
    }

    private static boolean typesMatch(byte b, byte c, byte d) {
        return b == d && (c == d || c == 0);
    }

    private static <T extends Tag> void fillOne(CollectionTag<T> collectionTag, Tag tag, Tag tag2) {
        if (tag instanceof CollectionTag) {
            CollectionTag<?> collectionTag2 = (CollectionTag) tag;
            collectionTag2.forEach(tagx -> {
                collectionTag.add((T) tagx);
            });
        }

        collectionTag.add((T) tag2);
    }

    private static <T extends Tag> void fillMany(CollectionTag<T> collectionTag, Tag tag, List<Tag> list) {
        if (tag instanceof CollectionTag) {
            CollectionTag<?> collectionTag2 = (CollectionTag) tag;
            collectionTag2.forEach(tagx -> {
                collectionTag.add((T) tagx);
            });
        }

        list.forEach(tagx -> {
            collectionTag.add((T) tagx);
        });
    }

    @Override
    public Tag empty() {
        return EndTag.INSTANCE;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> dynamicOps, Tag tag) {
        switch (tag.getId()) {
            case 0:
                return dynamicOps.empty();
            case 1:
                return dynamicOps.createByte(((NumericTag) tag).getAsByte());
            case 2:
                return dynamicOps.createShort(((NumericTag) tag).getAsShort());
            case 3:
                return dynamicOps.createInt(((NumericTag) tag).getAsInt());
            case 4:
                return dynamicOps.createLong(((NumericTag) tag).getAsLong());
            case 5:
                return dynamicOps.createFloat(((NumericTag) tag).getAsFloat());
            case 6:
                return dynamicOps.createDouble(((NumericTag) tag).getAsDouble());
            case 7:
                return dynamicOps.createByteList(ByteBuffer.wrap(((ByteArrayTag) tag).getAsByteArray()));
            case 8:
                return dynamicOps.createString(tag.getAsString());
            case 9:
                return convertList(dynamicOps, tag);
            case 10:
                return convertMap(dynamicOps, tag);
            case 11:
                return dynamicOps.createIntList(Arrays.stream(((IntArrayTag) tag).getAsIntArray()));
            case 12:
                return dynamicOps.createLongList(Arrays.stream(((LongArrayTag) tag).getAsLongArray()));
            default:
                throw new IllegalStateException("Unknown tag type: " + tag);
        }
    }

    @Override
    public DataResult<Number> getNumberValue(Tag tag) {
        return tag instanceof NumericTag ? DataResult.success(((NumericTag) tag).getAsNumber()) : DataResult.error("Not a number");
    }

    @Override
    public Tag createNumeric(Number number) {
        return DoubleTag.valueOf(number.doubleValue());
    }

    @Override
    public Tag createByte(byte b) {
        return ByteTag.valueOf(b);
    }

    @Override
    public Tag createShort(short s) {
        return ShortTag.valueOf(s);
    }

    @Override
    public Tag createInt(int i) {
        return IntTag.valueOf(i);
    }

    @Override
    public Tag createLong(long l) {
        return LongTag.valueOf(l);
    }

    @Override
    public Tag createFloat(float f) {
        return FloatTag.valueOf(f);
    }

    @Override
    public Tag createDouble(double d) {
        return DoubleTag.valueOf(d);
    }

    @Override
    public Tag createBoolean(boolean bl) {
        return ByteTag.valueOf(bl);
    }

    @Override
    public DataResult<String> getStringValue(Tag tag) {
        return tag instanceof StringTag ? DataResult.success(tag.getAsString()) : DataResult.error("Not a string");
    }

    @Override
    public Tag createString(String string) {
        return StringTag.valueOf(string);
    }

    @Override
    public DataResult<Tag> mergeToList(Tag tag, Tag tag2) {
        if (!(tag instanceof CollectionTag) && !(tag instanceof EndTag)) {
            return DataResult.error("mergeToList called with not a list: " + tag, tag);
        } else {
            CollectionTag<?> collectionTag = createGenericList(tag instanceof CollectionTag ? ((CollectionTag) tag).getElementType() : 0, tag2.getId());
            fillOne(collectionTag, tag, tag2);
            return DataResult.success(collectionTag);
        }
    }

    @Override
    public DataResult<Tag> mergeToList(Tag tag, List<Tag> list) {
        if (!(tag instanceof CollectionTag) && !(tag instanceof EndTag)) {
            return DataResult.error("mergeToList called with not a list: " + tag, tag);
        } else {
            CollectionTag<?> collectionTag = createGenericList(tag instanceof CollectionTag ? ((CollectionTag) tag).getElementType() : 0, list.stream().findFirst().map(Tag::getId).orElse((byte) 0));
            fillMany(collectionTag, tag, list);
            return DataResult.success(collectionTag);
        }
    }

    @Override
    public DataResult<Tag> mergeToMap(Tag tag, Tag tag2, Tag tag3) {
        if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
            return DataResult.error("mergeToMap called with not a map: " + tag, tag);
        } else if (!(tag2 instanceof StringTag)) {
            return DataResult.error("key is not a string: " + tag2, tag);
        } else {
            CompoundTag compoundTag = new CompoundTag();
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag2 = (CompoundTag) tag;
                compoundTag2.getAllKeys().forEach(string -> {
                    compoundTag.put(string, compoundTag2.get(string));
                });
            }

            compoundTag.put(tag2.getAsString(), tag3);
            return DataResult.success(compoundTag);
        }
    }

    @Override
    public DataResult<Tag> mergeToMap(Tag tag, MapLike<Tag> mapLike) {
        if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
            return DataResult.error("mergeToMap called with not a map: " + tag, tag);
        } else {
            CompoundTag compoundTag = new CompoundTag();
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag2 = (CompoundTag) tag;
                compoundTag2.getAllKeys().forEach(string -> {
                    compoundTag.put(string, compoundTag2.get(string));
                });
            }

            List<Tag> list = Lists.newArrayList();
            mapLike.entries().forEach(pair -> {
                Tag tag1 = pair.getFirst();
                if (!(tag1 instanceof StringTag)) {
                    list.add(tag1);
                } else {
                    compoundTag.put(tag1.getAsString(), pair.getSecond());
                }
            });
            return !list.isEmpty() ? DataResult.error("some keys are not strings: " + list, compoundTag) : DataResult.success(compoundTag);
        }
    }

    @Override
    public DataResult<Stream<Pair<Tag, Tag>>> getMapValues(Tag tag) {
        if (!(tag instanceof CompoundTag)) {
            return DataResult.error("Not a map: " + tag);
        } else {
            CompoundTag compoundTag = (CompoundTag) tag;
            return DataResult.success(compoundTag.getAllKeys().stream().map(string -> {
                return Pair.of(createString(string), compoundTag.get(string));
            }));
        }
    }

    @Override
    public DataResult<Consumer<BiConsumer<Tag, Tag>>> getMapEntries(Tag tag) {
        if (!(tag instanceof CompoundTag)) {
            return DataResult.error("Not a map: " + tag);
        } else {
            CompoundTag compoundTag = (CompoundTag) tag;
            return DataResult.success(biConsumer -> {
                compoundTag.getAllKeys().forEach(string -> {
                    biConsumer.accept(createString(string), compoundTag.get(string));
                });
            });
        }
    }

    @Override
    public DataResult<MapLike<Tag>> getMap(Tag tag) {
        if (!(tag instanceof CompoundTag)) {
            return DataResult.error("Not a map: " + tag);
        } else {
            final CompoundTag compoundTag = (CompoundTag) tag;
            return DataResult.success(new MapLike<Tag>() {
                @Override
                @Nullable
                public Tag get(Tag tag) {
                    return compoundTag.get(tag.getAsString());
                }

                @Override
                @Nullable
                public Tag get(String string) {
                    return compoundTag.get(string);
                }

                @Override
                public Stream<Pair<Tag, Tag>> entries() {
                    return compoundTag.getAllKeys().stream().map(string -> {
                        return Pair.of(createString(string), compoundTag.get(string));
                    });
                }

                @Override
                public String toString() {
                    return "MapLike[" + compoundTag + "]";
                }
            });
        }
    }

    @Override
    public Tag createMap(Stream<Pair<Tag, Tag>> stream) {
        CompoundTag compoundTag = new CompoundTag();
        stream.forEach(pair -> {
            compoundTag.put(pair.getFirst().getAsString(), pair.getSecond());
        });
        return compoundTag;
    }

    @Override
    public DataResult<Stream<Tag>> getStream(Tag tag) {
        return tag instanceof CollectionTag ? DataResult.success(((CollectionTag) tag).stream().map(tagx -> {
            return tagx;
        })) : DataResult.error("Not a list");
    }

    @Override
    public DataResult<Consumer<Consumer<Tag>>> getList(Tag tag) {
        if (tag instanceof CollectionTag) {
            CollectionTag<?> collectionTag = (CollectionTag) tag;
            collectionTag.getClass();
            return DataResult.success(collectionTag::forEach);
        } else {
            return DataResult.error("Not a list: " + tag);
        }
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(Tag tag) {
        return tag instanceof ByteArrayTag ? DataResult.success(ByteBuffer.wrap(((ByteArrayTag) tag).getAsByteArray())) : DynamicOps.super.getByteBuffer(tag);
    }

    @Override
    public Tag createByteList(ByteBuffer byteBuffer) {
        return new ByteArrayTag(DataFixUtils.toArray(byteBuffer));
    }

    @Override
    public DataResult<IntStream> getIntStream(Tag tag) {
        return tag instanceof IntArrayTag ? DataResult.success(Arrays.stream(((IntArrayTag) tag).getAsIntArray())) : DynamicOps.super.getIntStream(tag);
    }

    @Override
    public Tag createIntList(IntStream intStream) {
        return new IntArrayTag(intStream.toArray());
    }

    @Override
    public DataResult<LongStream> getLongStream(Tag tag) {
        return tag instanceof LongArrayTag ? DataResult.success(Arrays.stream(((LongArrayTag) tag).getAsLongArray())) : DynamicOps.super.getLongStream(tag);
    }

    @Override
    public Tag createLongList(LongStream longStream) {
        return new LongArrayTag(longStream.toArray());
    }

    @Override
    public Tag createList(Stream<Tag> stream) {
        PeekingIterator<Tag> peekingIterator = Iterators.peekingIterator(stream.iterator());
        if (!peekingIterator.hasNext()) {
            return new ListTag();
        } else {
            Tag tag = peekingIterator.peek();
            ArrayList list3;
            if (tag instanceof ByteTag) {
                list3 = Lists.newArrayList(Iterators.transform(peekingIterator, tagx -> {
                    return ((ByteTag) tagx).getAsByte();
                }));
                return new ByteArrayTag(list3);
            } else if (tag instanceof IntTag) {
                list3 = Lists.newArrayList(Iterators.transform(peekingIterator, tagx -> {
                    return ((IntTag) tagx).getAsInt();
                }));
                return new IntArrayTag(list3);
            } else if (tag instanceof LongTag) {
                list3 = Lists.newArrayList(Iterators.transform(peekingIterator, tagx -> {
                    return ((LongTag) tagx).getAsLong();
                }));
                return new LongArrayTag(list3);
            } else {
                ListTag listTag = new ListTag();

                while (peekingIterator.hasNext()) {
                    Tag tag2 = peekingIterator.next();
                    if (!(tag2 instanceof EndTag)) {
                        listTag.add(tag2);
                    }
                }

                return listTag;
            }
        }
    }

    @Override
    public Tag remove(Tag tag, String string) {
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag) tag;
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag.getAllKeys().stream().filter(string2 -> {
                return !Objects.equals(string2, string);
            }).forEach(stringx -> {
                compoundTag2.put(stringx, compoundTag.get(stringx));
            });
            return compoundTag2;
        } else {
            return tag;
        }
    }

    @Override
    public String toString() {
        return "NBT";
    }

    @Override
    public RecordBuilder<Tag> mapBuilder() {
        return new NbtRecordBuilder();
    }

    class NbtRecordBuilder extends AbstractStringBuilder<Tag, CompoundTag> {
        protected NbtRecordBuilder() {
            super(NbtOps.this);
        }

        @Override
        protected CompoundTag initBuilder() {
            return new CompoundTag();
        }

        @Override
        protected CompoundTag append(String string, Tag tag, CompoundTag compoundTag) {
            compoundTag.put(string, tag);
            return compoundTag;
        }

        @Override
        protected DataResult<Tag> build(CompoundTag compoundTag, Tag tag) {
            if (tag != null && tag != EndTag.INSTANCE) {
                if (!(tag instanceof CompoundTag)) {
                    return DataResult.error("mergeToMap called with not a map: " + tag, tag);
                } else {
                    CompoundTag compoundTag2 = new CompoundTag(Maps.newHashMap(((CompoundTag) tag).entries()));
                    Iterator var4 = compoundTag.entries().entrySet().iterator();

                    while (var4.hasNext()) {
                        Entry<String, Tag> entry = (Entry) var4.next();
                        compoundTag2.put(entry.getKey(), entry.getValue());
                    }

                    return DataResult.success(compoundTag2);
                }
            } else {
                return DataResult.success(compoundTag);
            }
        }
    }
}
