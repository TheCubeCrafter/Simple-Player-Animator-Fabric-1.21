// --- EventTracker.java ---
package com.simpleplayeranimator.trigger;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class EventTracker {

    private static boolean wasSneaking = false;
    private static boolean wasSprinting = false;
    private static boolean wasSwinging = false;
    private static boolean wasJumping = false;
    private static boolean wasEating = false;
    private static boolean wasDrinking = false;
    private static boolean wasInWater = false;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            ClientPlayerEntity player = client.player;

            if (!wasSneaking && player.isSneaking()) {
                TriggerManager.registerEvent("sneak");
            }
            wasSneaking = player.isSneaking();

            if (!wasSprinting && player.isSprinting()) {
                TriggerManager.registerEvent("sprint");
            }
            wasSprinting = player.isSprinting();

            if (!wasSwinging && player.handSwinging) {
                TriggerManager.registerEvent("swing");
            }
            wasSwinging = player.handSwinging;

            if (!wasJumping && player.getVelocity().y > 0 && player.isOnGround() == false) {
                TriggerManager.registerEvent("jump");
            }
            wasJumping = player.getVelocity().y > 0;

            ItemStack item = player.getMainHandStack();
            boolean using = player.isUsingItem();

            if (using) {
                if (item.getItem().isFood()) {
                    if (!wasEating) TriggerManager.registerEvent("eat");
                    wasEating = true;
                } else if (item.getItem() == Items.POTION) {
                    if (!wasDrinking) TriggerManager.registerEvent("drink");
                    wasDrinking = true;
                }
            } else {
                wasEating = false;
                wasDrinking = false;
            }

            if (!wasInWater && player.isTouchingWater()) {
                TriggerManager.registerEvent("swim");
            }
            wasInWater = player.isTouchingWater();
        });
    }
}
