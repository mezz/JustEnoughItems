package mezz.jei.api.recipe.category;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * Simple abstract implementation of {@link IRecipeCategory} to help simplify creating recipe categories.
 * @since 19.19.0
 */
public abstract class AbstractRecipeCategory<T> implements IRecipeCategory<T> {
	private final IRecipeType<T> recipeType;
	private final Component title;
	private final @Nullable IDrawable icon;
	private final int width;
	private final int height;

	/**
	 * Creates a category with the given title and layout dimensions.
	 *
	 * @param icon the category tab icon, or null to use a crafting station
	 * @since 19.19.0
	 */
	public AbstractRecipeCategory(IRecipeType<T> recipeType, Component title, @Nullable IDrawable icon, int width, int height) {
		this.recipeType = recipeType;
		this.title = title;
		this.icon = icon;
		this.width = width;
		this.height = height;
	}

	@Override
	public final IRecipeType<T> getRecipeType() {
		return recipeType;
	}

	@Override
	public final Component getTitle() {
		return title;
	}

	@Override
	public final @Nullable IDrawable getIcon() {
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
