package org.hariyama.horrormode;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;

public final class HorrorNetwork {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(HorrorMode.ID, "main"), () -> "3", "3"::equals, "3"::equals);
    public record Mood() { }
    public static void register() {
        CHANNEL.messageBuilder(Mood.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((msg, buf) -> { })
                .decoder(buf -> new Mood())
                .consumerMainThread((msg, context) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> org.hariyama.horrormode.client.AlwaysGrowingMood.triggerSound())).add();
    }
}
