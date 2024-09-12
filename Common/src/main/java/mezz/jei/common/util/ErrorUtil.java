package mezz.jei.common.util;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformModHelper;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.Services;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ErrorUtil {
	private static final Logger LOGGER = LogManager.getLogger();

	private ErrorUtil() {
	}

	public static <T> String getIngredientInfo(T ingredient, IIngredientType<T> ingredientType, IIngredientManager ingredientManager) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		return ingredientHelper.getErrorInfo(ingredient);
	}

	public static String getItemStackInfo(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			return "null";
		}
		Item item = itemStack.getItem();
		IPlatformRegistry<Item> itemRegistry = Services.PLATFORM.getRegistry(Registries.ITEM);

		final String itemName = itemRegistry.getRegistryName(item)
			.map(ResourceLocation::toString)
			.orElseGet(() -> {
				if (item instanceof BlockItem blockItem) {
					final String blockName = getBlockName(blockItem);
					return "BlockItem(" + blockName + ")";
				} else {
					return item.getClass().getName();
				}
			});

		CompoundTag nbt = itemStack.getTag();
		if (nbt != null) {
			return itemStack + " " + itemName + " nbt:" + nbt;
		}
		return itemStack + " " + itemName;
	}

	@SuppressWarnings("ConstantValue")
	private static String getBlockName(BlockItem blockItem) {
		Block block = blockItem.getBlock();
		if (block == null) {
			return "null";
		}
		Registry<Block> blockRegistry = RegistryUtil.getRegistry(Registries.BLOCK);
		ResourceLocation key = blockRegistry.getKey(block);
		if (key != null) {
			return key.toString();
		}
		return block.getClass().getName();
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
				throw new IllegalArgumentException(recipeType + " recipes must be an instance of " + recipeClass + ". Instead got: " + recipe.getClass());
			}
		}
	}

	public static <T> CrashReport createIngredientCrashReport(Throwable throwable, String title, IIngredientManager ingredientManager, IIngredientType<T> ingredientType, T ingredient) {
		CrashReport crashReport = CrashReport.forThrowable(throwable, title);
		CrashReportCategory category = crashReport.addCategory("Ingredient");
		setIngredientCategoryDetails(category, ingredientType, ingredient, ingredientManager);
		return crashReport;
	}

	public static <T> void logIngredientCrash(Throwable throwable, String title, IIngredientManager ingredientManager, IIngredientType<T> ingredientType, T ingredient) {
		CrashReportCategory category = new CrashReportCategory("Ingredient");
		setIngredientCategoryDetails(category, ingredientType, ingredient, ingredientManager);
		LOGGER.error(crashReportToString(throwable, title, List.of(category)));
	}

	public static <T> void logIngredientCrash(
		Throwable throwable,
		String title,
		IIngredientManager ingredientManager,
		IIngredientType<T> ingredientType,
		T ingredient,
		CrashReportCategory... extraCategories
	) {
		CrashReportCategory category = new CrashReportCategory("Ingredient");
		setIngredientCategoryDetails(category, ingredientType, ingredient, ingredientManager);
		List<CrashReportCategory> categoryList = new ArrayList<>();
		categoryList.add(category);
		categoryList.addAll(List.of(extraCategories));
		LOGGER.error(crashReportToString(throwable, title, categoryList));
	}

	private static <T> void setIngredientCategoryDetails(CrashReportCategory category, IIngredientType<T> ingredientType, T ingredient, IIngredientManager ingredientManager) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
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

	public static String crashReportToString(Throwable t, String title, Collection<CrashReportCategory> categories) {
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
