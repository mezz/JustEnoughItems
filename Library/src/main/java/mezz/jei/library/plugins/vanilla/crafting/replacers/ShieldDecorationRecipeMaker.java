package mezz.jei.library.plugins.vanilla.crafting.replacers;

import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.platform.IPlatformRecipeHelper.ShieldDecorationRecipeData;
import mezz.jei.common.util.RegistryUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.ShieldDecorationRecipe;
import net.minecraft.world.item.crafting.TransmuteRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public final class ShieldDecorationRecipeMaker implements IRecipeReplacer {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String GROUP = "jei.shield.decoration";
	private static final String JEI_RECIPE_PATH_PREFIX = "jei.shield.decoration.";
	private final IPlatformRecipeHelper recipeHelper;

	public ShieldDecorationRecipeMaker(IPlatformRecipeHelper recipeHelper) {
		this.recipeHelper = recipeHelper;
	}

	@Override
	public boolean replace(RecipeHolder<CraftingRecipe> recipe, Consumer<RecipeHolder<CraftingRecipe>> replacements) {
		if (recipe.value() instanceof ShieldDecorationRecipe shieldDecorationRecipe) {
			try {
				createRecipes(recipe.id(), shieldDecorationRecipe, replacements);
			} catch (RuntimeException e) {
				LOGGER.warn("Skipping shield decoration recipe {} because JEI failed to create replacement recipes.", recipe.id().identifier(), e);
			}
		}
		return false;
	}

	private void createRecipes(
		ResourceKey<Recipe<?>> originalRecipeId,
		ShieldDecorationRecipe shieldDecorationRecipe,
		Consumer<RecipeHolder<CraftingRecipe>> recipes
	) {
		ShieldDecorationRecipeData recipeData = recipeHelper.getShieldDecorationRecipeData(shieldDecorationRecipe);
		if (recipeData.target().isEmpty()) {
			LOGGER.warn("Skipping shield decoration recipe {} because its target ingredient is empty.", originalRecipeId.identifier());
			return;
		}
		ItemStack target = getFirstItemStack(recipeData.target());
		if (target == null) {
			LOGGER.warn("Skipping shield decoration recipe {} because its target ingredient has no item stacks.", originalRecipeId.identifier());
			return;
		}

		List<BannerItem> banners = getBannerItems(recipeData.banner());
		if (banners.isEmpty()) {
			LOGGER.warn("Skipping shield decoration recipe {} because its banner ingredient has no banner items.", originalRecipeId.identifier());
			return;
		}

		for (BannerItem banner : banners) {
			RecipeHolder<CraftingRecipe> recipe = createRecipe(originalRecipeId, recipeData, target, banner);
			if (recipe != null) {
				recipes.accept(recipe);
			}
		}
	}

	private static List<BannerItem> getBannerItems(Ingredient bannerIngredient) {
		return RegistryUtil.getRegistry(Registries.ITEM)
			.listElements()
			.filter(Holder::isBound)
			.filter(holder -> holder.value() instanceof BannerItem)
			.filter(bannerIngredient::acceptsItem)
			.map(Holder::value)
			.map(BannerItem.class::cast)
			.distinct()
			.toList();
	}

	@SuppressWarnings("deprecation")
	private static @Nullable ItemStack getFirstItemStack(Ingredient ingredient) {
		return ingredient.items()
			.filter(Holder::isBound)
			.findFirst()
			.map(ItemStack::new)
			.orElse(null);
	}

	private static @Nullable RecipeHolder<CraftingRecipe> createRecipe(ResourceKey<Recipe<?>> originalRecipeId, ShieldDecorationRecipeData recipeData, ItemStack target, BannerItem banner) {
		ItemStackTemplate output = createOutput(originalRecipeId, recipeData.result(), target, banner);
		if (output == null) {
			return null;
		}

		Identifier id = createRecipeId(originalRecipeId, banner);
		ResourceKey<Recipe<?>> resourceKey = ResourceKey.create(Registries.RECIPE, id);
		CraftingRecipe recipe = new ShapelessRecipe(
			new Recipe.CommonInfo(false),
			new CraftingRecipe.CraftingBookInfo(
				CraftingBookCategory.MISC,
				GROUP
			),
			output,
			List.of(
				recipeData.target(),
				Ingredient.of(banner)
			)
		);
		return new RecipeHolder<>(resourceKey, recipe);
	}

	private static Identifier createRecipeId(ResourceKey<Recipe<?>> originalRecipeId, BannerItem banner) {
		Identifier originalId = originalRecipeId.identifier();
		Identifier bannerId = getBannerId(banner);
		String path = JEI_RECIPE_PATH_PREFIX + originalId.getPath() + "." + bannerId.getNamespace() + "." + bannerId.getPath();
		return Identifier.fromNamespaceAndPath(originalId.getNamespace(), path);
	}

	private static @Nullable ItemStackTemplate createOutput(ResourceKey<Recipe<?>> originalRecipeId, ItemStackTemplate result, ItemStack target, BannerItem banner) {
		ItemStack output = TransmuteRecipe.createWithOriginalComponents(result, target);
		if (output.isEmpty()) {
			LOGGER.warn("Skipping shield decoration recipe {} for banner {} because its result item stack is empty.", originalRecipeId.identifier(), getBannerId(banner));
			return null;
		}
		ItemStack bannerStack = new ItemStack(banner);
		output.set(DataComponents.BANNER_PATTERNS, bannerStack.get(DataComponents.BANNER_PATTERNS));
		output.set(DataComponents.BASE_COLOR, banner.getColor());
		return ItemStackTemplate.fromNonEmptyStack(output);
	}

	private static Identifier getBannerId(BannerItem banner) {
		Identifier bannerId = RegistryUtil.getRegistry(Registries.ITEM).getKey(banner);
		if (bannerId == null) {
			throw new IllegalStateException("Banner item is not registered: " + banner);
		}
		return bannerId;
	}
}
