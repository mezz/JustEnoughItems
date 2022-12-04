package mezz.jei.library.load;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableTable;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.common.focus.FocusFactory;
import mezz.jei.common.gui.GuiHelper;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.ingredients.IngredientManager;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.ingredients.subtypes.SubtypeManager;
import mezz.jei.common.load.PluginCaller;
import mezz.jei.common.load.registration.AdvancedRegistration;
import mezz.jei.common.load.registration.RecipeCatalystRegistration;
import mezz.jei.common.load.registration.RecipeCategoryRegistration;
import mezz.jei.common.load.registration.RecipeTransferRegistration;
import mezz.jei.common.load.registration.RegisteredIngredientsBuilder;
import mezz.jei.common.load.registration.SubtypeRegistration;
import mezz.jei.common.load.registration.VanillaCategoryExtensionRegistration;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.Services;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.common.runtime.JeiHelpers;
import mezz.jei.common.transfer.RecipeTransferHandlerHelper;
import mezz.jei.common.util.LoggedTimer;
import mezz.jei.common.util.StackHelper;
import mezz.jei.library.load.registration.GuiHandlerRegistration;
import mezz.jei.library.load.registration.RecipeRegistration;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import mezz.jei.library.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.library.recipes.RecipeManager;
import mezz.jei.library.recipes.RecipeManagerInternal;
import mezz.jei.library.startup.StartData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class PluginLoader {
	private final StartData data;
	private final LoggedTimer timer;
	private final RegisteredIngredients registeredIngredients;
	private final IIngredientManager ingredientManager;
	private final JeiHelpers jeiHelpers;

	public PluginLoader(StartData data, IModIdHelper modIdHelper, IColorHelper colorHelper) {
		this.data = data;
		this.timer = new LoggedTimer();

		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		List<IModPlugin> plugins = data.plugins();
		SubtypeRegistration subtypeRegistration = new SubtypeRegistration();
		PluginCaller.callOnPlugins("Registering item subtypes", plugins, p -> p.registerItemSubtypes(subtypeRegistration));
		PluginCaller.callOnPlugins("Registering fluid subtypes", plugins, p ->
			p.registerFluidSubtypes(subtypeRegistration, fluidHelper)
		);
		SubtypeManager subtypeManager = new SubtypeManager(subtypeRegistration);

		RegisteredIngredientsBuilder registeredIngredientsBuilder = new RegisteredIngredientsBuilder(subtypeManager, colorHelper);
		PluginCaller.callOnPlugins("Registering ingredients", plugins, p -> p.registerIngredients(registeredIngredientsBuilder));
		this.registeredIngredients = registeredIngredientsBuilder.build();

		this.ingredientManager = new IngredientManager(registeredIngredients);

		StackHelper stackHelper = new StackHelper(subtypeManager);
		GuiHelper guiHelper = new GuiHelper(registeredIngredients, data.textures());
		FocusFactory focusFactory = new FocusFactory(registeredIngredients);
		this.jeiHelpers = new JeiHelpers(guiHelper, stackHelper, modIdHelper, focusFactory, colorHelper, registeredIngredients);
	}

	@Unmodifiable
	private List<IRecipeCategory<?>> createRecipeCategories(List<IModPlugin> plugins, VanillaPlugin vanillaPlugin) {
		RecipeCategoryRegistration recipeCategoryRegistration = new RecipeCategoryRegistration(jeiHelpers);
		PluginCaller.callOnPlugins("Registering categories", plugins, p -> p.registerCategories(recipeCategoryRegistration));
		CraftingRecipeCategory craftingCategory = vanillaPlugin.getCraftingCategory()
			.orElseThrow(() -> new NullPointerException("vanilla crafting category"));
		VanillaCategoryExtensionRegistration vanillaCategoryExtensionRegistration = new VanillaCategoryExtensionRegistration(craftingCategory);
		PluginCaller.callOnPlugins("Registering vanilla category extensions", plugins, p -> p.registerVanillaCategoryExtensions(vanillaCategoryExtensionRegistration));
		return recipeCategoryRegistration.getRecipeCategories();
	}

	public IScreenHelper createGuiScreenHelper(List<IModPlugin> plugins, IJeiHelpers jeiHelpers) {
		GuiHandlerRegistration guiHandlerRegistration = new GuiHandlerRegistration(jeiHelpers);
		PluginCaller.callOnPlugins("Registering gui handlers", plugins, p -> p.registerGuiHandlers(guiHandlerRegistration));
		return guiHandlerRegistration.createGuiScreenHelper(registeredIngredients);
	}

	public ImmutableTable<Class<? extends AbstractContainerMenu>, RecipeType<?>, IRecipeTransferHandler<?, ?>> createRecipeTransferHandlers(List<IModPlugin> plugins) {
		IStackHelper stackHelper = jeiHelpers.getStackHelper();
		IRecipeTransferHandlerHelper handlerHelper = new RecipeTransferHandlerHelper(stackHelper);
		RecipeTransferRegistration recipeTransferRegistration = new RecipeTransferRegistration(stackHelper, handlerHelper, this.jeiHelpers, data.serverConnection());
		PluginCaller.callOnPlugins("Registering recipes transfer handlers", plugins, p -> p.registerRecipeTransferHandlers(recipeTransferRegistration));
		return recipeTransferRegistration.getRecipeTransferHandlers();
	}

	public RecipeManager createRecipeManager(
		List<IModPlugin> plugins,
		VanillaPlugin vanillaPlugin,
		RecipeCategorySortingConfig recipeCategorySortingConfig,
		IModIdHelper modIdHelper,
		IIngredientVisibility ingredientVisibility
	) {
		List<IRecipeCategory<?>> recipeCategories = createRecipeCategories(plugins, vanillaPlugin);

		RecipeCatalystRegistration recipeCatalystRegistration = new RecipeCatalystRegistration(registeredIngredients, ingredientManager, jeiHelpers);
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
			recipeCategorySortingConfig
		);
		timer.stop();

		VanillaRecipeFactory vanillaRecipeFactory = new VanillaRecipeFactory(registeredIngredients);
		RecipeRegistration recipeRegistration = new RecipeRegistration(jeiHelpers, registeredIngredients, ingredientManager, ingredientVisibility, vanillaRecipeFactory, recipeManagerInternal);
		PluginCaller.callOnPlugins("Registering recipes", plugins, p -> p.registerRecipes(recipeRegistration));

		Textures textures = data.textures();
		return new RecipeManager(recipeManagerInternal, modIdHelper, registeredIngredients, textures);
	}

	public IRegisteredIngredients getRegisteredIngredients() {
		return registeredIngredients;
	}

	public IIngredientManager getIngredientManager() {
		return ingredientManager;
	}

	public JeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}
}
