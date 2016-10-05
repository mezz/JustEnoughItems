package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class ErrorUtil {
	public static <T> String getInfoFromBrokenRecipe(T recipe, IRecipeHandler<T> recipeHandler) {
		StringBuilder recipeInfoBuilder = new StringBuilder();
		try {
			recipeInfoBuilder.append(recipe);
		} catch (RuntimeException e) {
			Log.error("Failed recipe.toString", e);
			recipeInfoBuilder.append(recipe.getClass());
		}

		IRecipeWrapper recipeWrapper;

		try {
			recipeWrapper = recipeHandler.getRecipeWrapper(recipe);
		} catch (RuntimeException ignored) {
			recipeInfoBuilder.append("\nFailed to create recipe wrapper");
			return recipeInfoBuilder.toString();
		} catch (LinkageError ignored) {
			recipeInfoBuilder.append("\nFailed to create recipe wrapper");
			return recipeInfoBuilder.toString();
		}

		Ingredients ingredients = new Ingredients();

		try {
			recipeWrapper.getIngredients(ingredients);
		} catch (RuntimeException ignored) {
			recipeInfoBuilder.append("\nFailed to get ingredients from recipe wrapper");
			return recipeInfoBuilder.toString();
		} catch (AbstractMethodError ignored) { // legacy
			return legacy_getInfoFromBrokenRecipe(recipeInfoBuilder, recipeWrapper);
		} catch (LinkageError ignored) {
			recipeInfoBuilder.append("\nFailed to get ingredients from recipe wrapper");
			return recipeInfoBuilder.toString();
		}

		recipeInfoBuilder.append("\nOutputs:");
		Set<Class> outputClasses = ingredients.getOutputIngredients().keySet();
		for (Class<?> outputClass : outputClasses) {
			List<String> ingredientOutputInfo = getIngredientOutputInfo(outputClass, ingredients);
			recipeInfoBuilder.append('\n').append(outputClass.getName()).append(": ").append(ingredientOutputInfo);
		}

		recipeInfoBuilder.append("\nInputs:");
		Set<Class> inputClasses = ingredients.getInputIngredients().keySet();
		for (Class<?> inputClass : inputClasses) {
			List<String> ingredientInputInfo = getIngredientInputInfo(inputClass, ingredients);
			recipeInfoBuilder.append('\n').append(inputClass.getName()).append(": ").append(ingredientInputInfo);
		}

		return recipeInfoBuilder.toString();
	}

	private static <T> List<String> getIngredientOutputInfo(Class<T> ingredientClass, IIngredients ingredients) {
		IIngredientHelper<T> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(ingredientClass);

		List<T> outputs = ingredients.getOutputs(ingredientClass);
		List<String> infos = new ArrayList<String>(outputs.size());

		for (T output : outputs) {
			String errorInfo = ingredientHelper.getErrorInfo(output);
			infos.add(errorInfo);
		}

		return infos;
	}

	private static <T> List<String> getIngredientInputInfo(Class<T> ingredientClass, IIngredients ingredients) {
		IIngredientHelper<T> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(ingredientClass);

		List<List<T>> inputs = ingredients.getInputs(ingredientClass);
		List<String> allInfos = new ArrayList<String>(inputs.size());

		for (List<T> inputList : inputs) {
			List<String> infos = new ArrayList<String>(inputList.size());
			for (T input : inputList) {
				String errorInfo = ingredientHelper.getErrorInfo(input);
				infos.add(errorInfo);
			}
			allInfos.add(infos.toString());
		}

		return allInfos;
	}

	private static String legacy_getInfoFromBrokenRecipe(StringBuilder recipeInfoBuilder, IRecipeWrapper recipeWrapper) {
		recipeInfoBuilder.append("\nOutput ItemStacks: ");
		try {
			List outputs = recipeWrapper.getOutputs();
			List<String> itemStackIngredientsInfo = getItemStackIngredientsInfo(outputs);
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
			List<String> itemStackIngredientsInfo = getItemStackIngredientsInfo(inputs);
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

	@Nullable
	public static List<String> getItemStackIngredientsInfo(@Nullable List list) {
		if (list == null) {
			return null;
		}
		StackHelper stackHelper = Internal.getStackHelper();

		List<String> ingredientsInfo = new ArrayList<String>();
		for (Object ingredient : list) {
			List<String> ingredientInfo = new ArrayList<String>();

			List<ItemStack> stacks = stackHelper.toItemStackList(ingredient);
			String oreDict = stackHelper.getOreDictEquivalent(stacks);
			if (oreDict != null) {
				ingredientInfo.add("OreDict: " + oreDict);
			}

			for (ItemStack stack : stacks) {
				String itemStackInfo = getItemStackInfo(stack);
				ingredientInfo.add(itemStackInfo);
			}

			ingredientsInfo.add(ingredientInfo.toString() + "\n");
		}
		return ingredientsInfo;
	}

	public static String getItemStackInfo(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			return "null";
		}
		Item item = itemStack.getItem();
		if (item == null) {
			return itemStack.stackSize + "x (null)";
		}

		final String itemName;
		ResourceLocation registryName = item.getRegistryName();
		if (registryName != null) {
			itemName = registryName.toString();
		} else if (item instanceof ItemBlock) {
			final String blockName;
			Block block = ((ItemBlock) item).getBlock();
			if (block == null) {
				blockName = "null";
			} else {
				ResourceLocation blockRegistryName = block.getRegistryName();
				if (blockRegistryName != null) {
					blockName = blockRegistryName.toString();
				} else {
					blockName = block.getClass().getName();
				}
			}
			itemName = "ItemBlock(" + blockName + ")";
		} else {
			itemName = item.getClass().getName();
		}

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt != null) {
			return itemStack + " " + itemName + " nbt:" + nbt;
		}
		return itemStack + " " + itemName;
	}
}
