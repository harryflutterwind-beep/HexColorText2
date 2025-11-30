// src/main/java/com/example/examplemod/client/HexChatWrapFix.java
package com.example.examplemod.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;

public class HexChatWrapFix {

    // ============================================================
    // 0. HEADER FOR NEXT LINE (vanilla + custom tags)
    // ============================================================

    /**
     * Full "header" for the next wrapped line.
     *
     * - Vanilla-style: active § color / style codes at end of this line
     * - Custom: any open <grad>/<wave>/<pulse>/etc tags from this line
     */
    public static String computeHeaderForNextLine(String line) {
        if (line == null || line.isEmpty())
            return "";

        String vanillaHeader = getActiveVanillaFormats(line);
        String customHeader  = computeCarryFromLine(line);

        // Prepend vanilla § codes, then our tags
        return vanillaHeader + customHeader;
    }

    /**
     * Local clone of FontRenderer.getFormatFromString, but public and
     * independent of FontRenderer.
     */
    private static String getActiveVanillaFormats(String s) {
        if (s == null || s.isEmpty()) return "";

        String result = "";
        int len = s.length();

        for (int i = 0; i < len - 1; ++i) {
            char c = s.charAt(i);
            if (c != '§') continue;

            char code = Character.toLowerCase(s.charAt(i + 1));
            i++; // skip the format char we just consumed

            // color (0–9, a–f)
            if ((code >= '0' && code <= '9') ||
                    (code >= 'a' && code <= 'f')) {
                // color replaces previous color + styles
                result = "§" + code;
                continue;
            }

            // special formats k–o, reset r
            if (code == 'r') {
                // reset clears everything
                result = "";
            } else if ("klmno".indexOf(code) >= 0) {
                // append extra style
                result = result + "§" + code;
            }
        }

        return result;
    }
    private static String getActiveLegacyFormatting(String line) {
        StringBuilder out = new StringBuilder();

        char[] chars = line.toCharArray();
        char color = 0;

        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strike = false;
        boolean obf = false;

        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '§') {
                char c = Character.toLowerCase(chars[i + 1]);

                switch (c) {
                    case 'k': obf = true; break;
                    case 'l': bold = true; break;
                    case 'm': strike = true; break;
                    case 'n': underline = true; break;
                    case 'o': italic = true; break;

                    // Colors
                    case '0': case '1': case '2': case '3':
                    case '4': case '5': case '6': case '7':
                    case '8': case '9':
                    case 'a': case 'b': case 'c':
                    case 'd': case 'e': case 'f':
                        color = c;
                        bold = italic = underline = strike = obf = false;
                        break;

                    // Reset §r
                    case 'r':
                        color = 0;
                        bold = italic = underline = strike = obf = false;
                        break;
                }
            }
        }

        if (color != 0) out.append("§").append(color);
        if (bold) out.append("§l");
        if (italic) out.append("§o");
        if (underline) out.append("§n");
        if (strike) out.append("§m");
        if (obf) out.append("§k");

        return out.toString();
    }

    // ============================================================
    // 1. CUSTOM TAG CARRY LOGIC
    // ============================================================

    /**
     * Figure out which of our custom tags are still "open" at the end
     * of this line and should be carried to the next line.
     *
     * We treat these as long-running, full-line styles:
     *   - <grad ...>  </grad>
     *   - <pulse ...> </pulse>
     *   - <wave ...>  </wave>
     *   - <zoom ...>  </zoom>
     *   - <shake ...> </shake>
     *   - <wobble ...></wobble>
     *   - <jitter ...></jitter>
     *   - <outline ...></outline>
     *   - <shadow ...></shadow>
     *   - <glow ...>  </glow>
     *   - <sparkle ...></sparkle>
     *   - <flicker ...></flicker>
     *   - <glitch ...></glitch>
     *   - <scroll ...></scroll>
     *   - <rain>, <rainbow>, <rb>, <rbw> and their closes
     */
    public static String computeCarryFromLine(String line) {
        if (line == null || line.isEmpty()) {
            return "";
        }

        String lower = line.toLowerCase();
        StringBuilder sb = new StringBuilder();

        // --------------------------------------------------------------------
        // 1) CARRY LEGACY FORMATTING (§ codes)
        // --------------------------------------------------------------------
        String legacy = getActiveLegacyFormatting(line);
        if (!legacy.isEmpty()) {
            sb.append(legacy);
        }

        // --------------------------------------------------------------------
        // 2) HANDLE ALL SIMPLE TAGS (grad, pulse, wave…)
        // --------------------------------------------------------------------
        String[] simpleTags = new String[]{
                "grad", "pulse", "wave", "zoom", "shake",
                "wobble", "jitter", "outline", "shadow",
                "glow", "sparkle", "flicker", "glitch", "scroll"
        };

        for (String name : simpleTags) {

            boolean open =
                    countAny(lower, "<" + name, "«" + name) >
                            countAny(lower, "</" + name, "«/" + name);

            if (open) {
                String header = findLastTagHeader(line, name);
                if (header != null) {
                    sb.append(header);
                }
            }
        }

        // --------------------------------------------------------------------
        // 3) SPECIAL HANDLING FOR RAINBOW GROUP
        // (rain, rainbow, rb, rbw)
        // --------------------------------------------------------------------
        boolean rainOpen =
                countAny(lower,
                        "<rain", "«rain", "<rainbow", "«rainbow",
                        "<rb", "«rb", "<rbw", "«rbw") >
                        countAny(lower,
                                "</rain", "«/rain", "</rainbow", "«/rainbow",
                                "</rb", "«/rb", "</rbw", "«/rbw");

        if (rainOpen) {
            String h = findLastRainbowHeader(line);
            if (h != null) {
                sb.append(h);
            }
        }

        return sb.toString();
    }

  //  @SuppressWarnings("rawtypes")
    @SuppressWarnings("rawtypes")
    public static List<String> carryAnimatedAcross(List lines) {
        List<String> out = new ArrayList<String>();
        if (lines == null || lines.isEmpty()) {
            return out;
        }

        // ─────────────────────────────────────────────
        // 1) Detect "global" header from FIRST line
        //    (e.g. <wave>, <grad ...>, <zoom>, <shake>, <rainbow>, etc.)
        // ─────────────────────────────────────────────
        Object firstObj = lines.get(0);
        String firstRaw = (firstObj == null) ? "" : firstObj.toString();

        // Just grab the leading header tag (<wave ...>, <grad ...>, etc.)
        // after hoistGlobalHeader has moved it to the front.
        String globalTagHeader = detectLeadingHeaderTag(firstRaw);
        if (globalTagHeader == null) {
            globalTagHeader = "";
        }

        // §-codes carry separately
        String formatCarry = "";
        int idx = 0;

        // ─────────────────────────────────────────────
        // 2) Process each physical wrapped line
        // ─────────────────────────────────────────────
        for (Object o : lines) {
            String raw = (o == null) ? "" : o.toString();
            String stripped = stripLeadingResets(raw);

            // Body of this line, with any in-line header removed on line 0
            String body = stripped;
            if (!globalTagHeader.isEmpty() && idx == 0) {
                int pos = body.indexOf(globalTagHeader);
                if (pos != -1) {
                    body = body.substring(0, pos)
                            + body.substring(pos + globalTagHeader.length());
                }
            }

            // Global tag header is applied to EVERY line
            String tagPrefix = globalTagHeader.isEmpty() ? "" : globalTagHeader;

            String withCarry = formatCarry + tagPrefix + body;

            System.out.println("[HexCarry] line[" + idx + "] raw='" + raw + "'");
            System.out.println("[HexCarry]   stripped='" + stripped + "'");
            System.out.println("[HexCarry]   body='" + body + "'");
            System.out.println("[HexCarry]   prefix='" + formatCarry + tagPrefix + "'");
            System.out.println("[HexCarry]   withCarry='" + withCarry + "'");

            out.add(withCarry);

            // Update § color/style carry for the NEXT line
            formatCarry = getActiveVanillaFormats(withCarry);

            idx++;
        }

        return out;
    }

    // ============================================================
    // 3. SUPPORT HELPERS
    // ============================================================

    public static String stripLeadingResets(String s) {
        if (s == null || s.isEmpty()) return "";
        int i = 0;
        int n = s.length();

        while (i + 1 < n && s.charAt(i) == '§'
                && Character.toLowerCase(s.charAt(i + 1)) == 'r') {
            i += 2; // skip this "§r"
        }

        return s.substring(i);
    }
    private static String detectLeadingHeaderTag(String s) {
        if (s == null || s.isEmpty()) return "";

        // Remove leading §r (you already have this helper)
        String stripped = stripLeadingResets(s);
        if (stripped.isEmpty()) return "";

        char first = stripped.charAt(0);
        if (first != '<' && first != '«') {
            // No header-style tag at the very front
            return "";
        }

        // For '<tag ...>' we close at '>'
        // For '«tag ...»' we close at '»'
        char closeCh = (first == '«') ? '»' : '>';
        int end = stripped.indexOf(closeCh, 1);
        if (end < 0) {
            // malformed tag, bail out
            return "";
        }

        // Return the tag including its closing char ("<wave ...>" or "«grad ...»")
        return stripped.substring(0, end + 1);
    }

    private static int count(String text, String find) {
        if (text == null || find == null) return 0;
        int idx = 0, c = 0;
        while ((idx = text.indexOf(find, idx)) != -1) {
            c++;
            idx += find.length();
        }
        return c;
    }

    private static int countAny(String text, String... needles) {
        if (text == null) return 0;
        int total = 0;
        for (String n : needles) {
            if (n != null && !n.isEmpty()) {
                total += count(text, n);
            }
        }
        return total;
    }

    private static String findLastTagHeader(String line, String tagName) {
        if (line == null) return null;

        String needleA = "<" + tagName;
        String needleB = "«" + tagName;

        int idxA = line.lastIndexOf(needleA);
        int idxB = line.lastIndexOf(needleB);

        int idx = Math.max(idxA, idxB);
        if (idx < 0) return null;

        char openCh = line.charAt(idx);
        char closeCh = (openCh == '«') ? '»' : '>';

        int end = line.indexOf(closeCh, idx + 1);
        if (end < 0) end = line.length();

        return line.substring(idx, Math.min(end + 1, line.length()));
    }

    private static String findLastRainbowHeader(String line) {
        if (line == null) return null;

        String[] names = {"rainbow", "rb", "rbw", "rain"};

        int bestIdx = -1;
        String bestName = null;
        char bestOpen = '<';

        for (String name : names) {
            String a = "<" + name;
            String b = "«" + name;

            int idxA = line.lastIndexOf(a);
            int idxB = line.lastIndexOf(b);

            if (idxA > bestIdx) {
                bestIdx = idxA;
                bestName = name;
                bestOpen = '<';
            }
            if (idxB > bestIdx) {
                bestIdx = idxB;
                bestName = name;
                bestOpen = '«';
            }
        }

        if (bestIdx < 0 || bestName == null) return null;

        char closeCh = (bestOpen == '«') ? '»' : '>';
        int end = line.indexOf(closeCh, bestIdx + 1);
        if (end < 0) end = line.length();

        return line.substring(bestIdx, Math.min(end + 1, line.length()));
    }

    // ============================================================
    // 4. COMPONENT-LEVEL WRAP (for GuiNewChat)
    // ============================================================

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List carryComponents(List components) {
        if (components == null || components.isEmpty()) {
            return components;
        }

        // Pull text + styles out
        List texts  = new ArrayList(components.size());
        List styles = new ArrayList(components.size());

        for (int i = 0; i < components.size(); i++) {
            Object o = components.get(i);

            if (o instanceof IChatComponent) {
                IChatComponent ic = (IChatComponent)o;

                // keep vanilla § codes in the string
                texts.add(ic.getFormattedText());

                ChatStyle st = ic.getChatStyle();
                styles.add(st != null ? st.createShallowCopy() : new ChatStyle());
            } else {
                texts.add(o != null ? o.toString() : "");
                styles.add(new ChatStyle());
            }
        }

        // Apply your string-level carry logic
        List carried = carryAnimatedAcross(texts);

        // Rebuild components with same styles
        List out = new ArrayList(carried.size());
        for (int i = 0; i < carried.size(); i++) {
            String s = (String)carried.get(i);
            ChatComponentText c = new ChatComponentText(s);

            if (i < styles.size() && styles.get(i) != null) {
                c.setChatStyle((ChatStyle)styles.get(i));
            }

            out.add(c);
        }

        System.out.println("[HexCarry] carryComponents: in=" + components.size()
                + " out=" + out.size());

        return out;
    }

}
