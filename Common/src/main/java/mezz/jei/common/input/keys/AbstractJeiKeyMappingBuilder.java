package mezz.jei.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractJeiKeyMappingBuilder implements IJeiKeyMappingBuilder {
    protected abstract IJeiKeyMappingInternal buildMouse(int mouseButton);

    @Override
    public final IJeiKeyMappingInternal buildMouseLeft() {
        return buildMouse(InputConstants.MOUSE_BUTTON_LEFT);
    }

    @Override
    public final IJeiKeyMappingInternal buildMouseRight() {
        return buildMouse(InputConstants.MOUSE_BUTTON_RIGHT);
    }

    @Override
    public final IJeiKeyMappingInternal buildMouseMiddle() {
        return buildMouse(InputConstants.MOUSE_BUTTON_MIDDLE);
    }

    @Override
    public final IJeiKeyMappingInternal buildUnbound() {
        return buildKeyboardKey(GLFW.GLFW_KEY_UNKNOWN);
    }
}
