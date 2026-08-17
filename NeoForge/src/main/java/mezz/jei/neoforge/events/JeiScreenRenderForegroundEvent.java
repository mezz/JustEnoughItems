package mezz.jei.neoforge.events;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.Event;

/**
 * Bridges the missing NeoForge 1.21.11 foreground event for non-container screens.
 */
public class JeiScreenRenderForegroundEvent extends Event {
	private final Screen screen;
	private final GuiGraphics guiGraphics;
	private final int mouseX;
	private final int mouseY;

	public JeiScreenRenderForegroundEvent(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		this.screen = screen;
		this.guiGraphics = guiGraphics;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public Screen getScreen() {
		return screen;
	}

	public GuiGraphics getGuiGraphics() {
		return guiGraphics;
	}

	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}
}
