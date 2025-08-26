package pmcp.mode.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import pmcp.PMCP;
import pmcp.event.EventTarget;
import pmcp.event.mode.render.EventRender2D;
import pmcp.mode.Mode;
import pmcp.mode.client.HUDSetting;
import pmcp.mode.move.Fly;
import pmcp.mode.player.Blink;
import pmcp.mode.world.Scaffold;
import pmcp.ui.font.FontManager;
import pmcp.utils.category.Category;
import pmcp.utils.category.UserCategory;
import pmcp.utils.heypixel.VIPUtils;
import pmcp.utils.math.MathUtils;
import pmcp.utils.render.AnimationUtils;
import pmcp.utils.render.ColorUtils;
import pmcp.utils.render.RenderUtils;
import pmcp.utils.render.StencilUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

public class DynamicIsLand extends Mode {

    public static DynamicIsLand Instance;

    private final List<HUDInfo> hudInfoList = new ArrayList<>();
    private final List<HUDRenderEntry> renderQueue = new ArrayList<>();
    private final HUDInfo placeholderHUD = new HUDInfo(() -> "", () -> false, () -> 1.0, HUDCategory.PLACEHOLDER);
    private float tabAnim = 0f;

    public Component header;
    public Component footer;

    public DynamicIsLand() {
        super("DynamicIsLand", "灵动岛", "Displays dynamic island-style HUD info", Category.HUD);
        Instance = this;
    }

    @Override
    public void onEnable() {
        hudInfoList.clear();

        hudInfoList.add(new HUDInfo(() -> {
            ItemStack item = Minecraft.getInstance().player.getUseItem();
            if (item.getItem() instanceof PotionItem) return "[POT] Drinking Potion";
            if (item.getItem().isEdible()) return "[MEAT] Eating";
            return "";
        }, () -> {
            ItemStack item = Minecraft.getInstance().player.getUseItem();
            return (Minecraft.getInstance().player.isUsingItem() &&
                    (item.getItem() instanceof PotionItem || item.getItem().isEdible())) && mc.options.keyUse.isDown();
        }, () -> Minecraft.getInstance().player.getUseItemRemainingTicks() / 32.0, HUDCategory.ITEM_USE));

        hudInfoList.add(new HUDInfo("Scaffold Blocks",
                () -> Scaffold.Instance.blockSize / (Scaffold.Instance.maxBlockSize + 1.0),
                () -> Scaffold.Instance.isEnable(),
                HUDCategory.BLOCK));

        hudInfoList.add(new HUDInfo("Blink Packets",
                () -> (Blink.Instance.packets.size() / Blink.Instance.maxPackets.getValue().doubleValue()),
                () -> Blink.Instance.isEnable(),
                HUDCategory.PACKET));

        hudInfoList.add(new HUDInfo("Fly Packets",
                () -> Fly.Instance.s12Packets.size() / (Fly.Instance.maxVelS12 + 1.0),
                () -> Fly.Instance.isEnable() && Fly.Instance.modeValue.getValue().equals(Fly.mode.LongJump),
                HUDCategory.FLY));
    }

