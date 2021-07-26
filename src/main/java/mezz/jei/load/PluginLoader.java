package mezz.jei.load;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.BookmarkConfig;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.IIngredientSorter;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.ModIngredientRegistration;
import mezz.jei.ingredients.RegisteredIngredient;
import mezz.jei.ingredients.SubtypeManager;
import mezz.jei.load.registration.AdvancedRegistration;
import mezz.jei.load.registration.GuiHandlerRegistration;
import mezz.jei.load.registration.RecipeCatalystRegistration;
import mezz.jei.load.registration.RecipeCategoryRegistration;
import mezz.jei.load.registration.RecipeRegistration;
import mezz.jei.load.registration.RecipeTransferRegistration;
import mezz.jei.load.registration.SubtypeRegistration;
import mezz.jei.load.registration.VanillaCategoryExtensionRegistration;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.recipes.RecipeManagerInternal;
import mezz.jei.recipes.RecipeManager;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.transfer.RecipeTransferHandlerHelper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.LoggedTimer;
import mezz.jei.util.StackHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class PluginLoader {
	private final LoggedTimer timer;
	private final IModIdHelper modIdHelper;
	private final IngredientBlacklistInternal blacklist;
	private final AdvancedRegistration advancedRegistration;
	private final IngredientManager ingredientManager;
	private final IClientConfig clientConfig;
	private final RecipeTransferRegistration recipeTransferRegistration;
	private final GuiHandlerRegistration guiHandlerRegistration;
	private final ImmutableList<IRecipeCategory<?>> recipeCategories;
	private final ImmutableListMultimap<ResourceLocation, Object> recipeCatalysts;
	private final ImmutableListMultimap<ResourceLocation, Object> recipes;
	private final ImmutableList<IRecipeManagerPlugin> recipeManagerPlugins;
	@Nullable
	private IRecipeManager recipeManager;
	@Nullable
	private IngredientFilter ingredientFilter;
	@Nullable
	private BookmarkList bookmarkList;

	public PluginLoader(
		List<IModPlugin> plugins,
		VanillaPlugin vanillaPlugin,
		Textures textures,
		IClientConfig clientConfig,
		IModIdHelper modIdHelper,
		boolean debugMode) {
		this.clientConfig = clientConfig;
		this.timer = new LoggedTimer();
		this.modIdHelper = modIdHelper;
		this.blacklist = new IngredientBlacklistInternal();

		SubtypeRegistration subtypeRegistration = new SubtypeRegistration();
		PluginCaller.callOnPlugins("Registering item subtypes", plugins, p -> p.registerItemSubtypes(subtypeRegistration));
		PluginCaller.callOnPlugins("Registering fluid subtypes", plugins, p -> p.registerFluidSubtypes(subtypeRegistration));
		SubtypeManager subtypeManager = new SubtypeManager(subtypeRegistration);

		ModIngredientRegistration modIngredientManager = new ModIngredientRegistration(subtypeManager);
		PluginCaller.callOnPlugins("Registering ingredients", plugins, p -> p.registerIngredients(modIngredientManager));
		List<RegisteredIngredient<?>> registeredIngredients = modIngredientManager.getRegisteredIngredients();
		ingredientManager = new IngredientManager(modIdHelper, blacklist, registeredIngredients, debugMode);
		Internal.setIngredientManager(ingredientManager);

		StackHelper stackHelper = new StackHelper(subtypeManager);
		Internal.setTextures(textures);
		GuiHelper guiHelper = new GuiHelper(ingredientManager, textures);
		JeiHelpers jeiHelpers = new JeiHelpers(guiHelper, stackHelper, modIdHelper);
		Internal.setHelpers(jeiHelpers);

		VanillaRecipeFactory vanillaRecipeFactory = new VanillaRecipeFactory(ingredientManager);
		IRecipeTransferHandlerHelper handlerHelper = new RecipeTransferHandlerHelper();
		recipeTransferRegistration = new RecipeTransferRegistration(jeiHelpers.getStackHelper(), handlerHelper, jeiHelpers);

		RecipeCategoryRegistration recipeCategoryRegistration = new RecipeCategoryRegistration(jeiHelpers);
		PluginCaller.callOnPlugins("Registering categories", plugins, p -> p.registerCategories(recipeCategoryRegistration));
		CraftingRecipeCategory craftingCategory = vanillaPlugin.getCraftingCategory();
		ErrorUtil.checkNotNull(craftingCategory, "vanilla crafting category");
		VanillaCategoryExtensionRegistration vanillaCategoryExtensionRegistration = new VanillaCategoryExtensionRegistration(craftingCategory);
		PluginCaller.callOnPlugins("Registering vanilla category extensions", plugins, p -> p.registerVanillaCategoryExtensions(vanillaCategoryExtensionRegistration));
		ImmutableMap<ResourceLocation, IRecipeCategory<?>> recipeCategoriesByUid = recipeCategoryRegistration.getRecipeCategoriesByUid();
		recipeCategories = recipeCategoryRegistration.getRecipeCategories();

		RecipeRegistration recipeRegistration = new RecipeRegistration(recipeCategoriesByUid, jeiHelpers, ingredientManager, vanillaRecipeFactory);
		PluginCaller.callOnPlugins("Registering recipes", plugins, p -> p.registerRecipes(recipeRegistration));
		PluginCaller.callOnPlugins("Registering recipes transfer handlers", plugins, p -> p.registerRecipeTransferHandlers(recipeTransferRegistration));
		recipes = recipeRegistration.getRecipes();

		RecipeCatalystRegistration recipeCatalystRegistration = new RecipeCatalystRegistration();
		PluginCaller.callOnPlugins("Registering recipe catalysts", plugins, p -> p.registerRecipeCatalysts(recipeCatalystRegistration));
		recipeCatalysts = recipeCatalystRegistration.getRecipeCatalysts();

		guiHandlerRegistration = new GuiHandlerRegistration();
		PluginCaller.callOnPlugins("Registering gui handlers", plugins, p -> p.registerGuiHandlers(guiHandlerRegistration));

		advancedRegistration = new AdvancedRegistration(jeiHelpers);
		PluginCaller.callOnPlugins("Registering advanced plugins", plugins, p -> p.registerAdvanced(advancedRegistration));
		recipeManagerPlugins = advancedRegistration.getRecipeManagerPlugins();
	}

	public GuiHandlerRegistration getGuiHandlerRegistration() {
		return guiHandlerRegistration;
	}

	public RecipeTransferRegistration getRecipeTransferRegistration() {
		return recipeTransferRegistration;
	}

	public IRecipeManager getRecipeManager(RecipeCategorySortingConfig recipeCategorySortingConfig) {
		if (recipeManager == null) {
			timer.start("Building recipe registry");
			RecipeManagerInternal recipeManagerInternal = new RecipeManagerInternal(
				recipeCategories,
				recipes,
				recipeCatalysts,
				ingredientManager,
				recipeManagerPlugins,
				recipeCategorySortingConfig
			);
			this.recipeManager = new RecipeManager(recipeManagerInternal, modIdHelper);
			timer.stop();
		}
		return recipeManager;
	}

	public IngredientFilter createIngredientFilter(IIngredientSorter ingredientSorter, IEditModeConfig editModeConfig, IIngredientFilterConfig ingredientFilterConfig) {
		if (ingredientFilter == null) {
			timer.start("Building ingredient list");
			NonNullList<IIngredientListElement<?>> ingredientList = IngredientListElementFactory.createBaseList(ingredientManager);
			timer.stop();
			timer.start("Building ingredient filter");
			ingredientFilter = new IngredientFilter(blacklist, clientConfig, ingredientFilterConfig, editModeConfig, ingredientManager, ingredientSorter, ingredientList, modIdHelper);
			Internal.setIngredientFilter(ingredientFilter);
			timer.stop();
		}
		return ingredientFilter;
	}

	public IngredientManager getIngredientManager() {
		return ingredientManager;
	}

	public BookmarkList createBookmarkList(BookmarkConfig bookmarkConfig) {
		if (bookmarkList == null) {
			timer.start("Building bookmarks");
			bookmarkList = new BookmarkList(ingredientManager, bookmarkConfig);
			bookmarkConfig.loadBookmarks(ingredientManager, bookmarkList);
			timer.stop();
		}
		return bookmarkList;
	}
}
