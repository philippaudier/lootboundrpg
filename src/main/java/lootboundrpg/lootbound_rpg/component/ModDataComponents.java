package lootboundrpg.lootbound_rpg.component;

import com.mojang.serialization.Codec;
import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

/**
 * Registers custom DataComponents for Lootbound RPG.
 *
 * MC 26.2 uses DataComponents instead of NBT tags for item data.
 * This is the modern, type-safe way to store custom data on items.
 */
public class ModDataComponents {

    /**
     * Equipment upgrade level component.
     * Stores the enhancement level from 0 to 10.
     *
     * Used on swords and pickaxes to track upgrade progression.
     * Level affects damage/mining speed bonuses.
     */
    public static final DataComponentType<Integer> EQUIPMENT_LEVEL = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            LootboundRpgMod.id("equipment_level"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, 10))  // Validates 0-10 range
                    .networkSynchronized(ByteBufCodecs.VAR_INT)  // Sync to client
                    .build()
    );

    /**
     * Equipment grade/rarity component.
     * Stores the grade as an integer (0=COMMON to 4=LEGENDARY).
     *
     * Grade provides a multiplier to upgrade bonuses:
     * COMMON(0)=1.0x, UNCOMMON(1)=1.05x, RARE(2)=1.10x, EPIC(3)=1.20x, LEGENDARY(4)=1.35x
     */
    public static final DataComponentType<Integer> EQUIPMENT_GRADE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            LootboundRpgMod.id("equipment_grade"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, 4))  // Validates 0-4 range (5 grades)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)  // Sync to client
                    .build()
    );

    /**
     * Called from mod initializer to trigger static initialization.
     */
    public static void register() {
        LootboundRpgMod.LOGGER.info("Registering Lootbound RPG data components...");
    }
}
