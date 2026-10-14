package org.hariyama.horrormode;

import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class Watcher extends PathfinderMob {
    private UUID watchedPlayer;
    public Watcher(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setNoAi(true);
        setSilent(true);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 1)
                .add(Attributes.MOVEMENT_SPEED, 0).add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }
    public void watch(Player player) { watchedPlayer = player.getUUID(); }
    public boolean isWatching(Player player) { return player.getUUID().equals(watchedPlayer); }
    @Override public boolean isPushable() { return false; }
    @Override public boolean doHurtTarget(Entity target) { return false; }
    @Override public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide) discard();
        return false;
    }
    @Override public void tick() {
        if (level() instanceof ServerLevel server) {
            Player player = watchedPlayer == null ? null : server.getPlayerByUUID(watchedPlayer);
            if (player == null || !player.isAlive() || player.isSpectator() || tickCount >= 400
                    || distanceToSqr(player) > 4096
                    || server.players().stream().anyMatch(p -> p.isAlive() && !p.isSpectator() && distanceToSqr(p) <= 16)) {
                discard();
                return;
            }
            lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition());
            setYHeadRot(getYRot());
            setYBodyRot(getYRot());
        }
        super.tick();
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (watchedPlayer != null) tag.putUUID("WatchedPlayer", watchedPlayer);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("WatchedPlayer")) watchedPlayer = tag.getUUID("WatchedPlayer");
        tickCount = 400;
    }
}
