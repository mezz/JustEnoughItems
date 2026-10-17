package mezz.jei.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

import java.util.function.Consumer;

public interface IJeiKeyMappingInternal extends IJeiKeyMappingWithExtraModifiers {
	KeyMapping getKeyMapping();

	boolean isDown();

	IJeiKeyMappingInternal register(Consumer<KeyMapping> registerMethod);

	static boolean isKeyDown(InputConstants.Key key) {
		if (InputConstants.UNKNOWN.equals(key)) {
			return false;
		}

		return switch (key.getType()) {
			case KEYBOARD -> InputConstants.isKeyDown(key.getValue());
			case MOUSE -> isMouseButtonDown(Minecraft.getInstance(), key.getValue());
		};
	}

	private static boolean isMouseButtonDown(Minecraft minecraft, int mouseButton) {
		return switch (mouseButton) {
			case InputConstants.MOUSE_BUTTON_LEFT -> minecraft.mouseHandler.isLeftPressed();
			case InputConstants.MOUSE_BUTTON_MIDDLE -> minecraft.mouseHandler.isMiddlePressed();
			case InputConstants.MOUSE_BUTTON_RIGHT -> minecraft.mouseHandler.isRightPressed();
			default -> false;
		};
	}
}
