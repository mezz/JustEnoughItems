package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.fabric.input.FabricKeyMapping;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;

/** Temporarily rebinds a key without discarding the active backend's modifier settings. */
final class KeyMappingTestHelper {
	private KeyMappingTestHelper() {}

	static KeyMapping find(String name) {
		return java.util.Arrays.stream(net.minecraft.client.Minecraft.getInstance().options.keyMappings)
			.filter(mapping -> mapping.getName().equals(name)).findFirst().orElseThrow();
	}

	static Runnable rebindUnmodified(KeyMapping mapping, InputConstants.Key key) {
		InputConstants.Key originalKey = InputConstants.getKey(mapping.saveString());
		Runnable restoreModifiers;
		if (mapping instanceof FabricKeyMapping fabricMapping) {
			int modifiers = fabricMapping.getModifiers();
			restoreModifiers = () -> fabricMapping.setModifiers(modifiers);
			fabricMapping.setKeyAndModifiers(key, 0);
		} else {
			if (FabricLoader.getInstance().isModLoaded("amecs_key_modifiers")) {
				restoreModifiers = AmecsKeyModifiersClientGameTestHelper.saveModifiers(mapping);
			} else {
				restoreModifiers = () -> {};
			}
			mapping.setKey(InputConstants.UNKNOWN);
			mapping.setKey(key);
		}
		KeyMapping.resetMapping();
		return () -> {
			mapping.setKey(originalKey);
			restoreModifiers.run();
			KeyMapping.resetMapping();
		};
	}
}
