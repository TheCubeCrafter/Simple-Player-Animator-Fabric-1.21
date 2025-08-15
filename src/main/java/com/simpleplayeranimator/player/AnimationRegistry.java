
package com.simpleplayeranimator.player;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;

import java.util.HashMap;
import java.util.Map;

public class AnimationRegistry {
    private static final Map<String, KeyframeAnimation> animations = new HashMap<>();

    public static void register(String name, KeyframeAnimation anim) {
        animations.put(name, anim);
    }

    public static KeyframeAnimation get(String name) {
        return animations.get(name);
    }
}
