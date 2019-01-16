package mezz.jei.gui.recipes;

import java.util.List;

import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraft.client.Minecraft;

import mezz.jei.Internal;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.GuiHelper;
import mezz.jei.input.IMouseHandler;

public abstract class RecipeGuiTab implements IMouseHandler {
	public static final int TAB_HEIGHT = 24;
	public static final int TAB_WIDTH = 24;

	protected final int x;
	protected final int y;
	private final HoverChecker hoverChecker;

	public RecipeGuiTab(int x, int y) {
		this.x = x;
		this.y = y;
		this.hoverChecker = new HoverChecker(y, y + TAB_HEIGHT, x, x + TAB_WIDTH, 0);
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return hoverChecker.checkHover(mouseX, mouseY);
	}

	@Override
	public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
		return false;
	}

	public abstract boolean isSelected(IRecipeCategory selectedCategory);

	public void draw(Minecraft minecraft, boolean selected, int mouseX, int mouseY) {
		GuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		IDrawable tab = selected ? guiHelper.getTabSelected() : guiHelper.getTabUnselected();

		tab.draw(minecraft, x, y);
	}

	public abstract List<String> getTooltip();
}
