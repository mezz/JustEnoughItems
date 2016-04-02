package mezz.jei.util;

import mezz.jei.Internal;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ErrorUtil {
	@Nonnull
	public static String getInfoFromBrokenRecipe(@Nonnull Object recipe, @Nonnull IRecipeHandler recipeHandler) {
		StringBuilder recipeInfoBuilder = new StringBuilder();
		try {
			recipeInfoBuilder.append(recipe);
		} catch (RuntimeException e) {
			Log.error("Failed recipe.toString", e);
			recipeInfoBuilder.append(recipe.getClass());
		}

		IRecipeWrapper recipeWrapper;

		try {
			//noinspection unchecked
			recipeWrapper = recipeHandler.getRecipeWrapper(recipe);
		} catch (RuntimeException ignored) {
			recipeInfoBuilder.append("\nFailed to create recipe wrapper");
			return recipeInfoBuilder.toString();
		}

		recipeInfoBuilder.append("\nOutput ItemStacks: ");
		try {
			List outputs = recipeWrapper.getOutputs();
			Object itemStackIngredientsInfo = getItemStackIngredientsInfo(outputs);
			recipeInfoBuilder.append(itemStackIngredientsInfo);
		} catch (RuntimeException e) {
			recipeInfoBuilder.append(e.getMessage());
		}

		recipeInfoBuilder.append("\nOutput Fluids: ");
		try {
			recipeInfoBuilder.append(recipeWrapper.getFluidOutputs());
		} catch (RuntimeException e) {
			recipeInfoBuilder.append(e.getMessage());
		}

		recipeInfoBuilder.append("\nInput ItemStacks: ");
		try {
			List inputs = recipeWrapper.getInputs();
			Object itemStackIngredientsInfo = getItemStackIngredientsInfo(inputs);
			recipeInfoBuilder.append(itemStackIngredientsInfo);
		} catch (RuntimeException e) {
			recipeInfoBuilder.append(e.getMessage());
		}

		recipeInfoBuilder.append("\nInput Fluids: ");
		try {
			recipeInfoBuilder.append(recipeWrapper.getFluidInputs());
		} catch (RuntimeException e) {
			recipeInfoBuilder.append(e.getMessage());
		}

		return recipeInfoBuilder.toString();
	}

	public static List<List<String>> getItemStackIngredientsInfo(@Nullable List list) {
		if (list == null) {
			return null;
		}
		StackHelper stackHelper = Internal.getStackHelper();

		List<List<String>> ingredientsInfo = new ArrayList<>();
		for (Object ingredient : list) {
			List<String> ingredientInfo = new ArrayList<>();

			List<ItemStack> stacks = stackHelper.toItemStackList(ingredient);
			String oreDict = stackHelper.getOreDictEquivalent(stacks);
			if (oreDict != null) {
				ingredientInfo.add("OreDict: " + oreDict);
			}

			for (ItemStack stack : stacks) {
				String itemStackInfo = getItemStackInfo(stack);
				ingredientInfo.add(itemStackInfo);
			}
			ingredientsInfo.add(ingredientInfo);
		}
		return ingredientsInfo;
	}

	public static String getItemStackInfo(@Nonnull ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item == null) {
			return itemStack.stackSize + "x (null)";
		}

		final String itemName;
		String registryName = item.getRegistryName().toString();
		if (registryName != null) {
			itemName = registryName;
		} else {
			itemName = item.getClass().getName();
		}
		return itemStack.toString().replace(item.getUnlocalizedName(), itemName);
	}
}
