package org.hariyama.horrormode.client;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.hariyama.horrormode.HorrorMode;
import net.minecraft.world.entity.Mob;

public final class VisitorRenderer<T extends Mob> extends MobRenderer<T, PlayerModel<T>> {
    private static final ResourceLocation SKIN = new ResourceLocation(HorrorMode.ID, "textures/entity/visitor.png");
    public VisitorRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }
    @Override public ResourceLocation getTextureLocation(T visitor) { return SKIN; }
}
