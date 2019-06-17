package mezz.jei.plugins.debug;

import net.minecraftforge.fml.client.config.GuiButtonExt;

import mezz.jei.gui.HoverChecker;

public class DebugRecipe {
	private final GuiButtonExt button;
	private final HoverChecker buttonHoverChecker;

	public DebugRecipe() {
		this.button = new GuiButtonExt(0, 0, 40, 20, "test", b -> {});
		this.buttonHoverChecker = new HoverChecker();
		this.buttonHoverChecker.updateBounds(this.button);
	}

	public GuiButtonExt getButton() {
		return button;
	}

	public boolean checkHover(double mouseX, double mouseY) {
		return buttonHoverChecker.checkHover(mouseX, mouseY);
	}
}
