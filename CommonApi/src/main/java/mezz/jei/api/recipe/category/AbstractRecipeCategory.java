package mezz.jei.api.recipe.category;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;

/**
 * Simple abstract implementation of {@link IRecipeCategory} to help simplify creating recipe categories.
 * @since 15.20.0
 */
public abstract class AbstractRecipeCategory<T> implements IRecipeCategory<T> {
	private final RecipeType<T> recipeType;
	private final Component title;
	private final IDrawable icon;
	private final int width;
	private final int height;

	/**
	 * @since 15.20.0
	 */
	public AbstractRecipeCategory(RecipeType<T> recipeType, Component title, IDrawable icon, int width, int height) {
		this.recipeType = recipeType;
		this.title = title;
		this.icon = icon;
		this.width = width;
		this.height = height;
	}

	@Override
	public final RecipeType<T> getRecipeType() {
		return recipeType;
	}

	@Override
	public final Component getTitle() {
		return title;
	}

	@Override
	public final IDrawable getIcon() {
		return icon;
	}

	@Override
	public final int getWidth() {
		return width;
	}

	@Override
	public final int getHeight() {
		return height;
	}
}
