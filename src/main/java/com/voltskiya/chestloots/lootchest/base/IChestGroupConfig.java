package com.voltskiya.chestloots.lootchest.base;

public interface IChestGroupConfig {

    private static int minToTicks(double minutes) {
        return (int) (minutes * 20 * 60);
    }

    double getFastRestockMin();

    double getSlowRestockMin();

    int getPlayerCountAtFastRestock();

    default void validate() {
        if (getSlowRestockMin() < getFastRestockMin())
            throw new IllegalStateException("LootChestConfig: defaultSlowRestockMin is < defaultFastRestockMin");
        if (getPlayerCountAtFastRestock() <= 0)
            throw new IllegalStateException("LootChestConfig: playerCountAtFastRestock is <= 0");
    }


    default double normalizeTimePassedToPerc(int playerCount, int timePassedTicks) {
        double multiplier = 1 - Math.min(1, playerCount / (double) this.getPlayerCountAtFastRestock());
        double range = getSlowRestockMin() - getFastRestockMin();
        double realRestockTime = getFastRestockMin() + range * multiplier;
        return timePassedTicks / (double) minToTicks(realRestockTime) * getNormalizedRestockTime();
    }

    default double getNormalizedRestockTime() {
        return 1; // some arbitrary number to normalize to
    }

}
