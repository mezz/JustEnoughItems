package mezz.jei.util;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

public class GuiAreaHelper {
	public static List<IAdvancedGuiHandler<?>> getActiveAdvancedGuiHandlers(List<IAdvancedGuiHandler<?>> advancedGuiHandlers, GuiScreen guiScreen) {
		List<IAdvancedGuiHandler<?>> activeAdvancedGuiHandler = new ArrayList<IAdvancedGuiHandler<?>>();
		if (guiScreen instanceof GuiContainer) {
			for (IAdvancedGuiHandler<?> advancedGuiHandler : advancedGuiHandlers) {
				Class<?> guiContainerClass = advancedGuiHandler.getGuiContainerClass();
				if (guiContainerClass.isInstance(guiScreen)) {
					activeAdvancedGuiHandler.add(advancedGuiHandler);
				}
			}
		}
		return activeAdvancedGuiHandler;
	}

	public static List<Rectangle> getGuiAreas(List<IAdvancedGuiHandler<?>> activeAdvancedGuiHandlers, GuiContainer guiContainer) {
		List<Rectangle> guiAreas = new ArrayList<Rectangle>();
		for (IAdvancedGuiHandler<?> advancedGuiHandler : activeAdvancedGuiHandlers) {
			List<Rectangle> guiExtraAreas = getGuiAreas(guiContainer, advancedGuiHandler);
			if (guiExtraAreas != null) {
				guiAreas.addAll(guiExtraAreas);
			}
		}
		return guiAreas;
	}

	@Nullable
	private static <T extends GuiContainer> List<Rectangle> getGuiAreas(GuiContainer gui, IAdvancedGuiHandler<T> advancedGuiHandler) {
		Class<T> guiClass = advancedGuiHandler.getGuiContainerClass();
		if (guiClass.isInstance(gui)) {
			T guiT = guiClass.cast(gui);
			return advancedGuiHandler.getGuiExtraAreas(guiT);
		}
		return null;
	}

	public static boolean intersects(List<Rectangle> areas, Rectangle comparisonArea) {
		for (Rectangle area : areas) {
			if (area.intersects(comparisonArea)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isMouseOverGuiArea(List<Rectangle> guiAreas, int mouseX, int mouseY) {
		for (Rectangle guiArea : guiAreas) {
			if (guiArea.contains(mouseX, mouseY)) {
				return true;
			}
		}
		return false;
	}
}
