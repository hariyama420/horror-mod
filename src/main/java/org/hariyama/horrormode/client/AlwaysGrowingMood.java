package org.hariyama.horrormode.client;

import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AmbientSoundHandler;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public final class AlwaysGrowingMood extends BiomeAmbientSoundsHandler {
    private static final Field HANDLERS = ObfuscationReflectionHelper.findField(LocalPlayer.class, "f_108593_");
    private static final Field VANILLA_MOOD = ObfuscationReflectionHelper.findField(BiomeAmbientSoundsHandler.class, "f_119636_");
    private final LocalPlayer player;
    private double progress;

    private AlwaysGrowingMood(LocalPlayer player) {
        super(player, Minecraft.getInstance().getSoundManager(), player.level().getBiomeManager());
        this.player = player;
    }

    @SuppressWarnings("unchecked")
    public static void install(LocalPlayer player) {
        try {
            var handlers = (List<AmbientSoundHandler>) HANDLERS.get(player);
            for (int i = 0; i < handlers.size(); i++) {
                if (handlers.get(i).getClass() == BiomeAmbientSoundsHandler.class) {
                    handlers.set(i, new AlwaysGrowingMood(player));
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot install Horror Mode ambient handler", e);
        }
    }

    public static void triggerSound() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        install(player);
        try {
            for (Object handler : (List<?>) HANDLERS.get(player)) {
                if (handler instanceof AlwaysGrowingMood mood) mood.progress = 1.0;
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot trigger mood sound", e);
        }
    }

    @Override public float getMoodiness() { return (float) progress; }

    @Override public void tick() {
        try {
            VANILLA_MOOD.setFloat(this, -1.0F);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot reset vanilla mood counter", e);
        }
        super.tick();
        var settings = player.level().getBiomeManager()
                .getNoiseBiomeAtPosition(player.getX(), player.getY(), player.getZ())
                .value().getAmbientMood().orElse(AmbientMoodSettings.LEGACY_CAVE_SETTINGS);
        progress += org.hariyama.horrormode.HorrorConfig.MOOD_SPEED.get() / Math.max(1, settings.getTickDelay());
        if (progress < 1.0 - 1.0E-9) return;
        progress = 0.0;
        var random = player.getRandom();
        double angle = random.nextDouble() * Math.PI * 2;
        double distance = settings.getBlockSearchExtent() + settings.getSoundPositionOffset();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forAmbientMood(
                settings.getSoundEvent().value(), random,
                player.getX() + Math.cos(angle) * distance,
                player.getEyeY() + random.nextDouble() * 4 - 2,
                player.getZ() + Math.sin(angle) * distance));
    }
}
