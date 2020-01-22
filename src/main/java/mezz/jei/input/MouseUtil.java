package mezz.jei.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;

public final class MouseUtil {
	public static double getX() {
		Minecraft minecraft = Minecraft.getInstance();
		MouseHelper mouseHelper = minecraft.mouseHelper;
		double scale = (double) minecraft.getMainWindow().getScaledWidth() / (double) minecraft.getMainWindow().getWidth();
		return mouseHelper.getMouseX() * scale;
	}

	public static double getY() {
		Minecraft minecraft = Minecraft.getInstance();
		MouseHelper mouseHelper = minecraft.mouseHelper;
		double scale = (double) minecraft.getMainWindow().getScaledHeight() / (double) minecraft.getMainWindow().getHeight();
		return mouseHelper.getMouseY() * scale;
	}
}
