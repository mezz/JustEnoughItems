package mezz.jei.library.load;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableTable;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.StackHelper;
import mezz.jei.core.util.LoggedTimer;
import mezz.jei.library.config.IModIdFormatConfig;
import mezz.jei.library.config.RecipeCategorySortingConfig;
import mezz.jei.library.focus.FocusFactory;
import mezz.jei.library.gui.GuiHelper;
import mezz.jei.library.helpers.ModIdHelper;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.AdvancedRegistration;
import mezz.jei.library.load.registration.GuiHandlerRegistration;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.load.registration.RecipeCatalystRegistration;
import mezz.jei.library.load.registration.RecipeCategoryRegistration;
import mezz.jei.library.load.registration.RecipeRegistration;
import mezz.jei.library.load.registration.RecipeTransferRegistration;
import mezz.jei.library.load.registration.SubtypeRegistration;
import mezz.jei.library.load.registration.VanillaCategoryExtensionRegistration;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import mezz.jei.library.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.library.recipes.RecipeManager;
import mezz.jei.library.recipes.RecipeManagerInternal;
import mezz.jei.library.runtime.JeiHelpers;
import mezz.jei.library.startup.JeiClientExecutor;
import mezz.jei.library.transfer.RecipeTransferHandlerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class PluginLoader {
	private final IConnectionToServer serverConnection;
	private final PluginCaller pluginCaller;
	private final JeiClientExecutor clientExecutor;
	private final LoggedTimer timer;
	private final IIngredientManager ingredientManager;
	private final JeiHelpers jeiHelpers;

	public PluginLoader(
		IConnectionToServer serverConnection,
		PluginCaller pluginCaller,
		IModIdFormatConfig modIdFormatConfig,
		IColorHelper colorHelper,
		JeiClientExecutor clientExecutor
	) {
		this.serverConnection = serverConnection;
		this.pluginCaller = pluginCaller;
		this.clientExecutor = clientExecutor;
		this.timer = new LoggedTimer();

		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		SubtypeRegistration subtypeRegistration = new SubtypeRegistration();
		pluginCaller.callOnPlugins(
			"Registering item subtypes",
			p -> p.registerItemSubtypes(subtypeRegistration),
			p -> p.registerItemSubtypes(subtypeRegistration, clientExecutor)
		);
		pluginCaller.callOnPlugins(
			"Registering fluid subtypes",
			p -> p.registerFluidSubtypes(subtypeRegistration, fluidHelper),
			p -> p.registerFluidSubtypes(subtypeRegistration, fluidHelper, clientExecutor)
		);
		SubtypeInterpreters subtypeInterpreters = subtypeRegistration.getInterpreters();
		SubtypeManager subtypeManager = new SubtypeManager(subtypeInterpreters);

		IngredientManagerBuilder ingredientManagerBuilder = new IngredientManagerBuilder(subtypeManager, colorHelper);
		pluginCaller.callOnPlugins(
			"Registering ingredients",
			p -> p.registerIngredients(ingredientManagerBuilder),
			p -> p.registerIngredients(ingredientManagerBuilder, clientExecutor)
		);
		this.ingredientManager = ingredientManagerBuilder.build();

		StackHelper stackHelper = new StackHelper(subtypeManager);
		GuiHelper guiHelper = new GuiHelper(ingredientManager);
		FocusFactory focusFactory = new FocusFactory(ingredientManager);
		IModIdHelper modIdHelper = new ModIdHelper(modIdFormatConfig, ingredientManager);
		this.jeiHelpers = new JeiHelpers(guiHelper, stackHelper, modIdHelper, focusFactory, colorHelper, ingredientManager);
	}

	@Unmodifiable
	private List<IRecipeCategory<?>> createRecipeCategories(VanillaPlugin vanillaPlugin) {
		RecipeCategoryRegistration recipeCategoryRegistration = new RecipeCategoryRegistration(jeiHelpers);
		pluginCaller.callOnPlugins(
			"Registering categories",
			p -> p.registerCategories(recipeCategoryRegistration),
			p ->p.registerCategories(recipeCategoryRegistration, clientExecutor)
		);
		CraftingRecipeCategory craftingCategory = vanillaPlugin.getCraftingCategory()
			.orElseThrow(() -> new NullPointerException("vanilla crafting category"));
		VanillaCategoryExtensionRegistration vanillaCategoryExtensionRegistration = new VanillaCategoryExtensionRegistration(craftingCategory, jeiHelpers);
		pluginCaller.callOnPlugins(
			"Registering vanilla category extensions",
			p -> p.registerVanillaCategoryExtensions(vanillaCategoryExtensionRegistration),
			p -> p.registerVanillaCategoryExtensions(vanillaCategoryExtensionRegistration, clientExecutor)
		);
		return recipeCategoryRegistration.getRecipeCategories();
	}

	public IScreenHelper createGuiScreenHelper(IJeiHelpers jeiHelpers) {
		GuiHandlerRegistration guiHandlerRegistration = new GuiHandlerRegistration(jeiHelpers);
		pluginCaller.callOnPlugins(
			"Registering gui handlers",
			p -> p.registerGuiHandlers(guiHandlerRegistration),
			p -> p.registerGuiHandlers(guiHandlerRegistration, clientExecutor)
		);
		return guiHandlerRegistration.createGuiScreenHelper(ingredientManager);
	}

	public ImmutableTable<Class<? extends AbstractContainerMenu>, RecipeType<?>, IRecipeTransferHandler<?, ?>> createRecipeTransferHandlers() {
		IStackHelper stackHelper = jeiHelpers.getStackHelper();
		IRecipeTransferHandlerHelper handlerHelper = new RecipeTransferHandlerHelper(stackHelper);
		RecipeTransferRegistration recipeTransferRegistration = new RecipeTransferRegistration(stackHelper, handlerHelper, jeiHelpers, serverConnection);
		pluginCaller.callOnPlugins(
			"Registering recipes transfer handlers",
			p -> p.registerRecipeTransferHandlers(recipeTransferRegistration),
			p -> p.registerRecipeTransferHandlers(recipeTransferRegistration, clientExecutor)
		);
		return recipeTransferRegistration.getRecipeTransferHandlers();
	}

	public RecipeManager createRecipeManager(
		VanillaPlugin vanillaPlugin,
		RecipeCategorySortingConfig recipeCategorySortingConfig,
		IModIdHelper modIdHelper,
		IIngredientVisibility ingredientVisibility
	) {
		List<IRecipeCategory<?>> recipeCategories = createRecipeCategories(vanillaPlugin);

		RecipeCatalystRegistration recipeCatalystRegistration = new RecipeCatalystRegistration(ingredientManager, jeiHelpers);
		pluginCaller.callOnPlugins(
			"Registering recipe catalysts",
			p -> p.registerRecipeCatalysts(recipeCatalystRegistration),
			p -> p.registerRecipeCatalysts(recipeCatalystRegistration, clientExecutor)
		);
		ImmutableListMultimap<ResourceLocation, ITypedIngredient<?>> recipeCatalysts = recipeCatalystRegistration.getRecipeCatalysts();

		AdvancedRegistration advancedRegistration = new AdvancedRegistration(jeiHelpers);
		pluginCaller.callOnPlugins(
			"Registering advanced plugins",
			p -> p.registerAdvanced(advancedRegistration),
			p -> p.registerAdvanced(advancedRegistration, clientExecutor)
		);
		List<IRecipeManagerPlugin> recipeManagerPlugins = advancedRegistration.getRecipeManagerPlugins();

		timer.start("Building recipe registry");
		RecipeManagerInternal recipeManagerInternal = new RecipeManagerInternal(
			recipeCategories,
			recipeCatalysts,
			ingredientManager,
			recipeManagerPlugins,
			recipeCategorySortingConfig,
			ingredientVisibility
		);
		timer.stop();

		VanillaRecipeFactory vanillaRecipeFactory = new VanillaRecipeFactory(ingredientManager);
		RecipeRegistration recipeRegistration = new RecipeRegistration(jeiHelpers, ingredientManager, ingredientVisibility, vanillaRecipeFactory, recipeManagerInternal);
		pluginCaller.callOnPlugins(
			"Registering recipes",
			p -> p.registerRecipes(recipeRegistration),
			p -> p.registerRecipes(recipeRegistration, clientExecutor)
		);

		Textures textures = Internal.getTextures();
		return new RecipeManager(recipeManagerInternal, modIdHelper, ingredientManager, textures, ingredientVisibility, clientExecutor);
	}

	public IIngredientManager getIngredientManager() {
		return ingredientManager;
	}

	public JeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}
}
