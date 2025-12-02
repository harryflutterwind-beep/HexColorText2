//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.examplemod.client;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class HexFontRenderer extends FontRenderer {
    private FontRenderer base;
    private static final int[] VANILLA_RGB = new int[]{
            0, 170, 43520, 43690, 11141120, 11141290,
            16755200, 11184810, 5592405, 5592575, 5635925,
            5636095, 16733525, 16733695, 16777045, 16777215
    };
    private static final boolean DEBUG_WRAP = false;
// ─────────────────────────────────────────────
// CORE TAG PATTERNS
// ─────────────────────────────────────────────
// At top of HexFontRenderer (with your other patterns)

    // NEW: detect the *first* global-style open tag in a line
    private static final Pattern GLOBAL_HEADER_TAG =
            Pattern.compile(
                    "(?i)(<\\s*(grad|pulse|wave|zoom|shake|scroll|jitter|wobble|sparkle|flicker|glitch|outline|shadow|glow|rain|rainbow|rb|rbw)[^>]*>)"
            );

    private static final Pattern TAG_HEX_ANY =
            Pattern.compile("<\\s*#?([0-9a-fA-F]{3}|[0-9a-fA-F]{6})(?i:([lmonkr]*))\\s*>");
    private static final Pattern TAG_HEX_ANY_CHEV =
            Pattern.compile("[«]\\s*#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})(?i:([lmonkr]*))\\s*[»]");

    // long-form opens
    private static final Pattern TAG_GRAD_OPEN =
            Pattern.compile("(?i)<\\s*grad\\b([^>]*)>");

    // accept <rainbow>, <rb>, <rbw>
    private static final Pattern TAG_RBW_OPEN =
            Pattern.compile("(?i)<\\s*(?:rainbow|rb|rbw)(?:\\s+([^>]*))?\\s*>");

    private static final Pattern TAG_PULSE_OPEN =
            Pattern.compile("(?i)<\\s*pulse\\b([^>]*)>");

    private static final String LEG = "(?i)(?:§[0-9A-FK-OR])*";

    // closes
    private static final Pattern TAG_HEX_CLOSE =
            Pattern.compile(LEG + "</\\s*#\\s*>" + LEG);
    private static final Pattern TAG_HEX_CLOSE_CHEV =
            Pattern.compile(LEG + "[«]" + LEG + "/\\s*#" + LEG + "[»]" + LEG);

    private static final Pattern TAG_GRAD_CLOSE =
            Pattern.compile(LEG + "</\\s*grad\\s*>" + LEG);

    // accept </rainbow>, </rb>, </rbw>
    private static final Pattern TAG_RBW_CLOSE =
            Pattern.compile(LEG + "</\\s*(?:rainbow|rb|rbw)\\s*>" + LEG);

    private static final Pattern TAG_PULSE_CLOSE =
            Pattern.compile(LEG + "</\\s*pulse\\s*>" + LEG);

    private static final Pattern TAG_GRAD_CLOSE_CHEV =
            Pattern.compile(LEG + "[«]" + LEG + "/\\s*(?i:grad)" + LEG + "[»]" + LEG);

    private static final Pattern TAG_RBW_CLOSE_CHEV =
            Pattern.compile(LEG + "[«]" + LEG + "/\\s*(?i:(?:rainbow|rb|rbw))" + LEG + "[»]" + LEG);

    private static final Pattern TAG_PULSE_CLOSE_CHEV =
            Pattern.compile(LEG + "[«]" + LEG + "/\\s*(?i:pulse)" + LEG + "[»]" + LEG);

    // “any close” helpers
    private static final Pattern GRAD_CLOSE_ANY =
            Pattern.compile(LEG + "(?:</\\s*grad\\s*>|[«]" + LEG + "/\\s*(?:grad)" + LEG + "[»])" + LEG);

    private static final Pattern RBW_CLOSE_ANY =
            Pattern.compile(LEG + "(?:</\\s*(?:rainbow|rb|rbw)\\s*>"
                    + "|[«]" + LEG + "/\\s*(?:rainbow|rb|rbw)" + LEG + "[»])" + LEG);

    private static final Pattern PULSE_CLOSE_ANY =
            Pattern.compile(LEG + "(?:</\\s*pulse\\s*>|[«]" + LEG + "/\\s*(?:pulse)" + LEG + "[»])" + LEG);

    // chevron opens
    private static final Pattern TAG_GRAD_OPEN_CHEV =
            Pattern.compile("(?i)[«]\\s*grad\\b([^»]*)[»]");
    private static final Pattern TAG_RBW_OPEN_CHEV =
            Pattern.compile("(?i)[«]\\s*(?:rainbow|rb|rbw)(?:\\s+([^»]*))?\\s*[»]");
    private static final Pattern TAG_PULSE_OPEN_CHEV =
            Pattern.compile("(?i)[«]\\s*pulse\\b([^»]*)[»]");

// ─────────────────────────────────────────────
// SHORT-HAND OPEN TAGS (new)
// ─────────────────────────────────────────────

    // <g:#F00:#0F0>    → gradient
    private static final Pattern TAG_GRAD_SHORT =
            Pattern.compile("(?i)<\\s*g:([^>]*)>");

    // <pl:#FF00FF>     → pulse
    private static final Pattern TAG_PULSE_SHORT =
            Pattern.compile("(?i)<\\s*pl:([^>]*)>");

    // <wave:a:5:2>     → wave (type, speed, amp)
    private static final Pattern TAG_WAVE_SHORT =
            Pattern.compile("(?i)<\\s*wave:([a-z]):([0-9.]+):([0-9.]+)>");

    // <zoom:a:1.5:20>  → zoom (type, scale, cycle)
    private static final Pattern TAG_ZOOM_SHORT =
            Pattern.compile("(?i)<\\s*zoom:([a-z]):([0-9.]+):([0-9.]+)>");

    // <#FFD700> ... </#>
    private static final Pattern TAG_COLOR_BLOCK_OPEN =
            Pattern.compile("(?i)<#([0-9a-f]{6})>");
    private static final Pattern TAG_COLOR_BLOCK_CLOSE =
            Pattern.compile("(?i)</#>");

// ─────────────────────────────────────────────
// MISC EXISTING PATTERNS (unchanged except rainbow)
// ─────────────────────────────────────────────

    private static final boolean FORCE_HEX_EVERYWHERE = true;
    private static final String TOKEN_OPEN = "\ue000";
    private static final String TOKEN_CLOSE = "\ue001";
    private static final String TOKEN_TAG = "\ue002";
    private static final boolean BYPASS_ONLY_WHITELIST = true;

    private final Random obfRng = new Random();
    private final Map<Integer, char[]> obfByWidth = new HashMap();
    private static final String OBF_POOL =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()[]{}<>?/\\|+-_=;:,.'\\\"~";

    private static final Pattern HEX_WITH_STYLES =
            Pattern.compile("(?i)§#([0-9a-f]{6})([lmonkr]*)");

    // include rb / rbw in “any tag”
    private static final Pattern TAG_ANY = Pattern.compile(
            "(<grad[^>]*>"
                    + "|«grad[^»]*»"
                    + "|<(?:rainbow|rb|rbw)[^>]*>"
                    + "|«(?:rainbow|rb|rbw)[^»]*»"
                    + "|<pulse[^>]*>"
                    + "|«pulse[^»]*»"
                    + "|</grad>"
                    + "|«/grad»"
                    + "|</(?:rainbow|rb|rbw)>"
                    + "|«/(?:rainbow|rb|rbw)»"
                    + "|</pulse>"
                    + "|«/pulse»)",
            Pattern.CASE_INSENSITIVE
    );

    // strip patterns – only rainbow bits changed to (?:rainbow|rb|rbw)
// strip patterns – now includes all style tags
    private static final Pattern TAG_STRIP = Pattern.compile(
            "(</\\s*#\\s*>)"
                    + "|(<\\s*#?([0-9a-fA-F]{3}|[0-9a-fA-F]{6})(?i:[lmonkr]*)\\s*>)"
                    + "|([«]\\s*#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})(?i:[lmonkr]*)\\s*[»])"
                    + "|([«]\\s*/\\s*#\\s*[»])"

                    // <grad> and friends
                    + "|((?i)<\\s*grad\\b[^>]*>)"
                    + "|((?i)</\\s*grad\\s*>)"
                    + "|((?i)<\\s*(?:rain|rainbow|rb|rbw)\\b[^>]*>)"
                    + "|((?i)</\\s*(?:rain|rainbow|rb|rbw)\\s*>)"
                    + "|((?i)<\\s*pulse\\b[^>]*>)"
                    + "|((?i)</\\s*pulse\\s*>)"

                    // NEW: motion / FX tags
                    + "|((?i)<\\s*(?:wave|zoom|scroll|shake|jitter|wobble"
                    +              "|sparkle|flicker|glitch|outline|shadow|glow)\\b[^>]*>)"
                    + "|((?i)</\\s*(?:wave|zoom|scroll|shake|jitter|wobble"
                    +               "|sparkle|flicker|glitch|outline|shadow|glow)\\s*>)"

                    // «grad» etc.
                    + "|((?i)[«]\\s*grad\\b[^»]*[»])"
                    + "|((?i)[«]\\s*/\\s*grad\\s*[»])"
                    + "|((?i)[«]\\s*(?:rain|rainbow|rb|rbw)\\b[^»]*[»])"
                    + "|((?i)[«]\\s*/\\s*(?:rain|rainbow|rb|rbw)\\s*[»])"
                    + "|((?i)[«]\\s*pulse\\b[^»]*[»])"
                    + "|((?i)[«]\\s*/\\s*pulse\\s*[»])"

                    // «wave», «zoom», etc.
                    + "|((?i)[«]\\s*(?:wave|zoom|scroll|shake|jitter|wobble"
                    +              "|sparkle|flicker|glitch|outline|shadow|glow)\\b[^»]*[»])"
                    + "|((?i)[«]\\s*/\\s*(?:wave|zoom|scroll|shake|jitter|wobble"
                    +               "|sparkle|flicker|glitch|outline|shadow|glow)\\s*[»])"

                    // inline hex §#RRGGBB
                    + "|(?i)(§#[0-9a-fA-F]{6}[lmonkr]*)"
                    + "|(?i)(§#[0-9a-fA-F]{3}[lmonkr]*)"
    );

    // Match *any* vanilla style code: '§' plus the next char.
// Using '.' makes sure we never strip just '§' and leave the code char behind.
    private static final Pattern LEGACY_CTRL_ANY =
            Pattern.compile("§.", Pattern.DOTALL);

    private static final Pattern CTRL_STRIP =
            Pattern.compile("[\\p{Cntrl}&&[^\\n\\t]]");
    private static final Pattern ZW_STRIP =
            Pattern.compile("[\\u200B\\u200C\\u200D\\u2060]");
    private static final Pattern UNSUPPORTED_STRIP =
            Pattern.compile("[\\p{Cs}]");

    // include rb / rbw here too
// Any number of our angle-bracket / chevron tags at the front of the line,
// followed by at least one space. Used to re-inject tags on wrapped lines.
    private static final Pattern LEADING_TAGS_THEN_SPACE = Pattern.compile(
            "^(" +
                    // <#FFFFFF>, <#FFF>, etc. (+ optional legacy styles)
                    "(?:<\\s*#?[0-9a-fA-F]{3,6}(?i:[lmonkr]*)\\s*>)|" +
                    // «#FFFFFF», «#FFF»
                    "(?:[«]\\s*#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})(?i:[lmonkr]*)\\s*[»])|" +

                    // <grad ...> / <g:...> and chevrons
                    "(?:(?i)<\\s*(?:grad\\b|g:)[^>]*>)|" +
                    "(?:(?i)[«]\\s*(?:grad\\b|g:)[^»]*[»])|" +

                    // <rainbow ...>, <rb ...>, <rbw ...> and chevrons
                    "(?:(?i)<\\s*(?:rainbow|rb|rbw)\\b[^>]*>)|" +
                    "(?:(?i)[«]\\s*(?:rainbow|rb|rbw)\\b[^»]*[»])|" +

                    // <pulse ...>, <pl:...> and chevrons
                    "(?:(?i)<\\s*(?:pulse\\b|pl:)[^>]*>)|" +
                    "(?:(?i)[«]\\s*(?:pulse\\b|pl:)[^»]*[»])|" +

                    // Motion / FX tags: wave, zoom, shake, scroll, jitter, wobble,
                    // outline, shadow, sparkle, flicker, glitch (long forms)
                    "(?:(?i)<\\s*(?:wave|zoom|shake|scroll|jitter|wobble|outline|shadow|sparkle|flicker|glitch)\\b[^>]*>)|" +
                    "(?:(?i)[«]\\s*(?:wave|zoom|shake|scroll|jitter|wobble|outline|shadow|sparkle|flicker|glitch)\\b[^»]*[»])" +
                    ")+\\s+"
    );

    // Add near the other patterns at the top if you want:
    private static final String STYLE_TAG_NAMES =
            "(?:grad|rain|rainbow|rb|rbw|pulse"
                    + "|wave|zoom|scroll|shake|jitter|wobble"
                    + "|sparkle|flicker|glitch|outline|shadow|glow|#)";

    // Opening tags: <grad ...>, <wave ...>, <#...>, etc.
    private static final Pattern WRAP_PROTECT_OPEN =
            Pattern.compile("(?i)<\\s*" + STYLE_TAG_NAMES + "\\b[^>]*>");

    // Closing tags: </grad>, </wave>, </#>, etc.
    private static final Pattern WRAP_PROTECT_CLOSE =
            Pattern.compile("(?i)</\\s*" + STYLE_TAG_NAMES + "\\s*>");

    // (Optional) if you also want to protect « » variants:
    private static final Pattern WRAP_PROTECT_OPEN_CHEV =
            Pattern.compile("(?i)[«]\\s*" + STYLE_TAG_NAMES + "\\b[^»]*[»]");
    private static final Pattern WRAP_PROTECT_CLOSE_CHEV =
            Pattern.compile("(?i)[«]\\s*/\\s*" + STYLE_TAG_NAMES + "\\s*[»]");

    private static int vanillaColorFor(char k) {
        int idx = -1;
        if (k >= '0' && k <= '9') {
            idx = k - 48;
        } else if (k >= 'a' && k <= 'f') {
            idx = 10 + (k - 97);
        } else if (k >= 'A' && k <= 'F') {
            idx = 10 + (k - 65);
        }

        return idx >= 0 && idx < 16 ? VANILLA_RGB[idx] : -1;
    }

    // At top of HexFontRenderer (with your other patterns)
    private static final Pattern PROTECT_TAG =
            Pattern.compile("(?i)(<\\s*/?\\s*(grad|rain|rainbow|rb|rbw|pulse|wave|shake|scroll|zoom|jitter|wobble|sparkle|flicker|glitch|outline|shadow|glow)[^>]*>)");

    // ----------------------------------------------------------
