// --- TriggerManager.java ---
package com.simpleplayeranimator.trigger;

import com.google.gson.*;
import com.simpleplayeranimator.player.AnimationRegistry;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.PlayerAnimationAccess;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class TriggerManager implements ClientModInitializer {

    private static class TriggerData {
        String modId;
        String animation;
        String type;
        String event;
        String packetId;
        JsonObject condition;
        int interval;
        int lastTick;
    }

    private static final List<TriggerData> triggers = new ArrayList<>();
    private static final Set<String> activeEvents = new HashSet<>();

    @Override
    public void onInitializeClient() {
        loadTriggers();
        EventTracker.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            int age = client.player.age;

            for (TriggerData data : triggers) {
                if (!"event".equals(data.type)) continue;
                if (!FabricLoader.getInstance().isModLoaded(data.modId)) continue;
                if (!activeEvents.contains(data.event)) continue;

                if (data.interval == 0 || (age - data.lastTick) >= data.interval) {
                    var anim = AnimationRegistry.get(data.animation);
                    if (anim != null) {
                        PlayerAnimationAccess.getPlayerAnimLayer(client.player)
                                .setAnimation(new KeyframeAnimationPlayer(anim));
                        data.lastTick = age;
                    }
                }
            }
            activeEvents.clear();
        });
    }

    public static void registerEvent(String eventId) {
        activeEvents.add(eventId);
    }

    public static void loadTriggers() {
        try {
            Path triggerFolder = FabricLoader.getInstance().getConfigDir()
                    .resolve("simpleplayeranimator").resolve("spa_triggers");
            File dir = triggerFolder.toFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files == null) return;

            for (File file : files) {
                JsonArray arr = JsonParser.parseReader(new FileReader(file)).getAsJsonArray();
                for (JsonElement e : arr) {
                    JsonObject obj = e.getAsJsonObject();
                    JsonObject trig = obj.getAsJsonObject("trigger");
                    TriggerData data = new TriggerData();
                    data.modId = obj.get("modId").getAsString();
                    data.animation = obj.get("animation").getAsString();
                    data.type = trig.has("type") ? trig.get("type").getAsString() : "packet";
                    data.packetId = trig.has("packetId") ? trig.get("packetId").getAsString() : null;
                    data.event = trig.has("event") ? trig.get("event").getAsString() : null;
                    data.condition = trig;
                    data.interval = obj.has("interval") ? obj.get("interval").getAsInt() : 0;
                    data.lastTick = 0;
                    triggers.add(data);

                    if ("packet".equals(data.type) && data.packetId != null) {
                        Identifier channel = new Identifier(data.packetId);
                        ClientPlayNetworking.registerGlobalReceiver(channel, (client, handler, buf, responseSender) -> {
                            client.execute(() -> handlePacketTrigger(client, data, buf));
                        });
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SPA] Failed to load spa_triggers");
            e.printStackTrace();
        }
    }

    private static void handlePacketTrigger(MinecraftClient client, TriggerData data, PacketByteBuf buf) {
        if (client.player == null) return;
        if (!FabricLoader.getInstance().isModLoaded(data.modId)) return;

        for (Map.Entry<String, JsonElement> entry : data.condition.entrySet()) {
            String key = entry.getKey();
            if ("packetId".equals(key) || "type".equals(key)) continue;

            if (!buf.isReadable()) return;

            JsonElement expected = entry.getValue();
            boolean matches = false;

            if (expected.isJsonPrimitive()) {
                JsonPrimitive prim = expected.getAsJsonPrimitive();
                if (prim.isNumber()) {
                    int value = buf.readInt();
                    matches = value == prim.getAsInt();
                } else if (prim.isBoolean()) {
                    boolean value = buf.readBoolean();
                    matches = value == prim.getAsBoolean();
                } else if (prim.isString()) {
                    String value = buf.readString();
                    matches = value.equals(prim.getAsString());
                }
            }

            if (!matches) return;
        }

        int age = client.player.age;
        if (data.interval == 0 || (age - data.lastTick) >= data.interval) {
            var anim = AnimationRegistry.get(data.animation);
            if (anim != null) {
                PlayerAnimationAccess.getPlayerAnimLayer(client.player)
                        .setAnimation(new KeyframeAnimationPlayer(anim));
                data.lastTick = age;
            }
        }
    }
}
