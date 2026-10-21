package mezz.jei.gui.input.focus;

import net.minecraft.client.gui.components.events.GuiEventListener;

/** A JEI control with explicit keyboard focus on Minecraft 1.19. */
public interface IKeyboardFocusTarget extends GuiEventListener {
	void setKeyboardFocus(boolean focused);

	boolean hasKeyboardFocus();
}
