package mezz.jei.library.plugins.vanilla.crafting.replacers;

import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.common.platform.IPlatformIngredientHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.RegistryWrapper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.List;
import java.util.Optional;

public final class TippedArrowRecipeMaker {

	public static List<RecipeHolder<CraftingRecipe>> createRecipes(IStackHelper stackHelper) {
		String group = "jei.tipped.arrow";
		ItemStack arrowStack = new ItemStack(Items.ARROW);
		Ingredient arrowIngredient = Ingredient.of(arrowStack);

		RegistryWrapper<Potion> potionRegistry = RegistryWrapper.getRegistry(Registries.POTION);
		IPlatformIngredientHelper ingredientHelper = Services.PLATFORM.getIngredientHelper();
		return potionRegistry.getHolderStream()
			.map(potion -> {
				ItemStack input = PotionContents.createItemStack(Items.LINGERING_POTION, potion);
				ItemStack output = PotionContents.createItemStack(Items.TIPPED_ARROW, potion);
				output.setCount(8);

				Ingredient potionIngredient = ingredientHelper.createNbtIngredient(input, stackHelper);
				NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY,
					arrowIngredient, arrowIngredient, arrowIngredient,
					arrowIngredient, potionIngredient, arrowIngredient,
					arrowIngredient, arrowIngredient, arrowIngredient
				);
				ResourceLocation id = ResourceLocation.withDefaultNamespace("jei.tipped.arrow." + output.getDescriptionId());
				ShapedRecipePattern pattern = new ShapedRecipePattern(3, 3, inputs, Optional.empty());
				CraftingRecipe recipe = new ShapedRecipe(group, CraftingBookCategory.MISC, pattern, output);
				return new RecipeHolder<>(id, recipe);
			})
			.toList();
	}

	private TippedArrowRecipeMaker() {

	}
}
