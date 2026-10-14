package org.hariyama.horrormode;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = HorrorMode.ID)
public final class HorrorEvents {
    private static final String[] CHAT_FALLBACKS = {
            "Ты здесь не один.",
            "Я вижу тебя.",
            "Не оборачивайся.",
            "Ты слышишь шаги?",
            "Здесь нет выхода.",
            "Я уже рядом."
    };
    private static final int[] OBFUSCATED_MESSAGE_LENGTHS = {12, 24, 40, 64};
    private static final Map<UUID, long[]> TIMERS = new HashMap<>();
    public static boolean eligible(ServerPlayer p) { return p.isAlive() && !p.isCreative() && !p.isSpectator(); }
    private static int delay(ServerPlayer p, int action) {
        int min = Math.min(HorrorConfig.MIN_DELAYS[action].get(), HorrorConfig.MAX_DELAYS[action].get());
        int max = Math.max(HorrorConfig.MIN_DELAYS[action].get(), HorrorConfig.MAX_DELAYS[action].get());
        return (min + p.getRandom().nextInt(max - min + 1)) * 20;
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer p)) return;
        if (!HorrorConfig.ENABLED.get() || !eligible(p)) { TIMERS.remove(p.getUUID()); return; }
        long now = p.serverLevel().getGameTime();
        long[] timers = TIMERS.computeIfAbsent(p.getUUID(), id -> {
            long[] times = new long[HorrorConfig.MIN_DELAYS.length];
            for (int i = 0; i < times.length; i++) times[i] = now + delay(p, i);
            return times;
        });
        for (int action = 0; action < timers.length; action++) {
            if (now >= timers[action]) {
                trigger(p, action == 4 ? 5 : action); // Mood (4) remains command-only.
                timers[action] = now + delay(p, action);
            }
        }
    }
    static boolean trigger(ServerPlayer p, int action) {
        switch (action) {
            case 0 -> p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, HorrorConfig.DARKNESS_SECONDS.get() * 20, 0));
            case 1 -> p.sendSystemMessage(chatMessage(p));
            case 2 -> { return spawn(p); }
            case 3 -> { return spawnWatcher(p); }
            case 4 -> HorrorNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new HorrorNetwork.Mood());
            case 5 -> p.playNotifySound(HorrorMode.RANDOM_AMBIENT.get(), net.minecraft.sounds.SoundSource.AMBIENT, 1.0F, 1.0F);
            default -> { return false; }
        }
        return true;
    }
    private static Component chatMessage(ServerPlayer player) {
        int index = player.getRandom().nextInt(CHAT_FALLBACKS.length + OBFUSCATED_MESSAGE_LENGTHS.length);
        if (index >= CHAT_FALLBACKS.length) {
            int length = OBFUSCATED_MESSAGE_LENGTHS[index - CHAT_FALLBACKS.length];
            return Component.literal("|".repeat(length))
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.OBFUSCATED);
        }
        return Component.empty().withStyle(ChatFormatting.DARK_RED)
                .append(Component.literal("||| ").withStyle(ChatFormatting.OBFUSCATED))
                .append(Component.translatableWithFallback("message.horrormode.chat" + index, CHAT_FALLBACKS[index]))
                .append(Component.literal(" ||||||").withStyle(ChatFormatting.OBFUSCATED));
    }
    private static boolean spawn(ServerPlayer p) {
        var level = p.serverLevel();
        if (!level.getEntitiesOfClass(Visitor.class, p.getBoundingBox().inflate(64), npc -> npc.isHunting(p)).isEmpty()) return false;
        var npc = HorrorMode.VISITOR.get().create(level);
        if (npc == null) return false;
        for (int attempt = 0; attempt < 32; attempt++) {
            double angle = p.getRandom().nextDouble() * Math.PI * 2;
            double radius = 5 + p.getRandom().nextInt(5);
            BlockPos center = BlockPos.containing(p.getX() + Math.cos(angle) * radius, p.getY(), p.getZ() + Math.sin(angle) * radius);
            for (int dy = 3; dy >= -3; dy--) {
                BlockPos pos = center.offset(0, dy, 0);
                if (!level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos)
                        || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP)) continue;
                npc.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                if (!level.noCollision(npc) || level.containsAnyLiquid(npc.getBoundingBox())) continue;
                npc.hunt(p);
                if (level.addFreshEntity(npc)) {
                    level.playSound(null, npc.getX(), npc.getY(), npc.getZ(),
                            HorrorMode.ATTACKER_SOUND.get(),
                            net.minecraft.sounds.SoundSource.HOSTILE, 2.0F, 1.0F);
                    return true;
                }
                return false;
            }
        }
        return false;
    }
    private static boolean spawnWatcher(ServerPlayer p) {
        var level = p.serverLevel();
        if (!level.getEntitiesOfClass(Watcher.class, p.getBoundingBox().inflate(64), npc -> npc.isWatching(p)).isEmpty()) return false;
        var npc = HorrorMode.WATCHER.get().create(level);
        if (npc == null) return false;
        double facing = Math.atan2(p.getLookAngle().z, p.getLookAngle().x);
        for (int attempt = 0; attempt < 48; attempt++) {
            double angle = facing + (p.getRandom().nextDouble() - 0.5) * Math.PI;
            double radius = 12 + p.getRandom().nextInt(13);
            BlockPos center = BlockPos.containing(p.getX() + Math.cos(angle) * radius, p.getY(), p.getZ() + Math.sin(angle) * radius);
            for (int dy = 6; dy >= -6; dy--) {
                BlockPos pos = center.offset(0, dy, 0);
                if (!level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos)
                        || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP)) continue;
                npc.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                if (!level.noCollision(npc) || level.containsAnyLiquid(npc.getBoundingBox())
                        || !npc.hasLineOfSight(p)
                        || level.players().stream().anyMatch(other -> !other.isSpectator() && npc.distanceToSqr(other) <= 16)) continue;
                npc.watch(p);
                npc.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, p.getEyePosition());
                npc.setYHeadRot(npc.getYRot());
                npc.setYBodyRot(npc.getYRot());
                return level.addFreshEntity(npc);
            }
        }
        return false;
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e) { clear(e.getEntity().getUUID()); }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e) { clear(e.getEntity().getUUID()); }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e) { clear(e.getEntity().getUUID()); }
    private static void clear(UUID id) { TIMERS.remove(id); }
    @SubscribeEvent public static void stop(ServerStoppedEvent e) { TIMERS.clear(); }
}
