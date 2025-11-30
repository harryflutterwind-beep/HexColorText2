// src/main/java/com/example/examplemod/client/ClientProxy.java
package com.example.examplemod.client;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.MinecraftForge;

/**
 * Updated ClientProxy
 * ---------------------------------------------------------
 * No HexFontBootstrap. ChatHexHandler + HexFontRenderer
 * handle chat font swapping themselves.
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends com.example.examplemod.CommonProxy {

    @Override
    public void init() {
        super.init();

        // ========================================================
        // 1) Register chat + GUI handler
        //    Handles installing HexFontRenderer ONLY in GuiChat
        //    and restoring vanilla elsewhere.
        // ========================================================
        ChatHexHandler handler = new ChatHexHandler();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance().bus().register(handler);

        // ========================================================
        // 2) Client-side renderers
        // ========================================================
        try {
            ItemBeamRenderer.register();
        } catch (Throwable t) {
            System.out.println("[ItemBeamRenderer] Registration skipped: " + t);
        }

        // ========================================================
        // 3) Rarity + Chaos slot overlay
        // ========================================================
        HexSlotOverlay.register();
    }
}
