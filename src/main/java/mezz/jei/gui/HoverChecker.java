package mezz.jei.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;

public class HoverChecker {
	private int top;
	private int bottom;
	private int left;
	private int right;

	public HoverChecker() {
	}

	public HoverChecker(int top, int bottom, int left, int right) {
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}

	public void updateBounds(Button button) {
		this.top = button.y;
		this.bottom = button.y + button.getHeight();
		this.left = button.x;
		this.right = button.x + button.getWidth();
	}

	public void updateBounds(int top, int bottom, int left, int right) {
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}

	public void updateBounds(Rect2i rect) {
		this.top = rect.getY();
		this.bottom = rect.getY() + rect.getHeight();
		this.left = rect.getX();
		this.right = rect.getX() + rect.getWidth();
	}

	public boolean checkHover(double mouseX, double mouseY) {
		return mouseY >= top && mouseY <= bottom && mouseX >= left && mouseX <= right;
	}
}
