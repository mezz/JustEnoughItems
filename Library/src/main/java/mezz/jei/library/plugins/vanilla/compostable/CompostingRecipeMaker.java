package mezz.jei.library.plugins.vanilla.compostable;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.vanilla.IJeiCompostingRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.ResourceLocation;
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
		Collection<ItemStack> allIngredients = ingredientManager.getAllItemStacks();
		IIngredientHelper<ItemStack> ingredientHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);

		return allIngredients.stream()
			.<IJeiCompostingRecipe>mapMulti((itemStack, consumer) -> {
				Item item = itemStack.getItem();
				float compostValue = compostables.getOrDefault(item, 0);
				if (compostValue > 0) {
					ResourceLocation resourceLocation = ingredientHelper.getResourceLocation(itemStack);
					String ingredientUidPath = resourceLocation.getPath();
					ResourceLocation recipeUid = ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, ingredientUidPath);
					CompostingRecipe recipe = new CompostingRecipe(itemStack, compostValue, recipeUid);
					consumer.accept(recipe);
				}
			})
			.limit(compostables.size())
			.sorted(Comparator.comparingDouble(IJeiCompostingRecipe::getChance))
			.toList();
	}
}
