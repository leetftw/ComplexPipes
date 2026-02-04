package com.leetftw.complexpipes.common;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue STACK_UPGRADE_MULTIPLIER = BUILDER
            .comment("Determines the multiplier for Stack Upgrades.")
            .defineInRange("stackUpgradeMultiplier", 2.0, 1, 256);
    public static final ModConfigSpec.IntValue MAX_STACK_UPGRADES = BUILDER
            .comment("Determines the maximum amount of Stack Upgrades that can be installed in a single pipe connection.")
            .defineInRange("maxStackUpgrades", 6, 0, 256);

    public static final ModConfigSpec.DoubleValue SPEED_UPGRADE_MULTIPLIER = BUILDER
            .comment("Determines the multiplier for Speed Upgrades.")
            .defineInRange("speedUpgradeMultiplier", 0.5, Double.MIN_VALUE, 1);
    public static final ModConfigSpec.IntValue MAX_SPEED_UPGRADES = BUILDER
            .comment("Determines the maximum amount of Speed Upgrades that can be installed in a single pipe connection.")
            .defineInRange("maxSpeedUpgrades", 4, 0, 256);

    static final ModConfigSpec SPEC = BUILDER.build();
}
