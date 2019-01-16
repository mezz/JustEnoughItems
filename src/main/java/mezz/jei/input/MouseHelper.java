package mezz.jei.input;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import mezz.jei.config.Constants;
import org.lwjgl.input.Mouse;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = {Side.CLIENT})
public final class MouseHelper {
	private static class DisplayInfo {
		private final ScaledResolution scaledresolution;
		private final int displayWidth;
		private final int displayHeight;

		public DisplayInfo() {
			Minecraft minecraft = Minecraft.getMinecraft();
			displayWidth = minecraft.displayWidth;
			displayHeight = minecraft.displayHeight;
			scaledresolution = new ScaledResolution(minecraft);
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

	private static DisplayInfo INFO = new DisplayInfo();

	private MouseHelper() {

	}

	public static int getX() {
		return INFO.getX();
	}

	public static int getY() {
		return INFO.getY();
	}

	@SubscribeEvent
	public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
		INFO = new DisplayInfo();
	}
}
