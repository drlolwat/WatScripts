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

        // weapons
        addWeapon("Bronze sword", CombatType.MELEE);
        addWeapon("Iron scimitar", CombatType.MELEE);

        addWeapon("Shortbow", CombatType.RANGED);
        addWeapon("Staff of air", CombatType.MAGIC);

        // bows
        addSkilledWeapon("Oak shortbow", 1, 1, 1, -1, 5, CombatType.RANGED);
        addSkilledWeapon("Willow shortbow", 1, 1, 1, -1, 20, CombatType.RANGED);
        addSkilledWeapon("Maple shortbow", 1, 1, 1, -1, 30, CombatType.RANGED);
        addSkilledWeapon("Yew shortbow", 1, 1, 1, -1, 40, CombatType.RANGED);
        addSkilledWeapon("Magic shortbow", 1, 1, 1, -1, 50, CombatType.RANGED);
        addSkilledWeapon("Dark bow", 1, 1, 1, -1, 60, CombatType.RANGED);

        addWearable("Leather cowl", EquipmentSlot.HAT, CombatType.RANGED);
        addWearable("Leather body", EquipmentSlot.CHEST, CombatType.RANGED);
        addWearable("Leather chaps", EquipmentSlot.LEGS, CombatType.RANGED);
        addWearable("Leather vambraces", EquipmentSlot.HANDS, CombatType.RANGED);
        addWearable("Spiky vambraces", EquipmentSlot.HANDS, CombatType.RANGED);

        addSkilledWearable("Hardleather body", 1, 1, 10, -1, 1, CombatType.RANGED, EquipmentSlot.CHEST);
        addSkilledWearable("Coif", 1, 1, 1, -1, 20, CombatType.RANGED, EquipmentSlot.HAT);
        addSkilledWearable("Studded body", 1, 1, 20, -1, 20, CombatType.RANGED, EquipmentSlot.CHEST);
        addSkilledWearable("Studded chaps", 1, 1, 1, -1, 20, CombatType.RANGED, EquipmentSlot.LEGS);

        addSkilledWearable("Snakeskin boots", 1, 1, 30, -1, 30, CombatType.RANGED, EquipmentSlot.FEET);
        addSkilledWearable("Snakeskin bandana", 1, 1, 30, -1, 30, CombatType.RANGED, EquipmentSlot.HAT);

        addQuestedWearable("Green d'hide body", 1, 1, 40, -1, 40, EquipmentSlot.CHEST, CombatType.RANGED,
                Collections.singletonList(FreeQuest.DRAGON_SLAYER));

        addSkilledWearable("Frog-leather boots", 1, 1, 25, -1, 25, CombatType.RANGED, EquipmentSlot.FEET);
        addSkilledWearable("Ranger boots", 1, 1, 1, -1, 40, CombatType.RANGED, EquipmentSlot.FEET);

        addSkilledWearable("Green d'hide chaps", 1, 1, 1, -1, 40, CombatType.RANGED, EquipmentSlot.LEGS);
        addSkilledWearable("Green d'hide vambraces", 1, 1, 1, -1, 40, CombatType.RANGED, EquipmentSlot.HANDS);
        addSkilledWearable("Blue d'hide body", 1, 1, 40, -1, 50, CombatType.RANGED, EquipmentSlot.CHEST);
        addSkilledWearable("Blue d'hide chaps", 1, 1, 1, -1, 50, CombatType.RANGED, EquipmentSlot.LEGS);
        addSkilledWearable("Blue d'hide vambraces", 1, 1, 1, -1, 50, CombatType.RANGED, EquipmentSlot.HANDS);
        addSkilledWearable("Red d'hide body", 1, 1, 40, -1, 60, CombatType.RANGED, EquipmentSlot.CHEST);
        addSkilledWearable("Red d'hide chaps", 1, 1, 1, -1, 60, CombatType.RANGED, EquipmentSlot.LEGS);
        addSkilledWearable("Red d'hide vambraces", 1, 1, 1, -1, 60, CombatType.RANGED, EquipmentSlot.HANDS);
        addSkilledWearable("Black d'hide body", 1, 1, 40, -1, 70, CombatType.RANGED, EquipmentSlot.CHEST);
        addSkilledWearable("Black d'hide chaps", 1, 1, 1, -1, 70, CombatType.RANGED, EquipmentSlot.LEGS);
        addSkilledWearable("Black d'hide vambraces", 1, 1, 1, -1, 70, CombatType.RANGED, EquipmentSlot.HANDS);

        addQuestedWearable("Ava's attractor", 1, 1, 1, -1, 30, EquipmentSlot.CAPE, CombatType.RANGED,
                Collections.singletonList(PaidQuest.ANIMAL_MAGNETISM));
        addQuestedWearable("Ava's accumulator", 1, 1, 1, -1, 50, EquipmentSlot.CAPE, CombatType.RANGED,
                Collections.singletonList(PaidQuest.ANIMAL_MAGNETISM));
        addQuestedWearable("Ava's assembler", 1, 1, 1, -1, 70, EquipmentSlot.CAPE, CombatType.RANGED,
                Collections.singletonList(PaidQuest.ANIMAL_MAGNETISM));

        addSkilledWeapon("Mithril scimitar", 20, 1, 1, -1, -1, CombatType.MELEE);
        addSkilledWeapon("Adamant scimitar", 30, 1, 1, -1, -1, CombatType.MELEE);
        addSkilledWeapon("Rune scimitar", 40, 1, 1, -1, -1, CombatType.MELEE);
        addSkilledWeapon("Dragon sword", 60, 1, 1, -1, -1, CombatType.MELEE);
        addSkilledWeapon("Abyssal whip", 70, 99, 1, -1, -1, CombatType.MELEE);

        addWearable("Iron arrow", EquipmentSlot.ARROWS, CombatType.RANGED);
        addSkilledWearable("Steel arrow", 1, 1, 1, -1, 5, CombatType.RANGED, EquipmentSlot.ARROWS);
        addSkilledWearable("Mithril arrow", 1, 1, 1, -1, 20, CombatType.RANGED, EquipmentSlot.ARROWS);
        addSkilledWearable("Adamant arrow", 1, 1, 1, -1, 30, CombatType.RANGED, EquipmentSlot.ARROWS);
        addSkilledWearable("Rune arrow", 1, 1, 1, -1, 40, CombatType.RANGED, EquipmentSlot.ARROWS);

        addWearable("Wooden shield", EquipmentSlot.SHIELD, CombatType.MELEE);
        addWearable("Iron platelegs", EquipmentSlot.LEGS, CombatType.MELEE);
        addWearable("Iron platebody", EquipmentSlot.CHEST, CombatType.MELEE);
        addWearable("Iron kiteshield", EquipmentSlot.SHIELD, CombatType.MELEE);
        addWearable("Iron full helm", EquipmentSlot.HAT, CombatType.MELEE);

        addSkilledWearable("Bronze defender", 1, 1, 1, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD);
        addSkilledWearable("Iron defender", 1, 1, 2, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD);
        addSkilledWearable("Steel defender", 1, 1, 11, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD);
        addSkilledWearable("Mithril defender", 1, 1, 21, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD);
        addSkilledWearable("Adamant defender", 1, 1, 31, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD);
        addSkilledWearable("Rune defender", 1, 1, 41, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD);
        addSkilledWearable("Dragon defender", 1, 1, 60, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD);
        addSkilledWearable("Avernic defender", 1, 1, 61, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD);

        addWearable("Leather boots", EquipmentSlot.FEET, CombatType.MELEE);
        addWearable("Leather boots", EquipmentSlot.FEET, CombatType.RANGED);
        addWearable("Leather boots", EquipmentSlot.FEET, CombatType.MAGIC);

        addWearable("Iron boots", EquipmentSlot.FEET, CombatType.MELEE);

        addSkilledWearable("Mithril boots", 1, 1, 20, -1, -1, CombatType.MELEE, EquipmentSlot.FEET);
        addSkilledWearable("Adamant boots", 1, 1, 30, -1, -1, CombatType.MELEE, EquipmentSlot.FEET);
        addSkilledWearable("Rune boots", 1, 1, 40, -1, -1, CombatType.MELEE, EquipmentSlot.FEET);
        addSkilledWearable("Dragon boots", 1, 1, 60, -1, -1, CombatType.MELEE, EquipmentSlot.FEET);

        addWearable("Leather gloves", EquipmentSlot.HANDS, CombatType.MELEE);
        addWearable("Iron gloves", EquipmentSlot.HANDS, CombatType.MELEE);
        addSkilledWearable("Mithril gloves", 1, 1, 20, -1, -1, CombatType.MELEE, EquipmentSlot.HANDS);
        addSkilledWearable("Adamant gloves", 1, 1, 30, -1, -1, CombatType.MELEE, EquipmentSlot.HANDS);
        addSkilledWearable("Rune gloves", 1, 1, 40, -1, -1, CombatType.MELEE, EquipmentSlot.HANDS);
        addSkilledWearable("Barrows gloves", 1, 1, 70, -1, -1, CombatType.MELEE, EquipmentSlot.HANDS);

        addWearable("Amulet of strength", EquipmentSlot.AMULET, CombatType.MELEE);
        addWearable("Amulet of accuracy", EquipmentSlot.AMULET, CombatType.RANGED);
        addWearable("Amulet of magic", EquipmentSlot.AMULET, CombatType.MAGIC);

        addSkilledWearable("Amulet of fury", 1, 1, 1, 1, 1, CombatType.MELEE, EquipmentSlot.AMULET);
        addSkilledWearable("Amulet of fury", 1, 1, 1, 1, 1, CombatType.RANGED, EquipmentSlot.AMULET);
        addSkilledWearable("Amulet of fury", 1, 1, 1, 1, 1, CombatType.MAGIC, EquipmentSlot.AMULET);

        addWearable("Black cape", EquipmentSlot.CAPE, CombatType.MELEE);
        addWearable("Black cape", EquipmentSlot.CAPE, CombatType.RANGED);
        addWearable("Black cape", EquipmentSlot.CAPE, CombatType.MAGIC);

        addSkilledWearable("Obsidian cape", 9, 9, 9, 1, 1, CombatType.MELEE, EquipmentSlot.CAPE);
        addSkilledWearable("Obsidian cape", 1, 1, 1, 1, 9, CombatType.RANGED, EquipmentSlot.CAPE);
        addSkilledWearable("Obsidian cape", 1, 1, 1, 9, 1, CombatType.MAGIC, EquipmentSlot.CAPE);

        addSkilledWearable("Fire cape", 10, 10, 10, 1, 1, CombatType.MELEE, EquipmentSlot.CAPE);
        addSkilledWearable("Fire cape", 1, 1, 1, 1, 10, CombatType.RANGED, EquipmentSlot.CAPE);
        addSkilledWearable("Fire cape", 1, 1, 1, 10, 1, CombatType.MAGIC, EquipmentSlot.CAPE);

        addSkilledWearable("Infernal cape", 11, 11, 11, 1, 1, CombatType.MELEE, EquipmentSlot.CAPE);
        addSkilledWearable("Infernal cape", 1, 1, 1, 1, 11, CombatType.RANGED, EquipmentSlot.CAPE);
        addSkilledWearable("Infernal cape", 1, 1, 1, 11, 1, CombatType.MAGIC, EquipmentSlot.CAPE);

        addSkilledWearable("Steel platelegs", 1, 1, 10, -1, -1, CombatType.MELEE, EquipmentSlot.LEGS);
        addSkilledWearable("Steel platebody", 1, 1, 10, -1, -1,CombatType.MELEE, EquipmentSlot.CHEST);
        addSkilledWearable("Steel kiteshield", 1, 1, 10, -1, -1,CombatType.MELEE, EquipmentSlot.SHIELD);
        addSkilledWearable("Steel full helm", 1, 1, 10, -1, -1,CombatType.MELEE, EquipmentSlot.HAT);

        addSkilledWearable("Mithril platelegs", 1, 1, 20, -1, -1,CombatType.MELEE, EquipmentSlot.LEGS);
        addSkilledWearable("Mithril platebody", 1, 1, 20, -1, -1,CombatType.MELEE, EquipmentSlot.CHEST);
        addSkilledWearable("Mithril kiteshield", 1, 1, 20, -1, -1,CombatType.MELEE, EquipmentSlot.SHIELD);
        addSkilledWearable("Mithril full helm", 1, 1, 20, -1, -1,CombatType.MELEE, EquipmentSlot.HAT);

        addSkilledWearable("Adamant platelegs", 1, 1, 30, -1, -1,CombatType.MELEE, EquipmentSlot.LEGS);
        addSkilledWearable("Adamant platebody", 1, 1, 30, -1, -1,CombatType.MELEE, EquipmentSlot.CHEST);
        addSkilledWearable("Adamant kiteshield", 1, 1, 30, -1, -1,CombatType.MELEE, EquipmentSlot.SHIELD);
        addSkilledWearable("Adamant full helm", 1, 1, 30, -1, -1,CombatType.MELEE, EquipmentSlot.HAT);

        addSkilledWearable("Rune platelegs", 1, 1, 40, -1, -1,CombatType.MELEE, EquipmentSlot.LEGS);
        addSkilledWearable("Rune chainbody", 1, 1, 40, -1, -1,CombatType.MELEE, EquipmentSlot.CHEST);

        addQuestedWearable("Rune platebody", 1, 1, 40, -1, -1, EquipmentSlot.CHEST, CombatType.MELEE,
                Collections.singletonList(FreeQuest.DRAGON_SLAYER));

        addSkilledWearable("Rune kiteshield", 1, 1, 40, -1, -1, CombatType.MELEE, EquipmentSlot.SHIELD);
        addSkilledWearable("Rune full helm", 1, 1, 40, -1, -1, CombatType.MELEE, EquipmentSlot.HAT);

        addSkilledWearable("Dragon platelegs", 1, 1, 60, -1, -1, CombatType.MELEE, EquipmentSlot.LEGS);
        addSkilledWearable("Dragon chainbody", 1, 1, 60, -1, -1, CombatType.MELEE, EquipmentSlot.CHEST);

        addQuestedWearable("Dragon platebody", 1, 1, 61, -1, -1, EquipmentSlot.CHEST, CombatType.MELEE,
                Collections.singletonList(FreeQuest.DRAGON_SLAYER));

        addSkilledWearable("Fighter torso", 1, 1, 62, -1, -1, CombatType.MELEE, EquipmentSlot.CHEST);
        addSkilledWearable("Dragon med helm", 1, 1, 60, -1, -1, CombatType.MELEE, EquipmentSlot.HAT);
        addSkilledWearable("Obsidian helmet", 1, 1, 61, -1, -1, CombatType.MELEE, EquipmentSlot.HAT);

        // other?
        addBasicItem("Coins");
        addBasicItem("Brass key");
        addBasicItem("Knife");

        // food
        addBasicItem("Trout");
        addBasicItem("Lobster");
        addBasicItem("Tuna");
        addBasicItem("Shark");
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

    private void addBasicItem(String name) {
        items.add(new WatItem(name));
    }

    private void addWeapon(String name, CombatType type) {
        items.add(new WatWeapon(name, type));
    }

    private void addWearable(String name, EquipmentSlot slot, CombatType type) {
        items.add(new WatWearable(name, slot, type));
    }

    private void addSkilledWeapon(String name, int attack, int strength, int defence, int magic, int ranged, CombatType type) {
        items.add(new WatWeapon(name, type, new HashMap<Skill, Integer>()
        {
            {
                put(Skill.ATTACK, attack);
                put(Skill.STRENGTH, strength);
                put(Skill.DEFENCE, defence);
                put(Skill.MAGIC, magic);
                put(Skill.RANGED, ranged);
            }
        }));
    }

    private void addQuestedWeapon(String name, int attack, int strength, int defence, int magic, int ranged, CombatType type, List<Quest> quests) {
        items.add(new WatWeapon(name, type, new HashMap<Skill, Integer>()
        {
            {
                put(Skill.ATTACK, attack);
                put(Skill.STRENGTH, strength);
                put(Skill.DEFENCE, defence);
                put(Skill.MAGIC, magic);
                put(Skill.RANGED, ranged);
            }
        }, quests));
    }

    private void addSkilledWearable(String name, int attack, int strength, int defence, int magic, int ranged, CombatType type, EquipmentSlot slot) {
        items.add(new WatWearable(name, slot, type, new HashMap<Skill, Integer>()
        {
            {
                put(Skill.ATTACK, attack);
                put(Skill.STRENGTH, strength);
                put(Skill.DEFENCE, defence);
                put(Skill.MAGIC, magic);
                put(Skill.RANGED, ranged);
            }
        }));
    }

    private void addQuestedWearable(String name, int attack, int strength, int defence, int magic, int ranged, EquipmentSlot slot, CombatType type, List<Quest> quests) {
        items.add(new WatWearable(name, slot, type, new HashMap<Skill, Integer>()
        {
            {
                put(Skill.ATTACK, attack);
                put(Skill.STRENGTH, strength);
                put(Skill.DEFENCE, defence);
                put(Skill.MAGIC, magic);
                put(Skill.RANGED, ranged);
            }
        }, quests));
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

    public WatWeapon getBestWeapon(CombatType type) {
        int availableMoney = Inventory.count("Coins");
        if (Bank.isOpen()) availableMoney += Bank.count("Coins");

        List<WatWeapon> weapons = new ArrayList<>();
        for (WatItem item : items) {
            if (item instanceof WatWeapon) weapons.add((WatWeapon) item);
        }

        return getBestItem(
                weapons,
                weapon -> weapon.getCombatType() == type
                        && meetsSkillAndQuestReqs(weapon.getLevelRequirements(), weapon.getQuestRequirements()),
                weapon -> weapon.getLevelRequirements() == null ? 0 : weapon.getLevelRequirements().values().stream().mapToInt(Integer::intValue).sum(),
                weapon -> (Bank.isOpen() && Bank.contains(weapon.getName())) || Inventory.contains(weapon.getName()) || Equipment.contains(weapon.getName()),
                WatItem::getPrice,
                availableMoney
        );
    }

    public WatWearable getBestWearable(EquipmentSlot slot, CombatType type) {
        int availableMoney = Inventory.count("Coins");
        if (Bank.isOpen()) availableMoney += Bank.count("Coins");

        List<WatWearable> wearables = new ArrayList<>();
        for (WatItem item : items) {
            if (item instanceof WatWearable) wearables.add((WatWearable) item);
        }

        return getBestItem(
                wearables,
                wearable -> wearable.getCombatType() == type
                        && wearable.getSlot() == slot
                        && meetsSkillAndQuestReqs(wearable.getLevelRequirements(), wearable.getQuestRequirements()),
                wearable -> wearable.getLevelRequirements() == null ? 0 : wearable.getLevelRequirements().values().stream().mapToInt(Integer::intValue).sum(),
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
