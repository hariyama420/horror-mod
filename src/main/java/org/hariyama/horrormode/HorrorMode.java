package org.hariyama.horrormode;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.*;

@Mod(HorrorMode.ID)
public final class HorrorMode {
    public static final String ID = "horrormode";
    private static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ID);
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> RANDOM_AMBIENT = SOUNDS.register("random_ambient",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(ID, "random_ambient")));
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> ATTACKER_SOUND = SOUNDS.register("attacker",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(ID, "attacker")));
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ID);
    public static final RegistryObject<EntityType<Visitor>> VISITOR = ENTITIES.register("visitor",
            () -> EntityType.Builder.of(Visitor::new, MobCategory.MONSTER).sized(0.6F, 1.95F)
                    .clientTrackingRange(8).build(ID + ":visitor"));
    public static final RegistryObject<EntityType<Watcher>> WATCHER = ENTITIES.register("watcher",
            () -> EntityType.Builder.of(Watcher::new, MobCategory.CREATURE).sized(0.6F, 1.95F)
                    .clientTrackingRange(8).build(ID + ":watcher"));
    public HorrorMode() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITIES.register(bus);
        SOUNDS.register(bus);
        bus.addListener(this::attributes);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, HorrorConfig.SPEC);
        HorrorNetwork.register();
    }
    private void attributes(EntityAttributeCreationEvent event) {
        event.put(VISITOR.get(), Visitor.createAttributes().build());
        event.put(WATCHER.get(), Watcher.createAttributes().build());
    }
}
