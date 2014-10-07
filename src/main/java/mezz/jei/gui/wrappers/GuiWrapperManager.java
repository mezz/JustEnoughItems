package mezz.jei.gui.wrappers;

import mezz.jei.util.Log;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class GuiWrapperManager {

	private HashMap<Class<? extends GuiScreen>, Class<? extends GuiScreen>> wrappers = new HashMap<Class<? extends GuiScreen>, Class<? extends GuiScreen>>();

	public GuiWrapperManager() {
		registerWrapper(GuiCrafting.class, GuiCraftingWrapper.class);
		registerWrapper(GuiChest.class, GuiChestWrapper.class);
		registerWrapper(GuiInventory.class, GuiInventoryWrapper.class);
	}

	public void registerWrapper(Class<? extends GuiScreen> guiClass, Class<? extends GuiScreen> wrapperClass) {
		wrappers.put(guiClass, wrapperClass);
	}

	public GuiScreen wrapGui(GuiScreen gui) {

		Class<? extends GuiScreen> wrapperClass = wrappers.get(gui.getClass());
		if (wrapperClass == null)
			return gui;

		try {
			return wrapperClass.getConstructor(gui.getClass()).newInstance(gui);
		} catch (InvocationTargetException e) {
			Log.error(e.getCause().toString());
			return gui;
		} catch (ReflectiveOperationException e) {
			Log.error(e.toString());
			return gui;
		}
	}
}
