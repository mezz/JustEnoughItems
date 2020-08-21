package mezz.jei.gui;

import net.minecraft.client.gui.widget.button.Button;

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
		this.bottom = button.y + button.getWidth_CLASH(); // TODO: bad MCP name for getHeight
		this.left = button.x;
		this.right = button.x + button.getWidth();
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