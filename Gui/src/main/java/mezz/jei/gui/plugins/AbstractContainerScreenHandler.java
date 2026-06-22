package mezz.jei.gui.plugins;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.gui.GuiProperties;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

public class AbstractContainerScreenHandler<T extends AbstractContainerMenu> implements IScreenHandler<AbstractContainerScreen<T>> {
	@Override
	public @Nullable IGuiProperties apply(AbstractContainerScreen<T> containerScreen) {
		if (containerScreen.width <= 0 || containerScreen.height <= 0) {
			return null;
		}
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		int x = screenHelper.getLeftPos(containerScreen);
		int y = screenHelper.getTopPos(containerScreen);
		int width = screenHelper.getImageWidth(containerScreen);
		int height = screenHelper.getImageHeight(containerScreen);
		if (x < 0) {
			width -= x;
			x = 0;
		}
		if (y < 0) {
			height -= y;
			y = 0;
		}
		if (width <= 0 || height <= 0) {
			return null;
		}
		return new GuiProperties(
			containerScreen.getClass(),
			x,
			y,
			width,
			height,
			containerScreen.width,
			containerScreen.height
		);
	}
}
