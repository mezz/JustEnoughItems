package mezz.jei.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public interface IJeiKeyMappingInternal extends IJeiKeyMappingWithExtraModifiers {
	KeyMapping getKeyMapping();

	boolean isDown();

	IJeiKeyMappingInternal register(Consumer<KeyMapping> registerMethod);

	static boolean isKeyDown(InputConstants.Key key) {
		if (InputConstants.UNKNOWN.equals(key)) {
			return false;
		}

		Minecraft minecraft = Minecraft.getInstance();
		long windowHandle = minecraft.getWindow().getWindow();
		return switch (key.getType()) {
			case KEYSYM -> InputConstants.isKeyDown(windowHandle, key.getValue());
			case MOUSE -> GLFW.glfwGetMouseButton(windowHandle, key.getValue()) == GLFW.GLFW_PRESS;
			case SCANCODE -> false;
		};
	}
}
