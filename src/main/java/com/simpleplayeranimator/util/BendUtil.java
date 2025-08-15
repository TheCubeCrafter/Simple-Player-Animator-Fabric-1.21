
package com.simpleplayeranimator.util;

import io.github.kosmx.bendylib.impl.IBendable;
import net.minecraft.client.model.ModelPart;

public class BendUtil {
    public static void applyBend(ModelPart part, float bendValue, int direction) {
        if (part instanceof IBendable bendable) {
            bendable.setBend(bendValue, direction);
        }
    }
}
