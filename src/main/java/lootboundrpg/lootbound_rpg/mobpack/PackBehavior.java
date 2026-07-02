package lootboundrpg.lootbound_rpg.mobpack;

import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Manages pack behavior for spawned mob packs.
 *
 * Features:
 * - Pack cohesion: mobs stay near their pack leader
 * - Linked aggro: attacking one mob alerts the whole pack
 */
public class PackBehavior {

    // Pack tracking: packId -> list of mob UUIDs in the pack
    private static final Map<UUID, Set<UUID>> packs = new HashMap<>();

    // Reverse lookup: mobId -> packId
    private static final Map<UUID, UUID> mobToPack = new HashMap<>();

    // Pack leaders: packId -> leader mob UUID
    private static final Map<UUID, UUID> packLeaders = new HashMap<>();

    // Maximum distance mobs will stay from pack leader
    public static final double PACK_COHESION_RADIUS = 20.0;

    // Distance at which linked aggro triggers
    public static final double AGGRO_LINK_RADIUS = 24.0;

    // Tick counter for periodic cohesion checks
    private static int tickCounter = 0;
    private static final int COHESION_CHECK_INTERVAL = 40; // Every 2 seconds

    /**
     * Registers the pack behavior event handlers.
     */
    public static void register() {
        LootboundRpgMod.LOGGER.info("[PackBehavior] Registering pack behavior system...");

        // Listen for damage events to trigger linked aggro
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (entity instanceof Mob mob && source.getEntity() instanceof Player player) {
                triggerLinkedAggro(mob, player);
            }
        });

        // Periodic cohesion check
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter < COHESION_CHECK_INTERVAL) return;
            tickCounter = 0;

            for (ServerLevel level : server.getAllLevels()) {
                checkPackCohesion(level);
                cleanup(level);
            }
        });
    }

    /**
     * Creates a new pack and returns its ID.
     */
    public static UUID createPack() {
        UUID packId = UUID.randomUUID();
        packs.put(packId, new HashSet<>());
        return packId;
    }

    /**
     * Adds a mob to a pack. The first mob added becomes the leader.
     */
    public static void addToPack(UUID packId, Mob mob) {
        Set<UUID> packMembers = packs.get(packId);
        if (packMembers == null) return;

        UUID mobId = mob.getUUID();
        packMembers.add(mobId);
        mobToPack.put(mobId, packId);

        // First mob is the leader
        if (!packLeaders.containsKey(packId)) {
            packLeaders.put(packId, mobId);
        }
    }

    /**
     * Checks pack cohesion and makes mobs move toward leader if too far.
     */
    private static void checkPackCohesion(ServerLevel level) {
        for (Map.Entry<UUID, Set<UUID>> entry : packs.entrySet()) {
            UUID packId = entry.getKey();
            Set<UUID> members = entry.getValue();

            Mob leader = getPackLeader(packId, level);
            if (leader == null || !leader.isAlive()) continue;

            for (UUID memberId : members) {
                if (memberId.equals(leader.getUUID())) continue;

                if (level.getEntity(memberId) instanceof Mob mob) {
                    if (!mob.isAlive()) continue;

                    // Skip if mob has a target (it's fighting)
                    if (mob.getTarget() != null) continue;

                    double distance = mob.distanceTo(leader);
                    if (distance > PACK_COHESION_RADIUS) {
                        // Move toward leader
                        mob.getNavigation().moveTo(leader, 1.0);
                    }
                }
            }
        }
    }

    /**
     * When a pack mob is attacked, all nearby pack members target the attacker.
     */
    private static void triggerLinkedAggro(Mob attackedMob, Player attacker) {
        UUID packId = mobToPack.get(attackedMob.getUUID());
        if (packId == null) return;

        Set<UUID> packMembers = packs.get(packId);
        if (packMembers == null) return;

        if (!(attackedMob.level() instanceof ServerLevel level)) return;

        // Alert all pack members within range
        for (UUID memberId : packMembers) {
            if (memberId.equals(attackedMob.getUUID())) continue;

            if (level.getEntity(memberId) instanceof Mob packMate) {
                if (!packMate.isAlive()) continue;

                double distance = packMate.distanceTo(attackedMob);
                if (distance <= AGGRO_LINK_RADIUS) {
                    packMate.setTarget(attacker);
                }
            }
        }
    }

    /**
     * Removes a mob from its pack (called when mob dies).
     */
    public static void removeFromPack(UUID mobId) {
        UUID packId = mobToPack.remove(mobId);
        if (packId == null) return;

        Set<UUID> packMembers = packs.get(packId);
        if (packMembers != null) {
            packMembers.remove(mobId);

            // If pack is empty, clean up
            if (packMembers.isEmpty()) {
                packs.remove(packId);
                packLeaders.remove(packId);
            }
            // If leader died, assign new leader
            else if (packLeaders.get(packId) != null && packLeaders.get(packId).equals(mobId)) {
                // Pick first remaining member as new leader
                packMembers.stream().findFirst().ifPresent(newLeader ->
                    packLeaders.put(packId, newLeader)
                );
            }
        }
    }

    /**
     * Gets the leader mob of a pack.
     */
    public static Mob getPackLeader(UUID packId, ServerLevel level) {
        UUID leaderId = packLeaders.get(packId);
        if (leaderId == null) return null;

        if (level.getEntity(leaderId) instanceof Mob leader) {
            return leader;
        }
        return null;
    }

    /**
     * Cleans up dead mobs from pack tracking.
     */
    public static void cleanup(ServerLevel level) {
        List<UUID> deadMobs = new ArrayList<>();
        for (UUID mobId : new ArrayList<>(mobToPack.keySet())) {
            var entity = level.getEntity(mobId);
            if (entity == null || !entity.isAlive()) {
                deadMobs.add(mobId);
            }
        }
        deadMobs.forEach(PackBehavior::removeFromPack);
    }
}
