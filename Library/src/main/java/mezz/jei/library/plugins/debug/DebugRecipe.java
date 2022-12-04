package mezz.jei.library.plugins.debug;

import net.minecraft.client.gui.components.Button;

import net.minecraft.network.chat.Component;

public class DebugRecipe {
	private final Button button;

	public DebugRecipe() {
		this.button = new Button(0, 0, 40, 20, Component.literal("test"), b -> {
		});
	}

	public Button getButton() {
		return button;
	}

	public boolean checkHover(double mouseX, double mouseY) {
		return this.button.isMouseOver(mouseX, mouseY);
	}
}
