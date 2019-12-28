package mezz.jei.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;

public final class MouseUtil {
	public static double getX() {
		Minecraft minecraft = Minecraft.getInstance();
		MouseHelper mouseHelper = minecraft.mouseHelper;
		double scale = (double) minecraft.func_228018_at_().getScaledWidth() / (double) minecraft.func_228018_at_().getWidth();
		return mouseHelper.getMouseX() * scale;
	}

	public static double getY() {
		Minecraft minecraft = Minecraft.getInstance();
		MouseHelper mouseHelper = minecraft.mouseHelper;
		double scale = (double) minecraft.func_228018_at_().getScaledHeight() / (double) minecraft.func_228018_at_().getHeight();
		return mouseHelper.getMouseY() * scale;
	}
}
