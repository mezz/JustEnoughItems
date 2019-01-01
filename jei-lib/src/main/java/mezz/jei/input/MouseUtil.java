package mezz.jei.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;

public final class MouseUtil {
	public static double getX() {
		Minecraft minecraft = Minecraft.getInstance();
		MouseHelper mouseHelper = minecraft.mouseHelper;
		double scale = (double) minecraft.mainWindow.getScaledWidth() / (double) minecraft.mainWindow.getWidth();
		return mouseHelper.getMouseX() * scale;
	}

	public static double getY() {
		Minecraft minecraft = Minecraft.getInstance();
		MouseHelper mouseHelper = minecraft.mouseHelper;
		double scale = (double) minecraft.mainWindow.getScaledHeight() / (double) minecraft.mainWindow.getHeight();
		return mouseHelper.getMouseY() * scale;
	}
}