//  Wrap helpers used by listFormattedStringToWidth
// ----------------------------------------------------------
    private static String preprocessForWrap(String s) {
        if (s == null || s.isEmpty()) return "";

        Matcher m = PROTECT_TAG.matcher(s);
        StringBuffer out = new StringBuffer();

        while (m.find()) {
            String tag = m.group(1); // the full <wave ...> or </wave>
            // Wrap just the TAG in a protected range:
            String wrapped = "\ue000" + tag + "\ue002";
            m.appendReplacement(out, Matcher.quoteReplacement(wrapped));
        }
        m.appendTail(out);

        return out.toString();
    }

    private static String postprocessAfterWrap(String s) {
        if (s == null || s.isEmpty()) return "";

        StringBuilder out = new StringBuilder();
        int len = s.length();
        int i = 0;

        while (i < len) {
            char ch = s.charAt(i);

            if (ch == '\ue000' || ch == '\ue001') {
                // restore everything up to the terminator
                int j = i + 1;
                while (j < len && s.charAt(j) != '\ue002') j++;
                if (j < len) j++; // include terminator

                // strip the sentinels themselves
                String inner = s.substring(i + 1, j - 1);
                out.append(inner);

                i = j;
                continue;
            }

            // normal char
            out.append(ch);
            i++;
        }

        return out.toString();
    }


    private static boolean isForgeModListLike() {
        GuiScreen s = Minecraft.getMinecraft().currentScreen;
        if (s == null) {
            return false;
        } else {
            String cn = s.getClass().getName();
            return cn.startsWith("cpw.mods.fml.client.GuiModList") || cn.startsWith("cpw.mods.fml.client.GuiScrollingList") || cn.startsWith("cpw.mods.fml.client.GuiSlotModList") || cn.startsWith("cpw.mods.fml.client.config.GuiConfig") || cn.contains("GuiModList") || cn.contains("GuiConfig");
        }
    }

    private static String applyHexShortcuts(String s) {
        if (s != null && s.indexOf(60) >= 0) {
            s = s.replace("<g_fire>", "<grad #FF2000 #FF5A00 #FFCC00 #FF5A00 #FF2000 scroll=0.20>");
            return s;
        } else {
            return s;
        }
    }

    private static String sanitize(String s) {
        if (s == null) {
            return null;
        } else {
            s = applyHexShortcuts(s);
            s = CTRL_STRIP.matcher(s).replaceAll("");
            s = ZW_STRIP.matcher(s).replaceAll("");
            s = s.replace(' ', ' ').replace(' ', ' ').replace(' ', ' ');
            return s;
        }
    }

    private void buildObfBuckets() {
        Map<Integer, ArrayList<Character>> tmp = new HashMap();

        for(int i = 0; i < "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()[]{}<>?/\\|+-_=;:,.'\\\"~".length(); ++i) {
            char ch = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()[]{}<>?/\\|+-_=;:,.'\\\"~".charAt(i);
            int w = this.base.getCharWidth(ch);
            if (w > 0) {
                ArrayList<Character> list = (ArrayList)tmp.get(w);
                if (list == null) {
                    list = new ArrayList();
                    tmp.put(w, list);
                }

                list.add(ch);
            }
        }

        for(Map.Entry<Integer, ArrayList<Character>> e : tmp.entrySet()) {
            ArrayList<Character> list = (ArrayList)e.getValue();
            char[] arr = new char[list.size()];

            for(int i = 0; i < arr.length; ++i) {
                arr[i] = (Character)list.get(i);
            }

            this.obfByWidth.put(e.getKey(), arr);
        }

    }
    // Move the first global style tag (<grad>, <wave>, etc.) to the *front*
