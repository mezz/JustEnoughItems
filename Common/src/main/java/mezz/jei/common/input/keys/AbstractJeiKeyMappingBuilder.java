package mezz.jei.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractJeiKeyMappingBuilder implements IJeiKeyMappingBuilder {
    protected abstract IJeiKeyMapping buildMouse(int mouseButton);

    @Override
    public final IJeiKeyMapping buildMouseLeft() {
        return buildMouse(InputConstants.MOUSE_BUTTON_LEFT);
    }

    @Override
    public final IJeiKeyMapping buildMouseRight() {
        return buildMouse(InputConstants.MOUSE_BUTTON_RIGHT);
    }

    @Override
    public final IJeiKeyMapping buildMouseMiddle() {
        return buildMouse(InputConstants.MOUSE_BUTTON_MIDDLE);
    }

    @Override
    public final IJeiKeyMapping buildUnbound() {
        return buildKeyboardKey(GLFW.GLFW_KEY_UNKNOWN);
    }
}
