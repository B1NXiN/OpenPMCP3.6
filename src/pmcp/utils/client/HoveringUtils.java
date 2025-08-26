package pmcp.utils.client;

public class HoveringUtils {

    public static boolean isHovering(int x, int y, int width, int height,double mouseX, double mouseY) {
        float startX = x;
        float startY = y;
        float w = width;
        float h = height;

        if (width < 0) {
            startX += width;
            w = Math.abs(w);
        }

        if (height < 0) {
            startY += height;
            h = Math.abs(h);
        }

        return mouseX >= startX && mouseX <= startX + w && mouseY >= startY && mouseY <= startY + h;
    }
    public static boolean isHovering(double x, double y, double width, double height,double mouseX, double mouseY) {
        float startX = (float) x;
        float startY = (float) y;
        float w = (float) width;
        float h = (float) height;

        if (width < 0) {
            startX += width;
            w = Math.abs(w);
        }

        if (height < 0) {
            startY += height;
            h = Math.abs(h);
        }

        return mouseX >= startX && mouseX <= startX + w && mouseY >= startY && mouseY <= startY + h;
    }
}
