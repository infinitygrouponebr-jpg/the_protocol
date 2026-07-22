package Infinitygroup.the_protocol.perk;

/** A future unlocked perk definition. Perks are not assigned in this initial foundation. */
public record Perk(String id, PerkType type, int requiredLevel) {
}
