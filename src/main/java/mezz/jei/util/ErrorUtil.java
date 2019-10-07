package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.config.Config;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.startup.IModIdHelper;

public final class ErrorUtil {
	@Nullable
	private static IModIdHelper modIdHelper;

	private ErrorUtil() {
	}

	public static void setModIdHelper(IModIdHelper modIdHelper) {
		ErrorUtil.modIdHelper = modIdHelper;
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
		Set<IIngredientType> outputTypes = ingredients.getOutputIngredients().keySet();
		for (IIngredientType<?> outputType : outputTypes) {
			List<String> ingredientOutputInfo = getIngredientOutputInfo(outputType, ingredients);
			recipeInfoBuilder.append('\n').append(outputType.getIngredientClass().getName()).append(": ").append(ingredientOutputInfo);
		}

		recipeInfoBuilder.append("\nInputs:");
		Set<IIngredientType> inputTypes = ingredients.getInputIngredients().keySet();
		for (IIngredientType<?> inputType : inputTypes) {
			List<String> ingredientInputInfo = getIngredientInputInfo(inputType, ingredients);
			recipeInfoBuilder.append('\n').append(inputType.getIngredientClass().getName()).append(": ").append(ingredientInputInfo);
		}

		return recipeInfoBuilder.toString();
	}

	private static <T> List<String> getIngredientOutputInfo(IIngredientType<T> ingredientType, IIngredients ingredients) {
		List<List<T>> outputs = ingredients.getOutputs(ingredientType);
		return getIngredientInfo(ingredientType, outputs);
	}

	private static <T> List<String> getIngredientInputInfo(IIngredientType<T> ingredientType, IIngredients ingredients) {
		List<List<T>> inputs = ingredients.getInputs(ingredientType);
		return getIngredientInfo(ingredientType, inputs);
	}

	public static String getNameForRecipe(Object recipe) {
		if (recipe instanceof IForgeRegistryEntry) {
			IForgeRegistryEntry registryEntry = (IForgeRegistryEntry) recipe;
			ResourceLocation registryName = registryEntry.getRegistryName();
			if (registryName != null) {
				if (modIdHelper != null) {
					String modId = registryName.getNamespace();
					String modName = modIdHelper.getModNameForModId(modId);
					return modName + " " + registryName + " " + recipe.getClass();
				}
				return registryName + " " + recipe.getClass();
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
		List<String> ingredientOutputInfo = getIngredientInfo(VanillaTypes.ITEM, outputs);
		recipeInfoBuilder.append('\n').append(ItemStack.class.getName()).append(": ").append(ingredientOutputInfo);

		recipeInfoBuilder.append("\nInputs:");
		List<List<ItemStack>> inputLists = Internal.getStackHelper().expandRecipeItemStackInputs(inputs, false);
		List<String> ingredientInputInfo = getIngredientInfo(VanillaTypes.ITEM, inputLists);
		recipeInfoBuilder.append('\n').append(ItemStack.class.getName()).append(": ").append(ingredientInputInfo);

		return recipeInfoBuilder.toString();
	}

	public static <T> String getIngredientInfo(T ingredient) {
		IIngredientHelper<T> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(ingredient);
		return ingredientHelper.getErrorInfo(ingredient);
	}

	public static <T> List<String> getIngredientInfo(IIngredientType<T> ingredientType, List<? extends List<T>> ingredients) {
		IIngredientHelper<T> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(ingredientType);
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
		for (T value : values) {
			if (value == null) {
				throw new NullPointerException(name + " must not contain null values.");
			}
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

	public static <T> void checkIsValidIngredient(@Nullable T ingredient, String name) {
		checkNotNull(ingredient, name);
		IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		if (!ingredientHelper.isValidIngredient(ingredient)) {
			String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
			throw new IllegalArgumentException("Invalid ingredient found. Parameter Name: " + name + " Ingredient Info: " + ingredientInfo);
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static void assertMainThread() {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft != null && !minecraft.isCallingFromMinecraftThread()) {
			Thread currentThread = Thread.currentThread();
			throw new IllegalStateException(
				"A JEI API method is being called by another mod from the wrong thread:\n" +
					currentThread + "\n" +
					"It must be called on the main thread by using Minecraft.addScheduledTask."
			);
		}
	}

	public static <T> ReportedException createRenderIngredientException(Throwable throwable, final T ingredient) {
		IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientType<T> ingredientType = ingredientRegistry.getIngredientType(ingredient);
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);

		CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering ingredient");
		CrashReportCategory ingredientCategory = crashreport.makeCategory("Ingredient being rendered");

		if (modIdHelper != null) {
			ingredientCategory.addDetail("Mod Name", () -> {
				String modId = ingredientHelper.getDisplayModId(ingredient);
				return modIdHelper.getModNameForModId(modId);
			});
		}
		ingredientCategory.addDetail("Registry Name", () -> {
			String modId = ingredientHelper.getModId(ingredient);
			String resourceId = ingredientHelper.getResourceId(ingredient);
			return modId + ":" + resourceId;
		});
		ingredientCategory.addDetail("Display Name", () -> ingredientHelper.getDisplayName(ingredient));
		ingredientCategory.addDetail("String Name", ingredient::toString);

		CrashReportCategory jeiCategory = crashreport.makeCategory("JEI render details");
		jeiCategory.addDetail("Unique Id (for Blacklist)", () -> ingredientHelper.getUniqueId(ingredient));
		jeiCategory.addDetail("Ingredient Type", () -> ingredientType.getIngredientClass().toString());
		jeiCategory.addDetail("Error Info", () -> ingredientHelper.getErrorInfo(ingredient));
		jeiCategory.addDetail("Filter Text", Config::getFilterText);
		jeiCategory.addDetail("Edit Mode Enabled", () -> Boolean.toString(Config.isEditModeEnabled()));
		jeiCategory.addDetail("Debug Mode Enabled", () -> Boolean.toString(Config.isDebugModeEnabled()));

		throw new ReportedException(crashreport);
	}
}
