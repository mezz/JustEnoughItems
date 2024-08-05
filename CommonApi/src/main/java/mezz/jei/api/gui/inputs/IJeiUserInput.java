package mezz.jei.api.gui.inputs;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.api.runtime.IJeiKeyMappings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.events.GuiEventListener;

/**
 * Represents a click or key press.
 *
 * @since 15.9.0
 */
public interface IJeiUserInput {
	/**
	 * Vanilla information about a click or key press.
	 *
	 * @since 15.9.0
	 */
	InputConstants.Key getKey();

	/**
	 * Modifiers passed into methods like {@link GuiEventListener#mouseClicked}
	 *
	 * @since 15.9.0
	 */
	int getModifiers();

	/**
	 * True on mouse down, used to check if a click could be handled.
	 *
	 * False on mouse up and key down: when the input should execute an action.
	 *
	 * Key up is ignored because JEI handles key down immediately.
	 *
	 * @since 15.9.0
	 */
	boolean isSimulate();

	/**
	 * Check if the input matches a given vanilla {@link KeyMapping}.
	 *
	 * @return true if this input and modifiers match the given key mapping.
	 *
	 * @since 15.9.0
	 */
	boolean is(KeyMapping keyMapping);

	/**
	 * Check if the input matches a given {@link IJeiKeyMapping}.
	 * See all the mappings in {@link IJeiKeyMappings}
	 *
	 * @return true if this input and modifiers match the given key mapping.
	 *
	 * @since 15.9.0
	 */
	boolean is(IJeiKeyMapping keyMapping);
}
