package mezz.jei.util;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Render {

	private static final String[] renderToolTip = new String[]{"func_146285_a", "renderToolTip"};

    public static void renderToolTip(ItemStack itemStack, int mouseX, int mouseY) {
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		try {
			Method m = ReflectionHelper.findMethod(GuiScreen.class, screen, renderToolTip, ItemStack.class, int.class, int.class);
			m.invoke(screen, itemStack, mouseX, mouseY);
			RenderHelper.disableStandardItemLighting();
		} catch (InvocationTargetException e) {
			Log.debug("Bad tooltip for item: " + itemStack);
		} catch (IllegalAccessException e) {
			Log.error("Failed invocation when trying to create a tooltip");
		}
    }
}
