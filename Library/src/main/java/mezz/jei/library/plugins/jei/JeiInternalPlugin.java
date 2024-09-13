package mezz.jei.library.plugins.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.library.plugins.jei.info.IngredientInfoRecipeCategory;
import mezz.jei.library.plugins.jei.tags.ITagInfoRecipe;
import mezz.jei.library.plugins.jei.tags.TagInfoRecipeCategory;
import mezz.jei.library.plugins.jei.tags.TagInfoRecipeMaker;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@JeiPlugin
public class JeiInternalPlugin implements IModPlugin {
	private final List<TagInfoRecipeMaker<?, ?>> tagInfoRecipeMakers = new ArrayList<>();

	@Override
	public ResourceLocation getPluginUid() {
		return ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "internal");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		IIngredientManager ingredientManager = jeiHelpers.getIngredientManager();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		Textures textures = Internal.getTextures();

		registration.addRecipeCategories(new IngredientInfoRecipeCategory(guiHelper, textures));

		tagInfoRecipeMakers.clear();
		IJeiClientConfigs jeiClientConfigs = Internal.getJeiClientConfigs();
		IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
		if (clientConfig.isShowTagRecipesEnabled()) {
			RegistryUtil.getRegistryAccess()
				.registries()
				.forEach(entry -> {
					Registry<?> registry = entry.value();
					createAndRegisterTagCategory(registration, tagInfoRecipeMakers, ingredientManager, registry);
				});
		}
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		IJeiClientConfigs jeiClientConfigs = Internal.getJeiClientConfigs();
		IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
		if (clientConfig.isShowTagRecipesEnabled()) {
			for (TagInfoRecipeMaker<?, ?> data : tagInfoRecipeMakers) {
				data.addRecipes(registration);
			}
		}
		tagInfoRecipeMakers.clear();
	}

	private static <B> void createAndRegisterTagCategory(
		IRecipeCategoryRegistration registration,
		List<TagInfoRecipeMaker<?, ?>> tagInfoRecipeMakers,
		IIngredientManager ingredientManager,
		Registry<B> registry
	) {
		registry.getAny()
			.ifPresent(holder -> {
				IJeiHelpers jeiHelpers = registration.getJeiHelpers();
				IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

				B ingredient = holder.value();

				IIngredientType<B> type = ingredientManager.getIngredientTypeChecked(ingredient).orElse(null);
				if (type != null) {
					ResourceLocation registryLocation = registry.key().location();
					RecipeType<ITagInfoRecipe> recipeType = createTagInfoRecipeType(registryLocation);
					registration.addRecipeCategories(
						new TagInfoRecipeCategory<>(guiHelper, recipeType, registryLocation)
					);
					tagInfoRecipeMakers.add(new TagInfoRecipeMaker<>(type, recipeType, Function.identity(), registry.key()));
					return;
				}

				IIngredientTypeWithSubtypes<B, Object> typeWithSubtypes = ingredientManager.getIngredientTypeWithSubtypesFromBase(ingredient).orElse(null);
				if (typeWithSubtypes != null) {
					if (createAndRegisterTagCategory(registration, tagInfoRecipeMakers, registry, ingredient, typeWithSubtypes)) {
						return;
					}
				}

				if (ingredient instanceof ItemLike) {
					@SuppressWarnings("unchecked")
					Registry<? extends ItemLike> itemLikeRegistry = (Registry<? extends ItemLike>) registry;
					if (createAndRegisterItemLikeTagCategory(registration, tagInfoRecipeMakers, itemLikeRegistry)) {
						return;
					}
				}
		});
	}

	private static RecipeType<ITagInfoRecipe> createTagInfoRecipeType(ResourceLocation registryLocation) {
		return RecipeType.create(registryLocation.getNamespace(), "tag_recipes/" + registryLocation.getPath(), ITagInfoRecipe.class);
	}

	private static <B, I> boolean createAndRegisterTagCategory(
		IRecipeCategoryRegistration registration,
		List<TagInfoRecipeMaker<?, ?>> tagInfoRecipeMakers,
		Registry<B> registry,
		B baseIngredient,
		IIngredientTypeWithSubtypes<B, I> knownType
	) {
		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		try {
			knownType.getDefaultIngredient(baseIngredient);
		} catch (UnsupportedOperationException ignored) {
			// this method is optional and may not be supported
			return false;
		}
		ResourceLocation registryLocation = registry.key().location();

		RecipeType<ITagInfoRecipe> recipeType = createTagInfoRecipeType(registryLocation);

		registration.addRecipeCategories(
			new TagInfoRecipeCategory<>(guiHelper, recipeType, registryLocation)
		);
		tagInfoRecipeMakers.add(new TagInfoRecipeMaker<>(knownType, recipeType, knownType::getDefaultIngredient, registry.key()));
		return true;
	}

	private static <B extends ItemLike> boolean createAndRegisterItemLikeTagCategory(
		IRecipeCategoryRegistration registration,
		List<TagInfoRecipeMaker<?, ?>> tagInfoRecipeMakers,
		Registry<B> registry
	) {
		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

		ResourceKey<? extends Registry<B>> registryKey = registry.key();
		ResourceLocation registryLocation = registryKey.location();
		RecipeType<ITagInfoRecipe> recipeType = createTagInfoRecipeType(registryLocation);
		registration.addRecipeCategories(
			new TagInfoRecipeCategory<>(guiHelper, recipeType, registryLocation)
		);
		tagInfoRecipeMakers.add(new TagInfoRecipeMaker<>(
			VanillaTypes.ITEM_STACK,
			recipeType,
			i -> i.asItem().getDefaultInstance(),
			registryKey
		));
		return true;
	}
}
