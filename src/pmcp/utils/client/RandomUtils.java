package pmcp.utils.client;

public class RandomUtils {



    public static int next(int startInclusive, int endInclusive) {
        if (startInclusive == endInclusive) {
            return startInclusive;
        }
        if (endInclusive < startInclusive) {
            throw new IllegalArgumentException("结束值必须大于或等于开始值");
        }
        return startInclusive + (int) (Math.random() * (endInclusive - startInclusive + 1));
    }

    public static double next(double startInclusive, double endInclusive) {
        if (startInclusive == endInclusive) {
            return startInclusive;
        }
        if (endInclusive < startInclusive) {
            throw new IllegalArgumentException("结束值必须大于或等于开始值");
        }
        return startInclusive + (double) (Math.random() * (endInclusive - startInclusive + 1));
    }

    public static float next(float startInclusive, float endInclusive) {
        if (startInclusive == endInclusive) {
            return startInclusive;
        }
        if (endInclusive < startInclusive) {
            throw new IllegalArgumentException("结束值必须大于或等于开始值");
        }
        return startInclusive + (float) (Math.random() * (endInclusive - startInclusive + 1));
    }
}
