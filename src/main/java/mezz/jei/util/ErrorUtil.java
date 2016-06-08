package mezz.jei.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class ErrorUtil {
	@Nonnull
	public static <T> String getInfoFromBrokenRecipe(@Nonnull T recipe, @Nonnull IRecipeHandler<T> recipeHandler) {
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
		}

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

	public static List<String> getItemStackIngredientsInfo(@Nullable List list) {
		if (list == null) {
			return null;
		}
		StackHelper stackHelper = Internal.getStackHelper();

		List<String> ingredientsInfo = new ArrayList<>();
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

			ingredientsInfo.add(ingredientInfo.toString() + "\n");
		}
		return ingredientsInfo;
	}

	public static String getItemStackInfo(@Nonnull ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item == null) {
			return itemStack.stackSize + "x (null)";
		}

		final String itemName;
		ResourceLocation registryName = item.getRegistryName();
		if (registryName != null) {
			itemName = registryName.toString();
		} else if (item instanceof ItemBlock) {
			itemName = "ItemBlock(" + ((ItemBlock) item).getBlock() + ")";
		} else {
			itemName = item.getClass().getName();
		}

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt != null) {
			return itemStack.toString() + " " + itemName + " nbt:" + nbt;
		}
		return itemStack.toString() + " " + itemName;
	}
}
