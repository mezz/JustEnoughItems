package mezz.jei.forge.platform;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.category.extensions.vanilla.brewing.IBrewingCategoryExtension;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.library.util.ResourceLocationUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipe;

import java.util.Arrays;
import java.util.List;

public class BrewingRecipeCategoryExtension implements IBrewingCategoryExtension<BrewingRecipe> {
	private final IIngredientHelper<ItemStack> itemStackHelper;

	public BrewingRecipeCategoryExtension(IIngredientHelper<ItemStack> itemStackHelper) {
		this.itemStackHelper = itemStackHelper;
	}

	@Override
	public List<IJeiBrewingRecipe> getBrewingRecipes(
		BrewingRecipe brewingRecipe,
		IVanillaRecipeFactory vanillaRecipeFactory
	) {
		List<ItemStack> ingredients = Arrays.stream(brewingRecipe.getIngredient().getItems())
			.filter(i -> !i.isEmpty())
			.toList();
		if (ingredients.isEmpty()) {
			return List.of();
		}

		Ingredient inputIngredient = brewingRecipe.getInput();
		List<ItemStack> inputs = Arrays.stream(inputIngredient.getItems())
			.filter(i -> !i.isEmpty())
			.toList();
		if (inputs.isEmpty()) {
			return List.of();
		}

		ItemStack output = brewingRecipe.getOutput();
		if (output.isEmpty()) {
			return List.of();
		}

		String outputModId = itemStackHelper.getResourceLocation(output).getNamespace();
		String outputUid = itemStackHelper.getUniqueId(output, UidContext.Recipe);
		String uidPath = ResourceLocationUtil.sanitizePath(outputUid);
		IJeiBrewingRecipe recipe = vanillaRecipeFactory.createBrewingRecipe(
			ingredients,
			inputs,
			output,
			new ResourceLocation(outputModId, uidPath)
		);
		return List.of(recipe);
	}
}
