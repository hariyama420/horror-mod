package org.hariyama.horrormode;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class Visitor extends Monster {
    private UUID victim;
    public Visitor(EntityType<? extends Monster> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.55).add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }
    public boolean isHunting(Player player) { return player.getUUID().equals(victim); }
    public void hunt(Player player) { victim = player.getUUID(); setTarget(player); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25, false));
    }
    @Override public boolean canPickUpLoot() { return false; }
    @Override public boolean doHurtTarget(Entity target) {
        if (target instanceof Player player && player.getUUID().equals(victim)
                && player.isAlive() && !player.isCreative() && !player.isSpectator()) {
            player.setHealth(HorrorConfig.remainingHealth(player.getHealth(), HorrorConfig.DAMAGE_PERCENT.get()));
            player.hurtTime = 10;
            level().broadcastEntityEvent(player, (byte) 2);
            discard();
            return true;
        }
        return false;
    }
    @Override public void tick() {
        if (level() instanceof ServerLevel server) {
            var player = victim == null ? null : server.getPlayerByUUID(victim);
            if (tickCount > 600 || player == null || !player.isAlive() || player.isCreative()
                    || player.isSpectator() || distanceToSqr(player) > 4096) { discard(); return; }
            setTarget(player);
        }
        super.tick();
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (victim != null) tag.putUUID("Victim", victim);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Victim")) victim = tag.getUUID("Victim");
        tickCount = 601;
    }
}
