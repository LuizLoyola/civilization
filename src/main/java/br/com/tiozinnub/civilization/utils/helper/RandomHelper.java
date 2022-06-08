package br.com.tiozinnub.civilization.utils.helper;

import net.minecraft.util.math.random.Random;

public class RandomHelper {
    public static <T> T pickOneWeighted(Random random, T item1, int weight1, T item2, int weight2) {
        return random.nextInt(weight1 + weight2) < weight1 ? item1 : item2;
    }

    public static <T> T pickOne(Random random, T item1, T item2) {
        return pickOneWeighted(random, item1, 1, item2, 1);
    }

    public static int between(Random random, int startInclusive, int endExclusive) {
        int start = Math.min(endExclusive, startInclusive);
        int end = Math.max(startInclusive, endExclusive);
        return random.nextInt(end - start) + start;
    }

    public static float between(Random random, float startInclusive, float endExclusive) {
        float start = Math.min(endExclusive, startInclusive);
        float end = Math.max(startInclusive, endExclusive);
        return random.nextFloat() * (end - start) + start;
    }

    public static boolean tryChance(Random random, double chance) {
        return random.nextDouble() < chance;
    }
}
