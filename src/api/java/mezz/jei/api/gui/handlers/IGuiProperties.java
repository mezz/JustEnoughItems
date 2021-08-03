package mezz.jei.api.gui.handlers;

import net.minecraft.client.gui.screens.Screen;

/**
 * Defines the properties of a gui so that JEI can draw next to it.
 * Created by {@link IScreenHandler#apply(Screen)}
 */
public interface IGuiProperties {
	Class<? extends Screen> getScreenClass();

	int getGuiLeft();

	int getGuiTop();

	int getGuiXSize();

	int getGuiYSize();

	int getScreenWidth();

	int getScreenHeight();
}
