package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;
import java.util.List;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ShapelessOreRecipeHandler implements IRecipeHandler<ShapelessOreRecipe> {
	@Nonnull
	private final IGuiHelper guiHelper;

	public ShapelessOreRecipeHandler(@Nonnull IGuiHelper guiHelper) {
		this.guiHelper = guiHelper;
	}

	@Override
	@Nonnull
	public Class<ShapelessOreRecipe> getRecipeClass() {
		return ShapelessOreRecipe.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid(@Nonnull ShapelessOreRecipe recipe) {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull ShapelessOreRecipe recipe) {
		return new ShapelessOreRecipeWrapper(guiHelper, recipe);
	}

	@Override
	public boolean isRecipeValid(@Nonnull ShapelessOreRecipe recipe) {
		if (recipe.getRecipeOutput() == null) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
			Log.error("Recipe has no outputs. {}", recipeInfo);
			return false;
		}
		int inputCount = 0;
		for (Object input : recipe.getInput()) {
			if (input instanceof List) {
				if (((List) input).isEmpty()) {
					// missing items for an oreDict name. This is normal behavior, but the recipe is invalid.
					return false;
				}
			}
			if (input != null) {
				inputCount++;
			}
		}
		if (inputCount > 9) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
			Log.error("Recipe has too many inputs. {}", recipeInfo);
			return false;
		}
		if (inputCount == 0) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
			Log.error("Recipe has no inputs. {}", recipeInfo);
			return false;
		}
		return true;
	}
}
