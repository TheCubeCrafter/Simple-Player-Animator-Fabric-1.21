

package com.simpleplayeranimator;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import com.simpleplayeranimator.loader.AnimationLoader;
import com.simpleplayeranimator.trigger.TriggerManager;

public class SimplePlayerAnimator implements ModInitializer, ClientModInitializer {
 public static final String MODID = "simpleplayeranimator";

 @Override
 public void onInitialize() {
  AnimationLoader.loadAnimations();
 }
}
