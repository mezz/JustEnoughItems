package mezz.jei.gui.recipes;

import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import net.minecraft.client.gui.GuiGraphics;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.common.util.ImmutableRect2i;

public abstract class RecipeGuiTab implements IUserInputHandler {
	public static final int TAB_HEIGHT = 24;
	public static final int TAB_WIDTH = 24;

	protected final int x;
	protected final int y;
	private final ImmutableRect2i area;

	public RecipeGuiTab(int x, int y) {
		this.x = x;
		this.y = y;
		this.area = new ImmutableRect2i(x, y, TAB_WIDTH, TAB_HEIGHT);
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY);
	}

	public abstract boolean isSelected(IRecipeCategory<?> selectedCategory);

	public void draw(boolean selected, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		Textures textures = Internal.getTextures();
		IDrawable tab = selected ? textures.getTabSelected() : textures.getTabUnselected();

		tab.draw(guiGraphics, x, y);
	}

	public abstract JeiTooltip getTooltip();
}
