package de.maxhenkel.voicechat.api.NbtTags;

import java.util.AbstractList;

public abstract class CollectionTag<T extends Tag> extends AbstractList<T> implements Tag {
    public CollectionTag() {
    }

    @Override
    public abstract T set(int i, T tag);

    @Override
    public abstract void add(int i, T tag);

    @Override
    public abstract T remove(int i);

    public abstract boolean setTag(int i, Tag tag);

    public abstract boolean addTag(int i, Tag tag);

    public abstract byte getElementType();
}
