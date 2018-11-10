package server.core.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {
    public static final int BASE_CHANCE = 10000;

    public static int random(int max) {
        return ThreadLocalRandom.current().nextInt(max);
    }

    public static int random(int min, int max) {
        return min + random(max - min);
    }

    public static int random(int[] table) {
        int index = random(table.length);
        return table[index];
    }

    public static boolean hit(int chance, int total){
        return chance < random(total);
    }

    public static boolean hit(int chance){
        return hit(chance, BASE_CHANCE);
    }

    public static boolean miss(int chance, int total){
        return chance > random(total);
    }

    public static boolean miss(int chance){
        return miss(chance, BASE_CHANCE);
    }
}
