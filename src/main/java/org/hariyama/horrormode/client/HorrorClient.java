package org.hariyama.horrormode.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.hariyama.horrormode.*;

@Mod.EventBusSubscriber(modid = HorrorMode.ID, value = Dist.CLIENT)
public final class HorrorClient {
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        var player = Minecraft.getInstance().player;
        if (player != null) AlwaysGrowingMood.install(player);
    }
    @Mod.EventBusSubscriber(modid = HorrorMode.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(HorrorMode.VISITOR.get(), VisitorRenderer<Visitor>::new);
            event.registerEntityRenderer(HorrorMode.WATCHER.get(), VisitorRenderer<Watcher>::new);
        }
    }
}
