package de.maxhenkel.voicechat.api.NbtTags;

public abstract class NumericTag implements Tag {
    protected NumericTag() {
    }

    public abstract long getAsLong();

    public abstract int getAsInt();

    public abstract short getAsShort();

    public abstract byte getAsByte();

    public abstract double getAsDouble();

    public abstract float getAsFloat();

    public abstract Number getAsNumber();
}

