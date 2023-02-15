package br.com.tiozinnub.civilization.utils.helper;

import net.minecraft.util.math.random.Random;

import java.util.EnumSet;
import java.util.List;

public class RandomHelper {
    public static <T> T pickOne(Random random, List<Weighted<T>> list) {
        var total = list.stream().mapToInt(Weighted::weight).sum();
        var randomInt = random.nextInt(total);

        for (Weighted<T> weighted : list) {
            if (randomInt < weighted.weight) {
                return weighted.item;
            }
            randomInt -= weighted.weight;
        }

        // uhh, this should never happen
        return null;
    }

    public static <T> T pickOne(Random random, T item1, int weight1, T item2, int weight2) {
        return pickOne(random, List.of(new Weighted<>(item1, weight1), new Weighted<>(item2, weight2)));
    }

    public static <T> T pickOne(Random random, T item1, T item2) {
        return flipACoin(random) ? item1 : item2;
    }

    public static <T extends Enum<T>> T pickOne(Random random, EnumSet<T> items) {
        return pickOne(random, items.stream().map(item -> new Weighted<>(item, 1)).toList());
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

    public static boolean flipACoin(Random random) {
        return random.nextBoolean();
    }

    public record Weighted<T>(T item, int weight) {
        public static <T> Weighted<T> of(T item, int weight) {
            return new Weighted<>(item, weight);
        }
    }
}
