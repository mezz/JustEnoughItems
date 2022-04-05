package mezz.jei.common.plugins.vanilla.compostable;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.vanilla.IJeiCompostingRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class CompostingRecipeMaker {
	public static List<IJeiCompostingRecipe> getRecipes(IIngredientManager ingredientManager) {
		Object2FloatMap<ItemLike> compostables = ComposterBlock.COMPOSTABLES;
		Collection<ItemStack> allIngredients = ingredientManager.getAllIngredients(VanillaTypes.ITEM_STACK);

		return allIngredients.stream()
			.<IJeiCompostingRecipe>mapMulti((itemStack, consumer) -> {
				Item item = itemStack.getItem();
				float compostValue = compostables.getOrDefault(item, 0);
				if (compostValue > 0) {
					CompostingRecipe recipe = new CompostingRecipe(itemStack, compostValue);
					consumer.accept(recipe);
				}
			})
			.limit(compostables.size())
			.sorted(Comparator.comparingDouble(IJeiCompostingRecipe::getChance))
			.toList();
	}
}
