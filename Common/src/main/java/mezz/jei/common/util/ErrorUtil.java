package mezz.jei.common.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformModHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
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

	public static String getItemStackInfo(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			return "null";
		}
		Item item = itemStack.getItem();
		String itemName = getItemName(item);
		String components = itemStack.getComponentsPatch().toString();
		return itemStack + " " + itemName + " components:" + components;
	}

	private static String getItemName(Item item) {
		Registry<Item> itemRegistry = RegistryUtil.getRegistry(Registries.ITEM);
		ResourceLocation key = itemRegistry.getKey(item);

		if (key != null) {
			return key.toString();
		} else if (item instanceof BlockItem blockItem) {
			final String blockName = getBlockName(blockItem);
			return "BlockItem(" + blockName + ")";
		}
		return item.getClass().getName();
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

	public static <T> CrashReport createIngredientCrashReport(Throwable throwable, String title, IIngredientManager ingredientManager, ITypedIngredient<T> typedIngredient) {
		return createIngredientCrashReport(throwable, title, ingredientManager, typedIngredient.getType(), typedIngredient.getIngredient());
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
		LOGGER.error(crashReportToString(throwable, title, category));
	}

	private static <T> void setIngredientCategoryDetails(CrashReportCategory category, IIngredientType<T> ingredientType, T ingredient, IIngredientManager ingredientManager) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Codec<T> ingredientCodec = ingredientManager.getIngredientCodec(ingredientType);

		IPlatformModHelper modHelper = Services.PLATFORM.getModHelper();

		category.setDetail("Name", () -> ingredientHelper.getDisplayName(ingredient));
		category.setDetail("Mod's Name", () -> {
			String modId = ingredientHelper.getDisplayModId(ingredient);
			return modHelper.getModNameForModId(modId);
		});
		category.setDetail("Registry Name", () -> ingredientHelper.getResourceLocation(ingredient).toString());
		category.setDetail("Class Name", () -> ingredient.getClass().toString());
		category.setDetail("toString Name", ingredient::toString);
		category.setDetail("JSON", () -> {
			Minecraft minecraft = Minecraft.getInstance();
			ClientLevel level = minecraft.level;
			assert level != null;
			RegistryAccess registryAccess = level.registryAccess();
			RegistryOps<JsonElement> registryOps = registryAccess.createSerializationContext(JsonOps.INSTANCE);
			return ingredientCodec.encodeStart(registryOps, ingredient)
				.mapOrElse(
					JsonElement::toString,
					DataResult.Error::message
				);
		});
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
