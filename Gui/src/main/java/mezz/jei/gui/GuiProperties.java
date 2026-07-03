package mezz.jei.gui;

import com.google.common.base.Preconditions;
import mezz.jei.api.gui.handlers.IGuiProperties;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

public record GuiProperties(
	Class<? extends Screen> screenClass,
	int guiLeft,
	int guiTop,
	int guiXSize,
	int guiYSize,
	int screenWidth,
	int screenHeight
) implements IGuiProperties {

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean areEqual(@Nullable IGuiProperties a, @Nullable IGuiProperties b) {
		if (a == b) {
			return true;
		}
		return a != null && b != null &&
			a.screenClass().equals(b.screenClass()) &&
			a.guiLeft() == b.guiLeft() &&
			a.guiXSize() == b.guiXSize() &&
			a.screenWidth() == b.screenWidth() &&
			a.screenHeight() == b.screenHeight();
	}

	public GuiProperties {
		Preconditions.checkArgument(guiXSize > 0, "guiXSize must be > 0");
		Preconditions.checkArgument(guiYSize > 0, "guiYSize must be > 0");
		Preconditions.checkArgument(screenWidth > 0, "screenWidth must be > 0");
		Preconditions.checkArgument(screenHeight > 0, "screenHeight must be > 0");
	}


}