    @EventTarget
    public void onRender2D(EventRender2D render2D) {
        if (mc.player == null || mc.level == null || mc.screen != null) return;

        if (render2D.getType() == EventRender2D.Type.POST) {
            boolean tabHeld = mc.options.keyPlayerList.isDown();
            tabAnim += ((tabHeld ? 1f : 0f) - tabAnim) * 0.2f;
            if (tabAnim > 0.01f) {
                renderTabListAsCard(render2D, tabAnim);
                if (tabHeld) return;
            }

            PoseStack pose = render2D.getGuiGraphics().pose();
            FontManager font = PMCP.Instance.getFontManager();

            double bps = Math.min(getBPS(), 50);
            String string = getUserName() + " | FPS: " + Minecraft.getInstance().getFps() + " | " + String.format("BPS: %.1f", bps);
            float cardWidth = Math.max(130, font.font16.getStringWidth(string));
            float cardHeight = 24;
            float spacing = 6;
            float baseX = render2D.getGuiGraphics().guiWidth() / 2f - cardWidth / 2f;
            float baseY = 10;

            boolean hasActive = hudInfoList.stream().anyMatch(h -> h.isVisible.get());
            placeholderHUD.name = getUserName() + " | FPS: " + Minecraft.getInstance().getFps();

            if (renderQueue.stream().noneMatch(e -> e.info.category == HUDCategory.PLACEHOLDER)) {
                renderQueue.add(new HUDRenderEntry(placeholderHUD));
            }
            renderQueue.stream()
                    .filter(e -> e.info.category == HUDCategory.PLACEHOLDER)
                    .forEach(e -> e.active = !hasActive);

            for (HUDInfo info : hudInfoList) {
                info.name = info.getName();
                String id = info.getId();
                boolean visible = info.isVisible.get();
                if (visible && renderQueue.stream().noneMatch(e -> e.getId().equals(id))) {
                    renderQueue.add(new HUDRenderEntry(info));
                }
                renderQueue.stream().filter(e -> e.getId().equals(id)).forEach(e -> e.active = visible);
            }

            renderQueue.removeIf(e -> !e.active && e.alpha <= 0.01f && e.yOffset <= -28f);

            pose.pushPose();
            float yOffset = baseY;
            for (HUDRenderEntry entry : renderQueue) {
                HUDInfo info = entry.info;
                entry.alpha = (float) AnimationUtils.smooth(entry.active ? 1f : 0f, entry.alpha, 0.1);
                entry.yOffset = (float) AnimationUtils.smooth(entry.active ? 0f : -30f, entry.yOffset, 0.1);
                info.displayProgress = AnimationUtils.smooth(info.progressSupplier.get(), info.displayProgress, 0.1);

                if (entry.alpha < 0.01f && entry.yOffset <= -28f) continue;

                int alpha = (int) (entry.alpha * 255f);
                Color fill = ColorUtils.getColor(new Color(0x56CC9D), alpha);
                Color text = new Color(220, 220, 220, alpha);

                float y = yOffset + entry.yOffset;
                StencilUtils.write(false);
                RenderUtils.drawRoundedRect(pose, baseX, y, cardWidth, 24, 4, HUDSetting.Instance.getColor());
                StencilUtils.erase(true);

                RenderUtils.drawRoundedRect(pose, baseX, y, cardWidth, 24, 4, HUDSetting.Instance.getColor());

                if (info.category == HUDCategory.PLACEHOLDER) {
                    double progress = Math.min(bps / 20.0, 1.0);
                    Color bpsColor = bps <= 20 ? ColorUtils.getColor(new Color(0x56CC9D), alpha) : new Color(220, 60, 60, alpha);
                    RenderUtils.drawRoundedRect(pose, baseX + 4, y + 24 - 8, (float) (progress * (cardWidth - 8)), 4, 2, bpsColor);
                    font.font16.drawString(pose, string, baseX + 6, y + 6, Color.WHITE.getRGB());
                } else {
                    RenderUtils.drawRoundedRect(pose, baseX + 4, y + 24 - 8, (float) (info.displayProgress * (cardWidth - 8)), 4, 2, fill);
                    switch (info.category) {
                        case BLOCK ->
                                font.font16.drawString(pose, info.getName() + ": " + Scaffold.Instance.blockSize, baseX + 6, y + 6, text.getRGB());
                        case PACKET ->
                                font.font16.drawString(pose, info.getName() + ": " + Blink.Instance.packets.size(), baseX + 6, y + 6, text.getRGB());
                        case FLY ->
                                font.font16.drawString(pose, info.getName() + ": " + Fly.Instance.s12Packets.size(), baseX + 6, y + 6, text.getRGB());
                        default -> font.font16.drawString(pose, info.getName(), baseX + 6, y + 6, text.getRGB());
                    }
                }

                StencilUtils.dispose();
                yOffset += 24 + spacing;
            }

            pose.popPose();
        }
    }

