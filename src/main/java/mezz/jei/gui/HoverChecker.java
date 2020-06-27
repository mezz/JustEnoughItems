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
		this.top = button.field_230691_m_;
		this.bottom = button.field_230691_m_ + button.getHeight();
		this.left = button.field_230690_l_;
		this.right = button.field_230690_l_ + button.func_230998_h_();
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