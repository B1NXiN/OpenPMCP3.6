package pmcp.ui.font;

import net.minecraft.client.gui.GuiGraphics;
import pmcp.PMCP;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class FontManager {

    public static Font defRegular;

    public TrueTypeFont font10;

    public TrueTypeFont font12;
    public TrueTypeFont font13;
    public TrueTypeFont font14;
    public TrueTypeFont font15;

    public TrueTypeFont font16;
    public TrueTypeFont font18;
    public TrueTypeFont font19;
    public TrueTypeFont font20;
    public TrueTypeFont font22;
    public TrueTypeFont font24;
    public TrueTypeFont font26;

    public TrueTypeFont font28;
    public TrueTypeFont font30;
    public TrueTypeFont font32;
    public TrueTypeFont font36;
    public TrueTypeFont font34;
    public TrueTypeFont font40;
    public TrueTypeFont font42;
    public TrueTypeFont font64;

    public TrueTypeFont icon18;
    public TrueTypeFont icon25;
    public TrueTypeFont icon30;
    public TrueTypeFont icon33;
    public TrueTypeFont icon45;

    public TrueTypeFont noit10;
    public TrueTypeFont noit12;
    public TrueTypeFont noit14;
    public TrueTypeFont noit16;
    public TrueTypeFont noit18;
    public TrueTypeFont noit20;
    public TrueTypeFont noit30;
    public TrueTypeFont noit40;
    public TrueTypeFont noit45;

    public TrueTypeFont profont10;
    public TrueTypeFont profont14;
    public TrueTypeFont profont16;
    public TrueTypeFont profont18;
    public TrueTypeFont profont20;

    public TrueTypeFont big18;

    public TrueTypeFont classic14;
    public TrueTypeFont classic16;
    public TrueTypeFont classic18;

    public TrueTypeFont classic24;

    public TrueTypeFont yuanshen16;
    public TrueTypeFont yuanshen18;

    public void init() {
        defRegular = new Font("微软雅黑", Font.PLAIN, 24);
        font10 = createFontRenderer(getFont(10, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font12 = createFontRenderer(getFont(12, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font13 = createFontRenderer(getFont(13, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font14 = createFontRenderer(getFont(14, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font15 = createFontRenderer(getFont(15, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font16 = createFontRenderer(getFont(16, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font18 = createFontRenderer(getFont(18, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font19 = createFontRenderer(getFont(19, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font20 = createFontRenderer(getFont(20, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font22 = createFontRenderer(getFont(22, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font24 = createFontRenderer(getFont(24, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font26 = createFontRenderer(getFont(26, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font28 = createFontRenderer(getFont(28, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font30 = createFontRenderer(getFont(30, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font32 = createFontRenderer(getFont(32, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font34 = createFontRenderer(getFont(34, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font36 = createFontRenderer(getFont(36, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font40 = createFontRenderer(getFont(20, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font42 = createFontRenderer(getFont(42, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));
        font64 = createFontRenderer(getFont(64, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\font.ttf")));

        icon18 = createFontRenderer(getFont(18, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\icons.ttf")));
        icon25 = createFontRenderer(getFont(25, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\icons.ttf")));
        icon30 = createFontRenderer(getFont(30, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\icons.ttf")));
        icon33 = createFontRenderer(getFont(33, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\icons.ttf")));
        icon45 = createFontRenderer(getFont(45, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\icons.ttf")));

        noit10 = createFontRenderer(getFont(10, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\icon-noti.ttf")));
        noit12 = createFontRenderer(getFont(12, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\icon-noti.ttf")));
        noit14 = createFontRenderer(getFont(14, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\icon-noti.ttf")));
        noit16 = createFontRenderer(getFont(16, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\icon-noti.ttf")));
        noit18 = createFontRenderer(getFont(18, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\icon-noti.ttf")));
        noit20 = createFontRenderer(getFont(20, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\icon-noti.ttf")));
        noit30 = createFontRenderer(getFont(30, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\icon-noti.ttf")));
        noit40 = createFontRenderer(getFont(40, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\icon-noti.ttf")));
        noit45 = createFontRenderer(getFont(45, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\icon-noti.ttf")));

        profont10 = createFontRenderer(getFont(10, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\profont.ttf")));
        profont14 = createFontRenderer(getFont(14, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\profont.ttf")));
        profont16 = createFontRenderer(getFont(16, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\profont.ttf")));
        profont18 = createFontRenderer(getFont(18, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\profont.ttf")));
        profont20 = createFontRenderer(getFont(20, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\profont.ttf")));

        big18 = createFontRenderer(getFont(18, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\big.ttf")));

        classic14 = createFontRenderer(getFont(14, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\classic.ttf")));
        classic16 = createFontRenderer(getFont(16, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\classic.ttf")));
        classic18 = createFontRenderer(getFont(18, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\classic.ttf")));
        classic24 = createFontRenderer(getFont(24, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\classic.ttf")));

        yuanshen16 = createFontRenderer(getFont(16, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\yuanshen.ttf")));
        yuanshen18 = createFontRenderer(getFont(18, new File(PMCP.Instance.getResourcesManager().font.getAbsolutePath() + "\\yuanshen.ttf")));
    }

    public static Font driveFont(Font font, int style, int size) {
        return font.deriveFont(style, size);
    }

    public Font getFont(int size, File file) {
        Font font;
        try (FileInputStream fis = new FileInputStream(file)) {
            font = Font.createFont(Font.TRUETYPE_FONT, fis).deriveFont(Font.PLAIN, (float) size);
        } catch (Exception ex) {
            ex.printStackTrace();
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
    }

    public TrueTypeFont createFontRenderer(Font font) {
        return new TrueTypeFont(font, true, false);
    }
}
