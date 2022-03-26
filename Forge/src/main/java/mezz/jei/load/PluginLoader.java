package mezz.jei.load;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableTable;
import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.common.network.IServerConnection;
import mezz.jei.config.BookmarkConfig;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.overlay.IFilterTextSource;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.IIngredientSorter;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.IngredientVisibility;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.ingredients.RegisteredIngredientsBuilder;
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
import mezz.jei.recipes.FocusFactory;
import mezz.jei.recipes.RecipeManager;
import mezz.jei.recipes.RecipeManagerInternal;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.transfer.RecipeTransferHandlerHelper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.common.util.LoggedTimer;
import mezz.jei.util.StackHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class PluginLoader {
	private final LoggedTimer timer;
	private final IModIdHelper modIdHelper;
	private final RegisteredIngredients registeredIngredients;
	private final IIngredientManager ingredientManager;
	private final JeiHelpers jeiHelpers;
	private final IngredientVisibility ingredientVisibility;
	private final IngredientFilter ingredientFilter;
	private final IServerConnection serverConnection;

	public PluginLoader(
		List<IModPlugin> plugins,
		Textures textures,
		IClientConfig clientConfig,
		IModIdHelper modIdHelper,
		IIngredientSorter ingredientSorter,
		IIngredientFilterConfig ingredientFilterConfig,
		IWorldConfig worldConfig,
		IEditModeConfig editModeConfig,
		IFilterTextSource filterTextSource,
		IServerConnection serverConnection
	) {
		this.timer = new LoggedTimer();
		this.modIdHelper = modIdHelper;
		this.serverConnection = serverConnection;
		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();

		SubtypeRegistration subtypeRegistration = new SubtypeRegistration();
		PluginCaller.callOnPlugins("Registering item subtypes", plugins, p -> p.registerItemSubtypes(subtypeRegistration));
		PluginCaller.callOnPlugins("Registering fluid subtypes", plugins, p -> p.registerFluidSubtypes(subtypeRegistration));
		SubtypeManager subtypeManager = new SubtypeManager(subtypeRegistration);

		RegisteredIngredientsBuilder registeredIngredientsBuilder = new RegisteredIngredientsBuilder(subtypeManager);
		PluginCaller.callOnPlugins("Registering ingredients", plugins, p -> p.registerIngredients(registeredIngredientsBuilder));
		this.registeredIngredients = registeredIngredientsBuilder.build();
		Internal.setIngredientManager(this.registeredIngredients);

		this.ingredientVisibility = new IngredientVisibility(blacklist, worldConfig, editModeConfig, this.registeredIngredients);
		Internal.setIngredientVisibility(this.ingredientVisibility);

		this.timer.start("Building ingredient list");
		NonNullList<IListElement<?>> ingredientList = IngredientListElementFactory.createBaseList(this.registeredIngredients);
		this.timer.stop();

		this.timer.start("Building ingredient filter");
		this.ingredientFilter = new IngredientFilter(
			filterTextSource,
			clientConfig,
			ingredientFilterConfig,
			registeredIngredients,
			ingredientSorter,
			ingredientList,
			modIdHelper,
			ingredientVisibility
		);
		this.timer.stop();

		this.ingredientManager = new IngredientManager(modIdHelper, blacklist, clientConfig, registeredIngredients, ingredientFilter);

		StackHelper stackHelper = new StackHelper(subtypeManager);
		GuiHelper guiHelper = new GuiHelper(registeredIngredients, textures);
		FocusFactory focusFactory = new FocusFactory(registeredIngredients);
		this.jeiHelpers = new JeiHelpers(guiHelper, stackHelper, modIdHelper, focusFactory);
		Internal.setHelpers(jeiHelpers);
	}

	@Unmodifiable
	private List<IRecipeCategory<?>> createRecipeCategories(List<IModPlugin> plugins, VanillaPlugin vanillaPlugin) {
		RecipeCategoryRegistration recipeCategoryRegistration = new RecipeCategoryRegistration(jeiHelpers);
		PluginCaller.callOnPlugins("Registering categories", plugins, p -> p.registerCategories(recipeCategoryRegistration));
		CraftingRecipeCategory craftingCategory = vanillaPlugin.getCraftingCategory();
		ErrorUtil.checkNotNull(craftingCategory, "vanilla crafting category");
		VanillaCategoryExtensionRegistration vanillaCategoryExtensionRegistration = new VanillaCategoryExtensionRegistration(craftingCategory);
		PluginCaller.callOnPlugins("Registering vanilla category extensions", plugins, p -> p.registerVanillaCategoryExtensions(vanillaCategoryExtensionRegistration));
		return recipeCategoryRegistration.getRecipeCategories();
	}

	public GuiScreenHelper createGuiScreenHelper(List<IModPlugin> plugins) {
		GuiHandlerRegistration guiHandlerRegistration = new GuiHandlerRegistration();
		PluginCaller.callOnPlugins("Registering gui handlers", plugins, p -> p.registerGuiHandlers(guiHandlerRegistration));
		return guiHandlerRegistration.createGuiScreenHelper(registeredIngredients);
	}

	public ImmutableTable<Class<?>, RecipeType<?>, IRecipeTransferHandler<?, ?>> createRecipeTransferHandlers(List<IModPlugin> plugins, RecipeManager recipeManager) {
		IRecipeTransferHandlerHelper handlerHelper = new RecipeTransferHandlerHelper();
		RecipeTransferRegistration recipeTransferRegistration = new RecipeTransferRegistration(jeiHelpers.getStackHelper(), handlerHelper, jeiHelpers, recipeManager, serverConnection);
		PluginCaller.callOnPlugins("Registering recipes transfer handlers", plugins, p -> p.registerRecipeTransferHandlers(recipeTransferRegistration));
		return recipeTransferRegistration.getRecipeTransferHandlers();
	}

	public RecipeManager createRecipeManager(
		List<IModPlugin> plugins,
		VanillaPlugin vanillaPlugin,
		RecipeCategorySortingConfig recipeCategorySortingConfig
	) {
		List<IRecipeCategory<?>> recipeCategories = createRecipeCategories(plugins, vanillaPlugin);

		RecipeCatalystRegistration recipeCatalystRegistration = new RecipeCatalystRegistration(registeredIngredients);
		PluginCaller.callOnPlugins("Registering recipe catalysts", plugins, p -> p.registerRecipeCatalysts(recipeCatalystRegistration));
		ImmutableListMultimap<ResourceLocation, ITypedIngredient<?>> recipeCatalysts = recipeCatalystRegistration.getRecipeCatalysts();

		AdvancedRegistration advancedRegistration = new AdvancedRegistration(jeiHelpers);
		PluginCaller.callOnPlugins("Registering advanced plugins", plugins, p -> p.registerAdvanced(advancedRegistration));
		List<IRecipeManagerPlugin> recipeManagerPlugins = advancedRegistration.getRecipeManagerPlugins();

		timer.start("Building recipe registry");
		RecipeManagerInternal recipeManagerInternal = new RecipeManagerInternal(
			recipeCategories,
			recipeCatalysts,
			registeredIngredients,
			recipeManagerPlugins,
			recipeCategorySortingConfig,
			ingredientVisibility
		);
		timer.stop();

		VanillaRecipeFactory vanillaRecipeFactory = new VanillaRecipeFactory(registeredIngredients);
		RecipeRegistration recipeRegistration = new RecipeRegistration(jeiHelpers, registeredIngredients, ingredientManager, ingredientVisibility, vanillaRecipeFactory, recipeManagerInternal);
		PluginCaller.callOnPlugins("Registering recipes", plugins, p -> p.registerRecipes(recipeRegistration));

		return new RecipeManager(recipeManagerInternal, modIdHelper, registeredIngredients);
	}

	public IngredientFilter getIngredientFilter() {
		return ingredientFilter;
	}

	public IngredientVisibility getIngredientVisibility() {
		return ingredientVisibility;
	}

	public RegisteredIngredients getRegisteredIngredients() {
		return registeredIngredients;
	}

	public IIngredientManager getIngredientManager() {
		return ingredientManager;
	}

	public JeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	public BookmarkList createBookmarkList(BookmarkConfig bookmarkConfig) {
		timer.start("Building bookmarks");
		BookmarkList bookmarkList = new BookmarkList(registeredIngredients, bookmarkConfig);
		bookmarkConfig.loadBookmarks(registeredIngredients, bookmarkList);
		timer.stop();
		return bookmarkList;
	}
}
