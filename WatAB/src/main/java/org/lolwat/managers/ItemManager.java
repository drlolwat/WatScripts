package org.lolwat.managers;

import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.PaidQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.gear.WatWeapon;
import org.lolwat.types.gear.WatWearable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
        addWeapon("Bronze sword");
        addWeapon("Iron scimitar");

        addSkilledWeapon("Shortbow", 1, 1, 1, -1, 1);
        addSkilledWeapon("Staff of Air", 1, 1, 1, 1, -1);

        addSkilledWeapon("Mithril scimitar", 20, 1, 1, -1, -1);
        addSkilledWeapon("Adamant scimitar", 30, 1, 1, -1, -1);
        addSkilledWeapon("Rune scimitar", 40, 1, 1, -1, -1);

        addSkilledWeapon("Dragon sword", 60, 1, 1, -1, -1);

        addQuestedWeapon("Dragon scimitar", 60, 1, 1, -1, -1,
                Collections.singletonList(PaidQuest.MONKEY_MADNESS));

        addSkilledWeapon("Abyssal whip", 70, 1, 1, -1, -1);

        // armor
        addWearable("Wooden shield", EquipmentSlot.SHIELD);
        addWearable("Iron platelegs", EquipmentSlot.LEGS);
        addWearable("Iron platebody", EquipmentSlot.CHEST);
        addWearable("Iron kiteshield", EquipmentSlot.SHIELD);
        addWearable("Iron full helm", EquipmentSlot.HAT);

        addSkilledWearable("Steel platelegs", 1, 1, 10, -1, -1, EquipmentSlot.LEGS);
        addSkilledWearable("Steel platebody", 1, 1, 10, -1, -1, EquipmentSlot.CHEST);
        addSkilledWearable("Steel kiteshield", 1, 1, 10, -1, -1, EquipmentSlot.SHIELD);
        addSkilledWearable("Steel full helm", 1, 1, 10, -1, -1, EquipmentSlot.HAT);

        addSkilledWearable("Mithril platelegs", 1, 1, 20, -1, -1, EquipmentSlot.LEGS);
        addSkilledWearable("Mithril platebody", 1, 1, 20, -1, -1, EquipmentSlot.CHEST);
        addSkilledWearable("Mithril kiteshield", 1, 1, 20, -1, -1, EquipmentSlot.SHIELD);
        addSkilledWearable("Mithril full helm", 1, 1, 20, -1, -1, EquipmentSlot.HAT);

        addSkilledWearable("Adamant platelegs", 1, 1, 30, -1, -1, EquipmentSlot.LEGS);
        addSkilledWearable("Adamant platebody", 1, 1, 30, -1, -1, EquipmentSlot.CHEST);
        addSkilledWearable("Adamant kiteshield", 1, 1, 30, -1, -1, EquipmentSlot.SHIELD);
        addSkilledWearable("Adamant full helm", 1, 1, 30, -1, -1, EquipmentSlot.HAT);

        addSkilledWearable("Rune platelegs", 1, 1, 40, -1, -1, EquipmentSlot.LEGS);
        addSkilledWearable("Rune chainbody", 1, 1, 40, -1, -1, EquipmentSlot.CHEST);

        addQuestedWearable("Rune platebody", 1, 1, 40, -1, -1, EquipmentSlot.CHEST,
                Collections.singletonList(FreeQuest.DRAGON_SLAYER));

        addSkilledWearable("Rune kiteshield", 1, 1, 40, -1, -1, EquipmentSlot.SHIELD);
        addSkilledWearable("Rune full helm", 1, 1, 40, -1, -1, EquipmentSlot.HAT);

        addSkilledWearable("Dragon platelegs", 1, 1, 60, -1, -1, EquipmentSlot.LEGS);
        addSkilledWearable("Dragon chainbody", 1, 1, 60, -1, -1, EquipmentSlot.CHEST);

        addQuestedWearable("Dragon platebody", 1, 1, 60, -1, -1, EquipmentSlot.CHEST,
                Collections.singletonList(FreeQuest.DRAGON_SLAYER));

        addSkilledWearable("Dragon med helm", 1, 1, 60, -1, -1, EquipmentSlot.HAT);

        // other?
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

    private void addWeapon(String name) {
        items.add(new WatWeapon(name));
    }

    private void addWearable(String name, EquipmentSlot slot) {
        items.add(new WatWearable(name, slot));
    }

    private void addSkilledWeapon(String name, int attack, int strength, int defence, int magic, int ranged) {
        items.add(new WatWeapon(name, new HashMap<Skill, Integer>()
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

    private void addQuestedWeapon(String name, int attack, int strength, int defence, int magic, int ranged, List<Quest> quests) {
        items.add(new WatWeapon(name, new HashMap<Skill, Integer>()
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

    private void addSkilledWearable(String name, int attack, int strength, int defence, int magic, int ranged, EquipmentSlot slot) {
        items.add(new WatWearable(name, slot, new HashMap<Skill, Integer>()
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

    private void addQuestedWearable(String name, int attack, int strength, int defence, int magic, int ranged, EquipmentSlot slot, List<Quest> quests) {
        items.add(new WatWearable(name, slot, new HashMap<Skill, Integer>()
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
}
