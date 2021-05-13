package de.maxhenkel.voicechat.api;

import de.maxhenkel.voicechat.Voicechat;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class ResourceLocation {
    String modid;
    String path;
    public ResourceLocation(String modid, String path) {
        this.modid = modid;
        this.path = path;
    }

    public void registerIncomingChannel(Voicechat main, PluginMessageListener listener) {
        main.getServer().getMessenger().registerIncomingPluginChannel(main, toString(), listener);
    }

    public void registerOutgoingChannel(Voicechat main) {
        main.getServer().getMessenger().registerOutgoingPluginChannel(main, toString());
    }

    @Override
    public String toString() {
        return modid+':'+path;
    }
}
