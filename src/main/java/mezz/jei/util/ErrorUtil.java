package mezz.jei.util;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.config.ClientConfig;
import mezz.jei.ingredients.IIngredientSupplier;
import mezz.jei.recipes.RecipeManagerInternal;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.Internal;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IWorldConfig;
import mezz.jei.ingredients.IngredientManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ErrorUtil {
	private static final Logger LOGGER = LogManager.getLogger();
	@Nullable
	private static IModIdHelper modIdHelper;
	@Nullable
	private static IWorldConfig worldConfig;

	private ErrorUtil() {
	}

	public static void setModIdHelper(IModIdHelper modIdHelper) {
		ErrorUtil.modIdHelper = modIdHelper;
	}

	public static void setWorldConfig(IWorldConfig worldConfig) {
		ErrorUtil.worldConfig = worldConfig;
	}

	public static <T> String getInfoFromRecipe(T recipe, IRecipeCategory<T> recipeCategory) {
		StringBuilder recipeInfoBuilder = new StringBuilder();
		String recipeName = getNameForRecipe(recipe);
		recipeInfoBuilder.append(recipeName).append(" {");

		IIngredientSupplier ingredientSupplier = RecipeManagerInternal.getIngredientSupplier(recipe, recipeCategory);
		if (ingredientSupplier == null) {
			recipeInfoBuilder.append("\nFailed to get ingredients from recipe wrapper");
			return recipeInfoBuilder.toString();
		}

		recipeInfoBuilder.append("\n  Outputs:");
		appendRoleData(ingredientSupplier, RecipeIngredientRole.OUTPUT, recipeInfoBuilder);

		recipeInfoBuilder.append("\n  Inputs:");
		appendRoleData(ingredientSupplier, RecipeIngredientRole.INPUT, recipeInfoBuilder);

		recipeInfoBuilder.append("\n  Catalysts:");
		appendRoleData(ingredientSupplier, RecipeIngredientRole.CATALYST, recipeInfoBuilder);

		recipeInfoBuilder.append("\n}");

		return recipeInfoBuilder.toString();
	}

	private static void appendRoleData(IIngredientSupplier ingredientSupplier, RecipeIngredientRole role, StringBuilder recipeInfoBuilder) {
		ingredientSupplier.getIngredientTypes(role)
			.forEach(ingredientType -> {
				String ingredientOutputInfo = getIngredientInfo(ingredientType, role, ingredientSupplier);
				recipeInfoBuilder
					.append("\n    ")
					.append(ingredientType.getIngredientClass().getName())
					.append(": ")
					.append(ingredientOutputInfo);
			});
	}

	private static <T> String getIngredientInfo(IIngredientType<T> ingredientType, RecipeIngredientRole role, IIngredientSupplier ingredients) {
		List<T> ingredientList = ingredients.getIngredientStream(ingredientType, role).toList();
		IIngredientHelper<T> ingredientHelper = Internal.getIngredientManager().getIngredientHelper(ingredientType);

		Stream<String> stringStream = ingredientList.stream()
			.map(ingredientHelper::getErrorInfo);

		return truncatedStream(stringStream, ingredientList.size(), 10)
			.toList()
			.toString();
	}

	public static String getNameForRecipe(Object recipe) {
		ResourceLocation registryName = null;
		if (recipe instanceof Recipe) {
			registryName = ((Recipe<?>) recipe).getId();
		} else if (recipe instanceof IForgeRegistryEntry<?> registryEntry) {
			registryName = registryEntry.getRegistryName();
		}
		if (registryName != null) {
			if (modIdHelper != null) {
				String modId = registryName.getNamespace();
				String modName = modIdHelper.getModNameForModId(modId);
				return modName + " " + registryName + " " + recipe.getClass();
			}
			return registryName + " " + recipe.getClass();
		}
		try {
			return recipe.toString();
		} catch (RuntimeException e) {
			LOGGER.error("Failed recipe.toString", e);
			return recipe.getClass().toString();
		}
	}

	public static <T> String getIngredientInfo(T ingredient, IIngredientType<T> ingredientType) {
		IngredientManager ingredientManager = Internal.getIngredientManager();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		return ingredientHelper.getErrorInfo(ingredient);
	}

	private static Stream<String> truncatedStream(Stream<String> stream, int size, int limit) {
		if (size + 1 > limit) {
			return Stream.concat(
				stream.limit(limit),
				Stream.of(String.format("<truncated to %s elements, skipped %s>", limit, size - limit))
			);
		}
		return stream;
	}

	@SuppressWarnings("ConstantConditions")
	public static String getItemStackInfo(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			return "null";
		}
		Item item = itemStack.getItem();
		final String itemName;
		ResourceLocation registryName = item.getRegistryName();
		if (registryName != null) {
			itemName = registryName.toString();
		} else if (item instanceof BlockItem) {
			final String blockName;
			Block block = ((BlockItem) item).getBlock();
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
			itemName = "BlockItem(" + blockName + ")";
		} else {
			itemName = item.getClass().getName();
		}

		CompoundTag nbt = itemStack.getTag();
		if (nbt != null) {
			return itemStack + " " + itemName + " nbt:" + nbt;
		}
		return itemStack + " " + itemName;
	}

	@SuppressWarnings("ConstantConditions")
	public static String getFluidStackInfo(FluidStack fluidStack) {
		if (fluidStack == null) {
			return "null";
		}
		Fluid fluid = fluidStack.getFluid();
		final String fluidName;
		ResourceLocation registryName = fluid.getRegistryName();
		if (registryName != null) {
			fluidName = registryName.toString();
		} else {
			fluidName = fluid.getClass().getName();
		}

		CompoundTag nbt = fluidStack.getTag();
		if (nbt != null) {
			return fluidStack + " " + fluidName + " nbt:" + nbt;
		}
		return fluidStack + " " + fluidName;
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
	public static void checkNotEmpty(FluidStack fluidStack) {
		if (fluidStack == null) {
			throw new NullPointerException("FluidStack must not be null.");
		} else if (fluidStack.isEmpty()) {
			String info = getFluidStackInfo(fluidStack);
			throw new IllegalArgumentException("FluidStack value must not be empty. " + info);
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static <T> void checkNotEmpty(T[] values, String name) {
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

	public static <T> ReportedException createRenderIngredientException(Throwable throwable, final T ingredient) {
		IIngredientManager ingredientManager = Internal.getIngredientManager();
		IIngredientType<T> ingredientType = ingredientManager.getIngredientType(ingredient);
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering ingredient");
		CrashReportCategory ingredientCategory = crashreport.addCategory("Ingredient being rendered");

		if (modIdHelper != null) {
			ingredientCategory.setDetail("Mod Name", () -> {
				String modId = ingredientHelper.getDisplayModId(ingredient);
				return modIdHelper.getModNameForModId(modId);
			});
		}
		ingredientCategory.setDetail("Registry Name", () -> ingredientHelper.getResourceLocation(ingredient).toString());
		ingredientCategory.setDetail("Display Name", () -> ingredientHelper.getDisplayName(ingredient));
		ingredientCategory.setDetail("String Name", ingredient::toString);

		CrashReportCategory jeiCategory = crashreport.addCategory("JEI render details");
		jeiCategory.setDetail("Unique Id (for Blacklist)", () -> ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient));
		jeiCategory.setDetail("Ingredient Type", () -> ingredientType.getIngredientClass().toString());
		jeiCategory.setDetail("Error Info", () -> ingredientHelper.getErrorInfo(ingredient));
		if (worldConfig != null) {
			jeiCategory.setDetail("Filter Text", () -> worldConfig.getFilterText());
			jeiCategory.setDetail("Edit Mode Enabled", () -> Boolean.toString(worldConfig.isEditModeEnabled()));
		}
		jeiCategory.setDetail("Debug Mode Enabled", () -> Boolean.toString(ClientConfig.getInstance().isDebugModeEnabled()));

		throw new ReportedException(crashreport);
	}
}
