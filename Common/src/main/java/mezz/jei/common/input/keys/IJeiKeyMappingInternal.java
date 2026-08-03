package mezz.jei.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public interface IJeiKeyMappingInternal extends IJeiKeyMappingWithExtraModifiers {
	@Override
	boolean isActiveAndMatches(InputConstants.Key key);

	@Override
	default boolean isActiveAndMatchesAllowingExtraModifiers(InputConstants.Key key) {
		return isActiveAndMatches(key);
	}

	@Override
	boolean isUnbound();

	@Override
	Component getTranslatedKeyMessage();

	boolean isDown();

	IJeiKeyMappingInternal register(Consumer<KeyMapping> registerMethod);

	static boolean isKeyDown(InputConstants.Key key) {
		if (InputConstants.UNKNOWN.equals(key)) {
			return false;
		}

		Minecraft minecraft = Minecraft.getInstance();
		long windowHandle = minecraft.getWindow().getWindow();
		return switch (key.getType()) {
			case KEYSYM -> isKeysymDown(windowHandle, key.getValue());
			case MOUSE -> GLFW.glfwGetMouseButton(windowHandle, key.getValue()) == GLFW.GLFW_PRESS;
			case SCANCODE -> false;
		};
	}

	private static boolean isKeysymDown(long windowHandle, int keyValue) {
		return switch (keyValue) {
			case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT ->
				isEitherKeysymDown(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT);
			case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL ->
				isEitherKeysymDown(windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL);
			case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT ->
				isEitherKeysymDown(windowHandle, GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT);
			case GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER ->
				isEitherKeysymDown(windowHandle, GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER);
			default -> InputConstants.isKeyDown(windowHandle, keyValue);
		};
	}

	private static boolean isEitherKeysymDown(long windowHandle, int leftKeyValue, int rightKeyValue) {
		return InputConstants.isKeyDown(windowHandle, leftKeyValue) ||
			InputConstants.isKeyDown(windowHandle, rightKeyValue);
	}
}
