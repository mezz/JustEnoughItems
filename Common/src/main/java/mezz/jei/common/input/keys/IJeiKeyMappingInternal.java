package mezz.jei.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.runtime.IJeiKeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public interface IJeiKeyMappingInternal extends IJeiKeyMapping {
	@Override
	boolean isActiveAndMatches(InputConstants.Key key);

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
		long windowHandle = minecraft.getWindow().handle();
		return switch (key.getType()) {
			case KEYSYM -> InputConstants.isKeyDown(minecraft.getWindow(), key.getValue());
			case MOUSE -> GLFW.glfwGetMouseButton(windowHandle, key.getValue()) == GLFW.GLFW_PRESS;
			case SCANCODE -> false;
		};
	}
}
