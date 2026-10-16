package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class FurnaceSmeltingCategory extends AbstractCookingCategory<SmeltingRecipe> {
	public FurnaceSmeltingCategory(IGuiHelper guiHelper) {
		super(guiHelper, RecipeTypes.SMELTING, Blocks.FURNACE, "gui.jei.category.smelting", 200, 116, 54);
	}

	@Override
	protected List<Identifier> getDefaultSourceRecipeIds(RecipeHolder<SmeltingRecipe> recipeHolder) {
		Identifier identifier = recipeHolder.id().identifier();
		if (identifier.equals(FurnaceRecipeMaker.SPONGE_WITH_BUCKET)) {
			return List.of(FurnaceRecipeMaker.SPONGE_RECIPE.identifier());
		}
		return super.getDefaultSourceRecipeIds(recipeHolder);
	}
}