// of the string, so it wraps the entire message (including "<Player>").
    private static String hoistGlobalHeader(String s) {
        if (s == null || s.isEmpty()) return s;

        Matcher m = GLOBAL_HEADER_TAG.matcher(s);
        if (!m.find()) {
            return s; // no global tag → nothing to do
        }

        String header = m.group(1);       // the full "<wave ...>" or "<grad ...>"
        int start = m.start(1);

        // If it's already at the very front, we don't change anything.
        if (start == 0) {
            return s;
        }

        // Move the header to the front and remove it from its original spot
        String before = s.substring(0, start);
        String after  = s.substring(start + header.length());

        return header + before + after;
    }

    private char obfuscateCharSameWidth(char original) {
        // get the width of the original character
        int w = this.base.getCharWidth(original);

        if (w <= 0) {
            return original;
        }

        // lookup bucket for this width
        char[] bucket = (char[]) this.obfByWidth.get(w);

        if (bucket != null && bucket.length > 0) {
            return bucket[this.obfRng.nextInt(bucket.length)];
        }

        // fallback character set
        final String fallback =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789" +
                        "!@#$%^&*()[]{}<>?/\\|+-_=;:,.'\\\"~";

        return fallback.charAt(this.obfRng.nextInt(fallback.length()));
    }

    private static String stylesToLegacy(String styles) {
        if (styles != null && !styles.isEmpty()) {
            StringBuilder sb = new StringBuilder(styles.length() * 2);

            for(char ch : styles.toLowerCase().toCharArray()) {
                switch (ch) {
                    case 'k':
                        sb.append("§k");
                        break;
                    case 'l':
                        sb.append("§l");
                        break;
                    case 'm':
                        sb.append("§m");
                        break;
                    case 'n':
                        sb.append("§n");
                        break;
                    case 'o':
                        sb.append("§o");
                    case 'p':
                    case 'q':
                    default:
                        break;
                    case 'r':
                        sb.append("§r");
                }
            }

            return sb.toString();
        } else {
            return "";
        }
    }

    private static String stripLeadingTagSpace(String s) {
        return s != null && !s.isEmpty() ? LEADING_TAGS_THEN_SPACE.matcher(s).replaceFirst("$1") : s;
    }

    private static String cleanPayload(String s) {
        if (s == null) {
            return "";
        } else {
            s = sanitize(s);
            s = stripLeadingTagSpace(s);
            s = TAG_STRIP.matcher(s).replaceAll("");
            return s;
        }
    }

    public HexFontRenderer(FontRenderer base) {
        super(
                Minecraft.getMinecraft().gameSettings,
                // Keep using whatever texture the original font was using
                resolveFontTexture(base),
                Minecraft.getMinecraft().getTextureManager(),
                base.getUnicodeFlag()
        );

        // Unwrap if someone passes us another HexFontRenderer
        FontRenderer effectiveBase = base;
        if (base instanceof HexFontRenderer) {
            try {
                Field f = HexFontRenderer.class.getDeclaredField("base");
                f.setAccessible(true);
                FontRenderer inner = (FontRenderer) f.get(base);
                if (inner != null) {
                    effectiveBase = inner;
                }
            } catch (Throwable ignored) {}
        }

        this.base = effectiveBase;

        // Copy over state from the vanilla renderer so our parent behaves identically
        this.fontRandom = this.base.fontRandom;
        this.setUnicodeFlag(this.base.getUnicodeFlag());
        this.setBidiFlag(this.base.getBidiFlag());

        // Optional but handy: one-time debug so we know what we're wrapping
        try {
            System.out.println("[HexFont] HexFontRenderer created, wrapping " + this.base.getClass().getName());
        } catch (Throwable ignored) {}

        // If you have any extra setup (bucket maps, regex compilation, etc.), keep it here:
        this.buildObfBuckets();
    }

    private static ResourceLocation resolveFontTexture(FontRenderer base) {
        try {
            Field f;
            try {
                f = FontRenderer.class.getDeclaredField("locationFontTexture");
            } catch (NoSuchFieldException var3) {
                f = FontRenderer.class.getDeclaredField("field_111273_g");
            }

            f.setAccessible(true);
            return (ResourceLocation)f.get(base);
        } catch (Throwable var4) {
            return new ResourceLocation("textures/font/ascii.png");
        }
    }

    private void syncBaseStateFromThis() {
        try {
            // sync unicode flag
            this.base.setUnicodeFlag(this.getUnicodeFlag());

            // sync bidi flag (right-to-left languages)
            this.base.setBidiFlag(this.getBidiFlag());
        } catch (Throwable ignored) {}
    }


    private static boolean isFromClassPrefix(String fqcnPrefix) {
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        int n = Math.min(st.length, 48);

        for(int i = 2; i < n; ++i) {
            String cn = st[i].getClassName();
            if (cn != null && cn.startsWith(fqcnPrefix)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isFromAnyPackage(String[] needles) {
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        int n = Math.min(st.length, 64);

        for(int i = 2; i < n; ++i) {
            String cn = st[i].getClassName();
            if (cn != null) {
                String lc = cn.toLowerCase();

                for(String needle : needles) {
                    if (lc.contains(needle)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean isJourneyMapManageWaypointsScreen() {
        GuiScreen s = Minecraft.getMinecraft().currentScreen;
        if (s == null) {
            return false;
        } else {
            String cn = s.getClass().getName().toLowerCase();
            return cn.contains("journeymap") && cn.contains("waypoint");
        }
    }

    private static boolean isForgeModListScreen() {
        GuiScreen s = Minecraft.getMinecraft().currentScreen;
        if (s == null) {
            return false;
        } else {
            String cn = s.getClass().getName();
            return cn.startsWith("cpw.mods.fml.client.GuiModList") || cn.contains("GuiModList") || cn.startsWith("cpw.mods.fml.client.GuiScrollingList") || cn.startsWith("cpw.mods.fml.client.GuiSlotModList");
        }
    }

    private static boolean isForgeConfigGui() {
        GuiScreen s = Minecraft.getMinecraft().currentScreen;
        if (s == null) {
            return false;
        } else {
            String cn = s.getClass().getName();
            return cn.startsWith("cpw.mods.fml.client.config.GuiConfig") || cn.contains("GuiConfig");
        }
    }


    private static boolean hasHexControls(String s) {
        return s != null && s.indexOf(167) >= 0 && s.contains("§#");
    }

    private static boolean hasOurTags(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }

        // Any hit in TAG_STRIP means we found one of "our" constructs:
        //  - <grad>, <rain>, <rainbow>, <pulse>
        //  - <wave>, <zoom>, <scroll>, <shake>, <jitter>, <wobble>
        //  - <sparkle>, <flicker>, <glitch>, <outline>, <shadow>, <glow>
        //  - chevron forms «...»
        //  - inline §#RRGGBB / §#RGB, etc.
        return TAG_STRIP.matcher(s).find();
    }


    private static boolean looksLikeOurSyntax(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }

        // Any of our known <grad>/<rain>/<pulse>/<#...> tags (including « » variants)
        if (TAG_STRIP.matcher(s).find()) {
            return true;
        }

        // Inline §#RRGGBB without tags
        if (hasHexControls(s)) {
            return true;
        }

        // No HexColorCodes syntax at all → treat as vanilla text
        return false;
    }



    @Override
    public int getStringWidth(String text) {
        // CNPC GUIs must use vanilla metrics
        if (isCustomNpcScreen()) {
            return this.base.getStringWidth(text == null ? "" : text);
        }

        if (text == null) {
            return 0;
        }

        // Keep whatever sanitize() you already use for debug / control chars
        String s = sanitize(text);
        if (s == null || s.isEmpty()) {
            return 0;
        }

        // Let vanilla handle mod-list / JM special screens if you want:
        if (isForgeModListLike() || isJourneyMapManageWaypointsScreen()) {
            return this.base.getStringWidth(s);
        }

        // If the string has none of our tags or hex controls, just delegate
        if (!hasOurTags(s) && !hasHexControls(s)) {
            return this.base.getStringWidth(s);
        }

        // Strip our <grad>/<pulse>/<wave>/… tags and § codes → visible text
        String plain = TAG_STRIP.matcher(s).replaceAll("");
        plain = LEGACY_CTRL_ANY.matcher(plain).replaceAll("");

        return this.base.getStringWidth(plain);
    }

    @Override
    public String trimStringToWidth(String text, int width, boolean reverse) {
        // CNPC GUIs must use vanilla trimming
        if (isCustomNpcScreen()) {
            return this.base.trimStringToWidth(text == null ? "" : text, width, reverse);
        }

        // Leave special GUIs alone
        if (isForgeModListLike() || isJourneyMapManageWaypointsScreen()) {
            return this.base.trimStringToWidth(text, width, reverse);
        }

        String s = sanitize(text);
        if (s == null || s.isEmpty()) {
            return s;
        }

        // Only do tag-aware logic when needed
        if (!hasOurTags(s) && !hasHexControls(s)) {
            return this.base.trimStringToWidth(s, width, reverse);
        }

        s = stripLeadingTagSpace(s);

        // 1) "visible only" string for measuring
        String plain = TAG_STRIP.matcher(s).replaceAll("");
        plain = LEGACY_CTRL_ANY.matcher(plain).replaceAll("");

        // 2) Ask vanilla how many visible chars fit
        String cutPlain = this.base.trimStringToWidth(plain, width, reverse);
        int need = cutPlain.length();

        if (!reverse) {
            int i = 0;
            int produced = 0;
            int n = s.length();

            while (i < n && produced < need) {
                int next = skipAnyTagForward(s, i);
                if (next != i) {
                    // skipped a tag / § code (zero-width)
                    i = next;
                } else {
                    i++;
                    produced++;
                }
            }

            return s.substring(0, i);
        } else {
            int i = s.length();
            int produced = 0;

            while (i > 0 && produced < need) {
                int prev = skipAnyTagBackward(s, i);
                if (prev != i) {
                    i = prev;
                } else {
                    i--;
                    produced++;
                }
            }

            return s.substring(i);
        }
    }

    // Canonical style tag names we treat as "animated blocks"
    private static final String[] ANIMATED_TAG_NAMES = new String[] {
            "grad",
            "rainbow",
            "pulse",
            "wave",
            "zoom",
            "scroll",
            "shake",
            "wobble",
            "jitter",
            "outline",
            "shadow",
            "glow",
            "sparkle",
            "flicker",
            "glitch"
    };

    /**
     * Parse a tag like:
     *   "<grad #F00 #0F0>"
     *   "«grad #F00 #0F0»"
     *   "</wave>"
     *   "«/wave»"
     *
     * and return the canonical style name ("grad", "rainbow", "wave", etc.)
     * or null if it's not one of our animated/FX tags.
     */
    private static String getAnimatedTagName(String raw) {
        if (raw == null) return null;

        String t = raw.trim().toLowerCase();

        // strip leading '<' / '«'
        int idx = 0;
        if (idx < t.length() && (t.charAt(idx) == '<' || t.charAt(idx) == '«')) {
            idx++;
        }

        // optional '/'
        if (idx < t.length() && t.charAt(idx) == '/') {
            idx++;
        }

        // skip whitespace
        while (idx < t.length() && Character.isWhitespace(t.charAt(idx))) {
            idx++;
        }

        // read the tag name [a-z]+
        int start = idx;
        while (idx < t.length()) {
            char c = t.charAt(idx);
            if (!Character.isLetter(c)) break;
            idx++;
        }
        if (idx <= start) {
            return null;
        }

        String name = t.substring(start, idx);

        // normalize rainbow aliases if you ever see them here
        if ("rain".equals(name) || "rb".equals(name) || "rbw".equals(name)) {
            name = "rainbow";
        }

        // check against our canonical list
        for (String allowed : ANIMATED_TAG_NAMES) {
            if (allowed.equals(name)) {
                return name;
            }
        }

        return null;
    }

    private static boolean isAnimatedOpenTag(String tag) {
        if (tag == null) return false;
        String t = tag.trim().toLowerCase();

        // opening tags start with "<name" or "«name" (no slash right after)
        if (!(t.startsWith("<") || t.startsWith("«"))) {
            return false;
        }
        // "< /" or "« /" → not an opener
        int idx = 1;
        if (idx < t.length() && t.charAt(idx) == '/') {
            return false;
        }

        return getAnimatedTagName(tag) != null;
    }
    private static boolean isCustomNpcScreen() {
        GuiScreen s = Minecraft.getMinecraft().currentScreen;
        if (s == null) return false;

        String cn = s.getClass().getName();
        String lc = cn.toLowerCase();

        // --------------------------------------------------------
        // 1) EXPLICITLY SKIP SCRIPT / SCRIPTER GUIs
        //    → we *do not* want to disable HexFont globally
        //      just because the Scripter tool is open.
        // --------------------------------------------------------
        if (cn.startsWith("noppes.npcs.scripted.gui")
                || lc.contains("scriptgui")
                || lc.contains("guiscript")
                || lc.contains("scriptconsole")
                || lc.contains("scriptlanguages")
                || lc.contains("scriptmenu")) {
            return false;
        }

        // --------------------------------------------------------
        // 2) Normal CNPC client GUIs (dialogs, editors, etc.)
        //    These should still bypass HexFont like before.
        // --------------------------------------------------------
        if (cn.startsWith("noppes.npcs.client.gui")) {
            return true;
        }

        // --------------------------------------------------------
        // 3) Fallback for any other CNPC GUIs that aren’t in the
        //    usual package, but are *not* script-related.
        // --------------------------------------------------------
        if ((lc.contains("noppes.npcs") || lc.contains("customnpc"))
                && !lc.contains("script")) {
            return true;
        }

        return false;
    }


    private static boolean shouldBypassCustomRendering() {
        // Forge mod list / config → always bypass
        if (isForgeModListScreen() || isForgeConfigGui()) {
            return true;
        }

        // Our “special” renderers that SHOULD use Hex effects:
        if (isFromClassPrefix("net.minecraft.client.gui.GuiNewChat")) return false;
        if (isFromClassPrefix("net.minecraft.client.gui.GuiIngame")) return false;

        GuiScreen s = Minecraft.getMinecraft().currentScreen;
        if (s == null) return false;

        if (s instanceof GuiChat)      return false;
        if (s instanceof GuiInventory) return false;
        if (s instanceof GuiContainer) return false;

        // NEW: CustomNPCs → **always bypass**
        if (isCustomNpcScreen()) return true;

        // Journeymap waypoints etc → bypass
        if (isJourneyMapManageWaypointsScreen()) return true;

        return false;
    }


    private static boolean isAnimatedCloseTag(String tag) {
        if (tag == null) return false;
        String t = tag.trim().toLowerCase();

        // closing tags: "</name>" or "«/name»"
        if (!(t.startsWith("</") || t.startsWith("«/"))) {
            return false;
        }

        return getAnimatedTagName(tag) != null;
    }

    /**
     * Scan a wrapped line and figure out which animated blocks
     * (grad/rainbow/pulse/wave/zoom/etc.) are still open at the end.
     *
     * Returns a prefix like:
     *   "<grad><wave>"
     *
     * that you can prepend to the *next* line so the effects continue.
     */
    public static String carryAnimatedOpeners(String line) {
        if (line == null || line.isEmpty()) {
            return "";
        }

        Deque<String> stack = new ArrayDeque<String>();

        int len = line.length();
        for (int i = 0; i < len; i++) {
            char c = line.charAt(i);
            if (c != '<' && c != '«') {
                continue;
            }

            char closeChar = (c == '<') ? '>' : '»';
            int end = line.indexOf(closeChar, i + 1);
            if (end == -1) {
                break; // malformed tag, bail out
            }

            String tag = line.substring(i, end + 1);

            if (isAnimatedOpenTag(tag)) {
                String name = getAnimatedTagName(tag);
                if (name != null) {
                    stack.push(name);
                }
            } else if (isAnimatedCloseTag(tag)) {
                String name = getAnimatedTagName(tag);
                if (name != null && !stack.isEmpty()) {
                    // remove the first matching from the top
                    java.util.Iterator<String> it = stack.iterator();
                    while (it.hasNext()) {
                        if (it.next().equals(name)) {
                            it.remove();
                            break;
                        }
                    }
                }
            }

            i = end; // jump past this tag
        }

        if (stack.isEmpty()) {
            return "";
        }

        // Rebuild open tags in outermost→innermost order
        StringBuilder prefix = new StringBuilder();
        java.util.Iterator<String> it = stack.descendingIterator();
        while (it.hasNext()) {
            String name = it.next();
            prefix.append('<').append(name).append('>');
        }

        return prefix.toString();
    }

    private static String findUnclosedBlock(String line, Pattern openAngle, Pattern openChev, Pattern closeAngle, Pattern closeChev) {
        if (line != null && !line.isEmpty()) {
            int lastOpenStart = -1;
            int lastOpenEnd = -1;

            for(Matcher moA = openAngle.matcher(line); moA.find(); lastOpenEnd = moA.end()) {
                lastOpenStart = moA.start();
            }

            Matcher moC = openChev.matcher(line);

            while(moC.find()) {
                if (moC.start() > lastOpenStart) {
                    lastOpenStart = moC.start();
                    lastOpenEnd = moC.end();
                }
            }

            if (lastOpenStart < 0) {
                return "";
            } else {
                boolean closed = false;
                Matcher mcA = closeAngle.matcher(line).region(lastOpenEnd, line.length());
                if (mcA.find()) {
                    closed = true;
                }

                Matcher mcC = closeChev.matcher(line).region(lastOpenEnd, line.length());
                if (mcC.find()) {
                    closed = true;
                }

                if (closed) {
                    return "";
                } else {
                    return line.substring(lastOpenStart, lastOpenEnd);
                }
            }
        } else {
            return "";
        }
    }
    @Override
    public List<String> listFormattedStringToWidth(String text, int width) {
        // CNPC GUIs must use vanilla wrapping completely
        if (isCustomNpcScreen()) {
            // mimic vanilla null behaviour
            if (text == null) {
                List<String> out = new ArrayList<String>();
                out.add("");
                return out;
            }
            return this.base.listFormattedStringToWidth(text, width);
        }

        // Safety / null behaviour identical to vanilla
        if (text == null) {
            List<String> out = new ArrayList<String>();
            out.add("");
            return out;
        }

        // Keep all the “special screens” bypasses you already had
        if (isForgeModListLike()) {
            return this.base.listFormattedStringToWidth(text, width);
        }
        if (isJourneyMapManageWaypointsScreen()) {
            return this.base.listFormattedStringToWidth(text, width);
        }

        // If it DOESN'T look like our <grad>/<wave>/etc syntax,
        // don't touch it – let vanilla handle colors & wrapping.
        if (!looksLikeOurSyntax(text)) {
            return this.base.listFormattedStringToWidth(text, width);
        }

        // From here on: text *does* use our syntax, so we run the
        // custom wrapping logic.

        this.syncBaseStateFromThis();

        System.out.println("[HexWrap] ========= listFormattedStringToWidth =========");
        System.out.println("[HexWrap] IN text='" + text + "'");
        System.out.println("[HexWrap] width=" + width);

        // 1) sanitize + hoist our global header tags
        String s = sanitize(text);
        System.out.println("[HexWrap] sanitize -> '" + s + "'");
        s = hoistGlobalHeader(s);
        System.out.println("[HexWrap] hoistGlobalHeader -> '" + s + "'");

        if (s.isEmpty()) {
            List<String> out = new ArrayList<String>();
            out.add("");
            return out;
        }

        // 2) protect tags so width calc only sees visible glyphs
        String protectedS = preprocessForWrap(s);
        System.out.println("[HexWrap] protectedS='" + protectedS + "'");

        List<String> lines = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        int visibleWidth = 0;

        int len = protectedS.length();
        int i = 0;

        while (i < len) {
            char ch = protectedS.charAt(i);

            // protected ranges (our fake runes)
            if (ch == '\ue000' || ch == '\ue001') {
                int j = i + 1;
                while (j < len && protectedS.charAt(j) != '\ue002') {
                    j++;
                }
                if (j < len) j++; // skip terminator
                current.append(protectedS, i, j);
                i = j;
                continue;
            }

            // vanilla § formatting – copy as-is, zero width
            if (ch == '§' && i + 1 < len) {
                current.append(protectedS, i, i + 2);
                i += 2;
                continue;
            }

            int w = this.base.getCharWidth(ch);

            if (visibleWidth + w > width && visibleWidth > 0) {
                String line = stripLeadingTagSpace(current.toString());
                if (!line.isEmpty()) {
                    lines.add(line);
                }
                System.out.println("[HexWrap] break line -> '" + line + "'");
                current.setLength(0);
                visibleWidth = 0;
                // re-handle current ch as first char of next line
            } else {
                current.append(ch);
                visibleWidth += w;
                i++;
            }
        }

        if (current.length() > 0) {
            String line = stripLeadingTagSpace(current.toString());
            lines.add(line);
            System.out.println("[HexWrap] final line -> '" + line + "'");
        }

        if (lines.isEmpty()) {
            lines.add("");
        }

        // 3) restore real tags per line
        List<String> restored = new ArrayList<String>(lines.size());
        for (int li = 0; li < lines.size(); li++) {
            String before = lines.get(li);
            String after = postprocessAfterWrap(before);
            restored.add(after);
            System.out.println("[HexWrap] postprocess line[" + li + "] '" + before + "' -> '" + after + "'");
        }

        // 4) carry vanilla § formats + our animated tags across wrapped lines
        @SuppressWarnings({ "rawtypes", "unchecked" })
        List<String> carried = HexChatWrapFix.carryAnimatedAcross((List) restored);
        for (int li = 0; li < carried.size(); li++) {
            System.out.println("[HexWrap] CARRIED line[" + li + "] = '" + carried.get(li) + "'");
        }

        System.out.println("[HexWrap] ==============================================");

        return carried;
    }


    public String safeTrimStringToWidth(String text, int width) {
        System.out.println("[HexWrap] safeTrimStringToWidth text='" + text + "' width=" + width);


        if (text == null || text.isEmpty()) return "";

        int currentWidth = 0;
        int i = 0;
        int len = text.length();

        while (i < len) {
            char c = text.charAt(i);

            // Skip <tags ...>
            if (c == '<') {
                int end = text.indexOf('>', i);
                if (end != -1) {
                    i = end + 1;
                    continue;
                }
            }

            // Skip § codes
            if (c == '§' && i + 1 < len) {
                i += 2;
                continue;
            }

            // Visible char width using THIS renderer instance
            currentWidth += this.getCharWidth(c);

            if (currentWidth > width)
                break;

            i++;
        }

        return text.substring(0, i);
    }



    private static int skipAnyTagForward(String s, int from) {
        Matcher m = TAG_STRIP.matcher(s);
        if (m.region(from, s.length()).lookingAt()) {
            return m.end();
        } else {
            Matcher l = LEGACY_CTRL_ANY.matcher(s);
            return l.region(from, s.length()).lookingAt() ? l.end() : from;
        }
    }

    private static int skipAnyTagBackward(String s, int endExclusive) {
        Matcher m = TAG_STRIP.matcher(s);

        while(m.find()) {
            if (m.end() == endExclusive) {
                return m.start();
            }

            if (m.end() > endExclusive) {
                break;
            }
        }

        Matcher l = LEGACY_CTRL_ANY.matcher(s);

        while(l.find()) {
            if (l.end() == endExclusive) {
                return l.start();
            }

            if (l.end() > endExclusive) {
                break;
            }
        }

        return endExclusive;
    }

    private static String dropTagOnlyPrefix(String s) {
        if (s != null && !s.isEmpty()) {
            int i = 0;

            int n;
            int next;
            for(n = s.length(); i < n; i = next) {
                next = skipAnyTagForward(s, i);
                if (next == i) {
                    break;
                }
            }

            return i > 0 && i <= n ? s.substring(i) : s;
        } else {
            return s;
        }
    }

    private static String applyAndStripLegacyCodes(String s, LegacyState st) {
        if (s != null && !s.isEmpty()) {
            StringBuilder vis = new StringBuilder(s.length());
            int i = 0;
            int n = s.length();

            while(i < n) {
                char c = s.charAt(i);
                Matcher m = HEX_WITH_STYLES.matcher(s.substring(i));
                if (m.lookingAt()) {
                    String hex = m.group(1);
                    String styles = m.group(2);
                    if (styles != null && !styles.isEmpty()) {
                        for(char flag : styles.toLowerCase().toCharArray()) {
                            switch (flag) {
                                case 'k':
                                    st.obfuscated = true;
                                    st.obfOnce = true;
                                    break;
                                case 'l':
                                    st.bold = true;
                                    break;
                                case 'm':
                                    st.strikethrough = true;
                                    break;
                                case 'n':
                                    st.underline = true;
                                    break;
                                case 'o':
                                    st.italic = true;
                                case 'p':
                                case 'q':
                                default:
                                    break;
                                case 'r':
                                    st.reset();
                            }
                        }
                    }

                    i += m.end();
                } else if (c == 167 && i + 1 < n) {
                    char k = Character.toLowerCase(s.charAt(i + 1));
                    switch (k) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                        case 'a':
                        case 'b':
                        case 'c':
                        case 'd':
                        case 'e':
                        case 'f':
                        case 'r':
                            st.reset();
                        case ':':
                        case ';':
                        case '<':
                        case '=':
                        case '>':
                        case '?':
                        case '@':
                        case 'A':
                        case 'B':
                        case 'C':
                        case 'D':
                        case 'E':
                        case 'F':
                        case 'G':
                        case 'H':
                        case 'I':
                        case 'J':
                        case 'K':
                        case 'L':
                        case 'M':
                        case 'N':
                        case 'O':
                        case 'P':
                        case 'Q':
                        case 'R':
                        case 'S':
                        case 'T':
                        case 'U':
                        case 'V':
                        case 'W':
                        case 'X':
                        case 'Y':
                        case 'Z':
                        case '[':
                        case '\\':
                        case ']':
                        case '^':
                        case '_':
                        case '`':
                        case 'g':
                        case 'h':
                        case 'i':
                        case 'j':
                        case 'p':
                        case 'q':
                        default:
                            break;
                        case 'k':
                            st.obfuscated = true;
                            st.obfOnce = true;
                            break;
                        case 'l':
                            st.bold = true;
                            break;
                        case 'm':
                            st.strikethrough = true;
                            break;
                        case 'n':
                            st.underline = true;
                            break;
                        case 'o':
                            st.italic = true;
                    }

                    i += 2;
                } else {
                    vis.append(c);
                    ++i;
                }
            }

            return vis.toString();
        } else {
            return s;
        }
    }

    private static String legacyPrefix(LegacyState st) {
        StringBuilder sb = new StringBuilder(10);
        if (st.bold) {
            sb.append("§l");
        }

        if (st.italic) {
            sb.append("§o");
        }

        if (st.underline) {
            sb.append("§n");
        }

        if (st.strikethrough) {
            sb.append("§m");
        }

        if (st.obfuscated) {
            sb.append("§k");
        }

        return sb.toString();
    }

    private static String legacyPrefixNoObf(LegacyState st) {
        StringBuilder sb = new StringBuilder(10);
        if (st.bold) {
            sb.append("§l");
        }

        if (st.italic) {
            sb.append("§o");
        }

        if (st.underline) {
            sb.append("§n");
        }

        if (st.strikethrough) {
            sb.append("§m");
        }

        return sb.toString();
    }

    @Override
    public int drawString(String text, int x, int y, int color, boolean shadow) {
        // ⬅ NEW: never use HexFontRenderer inside CNPC GUIs
        if (isCustomNpcScreen()) {
            return this.base.drawString(text, x, y, color, shadow);
        }

        if (isForgeModListLike()) {
            return this.base.drawString(text, x, y, color, shadow);
        }

        this.syncBaseStateFromThis();

        if (isJourneyMapManageWaypointsScreen()) {
            return this.base.drawString(text, x, y, color, shadow);
        }

        // If it does NOT look like our <grad>/<wave>/etc syntax, bail out
        if (!looksLikeOurSyntax(text)) {
            return this.base.drawString(text, x, y, color, shadow);
        }

        // Only our syntax reaches here
        return this.drawHexAware(text, x, y, color, shadow);
    }

    @Override
    public int drawStringWithShadow(String text, int x, int y, int color) {
        // Never use HexFontRenderer inside CNPC GUIs
        if (isCustomNpcScreen()) {
            return this.base.drawStringWithShadow(text, x, y, color);
        }

        if (isForgeModListLike()) {
            return this.base.drawStringWithShadow(text, x, y, color);
        }

        this.syncBaseStateFromThis();

        if (isJourneyMapManageWaypointsScreen()) {
            return this.base.drawStringWithShadow(text, x, y, color);
        }

        // If it does NOT look like our <grad>/<wave>/etc syntax, bail out
        if (!looksLikeOurSyntax(text)) {
            return this.base.drawStringWithShadow(text, x, y, color);
        }

        // Only our tagged messages reach here
        return this.drawHexAware(text, x, y, color, true);
    }

    private static String dropOurTagsOnlyPrefix(String s) {
        if (s != null && !s.isEmpty()) {
            int i = 0;

            int n;
            Matcher m;
            for(n = s.length(); i < n; i = m.end()) {
                m = TAG_STRIP.matcher(s);
                if (!m.region(i, n).lookingAt()) {
                    break;
                }
            }

            return i > 0 && i <= n ? s.substring(i) : s;
        } else {
            return s;
        }
    }

    private static boolean startsWithColorOrReset(String s) {
        if (s != null && s.length() >= 2) {
            if (s.charAt(0) != 167) {
                return false;
            } else {
                char k = Character.toLowerCase(s.charAt(1));
                return k >= '0' && k <= '9' || k >= 'a' && k <= 'f' || k == 'r';
            }
        } else {
            return false;
        }
    }

    private static int skipLegacyCodes(String s, int i) {
        for(int n = s.length(); i + 1 < n && s.charAt(i) == 167; i += 2) {
            char k = Character.toLowerCase(s.charAt(i + 1));
            if ("0123456789abcdefklmnor".indexOf(k) < 0) {
                break;
            }
        }

        return i;
    }

    private int drawHexAware(String text, int x, int y, int fallbackColor, boolean shadow) {
        // 1) Always sanitize first
        text = sanitize(text);
        if (text == null || text.length() == 0) {
            return 0;
        }

        // 2) Normalize §# → <#...> tags, then strip inline hex so our tag parser owns them
        if (text.indexOf('§') >= 0) {
            text = normalizeControlsToTags(text);
            text = postNormalizeCleanup(text);
            text = stripHexAndReset(text);
        } else {
            text = stripHexAndReset(text);
        }

        // 3) Trim junk spaces after leading tags
        text = stripLeadingTagSpace(text);

        // 4) If there are no tags at all, just use vanilla
        if (text.indexOf('<') < 0 && text.indexOf('«') < 0) {
            return this.base.drawString(text, x, y, fallbackColor, shadow);
        }

        int cursorX = x;
        int maxRight = x;

        boolean hexActive = false;
        int activeHexRGB = fallbackColor;

        Deque<Integer> colorStack = new ArrayDeque<Integer>();
        colorStack.push(fallbackColor);

        List<Op> ops = this.parseToOps(text);
        LegacyState legacy = new LegacyState();

        for (Op op : ops) {
            switch (op.kind) {

                case TEXT:
                    if (op.payload != null && op.payload.length() > 0) {
                        String seg = dropOurTagsOnlyPrefix(op.payload);
                        int adv = this.drawTextWithLegacyInline(
                                seg,
                                cursorX,
                                y,
                                hexActive ? activeHexRGB : fallbackColor,
                                shadow,
                                legacy
                        );
                        cursorX += adv;
                        maxRight = Math.max(maxRight, cursorX);
                    }
                    break;

                case PUSH_HEX:
                    colorStack.push(activeHexRGB);
                    activeHexRGB = op.rgb;
                    hexActive = true;
                    break;

                case POP_HEX:
                    activeHexRGB = colorStack.isEmpty() ? fallbackColor : colorStack.pop();
                    hexActive = (activeHexRGB != fallbackColor);
                    break;

                case GRADIENT_MULTI: {
                    Integer saved = legacy.colorRgb;
                    String fmt = legacyPrefixNoObf(legacy)
                            + (op.legacyFromTag == null ? "" : op.legacyFromTag);

                    int adv = this.drawGradientMulti(
                            op.payload,
                            cursorX,
                            y,
                            op.stops,
                            shadow,
                            op.scroll,
                            op.speed,
                            op.pulseOn,
                            op.pulseAmp,
                            op.pulseSpeed,
                            fmt
                    );

                    legacy.colorRgb = saved;
                    cursorX += adv;
                    maxRight = Math.max(maxRight, cursorX);
                    break;
                }

                case RAINBOW_TEXT: {
                    Integer saved = legacy.colorRgb;
                    String fmt = legacyPrefixNoObf(legacy)
                            + (op.legacyFromTag == null ? "" : op.legacyFromTag);

                    int adv = this.drawRainbowAndReturnAdvanceCustom(
                            op.payload,
                            cursorX,
                            y,
                            shadow,
                            op.cycles,
                            op.sat,
                            op.val,
                            op.phase,
                            op.speed,
                            op.pulseOn,
                            op.pulseAmp,
                            op.pulseSpeed,
                            fmt
                    );

                    legacy.colorRgb = saved;
                    cursorX += adv;
                    maxRight = Math.max(maxRight, cursorX);
                    break;
                }

                case PULSE_HEX: {
                    Integer saved = legacy.colorRgb;
                    String fmt = legacyPrefixNoObf(legacy)
                            + (op.legacyFromTag == null ? "" : op.legacyFromTag);
                    int rgb = op.solid >= 0 ? op.solid : 0xFFFFFF;

                    int adv = this.drawPulse(
                            op.payload,
                            cursorX,
                            y,
                            shadow,
                            rgb,
                            op.speed > 0.0F ? op.speed : 1.0F,
                            op.amp > 0.0F ? op.amp : 0.25F,
                            fmt
                    );

                    legacy.colorRgb = saved;
                    cursorX += adv;
                    maxRight = Math.max(maxRight, cursorX);
                    break;
                }

                case WAVE_TEXT:
                {
                    int baseColor = (op.rgb >= 0 ? op.rgb : (hexActive ? activeHexRGB : fallbackColor));
                    cursorX += drawWave(
                            op.payload,
                            cursorX,
                            y,
                            baseColor,
                            shadow
                    );
                    maxRight = Math.max(maxRight, cursorX);
                    break;
                }


                case SHAKE_TEXT:
                {
                    int baseColor = (op.rgb >= 0 ? op.rgb : (hexActive ? activeHexRGB : fallbackColor));
                    cursorX += drawShake(
                            op.payload,
                            cursorX,
                            y,
                            baseColor,
                            shadow
                    );
                    maxRight = Math.max(maxRight, cursorX);
                    break;
                }

                case ZOOM_TEXT:
                {
                    int baseColor = (op.rgb >= 0 ? op.rgb : (hexActive ? activeHexRGB : fallbackColor));
                    cursorX += drawZoom(
                            op.payload,
                            cursorX,
                            y,
                            baseColor,
                            shadow
                    );
                    maxRight = Math.max(maxRight, cursorX);
                    break;
                }

                case RAIN_TEXT:
                {
                    int baseColor = (op.rgb >= 0 ? op.rgb : (hexActive ? activeHexRGB : fallbackColor));
                    cursorX += drawRain(
                            op.payload,
                            cursorX,
                            y,
                            baseColor,
                            shadow
                    );
                    maxRight = Math.max(maxRight, cursorX);
                    break;
                }

                case SCROLL_TEXT:
                {
                    int baseColor = (op.rgb >= 0 ? op.rgb : (hexActive ? activeHexRGB : fallbackColor));
                    cursorX += drawScroll(
                            op.payload,
                            cursorX,
                            y,
                            baseColor,
                            shadow
                    );
                    maxRight = Math.max(maxRight, cursorX);
                    break;
                }

                case JITTER_TEXT:
                {
                    int baseColor = (op.rgb >= 0 ? op.rgb : (hexActive ? activeHexRGB : fallbackColor));
                    cursorX += drawJitter(
                            op.payload,
                            cursorX,
                            y,
                            baseColor,
                            shadow,
                            op.amp,
                            op.speed
                    );
                    maxRight = Math.max(maxRight, cursorX);
                    break;
                }

                case WOBBLE_TEXT:
                {
                    int baseColor = (op.rgb >= 0 ? op.rgb : (hexActive ? activeHexRGB : fallbackColor));
                    cursorX += drawWobble(
                            op.payload,
                            cursorX,
                            y,
                            baseColor,
                            shadow,
                            op.amp,
                            op.speed
                    );
                    maxRight = Math.max(maxRight, cursorX);
                    break;
                }


                case OUTLINE_TEXT:
                {
                    int baseColor = (op.rgb >= 0 ? op.rgb : (hexActive ? activeHexRGB : fallbackColor));
                    cursorX += drawOutlineText(
                            op.payload,
                            cursorX,
                            y,
                            baseColor,
                            op.outlineColor,
                            shadow
                    );
                    maxRight = Math.max(maxRight, cursorX);
                    break;
                }

                case SHADOW_TEXT:
                {
                    int baseColor = (op.rgb >= 0 ? op.rgb : (hexActive ? activeHexRGB : fallbackColor));
                    cursorX += drawShadowText(
                            op.payload,
                            cursorX,
                            y,
                            baseColor,
                            op.shadowColor,
                            shadow
                    );
                    maxRight = Math.max(maxRight, cursorX);
                    break;
                }

                case SPARKLE_TEXT:
                {
                    int baseColor = (op.rgb >= 0 ? op.rgb : (hexActive ? activeHexRGB : fallbackColor));
                    cursorX += drawSparkleText(
                            op.payload,
                            cursorX,
                            y,
                            baseColor,
                            shadow,
                            op.sparkleIntensity,
                            op.speed
                    );
                    maxRight = Math.max(maxRight, cursorX);
                    break;
                }

                case FLICKER_TEXT:
                {
                    int baseColor = (op.rgb >= 0 ? op.rgb : (hexActive ? activeHexRGB : fallbackColor));
                    cursorX += drawFlickerText(
                            op.payload,
                            cursorX,
                            y,
                            baseColor,
                            shadow,
                            op.flickerSpeed
                    );
                    maxRight = Math.max(maxRight, cursorX);
                    break;
                }

                case GLITCH_TEXT:
                {
                    int baseColor = (op.rgb >= 0 ? op.rgb : (hexActive ? activeHexRGB : fallbackColor));
                    cursorX += drawGlitchText(
                            op.payload,
                            cursorX,
                            y,
                            baseColor,
                            shadow,
                            op.glitchAmount
                    );
                    maxRight = Math.max(maxRight, cursorX);
                    break;
                }

            }
        }

        return maxRight - x;
    }

    private static float timeSeconds() {
        return Minecraft.getSystemTime() / 1000.0F;
    }

    private int drawGradientMulti(String s, int x, int y, int[] stops, boolean shadow,
                                  boolean scroll, float speed, boolean pulseOn,
                                  float pulseAmp, float pulseSpeed, String legacyFmt) {

        s = cleanPayload(s);
        LegacyState local = new LegacyState();
        applyAndStripLegacyCodes(legacyFmt == null ? "" : legacyFmt, local);

        if (legacyFmt != null && legacyFmt.contains("§k")) {
            local.obfOnce = false;
        }

        s = applyAndStripLegacyCodes(s, local);

        if (s.isEmpty() || stops == null || stops.length < 2) {
            return 0;
        }

        float fx = (float) x;
        int len = s.length();
        int segments = stops.length - 1;

        float phase = (scroll && speed > 0.0F)
                ? (timeSeconds() * speed) % 1.0F
                : 0.0F;

        float vMul = 1.0F;
        if (pulseOn) {
            float omega = Math.max(0.01F, pulseSpeed) * (float) (Math.PI * 2F);
            float p = 0.5F + 0.5F * (float) Math.sin(timeSeconds() * omega);
            vMul = clamp01(1.0F - pulseAmp + pulseAmp * p);
        }

        for (int i = 0; i < len; i++) {
            float t = (len <= 1) ? 0.0F : (float) i / (float) (len - 1);
            t = (t + phase) % 1.0F;

            float segPos = t * (float) segments;
            int si = Math.min((int) Math.floor(segPos), segments - 1);
            float lt = segPos - si;

            int rgb = lerpRGB(stops[si], stops[si + 1], lt);

            int rI = (rgb >> 16) & 255;
            int gI = (rgb >> 8) & 255;
            int bI = rgb & 255;

            if (pulseOn) {
                float mul = clamp01(vMul);
                rI = Math.round(rI * mul);
                gI = Math.round(gI * mul);
                bI = Math.round(bI * mul);
                rI = Math.min(255, Math.max(0, rI));
                gI = Math.min(255, Math.max(0, gI));
                bI = Math.min(255, Math.max(0, bI));
            }

            rgb = (rI << 16) | (gI << 8) | bI;

            char ch = s.charAt(i);
            if (local.obfuscated) {
                ch = this.obfuscateCharSameWidth(ch);
            }

            String prefix = legacyPrefix(local);

            this.base.drawString(prefix + ch, (int) fx, y, rgb, shadow);
            fx += this.base.getCharWidth(ch);

            if (local.obfOnce) {
                local.obfuscated = false;
                local.obfOnce = false;
            }
        }

        return Math.round(fx) - x;
    }

    private static int hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float x = c * (1.0F - Math.abs(h / 60.0F % 2.0F - 1.0F));
        float m = v - c;
        float r = 0.0F;
        float g = 0.0F;
        float b = 0.0F;
        if (h < 60.0F) {
            r = c;
            g = x;
            b = 0.0F;
        } else if (h < 120.0F) {
            r = x;
            g = c;
            b = 0.0F;
        } else if (h < 180.0F) {
            r = 0.0F;
            g = c;
            b = x;
        } else if (h < 240.0F) {
            r = 0.0F;
            g = x;
            b = c;
        } else if (h < 300.0F) {
            r = x;
            g = 0.0F;
            b = c;
        } else {
            r = c;
            g = 0.0F;
            b = x;
        }

        int R = Math.round((r + m) * 255.0F);
        int G = Math.round((g + m) * 255.0F);
        int B = Math.round((b + m) * 255.0F);
        return R << 16 | G << 8 | B;
    }

    private int drawRainbowAndReturnAdvanceCustom(String s, int x, int y, boolean shadow, int cycles, float sat, float val, float phase, float speed, boolean pulseOn, float pulseAmp, float pulseSpeed, String legacyFmt) {
        s = cleanPayload(s);
        LegacyState local = new LegacyState();
        applyAndStripLegacyCodes(legacyFmt == null ? "" : legacyFmt, local);
        if (legacyFmt != null && legacyFmt.contains("§k")) {
            local.obfOnce = false;
            local.obfuscated = true;
        }

        s = applyAndStripLegacyCodes(s, local);
        if (s.isEmpty()) {
            return 0;
        } else {
            float fx = (float)x;
            int n = s.length();
            int cy = Math.max(1, cycles);
            float tphase = speed > 0.0F ? timeSeconds() * speed % 1.0F : 0.0F;
            float vNow = val;
            if (pulseOn) {
                float omega = Math.max(0.01F, pulseSpeed) * ((float)Math.PI * 2F);
                float p = (float)((double)0.5F + (double)0.5F * Math.sin((double)(timeSeconds() * omega)));
                vNow = clamp01((1.0F - pulseAmp) * val + pulseAmp * val * p);
            }

            for(int i = 0; i < n; ++i) {
                float t = n <= 1 ? 0.0F : (float)i / (float)(n - 1);
                float h01 = (phase + tphase + t * (float)cy) % 1.0F;
                int rgb = hsvToRgb(h01 * 360.0F, sat, vNow);
                char ch = s.charAt(i);
                if (local.obfuscated) {
                    ch = this.obfuscateCharSameWidth(ch);
                }

                String prefix = legacyPrefix(local);
                this.base.drawString(prefix + ch, (int)fx, y, rgb, shadow);
                fx += (float)this.base.getCharWidth(ch);
                if (local.obfOnce) {
                    local.obfuscated = false;
                    local.obfOnce = false;
                }
            }

            return Math.round(fx) - x;
        }
    }

    private int drawPulse(String s, int x, int y, boolean shadow, int baseRgb, float speed, float amp, String legacyFmt) {
        s = cleanPayload(s);
        LegacyState local = new LegacyState();
        applyAndStripLegacyCodes(legacyFmt == null ? "" : legacyFmt, local);
        if (legacyFmt != null && legacyFmt.contains("§k")) {
            local.obfOnce = false;
            local.obfuscated = true;
        }

        s = applyAndStripLegacyCodes(s, local);
        if (s.isEmpty()) {
            return 0;
        } else {
            float fx = (float)x;
            float r = (float)(baseRgb >> 16 & 255) / 255.0F;
            float g = (float)(baseRgb >> 8 & 255) / 255.0F;
            float b = (float)(baseRgb & 255) / 255.0F;
            float max = Math.max(r, Math.max(g, b));
            float min = Math.min(r, Math.min(g, b));
            float d = max - min;
            float sSat = max == 0.0F ? 0.0F : d / max;
            float h;
            if (d == 0.0F) {
                h = 0.0F;
            } else if (max == r) {
                h = 60.0F * ((g - b) / d % 6.0F);
            } else if (max == g) {
                h = 60.0F * ((b - r) / d + 2.0F);
            } else {
                h = 60.0F * ((r - g) / d + 4.0F);
            }

            if (h < 0.0F) {
                h += 360.0F;
            }

            float t = timeSeconds();
            float omega = Math.max(0.01F, speed) * ((float)Math.PI * 2F);
            float pulse = (float)((double)0.5F + (double)0.5F * Math.sin((double)(t * omega)));
            float v = clamp01(max * (1.0F - amp) + max * amp * pulse);
            int rgb = hsvToRgb(h, sSat, v);

            for(int i = 0; i < s.length(); ++i) {
                char ch = s.charAt(i);
                if (local.obfuscated) {
                    ch = this.obfuscateCharSameWidth(ch);
                }

                String prefix = legacyPrefix(local);
                this.base.drawString(prefix + ch, (int)fx, y, rgb, shadow);
                fx += (float)this.base.getCharWidth(ch);
                if (local.obfOnce) {
                    local.obfuscated = false;
                    local.obfOnce = false;
                }
            }

            return Math.round(fx) - x;
        }
    }

    private int drawWave(String s, int x, int y, int color, boolean shadow) {
        float t = timeSeconds();
        float amp = 2.0f;      // amplitude
        float speed = 6.0F;    // wavespeed

        float fx = x;
        for (int i = 0; i < s.length(); i++) {
            float dy = (float) Math.sin((t * speed) + (i * 0.4F)) * amp;
            char c = s.charAt(i);
            this.base.drawString("" + c, (int) fx, (int) (y + dy), color, shadow);
            fx += this.base.getCharWidth(c);
        }
        return (int) (fx - x);
    }

    private int drawShake(String s, int x, int y, int color, boolean shadow) {
        float fx = x;
        Random r = this.obfRng;

        for (int i = 0; i < s.length(); i++) {
            float dx = (r.nextFloat() - 0.5F) * 2F;
            float dy = (r.nextFloat() - 0.5F) * 2F;
            char c = s.charAt(i);
            this.base.drawString("" + c, (int) (fx + dx), (int) (y + dy), color, shadow);
            fx += this.base.getCharWidth(c);
        }
        return (int) (fx - x);
    }

    private int drawZoom(String s, int x, int y, int color, boolean shadow) {
        float t = timeSeconds();
        float scale = 1.0F + 0.2F * (float) Math.sin(t * 5.0F);

        float fx = x;
        for (int i = 0; i < s.length(); i++) {
            GL11.glPushMatrix();
            GL11.glTranslatef(fx, y, 0);
            GL11.glScalef(scale, scale, 1.0F);
            char c = s.charAt(i);
            this.base.drawString("" + c, 0, 0, color, shadow);
            GL11.glPopMatrix();

            fx += this.base.getCharWidth(c) * scale;
        }
        return (int) (fx - x);
    }

    private int drawRain(String s, int x, int y, int color, boolean shadow) {
        float t = timeSeconds();
        float fx = x;

        for (int i = 0; i < s.length(); i++) {
            float dy = (float) (Math.sin((t * 8) + i) * 4.0);
            char c = s.charAt(i);
            this.base.drawString("" + c, (int) fx, (int) (y + dy), color, shadow);
            fx += this.base.getCharWidth(c);
        }
        return (int) (fx - x);
    }

    private int drawScroll(String s, int x, int y, int color, boolean shadow) {
        float t = timeSeconds();
        float fx = x;

        for (int i = 0; i < s.length(); i++) {
            float dx = (float) Math.sin(t * 5.0F + i * 0.4F) * 2.0F;
            char c = s.charAt(i);
            this.base.drawString("" + c, (int) (fx + dx), y, color, shadow);
            fx += this.base.getCharWidth(c);
        }
        return (int) (fx - x);
    }

    private int drawJitter(String s, int x, int y, int color, boolean shadow, float amp, float speed) {
        s = cleanPayload(s);
        if (s == null || s.isEmpty()) return 0;

        float fx = x;
        float jitterAmp = amp > 0.0F ? amp : 1.0F;
        Random r = this.obfRng;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            float dx = (r.nextFloat() - 0.5F) * 2.0F * jitterAmp;
            float dy = (r.nextFloat() - 0.5F) * 2.0F * jitterAmp;
            this.base.drawString(String.valueOf(c), (int) (fx + dx), (int) (y + dy), color, shadow);
            fx += this.base.getCharWidth(c);
        }
        return Math.round(fx) - x;
    }

    private int drawWobble(String s, int x, int y, int color, boolean shadow, float amp, float speed) {
        s = cleanPayload(s);
        if (s == null || s.isEmpty()) return 0;

        float fx = x;
        float a = amp > 0.0F ? amp : 3.0F;
        float sp = speed > 0.0F ? speed : 2.0F;
        float t = timeSeconds();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            float dy = (float) Math.sin((t * sp) + i * 0.5F) * a;
            this.base.drawString(String.valueOf(c), (int) fx, (int) (y + dy), color, shadow);
            fx += this.base.getCharWidth(c);
        }
        return Math.round(fx) - x;
    }

    private int drawOutlineText(String s, int x, int y, int textColor, int outlineColor, boolean shadow) {
        if (s == null || s.isEmpty()) return 0;

        // Draw 4 directions of outline
        LegacyState dummy1 = new LegacyState();
        LegacyState dummy2 = new LegacyState();
        LegacyState dummy3 = new LegacyState();
        LegacyState dummy4 = new LegacyState();
        LegacyState main    = new LegacyState();

        int adv = this.drawTextWithLegacyInline(s, x + 1, y, outlineColor, shadow, dummy1);
        this.drawTextWithLegacyInline(s, x - 1, y, outlineColor, shadow, dummy2);
        this.drawTextWithLegacyInline(s, x, y + 1, outlineColor, shadow, dummy3);
        this.drawTextWithLegacyInline(s, x, y - 1, outlineColor, shadow, dummy4);

        // Center text
        this.drawTextWithLegacyInline(s, x, y, textColor, shadow, main);

        return adv;
    }

    private int drawShadowText(String s, int x, int y, int textColor, int shadowColor, boolean shadow) {
        if (s == null || s.isEmpty()) return 0;

        LegacyState sh = new LegacyState();
        LegacyState main = new LegacyState();

        // Drop shadow offset
        this.drawTextWithLegacyInline(s, x + 1, y + 1, shadowColor, false, sh);
        int adv = this.drawTextWithLegacyInline(s, x, y, textColor, shadow, main);

        return adv;
    }

    private int drawSparkleText(String s, int x, int y, int color, boolean shadow, float intensity, float speed) {
        s = cleanPayload(s);
        if (s == null || s.isEmpty()) return 0;

        float fx = x;
        float inten = intensity <= 0.0F ? 1.0F : intensity;
        float sp = speed <= 0.0F ? 4.0F : speed;
        float t = timeSeconds() * sp;
        Random r = this.obfRng;

        float baseProb = clamp01(inten * 0.4F);

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            // randomly brighten some chars
            int drawColor = color;
            if (r.nextFloat() < baseProb) {
                drawColor = 0xFFFFFF;
            }

            this.base.drawString(String.valueOf(c), (int) fx, y, drawColor, shadow);
            fx += this.base.getCharWidth(c);
        }
        return Math.round(fx) - x;
    }

    private int drawFlickerText(String s, int x, int y, int color, boolean shadow, float speed) {
        s = cleanPayload(s);
        if (s == null || s.isEmpty()) return 0;

        float fx = x;
        float sp = speed <= 0.0F ? 6.0F : speed;
        float t = timeSeconds() * sp;
        float phase = (float) Math.sin(t);
        float mul = 0.4F + 0.6F * Math.abs(phase); // 0.4 .. 1.0

        int r = (int) (((color >> 16) & 255) * mul);
        int g = (int) (((color >> 8) & 255) * mul);
        int b = (int) ((color & 255) * mul);
        int flickerColor = (r << 16) | (g << 8) | b;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            this.base.drawString(String.valueOf(c), (int) fx, y, flickerColor, shadow);
            fx += this.base.getCharWidth(c);
        }
        return Math.round(fx) - x;
    }

    private int drawGlitchText(String s, int x, int y, int color, boolean shadow, float amount) {
        s = cleanPayload(s);
        if (s == null || s.isEmpty()) return 0;

        float fx = x;
        float amt = amount <= 0.0F ? 1.0F : amount;
        Random r = this.obfRng;

        float prob = clamp01(amt * 0.35F);

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            // random char swap
            if (r.nextFloat() < prob) {
                c = this.obfuscateCharSameWidth(c);
            }

            // random small offset
            float dx = (r.nextFloat() - 0.5F) * 2.0F * amt;
            float dy = (r.nextFloat() - 0.5F) * 2.0F * amt;

            this.base.drawString(String.valueOf(c), (int) (fx + dx), (int) (y + dy), color, shadow);
            fx += this.base.getCharWidth(c);
        }
        return Math.round(fx) - x;
    }

    private int drawTextWithLegacyInline(String s, int x, int y, int color, boolean shadow, LegacyState legacy) {
        int startX = x;
        if (s != null && !s.isEmpty()) {
            int i = 0;
            int n = s.length();

            while (i < n) {
                char c = s.charAt(i);

                // Handle § formatting
                if (c == '§' && i + 1 < n) {
                    char k = s.charAt(i + 1);
                    char kl = Character.toLowerCase(k);

                    int col = vanillaColorFor(k);
                    if (col >= 0) {
                        legacy.reset();
                        legacy.colorRgb = col;
                        i += 2;
                        continue;
                    }

                    switch (kl) {
                        case 'k':
                            legacy.obfuscated = true;
                            legacy.obfOnce = true;
                            i += 2;
                            i = skipLegacyCodes(s, i);
                            continue;

                        case 'l':
                            legacy.bold = true;
                            i += 2;
                            continue;

                        case 'm':
                            legacy.strikethrough = true;
                            i += 2;
                            continue;

                        case 'n':
                            legacy.underline = true;
                            i += 2;
                            continue;

                        case 'o':
                            legacy.italic = true;
                            i += 2;
                            continue;

                        case 'r':
                            legacy.reset();
                            i += 2;
                            continue;

                        default:
                            break;
                    }
                }

                String prefix = legacyPrefix(legacy);
                int drawRgb = (legacy.colorRgb != null ? legacy.colorRgb : color);

                char out = legacy.obfuscated
                        ? this.obfuscateCharSameWidth(c)
                        : c;

                // DRAW GLYPH (MCP)
                this.base.drawString(prefix + out, x, y, drawRgb, shadow);

                // ADVANCE WIDTH (MCP)
                x += this.base.getCharWidth(out);

                if (legacy.obfOnce) {
                    legacy.obfuscated = false;
                    legacy.obfOnce = false;
                }

                i++;
            }

            return x - startX;
        }

        return 0;
    }

    private static String stripHexAndReset(String s) {
        if (s != null && !s.isEmpty()) {
            s = s.replaceAll("(?i)§#[0-9a-f]{6}[lmonkr]*", "");
            s = s.replaceAll("(?i)§#[0-9a-f]{3}[lmonkr]*", "");
            return s;
        } else {
            return s;
        }
    }

    private static String postNormalizeCleanup(String s) {
        if (s != null && !s.isEmpty()) {
            s = s.replaceAll("(?is)<\\s*#?[0-9a-f]{3,6}\\s*>\\s*</\\s*#\\s*>", "");
            s = s.replaceAll("(?is)<\\s*(grad|rainbow|pulse)\\b[^>]*>\\s*</\\s*\\1\\s*>", "");
            return s;
        } else {
            return s;
        }
    }

    private static int indexOfIgnoreSpace(String s, int from, String needle) {
        String collapsed = s.substring(from).replaceAll("\\s+", " ");
        String n = needle.replaceAll("\\s+", " ");
        int pos = collapsed.toLowerCase().indexOf(n.toLowerCase());
        if (pos < 0) {
            return -1;
        } else {
            int i = from;

            for(int seen = 0; i < s.length() && seen < pos; ++i) {
                char ch = s.charAt(i);
                if (!Character.isWhitespace(ch)) {
                    ++seen;
                } else {
                    while(i < s.length() && Character.isWhitespace(s.charAt(i))) {
                        ++i;
                    }

                    ++seen;
                    --i;
                }
            }

            return i;
        }
    }

    private static int skipPastCloser(String s, int start) {
        int i;
        for(i = start; i < s.length() && s.charAt(i) != '>'; ++i) {
        }

        if (i < s.length()) {
            ++i;
        }

        return i;
    }

    private static String normalizeControlsToTags(String text) {
        if (text != null && !text.isEmpty()) {
            StringBuilder out = new StringBuilder(text.length() + 16);
            boolean open = false;
            int i = 0;
            int n = text.length();

            while(i < n) {
                char c = text.charAt(i);
                if (c == 167 && i + 1 < n) {
                    char n1 = text.charAt(i + 1);
                    if (n1 == '#' && i + 3 < n) {
                        if (i + 7 < n) {
                            String hex6 = text.substring(i + 2, i + 8);
                            if (hex6.matches("(?i)[0-9a-f]{6}")) {
                                if (open) {
                                    out.append("</#>");
                                }

                                out.append("<#").append(hex6).append(">");
                                open = true;
                                i += 8;
                                continue;
                            }
                        }

                        String hex3 = text.substring(i + 2, Math.min(i + 5, n));
                        if (hex3.matches("(?i)[0-9a-f]{3}")) {
                            String h = hex3.toUpperCase();
                            String expanded = "" + h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2);
                            if (open) {
                                out.append("</#>");
                            }

                            out.append("<#").append(expanded).append(">");
                            open = true;
                            i += 5;
                            continue;
                        }
                    }

                    if (n1 == 'r') {
                        if (open) {
                            out.append("</#>");
                            open = false;
                        }

                        out.append("§r");
                        i += 2;
                        continue;
                    }

                    if ("0123456789abcdeflmonkr".indexOf(Character.toLowerCase(n1)) >= 0) {
                        out.append('§').append(n1);
                        i += 2;
                        continue;
                    }
                }

                out.append(c);
                ++i;
            }

            if (open) {
                out.append("</#>");
            }

            return out.toString();
        } else {
            return text;
        }
    }

    public static String formatInline(String s) {
        if (s == null) {
            return "";
        } else {
            s = s.replace('&', '§');
            String out = s.replaceAll("(?i)§#([0-9a-f]{6})([lmonkr]*)", "<#$1>$2</#>");
            out = out.replaceAll("(?i)§#([0-9a-f]{3})([lmonkr]*)", "<#$1>$2</#>");
            return out;
        }
    }

    private static int lerpRGB(int a, int b, float t) {
        int ar = a >> 16 & 255;
        int ag = a >> 8 & 255;
        int ab = a & 255;
        int br = b >> 16 & 255;
        int bg = b >> 8 & 255;
        int bb = b & 255;
        int r = (int)((float)ar + (float)(br - ar) * t);
        int g = (int)((float)ag + (float)(bg - ag) * t);
        int bl = (int)((float)ab + (float)(bb - ab) * t);
        return r << 16 | g << 8 | bl;
    }
    // pulls first #RRGGBB / #RGB from tag attrs into op.rgb
    private static void parseMotionColor(String attrs, Op out) {
        if (attrs == null) return;
        List<String> hexes = parseHexTokens(attrs);
        if (!hexes.isEmpty()) {
            out.rgb = parseHexRGB(hexes.get(0));
        }
    }

    private static int parseHexRGB(String hex) {
        hex = hex.trim();
        if (hex.length() == 3) {
            char r = hex.charAt(0);
            char g = hex.charAt(1);
            char b = hex.charAt(2);
            hex = "" + r + r + g + g + b + b;
        }

        return Integer.parseInt(hex, 16) & 16777215;
    }

    private static List<String> parseHexTokens(String attrs) {
        List<String> list = new ArrayList();
        if (attrs == null) {
            return list;
        } else {
            Matcher m = Pattern.compile("(?i)(?:^|\\s)#([0-9a-f]{3}|[0-9a-f]{6})(?=$|\\s|[,;])").matcher(attrs);

            while(m.find()) {
                list.add(m.group(1));
            }

            return list;
        }
    }

    private static float parseFloatSafe(String s, float d) {
        try {
            return Float.parseFloat(s);
        } catch (Throwable var3) {
            return d;
        }
    }

    private static void parsePulseArgs(String attrs, Op out) {
        List<String> hexes = parseHexTokens(attrs);
        if (!hexes.isEmpty()) {
            out.solid = parseHexRGB((String)hexes.get(0));
        }

        Matcher kv = Pattern.compile("(?i)\\b(speed|amp)\\s*=\\s*([\\d.]+)").matcher(attrs == null ? "" : attrs);

        while(kv.find()) {
            String k = kv.group(1).toLowerCase();
            String v = kv.group(2);
            if ("speed".equals(k)) {
                out.speed = Math.max(0.0F, parseFloatSafe(v, 0.0F));
            } else if ("amp".equals(k)) {
                out.amp = clamp01(parseFloatSafe(v, 0.25F));
            }
        }

    }
    // Generic motion parser for tags like <wave>, <shake>, <jitter>, <wobble>
    // understands: amp=, amplitude=, intensity=, speed=
