package mezz.jei.library.plugins.debug;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.recipe.advanced.IRecipeButtonControllerFactory;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IModInfoRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.library.plugins.debug.ingredients.DebugIngredient;
import mezz.jei.library.plugins.debug.ingredients.DebugIngredientHelper;
import mezz.jei.library.plugins.debug.ingredients.DebugIngredientListFactory;
import mezz.jei.library.plugins.debug.ingredients.DebugIngredientRenderer;
import mezz.jei.library.plugins.debug.ingredients.ErrorIngredient;
import mezz.jei.library.plugins.debug.ingredients.ErrorIngredientHelper;
import mezz.jei.library.plugins.debug.ingredients.ErrorIngredientListFactory;
import mezz.jei.library.plugins.debug.ingredients.ErrorIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@JeiPlugin
public class JeiDebugPlugin implements IModPlugin {
	private @Nullable DebugRecipeCategory<?> debugRecipeCategory;

	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "debug");
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		if (DebugConfig.isDebugModeEnabled()) {
			DebugIngredientHelper ingredientHelper = new DebugIngredientHelper();
			DebugIngredientRenderer ingredientRenderer = new DebugIngredientRenderer(ingredientHelper);
			registration.register(DebugIngredient.TYPE, Collections.emptyList(), ingredientHelper, ingredientRenderer, DebugIngredient.CODEC);

			if (DebugConfig.isCrashingTestIngredientsEnabled()) {
				ErrorIngredientHelper errorIngredientHelper = new ErrorIngredientHelper();
				ErrorIngredientRenderer errorIngredientRenderer = new ErrorIngredientRenderer(errorIngredientHelper);
				Collection<ErrorIngredient> errorIngredients = ErrorIngredientListFactory.create();
				registration.register(ErrorIngredient.TYPE, errorIngredients, errorIngredientHelper, errorIngredientRenderer, ErrorIngredient.CODEC);
			}
		}
	}

	@Override
	public void registerExtraIngredients(IExtraIngredientRegistration registration) {
		if (DebugConfig.isDebugModeEnabled()) {
			registration.addExtraIngredients(DebugIngredient.TYPE, DebugIngredientListFactory.create(0, 10));
		}
	}

	@Override
	public void registerIngredientAliases(IIngredientAliasRegistration registration) {
		registration.addAlias(
			new ItemStack(Items.PANDA_SPAWN_EGG),
			"jei.alias.panda.spawn.egg"
		);

		registration.addAlias(
			new ItemStack(Items.VILLAGER_SPAWN_EGG),
			"jei.alias.villager.spawn.egg"
		);

		registration.addAliases(
			VanillaTypes.ITEM_STACK,
			List.of(
				new ItemStack(Items.STRUCTURE_VOID),
				new ItemStack(Items.BARRIER)
			),
			"nothing"
		);

		registration.addAliases(
			VanillaTypes.ITEM_STACK,
			List.of(
				new ItemStack(Items.GOLDEN_HOE),
				new ItemStack(Items.DIAMOND_BLOCK)
			),
			List.of("shiny", "valuable", "Expensive", "expansive", "extensive")
		);

		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		registerFluidAliases(registration, fluidHelper);
	}

	private <T> void registerFluidAliases(IIngredientAliasRegistration registration, IPlatformFluidHelper<T> fluidHelper) {
		@SuppressWarnings("deprecation")
		Holder.Reference<Fluid> water = Fluids.WATER.builtInRegistryHolder();
		registration.addAliases(
			fluidHelper.getFluidIngredientType(),
			fluidHelper.create(water, fluidHelper.bucketVolume()),
			List.of("wet", "aqua", "sea", "ocean")
		);
	}

	@Override
	public void registerModInfo(IModInfoRegistration registration) {
		registration.addModAliases(ModIds.JEI_ID, "jei");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		if (DebugConfig.isDebugModeEnabled()) {
			IJeiHelpers jeiHelpers = registration.getJeiHelpers();
			IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
			IPlatformFluidHelper<?> platformFluidHelper = jeiHelpers.getPlatformFluidHelper();
			IIngredientManager ingredientManager = jeiHelpers.getIngredientManager();
			Textures textures = Internal.getTextures();
			this.debugRecipeCategory = new DebugRecipeCategory<>(guiHelper, platformFluidHelper, ingredientManager);
			registration.addRecipeCategories(
				debugRecipeCategory,
				new DebugFocusRecipeCategory<>(platformFluidHelper),
				new ObnoxiouslyLargeCategory(guiHelper, textures, ingredientManager),
				new ErrorRecipeCategory()
			);
		}
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		if (DebugConfig.isDebugModeEnabled()) {
			registration.addItemStackInfo(List.of(
				new ItemStack(Blocks.OAK_DOOR),
				new ItemStack(Blocks.SPRUCE_DOOR),
				new ItemStack(Blocks.BIRCH_DOOR),
				new ItemStack(Blocks.JUNGLE_DOOR),
				new ItemStack(Blocks.ACACIA_DOOR),
				new ItemStack(Blocks.DARK_OAK_DOOR)
				),
				Component.translatable("description.jei.wooden.door.1"), // actually 2 lines
				Component.translatable("description.jei.wooden.door.2"),
				Component.translatable("description.jei.wooden.door.3")
			);

			IJeiHelpers jeiHelpers = registration.getJeiHelpers();
			IPlatformFluidHelper<?> platformFluidHelper = jeiHelpers.getPlatformFluidHelper();
			registerFluidRecipes(registration, platformFluidHelper);
			registration.addIngredientInfo(new DebugIngredient(1), DebugIngredient.TYPE, Component.literal("debug"));
			registration.addIngredientInfo(new DebugIngredient(2), DebugIngredient.TYPE,
				Component.literal("debug colored").withStyle(ChatFormatting.AQUA),
				Component.literal("debug\\nSplit and colored").withStyle(ChatFormatting.LIGHT_PURPLE),
				Component.translatable("description.jei.debug.formatting.1", "various"),
				Component.translatable("description.jei.debug.formatting.1", "various\\nsplit"),
				Component.translatable("description.jei.debug.formatting.1", Component.literal("various colored").withStyle(ChatFormatting.RED)),
				Component.translatable("description.jei.debug.formatting.1",
					Component.literal("various\\nsplit colored").withStyle(ChatFormatting.DARK_AQUA)
				),
				Component.translatable("description.jei.debug.formatting.1", "\\nSplitting at the start"),
				Component.translatable("description.jei.debug.formatting.1", "various all colored").withStyle(ChatFormatting.RED),
				Component.translatable("description.jei.debug.formatting.1",
					Component.translatable("description.jei.debug.formatting.3", "various").withStyle(ChatFormatting.DARK_AQUA)
				),
				Component.translatable("description.jei.debug.formatting.2",
					Component.literal("multiple").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC),
					Component.literal("various").withStyle(ChatFormatting.RED)
				).withStyle(ChatFormatting.BLUE),
				Component.translatable("description.jei.debug.formatting.1",
					Component.translatable("description.jei.debug.formatting.3",
						Component.translatable("description.jei.debug.formatting.2",
							Component.literal("multiple").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC),
							Component.literal("various").withStyle(ChatFormatting.RED)
						).withStyle(ChatFormatting.DARK_AQUA)
					)
				)
			);

			registration.addRecipes(DebugRecipeCategory.TYPE, List.of(
				new DebugRecipe(),
				new DebugRecipe()
			));

			registration.addRecipes(DebugFocusRecipeCategory.TYPE, List.of(
				new DebugRecipe()
			));

			Identifier testRecipeWithoutTemplateId = Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "test_recipe_without_template");
			RecipeHolder<SmithingRecipe> testRecipeWithoutTemplate = new RecipeHolder<>(
				ResourceKey.create(Registries.RECIPE, testRecipeWithoutTemplateId),
				new SmithingTransformRecipe(
					new Recipe.CommonInfo(false),
					Optional.empty(),
					Ingredient.of(Items.APPLE),
					Optional.of(Ingredient.of(Items.BAKED_POTATO)),
					new ItemStackTemplate(Items.ACACIA_BOAT)
				)
			);
			registration.addRecipes(RecipeTypes.SMITHING, List.of(
				testRecipeWithoutTemplate
			));

			registration.addRecipes(ObnoxiouslyLargeCategory.TYPE, List.of(new ObnoxiouslyLargeRecipe()));

			if (DebugConfig.isCrashingTestRecipesEnabled()) {
				registration.addRecipes(ErrorRecipeCategory.TYPE, Arrays.stream(ErrorRecipe.CrashType.values()).map(ErrorRecipe::new).toList());
			}
		}
	}

	private <T> void registerFluidRecipes(IRecipeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
		long bucketVolume = platformFluidHelper.bucketVolume();
		T fluidIngredient = platformFluidHelper.create(Fluids.WATER.defaultFluidState().typeHolder(), bucketVolume);
		registration.addIngredientInfo(fluidIngredient, platformFluidHelper.getFluidIngredientType(), Component.literal("water"));

		fluidIngredient = platformFluidHelper.create(Fluids.LAVA.defaultFluidState().typeHolder(), 1);
		registration.addIngredientInfo(fluidIngredient, platformFluidHelper.getFluidIngredientType(), Component.literal("small amount of lava that should still show as 1 bucket"));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		if (DebugConfig.isDebugModeEnabled()) {
			IJeiHelpers jeiHelpers = registration.getJeiHelpers();
			IIngredientManager ingredientManager = jeiHelpers.getIngredientManager();

			registration.addGuiContainerHandler(BrewingStandScreen.class, new DebugBrewingStandScreenHandler());

			registration.addGhostIngredientHandler(BrewingStandScreen.class, new DebugGhostIngredientHandler<>(ingredientManager));
			registration.addGhostIngredientHandler(BrewingStandScreen.class, new DebugGhostIngredientHandlerTwo<>(ingredientManager));
		}
	}

	@Override
	public <T> void registerFluidSubtypes(ISubtypeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
		Fluid water = Fluids.WATER;
		IIngredientTypeWithSubtypes<Fluid, T> ingredientType = platformFluidHelper.getFluidIngredientType();
		FluidSubtypeHandlerTest<T> subtype = new FluidSubtypeHandlerTest<>(ingredientType);
		registration.registerSubtypeInterpreter(ingredientType, water, subtype);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		if (DebugConfig.isDebugModeEnabled()) {
			IPlatformFluidHelper<?> fluidHelper = Services.PLATFORM.getFluidHelper();
			registerRecipeCatalysts(registration, fluidHelper);
		}
	}

	private <T> void registerRecipeCatalysts(IRecipeCatalystRegistration registration, IPlatformFluidHelper<T> fluidHelper) {
		long bucketVolume = fluidHelper.bucketVolume();

		registration.addCraftingStation(DebugRecipeCategory.TYPE, DebugIngredient.TYPE, new DebugIngredient(7));
		registration.addCraftingStation(DebugRecipeCategory.TYPE, fluidHelper.getFluidIngredientType(), fluidHelper.create(Fluids.WATER.defaultFluidState().typeHolder(), bucketVolume));
		registration.addCraftingStation(DebugRecipeCategory.TYPE, Items.STICK);

		RegistryUtil.getRegistry(Registries.ITEM)
			.stream()
			.limit(300)
			.forEach(item -> {
				ItemStack catalystIngredient = new ItemStack(item);
				if (!catalystIngredient.isEmpty()) {
					registration.addCraftingStation(DebugRecipeCategory.TYPE, catalystIngredient);
				}
			});
	}

	@Override
	public void registerAdvanced(IAdvancedRegistration registration) {
		if (DebugConfig.isDebugModeEnabled()) {
			IJeiHelpers jeiHelpers = registration.getJeiHelpers();

			jeiHelpers
				.getAllRecipeTypes()
				.filter(r -> r.getUid().getNamespace().equals(ModIds.JEI_ID))
				.forEach(r -> registration.addRecipeCategoryDecorator(r, DebugCategoryDecorator.getInstance()));

			registration.addSimpleRecipeManagerPlugin(RecipeTypes.CRAFTING, new DebugSimpleRecipeManagerPlugin(jeiHelpers));

			IRecipeButtonControllerFactory debugButton = new IRecipeButtonControllerFactory() {
				@Override
				public <T> IIconButtonController createButtonController(IRecipeLayoutDrawable<T> recipeLayoutDrawable) {
					return new IIconButtonController() {
						@Override
						public void initState(IButtonState state) {
							state.setIcon(Internal.getTextures().getShapelessIcon());
						}

						@Override
						public boolean onPress(IJeiUserInput input) {
							return false;
						}

						@Override
						public void getTooltips(ITooltipBuilder tooltip) {
							tooltip.add(Component.literal("Debug Button"));
						}
					};
				}
			};
			for (int i = 0; i < 9; i++) {
				registration.addRecipeButtonFactory(debugButton);
			}
		}
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		if (DebugConfig.isDebugModeEnabled()) {
			ErrorUtil.assertMainThread();
			Registry<Enchantment> registry = RegistryUtil.getRegistry(Registries.ENCHANTMENT);
			Optional<Holder.Reference<Enchantment>> enchantment = registry.get(Enchantments.FIRE_ASPECT);
			assert enchantment.isPresent();
			if (debugRecipeCategory != null) {
				debugRecipeCategory.setRuntime(jeiRuntime);
			}
			IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();
			ingredientManager.addIngredientsAtRuntime(DebugIngredient.TYPE, DebugIngredientListFactory.create(10, 20));
		}
	}
}
