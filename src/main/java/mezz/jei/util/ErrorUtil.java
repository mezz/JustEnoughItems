package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.startup.ForgeModIdHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public final class ErrorUtil {
	private ErrorUtil() {
	}

	public static <T> String getInfoFromRecipe(T recipe, IRecipeWrapper recipeWrapper) {
		StringBuilder recipeInfoBuilder = new StringBuilder();
		String recipeName = getNameForRecipe(recipe);
		recipeInfoBuilder.append(recipeName);

		Ingredients ingredients = new Ingredients();

		try {
			recipeWrapper.getIngredients(ingredients);
		} catch (RuntimeException | LinkageError ignored) {
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
		List<List<T>> outputs = ingredients.getOutputs(ingredientClass);
		return getIngredientInfo(ingredientClass, outputs);
	}

	private static <T> List<String> getIngredientInputInfo(Class<T> ingredientClass, IIngredients ingredients) {
		List<List<T>> inputs = ingredients.getInputs(ingredientClass);
		return getIngredientInfo(ingredientClass, inputs);
	}

	public static String getNameForRecipe(Object recipe) {
		if (recipe instanceof IForgeRegistryEntry) {
			IForgeRegistryEntry registryEntry = (IForgeRegistryEntry) recipe;
			ResourceLocation registryName = registryEntry.getRegistryName();
			if (registryName != null) {
				String modId = registryName.getResourceDomain();
				String modName = ForgeModIdHelper.getInstance().getModNameForModId(modId);
				return modName + " " + registryName;
			}
		}
		try {
			return recipe.toString();
		} catch (RuntimeException e) {
			Log.get().error("Failed recipe.toString", e);
			return recipe.getClass().toString();
		}
	}

	public static <T> String getInfoFromBrokenCraftingRecipe(T recipe, List inputs, ItemStack output) {
		StringBuilder recipeInfoBuilder = new StringBuilder();
		String recipeName = getNameForRecipe(recipe);
		recipeInfoBuilder.append(recipeName);

		recipeInfoBuilder.append("\nOutputs:");
		List<List<ItemStack>> outputs = Collections.singletonList(Collections.singletonList(output));
		List<String> ingredientOutputInfo = getIngredientInfo(ItemStack.class, outputs);
		recipeInfoBuilder.append('\n').append(ItemStack.class.getName()).append(": ").append(ingredientOutputInfo);

		recipeInfoBuilder.append("\nInputs:");
		List<List<ItemStack>> inputLists = Internal.getStackHelper().expandRecipeItemStackInputs(inputs, false);
		List<String> ingredientInputInfo = getIngredientInfo(ItemStack.class, inputLists);
		recipeInfoBuilder.append('\n').append(ItemStack.class.getName()).append(": ").append(ingredientInputInfo);

		return recipeInfoBuilder.toString();
	}

	public static <T> String getIngredientInfo(T ingredient) {
		IIngredientHelper<T> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(ingredient);
		return ingredientHelper.getErrorInfo(ingredient);
	}

	public static <T> List<String> getIngredientInfo(Class<T> ingredientClass, List<? extends List<T>> ingredients) {
		IIngredientHelper<T> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(ingredientClass);
		List<String> allInfos = new ArrayList<>(ingredients.size());

		for (List<T> inputList : ingredients) {
			List<String> infos = new ArrayList<>(inputList.size());
			for (T input : inputList) {
				String errorInfo = ingredientHelper.getErrorInfo(input);
				infos.add(errorInfo);
			}
			allInfos.add(infos.toString());
		}

		return allInfos;
	}

	public static String getItemStackInfo(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			return "null";
		}
		Item item = itemStack.getItem();
		final String itemName;
		ResourceLocation registryName = item.getRegistryName();
		if (registryName != null) {
			itemName = registryName.toString();
		} else if (item instanceof ItemBlock) {
			final String blockName;
			Block block = ((ItemBlock) item).getBlock();
			//noinspection ConstantConditions
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

	public static void checkNotEmpty(@Nullable String string, String name) {
		if (string == null) {
			throw new NullPointerException(name + " must not be null.");
		} else if (string.isEmpty()) {
			throw new IllegalArgumentException(name + " must not be empty.");
		}
	}

	public static void checkNotEmpty(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			throw new NullPointerException("ItemStack must not be null.");
		} else if (itemStack.isEmpty()) {
			String info = getItemStackInfo(itemStack);
			throw new IllegalArgumentException("ItemStack value must not be empty. " + info);
		}
	}

	public static void checkNotEmpty(@Nullable ItemStack itemStack, String name) {
		if (itemStack == null) {
			throw new NullPointerException(name + " must not be null.");
		} else if (itemStack.isEmpty()) {
			String info = getItemStackInfo(itemStack);
			throw new IllegalArgumentException("ItemStack " + name + " must not be empty. " + info);
		}
	}

	public static <T> void checkNotEmpty(@Nullable T[] values, String name) {
		if (values == null) {
			throw new NullPointerException(name + " must not be null.");
		} else if (values.length <= 0) {
			throw new IllegalArgumentException(name + " must not be empty.");
		}
	}

	public static void checkNotEmpty(@Nullable Collection values, String name) {
		if (values == null) {
			throw new NullPointerException(name + " must not be null.");
		} else if (values.isEmpty()) {
			throw new IllegalArgumentException(name + " must not be empty.");
		} else if (!(values instanceof NonNullList)) {
			for (Object value : values) {
				if (value == null) {
					throw new NullPointerException(name + " must not contain null values.");
				}
			}
		}
	}

	public static <T> T checkNotNull(@Nullable T object, String name) {
		if (object == null) {
			throw new NullPointerException(name + " must not be null.");
		}
		return object;
	}

	public static void checkIsValidIngredient(@Nullable Object ingredient, String name) {
		checkNotNull(ingredient, name);
		if (!Internal.getIngredientRegistry().isValidIngredient(ingredient)) {
			throw new IllegalArgumentException("Invalid ingredient found. Parameter Name: " + name + " Class: " + ingredient.getClass() + " Object: " + ingredient);
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static void assertMainThread() {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft != null && !minecraft.isCallingFromMinecraftThread()) {
			throw new IllegalStateException("A JEI API method is being called by another mod from the wrong thread. It must be called on the main thread.");
		}
	}
}
