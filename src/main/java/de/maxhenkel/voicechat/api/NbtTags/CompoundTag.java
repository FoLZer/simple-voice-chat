package de.maxhenkel.voicechat.api.NbtTags;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.server.v1_16_R3.CrashReport;
import net.minecraft.server.v1_16_R3.CrashReportSystemDetails;
import net.minecraft.server.v1_16_R3.ReportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class CompoundTag implements Tag {
    public static final Codec<CompoundTag> CODEC;
    public static final TagType<CompoundTag> TYPE;
    private static final Logger LOGGER;
    private static final Pattern SIMPLE_VALUE;

    static {
        CODEC = Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
            Tag tag = dynamic.convert(NbtOps.INSTANCE).getValue();
            return tag instanceof CompoundTag ? DataResult.success((CompoundTag) tag) : DataResult.error("Not a compound tag: " + tag);
        }, compoundTag -> {
            return new Dynamic(NbtOps.INSTANCE, compoundTag);
        });
        LOGGER = LogManager.getLogger();
        SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
        TYPE = new TagType<CompoundTag>() {
            @Override
            public CompoundTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
                nbtAccounter.accountBits(384L);
                if (i > 512) {
                    throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
                } else {
                    HashMap map = Maps.newHashMap();

                    byte b;
                    while ((b = readNamedTagType(dataInput, nbtAccounter)) != 0) {
                        String string = readNamedTagName(dataInput, nbtAccounter);
                        nbtAccounter.accountBits(224 + 16 * string.length());
                        Tag tag = readNamedTagData(TagTypes.getType(b), string, dataInput, i + 1, nbtAccounter);
                        if (map.put(string, tag) != null) {
                            nbtAccounter.accountBits(288L);
                        }
                    }

                    return new CompoundTag(map);
                }
            }

            @Override
            public String getName() {
                return "COMPOUND";
            }

            @Override
            public String getPrettyName() {
                return "TAG_Compound";
            }
        };
    }

    private final Map<String, Tag> tags;

    protected CompoundTag(Map<String, Tag> map) {
        tags = map;
    }

    public CompoundTag() {
        this(Maps.newHashMap());
    }

    private static void writeNamedTag(String string, Tag tag, DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(tag.getId());
        if (tag.getId() != 0) {
            dataOutput.writeUTF(string);
            tag.write(dataOutput);
        }
    }

    private static byte readNamedTagType(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
        return dataInput.readByte();
    }

    private static String readNamedTagName(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
        return dataInput.readUTF();
    }

    private static Tag readNamedTagData(TagType<?> tagType, String string, DataInput dataInput, int i, NbtAccounter nbtAccounter) {
        try {
            return tagType.load(dataInput, i, nbtAccounter);
        } catch (IOException var8) {
            CrashReport crashReport = CrashReport.a(var8, "Loading NBT data");
            CrashReportSystemDetails crashReportCategory = crashReport.a("NBT Tag");
            crashReportCategory.a("Tag name", string);
            crashReportCategory.a("Tag type", tagType.getName());
            throw new ReportedException(crashReport);
        }
    }

    protected static String handleEscape(String string) {
        return SIMPLE_VALUE.matcher(string).matches() ? string : StringTag.quoteAndEscape(string);
    }

    protected static Component handleEscapePretty(String string) {
        if (SIMPLE_VALUE.matcher(string).matches()) {
            return Component.text(string).style(SYNTAX_HIGHLIGHTING_KEY);
        } else {
            String string2 = StringTag.quoteAndEscape(string);
            String string3 = string2.substring(0, 1);
            Component component = Component.text(string2.substring(1, string2.length() - 1)).style(SYNTAX_HIGHLIGHTING_KEY);
            return Component.text(string3).append(component).append(Component.text(string3));
        }
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        Iterator var2 = tags.keySet().iterator();

        while (var2.hasNext()) {
            String string = (String) var2.next();
            Tag tag = tags.get(string);
            writeNamedTag(string, tag, dataOutput);
        }

        dataOutput.writeByte(0);
    }

    public Set<String> getAllKeys() {
        return tags.keySet();
    }

    @Override
    public byte getId() {
        return 10;
    }

    @Override
    public TagType<CompoundTag> getType() {
        return TYPE;
    }

    public int size() {
        return tags.size();
    }

    @Nullable
    public Tag put(String string, Tag tag) {
        return tags.put(string, tag);
    }

    public void putByte(String string, byte b) {
        tags.put(string, ByteTag.valueOf(b));
    }

    public void putShort(String string, short s) {
        tags.put(string, ShortTag.valueOf(s));
    }

    public void putInt(String string, int i) {
        tags.put(string, IntTag.valueOf(i));
    }

    public void putLong(String string, long l) {
        tags.put(string, LongTag.valueOf(l));
    }

    public void putUUID(String string, UUID uUID) {
        tags.put(string, NbtUtils.createUUID(uUID));
    }

    public UUID getUUID(String string) {
        return NbtUtils.loadUUID(get(string));
    }

    public boolean hasUUID(String string) {
        Tag tag = get(string);
        return tag != null && tag.getType() == IntArrayTag.TYPE && ((IntArrayTag) tag).getAsIntArray().length == 4;
    }

    public void putFloat(String string, float f) {
        tags.put(string, FloatTag.valueOf(f));
    }

    public void putDouble(String string, double d) {
        tags.put(string, DoubleTag.valueOf(d));
    }

    public void putString(String string, String string2) {
        tags.put(string, StringTag.valueOf(string2));
    }

    public void putByteArray(String string, byte[] bs) {
        tags.put(string, new ByteArrayTag(bs));
    }

    public void putIntArray(String string, int[] is) {
        tags.put(string, new IntArrayTag(is));
    }

    public void putIntArray(String string, List<Integer> list) {
        tags.put(string, new IntArrayTag(list));
    }

    public void putLongArray(String string, long[] ls) {
        tags.put(string, new LongArrayTag(ls));
    }

    public void putLongArray(String string, List<Long> list) {
        tags.put(string, new LongArrayTag(list));
    }

    public void putBoolean(String string, boolean bl) {
        tags.put(string, ByteTag.valueOf(bl));
    }

    @Nullable
    public Tag get(String string) {
        return tags.get(string);
    }

    public byte getTagType(String string) {
        Tag tag = tags.get(string);
        return tag == null ? 0 : tag.getId();
    }

    public boolean contains(String string) {
        return tags.containsKey(string);
    }

    public boolean contains(String string, int i) {
        int j = getTagType(string);
        if (j == i) {
            return true;
        } else if (i != 99) {
            return false;
        } else {
            return j == 1 || j == 2 || j == 3 || j == 4 || j == 5 || j == 6;
        }
    }

    public byte getByte(String string) {
        try {
            if (contains(string, 99)) {
                return ((NumericTag) tags.get(string)).getAsByte();
            }
        } catch (ClassCastException var3) {
        }

        return 0;
    }

    public short getShort(String string) {
        try {
            if (contains(string, 99)) {
                return ((NumericTag) tags.get(string)).getAsShort();
            }
        } catch (ClassCastException var3) {
        }

        return 0;
    }

    public int getInt(String string) {
        try {
            if (contains(string, 99)) {
                return ((NumericTag) tags.get(string)).getAsInt();
            }
        } catch (ClassCastException var3) {
        }

        return 0;
    }

    public long getLong(String string) {
        try {
            if (contains(string, 99)) {
                return ((NumericTag) tags.get(string)).getAsLong();
            }
        } catch (ClassCastException var3) {
        }

        return 0L;
    }

    public float getFloat(String string) {
        try {
            if (contains(string, 99)) {
                return ((NumericTag) tags.get(string)).getAsFloat();
            }
        } catch (ClassCastException var3) {
        }

        return 0.0F;
    }

    public double getDouble(String string) {
        try {
            if (contains(string, 99)) {
                return ((NumericTag) tags.get(string)).getAsDouble();
            }
        } catch (ClassCastException var3) {
        }

        return 0.0D;
    }

    public String getString(String string) {
        try {
            if (contains(string, 8)) {
                return tags.get(string).getAsString();
            }
        } catch (ClassCastException var3) {
        }

        return "";
    }

    public byte[] getByteArray(String string) {
        try {
            if (contains(string, 7)) {
                return ((ByteArrayTag) tags.get(string)).getAsByteArray();
            }
        } catch (ClassCastException var3) {
            throw new ReportedException(createReport(string, ByteArrayTag.TYPE, var3));
        }

        return new byte[0];
    }

    public int[] getIntArray(String string) {
        try {
            if (contains(string, 11)) {
                return ((IntArrayTag) tags.get(string)).getAsIntArray();
            }
        } catch (ClassCastException var3) {
            throw new ReportedException(createReport(string, IntArrayTag.TYPE, var3));
        }

        return new int[0];
    }

    public long[] getLongArray(String string) {
        try {
            if (contains(string, 12)) {
                return ((LongArrayTag) tags.get(string)).getAsLongArray();
            }
        } catch (ClassCastException var3) {
            throw new ReportedException(createReport(string, LongArrayTag.TYPE, var3));
        }

        return new long[0];
    }

    public CompoundTag getCompound(String string) {
        try {
            if (contains(string, 10)) {
                return (CompoundTag) tags.get(string);
            }
        } catch (ClassCastException var3) {
            throw new ReportedException(createReport(string, TYPE, var3));
        }

        return new CompoundTag();
    }

    public ListTag getList(String string, int i) {
        try {
            if (getTagType(string) == 9) {
                ListTag listTag = (ListTag) tags.get(string);
                if (!listTag.isEmpty() && listTag.getElementType() != i) {
                    return new ListTag();
                }

                return listTag;
            }
        } catch (ClassCastException var4) {
            throw new ReportedException(createReport(string, ListTag.TYPE, var4));
        }

        return new ListTag();
    }

    public boolean getBoolean(String string) {
        return getByte(string) != 0;
    }

    public void remove(String string) {
        tags.remove(string);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("{");
        Collection<String> collection = tags.keySet();
        if (LOGGER.isDebugEnabled()) {
            List<String> list = Lists.newArrayList(tags.keySet());
            Collections.sort(list);
            collection = list;
        }

        String string;
        for (Iterator var5 = ((Collection) collection).iterator(); var5.hasNext(); stringBuilder.append(handleEscape(string)).append(':').append(tags.get(string))) {
            string = (String) var5.next();
            if (stringBuilder.length() != 1) {
                stringBuilder.append(',');
            }
        }

        return stringBuilder.append('}').toString();
    }

    public boolean isEmpty() {
        return tags.isEmpty();
    }

    private CrashReport createReport(String string, TagType<?> tagType, ClassCastException classCastException) {
        CrashReport crashReport = CrashReport.a(classCastException, "Reading NBT data");
        CrashReportSystemDetails crashReportCategory = crashReport.a("Corrupt NBT tag", 1);
        crashReportCategory.a("Tag type found", () -> {
            return tags.get(string).getType().getName();
        });
        crashReportCategory.a("Tag type expected", tagType::getName);
        crashReportCategory.a("Tag name", string);
        return crashReport;
    }

    @Override
    public CompoundTag copy() {
        Map<String, Tag> map = Maps.newHashMap(Maps.transformValues(tags, Tag::copy));
        return new CompoundTag(map);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            return object instanceof CompoundTag && Objects.equals(tags, ((CompoundTag) object).tags);
        }
    }

    @Override
    public int hashCode() {
        return tags.hashCode();
    }

    public CompoundTag merge(CompoundTag compoundTag) {
        Iterator var2 = compoundTag.tags.keySet().iterator();

        while (var2.hasNext()) {
            String string = (String) var2.next();
            Tag tag = compoundTag.tags.get(string);
            if (tag.getId() == 10) {
                if (contains(string, 10)) {
                    CompoundTag compoundTag2 = getCompound(string);
                    compoundTag2.merge((CompoundTag) tag);
                } else {
                    put(string, tag.copy());
                }
            } else {
                put(string, tag.copy());
            }
        }

        return this;
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        if (tags.isEmpty()) {
            return Component.text("{}");
        } else {
            TextComponent mutableComponent = Component.text("{");
            Collection<String> collection = tags.keySet();
            if (LOGGER.isDebugEnabled()) {
                List<String> list = Lists.newArrayList(tags.keySet());
                Collections.sort(list);
                collection = list;
            }

            if (!string.isEmpty()) {
                mutableComponent = mutableComponent.append(Component.text("\n"));
            }

            TextComponent mutableComponent2;
            for (Iterator iterator = ((Collection) collection).iterator(); iterator.hasNext(); mutableComponent = mutableComponent.append(mutableComponent2)) {
                String string2 = (String) iterator.next();
                mutableComponent2 = Component.text(Strings.repeat(string, i + 1)).append(handleEscapePretty(string2)).append(Component.text(String.valueOf(':'))).append(Component.text(" ")).append(tags.get(string2).getPrettyDisplay(string, i + 1));
                if (iterator.hasNext()) {
                    mutableComponent2 = mutableComponent2.append(Component.text(String.valueOf(','))).append(Component.text(string.isEmpty() ? " " : "\n"));
                }
            }

            if (!string.isEmpty()) {
                mutableComponent = mutableComponent.append(Component.text("\n")).append(Component.text(Strings.repeat(string, i)));
            }

            mutableComponent = mutableComponent.append(Component.text("}"));
            return mutableComponent;
        }
    }

    protected Map<String, Tag> entries() {
        return Collections.unmodifiableMap(tags);
    }
}
