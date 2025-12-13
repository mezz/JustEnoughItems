package mezz.jei.debug;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
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
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.MathUtil;
import mezz.jei.debug.ingredients.DebugIngredient;
import mezz.jei.debug.ingredients.DebugIngredientHelper;
import mezz.jei.debug.ingredients.DebugIngredientListFactory;
import mezz.jei.debug.ingredients.DebugIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@JeiPlugin
public class JeiDebugPlugin implements IModPlugin {
	private @Nullable DebugRecipeCategory<?> debugRecipeCategory;

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ModIds.JEI_ID, "debug");
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		DebugIngredientHelper ingredientHelper = new DebugIngredientHelper();
		DebugIngredientRenderer ingredientRenderer = new DebugIngredientRenderer(ingredientHelper);
		registration.register(DebugIngredient.TYPE, Collections.emptyList(), ingredientHelper, ingredientRenderer);
	}

	@Override
	public void registerExtraIngredients(IExtraIngredientRegistration registration) {
		registration.addExtraIngredients(DebugIngredient.TYPE, DebugIngredientListFactory.create(0, 10));
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
		registration.addAliases(
			fluidHelper.getFluidIngredientType(),
			fluidHelper.create(Fluids.WATER, fluidHelper.bucketVolume()),
			List.of("wet", "aqua", "sea", "ocean")
		);
	}

	@Override
	public void registerModInfo(IModInfoRegistration registration) {
		registration.addModAliases(ModIds.JEI_ID, "jei");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		IPlatformFluidHelper<?> platformFluidHelper = jeiHelpers.getPlatformFluidHelper();
		IIngredientManager ingredientManager = jeiHelpers.getIngredientManager();
		this.debugRecipeCategory = new DebugRecipeCategory<>(guiHelper, platformFluidHelper, ingredientManager);
		registration.addRecipeCategories(
			debugRecipeCategory,
			new DebugFocusRecipeCategory<>(platformFluidHelper),
			new ErrorRecipeCategory(guiHelper),
			new ObnoxiouslyLargeCategory(guiHelper, Internal.getTextures(), ingredientManager)
		);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
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

		UpgradeRecipe testRecipeWithoutBase = new UpgradeRecipe(
			new ResourceLocation(ModIds.JEI_ID, "test_recipe_without_base"),
			Ingredient.EMPTY,
			Ingredient.of(new ItemStack(Items.APPLE)),
			new ItemStack(Items.BAKED_POTATO)
		);
		registration.addRecipes(RecipeTypes.SMITHING, List.of(
			testRecipeWithoutBase
		));

		if (DebugConfig.isCrashingTestRecipesEnabled()) {
			registration.addRecipes(ErrorRecipeCategory.TYPE, Arrays.stream(ErrorRecipe.CrashType.values()).map(ErrorRecipe::new).toList());
		}

		registration.addRecipes(ObnoxiouslyLargeCategory.TYPE, List.of(new ObnoxiouslyLargeRecipe()));
	}

	private <T> void registerFluidRecipes(IRecipeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
		long bucketVolume = platformFluidHelper.bucketVolume();
		T fluidIngredient = platformFluidHelper.create(Fluids.WATER, bucketVolume, null);
		registration.addIngredientInfo(fluidIngredient, platformFluidHelper.getFluidIngredientType(), Component.literal("water"));

		fluidIngredient = platformFluidHelper.create(Fluids.LAVA.defaultFluidState().getType(), 1);
		registration.addIngredientInfo(fluidIngredient, platformFluidHelper.getFluidIngredientType(), Component.literal("small amount of lava that should still show as 1 bucket"));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		IIngredientManager ingredientManager = jeiHelpers.getIngredientManager();

		registration.addGuiContainerHandler(BrewingStandScreen.class, new IGuiContainerHandler<>() {
			@Override
			public List<Rect2i> getGuiExtraAreas(BrewingStandScreen containerScreen) {
				int widthMovement = (int) ((System.currentTimeMillis() / 100) % 100);
				int size = 25 + widthMovement;
				IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
				int guiLeft = screenHelper.getGuiLeft(containerScreen);
				int xSize = screenHelper.getXSize(containerScreen);
				int guiTop = screenHelper.getGuiTop(containerScreen);
				return List.of(
					new Rect2i(guiLeft + xSize, guiTop + 40, size, size)
				);
			}

			@Override
			public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(IClickableIngredientFactory factory, BrewingStandScreen containerScreen, double mouseX, double mouseY) {
				Rect2i area = new Rect2i(0, 0, 10, 10);
				if (MathUtil.contains(area, mouseX, mouseY)) {
					return factory.createBuilder(new ItemStack(Items.BOW))
						.buildWithArea(area);
				}
				return Optional.empty();
			}
		});

		registration.addGhostIngredientHandler(BrewingStandScreen.class, new DebugGhostIngredientHandler<>(ingredientManager));
		registration.addGhostIngredientHandler(BrewingStandScreen.class, new DebugGhostIngredientHandlerTwo<>(ingredientManager));
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
		IPlatformFluidHelper<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		registerRecipeCatalysts(registration, fluidHelper);
	}

	private <T> void registerRecipeCatalysts(IRecipeCatalystRegistration registration, IPlatformFluidHelper<T> fluidHelper) {
		long bucketVolume = fluidHelper.bucketVolume();

		registration.addRecipeCatalyst(DebugIngredient.TYPE, new DebugIngredient(7), DebugRecipeCategory.TYPE);
		registration.addRecipeCatalyst(fluidHelper.getFluidIngredientType(), fluidHelper.create(Fluids.WATER, bucketVolume, null), DebugRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(Items.STICK), DebugRecipeCategory.TYPE);
		IPlatformRegistry<Item> registry = Services.PLATFORM.getRegistry(Registry.ITEM_REGISTRY);
		registry.getValues()
			.limit(300)
			.forEach(item -> {
				ItemStack catalystIngredient = new ItemStack(item);
				if (!catalystIngredient.isEmpty()) {
					registration.addRecipeCatalyst(catalystIngredient, DebugRecipeCategory.TYPE);
				}
			});
	}

	@Override
	public void registerAdvanced(IAdvancedRegistration registration) {
		registration.getJeiHelpers()
			.getAllRecipeTypes()
			.filter(r -> r.getUid().getNamespace().equals(ModIds.JEI_ID))
			.forEach(r -> registration.addRecipeCategoryDecorator(r, DebugCategoryDecorator.getInstance()));
		registration.addTypedRecipeManagerPlugin(RecipeTypes.CRAFTING, new DebugSimpleRecipeManagerPlugin());

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

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		ErrorUtil.assertMainThread();
		if (debugRecipeCategory != null) {
			debugRecipeCategory.setRuntime(jeiRuntime);
		}
		IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();
		ingredientManager.addIngredientsAtRuntime(DebugIngredient.TYPE, DebugIngredientListFactory.create(10, 20));
	}
}
