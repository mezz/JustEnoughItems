package mezz.jei.plugins.vanilla.crafting;

import net.minecraft.item.crafting.CraftingManager;

import java.util.List;

public class CraftingRecipeMaker {

	@SuppressWarnings("unchecked")
	public static List<Object> getCraftingRecipes() {
		return CraftingManager.getInstance().getRecipeList();
	}

}
