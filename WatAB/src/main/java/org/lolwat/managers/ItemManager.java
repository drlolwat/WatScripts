package org.lolwat.managers;

import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.PaidQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;
import org.lolwat.misc.types.combat.CombatType;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.gear.WatTool;
import org.lolwat.types.gear.WatWeapon;
import org.lolwat.types.gear.WatWearable;

import java.util.*;

@Getter
public class ItemManager {
    @Getter @Setter
    private static ItemManager instance;
    @Getter
    List<WatItem> items;

    public ItemManager() {
        instance = this;
    }

    public void addDefaults() {
        items = new ArrayList<>();

        addWeapon("Bronze sword", CombatType.MELEE, 9);
        addSkilledWeapon("Iron scimitar", 1, 1, 1, -1, -1, CombatType.MELEE, 10);
        addSkilledWeapon("Steel scimitar", 5, 1, 1, -1, -1, CombatType.MELEE, 10);
        addWeapon("Shortbow", CombatType.RANGED, 10);
        addWeapon("Staff of air", CombatType.MAGIC, 10);

        addSkilledWeapon("Oak shortbow", 1, 1, 1, -1, 5, CombatType.RANGED, 10);
        addSkilledWeapon("Willow shortbow", 1, 1, 1, -1, 20, CombatType.RANGED, 10);
        addSkilledWeapon("Maple shortbow", 1, 1, 1, -1, 30, CombatType.RANGED, 10);
        addSkilledWeapon("Yew shortbow", 1, 1, 1, -1, 40, CombatType.RANGED, 10);
        addSkilledWeapon("Magic shortbow", 1, 1, 1, -1, 50, CombatType.RANGED, 10);
        addSkilledWeapon("Dark bow", 1, 1, 1, -1, 60, CombatType.RANGED, 10);

        addWearable("Leather cowl", EquipmentSlot.HAT, CombatType.RANGED, 10);
        addWearable("Leather body", EquipmentSlot.CHEST, CombatType.RANGED, 10);
        addWearable("Leather chaps", EquipmentSlot.LEGS, CombatType.RANGED, 10);
        addWearable("Leather vambraces", EquipmentSlot.HANDS, CombatType.RANGED, 10);
        addWearable("Spiky vambraces", EquipmentSlot.HANDS, CombatType.RANGED, 10);

        addWearable("Zamorak monk top", EquipmentSlot.CHEST, CombatType.MAGIC, 10);
        addWearable("Zamorak monk bottom", EquipmentSlot.LEGS, CombatType.MAGIC, 10);
        addWearable("Blue wizard hat", EquipmentSlot.HAT, CombatType.MAGIC, 10);
        addWearable("Leather gloves", EquipmentSlot.HANDS, CombatType.MAGIC, 10);

        addSkilledWearable("Mystic hat", 1, 1, 20, 40, -1, CombatType.MAGIC, EquipmentSlot.HAT, 10);
        addSkilledWearable("Mystic robe top", 1, 1, 20, 40, -1, CombatType.MAGIC, EquipmentSlot.CHEST, 10);
        addSkilledWearable("Mystic robe bottom", 1, 1, 20, 40, -1, CombatType.MAGIC, EquipmentSlot.LEGS, 10);
        addSkilledWearable("Mystic gloves", 1, 1, 20, 40, -1, CombatType.MAGIC, EquipmentSlot.HANDS, 10);
        addSkilledWearable("Mystic boots", 1, 1, 20, 40, -1, CombatType.MAGIC, EquipmentSlot.FEET, 10);

        addSkilledWearable("Hardleather body", 1, 1, 10, -1, 1, CombatType.RANGED, EquipmentSlot.CHEST, 10);
        addSkilledWearable("Coif", 1, 1, 1, -1, 20, CombatType.RANGED, EquipmentSlot.HAT, 10);
        addSkilledWearable("Studded body", 1, 1, 20, -1, 20, CombatType.RANGED, EquipmentSlot.CHEST, 10);
        addSkilledWearable("Studded chaps", 1, 1, 1, -1, 20, CombatType.RANGED, EquipmentSlot.LEGS, 10);

        addSkilledWearable("Snakeskin boots", 1, 1, 30, -1, 30, CombatType.RANGED, EquipmentSlot.FEET, 10);
        addSkilledWearable("Snakeskin bandana", 1, 1, 30, -1, 30, CombatType.RANGED, EquipmentSlot.HAT, 10);

        addQuestedWearable("Green d'hide body", 1, 1, 40, -1, 40, EquipmentSlot.CHEST, CombatType.RANGED,
                Collections.singletonList(FreeQuest.DRAGON_SLAYER), 10);

        addSkilledWearable("Frog-leather boots", 1, 1, 25, -1, 25, CombatType.RANGED, EquipmentSlot.FEET, 10);
        addSkilledWearable("Ranger boots", 1, 1, 1, -1, 40, CombatType.RANGED, EquipmentSlot.FEET, 10);

        addSkilledWearable("Green d'hide chaps", 1, 1, 1, -1, 40, CombatType.RANGED, EquipmentSlot.LEGS, 10);
        addSkilledWearable("Green d'hide vambraces", 1, 1, 1, -1, 40, CombatType.RANGED, EquipmentSlot.HANDS, 10);
        addSkilledWearable("Blue d'hide body", 1, 1, 40, -1, 50, CombatType.RANGED, EquipmentSlot.CHEST, 10);
        addSkilledWearable("Blue d'hide chaps", 1, 1, 1, -1, 50, CombatType.RANGED, EquipmentSlot.LEGS, 10);
        addSkilledWearable("Blue d'hide vambraces", 1, 1, 1, -1, 50, CombatType.RANGED, EquipmentSlot.HANDS, 10);
        addSkilledWearable("Red d'hide body", 1, 1, 40, -1, 60, CombatType.RANGED, EquipmentSlot.CHEST, 10);
        addSkilledWearable("Red d'hide chaps", 1, 1, 1, -1, 60, CombatType.RANGED, EquipmentSlot.LEGS, 10);
        addSkilledWearable("Red d'hide vambraces", 1, 1, 1, -1, 60, CombatType.RANGED, EquipmentSlot.HANDS, 10);
        addSkilledWearable("Black d'hide body", 1, 1, 40, -1, 70, CombatType.RANGED, EquipmentSlot.CHEST, 10);
        addSkilledWearable("Black d'hide chaps", 1, 1, 1, -1, 70, CombatType.RANGED, EquipmentSlot.LEGS, 10);
        addSkilledWearable("Black d'hide vambraces", 1, 1, 1, -1, 70, CombatType.RANGED, EquipmentSlot.HANDS, 10);

        addQuestedWearable("Ava's attractor", 1, 1, 1, -1, 30, EquipmentSlot.CAPE, CombatType.RANGED,
                Collections.singletonList(PaidQuest.ANIMAL_MAGNETISM), 8);
        addQuestedWearable("Ava's accumulator", 1, 1, 1, -1, 50, EquipmentSlot.CAPE, CombatType.RANGED,
                Collections.singletonList(PaidQuest.ANIMAL_MAGNETISM), 9);
        addQuestedWearable("Ava's assembler", 1, 1, 1, -1, 70, EquipmentSlot.CAPE, CombatType.RANGED,
                Collections.singletonList(PaidQuest.ANIMAL_MAGNETISM), 10);

        addSkilledWeapon("Mithril scimitar", 20, 1, 1, -1, -1, CombatType.MELEE, 10);
        addSkilledWeapon("Adamant scimitar", 30, 1, 1, -1, -1, CombatType.MELEE, 10);
        addSkilledWeapon("Rune scimitar", 40, 1, 1, -1, -1, CombatType.MELEE, 10);
        addSkilledWeapon("Dragon sword", 60, 1, 1, -1, -1, CombatType.MELEE, 9);
        addSkilledWeapon("Abyssal whip", 70, 99, 1, -1, -1, CombatType.MELEE, 10);

        addQuestedWeapon("Dragon scimitar", 60, 1, 1, -1, -1, CombatType.MELEE,
                Collections.singletonList(FreeQuest.DRAGON_SLAYER), 10);

        addWearable("Iron arrow", EquipmentSlot.ARROWS, CombatType.RANGED, 10);
        addSkilledWearable("Steel arrow", 1, 1, 1, -1, 5, CombatType.RANGED, EquipmentSlot.ARROWS, 10);
        addSkilledWearable("Mithril arrow", 1, 1, 1, -1, 20, CombatType.RANGED, EquipmentSlot.ARROWS, 10);
        addSkilledWearable("Adamant arrow", 1, 1, 1, -1, 30, CombatType.RANGED, EquipmentSlot.ARROWS, 10);
        addSkilledWearable("Rune arrow", 1, 1, 1, -1, 40, CombatType.RANGED, EquipmentSlot.ARROWS, 10);

        addWearable("Wooden shield", EquipmentSlot.SHIELD, CombatType.MELEE, 9);
        addWearable("Iron platelegs", EquipmentSlot.LEGS, CombatType.MELEE, 10);
        addWearable("Iron platebody", EquipmentSlot.CHEST, CombatType.MELEE, 10);
        addWearable("Iron kiteshield", EquipmentSlot.SHIELD, CombatType.MELEE, 10);
        addWearable("Iron full helm", EquipmentSlot.HAT, CombatType.MELEE, 10);

        addSkilledWearable("Bronze defender", 1, 1, 1, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD, 10);
        addSkilledWearable("Iron defender", 1, 1, 2, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD, 10);
        addSkilledWearable("Steel defender", 1, 1, 11, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD, 10);
        addSkilledWearable("Mithril defender", 1, 1, 21, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD, 10);
        addSkilledWearable("Adamant defender", 1, 1, 31, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD, 10);
        addSkilledWearable("Rune defender", 1, 1, 41, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD, 10);
        addSkilledWearable("Dragon defender", 1, 1, 60, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD, 9);
        addSkilledWearable("Avernic defender", 1, 1, 60, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD, 10);

        addWearable("Leather boots", EquipmentSlot.FEET, CombatType.MELEE, 10);
        addWearable("Leather boots", EquipmentSlot.FEET, CombatType.RANGED, 10);
        addWearable("Leather boots", EquipmentSlot.FEET, CombatType.MAGIC, 10);

        addWearable("Iron boots", EquipmentSlot.FEET, CombatType.MELEE, 10);

        addSkilledWearable("Mithril boots", 1, 1, 20, -1, -1, CombatType.MELEE, EquipmentSlot.FEET, 10);
        addSkilledWearable("Adamant boots", 1, 1, 30, -1, -1, CombatType.MELEE, EquipmentSlot.FEET, 10);
        addSkilledWearable("Rune boots", 1, 1, 40, -1, -1, CombatType.MELEE, EquipmentSlot.FEET, 10);
        addSkilledWearable("Dragon boots", 1, 1, 60, -1, -1, CombatType.MELEE, EquipmentSlot.FEET, 10);

        addWearable("Leather gloves", EquipmentSlot.HANDS, CombatType.MELEE, 10);
        addWearable("Iron gloves", EquipmentSlot.HANDS, CombatType.MELEE, 10);
        addSkilledWearable("Mithril gloves", 1, 1, 20, -1, -1, CombatType.MELEE, EquipmentSlot.HANDS, 10);
        addSkilledWearable("Adamant gloves", 1, 1, 30, -1, -1, CombatType.MELEE, EquipmentSlot.HANDS, 10);
        addSkilledWearable("Rune gloves", 1, 1, 40, -1, -1, CombatType.MELEE, EquipmentSlot.HANDS, 10);
        addSkilledWearable("Barrows gloves", 1, 1, 70, -1, -1, CombatType.MELEE, EquipmentSlot.HANDS, 10);

        addWearable("Amulet of strength", EquipmentSlot.AMULET, CombatType.MELEE, 10);
        addWearable("Amulet of accuracy", EquipmentSlot.AMULET, CombatType.RANGED, 10);
        addWearable("Amulet of magic", EquipmentSlot.AMULET, CombatType.MAGIC, 10);

        addSkilledWearable("Amulet of fury", 1, 1, 1, 1, 1, CombatType.MELEE, EquipmentSlot.AMULET, 10);
        addSkilledWearable("Amulet of fury", 1, 1, 1, 1, 1, CombatType.RANGED, EquipmentSlot.AMULET, 10);
        addSkilledWearable("Amulet of fury", 1, 1, 1, 1, 1, CombatType.MAGIC, EquipmentSlot.AMULET, 10);

        addWearable("Black cape", EquipmentSlot.CAPE, CombatType.MELEE, 9);
        addWearable("Black cape", EquipmentSlot.CAPE, CombatType.RANGED, 9);
        addWearable("Black cape", EquipmentSlot.CAPE, CombatType.MAGIC, 9);

        addSkilledWearable("Obsidian cape", 1, 1, 1, 1, 1, CombatType.MELEE, EquipmentSlot.CAPE, 10);
        addSkilledWearable("Obsidian cape", 1, 1, 1, 1, 1, CombatType.RANGED, EquipmentSlot.CAPE, 10);
        addSkilledWearable("Obsidian cape", 1, 1, 1, 1, 1, CombatType.MAGIC, EquipmentSlot.CAPE, 10);

        addSkilledWearable("Fire cape", 10, 10, 10, 1, 1, CombatType.MELEE, EquipmentSlot.CAPE, 10);
        addSkilledWearable("Fire cape", 1, 1, 1, 1, 10, CombatType.RANGED, EquipmentSlot.CAPE, 10);
        addSkilledWearable("Fire cape", 1, 1, 1, 10, 1, CombatType.MAGIC, EquipmentSlot.CAPE, 10);

        addSkilledWearable("Infernal cape", 11, 11, 11, 1, 1, CombatType.MELEE, EquipmentSlot.CAPE, 10);
        addSkilledWearable("Infernal cape", 1, 1, 1, 1, 11, CombatType.RANGED, EquipmentSlot.CAPE, 10);
        addSkilledWearable("Infernal cape", 1, 1, 1, 11, 1, CombatType.MAGIC, EquipmentSlot.CAPE, 10);

        addSkilledWearable("Steel platelegs", 1, 1, 10, -1, -1, CombatType.MELEE, EquipmentSlot.LEGS, 10);
        addSkilledWearable("Steel platebody", 1, 1, 10, -1, -1, CombatType.MELEE, EquipmentSlot.CHEST, 10);
        addSkilledWearable("Steel kiteshield", 1, 1, 10, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD, 10);
        addSkilledWearable("Steel full helm", 1, 1, 10, -1, -1, CombatType.MELEE, EquipmentSlot.HAT, 10);

        addSkilledWearable("Mithril platelegs", 1, 1, 20, -1, -1, CombatType.MELEE, EquipmentSlot.LEGS, 10);
        addSkilledWearable("Mithril platebody", 1, 1, 20, -1, -1, CombatType.MELEE, EquipmentSlot.CHEST, 10);
        addSkilledWearable("Mithril kiteshield", 1, 1, 20, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD, 10);
        addSkilledWearable("Mithril full helm", 1, 1, 20, -1, -1, CombatType.MELEE, EquipmentSlot.HAT, 10);

        addSkilledWearable("Adamant platelegs", 1, 1, 30, -1, -1, CombatType.MELEE, EquipmentSlot.LEGS, 10);
        addSkilledWearable("Adamant platebody", 1, 1, 30, -1, -1, CombatType.MELEE, EquipmentSlot.CHEST, 10);
        addSkilledWearable("Adamant kiteshield", 1, 1, 30, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD, 10);
        addSkilledWearable("Adamant full helm", 1, 1, 30, -1, -1, CombatType.MELEE, EquipmentSlot.HAT, 10);

        addSkilledWearable("Rune platelegs", 1, 1, 40, -1, -1, CombatType.MELEE, EquipmentSlot.LEGS, 10);
        addSkilledWearable("Rune chainbody", 1, 1, 40, -1, -1, CombatType.MELEE, EquipmentSlot.CHEST, 10);

        addQuestedWearable("Rune platebody", 1, 1, 40, -1, -1, EquipmentSlot.CHEST, CombatType.MELEE,
                Collections.singletonList(FreeQuest.DRAGON_SLAYER), 10);

        addSkilledWearable("Rune kiteshield", 1, 1, 40, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD, 10);
        addSkilledWearable("Rune full helm", 1, 1, 40, -1, -1, CombatType.MELEE, EquipmentSlot.HAT, 10);

        addSkilledWearable("Dragon platelegs", 1, 1, 60, -1, -1, CombatType.MELEE, EquipmentSlot.LEGS, 10);
        addSkilledWearable("Dragon chainbody", 1, 1, 60, -1, -1, CombatType.MELEE, EquipmentSlot.CHEST, 9);

        addQuestedWearable("Dragon platebody", 1, 1, 60, -1, -1, EquipmentSlot.CHEST, CombatType.MELEE,
                Collections.singletonList(FreeQuest.DRAGON_SLAYER), 8);

        addSkilledWearable("Fighter torso", 1, 1, 60, -1, -1, CombatType.MELEE, EquipmentSlot.CHEST, 10);
        addSkilledWearable("Dragon med helm", 1, 1, 60, -1, -1, CombatType.MELEE, EquipmentSlot.HAT, 9);
        addSkilledWearable("Obsidian helmet", 1, 1, 60, -1, -1, CombatType.MELEE, EquipmentSlot.HAT, 10);

        // other?
        addBasicItem("Coins");
        addBasicItem("Brass key");
        addBasicItem("Knife");
        addBasicItem("Feather");

        // food
        addBasicItem("Trout");
        addBasicItem("Lobster");
        addBasicItem("Tuna");
        addBasicItem("Shark");

        // teleport items
        addBasicItem("Varrock teleport");
        addBasicItem("Camelot teleport");
        addBasicItem("Falador teleport");

        // prayer stuff
        addBasicItem("Bones");
        addBasicItem("Big bones");

        // runes
        addBasicItem("Mind rune");
        addBasicItem("Earth rune");
        addBasicItem("Water rune");
        addBasicItem("Fire rune");
        addBasicItem("Chaos rune");
        addBasicItem("Death rune");
        addBasicItem("Nature rune");

        addTool("Bronze pickaxe", Skill.MINING, new HashMap<>(), new HashMap<>());
        addTool("Mithril pickaxe", Skill.MINING, new HashMap<Skill, Integer>() {
            {
                put(Skill.MINING, 21);
            }
        }, new HashMap<Skill, Integer>() {
            {
                put(Skill.ATTACK, 20);
            }
        });

        addTool("Adamant pickaxe", Skill.MINING, new HashMap<Skill, Integer>() {
            {
                put(Skill.MINING, 31);
            }
        }, new HashMap<Skill, Integer>() {
            {
                put(Skill.ATTACK, 30);
            }
        });

        addTool("Rune pickaxe", Skill.MINING, new HashMap<Skill, Integer>() {
            {
                put(Skill.MINING, 41);
            }
        }, new HashMap<Skill, Integer>() {
            {
                put(Skill.ATTACK, 40);
            }
        });

        addTool("Dragon pickaxe", Skill.MINING, new HashMap<Skill, Integer>() {
            {
                put(Skill.MINING, 61);
            }
        }, new HashMap<Skill, Integer>() {
            {
                put(Skill.ATTACK, 60);
            }
        });

        addTool("Bronze axe", Skill.WOODCUTTING, new HashMap<>(), new HashMap<>());
        addTool("Mithril axe", Skill.WOODCUTTING, new HashMap<Skill, Integer>() {
            {
                put(Skill.WOODCUTTING, 21);
            }
        }, new HashMap<Skill, Integer>() {
            {
                put(Skill.ATTACK, 20);
            }
        });

        addTool("Adamant axe", Skill.WOODCUTTING, new HashMap<Skill, Integer>() {
            {
                put(Skill.WOODCUTTING, 31);
            }
        }, new HashMap<Skill, Integer>() {
            {
                put(Skill.ATTACK, 30);
            }
        });

        addTool("Rune axe", Skill.WOODCUTTING, new HashMap<Skill, Integer>() {
            {
                put(Skill.WOODCUTTING, 41);
            }
        }, new HashMap<Skill, Integer>() {
            {
                put(Skill.ATTACK, 40);
            }
        });

        addTool("Dragon axe", Skill.WOODCUTTING, new HashMap<Skill, Integer>() {
            {
                put(Skill.WOODCUTTING, 61);
            }
        }, new HashMap<Skill, Integer>() {
            {
                put(Skill.ATTACK, 60);
            }
        });

        // ore
        addBasicItem("Tin ore");
        addBasicItem("Copper ore");
        addBasicItem("Iron ore");
        addBasicItem("Coal");
        addBasicItem("Mithril ore");
        addBasicItem("Adamantite ore");
        addBasicItem("Runite ore");

        // logs
        addBasicItem("Logs");
        addBasicItem("Oak logs");
        addBasicItem("Maple logs");

        // misc
        addBasicItem("Coin pouch");

        //addChargedItem("Ring of wealth (", "Ring of wealth (5)");
    }

