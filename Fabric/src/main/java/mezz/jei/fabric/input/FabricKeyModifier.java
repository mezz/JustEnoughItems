package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.EnumSet;
import java.util.Set;

/** Physical modifiers used by JEI's standalone Fabric key mappings. */
public enum FabricKeyModifier {
	ALT(GLFW.GLFW_MOD_ALT, "jei.key.combo.alt"),
	SHIFT(GLFW.GLFW_MOD_SHIFT, "jei.key.combo.shift"),
	CONTROL(GLFW.GLFW_MOD_CONTROL, "jei.key.combo.control"),
	COMMAND(GLFW.GLFW_MOD_SUPER, "jei.key.combo.command");

	private final int mask;
	private final String translationKey;

	FabricKeyModifier(int mask, String translationKey) {
		this.mask = mask;
		this.translationKey = translationKey;
	}

	/** Reads Minecraft event flags or the legacy options format. */
	public static EnumSet<FabricKeyModifier> fromMask(int mask) {
		EnumSet<FabricKeyModifier> modifiers = EnumSet.noneOf(FabricKeyModifier.class);
		for (FabricKeyModifier modifier : values()) {
			if ((mask & modifier.mask) != 0) {
				modifiers.add(modifier);
			}
		}
		return modifiers;
	}

	/** Writes the existing options format so saved bindings remain compatible. */
	public static int toMask(Set<FabricKeyModifier> modifiers) {
		int mask = 0;
		for (FabricKeyModifier modifier : modifiers) {
			mask |= modifier.mask;
		}
		return mask;
	}

	public static Set<FabricKeyModifier> from(JeiKeyModifier modifier) {
		return switch (modifier) {
			case NONE -> Set.of();
			case SHIFT -> Set.of(SHIFT);
			case CONTROL -> Set.of(CONTROL);
			case ALT -> Set.of(ALT);
			case CONTROL_OR_COMMAND -> {
				if (Minecraft.ON_OSX) {
					yield Set.of(COMMAND);
				}
				yield Set.of(CONTROL);
			}
		};
	}

	public static @Nullable FabricKeyModifier forKey(int key) {
		return switch (key) {
			case InputConstants.KEY_LSHIFT, InputConstants.KEY_RSHIFT -> SHIFT;
			case InputConstants.KEY_LCONTROL, InputConstants.KEY_RCONTROL -> CONTROL;
			case InputConstants.KEY_LALT, InputConstants.KEY_RALT -> ALT;
			case GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER -> COMMAND;
			default -> null;
		};
	}

	public static EnumSet<FabricKeyModifier> current() {
		Minecraft minecraft = Minecraft.getInstance();
		EnumSet<FabricKeyModifier> modifiers = EnumSet.noneOf(FabricKeyModifier.class);
		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), InputConstants.KEY_LSHIFT) || InputConstants.isKeyDown(minecraft.getWindow().getWindow(), InputConstants.KEY_RSHIFT)) {
			modifiers.add(SHIFT);
		}
		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), InputConstants.KEY_LCONTROL) || InputConstants.isKeyDown(minecraft.getWindow().getWindow(), InputConstants.KEY_RCONTROL)) {
			modifiers.add(CONTROL);
		}
		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), InputConstants.KEY_LALT) || InputConstants.isKeyDown(minecraft.getWindow().getWindow(), InputConstants.KEY_RALT)) {
			modifiers.add(ALT);
		}
		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SUPER) || InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SUPER)) {
			modifiers.add(COMMAND);
		}
		return modifiers;
	}

	public static Component getCombinedName(Set<FabricKeyModifier> modifiers, Component keyName) {
		for (FabricKeyModifier modifier : values()) {
			if (modifiers.contains(modifier)) {
				keyName = Component.translatable(modifier.translationKey, keyName);
			}
		}
		return keyName;
	}
}
