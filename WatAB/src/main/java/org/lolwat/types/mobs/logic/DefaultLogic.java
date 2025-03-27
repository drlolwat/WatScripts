package org.lolwat.types.mobs.logic;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.lolwat.types.interfaces.MobLogic;
import org.lolwat.types.mobs.Mob;

public class DefaultLogic implements MobLogic {
    @Override
    public void execute(Mob mob, Skill skill) {
        Logger.log("executing mob logic " + this.getClass().getSimpleName() + " for " + mob.getName() + " for skill " + skill.getName());
    }
}