    public WatItem getItem(String name) {
        for(WatItem item : items) {
            if(item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }

        Logger.error("WatItem config missing: " + name);
        return null;
    }

    private void addTool(String name, Skill skillUsed, HashMap<Skill, Integer> skillsNeeded, HashMap<Skill, Integer> skillsToEquip) {
        items.add(new WatTool(name, skillUsed, skillsNeeded, skillsToEquip));
    }

    private void addBasicItem(String name) {
        items.add(new WatItem(name));
    }

    private void addWeapon(String name, CombatType type, int weight) {
        items.add(new WatWeapon(name, type, weight));
    }

    private void addWearable(String name, EquipmentSlot slot, CombatType type, int weight) {
        items.add(new WatWearable(name, slot, type, weight));
    }

    private void addSkilledWeapon(String name, int attack, int strength, int defence, int magic, int ranged, CombatType type, int weight) {
        items.add(new WatWeapon(name, type, new HashMap<Skill, Integer>()
        {
            {
                put(Skill.ATTACK, attack);
                put(Skill.STRENGTH, strength);
                put(Skill.DEFENCE, defence);
                put(Skill.MAGIC, magic);
                put(Skill.RANGED, ranged);
            }
        }, weight));
    }

    private void addQuestedWeapon(String name, int attack, int strength, int defence, int magic, int ranged, CombatType type, List<Quest> quests, int weight) {
        items.add(new WatWeapon(name, type, new HashMap<Skill, Integer>()
        {
            {
                put(Skill.ATTACK, attack);
                put(Skill.STRENGTH, strength);
                put(Skill.DEFENCE, defence);
                put(Skill.MAGIC, magic);
                put(Skill.RANGED, ranged);
            }
        }, quests, weight));
    }

    private void addSkilledWearable(String name, int attack, int strength, int defence, int magic, int ranged, CombatType type, EquipmentSlot slot, int weight) {
        items.add(new WatWearable(name, slot, type, new HashMap<Skill, Integer>()
        {
            {
                put(Skill.ATTACK, attack);
                put(Skill.STRENGTH, strength);
                put(Skill.DEFENCE, defence);
                put(Skill.MAGIC, magic);
                put(Skill.RANGED, ranged);
            }
        }, weight));
    }

    private void addQuestedWearable(String name, int attack, int strength, int defence, int magic, int ranged, EquipmentSlot slot, CombatType type, List<Quest> quests, int weight) {
        items.add(new WatWearable(name, slot, type, new HashMap<Skill, Integer>()
        {
            {
                put(Skill.ATTACK, attack);
                put(Skill.STRENGTH, strength);
                put(Skill.DEFENCE, defence);
                put(Skill.MAGIC, magic);
                put(Skill.RANGED, ranged);
            }
        }, quests, weight));
    }

    private <T extends WatItem> T getBestItem(
            List<T> items,
            java.util.function.Predicate<T> filter,
            java.util.function.Function<T, Integer> scoreFunc,
            java.util.function.Function<T, Boolean> ownedFunc,
            java.util.function.Function<T, Integer> priceFunc,
            int availableMoney
    ) {
        List<T> candidates = new ArrayList<>();
        for (T item : items) {
            if (filter.test(item)) {
                candidates.add(item);
            }
        }
        candidates.sort((a, b) -> Integer.compare(scoreFunc.apply(b), scoreFunc.apply(a)));
        for (T item : candidates) {
            boolean owned = ownedFunc.apply(item);
            boolean tradeable = item.isTradeable();
            if (owned) return item;
            if (tradeable) {
                int price = priceFunc.apply(item);
                if (price > 0 && availableMoney >= price) return item;
            }
            // If not tradeable and not owned, skip
        }
        return null;
    }

    public WatTool getBestTool(Skill sk) {
        int availableMoney = Inventory.count("Coins");
        if (Bank.isOpen() || Bank.isCached()) availableMoney += Bank.count("Coins");

        List<WatTool> tools = new ArrayList<>();
        for (WatItem item : items) {
            if (item instanceof WatTool) tools.add((WatTool) item);
        }

        return getBestItem(
                tools,
                tool -> tool.getSkillUsed().equals(sk)
                        && meetsSkillAndQuestReqs(tool.getLevelRequirements(), tool.getQuestRequirements()),
                tool -> {
                    int reqSum = tool.getLevelRequirements() == null ? 0 : tool.getLevelRequirements().values().stream().mapToInt(Integer::intValue).sum();
                    return reqSum * tool.getWeight();
                },
                tool -> (Bank.isOpen() && Bank.contains(tool.getName())) || Inventory.contains(tool.getName()) || Equipment.contains(tool.getName()),
                WatItem::getPrice,
                availableMoney
        );
    }

    public WatWeapon getBestWeapon(CombatType type) {
        int availableMoney = Inventory.count("Coins");
        if (Bank.isOpen() || Bank.isCached()) availableMoney += Bank.count("Coins");

        List<WatWeapon> weapons = new ArrayList<>();
        for (WatItem item : items) {
            if (item instanceof WatWeapon) weapons.add((WatWeapon) item);
        }

        return getBestItem(
                weapons,
                weapon -> weapon.getCombatType() == type
                        && meetsSkillAndQuestReqs(weapon.getLevelRequirements(), weapon.getQuestRequirements()),
                weapon -> {
                    int reqSum = weapon.getLevelRequirements() == null ? 0 : weapon.getLevelRequirements().values().stream().mapToInt(Integer::intValue).sum();
                    return reqSum * weapon.getWeight();
                },
                weapon -> (Bank.isOpen() && Bank.contains(weapon.getName())) || Inventory.contains(weapon.getName()) || Equipment.contains(weapon.getName()),
                WatItem::getPrice,
                availableMoney
        );
    }

    public WatWearable getBestWearable(EquipmentSlot slot, CombatType type) {
        int availableMoney = Inventory.count("Coins");
        if (Bank.isOpen() || Bank.isCached()) availableMoney += Bank.count("Coins");

        List<WatWearable> wearables = new ArrayList<>();
        for (WatItem item : items) {
            if (item instanceof WatWearable) wearables.add((WatWearable) item);
        }

        return getBestItem(
                wearables,
                wearable -> wearable.getCombatType() == type
                        && wearable.getSlot() == slot
                        && meetsSkillAndQuestReqs(wearable.getLevelRequirements(), wearable.getQuestRequirements()),
                wearable -> {
                    int reqSum = wearable.getLevelRequirements() == null ? 0 : wearable.getLevelRequirements().values().stream().mapToInt(Integer::intValue).sum();
                    return reqSum * wearable.getWeight();
                },
                wearable -> (Bank.isOpen() && Bank.contains(wearable.getName())) || Inventory.contains(wearable.getName()) || Equipment.contains(wearable.getName()),
                WatItem::getPrice,
                availableMoney
        );
    }

    // Helper method for requirements
    private boolean meetsSkillAndQuestReqs(Map<Skill, Integer> skillReqs, List<Quest> questReqs) {
        if (skillReqs != null) {
            for (Map.Entry<Skill, Integer> req : skillReqs.entrySet()) {
                int playerLevel = Skills.getRealLevel(req.getKey());
                if (req.getValue() > 0 && playerLevel < req.getValue()) {
                    return false;
                }
            }
        }
        if (questReqs != null) {
            for (Quest q : questReqs) {
                if (!Quests.isFinished(q)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isValidWeapon(String name, CombatType type) {
        WatItem item = getItem(name);
        return item instanceof WatWeapon && ((WatWeapon) item).getCombatType().equals(type);
    }

    public boolean isValidTool(String name, Skill s) {
        WatItem item = getItem(name);
        return item instanceof WatTool && ((WatTool) item).getSkillUsed().equals(s);
    }

    public boolean isValidWearable(String name, EquipmentSlot slot, CombatType type) {
        for (WatItem item : items) {
            if (item instanceof WatWearable
                    && item.getName().equalsIgnoreCase(name)
                    && ((WatWearable) item).getSlot() == slot
                    && ((WatWearable) item).getCombatType() == type) {
                return true;
            }
        }
        return false;
    }
}
