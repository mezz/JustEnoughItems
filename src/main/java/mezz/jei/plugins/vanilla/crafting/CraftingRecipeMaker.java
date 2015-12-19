package mezz.jei.plugins.vanilla.crafting;

import java.util.List;

import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

public class CraftingRecipeMaker {
	private CraftingRecipeMaker() {

	}

	@SuppressWarnings("unchecked")
	public static List<IRecipe> getCraftingRecipes() {
		return CraftingManager.getInstance().getRecipeList();
	}

}
