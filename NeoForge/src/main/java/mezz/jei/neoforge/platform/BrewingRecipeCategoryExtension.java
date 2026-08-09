package mezz.jei.neoforge.platform;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.category.extensions.vanilla.brewing.IBrewingCategoryExtension;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.library.plugins.vanilla.ingredients.subtypes.PotionSubtypeInterpreter;
import mezz.jei.library.util.ResourceLocationUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;

import java.util.List;

public class BrewingRecipeCategoryExtension implements IBrewingCategoryExtension<BrewingRecipe> {
	private final IIngredientHelper<ItemStack> itemStackHelper;

	public BrewingRecipeCategoryExtension(IIngredientHelper<ItemStack> itemStackHelper) {
		this.itemStackHelper = itemStackHelper;
	}

	@Override
	public List<IJeiBrewingRecipe> getBrewingRecipes(
		BrewingRecipe brewingRecipe,
		IVanillaRecipeFactory vanillaRecipeFactory,
		ContextMap contextMap
	) {
		List<ItemStack> ingredients = brewingRecipe.getIngredient().display().resolveForStacks(contextMap);
		if (ingredients.isEmpty()) {
			return List.of();
		}

		Ingredient inputIngredient = brewingRecipe.getInput();
		SlotDisplay slotDisplay = inputIngredient.display();
		List<ItemStack> inputs = slotDisplay.resolve(contextMap, SlotDisplay.ItemStackContentsFactory.INSTANCE)
			.filter(i -> !i.isEmpty())
			.toList();
		if (inputs.isEmpty()) {
			return List.of();
		}

		ItemStack output = brewingRecipe.getOutput();
		if (output.isEmpty()) {
			return List.of();
		}

		String outputModId = itemStackHelper.getIdentifier(output).getNamespace();
		String outputUid = PotionSubtypeInterpreter.INSTANCE.getStringName(output);
		String uidPath = ResourceLocationUtil.sanitizePath(outputUid);
		IJeiBrewingRecipe recipe = vanillaRecipeFactory.createBrewingRecipe(
			ingredients,
			inputs,
			output,
			Identifier.fromNamespaceAndPath(outputModId, uidPath)
		);
		return List.of(recipe);
	}
}
