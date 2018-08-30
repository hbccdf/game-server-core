package server.core.util;

import java.util.Random;

public class RandomUtil {
    public static final int BASE_CHANCE = 10000;
    private static final Random random = new Random();

    public static final int random(int max) {
        return random.nextInt(max);
    }

    public static final int random(int min, int max) {
        return min + random(max);
    }

    public static int random(int[] table) {
        int index = random(table.length);
        return table[index];
    }

    public static boolean hit(int chance, int total){
        return chance < RandomUtil.random(total);
    }

    public static boolean hit(int chance){
        return hit(chance, BASE_CHANCE);
    }

    public static boolean miss(int chance, int total){
        return chance > RandomUtil.random(total);
    }

    public static boolean miss(int chance){
        return miss(chance, BASE_CHANCE);
    }
}
