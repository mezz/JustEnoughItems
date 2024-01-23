package mezz.jei.library.plugins.vanilla.crafting.replacers;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.common.platform.IPlatformIngredientHelper;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.Services;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.*;

import java.util.List;
import java.util.Optional;

public final class TippedArrowRecipeMaker {

	public static List<RecipeHolder<CraftingRecipe>> createRecipes(IStackHelper stackHelper) {
		String group = "jei.tipped.arrow";
		ItemStack arrowStack = new ItemStack(Items.ARROW);
		Ingredient arrowIngredient = Ingredient.of(arrowStack);

		IPlatformRegistry<Potion> potionRegistry = Services.PLATFORM.getRegistry(Registries.POTION);
		IPlatformIngredientHelper ingredientHelper = Services.PLATFORM.getIngredientHelper();
		return potionRegistry.getValues()
			.map(potion -> {
				ItemStack input = PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion);
				ItemStack output = PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW, 8), potion);

				Ingredient potionIngredient = ingredientHelper.createNbtIngredient(input, stackHelper);
				NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY,
					arrowIngredient, arrowIngredient, arrowIngredient,
					arrowIngredient, potionIngredient, arrowIngredient,
					arrowIngredient, arrowIngredient, arrowIngredient
				);
				ResourceLocation id = new ResourceLocation(ModIds.MINECRAFT_ID, "jei.tipped.arrow." + output.getDescriptionId());
				ShapedRecipePattern pattern = new ShapedRecipePattern(3, 3, inputs, Optional.empty());
				CraftingRecipe recipe = new ShapedRecipe(group, CraftingBookCategory.MISC, pattern, output);
				return new RecipeHolder<>(id, recipe);
			})
			.toList();
	}

	private TippedArrowRecipeMaker() {

	}
}
