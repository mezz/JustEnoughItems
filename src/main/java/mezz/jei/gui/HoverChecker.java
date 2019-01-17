package mezz.jei.gui;

import net.minecraft.client.gui.GuiButton;

public class HoverChecker {
	private int top;
	private int bottom;
	private int left;
	private int right;

	public void updateBounds(GuiButton button) {
		this.top = button.y;
		this.bottom = button.y + button.height;
		this.left = button.x;
		this.right = button.x + button.width;
	}

	public void updateBounds(int top, int bottom, int left, int right) {
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}

	public boolean checkHover(double mouseX, double mouseY) {
		return mouseY >= top && mouseY <= bottom && mouseX >= left && mouseX <= right;
	}
}