// Generic motion parser for tags like <wave>, <shake>, <jitter>, <wobble>, <zoom>, <rain>, <scroll>
// understands: #hex, amp= / amplitude= / intensity=, speed=
    private static void parseMotionArgs(String attrs, Op out, float defaultAmp, float defaultSpeed) {
        out.amp = defaultAmp;
        out.speed = defaultSpeed;
        out.rgb = -1; // no explicit color by default

        if (attrs == null || attrs.trim().isEmpty()) {
            return;
        }

        // NEW: first hex in the args becomes the text color for this motion op
        parseMotionColor(attrs, out);

        Matcher kv = Pattern.compile("(?i)\\b(amp|amplitude|intensity|speed)\\s*=\\s*([\\d.]+)").matcher(attrs);
        while (kv.find()) {
            String k = kv.group(1).toLowerCase();
            String v = kv.group(2);
            float f = parseFloatSafe(v, 0.0F);

            if ("speed".equals(k)) {
                out.speed = f;
            } else {
                // amp, amplitude, intensity → amplitude
                out.amp = f;
            }
        }
    }

    private static void parseOutlineArgs(String attrs, Op out) {
        if (attrs == null) return;
        // first hex becomes outline color
        List<String> hexes = parseHexTokens(attrs);
        if (!hexes.isEmpty()) {
            out.outlineColor = parseHexRGB(hexes.get(0));
        }
    }

    private static void parseShadowArgs(String attrs, Op out) {
        if (attrs == null) return;
        // first hex becomes shadow color
        List<String> hexes = parseHexTokens(attrs);
        if (!hexes.isEmpty()) {
            out.shadowColor = parseHexRGB(hexes.get(0));
        }
    }

    private static void parseSparkleArgs(String attrs, Op out) {
        // default sparkle
        out.sparkleIntensity = 1.0F;
        out.speed = 4.0F;
        out.rgb = -1;

        if (attrs == null || attrs.trim().isEmpty()) {
            return;
        }

        // NEW: allow #hex on <sparkle>
        parseMotionColor(attrs, out);

        Matcher kv = Pattern.compile("(?i)\\b(density|intensity|speed)\\s*=\\s*([\\d.]+)").matcher(attrs);
        while (kv.find()) {
            String k = kv.group(1).toLowerCase();
            String v = kv.group(2);
            float f = parseFloatSafe(v, 0.0F);

            if ("speed".equals(k)) {
                out.speed = f;
            } else {
                // density/intensity → intensity
                out.sparkleIntensity = f;
            }
        }
    }

    private static void parseFlickerArgs(String attrs, Op out) {
        // default flicker
        out.flickerSpeed = 6.0F;
        out.rgb = -1;

        if (attrs == null || attrs.trim().isEmpty()) {
            return;
        }

        // NEW: allow #hex on <flicker>
        parseMotionColor(attrs, out);

        Matcher kv = Pattern.compile("(?i)\\b(speed|rate)\\s*=\\s*([\\d.]+)").matcher(attrs);
        while (kv.find()) {
            String v = kv.group(2);
            out.flickerSpeed = Math.max(0.01F, parseFloatSafe(v, 6.0F));
        }
    }

    private static void parseGlitchArgs(String attrs, Op out) {
        // default glitch strength
        out.glitchAmount = 1.0F;
        out.rgb = -1;

        if (attrs == null || attrs.trim().isEmpty()) {
            return;
        }

        // NEW: allow #hex on <glitch>
        parseMotionColor(attrs, out);

        Matcher kv = Pattern.compile("(?i)\\b(intensity|amount|amp)\\s*=\\s*([\\d.]+)").matcher(attrs);
        while (kv.find()) {
            String v = kv.group(2);
            out.glitchAmount = Math.max(0.0F, parseFloatSafe(v, 1.0F));
        }
    }

    private static String stylesFromAttrs(String attrs) {
        if (attrs == null) {
            return "";
        } else {
            Matcher m = Pattern.compile("(?i)\\bstyles\\s*=\\s*([lmonkr]+)\\b").matcher(attrs);
            if (m.find()) {
                return stylesToLegacy(m.group(1));
            } else {
                m = Pattern.compile("(?i)\\b([lmonkr]+)\\b").matcher(attrs);

                while(m.find()) {
                    String token = m.group(1);
                    if (token != null && !token.isEmpty()) {
                        return stylesToLegacy(token);
                    }
                }

                return "";
            }
        }
    }

    private static void parseGradAnimArgs(String attrs, Op out) {
        if (attrs != null) {
            Matcher kv = Pattern.compile("(?i)\\b(scroll)\\s*=\\s*([\\d.]+)").matcher(attrs);
            if (kv.find()) {
                out.scroll = true;
                out.speed = Math.max(0.0F, parseFloatSafe(kv.group(2), 0.0F));
            }

            parsePulseParamsInline(attrs, out);
        }
    }

    private static void parseRainbowSpeed(String attrs, Op out) {
        if (attrs != null) {
            // allow both speed= and scroll=
            Matcher kv = Pattern.compile("(?i)\\b(speed|scroll)\\s*=\\s*([\\d.]+)").matcher(attrs);
            while (kv.find()) {
                String v = kv.group(2);
                out.speed = Math.max(0.0F, parseFloatSafe(v, 0.0F));
            }
        }
    }


    private static void parseRainbowArgs(String attrs, Op out) {
        if (attrs != null && !attrs.trim().isEmpty()) {
            Matcher lone = Pattern.compile("\\b(\\d+)\\b").matcher(attrs);
            if (lone.find()) {
                try {
                    out.cycles = Math.max(1, Integer.parseInt(lone.group(1)));
                } catch (Throwable var8) {
                }
            }

            Matcher kv = Pattern.compile("(?i)\\b(cycles|sat|val|phase)\\s*=\\s*([\\d.]+)").matcher(attrs);

            while(kv.find()) {
                String k = kv.group(1).toLowerCase();
                String v = kv.group(2);

                try {
                    if ("cycles".equals(k)) {
                        out.cycles = Math.max(1, (int)Float.parseFloat(v));
                    } else if ("sat".equals(k)) {
                        out.sat = clamp01(Float.parseFloat(v));
                    } else if ("val".equals(k)) {
                        out.val = clamp01(Float.parseFloat(v));
                    } else if ("phase".equals(k)) {
                        float p = Float.parseFloat(v);
                        out.phase = p - (float)Math.floor((double)p);
                    }
                } catch (Throwable var7) {
                }
            }

            parsePulseParamsInline(attrs, out);
        } else {
            parsePulseParamsInline(attrs, out);
        }
    }

    private static float clamp01(float f) {
        return f < 0.0F ? 0.0F : (f > 1.0F ? 1.0F : f);
    }

    private static void parsePulseParamsInline(String attrs, Op out) {
        if (attrs != null) {
            Matcher kv = Pattern.compile("(?i)\\b(pulse|pulsespeed|amp)\\s*=\\s*([\\w.]+)").matcher(attrs);

            while(kv.find()) {
                String k = kv.group(1).toLowerCase();
                String v = kv.group(2);
                if ("pulse".equals(k)) {
                    out.pulseOn = !"0".equals(v) && !"false".equalsIgnoreCase(v);
                } else if ("pulsespeed".equals(k)) {
                    out.pulseSpeed = Math.max(0.01F, parseFloatSafe(v, 1.0F));
                } else if ("amp".equals(k)) {
                    out.pulseAmp = clamp01(parseFloatSafe(v, 0.25F));
                }
            }

        }
    }
