package mezz.jei.util;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mezz.jei.Internal;
import mezz.jei.JeiRuntime;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

public final class GuiAreaHelper {
	private GuiAreaHelper() {
	}

	public static List<IAdvancedGuiHandler<?>> getActiveAdvancedGuiHandlers(List<IAdvancedGuiHandler<?>> advancedGuiHandlers, Class<? extends GuiScreen> guiScreenClass) {
		List<IAdvancedGuiHandler<?>> activeAdvancedGuiHandler = new ArrayList<IAdvancedGuiHandler<?>>();
		if (GuiContainer.class.isAssignableFrom(guiScreenClass)) {
			for (IAdvancedGuiHandler<?> advancedGuiHandler : advancedGuiHandlers) {
				Class<?> guiContainerClass = advancedGuiHandler.getGuiContainerClass();
				if (guiContainerClass.isAssignableFrom(guiScreenClass)) {
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

	public static boolean intersects(Collection<Rectangle> areas, Rectangle comparisonArea) {
		for (Rectangle area : areas) {
			if (area.intersects(comparisonArea)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isMouseOverGuiArea(Collection<Rectangle> guiAreas, int mouseX, int mouseY) {
		for (Rectangle guiArea : guiAreas) {
			if (guiArea.contains(mouseX, mouseY)) {
				return true;
			}
		}
		return false;
	}

	public static Set<Rectangle> getGuiAreas() {
		final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		if (currentScreen instanceof GuiContainer) {
			final GuiContainer guiContainer = (GuiContainer) currentScreen;
			final JeiRuntime jeiRuntime = Internal.getRuntime();
			if (jeiRuntime != null) {
				final Set<Rectangle> allGuiExtraAreas = new HashSet<Rectangle>();
				final List<IAdvancedGuiHandler<GuiContainer>> activeAdvancedGuiHandlers = jeiRuntime.getActiveAdvancedGuiHandlers(guiContainer);
				for (IAdvancedGuiHandler<GuiContainer> advancedGuiHandler : activeAdvancedGuiHandlers) {
					final List<Rectangle> guiExtraAreas = advancedGuiHandler.getGuiExtraAreas(guiContainer);
					if (guiExtraAreas != null) {
						allGuiExtraAreas.addAll(guiExtraAreas);
					}
				}
				return allGuiExtraAreas;
			}
		}
		return Collections.emptySet();
	}

}
