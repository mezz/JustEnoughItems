package mezz.jei.common.plugins.vanilla.cooking;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.helpers.IGuiHelper;

public class SmokingCategory extends AbstractCookingCategory<SmokingRecipe> {
	public SmokingCategory(IGuiHelper guiHelper) {
		super(guiHelper, Blocks.SMOKER, "gui.jei.category.smoking", 100);
	}

	@SuppressWarnings("removal")
	@Override
	public ResourceLocation getUid() {
		return getRecipeType().getUid();
	}

	@SuppressWarnings("removal")
	@Override
	public Class<? extends SmokingRecipe> getRecipeClass() {
		return getRecipeType().getRecipeClass();
	}

	@Override
	public RecipeType<SmokingRecipe> getRecipeType() {
		return RecipeTypes.SMOKING;
	}
}
