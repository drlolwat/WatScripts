package org.lolwat.types.interfaces;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.mobs.Mob;

import java.util.HashMap;

public interface MobLogic {
    default void execute(Mob mob, Skill skill) {
        if(!Players.getLocal().isInCombat() && Players.getLocal().getInteractingCharacter() == null) {
            NPC target = NPCs.closest(x -> x != null && x.exists() && !x.isInCombat() && !x.isHealthBarVisible() && x.getName().equals(mob.getName()));
            if (target != null) {
                if(!target.interact("Attack")) {
                    return;
                }

                Sleep.sleepUntil(() -> target.isInteracting(Players.getLocal()), 5000);

                if(!target.isInteracting(Players.getLocal())) {
                    Logger.log("did not get targets attention or its attacking someone else: " + target.getName());
                }

            } else {
                Logger.log("Target not found: " + mob.getName());
            }
        }
    }

    // WatItem, amount
    default HashMap<WatItem, Integer> inventoryLoadout() {
        return new HashMap<>();
    }

    // EquipmentSlot, continueIfCannotObtain
    default HashMap<EquipmentSlot, Boolean> slotsRequired() {
        return new HashMap<EquipmentSlot, Boolean>() {
            {
                put(EquipmentSlot.SHIELD, true);
                put(EquipmentSlot.HAT, true);
                put(EquipmentSlot.CHEST, true);
                put(EquipmentSlot.LEGS, true);
                put(EquipmentSlot.AMULET, true);
                put(EquipmentSlot.CAPE, true);
                put(EquipmentSlot.HANDS, true);
                put(EquipmentSlot.FEET, true);
            }
        };
    }

    default void runPriority(Mob mob, Skill skill) {
        Item i = Inventory.get(x -> x != null && x.hasAction("Eat"));
        if(i != null && Combat.getHealthPercent() <= 50) {
            if(!i.interact("Eat")) {
                Logger.log("Issue eating food during combat task [default interface method]");
            }
        }

        final CombatStyle current = Combat.getCombatStyle();

        if(current == null) {
            Logger.error("combat style was null somehow");
            return;
        }

        CombatStyle needed;

        // TODO MAGIC FIRST ELSE STR
        if(skill.equals(Skill.STRENGTH))
            needed = CombatStyle.STRENGTH;
        else if(skill.equals(Skill.DEFENCE))
            needed = CombatStyle.DEFENCE;
        else if(skill.equals(Skill.ATTACK))
            needed = CombatStyle.ATTACK;
        else if(skill.equals(Skill.RANGED))
            needed = CombatStyle.RANGED_RAPID;
        else
            needed = current;

        if(!current.equals(needed) || !Combat.isAutoRetaliateOn()) {
            if(!Tabs.isOpen(Tab.COMBAT) && !Tabs.open(Tab.COMBAT)) {
                Logger.error("error opening combat tab [default interface method]");
                return;
            }

            Sleep.sleepUntil(() -> Tabs.isOpen(Tab.COMBAT), 5000);

            if(!Combat.setCombatStyle(needed)) {
                Logger.error("error setting combat style to " + needed + " [default interface method]");
                return;
            }

            Sleep.sleepUntil(() -> !Combat.getCombatStyle().equals(current), 5000);

            if(!Combat.toggleAutoRetaliate(true)) {
                Logger.error("error toggling auto retaliate on [default interface method]");
                return;
            }
        }

        if(Dialogues.inDialogue() || Dialogues.canContinue()) {
            DialogueUtils.continueWhilePossible();
            return;
        }

        if(!Tabs.isOpen(Tab.INVENTORY) && !Tabs.open(Tab.INVENTORY)) {
            Logger.error("error opening inventory during combat [default interface method]");
        }
    }
}
