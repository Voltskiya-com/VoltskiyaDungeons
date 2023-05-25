package com.voltskiya.structure.lootchest.entity;

public interface IChestGroupConfig {

    private static int minToTicks(double minutes) {
        return (int) (minutes * 20 * 60);
    }

    double verifyFastRestockMin();

    double verifySlowRestockMin();

    int verifyPlayerCountAtFastRestock();

    default void validate() {
        if (verifySlowRestockMin() < verifyFastRestockMin())
            throw new IllegalStateException("LootChestConfig: defaultSlowRestockMin is < defaultFastRestockMin");
        if (verifyPlayerCountAtFastRestock() <= 0)
            throw new IllegalStateException("LootChestConfig: playerCountAtFastRestock is <= 0");
    }


    default double normalizeTimePassedToPerc(int playerCount, int timePassedTicks) {
        double multiplier = 1 - Math.min(1, playerCount / (double) this.verifyPlayerCountAtFastRestock());
        double range = verifySlowRestockMin() - verifyFastRestockMin();
        double realRestockTime = verifyFastRestockMin() + range * multiplier;
        return timePassedTicks / (double) minToTicks(realRestockTime) / normalizedRestockTime();
    }

    private double normalizedRestockTime() {
        return 1; // some arbitrary number to normalize to
    }

    default boolean shouldRestock(double timePassed) {
        return normalizedRestockTime() <= timePassed;
    }
}
