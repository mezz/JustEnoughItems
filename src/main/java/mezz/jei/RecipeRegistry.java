package mezz.jei;

import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeRegistry {

	public final List<IRecipe> allRecipes;
	public final Map<ItemStack, ItemStack> smelting;

	public final ArrayList<ShapedRecipes> shapedRecipes = new ArrayList<ShapedRecipes>();
	public final ArrayList<ShapedOreRecipe> shapedOreRecipes = new ArrayList<ShapedOreRecipe>();

	public final ArrayList<ShapelessRecipes> shapelessRecipes = new ArrayList<ShapelessRecipes>();
	public final ArrayList<ShapelessOreRecipe> shapelessOreRecipes = new ArrayList<ShapelessOreRecipe>();

	@SuppressWarnings("unchecked")
	public RecipeRegistry() {
		allRecipes = CraftingManager.getInstance().getRecipeList();
		smelting = FurnaceRecipes.smelting().getSmeltingList();

		for (IRecipe recipe : allRecipes) {
			if (recipe instanceof ShapedRecipes)
				shapedRecipes.add((ShapedRecipes)recipe);
			else if (recipe instanceof ShapedOreRecipe)
				shapedOreRecipes.add((ShapedOreRecipe)recipe);
			else if (recipe instanceof ShapelessRecipes)
				shapelessRecipes.add((ShapelessRecipes) recipe);
			else if (recipe instanceof ShapelessOreRecipe)
				shapelessOreRecipes.add((ShapelessOreRecipe)recipe);
			else
				Log.debug("Unhandled recipe type: " + recipe.toString());
		}
	}
}
