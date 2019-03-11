package mezz.jei.gui.recipes;

import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.HoverChecker;
import mezz.jei.gui.textures.Textures;
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
		this.hoverChecker = new HoverChecker();
		this.hoverChecker.updateBounds(y, y + TAB_HEIGHT, x, x + TAB_WIDTH);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return hoverChecker.checkHover(mouseX, mouseY);
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return false;
	}

	public abstract boolean isSelected(IRecipeCategory selectedCategory);

	public void draw(boolean selected, int mouseX, int mouseY) {
		Textures textures = Internal.getTextures();
		IDrawable tab = selected ? textures.getTabSelected() : textures.getTabUnselected();

		tab.draw(x, y);
	}

	public abstract List<String> getTooltip();
}
