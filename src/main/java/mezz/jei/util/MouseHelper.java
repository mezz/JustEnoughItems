package mezz.jei.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

public class MouseHelper {

	private final ScaledResolution scaledresolution;
	private final int displayWidth;
	private final int displayHeight;

	public MouseHelper(Minecraft minecraft) {
		displayWidth = minecraft.displayWidth;
		displayHeight = minecraft.displayHeight;
		scaledresolution = new ScaledResolution(minecraft, displayWidth, displayHeight);
	}

	public int getX() {
		int i = scaledresolution.getScaledWidth();
		return Mouse.getX() * i / displayWidth;
	}

	public int getY() {
		int j = scaledresolution.getScaledHeight();
		return j - Mouse.getY() * j / displayHeight - 1;
	}
}
