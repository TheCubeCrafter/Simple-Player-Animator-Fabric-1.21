// --- AnimationLoader.java ---
package com.simpleplayeranimator.loader;

import com.google.gson.Gson;
import com.simpleplayeranimator.model.AnimationParser;
import com.simpleplayeranimator.player.AnimationRegistry;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;

public class AnimationLoader {

    public static void loadAnimations() {
        Path animationFolder = FabricLoader.getInstance().getConfigDir()
                .resolve("simpleplayeranimator").resolve("spa_animations");
        File dir = animationFolder.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return;

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                KeyframeAnimation anim = AnimationParser.parse(new Gson().fromJson(reader, com.google.gson.JsonObject.class));
                if (anim != null && anim.name != null) {
                    AnimationRegistry.register(anim.name, anim);
                }
            } catch (Exception e) {
                System.err.println("[SPA] Failed to load animation: " + file.getName());
                e.printStackTrace();
            }
        }
    }
}
