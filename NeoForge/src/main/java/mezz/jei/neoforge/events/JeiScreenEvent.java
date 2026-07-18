package mezz.jei.neoforge.events;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.bus.api.Event;

public class JeiScreenEvent extends Event {
	private final AbstractContainerScreen<?> screen;
	private final GuiGraphicsExtractor guiGraphics;
	private final int mouseX;
	private final int mouseY;

	protected JeiScreenEvent(AbstractContainerScreen<?> screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		this.screen = screen;
		this.guiGraphics = guiGraphics;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public AbstractContainerScreen<?> getScreen() {
		return screen;
	}

	public GuiGraphicsExtractor getGuiGraphics() {
		return guiGraphics;
	}

	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}

	public static final class RenderForeground extends JeiScreenEvent {
		public RenderForeground(AbstractContainerScreen<?> screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
			super(screen, guiGraphics, mouseX, mouseY);
		}
	}
}
