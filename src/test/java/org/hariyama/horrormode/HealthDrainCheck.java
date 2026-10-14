package org.hariyama.horrormode;

/** Standalone regression check; no running game required. */
public final class HealthDrainCheck {
    public static void main(String[] args) {
        check(15, 25, 11.25F);
        check(15, 70, 4.5F);
        check(20, 70, 6);
        check(1, 70, 1);
        check(0.5F, 70, 0.5F);
        check(1, 25, 1);
        check(0.5F, 25, 0.5F);
        check(20, 0, 20);
        check(20, 100, 1);
        check(0, 25, 0);
        for (int i = 1; i <= 10000; i++) {
            float health = i / 100F;
            for (int percent = 0; percent <= 100; percent++) {
                float result = HorrorConfig.remainingHealth(health, percent);
                if (result <= 0 || result > health) throw new AssertionError("Unsafe health: " + result);
            }
        }
        System.out.println("Health drain checks passed");
    }
    private static void check(float health, double percent, float expected) {
        float actual = HorrorConfig.remainingHealth(health, percent);
        if (actual != expected) throw new AssertionError(actual + " != " + expected);
    }
}
