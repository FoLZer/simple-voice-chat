package de.maxhenkel.voicechat.api.NbtTags;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;

import java.io.DataOutput;
import java.io.IOException;

public interface Tag {
    Style SYNTAX_HIGHLIGHTING_KEY = Style.style(TextColor.color(HSVLike.fromRGB(0,255,255)));
    Style SYNTAX_HIGHLIGHTING_STRING = Style.style(TextColor.color(HSVLike.fromRGB(0,255,0)));
    Style SYNTAX_HIGHLIGHTING_NUMBER = Style.style(TextColor.color(HSVLike.fromRGB(255,255,0)));
    Style SYNTAX_HIGHLIGHTING_NUMBER_TYPE = Style.style(TextColor.color(HSVLike.fromRGB(255,0,0)));

    void write(DataOutput dataOutput) throws IOException;

    @Override
    String toString();

    byte getId();

    TagType<?> getType();

    Tag copy();

    default String getAsString() {
        return toString();
    }

    default Component getPrettyDisplay() {
        return getPrettyDisplay("", 0);
    }

    Component getPrettyDisplay(String string, int i);
}