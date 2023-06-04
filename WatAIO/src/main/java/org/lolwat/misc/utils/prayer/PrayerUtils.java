package org.lolwat.misc.utils.prayer;

import org.lolwat.misc.types.prayer.BoneType;

public class PrayerUtils {
    public static String getBonesFromType(BoneType type) {
        switch(type) {
            default: return null;
            case BIGBONES: return "Big bones";
        }
    }
}
