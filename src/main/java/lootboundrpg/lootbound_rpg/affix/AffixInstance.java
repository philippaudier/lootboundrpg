package lootboundrpg.lootbound_rpg.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Represents an affix instance with a specific tier (I, II, or III).
 */
public record AffixInstance(String affixId, int tier) {

    public static final Codec<AffixInstance> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(AffixInstance::affixId),
                    Codec.intRange(1, 3).fieldOf("tier").forGetter(AffixInstance::tier)
            ).apply(instance, AffixInstance::new)
    );

    /**
     * Gets the affix enum for this instance.
     */
    public EquipmentAffix getAffix() {
        return EquipmentAffix.fromId(affixId);
    }

    /**
     * Gets the value of this affix at its tier.
     */
    public double getValue() {
        EquipmentAffix affix = getAffix();
        return affix != null ? affix.getValue(tier) : 0;
    }

    /**
     * Gets the display percentage.
     */
    public int getDisplayPercent() {
        return (int) (getValue() * 100);
    }

    /**
     * Gets the tier as Roman numeral (I, II, III).
     */
    public String getTierRoman() {
        return switch (tier) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> "I";
        };
    }

    /**
     * Creates an instance from an affix and tier.
     */
    public static AffixInstance of(EquipmentAffix affix, int tier) {
        return new AffixInstance(affix.getId(), Math.max(1, Math.min(3, tier)));
    }
}
