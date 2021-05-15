package de.maxhenkel.voicechat.api.NbtTags;

public class NbtAccounter {
    public static final NbtAccounter UNLIMITED = new NbtAccounter(0L) {
        @Override
        public void accountBits(long l) {
        }
    };
    private final long quota;
    private long usage;

    public NbtAccounter(long l) {
        quota = l;
    }

    public void accountBits(long l) {
        usage += l / 8L;
        if (usage > quota) {
            throw new RuntimeException("Tried to read NBT tag that was too big; tried to allocate: " + usage + "bytes where max allowed: " + quota);
        }
    }
}