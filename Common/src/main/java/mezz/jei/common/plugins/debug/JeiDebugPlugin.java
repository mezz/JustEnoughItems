package mezz.jei.common.plugins.debug;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.config.ClientConfig;
import mezz.jei.common.plugins.jei.ingredients.DebugIngredient;
import mezz.jei.common.plugins.jei.ingredients.DebugIngredientHelper;
import mezz.jei.common.plugins.jei.ingredients.DebugIngredientListFactory;
import mezz.jei.common.plugins.jei.ingredients.DebugIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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

@JeiPlugin
public class JeiDebugPlugin implements IModPlugin {
	@Nullable
	public static IIngredientManager ingredientManager;
	@Nullable
	public static IJeiRuntime jeiRuntime;

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ModIds.JEI_ID, "debug");
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		if (ClientConfig.getInstance().isDebugModeEnabled()) {
			DebugIngredientHelper ingredientHelper = new DebugIngredientHelper();
			DebugIngredientRenderer ingredientRenderer = new DebugIngredientRenderer(ingredientHelper);
			registration.register(DebugIngredient.TYPE, Collections.emptyList(), ingredientHelper, ingredientRenderer);
		}
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		if (ClientConfig.getInstance().isDebugModeEnabled()) {
			IJeiHelpers jeiHelpers = registration.getJeiHelpers();
			IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
			IPlatformFluidHelper<?> platformFluidHelper = jeiHelpers.getPlatformFluidHelper();
			registration.addRecipeCategories(
				new DebugRecipeCategory<>(guiHelper, platformFluidHelper),
				new DebugFocusRecipeCategory<>(guiHelper, platformFluidHelper),
				new LegacyDebugRecipeCategory<>(guiHelper, platformFluidHelper)
			);
		}
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		JeiDebugPlugin.ingredientManager = registration.getIngredientManager();

		if (ClientConfig.getInstance().isDebugModeEnabled()) {
			registration.addIngredientInfo(List.of(
				new ItemStack(Blocks.OAK_DOOR),
				new ItemStack(Blocks.SPRUCE_DOOR),
				new ItemStack(Blocks.BIRCH_DOOR),
				new ItemStack(Blocks.JUNGLE_DOOR),
				new ItemStack(Blocks.ACACIA_DOOR),
				new ItemStack(Blocks.DARK_OAK_DOOR)
				),
				VanillaTypes.ITEM_STACK,
				new TranslatableComponent("description.jei.wooden.door.1"), // actually 2 lines
				new TranslatableComponent("description.jei.wooden.door.2"),
				new TranslatableComponent("description.jei.wooden.door.3")
			);

			IJeiHelpers jeiHelpers = registration.getJeiHelpers();
			IPlatformFluidHelper<?> platformFluidHelper = jeiHelpers.getPlatformFluidHelper();
			registerFluidRecipes(registration, platformFluidHelper);
			registration.addIngredientInfo(new DebugIngredient(1), DebugIngredient.TYPE, new TextComponent("debug"));
			registration.addIngredientInfo(new DebugIngredient(2), DebugIngredient.TYPE,
				new TextComponent("debug colored").withStyle(ChatFormatting.AQUA),
				new TextComponent("debug\\nSplit and colored").withStyle(ChatFormatting.LIGHT_PURPLE),
				new TranslatableComponent("description.jei.debug.formatting.1", "various"),
				new TranslatableComponent("description.jei.debug.formatting.1", "various\\nsplit"),
				new TranslatableComponent("description.jei.debug.formatting.1", new TextComponent("various colored").withStyle(ChatFormatting.RED)),
				new TranslatableComponent("description.jei.debug.formatting.1",
					new TextComponent("various\\nsplit colored").withStyle(ChatFormatting.DARK_AQUA)
				),
				new TranslatableComponent("description.jei.debug.formatting.1", "\\nSplitting at the start"),
				new TranslatableComponent("description.jei.debug.formatting.1", "various all colored").withStyle(ChatFormatting.RED),
				new TranslatableComponent("description.jei.debug.formatting.1",
					new TranslatableComponent("description.jei.debug.formatting.3", "various").withStyle(ChatFormatting.DARK_AQUA)
				),
				new TranslatableComponent("description.jei.debug.formatting.2",
					new TextComponent("multiple").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC),
					new TextComponent("various").withStyle(ChatFormatting.RED)
				).withStyle(ChatFormatting.BLUE),
				new TranslatableComponent("description.jei.debug.formatting.1",
					new TranslatableComponent("description.jei.debug.formatting.3",
						new TranslatableComponent("description.jei.debug.formatting.2",
							new TextComponent("multiple").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC),
							new TextComponent("various").withStyle(ChatFormatting.RED)
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

			//noinspection removal
			registration.addRecipes(List.of(
				new DebugRecipe(),
				new DebugRecipe()
			), LegacyDebugRecipeCategory.UID);
		}
	}

	private <T> void registerFluidRecipes(IRecipeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
		long bucketVolume = platformFluidHelper.bucketVolume();
		T fluidIngredient = platformFluidHelper.create(Fluids.WATER, bucketVolume, null);
		registration.addIngredientInfo(fluidIngredient, platformFluidHelper.getFluidIngredientType(), new TextComponent("water"));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		if (ClientConfig.getInstance().isDebugModeEnabled()) {
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

				@Nullable
				@Override
				public Object getIngredientUnderMouse(BrewingStandScreen containerScreen, double mouseX, double mouseY) {
					if (mouseX < 10 && mouseY < 10) {
						IPlatformFluidHelper<?> fluidHelper = Services.PLATFORM.getFluidHelper();
						long bucketVolume = fluidHelper.bucketVolume();
						return fluidHelper.create(Fluids.WATER, bucketVolume, null);
					}
					return null;
				}
			});

			registration.addGhostIngredientHandler(BrewingStandScreen.class, new DebugGhostIngredientHandler<>());
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
		if (ClientConfig.getInstance().isDebugModeEnabled()) {
			IPlatformFluidHelper<?> fluidHelper = Services.PLATFORM.getFluidHelper();
			registerRecipeCatalysts(registration, fluidHelper);
		}
	}

	private <T> void registerRecipeCatalysts(IRecipeCatalystRegistration registration, IPlatformFluidHelper<T> fluidHelper) {
		long bucketVolume = fluidHelper.bucketVolume();

		registration.addRecipeCatalyst(DebugIngredient.TYPE, new DebugIngredient(7), DebugRecipeCategory.TYPE);
		registration.addRecipeCatalyst(fluidHelper.getFluidIngredientType(), fluidHelper.create(Fluids.WATER, bucketVolume, null), DebugRecipeCategory.TYPE);
		registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, new ItemStack(Items.STICK), DebugRecipeCategory.TYPE);
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
		JeiDebugPlugin.jeiRuntime = jeiRuntime;

		if (ClientConfig.getInstance().isDebugModeEnabled()) {
			IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();
			ingredientManager.addIngredientsAtRuntime(DebugIngredient.TYPE, DebugIngredientListFactory.create());
		}
	}
}
