package mezz.jei.common.load;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableTable;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.Internal;
import mezz.jei.common.bookmarks.BookmarkList;
import mezz.jei.common.config.IBookmarkConfig;
import mezz.jei.common.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.common.focus.FocusFactory;
import mezz.jei.common.gui.GuiHelper;
import mezz.jei.common.gui.GuiScreenHelper;
import mezz.jei.common.gui.ingredients.IListElement;
import mezz.jei.common.filter.IFilterTextSource;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.ingredients.IIngredientSorter;
import mezz.jei.common.ingredients.IngredientBlacklistInternal;
import mezz.jei.common.ingredients.IngredientFilter;
import mezz.jei.common.ingredients.IngredientListElementFactory;
import mezz.jei.common.ingredients.IngredientManager;
import mezz.jei.common.ingredients.IngredientVisibility;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.ingredients.subtypes.SubtypeManager;
import mezz.jei.common.load.registration.AdvancedRegistration;
import mezz.jei.common.load.registration.GuiHandlerRegistration;
import mezz.jei.common.load.registration.RecipeCatalystRegistration;
import mezz.jei.common.load.registration.RecipeCategoryRegistration;
import mezz.jei.common.load.registration.RecipeRegistration;
import mezz.jei.common.load.registration.RecipeTransferRegistration;
import mezz.jei.common.load.registration.RegisteredIngredientsBuilder;
import mezz.jei.common.load.registration.SubtypeRegistration;
import mezz.jei.common.load.registration.VanillaCategoryExtensionRegistration;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.Services;
import mezz.jei.common.plugins.vanilla.VanillaPlugin;
import mezz.jei.common.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.common.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.common.recipes.RecipeManager;
import mezz.jei.common.recipes.RecipeManagerInternal;
import mezz.jei.common.runtime.JeiHelpers;
import mezz.jei.common.startup.ConfigData;
import mezz.jei.common.startup.StartData;
import mezz.jei.common.transfer.RecipeTransferHandlerHelper;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.LoggedTimer;
import mezz.jei.common.util.RecipeErrorUtil;
import mezz.jei.common.util.StackHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class PluginLoader {
	private final StartData data;
	private final LoggedTimer timer;
	private final RegisteredIngredients registeredIngredients;
	private final IIngredientManager ingredientManager;
	private final JeiHelpers jeiHelpers;
	private final IIngredientVisibility ingredientVisibility;
	private final IngredientFilter ingredientFilter;

	public PluginLoader(StartData data, IFilterTextSource filterTextSource, IModIdHelper modIdHelper, IIngredientSorter ingredientSorter) {
		this.data = data;
		this.timer = new LoggedTimer();
		ConfigData configData = data.configData();

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();

		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		List<IModPlugin> plugins = data.plugins();
		SubtypeRegistration subtypeRegistration = new SubtypeRegistration();
		PluginCaller.callOnPlugins("Registering item subtypes", plugins, p -> p.registerItemSubtypes(subtypeRegistration));
		PluginCaller.callOnPlugins("Registering fluid subtypes", plugins, p -> {
			p.registerFluidSubtypes(subtypeRegistration, fluidHelper);
			//noinspection removal
			p.registerFluidSubtypes(subtypeRegistration);
		});
		SubtypeManager subtypeManager = new SubtypeManager(subtypeRegistration);

		RegisteredIngredientsBuilder registeredIngredientsBuilder = new RegisteredIngredientsBuilder(subtypeManager);
		PluginCaller.callOnPlugins("Registering ingredients", plugins, p -> p.registerIngredients(registeredIngredientsBuilder));
		this.registeredIngredients = registeredIngredientsBuilder.build();
		Internal.setRegisteredIngredients(this.registeredIngredients);
		RecipeErrorUtil.setRegisteredIngredients(this.registeredIngredients);

		this.ingredientVisibility = new IngredientVisibility(
			blacklist,
			configData.worldConfig(),
			configData.editModeConfig(),
			this.registeredIngredients
		);

		this.timer.start("Building ingredient list");
		NonNullList<IListElement<?>> ingredientList = IngredientListElementFactory.createBaseList(this.registeredIngredients);
		this.timer.stop();

		this.timer.start("Building ingredient filter");
		this.ingredientFilter = new IngredientFilter(
			filterTextSource,
			configData.clientConfig(),
			configData.ingredientFilterConfig(),
			registeredIngredients,
			ingredientSorter,
			ingredientList,
			modIdHelper,
			ingredientVisibility
		);
		this.timer.stop();

		this.ingredientManager = new IngredientManager(
			modIdHelper,
			blacklist,
			configData.clientConfig(),
			registeredIngredients,
			ingredientFilter
		);

		StackHelper stackHelper = new StackHelper(subtypeManager);
		GuiHelper guiHelper = new GuiHelper(registeredIngredients, data.textures());
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
		RecipeTransferRegistration recipeTransferRegistration = new RecipeTransferRegistration(jeiHelpers.getStackHelper(), handlerHelper, jeiHelpers, recipeManager, data.serverConnection());
		PluginCaller.callOnPlugins("Registering recipes transfer handlers", plugins, p -> p.registerRecipeTransferHandlers(recipeTransferRegistration));
		return recipeTransferRegistration.getRecipeTransferHandlers();
	}

	public RecipeManager createRecipeManager(
		List<IModPlugin> plugins,
		VanillaPlugin vanillaPlugin,
		RecipeCategorySortingConfig recipeCategorySortingConfig,
		IModIdHelper modIdHelper
	) {
		List<IRecipeCategory<?>> recipeCategories = createRecipeCategories(plugins, vanillaPlugin);

		RecipeCatalystRegistration recipeCatalystRegistration = new RecipeCatalystRegistration(registeredIngredients, ingredientManager);
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

		Textures textures = data.textures();
		return new RecipeManager(recipeManagerInternal, modIdHelper, registeredIngredients, textures, ingredientVisibility);
	}

	public IngredientFilter getIngredientFilter() {
		return ingredientFilter;
	}

	public IIngredientVisibility getIngredientVisibility() {
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

	public BookmarkList createBookmarkList(IBookmarkConfig bookmarkConfig) {
		timer.start("Building bookmarks");
		BookmarkList bookmarkList = new BookmarkList(registeredIngredients, bookmarkConfig);
		bookmarkConfig.loadBookmarks(registeredIngredients, bookmarkList);
		timer.stop();
		return bookmarkList;
	}
}
