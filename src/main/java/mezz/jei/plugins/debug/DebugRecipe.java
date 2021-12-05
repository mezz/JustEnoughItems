package mezz.jei.plugins.debug;

import net.minecraft.network.chat.TextComponent;

import mezz.jei.gui.HoverChecker;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class DebugRecipe {
	private final ExtendedButton button;
	private final HoverChecker buttonHoverChecker;

	public DebugRecipe() {
		this.button = new ExtendedButton(0, 0, 40, 20, new TextComponent("test"), b -> {
		});
		this.buttonHoverChecker = new HoverChecker();
		this.buttonHoverChecker.updateBounds(this.button);
	}

	public ExtendedButton getButton() {
		return button;
	}

	public boolean checkHover(double mouseX, double mouseY) {
		return buttonHoverChecker.checkHover(mouseX, mouseY);
	}
}