    private void renderTabListAsCard(EventRender2D e, float a) {
        if (mc.getConnection() == null) return;

        var gg = e.getGuiGraphics();
        PoseStack pose = gg.pose();
        FontManager font = PMCP.Instance.getFontManager();

        // 在线玩家排序
        List<net.minecraft.client.multiplayer.PlayerInfo> players =
                new ArrayList<>(mc.getConnection().getOnlinePlayers());
        players.sort((p1, p2) -> {
            String t1 = p1.getTeam() != null ? p1.getTeam().getName() : "";
            String t2 = p2.getTeam() != null ? p2.getTeam().getName() : "";
            int c = t1.compareToIgnoreCase(t2);
            if (c != 0) return c;
            c = Integer.compare(p1.getLatency(), p2.getLatency());
            if (c != 0) return c;
            return getDisplayName(p1).getString().compareToIgnoreCase(getDisplayName(p2).getString());
        });

        int sw = gg.guiWidth();
        int sh = gg.guiHeight();

        final int PADDING = 10;
        final int TITLE_H = 20;
        final int ROW_H = 14;
        final int COL_PAD = 12;
        final int GAP_COL = 10;
        final int MAX_ROWS = Math.min(20, (sh - 120) / ROW_H);
        final int MIN_COL_W = 110;
        final int AVA = 12;
        final int AVA_GAP = 6;

        int n = players.size();
        if (n == 0) {
            drawCard(pose, sw, sh, PADDING, TITLE_H, 140, ROW_H + TITLE_H + PADDING * 2, a,
                    () -> font.font16.drawString(pose, "Players：0", sw / 2f - 40, sh / 2f - 6, new Color(230, 230, 230, (int) (230 * a)).getRGB()));
            return;
        }

        int sample = Math.min(n, 40);
        int maxNameW = 0;
        for (int i = 0; i < sample; i++) {
            maxNameW = Math.max(maxNameW, (int) font.font16.getComponentWidth(getDisplayName(players.get(i))));
        }
        int pingW = (int) font.font16.getStringWidth("999 ms");
        int colContentW = Math.max(MIN_COL_W, AVA + AVA_GAP + maxNameW + 8 + pingW);
        int colW = colContentW + COL_PAD * 2;

        int rows = Math.min(MAX_ROWS, n);
        int cols = (int) Math.ceil(n / (double) rows);
        int maxColsByWidth = Math.max(1, (sw - 120) / (colW + GAP_COL));
        cols = Math.min(cols, maxColsByWidth);
        rows = (int) Math.ceil(n / (double) cols);

        String serverName = "->" + ChatFormatting.BOLD + getServerDisplayName() + ChatFormatting.WHITE + "<-";

        int containerW = cols * colW + (cols - 1) * GAP_COL + PADDING * 2;
        int containerH = TITLE_H + rows * ROW_H + PADDING * 2;

        int cx = (sw - containerW) / 2;
        int cy = 10;

        Color glass = new Color(HUDSetting.Instance.getColor().getRed(), HUDSetting.Instance.getColor().getGreen(), HUDSetting.Instance.getColor().getBlue(), (int) (HUDSetting.Instance.getColor().getAlpha() * a));
        RenderUtils.drawRoundedRect(pose, cx, cy, containerW, containerH, 6, glass);

        float centerX = cx + containerW / 2f;

        int nameW;

        int serverColor = new Color(255, 255, 255, (int) (210 * a)).getRGB();

        nameW = (int) font.font16.getStringWidth(serverName);

        font.font16.drawString(pose, serverName, (int) (centerX - nameW / 2f), cy + 8, serverColor);

        int startX = cx + PADDING;
        int startY = cy + PADDING + TITLE_H;

        RenderUtils.drawRoundedRect(pose, cx + 2, cy + 6 + font.big18.getHeight() + font.font16.getHeight() + 3, containerW - 4, 2, 1, new Color(255,255,255, (int) (230 * a)));

        int index = 0;
        for (int c = 0; c < cols; c++) {
            int colX = startX + c * (colW + GAP_COL);
            for (int r = 0; r < rows && index < n; r++, index++) {
                int rowY = startY + r * ROW_H;

                var info = players.get(index);
                int ping = info.getLatency();

                int headX = colX + COL_PAD;
                int headY = rowY + (ROW_H - AVA) / 2;

                if (info.ircUser != null) {
                    String ircTag = "IRC";
                    int tagW = (int) font.profont18.getStringWidth(ircTag);
                    int tagH = (int) font.profont18.getHeight();

                    RenderUtils.drawRoundedRect(pose, (headX - tagW - 4) - 2, (headY + (AVA - font.profont18.getHeight()) / 2f) - 2, tagW + 4, tagH + 3, 2, new Color(140, 140, 140, (int) (230 * a)));
                    font.profont18.drawString(pose, ircTag, (headX - tagW - 4), (headY + (AVA - font.profont18.getHeight()) / 2f), new Color(100, 200, 255, (int) (230 * a)).getRGB());
                }

                int nameX = headX + AVA + AVA_GAP;
                int nameY = rowY + (ROW_H - font.font18.getHeight()) / 2 + 1;
                font.font16.drawComponent(pose, getDisplayName(info), nameX, nameY, new Color(255,255,255, (int) (255 * a)).getRGB());

                String pstr = ping + " ms";
                int pstrW = (int) font.yuanshen16.getStringWidth(pstr);
                int pingTextX = colX + colW - COL_PAD - pstrW;
                font.yuanshen16.drawString(pose, pstr, pingTextX, nameY, getPingColor(ping, (int)(230 * a)));

                if (a > 0.7) {
                    drawPlayerHead(gg, info.getSkinLocation(), headX, headY, AVA);
                    if (!VIPUtils.replaceVIP(getNameForDisplay(info).getString()).isEmpty()) {
                        drawVIP(gg, new ResourceLocation("pmcp/vip/" + VIPUtils.replaceVIP(getNameForDisplay(info).getString()) + ".png"), headX + (AVA + 5), headY - 1, AVA + 10, AVA);
                    }
                }
            }
        }
    }