// ─────────────────────────────────────────────
// Shorthand tag expander
// ─────────────────────────────────────────────

    private static String expandShortTags(String input) {
        if (input == null) return null;
        // Quick check – avoid work if no tags at all
        if (input.indexOf('<') < 0 && input.indexOf('«') < 0) return input;

        String s = input;
        StringBuffer sb;
        Matcher m;

        // <g:#FF0000:#00FF00[:extra]>
        m = TAG_GRAD_SHORT.matcher(s);
        sb = new StringBuffer();
        while (m.find()) {
            String body = m.group(1); // everything after "g:"
            String rep  = buildGradFromShort(body);
            m.appendReplacement(sb, Matcher.quoteReplacement(rep));
        }
        m.appendTail(sb);
        s = sb.toString();

        // <pl:#FF00FF[:extra]>
        m = TAG_PULSE_SHORT.matcher(s);
        sb = new StringBuffer();
        while (m.find()) {
            String body = m.group(1);
            String rep  = buildPulseFromShort(body);
            m.appendReplacement(sb, Matcher.quoteReplacement(rep));
        }
        m.appendTail(sb);
        s = sb.toString();

        // <wave:a:5:2>
        m = TAG_WAVE_SHORT.matcher(s);
        sb = new StringBuffer();
        while (m.find()) {
            String type  = m.group(1);
            String speed = m.group(2);
            String amp   = m.group(3);
            String rep   = buildWaveFromShort(type, speed, amp);
            m.appendReplacement(sb, Matcher.quoteReplacement(rep));
        }
        m.appendTail(sb);
        s = sb.toString();

        // <zoom:a:1.5:20>
        m = TAG_ZOOM_SHORT.matcher(s);
        sb = new StringBuffer();
        while (m.find()) {
            String type  = m.group(1);
            String scale = m.group(2);
            String cycle = m.group(3);
            String rep   = buildZoomFromShort(type, scale, cycle);
            m.appendReplacement(sb, Matcher.quoteReplacement(rep));
        }
        m.appendTail(sb);
        s = sb.toString();

        return s;
    }

    // <g:#FF0000:#00FF00:...>  →  <grad #FF0000 #00FF00 ...>
    private static String buildGradFromShort(String body) {
        if (body == null) body = "";
        body = body.trim();
        if (body.isEmpty()) return "<grad>";

        String[] parts = body.split(":");
        StringBuilder colors = new StringBuilder();
        StringBuilder extras = new StringBuilder();

        for (String rawPart : parts) {
            if (rawPart == null) continue;
            String p = rawPart.trim();
            if (p.isEmpty()) continue;

            if (p.matches("(?i)#?[0-9a-f]{3,6}")) {
                if (p.charAt(0) != '#') p = "#" + p;
                colors.append(' ').append(p);
            } else {
                if (extras.length() == 0) extras.append(' ');
                else extras.append(' ');
                extras.append(p);
            }
        }

        if (colors.length() == 0) {
            // No valid color tokens, keep whatever extra attrs we had
            return "<grad" + extras.toString() + ">";
        }
        return "<grad" + colors.toString() + extras.toString() + ">";
    }

    // <pl:#FF00FF:...>  →  <pulse #FF00FF ...>
    private static String buildPulseFromShort(String body) {
        if (body == null) body = "";
        body = body.trim();
        if (body.isEmpty()) return "<pulse>";

        String[] parts = body.split(":");
        String color = null;
        StringBuilder extras = new StringBuilder();

        for (String rawPart : parts) {
            if (rawPart == null) continue;
            String p = rawPart.trim();
            if (p.isEmpty()) continue;

            if (color == null && p.matches("(?i)#?[0-9a-f]{3,6}")) {
                if (p.charAt(0) != '#') p = "#" + p;
                color = p;
            } else {
                if (extras.length() == 0) extras.append(' ');
                else extras.append(' ');
                extras.append(p);
            }
        }

        if (color == null) {
            return "<pulse" + extras.toString() + ">";
        }
        return "<pulse " + color + extras.toString() + ">";
    }

    // <wave:a:5:2>  →  <wave type=a amp=2 speed=5>
    private static String buildWaveFromShort(String type, String speed, String amp) {
        String t = (type  == null || type.isEmpty())  ? "a"   : type;
        String s = (speed == null || speed.isEmpty()) ? "5"   : speed;
        String a = (amp   == null || amp.isEmpty())   ? "2"   : amp;
        return "<wave type=" + t + " amp=" + a + " speed=" + s + ">";
    }

    // <zoom:a:1.5:20>  →  <zoom type=a amp=1.5 speed=20 scale=1.5 cycle=20>
    private static String buildZoomFromShort(String type, String scale, String cycle) {
        String t  = (type  == null || type.isEmpty())  ? "a"   : type;
        String sc = (scale == null || scale.isEmpty()) ? "1.0" : scale;
        String cy = (cycle == null || cycle.isEmpty()) ? "20"  : cycle;

        // We set both amp/speed *and* scale/cycle so whatever parseMotionArgs looks for will be satisfied.
        return "<zoom type=" + t
                + " amp=" + sc
                + " speed=" + cy
                + " scale=" + sc
                + " cycle=" + cy
                + ">";
    }

    private List<Op> parseToOps(String raw) {
        List<Op> ops = new ArrayList();
        if (raw == null || raw.isEmpty()) return ops;

        // Expand shorthand tags (<g:...>, <pl:...>, <wave:a:...>, <zoom:a:...>)
        String s = expandShortTags(raw);
        while (true) {
            // Core tags
            Matcher mgA = TAG_GRAD_OPEN.matcher(s);
            Matcher mgC = TAG_GRAD_OPEN_CHEV.matcher(s);
            Matcher mrA = TAG_RBW_OPEN.matcher(s);
            Matcher mrC = TAG_RBW_OPEN_CHEV.matcher(s);
            Matcher mpA = TAG_PULSE_OPEN.matcher(s);
            Matcher mpC = TAG_PULSE_OPEN_CHEV.matcher(s);

            // WAVE
            Matcher mwA   = Pattern.compile("(?i)<\\s*wave([^>]*)>").matcher(s);
            Matcher mwC   = Pattern.compile("(?i)[«]\\s*wave([^»]*)[»]").matcher(s);

            // SHAKE
            Matcher mshA  = Pattern.compile("(?i)<\\s*shake([^>]*)>").matcher(s);
            Matcher mshC  = Pattern.compile("(?i)[«]\\s*shake([^»]*)[»]").matcher(s);

            // ZOOM
            Matcher mzA   = Pattern.compile("(?i)<\\s*zoom([^>]*)>").matcher(s);
            Matcher mzC   = Pattern.compile("(?i)[«]\\s*zoom([^»]*)[»]").matcher(s);

            // RAIN
            Matcher mrnA  = Pattern.compile("(?i)<\\s*rain([^>]*)>").matcher(s);
            Matcher mrnC  = Pattern.compile("(?i)[«]\\s*rain([^»]*)[»]").matcher(s);

            // SCROLL
            Matcher mscA  = Pattern.compile("(?i)<\\s*scroll([^>]*)>").matcher(s);
            Matcher mscC  = Pattern.compile("(?i)[«]\\s*scroll([^»]*)[»]").matcher(s);

            // JITTER
            Matcher mjitA = Pattern.compile("(?i)<\\s*jitter([^>]*)>").matcher(s);
            Matcher mjitC = Pattern.compile("(?i)[«]\\s*jitter([^»]*)[»]").matcher(s);

            // WOBBLE
            Matcher mwbA  = Pattern.compile("(?i)<\\s*wobble([^>]*)>").matcher(s);
            Matcher mwbC  = Pattern.compile("(?i)[«]\\s*wobble([^»]*)[»]").matcher(s);

            // OUTLINE
            Matcher moutA = Pattern.compile("(?i)<\\s*outline([^>]*)>").matcher(s);
            Matcher moutC = Pattern.compile("(?i)[«]\\s*outline([^»]*)[»]").matcher(s);

            // SHADOW
            Matcher mshdA = Pattern.compile("(?i)<\\s*shadow([^>]*)>").matcher(s);
            Matcher mshdC = Pattern.compile("(?i)[«]\\s*shadow([^»]*)[»]").matcher(s);

            // SPARKLE
            Matcher mspA  = Pattern.compile("(?i)<\\s*sparkle([^>]*)>").matcher(s);
            Matcher mspC  = Pattern.compile("(?i)[«]\\s*sparkle([^»]*)[»]").matcher(s);

            // FLICKER
            Matcher mfA   = Pattern.compile("(?i)<\\s*flicker([^>]*)>").matcher(s);
            Matcher mfC   = Pattern.compile("(?i)[«]\\s*flicker([^»]*)[»]").matcher(s);

            // GLITCH
            Matcher mglA  = Pattern.compile("(?i)<\\s*glitch([^>]*)>").matcher(s);
            Matcher mglC  = Pattern.compile("(?i)[«]\\s*glitch([^»]*)[»]").matcher(s);

            boolean gA  = mgA.find();
            boolean gC  = mgC.find();
            boolean rA  = mrA.find();
            boolean rC  = mrC.find();
            boolean pA  = mpA.find();
            boolean pC  = mpC.find();

            boolean wA  = mwA.find();
            boolean wC  = mwC.find();
            boolean shA = mshA.find();
            boolean shC = mshC.find();
            boolean zA  = mzA.find();
            boolean zC  = mzC.find();
            boolean rnA = mrnA.find();
            boolean rnC = mrnC.find();
            boolean scA = mscA.find();
            boolean scC = mscC.find();

            boolean jitA = mjitA.find();
            boolean jitC = mjitC.find();
            boolean wbA  = mwbA.find();
            boolean wbC  = mwbC.find();
            boolean outA = moutA.find();
            boolean outC = moutC.find();
            boolean shdA = mshdA.find();
            boolean shdC = mshdC.find();
            boolean spA  = mspA.find();
            boolean spC  = mspC.find();
            boolean flA  = mfA.find();
            boolean flC  = mfC.find();
            boolean glA  = mglA.find();
            boolean glC  = mglC.find();

            // If there are no more known tags, flush tail + stop
            if (!gA && !gC && !rA && !rC && !pA && !pC &&
                    !wA && !wC && !shA && !shC && !zA && !zC &&
                    !rnA && !rnC && !scA && !scC &&
                    !jitA && !jitC && !wbA && !wbC &&
                    !outA && !outC && !shdA && !shdC &&
                    !spA && !spC && !flA && !flC &&
                    !glA && !glC) {
                break;
            }

            // Earliest tag location of ANY type
            int idxGA   = gA   ? mgA.start()   : Integer.MAX_VALUE;
            int idxGC   = gC   ? mgC.start()   : Integer.MAX_VALUE;
            int idxRA   = rA   ? mrA.start()   : Integer.MAX_VALUE;
            int idxRC   = rC   ? mrC.start()   : Integer.MAX_VALUE;
            int idxPA   = pA   ? mpA.start()   : Integer.MAX_VALUE;
            int idxPC   = pC   ? mpC.start()   : Integer.MAX_VALUE;
            int idxWA   = wA   ? mwA.start()   : Integer.MAX_VALUE;
            int idxWC   = wC   ? mwC.start()   : Integer.MAX_VALUE;
            int idxSHA  = shA  ? mshA.start()  : Integer.MAX_VALUE;
            int idxSHC  = shC  ? mshC.start()  : Integer.MAX_VALUE;
            int idxZA   = zA   ? mzA.start()   : Integer.MAX_VALUE;
            int idxZC   = zC   ? mzC.start()   : Integer.MAX_VALUE;
            int idxRNA  = rnA  ? mrnA.start()  : Integer.MAX_VALUE;
            int idxRNC  = rnC  ? mrnC.start()  : Integer.MAX_VALUE;
            int idxSCA  = scA  ? mscA.start()  : Integer.MAX_VALUE;
            int idxSCC  = scC  ? mscC.start()  : Integer.MAX_VALUE;

            int idxJITA = jitA ? mjitA.start() : Integer.MAX_VALUE;
            int idxJITC = jitC ? mjitC.start() : Integer.MAX_VALUE;
            int idxWBA  = wbA  ? mwbA.start()  : Integer.MAX_VALUE;
            int idxWBC  = wbC  ? mwbC.start()  : Integer.MAX_VALUE;
            int idxOUTA = outA ? moutA.start() : Integer.MAX_VALUE;
            int idxOUTC = outC ? moutC.start() : Integer.MAX_VALUE;
            int idxSHDA = shdA ? mshdA.start() : Integer.MAX_VALUE;
            int idxSHDC = shdC ? mshdC.start() : Integer.MAX_VALUE;
            int idxSPA  = spA  ? mspA.start()  : Integer.MAX_VALUE;
            int idxSPC  = spC  ? mspC.start()  : Integer.MAX_VALUE;
            int idxFLA  = flA  ? mfA.start()   : Integer.MAX_VALUE;
            int idxFLC  = flC  ? mfC.start()   : Integer.MAX_VALUE;
            int idxGLA  = glA  ? mglA.start()  : Integer.MAX_VALUE;
            int idxGLC  = glC  ? mglC.start()  : Integer.MAX_VALUE;

            int pick = idxGA;
            pick = Math.min(pick, idxGC);
            pick = Math.min(pick, idxRA);
            pick = Math.min(pick, idxRC);
            pick = Math.min(pick, idxPA);
            pick = Math.min(pick, idxPC);
            pick = Math.min(pick, idxWA);
            pick = Math.min(pick, idxWC);
            pick = Math.min(pick, idxSHA);
            pick = Math.min(pick, idxSHC);
            pick = Math.min(pick, idxZA);
            pick = Math.min(pick, idxZC);
            pick = Math.min(pick, idxRNA);
            pick = Math.min(pick, idxRNC);
            pick = Math.min(pick, idxSCA);
            pick = Math.min(pick, idxSCC);

            pick = Math.min(pick, idxJITA);
            pick = Math.min(pick, idxJITC);
            pick = Math.min(pick, idxWBA);
            pick = Math.min(pick, idxWBC);
            pick = Math.min(pick, idxOUTA);
            pick = Math.min(pick, idxOUTC);
            pick = Math.min(pick, idxSHDA);
            pick = Math.min(pick, idxSHDC);
            pick = Math.min(pick, idxSPA);
            pick = Math.min(pick, idxSPC);
            pick = Math.min(pick, idxFLA);
            pick = Math.min(pick, idxFLC);
            pick = Math.min(pick, idxGLA);
            pick = Math.min(pick, idxGLC);

            if (pick > 0 && pick < Integer.MAX_VALUE) {
                // Emit any plain text before the first tag
                emitWithSimpleHex(s.substring(0, pick), ops);
                s = s.substring(pick);
            } else {
                // Re-scan on the sliced string; now we only care about tags at the FRONT
                mgA  = TAG_GRAD_OPEN.matcher(s);
                mgC  = TAG_GRAD_OPEN_CHEV.matcher(s);
                mrA  = TAG_RBW_OPEN.matcher(s);
                mrC  = TAG_RBW_OPEN_CHEV.matcher(s);
                mpA  = TAG_PULSE_OPEN.matcher(s);
                mpC  = TAG_PULSE_OPEN_CHEV.matcher(s);

                mwA   = Pattern.compile("(?i)<\\s*wave([^>]*)>").matcher(s);
                mwC   = Pattern.compile("(?i)[«]\\s*wave([^»]*)[»]").matcher(s);
                mshA  = Pattern.compile("(?i)<\\s*shake([^>]*)>").matcher(s);
                mshC  = Pattern.compile("(?i)[«]\\s*shake([^»]*)[»]").matcher(s);
                mzA   = Pattern.compile("(?i)<\\s*zoom([^>]*)>").matcher(s);
                mzC   = Pattern.compile("(?i)[«]\\s*zoom([^»]*)[»]").matcher(s);
                mrnA  = Pattern.compile("(?i)<\\s*rain([^>]*)>").matcher(s);
                mrnC  = Pattern.compile("(?i)[«]\\s*rain([^»]*)[»]").matcher(s);
                mscA  = Pattern.compile("(?i)<\\s*scroll([^>]*)>").matcher(s);
                mscC  = Pattern.compile("(?i)[«]\\s*scroll([^»]*)[»]").matcher(s);

                mjitA = Pattern.compile("(?i)<\\s*jitter([^>]*)>").matcher(s);
                mjitC = Pattern.compile("(?i)[«]\\s*jitter([^»]*)[»]").matcher(s);
                mwbA  = Pattern.compile("(?i)<\\s*wobble([^>]*)>").matcher(s);
                mwbC  = Pattern.compile("(?i)[«]\\s*wobble([^»]*)[»]").matcher(s);
                moutA = Pattern.compile("(?i)<\\s*outline([^>]*)>").matcher(s);
                moutC = Pattern.compile("(?i)[«]\\s*outline([^»]*)[»]").matcher(s);
                mshdA = Pattern.compile("(?i)<\\s*shadow([^>]*)>").matcher(s);
                mshdC = Pattern.compile("(?i)[«]\\s*shadow([^»]*)[»]").matcher(s);
                mspA  = Pattern.compile("(?i)<\\s*sparkle([^>]*)>").matcher(s);
                mspC  = Pattern.compile("(?i)[«]\\s*sparkle([^»]*)[»]").matcher(s);
                mfA   = Pattern.compile("(?i)<\\s*flicker([^>]*)>").matcher(s);
                mfC   = Pattern.compile("(?i)[«]\\s*flicker([^»]*)[»]").matcher(s);
                mglA  = Pattern.compile("(?i)<\\s*glitch([^>]*)>").matcher(s);
                mglC  = Pattern.compile("(?i)[«]\\s*glitch([^»]*)[»]").matcher(s);

                boolean rbwAt     = mrA.lookingAt()   || mrC.lookingAt();
                boolean gradAt    = mgA.lookingAt()   || mgC.lookingAt();
                boolean pulseAt   = mpA.lookingAt()   || mpC.lookingAt();
                boolean waveAt    = mwA.lookingAt()   || mwC.lookingAt();
                boolean shakeAt   = mshA.lookingAt()  || mshC.lookingAt();
                boolean zoomAt    = mzA.lookingAt()   || mzC.lookingAt();
                boolean rainAt    = mrnA.lookingAt()  || mrnC.lookingAt();
                boolean scrollAt  = mscA.lookingAt()  || mscC.lookingAt();
                boolean jitterAt  = mjitA.lookingAt() || mjitC.lookingAt();
                boolean wobbleAt  = mwbA.lookingAt()  || mwbC.lookingAt();
                boolean outlineAt = moutA.lookingAt() || moutC.lookingAt();
                boolean shadowAt  = mshdA.lookingAt() || mshdC.lookingAt();
                boolean sparkleAt = mspA.lookingAt()  || mspC.lookingAt();
                boolean flickerAt = mfA.lookingAt()   || mfC.lookingAt();
                boolean glitchAt  = mglA.lookingAt()  || mglC.lookingAt();

                // ─────────────────────────────────────
                // RAINBOW
                // ─────────────────────────────────────
                if (rbwAt) {
                    boolean chevron = mrC.lookingAt();
                    Matcher mOpen = chevron ? mrC : mrA;
                    int openEnd = mOpen.end();
                    String attrs = mOpen.group(1);
                    Matcher closePref = (chevron ? TAG_RBW_CLOSE_CHEV : TAG_RBW_CLOSE)
                            .matcher(s).region(openEnd, s.length());
                    Matcher closeOther = (chevron ? TAG_RBW_CLOSE : TAG_RBW_CLOSE_CHEV)
                            .matcher(s).region(openEnd, s.length());
                    Op r = new Op();
                    r.kind = HexFontRenderer.Kind.RAINBOW_TEXT;
                    parseRainbowArgs(attrs, r);
                    parseRainbowSpeed(attrs, r);
                    r.legacyFromTag = stylesFromAttrs(attrs);
                    if (closePref.find()) {
                        r.payload = s.substring(openEnd, closePref.start());
                        ops.add(r);
                        s = s.substring(closePref.end());
                    } else if (closeOther.find()) {
                        r.payload = s.substring(openEnd, closeOther.start());
                        ops.add(r);
                        s = s.substring(closeOther.end());
                    } else {
                        r.payload = s.substring(openEnd);
                        ops.add(r);
                        s = "";
                    }
                    continue;
                }

                // ─────────────────────────────────────
                // GRADIENT
                // ─────────────────────────────────────
                if (gradAt) {
                    boolean chevron = mgC.lookingAt();
                    Matcher mOpen = chevron ? mgC : mgA;
                    int openEnd = mOpen.end();
                    String attrs = mOpen.group(1);
                    List<String> hexes = parseHexTokens(attrs);
                    if (hexes.size() < 2) {
                        emitWithSimpleHex(s.substring(0, openEnd), ops);
                        s = s.substring(openEnd);
                    } else {
                        int[] stops = new int[hexes.size()];
                        for (int i = 0; i < hexes.size(); ++i) {
                            stops[i] = parseHexRGB((String) hexes.get(i));
                        }

                        Matcher closePref = (chevron ? TAG_GRAD_CLOSE_CHEV : TAG_GRAD_CLOSE)
                                .matcher(s).region(openEnd, s.length());
                        Matcher closeOther = (chevron ? TAG_GRAD_CLOSE : TAG_GRAD_CLOSE_CHEV)
                                .matcher(s).region(openEnd, s.length());

                        Op g = new Op();
                        g.kind = HexFontRenderer.Kind.GRADIENT_MULTI;
                        g.stops = stops;
                        parseGradAnimArgs(attrs, g);
                        g.legacyFromTag = stylesFromAttrs(attrs);
                        if (closePref.find()) {
                            g.payload = s.substring(openEnd, closePref.start());
                            ops.add(g);
                            s = s.substring(closePref.end());
                        } else if (closeOther.find()) {
                            g.payload = s.substring(openEnd, closeOther.start());
                            ops.add(g);
                            s = s.substring(closeOther.end());
                        } else {
                            g.payload = s.substring(openEnd);
                            ops.add(g);
                            s = "";
                        }
                    }
                    continue;
                }

                // ─────────────────────────────────────
                // BLOCK EFFECT TAGS (wave / shake / zoom / rain / scroll / jitter / wobble / outline / shadow / sparkle / flicker / glitch)
                // ─────────────────────────────────────

                if (waveAt) {
                    boolean chev = mwC.lookingAt();
                    Matcher mOpen = chev ? mwC : mwA;
                    int openEnd = mOpen.end();
                    String attrs = mOpen.group(1);

                    Matcher close = Pattern.compile("(?i)</\\s*wave\\s*>")
                            .matcher(s).region(openEnd, s.length());

                    boolean found = close.find();

                    Op w = new Op();
                    w.kind = Kind.WAVE_TEXT;
                    parseMotionArgs(attrs, w, 2.0F, 6.0F);

                    w.payload = found
                            ? s.substring(openEnd, close.start())
                            : s.substring(openEnd);

                    ops.add(w);
                    s = found ? s.substring(close.end()) : "";
                    continue;
                }

                if (shakeAt) {
                    boolean chev = mshC.lookingAt();
                    Matcher mOpen = chev ? mshC : mshA;
                    int openEnd = mOpen.end();
                    String attrs = mOpen.group(1);

                    Matcher close = Pattern.compile("(?i)</\\s*shake\\s*>")
                            .matcher(s).region(openEnd, s.length());

                    boolean found = close.find();

                    Op sh = new Op();
                    sh.kind = Kind.SHAKE_TEXT;
                    parseMotionArgs(attrs, sh, 2.0F, 0.0F);

                    sh.payload = found
                            ? s.substring(openEnd, close.start())
                            : s.substring(openEnd);

                    ops.add(sh);
                    s = found ? s.substring(close.end()) : "";
                    continue;
                }

                if (zoomAt) {
                    boolean chev = mzC.lookingAt();
                    Matcher mOpen = chev ? mzC : mzA;
                    int openEnd = mOpen.end();
                    String attrs = mOpen.group(1);

                    Matcher close = Pattern.compile("(?i)</\\s*zoom\\s*>")
                            .matcher(s).region(openEnd, s.length());

                    boolean found = close.find();

                    Op z = new Op();
                    z.kind = Kind.ZOOM_TEXT;
                    parseMotionArgs(attrs, z, 1.0F, 0.0F);

                    z.payload = found
                            ? s.substring(openEnd, close.start())
                            : s.substring(openEnd);

                    ops.add(z);
                    s = found ? s.substring(close.end()) : "";
                    continue;
                }

                if (rainAt) {
                    boolean chev = mrnC.lookingAt();
                    Matcher mOpen = chev ? mrnC : mrnA;
                    int openEnd = mOpen.end();
                    String attrs = mOpen.group(1);

                    Matcher close = Pattern.compile("(?i)</\\s*rain\\s*>")
                            .matcher(s).region(openEnd, s.length());

                    boolean found = close.find();

                    Op rn = new Op();
                    rn.kind = Kind.RAIN_TEXT;
                    parseMotionArgs(attrs, rn, 4.0F, 8.0F);

                    rn.payload = found
                            ? s.substring(openEnd, close.start())
                            : s.substring(openEnd);

                    ops.add(rn);
                    s = found ? s.substring(close.end()) : "";
                    continue;
                }

                if (scrollAt) {
                    boolean chev = mscC.lookingAt();
                    Matcher mOpen = chev ? mscC : mscA;
                    int openEnd = mOpen.end();
                    String attrs = mOpen.group(1);

                    Matcher close = Pattern.compile("(?i)</\\s*scroll\\s*>")
                            .matcher(s).region(openEnd, s.length());

                    boolean found = close.find();

                    Op sc = new Op();
                    sc.kind = Kind.SCROLL_TEXT;
                    parseMotionArgs(attrs, sc, 2.0F, 5.0F);

                    sc.payload = found
                            ? s.substring(openEnd, close.start())
                            : s.substring(openEnd);

                    ops.add(sc);
                    s = found ? s.substring(close.end()) : "";
                    continue;
                }

                if (jitterAt) {
                    boolean chev = mjitC.lookingAt();
                    Matcher mOpen = chev ? mjitC : mjitA;
                    int openEnd = mOpen.end();
                    String attrs = mOpen.group(1);

                    Matcher close = Pattern.compile("(?i)</\\s*jitter\\s*>")
                            .matcher(s).region(openEnd, s.length());

                    boolean found = close.find();

                    Op jt = new Op();
                    jt.kind = Kind.JITTER_TEXT;
                    parseMotionArgs(attrs, jt, 1.0F, 12.0F);

                    jt.payload = found
                            ? s.substring(openEnd, close.start())
                            : s.substring(openEnd);

                    ops.add(jt);
                    s = found ? s.substring(close.end()) : "";
                    continue;
                }

                if (wobbleAt) {
                    boolean chev = mwbC.lookingAt();
                    Matcher mOpen = chev ? mwbC : mwbA;
                    int openEnd = mOpen.end();
                    String attrs = mOpen.group(1);

                    Matcher close = Pattern.compile("(?i)</\\s*wobble\\s*>")
                            .matcher(s).region(openEnd, s.length());

                    boolean found = close.find();

                    Op wb = new Op();
                    wb.kind = Kind.WOBBLE_TEXT;
                    parseMotionArgs(attrs, wb, 4.0F, 2.0F);

                    wb.payload = found
                            ? s.substring(openEnd, close.start())
                            : s.substring(openEnd);

                    ops.add(wb);
                    s = found ? s.substring(close.end()) : "";
                    continue;
                }

                if (outlineAt) {
                    boolean chev = moutC.lookingAt();
                    Matcher mOpen = chev ? moutC : moutA;
                    int openEnd = mOpen.end();
                    String attrs = mOpen.group(1);

                    Matcher close = Pattern.compile("(?i)</\\s*outline\\s*>")
                            .matcher(s).region(openEnd, s.length());

                    boolean found = close.find();

                    Op o = new Op();
                    o.kind = Kind.OUTLINE_TEXT;
                    parseOutlineArgs(attrs, o);

                    o.payload = found
                            ? s.substring(openEnd, close.start())
                            : s.substring(openEnd);

                    ops.add(o);
                    s = found ? s.substring(close.end()) : "";
                    continue;
                }

                if (shadowAt) {
                    boolean chev = mshdC.lookingAt();
                    Matcher mOpen = chev ? mshdC : mshdA;
                    int openEnd = mOpen.end();
                    String attrs = mOpen.group(1);

                    Matcher close = Pattern.compile("(?i)</\\s*shadow\\s*>")
                            .matcher(s).region(openEnd, s.length());

                    boolean found = close.find();

                    Op o = new Op();
                    o.kind = Kind.SHADOW_TEXT;
                    parseShadowArgs(attrs, o);

                    o.payload = found
                            ? s.substring(openEnd, close.start())
                            : s.substring(openEnd);

                    ops.add(o);
                    s = found ? s.substring(close.end()) : "";
                    continue;
                }

                if (sparkleAt) {
                    boolean chev = mspC.lookingAt();
                    Matcher mOpen = chev ? mspC : mspA;
                    int openEnd = mOpen.end();
                    String attrs = mOpen.group(1);

                    Matcher close = Pattern.compile("(?i)</\\s*sparkle\\s*>")
                            .matcher(s).region(openEnd, s.length());

                    boolean found = close.find();

                    Op sp = new Op();
                    sp.kind = Kind.SPARKLE_TEXT;
                    parseSparkleArgs(attrs, sp);

                    sp.payload = found
                            ? s.substring(openEnd, close.start())
                            : s.substring(openEnd);

                    ops.add(sp);
                    s = found ? s.substring(close.end()) : "";
                    continue;
                }

                if (flickerAt) {
                    boolean chev = mfC.lookingAt();
                    Matcher mOpen = chev ? mfC : mfA;
                    int openEnd = mOpen.end();
                    String attrs = mOpen.group(1);

                    Matcher close = Pattern.compile("(?i)</\\s*flicker\\s*>")
                            .matcher(s).region(openEnd, s.length());

                    boolean found = close.find();

                    Op fk = new Op();
                    fk.kind = Kind.FLICKER_TEXT;
                    parseFlickerArgs(attrs, fk);

                    fk.payload = found
                            ? s.substring(openEnd, close.start())
                            : s.substring(openEnd);

                    ops.add(fk);
                    s = found ? s.substring(close.end()) : "";
                    continue;
                }

                if (glitchAt) {
                    boolean chev = mglC.lookingAt();
                    Matcher mOpen = chev ? mglC : mglA;
                    int openEnd = mOpen.end();
                    String attrs = mOpen.group(1);

                    Matcher close = Pattern.compile("(?i)</\\s*glitch\\s*>")
                            .matcher(s).region(openEnd, s.length());

                    boolean found = close.find();

                    Op gl = new Op();
                    gl.kind = Kind.GLITCH_TEXT;
                    parseGlitchArgs(attrs, gl);

                    gl.payload = found
                            ? s.substring(openEnd, close.start())
                            : s.substring(openEnd);

                    ops.add(gl);
                    s = found ? s.substring(close.end()) : "";
                    continue;
                }

                // ─────────────────────────────────────
                // PULSE (unchanged) — only if no other block tags matched at front
                // ─────────────────────────────────────
                if (!pulseAt) {
                    // No recognized tag at the front; bail from loop
                    break;
                }

                boolean chevron = mpC.lookingAt();
                Matcher mOpen = chevron ? mpC : mpA;
                int openEnd = mOpen.end();
                String attrs = mOpen.group(1);
                Matcher closePref = (chevron ? TAG_PULSE_CLOSE_CHEV : TAG_PULSE_CLOSE)
                        .matcher(s).region(openEnd, s.length());
                Matcher closeOther = (chevron ? TAG_PULSE_CLOSE : TAG_PULSE_CLOSE_CHEV)
                        .matcher(s).region(openEnd, s.length());
                Op p = new Op();
                p.kind = HexFontRenderer.Kind.PULSE_HEX;
                parsePulseArgs(attrs, p);
                p.legacyFromTag = stylesFromAttrs(attrs);
                if (closePref.find()) {
                    p.payload = s.substring(openEnd, closePref.start());
                    ops.add(p);
                    s = s.substring(closePref.end());
                } else if (closeOther.find()) {
                    p.payload = s.substring(openEnd, closeOther.start());
                    ops.add(p);
                    s = s.substring(closeOther.end());
                } else {
                    p.payload = s.substring(openEnd);
                    ops.add(p);
                    s = "";
                }
            }
        }

        if (!s.isEmpty()) {
            emitWithSimpleHex(s, ops);
        }

        return ops;
    }

    private static String stripStrayAnimatedClosers(String s) {
        if (s != null && !s.isEmpty()) {
            // existing: strip grad / rainbow / pulse closers (angle + chevron)
            s = GRAD_CLOSE_ANY.matcher(s).replaceAll("");
            s = RBW_CLOSE_ANY.matcher(s).replaceAll("");
            s = PULSE_CLOSE_ANY.matcher(s).replaceAll("");

            // NEW: strip closers for our extra block tags
            s = s.replaceAll(
                    "(?i)</\\s*(wave|shake|zoom|rain|scroll|outline|shadow|sparkle|flicker|glitch|jitter|wobble)\\s*>",
                    ""
            );

            // NEW: strip stray hex close tags too
            s = s.replaceAll("(?i)</\\s*#\\s*>", "");

            // existing generic “dangling close tag at end of line” cleanup
            s = s.replaceAll("(?i)</[a-z]*\\s*$", "");
            return s;
        }
        return s;
    }


    private static void emitWithSimpleHex(String s, List<Op> out) {
        int i = 0;
        int n = s.length();

        while (i < n) {

            // 1) Skip gradient / rainbow / pulse closers silently
            Matcher m = GRAD_CLOSE_ANY.matcher(s).region(i, n);
            if (m.lookingAt()) {
                i = m.end();
                continue;
            }
            m = RBW_CLOSE_ANY.matcher(s).region(i, n);
            if (m.lookingAt()) {
                i = m.end();
                continue;
            }
            m = PULSE_CLOSE_ANY.matcher(s).region(i, n);
            if (m.lookingAt()) {
                i = m.end();
                continue;
            }

            // 2) NEW: standalone hex close tags → POP_HEX, no visible text
            Matcher mCloseA = TAG_HEX_CLOSE.matcher(s).region(i, n);
            Matcher mCloseC = TAG_HEX_CLOSE_CHEV.matcher(s).region(i, n);
            if (mCloseA.lookingAt() || mCloseC.lookingAt()) {
                Op pop = new Op();
                pop.kind = Kind.POP_HEX;
                out.add(pop);

                i = mCloseA.lookingAt() ? mCloseA.end() : mCloseC.end();
                continue;
            }

            // 3) Look for hex OPEN tags in the remaining text
            Matcher openA = TAG_HEX_ANY.matcher(s).region(i, n);
            Matcher openC = TAG_HEX_ANY_CHEV.matcher(s).region(i, n);
            boolean hasA = openA.find();
            boolean hasC = openC.find();

            if (!hasA && !hasC) {
                // No more hex opens: flush the rest as plain text (minus stray animated closers)
                String tail = stripStrayAnimatedClosers(s.substring(i));
                if (!tail.isEmpty()) {
                    Op t = new Op();
                    t.kind = Kind.TEXT;
                    t.payload = tail;
                    out.add(t);
                }
                break;
            }

            int aStart = hasA ? openA.start() : Integer.MAX_VALUE;
            int cStart = hasC ? openC.start() : Integer.MAX_VALUE;
            boolean pickChevron = cStart < aStart;
            int segStart = pickChevron ? cStart : aStart;

            // Emit any plain text BEFORE this hex tag
            if (segStart > i) {
                String chunk = stripStrayAnimatedClosers(s.substring(i, segStart));
                if (!chunk.isEmpty()) {
                    Op t = new Op();
                    t.kind = Kind.TEXT;
                    t.payload = chunk;
                    out.add(t);
                }
            }

            if (pickChevron) {
                // «#RRGGBBstyles»
                Matcher mOpen = openC;
                int afterOpen = mOpen.end();
                int rgb = parseHexRGB(mOpen.group(1));

                Op push = new Op();
                push.kind = Kind.PUSH_HEX;
                push.rgb = rgb;
                out.add(push);

                String legacy = stylesToLegacy(mOpen.group(2));
                if (!legacy.isEmpty()) {
                    Op tStyle = new Op();
                    tStyle.kind = Kind.TEXT;
                    tStyle.payload = legacy;
                    out.add(tStyle);
                }

                // If we find a matching chevron closer in THIS substring, treat body as plain text
                Matcher close = TAG_HEX_CLOSE_CHEV.matcher(s).region(afterOpen, n);
                if (close.find()) {
                    if (close.start() > afterOpen) {
                        Op t = new Op();
                        t.kind = Kind.TEXT;
                        t.payload = s.substring(afterOpen, close.start());
                        out.add(t);
                    }

                    Op pop = new Op();
                    pop.kind = Kind.POP_HEX;
                    out.add(pop);

                    i = close.end();
                } else {
                    // No closer here → keep hex active; remainder becomes TEXT, POP handled later
                    if (afterOpen < n) {
                        String chunk = s.substring(afterOpen);
                        if (!chunk.isEmpty()) {
                            Op t = new Op();
                            t.kind = Kind.TEXT;
                            t.payload = chunk;
                            out.add(t);
                        }
                    }
                    i = n;
                }

            } else {
                // <#RRGGBBstyles>
                Matcher mOpen = openA;
                int afterOpen = mOpen.end();
                int rgb = parseHexRGB(mOpen.group(1));

                Op push = new Op();
                push.kind = Kind.PUSH_HEX;
                push.rgb = rgb;
                out.add(push);

                String legacy = stylesToLegacy(mOpen.group(2));
                if (!legacy.isEmpty()) {
                    Op tStyle = new Op();
                    tStyle.kind = Kind.TEXT;
                    tStyle.payload = legacy;
                    out.add(tStyle);
                }

                Matcher close = TAG_HEX_CLOSE.matcher(s).region(afterOpen, n);
                if (close.find()) {
                    if (close.start() > afterOpen) {
                        Op t = new Op();
                        t.kind = Kind.TEXT;
                        t.payload = s.substring(afterOpen, close.start());
                        out.add(t);
                    }

                    Op pop = new Op();
                    pop.kind = Kind.POP_HEX;
                    out.add(pop);

                    i = close.end();
                } else {
                    // No closer here → keep hex active until a later </#> or end-of-line
                    if (afterOpen < n) {
                        Op t = new Op();
                        t.kind = Kind.TEXT;
                        t.payload = s.substring(afterOpen);
                        out.add(t);
                    }
                    i = n;
                }
            }
        }
    }

    private static final class LegacyState {
        boolean bold;
        boolean italic;
        boolean underline;
        boolean strikethrough;
        boolean obfuscated;
        boolean obfOnce;
        Integer colorRgb;

        private LegacyState() {
        }

        void reset() {
            this.bold = this.italic = this.underline = this.strikethrough = this.obfuscated = this.obfOnce = false;
            this.colorRgb = null;
        }
    }

    private static enum Kind {
        TEXT,
        PUSH_HEX,
        POP_HEX,
        GRADIENT_MULTI,
        RAINBOW_TEXT,
        PULSE_HEX,
        WAVE_TEXT,
        SHAKE_TEXT,
        ZOOM_TEXT,
        RAIN_TEXT,
        SCROLL_TEXT,
        OUTLINE_TEXT,
        SHADOW_TEXT,
        SPARKLE_TEXT,
        FLICKER_TEXT,
        GLITCH_TEXT,
        JITTER_TEXT,
        WOBBLE_TEXT;

        private Kind() {
        }
    }

    private static class Op {
        Kind kind;

        // existing fields
        String payload;
        int rgb;
        int[] stops;
        int cycles;
        float sat;
        float val;
        float phase;
        float speed;
        boolean scroll;
        int solid;
        float amp;
        boolean pulseOn;
        float pulseAmp;
        float pulseSpeed;
        String legacyFromTag;

        // ──────────────────────────────
        // NEW minimal fields so new Kinds don't break
        // ──────────────────────────────

        // outline color (optional)
        int outlineColor = 0x000000;     // default black

        // shadow color
        int shadowColor = 0x000000;      // default black

        // sparkle intensity (0–1)
        float sparkleIntensity = 1.0F;

        // flicker speed (0–?)
        float flickerSpeed = 1.0F;

        // glitch amount (0–5)
        float glitchAmount = 1.0F;

        // jitter amplitude
        float jitterAmp = 1.0F;

        // wobble amplitude + speed
        float wobbleAmp = 1.0F;
        float wobbleSpeed = 1.0F;

        // ──────────────────────────────

        private Op() {
            // original defaults
            this.cycles     = 1;
            this.sat        = 1.0F;
            this.val        = 1.0F;
            this.phase      = 0.0F;
            this.speed      = 0.0F;
            this.scroll     = false;
            this.solid      = -1;
            this.amp        = 0.0F;
            this.pulseOn    = false;
            this.pulseAmp   = 0.0F;
            this.pulseSpeed = 1.0F;
            this.rgb        = -1;
        }
    }

}
