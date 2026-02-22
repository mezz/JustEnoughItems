package mezz.jei.library.plugins.vanilla.crafting.replacers;

import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.function.Consumer;

public interface IRecipeReplacer {
	/**
	 * @return true if this recipe replacer is finished and cannot replace more recipes
	 */
	boolean replace(RecipeHolder<CraftingRecipe> recipe, Consumer<RecipeHolder<CraftingRecipe>> replacements);
}
