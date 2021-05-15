package de.maxhenkel.voicechat.api.NbtTags;

import net.kyori.adventure.text.Component;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class EndTag implements Tag {
    public static final EndTag INSTANCE = new EndTag();
    public static final TagType<EndTag> TYPE = new TagType<EndTag>() {
        @Override
        public EndTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) {
            nbtAccounter.accountBits(64L);
            return INSTANCE;
        }

        @Override
        public String getName() {
            return "END";
        }

        @Override
        public String getPrettyName() {
            return "TAG_End";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };

    private EndTag() {
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
    }

    @Override
    public byte getId() {
        return 0;
    }

    @Override
    public TagType<EndTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "END";
    }

    @Override
    public EndTag copy() {
        return this;
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        return Component.empty();
    }
}