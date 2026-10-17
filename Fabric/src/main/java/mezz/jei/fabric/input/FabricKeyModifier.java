package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

/** Physical modifiers used by JEI's standalone Fabric key mappings. */
public enum FabricKeyModifier {
	ALT(InputConstants.MOD_ALT, "jei.key.combo.alt"),
	SHIFT(InputConstants.MOD_SHIFT, "jei.key.combo.shift"),
	CONTROL(InputConstants.MOD_CONTROL, "jei.key.combo.control"),
	COMMAND(InputConstants.MOD_SUPER, "jei.key.combo.command");

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
				if (InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY) {
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
			case InputConstants.KEY_LGUI, InputConstants.KEY_RGUI -> COMMAND;
			default -> null;
		};
	}

	public static EnumSet<FabricKeyModifier> current() {
		Minecraft minecraft = Minecraft.getInstance();
		EnumSet<FabricKeyModifier> modifiers = EnumSet.noneOf(FabricKeyModifier.class);
		if (minecraft.hasShiftDown()) {
			modifiers.add(SHIFT);
		}
		if (minecraft.hasControlDown()) {
			modifiers.add(CONTROL);
		}
		if (minecraft.hasAltDown()) {
			modifiers.add(ALT);
		}
		if (InputConstants.isKeyDown(InputConstants.KEY_LGUI) || InputConstants.isKeyDown(InputConstants.KEY_RGUI)) {
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
