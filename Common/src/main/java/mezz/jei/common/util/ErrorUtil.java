package mezz.jei.common.util;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformModHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class ErrorUtil {
	private static final Logger LOGGER = LogManager.getLogger();

	private ErrorUtil() {
	}

	public static <T> String getIngredientInfo(T ingredient, IIngredientType<T> ingredientType, IIngredientManager ingredientManager) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		return ingredientHelper.getErrorInfo(ingredient);
	}

	@SuppressWarnings("ConstantConditions")
	public static String getItemStackInfo(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			return "null";
		}
		Item item = itemStack.getItem();
		RegistryWrapper<Item> itemRegistry = RegistryWrapper.getRegistry(Registries.ITEM);

		final String itemName = itemRegistry.getRegistryName(item)
			.map(ResourceLocation::toString)
			.orElseGet(() -> {
				if (item instanceof BlockItem) {
					final String blockName;
					Block block = ((BlockItem) item).getBlock();
					if (block == null) {
						blockName = "null";
					} else {
						RegistryWrapper<Block> blockRegistry = RegistryWrapper.getRegistry(Registries.BLOCK);
						blockName = blockRegistry.getRegistryName(block)
							.map(ResourceLocation::toString)
							.orElseGet(() -> block.getClass().getName());
					}
					return "BlockItem(" + blockName + ")";
				} else {
					return item.getClass().getName();
				}
			});

		String components = itemStack.getComponentsPatch().toString();
		return itemStack + " " + itemName + " nbt:" + components;
	}

	@SuppressWarnings("ConstantConditions")
	public static void checkNotEmpty(ItemStack itemStack) {
		if (itemStack == null) {
			throw new NullPointerException("ItemStack must not be null.");
		} else if (itemStack.isEmpty()) {
			String info = getItemStackInfo(itemStack);
			throw new IllegalArgumentException("ItemStack value must not be empty. " + info);
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static void checkNotEmpty(ItemStack itemStack, String name) {
		if (itemStack == null) {
			throw new NullPointerException(name + " must not be null.");
		} else if (itemStack.isEmpty()) {
			String info = getItemStackInfo(itemStack);
			throw new IllegalArgumentException("ItemStack " + name + " must not be empty. " + info);
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static <T> void checkNotEmpty(T[] values, String name) {
		if (values == null) {
			throw new NullPointerException(name + " must not be null.");
		} else if (values.length == 0) {
			throw new IllegalArgumentException(name + " must not be empty.");
		}
		for (T value : values) {
			if (value == null) {
				throw new NullPointerException(name + " must not contain null values.");
			}
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static void checkNotEmpty(Collection<?> values, String name) {
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

	public static <T> void checkNotNull(@Nullable T object, String name) {
		if (object == null) {
			throw new NullPointerException(name + " must not be null.");
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static void checkNotNull(Collection<?> values, String name) {
		if (values == null) {
			throw new NullPointerException(name + " must not be null.");
		} else if (!(values instanceof NonNullList)) {
			for (Object value : values) {
				if (value == null) {
					throw new NullPointerException(name + " must not contain null values.");
				}
			}
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static void assertMainThread() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft != null && !minecraft.isSameThread()) {
			Thread currentThread = Thread.currentThread();
			throw new IllegalStateException(
				"A JEI API method is being called by another mod from the wrong thread:\n" +
					currentThread + "\n" +
					"It must be called on the main thread by using Minecraft.addScheduledTask."
			);
		}
	}

	public static <T> void validateRecipes(RecipeType<T> recipeType, Iterable<? extends T> recipes) {
		Class<?> recipeClass = recipeType.getRecipeClass();
		for (T recipe : recipes) {
			if (!recipeClass.isInstance(recipe)) {
				throw new IllegalArgumentException(recipeType.getUid() + " recipes must be an instance of " + recipeClass + ". Instead got: " + recipe.getClass());
			}
		}
	}

	public static <T> CrashReport createIngredientCrashReport(Throwable throwable, String title, IIngredientManager ingredientManager, ITypedIngredient<T> typedIngredient) {
		CrashReport crashReport = CrashReport.forThrowable(throwable, title);

		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		CrashReportCategory category = crashReport.addCategory("Ingredient");
		setIngredientCategoryDetails(category, typedIngredient, ingredientHelper);
		return crashReport;
	}

	public static <T> void logIngredientCrash(Throwable throwable, String title, IIngredientManager ingredientManager, ITypedIngredient<T> typedIngredient) {
		CrashReportCategory category = new CrashReportCategory("Ingredient");
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		setIngredientCategoryDetails(category, typedIngredient, ingredientHelper);
		LOGGER.error(crashReportToString(throwable, title, category));
	}

	private static <T> void setIngredientCategoryDetails(CrashReportCategory category, ITypedIngredient<T> typedIngredient, IIngredientHelper<T> ingredientHelper) {
		T ingredient = typedIngredient.getIngredient();
		IIngredientType<T> ingredientType = typedIngredient.getType();

		IPlatformModHelper modHelper = Services.PLATFORM.getModHelper();

		category.setDetail("Name", () -> ingredientHelper.getDisplayName(ingredient));
		category.setDetail("Mod's Name", () -> {
			String modId = ingredientHelper.getDisplayModId(ingredient);
			return modHelper.getModNameForModId(modId);
		});
		category.setDetail("Registry Name", () -> ingredientHelper.getResourceLocation(ingredient).toString());
		category.setDetail("Class Name", () -> ingredient.getClass().toString());
		category.setDetail("toString Name", ingredient::toString);
		category.setDetail("Unique Id for JEI (for JEI Blacklist)", () -> ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient));
		category.setDetail("Ingredient Type for JEI", () -> ingredientType.getIngredientClass().toString());
		category.setDetail("Error Info gathered from JEI", () -> ingredientHelper.getErrorInfo(ingredient));
	}

	private static String crashReportToString(Throwable t, String title, CrashReportCategory... categories) {
		StringBuilder sb = new StringBuilder();
		sb.append(title);
		sb.append(":\n\n");
		for (CrashReportCategory category : categories) {
			category.getDetails(sb);
			sb.append("\n\n");
		}
		sb.append("-- Stack Trace --\n\n");
		sb.append(ExceptionUtils.getStackTrace(t));
		return sb.toString();
	}
}