    private void drawCard(PoseStack pose, int sw, int sh, int padding, int titleH, int w, int h, float a, Runnable content) {
        int cx = (sw - w) / 2;
        int cy = (sh - h) / 2;
        Color glass = new Color(30, 30, 30, (int)(80 * a));
        RenderUtils.drawRoundedRect(pose, cx, cy, w, h, 6, HUDSetting.Instance.getColor());
        RenderUtils.drawRoundedRect(pose, cx, cy, w, h, 6, glass);
        content.run();
    }

    private String getUserName() {
        return PMCP.userCategory == UserCategory.FREE ? mc.getUser().getName() : PMCP.USERNAME;
    }

    private boolean isIrc(net.minecraft.client.multiplayer.PlayerInfo pi) {
        if (pi == null) return false;
        if (mc.player != null && pi.getProfile() == mc.player.getGameProfile()) {
            return mc.player.ircUser != null;
        }
        return pi.ircUser != null;
    }

    private String getIrcRank(net.minecraft.client.multiplayer.PlayerInfo pi) {
        if (mc.player != null && pi.getProfile() == mc.player.getGameProfile()) {
            return mc.player.ircUser != null ? String.valueOf(mc.player.ircUser.getRank()) : "";
        }
        return (pi.ircUser != null) ? String.valueOf(pi.ircUser.getRank()) : "";
    }

    public String getServerDisplayName() {
        if (mc.level != null && mc.isLocalServer()) {
            return "Singleplayer";
        }

        if (header != null) {
            String full = stripFormatting(header.getString());
            int newlineIndex = full.indexOf('\n');
            if (newlineIndex >= 0) {
                return full.substring(0, newlineIndex);
            }
            return full;
        }

        return "Minecraft Server";
    }

