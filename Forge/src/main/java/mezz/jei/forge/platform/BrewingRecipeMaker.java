package mezz.jei.forge.platform;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.util.BrewingRecipeMakerCommon;
import mezz.jei.library.util.ResourceLocationUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BrewingRecipeMaker {
	private static final Logger LOGGER = LogManager.getLogger();

	public static List<IJeiBrewingRecipe> getBrewingRecipes(IIngredientManager ingredientManager, IVanillaRecipeFactory vanillaRecipeFactory) {
		Collection<IBrewingRecipe> brewingRecipes = BrewingRecipeRegistry.getRecipes();

		Set<IJeiBrewingRecipe> recipes = brewingRecipes.stream()
			.filter(VanillaBrewingRecipe.class::isInstance)
			.map(VanillaBrewingRecipe.class::cast)
			.findFirst()
			.map(vanillaBrewingRecipe ->
				BrewingRecipeMakerCommon.getVanillaBrewingRecipes(
					vanillaRecipeFactory,
					ingredientManager,
					vanillaBrewingRecipe::getOutput
				)
			)
			.orElseGet(HashSet::new);

		IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);

		addModdedBrewingRecipes(
			vanillaRecipeFactory,
			itemStackHelper,
			brewingRecipes,
			recipes
		);

		return new ArrayList<>(recipes);
	}

	private static void addModdedBrewingRecipes(
		IVanillaRecipeFactory vanillaRecipeFactory,
		IIngredientHelper<ItemStack> itemStackHelper,
		Collection<IBrewingRecipe> brewingRecipes,
		Collection<IJeiBrewingRecipe> recipes
	) {
		Set<Class<?>> unhandledRecipeClasses = new HashSet<>();
		for (IBrewingRecipe iBrewingRecipe : brewingRecipes) {
			if (iBrewingRecipe instanceof BrewingRecipe brewingRecipe) {
				ItemStack[] ingredients = brewingRecipe.getIngredient().getItems();
				if (ingredients.length > 0) {
					Ingredient inputIngredient = brewingRecipe.getInput();
					ItemStack output = brewingRecipe.getOutput();
					List<ItemStack> inputs = Arrays.stream(inputIngredient.getItems())
						.filter(i -> !i.isEmpty())
						.toList();
					if (!output.isEmpty() && !inputs.isEmpty()) {
						String outputModId = itemStackHelper.getResourceLocation(output).getNamespace();
						String outputUid = itemStackHelper.getUniqueId(output, UidContext.Recipe);
						String uidPath = ResourceLocationUtil.sanitizePath(outputUid);
						IJeiBrewingRecipe recipe = vanillaRecipeFactory.createBrewingRecipe(
							List.of(ingredients),
							inputs,
							output,
							new ResourceLocation(outputModId, uidPath)
						);
						recipes.add(recipe);
					}
				}
			} else if (!(iBrewingRecipe instanceof VanillaBrewingRecipe)) {
				Class<?> recipeClass = iBrewingRecipe.getClass();
				if (!unhandledRecipeClasses.contains(recipeClass)) {
					unhandledRecipeClasses.add(recipeClass);
					LOGGER.debug("Can't handle brewing recipe class: {}", recipeClass);
				}
			}
		}
	}
}
