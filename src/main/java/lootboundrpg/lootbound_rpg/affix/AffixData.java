package lootboundrpg.lootbound_rpg.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data record for storing equipment affixes with their tiers.
 * Serializable for DataComponents storage.
 */
public record AffixData(List<AffixInstance> affixes) {

    public static final AffixData EMPTY = new AffixData(Collections.emptyList());

    /**
     * Codec for persistent storage (JSON serialization).
     */
    public static final Codec<AffixData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    AffixInstance.CODEC.listOf().fieldOf("affixes").forGetter(AffixData::affixes)
            ).apply(instance, AffixData::new)
    );

    /**
     * Stream codec for network synchronization.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, AffixData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AffixData decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<AffixInstance> instances = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                String id = buf.readUtf();
                int tier = buf.readVarInt();
                instances.add(new AffixInstance(id, tier));
            }
            return new AffixData(instances);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, AffixData data) {
            buf.writeVarInt(data.affixes.size());
            for (AffixInstance instance : data.affixes) {
                buf.writeUtf(instance.affixId());
                buf.writeVarInt(instance.tier());
            }
        }
    };

    /**
     * Checks if this data contains a specific affix (any tier).
     */
    public boolean hasAffix(EquipmentAffix affix) {
        for (AffixInstance instance : affixes) {
            if (instance.affixId().equals(affix.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the instance for a specific affix, or null if not present.
     */
    public AffixInstance getInstance(EquipmentAffix affix) {
        for (AffixInstance instance : affixes) {
            if (instance.affixId().equals(affix.getId())) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Checks if this data has any affixes.
     */
    public boolean isEmpty() {
        return affixes.isEmpty();
    }

    /**
     * Creates AffixData from a list of affix instances.
     */
    public static AffixData fromInstances(List<AffixInstance> instances) {
        return new AffixData(new ArrayList<>(instances));
    }

    /**
     * Creates AffixData with a single affix at a specific tier.
     */
    public static AffixData of(EquipmentAffix affix, int tier) {
        return new AffixData(List.of(AffixInstance.of(affix, tier)));
    }
}