    private Component getDisplayName(PlayerInfo pi) {
        if (isIrc(pi)) {
            String rank = getIrcRank(pi);
            String uname = (mc.player != null && pi.getProfile() == mc.player.getGameProfile() && mc.player.ircUser != null)
                    ? mc.player.ircUser.getUsername()
                    : (pi.ircUser != null ? pi.ircUser.getUsername() : pi.getProfile().getName());
            return Component.literal(rank.isEmpty() ? uname : (ChatFormatting.WHITE + "[" + rank + ChatFormatting.WHITE + "] " + ChatFormatting.AQUA + uname));
        }
        return getNameForDisplay(pi);
    }

    private int getPingColor(int ping, int alpha) {
        if (ping < 0)   return new Color(180, 180, 180, alpha).getRGB();
        if (ping < 75)  return new Color(120, 220, 120, alpha).getRGB();
        if (ping < 150) return new Color(230, 210, 110, alpha).getRGB();
        if (ping < 225) return new Color(255, 165,  90, alpha).getRGB();
        return new Color(220,  80,  80, alpha).getRGB();
    }

    private String stripFormatting(String s) {
        if (s == null) return "";
        return s.replaceAll("§[0-9A-FK-ORa-fk-or]", "");
    }

    private int teamColorOrDefault(PlayerInfo info, int alpha) {
        if (info.getTeam() != null) {
            Integer rgb = info.getTeam().getColor().getColor();
            if (rgb != null) {
                int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                return new Color(r, g, b, alpha).getRGB();
            }
        }
        return new Color(230, 230, 230, alpha).getRGB();
    }

    private void drawPlayerHead(GuiGraphics gg, ResourceLocation skin, int x, int y, int size) {
        if (skin == null) return;
        gg.blit(skin, x, y, size, size, 8, 8, 8, 8, 64, 64);   // 脸部
        gg.blit(skin, x, y, size, size, 40, 8, 8, 8, 64, 64);  // 帽层
    }

    private void drawVIP(GuiGraphics gg, ResourceLocation skin, int x, int y, int w, int h) {
        gg.blit(skin, x, y, w, h, 0, 0, w, h, w, h);
    }

    private Component getNameForDisplay(PlayerInfo p_94550_) {
        return p_94550_.getTabListDisplayName() != null ? this.decorateName(p_94550_, p_94550_.getTabListDisplayName().copy()) : this.decorateName(p_94550_, PlayerTeam.formatNameForTeam(p_94550_.getTeam(), Component.literal(p_94550_.getProfile().getName())));
    }

    private Component decorateName(PlayerInfo pPlayerInfo, MutableComponent pName) {
        return pPlayerInfo.getGameMode() == GameType.SPECTATOR ? pName.withStyle(ChatFormatting.ITALIC) : pName;
    }

    enum HUDCategory { ITEM_USE, MOVEMENT, PACKET, BLOCK, PLACEHOLDER, FLY }

    static class HUDInfo {
        public String name;
        public final Supplier<String> nameSupplier;
        public final Supplier<Boolean> isVisible;
        public final Supplier<Double> progressSupplier;
        public final HUDCategory category;
        public double displayProgress;

        public HUDInfo(String name, Supplier<Double> progressSupplier, Supplier<Boolean> isVisible, HUDCategory category) {
            this(() -> name, isVisible, progressSupplier, category);
        }
        public HUDInfo(Supplier<String> nameSupplier, Supplier<Boolean> isVisible, Supplier<Double> progressSupplier, HUDCategory category) {
            this.nameSupplier = nameSupplier;
            this.isVisible = isVisible;
            this.progressSupplier = progressSupplier;
            this.category = category;
        }
        public String getId() { return category.name() + ":" + getName(); }
        public String getName() { return nameSupplier != null ? nameSupplier.get() : name; }
    }

    static class HUDRenderEntry {
        public final HUDInfo info;
        public float yOffset = -30f;
        public float alpha = 0f;
        public boolean active = true;

        public HUDRenderEntry(HUDInfo info) { this.info = info; }
        public String getId() { return info.getId(); }
    }

    public double getBPS() {
        if (mc.player == null) return 0;
        double dx = mc.player.getX() - mc.player.xo;
        double dz = mc.player.getZ() - mc.player.zo;
        return Math.sqrt(dx * dx + dz * dz) * 20.0;
    }
}
