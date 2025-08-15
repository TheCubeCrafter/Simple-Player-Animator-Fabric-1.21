
package com.simpleplayeranimator.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kosmx.playerAnim.core.data.*;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.VecBuilder;

import java.util.Set;

public class AnimationParser {
    private static final Set<String> bendableParts = Set.of("rightArm", "leftArm", "rightLeg", "leftLeg");

    public static KeyframeAnimation parse(JsonObject json) {
        KeyframeAnimation anim = new KeyframeAnimation();
        JsonObject emote = json.getAsJsonObject("emote");
        boolean bendDegrees = emote.has("bendDegrees") && emote.get("bendDegrees").getAsBoolean();
        JsonArray moves = emote.getAsJsonArray("moves");

        for (JsonElement elem : moves) {
            JsonObject move = elem.getAsJsonObject();
            int tick = move.get("tick").getAsInt();
            Ease easing = Ease.valueOf(move.get("easing").getAsString());

            for (String part : new String[]{"head","torso","rightArm","leftArm","rightLeg","leftLeg","rightItem","leftItem"}) {
                if (move.has(part)) {
                    JsonObject p = move.getAsJsonObject(part);
                    VecBuilder vec = new VecBuilder(
                        p.has("x") ? p.get("x").getAsFloat() : 0f,
                        p.has("y") ? p.get("y").getAsFloat() : 0f,
                        p.has("z") ? p.get("z").getAsFloat() : 0f);
                    VecBuilder rot = new VecBuilder(
                        p.has("pitch") ? p.get("pitch").getAsFloat() : 0f,
                        p.has("yaw") ? p.get("yaw").getAsFloat() : 0f,
                        p.has("roll") ? p.get("roll").getAsFloat() : 0f);

                    anim.getPart(part).offset.setKeyframe(tick, vec, easing);
                    anim.getPart(part).rotation.setKeyframe(tick, rot, easing);

                    if (p.has("bend") && bendableParts.contains(part)) {
                        float bend = p.get("bend").getAsFloat();
                        if (bendDegrees) bend *= (float) Math.PI / 180f;
                        anim.getPart(part).customData.put("bend", bend);
                    }
                }
            }
        }

        anim.endTick = emote.get("stopTick").getAsInt();
        anim.beginTick = emote.get("beginTick").getAsInt();
        anim.loop = emote.get("isLoop").getAsBoolean();
        return anim;
    }
}
