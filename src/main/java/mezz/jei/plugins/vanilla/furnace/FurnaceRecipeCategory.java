package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import net.minecraft.util.StatCollector;

import mezz.jei.api.recipe.IRecipeCategory;

public abstract class FurnaceRecipeCategory implements IRecipeCategory {

	protected static final int inputSlot = 0;
	protected static final int fuelSlot = 1;
	protected static final int outputSlot = 2;

	@Nonnull
	private final String localizedName;

	public FurnaceRecipeCategory() {
		localizedName = StatCollector.translateToLocal("gui.jei.furnaceRecipes");
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}
}
