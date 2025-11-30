// src/main/java/net/minecraft/client/gui/GuiUtilRenderComponents.java
package net.minecraft.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;

import com.example.examplemod.client.HexFontRenderer;
import com.example.examplemod.client.HexChatWrapFix;

/**
 * HexColorCodes-patched version of GuiUtilRenderComponents.
 *
 * This class is responsible for splitting long chat messages
 * into visible lines. We completely override vanilla logic so
 * tags like <grad>, <wave>, <pulse>, <rain>, <shake>, etc.
 * survive across wrapped lines.
 *
 * This file is CRITICAL for proper multi-line animation.
 */
public class GuiUtilRenderComponents {

    /**
     * Vanilla: func_178908_a
     *
     * We rewrite the entire method into a safe line-splitting loop
     * using HexFontRenderer.safeTrimStringToWidth() and
     * HexChatWrapFix.computeCarryFromLine().
     */
    public static List func_178908_a(IChatComponent component,
                                     int maxWidth,
                                     FontRenderer font,
                                     boolean ignoreShadow,
                                     boolean dummyFlag) {

        List out = new ArrayList();

        if (component == null) {
            return out;
        }

        // The actual rendered text (already §-formatted)
        String raw = component.getFormattedText();
        if (raw == null || raw.isEmpty()) {
            out.add(new ChatComponentText(""));
            return out;
        }

        System.out.println("[HexWrap-GuiUtil] START maxWidth=" + maxWidth);
        System.out.println("[HexWrap-GuiUtil] raw='" + raw + "'");

        // FULL manual split loop
        String remaining = raw;

        while (remaining != null && !remaining.isEmpty()) {

            // Step 1 — Use our tag-aware renderer
            HexFontRenderer hex = (font instanceof HexFontRenderer)
                    ? (HexFontRenderer) font
                    : null;

            String trimmed = (hex != null)
                    ? hex.safeTrimStringToWidth(remaining, maxWidth)
                    : font.trimStringToWidth(remaining, maxWidth);


            if (trimmed == null || trimmed.isEmpty()) {
                // Should not happen unless width=0
                out.add(new ChatComponentText(remaining));
                break;
            }

            // Add the visible line
            out.add(new ChatComponentText(trimmed));

            System.out.println("[HexWrap-GuiUtil] trimmed='" + trimmed + "'");

            // End of string?
            if (trimmed.length() >= remaining.length()) {
                break;
            }

            // Step 2 — Compute tag carryover
            String carry = HexChatWrapFix.computeCarryFromLine(trimmed);

            // Step 3 — Compute remainder for next iteration
            String leftover = remaining.substring(trimmed.length());

            if (!carry.isEmpty()) {
                leftover = carry + leftover;
            }

            System.out.println("[HexWrap-GuiUtil] carry='" + carry + "'");
            System.out.println("[HexWrap-GuiUtil] leftover='" + leftover + "'");

            remaining = leftover;
        }

        return out;
    }

    /**
     * Vanilla helper. We don't need special handling yet.
     */
    public static String func_178909_a(String s, boolean flag) {
        return s;
    }
}
