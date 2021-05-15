package de.maxhenkel.voicechat.api.NbtTags;

import net.kyori.adventure.text.Component;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class StringTag implements Tag {
    private static final StringTag EMPTY = new StringTag("");
    public static final TagType<StringTag> TYPE = new TagType<StringTag>() {
        @Override
        public StringTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(288L);
            String string = dataInput.readUTF();
            nbtAccounter.accountBits(16 * string.length());
            return valueOf(string);
        }

        @Override
        public String getName() {
            return "STRING";
        }

        @Override
        public String getPrettyName() {
            return "TAG_String";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    private final String data;

    private StringTag(String string) {
        Objects.requireNonNull(string, "Null string not allowed");
        data = string;
    }

    public static StringTag valueOf(String string) {
        return string.isEmpty() ? EMPTY : new StringTag(string);
    }

    public static String quoteAndEscape(String string) {
        StringBuilder stringBuilder = new StringBuilder(" ");
        char c = 0;

        for (int i = 0; i < string.length(); ++i) {
            char d = string.charAt(i);
            if (d == '\\') {
                stringBuilder.append('\\');
            } else if (d == '"' || d == '\'') {
                if (c == 0) {
                    c = (char) (d == '"' ? 39 : 34);
                }

                if (c == d) {
                    stringBuilder.append('\\');
                }
            }

            stringBuilder.append(d);
        }

        if (c == 0) {
            c = 34;
        }

        stringBuilder.setCharAt(0, c);
        stringBuilder.append(c);
        return stringBuilder.toString();
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(data);
    }

    @Override
    public byte getId() {
        return 8;
    }

    @Override
    public TagType<StringTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return quoteAndEscape(data);
    }

    @Override
    public StringTag copy() {
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            return object instanceof StringTag && Objects.equals(data, ((StringTag) object).data);
        }
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public String getAsString() {
        return data;
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        String string2 = quoteAndEscape(data);
        String string3 = string2.substring(0, 1);
        Component component = Component.text(string2.substring(1, string2.length() - 1)).style(SYNTAX_HIGHLIGHTING_STRING);
        return Component.text(string3).append(component).append(Component.text(string3));
    }
}
