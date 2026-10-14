package org.hariyama.horrormode;

import net.minecraftforge.common.ForgeConfigSpec;

public final class HorrorConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.IntValue DARKNESS_SECONDS;
    public static final ForgeConfigSpec.IntValue[] MIN_DELAYS = new ForgeConfigSpec.IntValue[5];
    public static final ForgeConfigSpec.IntValue[] MAX_DELAYS = new ForgeConfigSpec.IntValue[5];
    public static final ForgeConfigSpec.DoubleValue DAMAGE_PERCENT, MOOD_SPEED;
    static {
        var b = new ForgeConfigSpec.Builder();
        ENABLED = b.comment("Enable automatic events for survival/adventure players.").define("automaticEvents", true);
        String[] names = {"darkness", "message", "attacker", "watcher", "sound"};
        int[] minimums = {50, 40, 180, 50, 90};
        int[] maximums = {90, 80, 300, 90, 180};
        b.push("events");
        for (int i = 0; i < names.length; i++) {
            b.push(names[i]);
            MIN_DELAYS[i] = b.defineInRange("minimumSeconds", minimums[i], 3, 3600);
            MAX_DELAYS[i] = b.defineInRange("maximumSeconds", maximums[i], 3, 3600);
            b.pop();
        }
        b.pop();
        DARKNESS_SECONDS = b.defineInRange("darknessSeconds", 8, 1, 60);
        DAMAGE_PERCENT = b.comment("Percentage of CURRENT health removed; always leaves at least 1 HP.")
                .defineInRange("npcDamagePercent", 70.0, 0.0, 100.0);
        MOOD_SPEED = b.comment("Multiplier for light-independent mood accumulation.")
                .defineInRange("moodSpeedMultiplier", 4.0, 0.1, 100.0);
        SPEC = b.build();
    }
    public static float remainingHealth(float health, double percent) {
        return Math.min(health, Math.max(1.0F, (float) (health * (1.0 - percent / 100.0))));
    }
}
