package mezz.jei.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Render {

    public static void renderToolTip(ItemStack itemStack, int mouseX, int mouseY) {
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		try {
			Method m = GuiScreen.class.getDeclaredMethod("renderToolTip", ItemStack.class, int.class, int.class);
			m.setAccessible(true);
			m.invoke(screen, itemStack, mouseX, mouseY);
			m.setAccessible(false);
		} catch (InvocationTargetException e) {
			Log.debug("Bad tooltip for item: " + itemStack);
		} catch (ReflectiveOperationException e) {
			Log.error("Failed reflection when trying to create a tooltip");
		}
    }
}
