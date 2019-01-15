package mezz.jei.api.gui;

import net.minecraft.client.gui.GuiScreen;

/**
 * Defines the properties of a gui so that JEI can draw next to it.
 *
 * @since JEI 4.8.4
 */
public interface IGuiProperties {
	Class<? extends GuiScreen> getGuiClass();

	int getGuiLeft();

	int getGuiTop();

	int getGuiXSize();

	int getGuiYSize();

	int getScreenWidth();

	int getScreenHeight();
}
