package mezz.jei.common.input;

import mezz.jei.api.runtime.IClickedIngredient;
import mezz.jei.api.runtime.IScreenHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.stream.Stream;

public class GuiContainerWrapper implements IRecipeFocusSource {
	private final IScreenHelper screenHelper;

	public GuiContainerWrapper(IScreenHelper screenHelper) {
		this.screenHelper = screenHelper;
	}

	@Override
	public Stream<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		Screen guiScreen = Minecraft.getInstance().screen;
		if (guiScreen == null) {
			return Stream.empty();
		}
		return screenHelper.getIngredientUnderMouse(guiScreen, mouseX, mouseY);
	}
}
