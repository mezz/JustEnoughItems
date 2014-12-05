package mezz.jei.plugins.forestry.fabricator;

import forestry.factory.gadgets.MachineFabricator;

import java.util.ArrayList;
import java.util.List;

public class FabricatorRecipeMaker {

	public static List<Object> getSmeltingRecipes() {
		return new ArrayList<Object>(MachineFabricator.RecipeManager.smeltings);
	}

	public static List<Object> getCraftingRecipes() {
		return new ArrayList<Object>(MachineFabricator.RecipeManager.recipes);
	}

}
