package mezz.jei.plugins.vanilla.crafting;

import java.util.List;

import net.minecraft.item.crafting.CraftingManager;

public class CraftingRecipeMaker {

	@SuppressWarnings("unchecked")
	public static List<Object> getCraftingRecipes() {
		return CraftingManager.getInstance().getRecipeList();
	}

}
