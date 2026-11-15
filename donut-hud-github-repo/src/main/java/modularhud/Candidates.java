package modularhud;

import java.util.List;

/** Pick-lists for the ESP selector screens. */
public final class Candidates {
    private Candidates() {}

    public static final List<String> BLOCKS = List.of(
            "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore", "minecraft:ancient_debris",
            "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore", "minecraft:gold_ore",
            "minecraft:deepslate_gold_ore", "minecraft:nether_gold_ore", "minecraft:iron_ore",
            "minecraft:deepslate_iron_ore", "minecraft:coal_ore", "minecraft:deepslate_coal_ore",
            "minecraft:copper_ore", "minecraft:deepslate_copper_ore", "minecraft:lapis_ore",
            "minecraft:deepslate_lapis_ore", "minecraft:redstone_ore", "minecraft:deepslate_redstone_ore",
            "minecraft:nether_quartz_ore", "minecraft:spawner", "minecraft:trial_spawner", "minecraft:vault",
            "minecraft:chest", "minecraft:trapped_chest", "minecraft:ender_chest", "minecraft:barrel",
            "minecraft:shulker_box", "minecraft:hopper", "minecraft:dropper", "minecraft:dispenser",
            "minecraft:furnace", "minecraft:blast_furnace", "minecraft:smoker", "minecraft:crafting_table",
            "minecraft:enchanting_table", "minecraft:anvil", "minecraft:beacon", "minecraft:conduit",
            "minecraft:end_portal_frame", "minecraft:end_portal", "minecraft:nether_portal",
            "minecraft:respawn_anchor", "minecraft:bedrock", "minecraft:obsidian", "minecraft:crying_obsidian",
            "minecraft:tnt", "minecraft:lava", "minecraft:water", "minecraft:sculk_shrieker",
            "minecraft:sculk_sensor", "minecraft:sculk_catalyst", "minecraft:budding_amethyst",
            "minecraft:amethyst_cluster", "minecraft:bee_nest", "minecraft:beehive", "minecraft:jukebox",
            "minecraft:note_block", "minecraft:bell", "minecraft:composter", "minecraft:lodestone"
    );

    public static final List<String> ENTITIES = List.of(
            "minecraft:player", "minecraft:zombie", "minecraft:skeleton", "minecraft:creeper",
            "minecraft:spider", "minecraft:cave_spider", "minecraft:enderman", "minecraft:witch",
            "minecraft:phantom", "minecraft:drowned", "minecraft:husk", "minecraft:stray",
            "minecraft:slime", "minecraft:blaze", "minecraft:ghast", "minecraft:wither_skeleton",
            "minecraft:piglin", "minecraft:piglin_brute", "minecraft:hoglin", "minecraft:zombified_piglin",
            "minecraft:warden", "minecraft:wither", "minecraft:ender_dragon", "minecraft:villager",
            "minecraft:wandering_trader", "minecraft:iron_golem", "minecraft:wolf", "minecraft:cat",
            "minecraft:horse", "minecraft:donkey", "minecraft:cow", "minecraft:pig", "minecraft:sheep",
            "minecraft:chicken", "minecraft:item", "minecraft:experience_orb", "minecraft:tnt",
            "minecraft:end_crystal", "minecraft:armor_stand", "minecraft:item_frame",
            "minecraft:minecart", "minecraft:chest_minecart", "minecraft:boat", "minecraft:ender_pearl",
            "minecraft:arrow", "minecraft:trident", "minecraft:fireball", "minecraft:wind_charge"
    );
}
