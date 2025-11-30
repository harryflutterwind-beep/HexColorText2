package com.example.examplemod.server;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.ServerChatEvent;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;

import com.example.examplemod.client.HexChatExpand;

public class ChatHexServer {

    @SubscribeEvent
    public void onChat(ServerChatEvent e) {
        try {
            String raw = e.message;
            if (raw == null) raw = "";



            // --- Only expand the tags that exist in HexChatExpand ---
            String msg = raw;

            msg = HexChatExpand.expandGradients(msg);
            msg = HexChatExpand.expandRainbow(msg);
            msg = HexChatExpand.expandPulse(msg);

            // inline formatting (&c → §c)
            msg = msg.replaceAll("(?i)&([0-9A-FK-OR])", "§$1");
            msg = msg.replaceAll("&##([0-9A-Fa-f]{6})", "§#$1");



            // --- Rebuild broadcast packet ---
            ChatComponentTranslation broadcast = new ChatComponentTranslation(
                    "chat.type.text",
                    e.username,
                    new ChatComponentText(msg)
            );

            e.component = broadcast;

        } catch (Throwable t) {
            System.out.println("[HexChatDbg][SERVER] ERROR: " + t);
        }
    }
}
