package mezz.jei.library.plugins.debug;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.MathUtil;
import mezz.jei.library.plugins.jei.ingredients.DebugIngredient;
import mezz.jei.library.plugins.jei.ingredients.DebugIngredientHelper;
import mezz.jei.library.plugins.jei.ingredients.DebugIngredientListFactory;
import mezz.jei.library.plugins.jei.ingredients.DebugIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

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
		if (DebugConfig.isDebugModeEnabled()) {
			DebugIngredientHelper ingredientHelper = new DebugIngredientHelper();
			DebugIngredientRenderer ingredientRenderer = new DebugIngredientRenderer(ingredientHelper);
			registration.register(DebugIngredient.TYPE, Collections.emptyList(), ingredientHelper, ingredientRenderer);
		}
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		if (DebugConfig.isDebugModeEnabled()) {
			IJeiHelpers jeiHelpers = registration.getJeiHelpers();
			IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
			IPlatformFluidHelper<?> platformFluidHelper = jeiHelpers.getPlatformFluidHelper();
			IIngredientManager ingredientManager = jeiHelpers.getIngredientManager();
			this.debugRecipeCategory = new DebugRecipeCategory<>(guiHelper, platformFluidHelper, ingredientManager);
			registration.addRecipeCategories(
				debugRecipeCategory,
				new DebugFocusRecipeCategory<>(guiHelper, platformFluidHelper)
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
		}
	}

	private <T> void registerFluidRecipes(IRecipeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
		long bucketVolume = platformFluidHelper.bucketVolume();
		T fluidIngredient = platformFluidHelper.create(Fluids.WATER, bucketVolume, null);
		registration.addIngredientInfo(fluidIngredient, platformFluidHelper.getFluidIngredientType(), Component.literal("water"));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		if (DebugConfig.isDebugModeEnabled()) {
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
				public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(BrewingStandScreen containerScreen, double mouseX, double mouseY) {
					Rect2i area = new Rect2i(0, 0, 10, 10);
					if (MathUtil.contains(area, mouseX, mouseY)) {
						return ingredientManager.createTypedIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.BOW))
							.map(item -> new DebugClickableIngredient<>(item, area));
					}
					return Optional.empty();
				}
			});

			registration.addGhostIngredientHandler(BrewingStandScreen.class, new DebugGhostIngredientHandler<>(ingredientManager));
		}
	}

	private static class DebugClickableIngredient<T> implements IClickableIngredient<T> {
		private final ITypedIngredient<T> typedIngredient;
		private final Rect2i area;

		public DebugClickableIngredient(ITypedIngredient<T> typedIngredient, Rect2i area) {
			this.typedIngredient = typedIngredient;
			this.area = area;
		}

		@Override
		public ITypedIngredient<T> getTypedIngredient() {
			return typedIngredient;
		}

		@Override
		public Rect2i getArea() {
			return area;
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

		registration.addRecipeCatalyst(DebugIngredient.TYPE, new DebugIngredient(7), DebugRecipeCategory.TYPE);
		registration.addRecipeCatalyst(fluidHelper.getFluidIngredientType(), fluidHelper.create(Fluids.WATER, bucketVolume, null), DebugRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(Items.STICK), DebugRecipeCategory.TYPE);
		IPlatformRegistry<Item> registry = Services.PLATFORM.getRegistry(Registry.ITEM_REGISTRY);
		registry.getValues()
			.limit(30)
			.forEach(item -> {
				ItemStack catalystIngredient = new ItemStack(item);
				if (!catalystIngredient.isEmpty()) {
					registration.addRecipeCatalyst(catalystIngredient, DebugRecipeCategory.TYPE);
				}
			});
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		if (DebugConfig.isDebugModeEnabled()) {
			if (debugRecipeCategory != null) {
				debugRecipeCategory.setRuntime(jeiRuntime);
			}
			IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();
			ingredientManager.addIngredientsAtRuntime(DebugIngredient.TYPE, DebugIngredientListFactory.create());
		}
	}
}
