package mezz.jei.plugins.debug;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.config.ClientConfig;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.plugins.jei.ingredients.DebugIngredient;
import mezz.jei.plugins.jei.ingredients.DebugIngredientHelper;
import mezz.jei.plugins.jei.ingredients.DebugIngredientListFactory;
import mezz.jei.plugins.jei.ingredients.DebugIngredientRenderer;

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
			registration.addRecipeCategories(
				new DebugRecipeCategory(guiHelper),
				new DebugFocusRecipeCategory(guiHelper),
				new LegacyDebugRecipeCategory(guiHelper)
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
				VanillaTypes.ITEM,
				new TranslatableComponent("description.jei.wooden.door.1"), // actually 2 lines
				new TranslatableComponent("description.jei.wooden.door.2"),
				new TranslatableComponent("description.jei.wooden.door.3")
			);

			registration.addIngredientInfo(new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME), VanillaTypes.FLUID, new TextComponent("water"));
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

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		if (ClientConfig.getInstance().isDebugModeEnabled()) {
			registration.addGuiContainerHandler(BrewingStandScreen.class, new IGuiContainerHandler<>() {
				@Override
				public List<Rect2i> getGuiExtraAreas(BrewingStandScreen containerScreen) {
					int widthMovement = (int) ((System.currentTimeMillis() / 100) % 100);
					int size = 25 + widthMovement;
					return List.of(
						new Rect2i(containerScreen.getGuiLeft() + containerScreen.getXSize(), containerScreen.getGuiTop() + 40, size, size)
					);
				}

				@Nullable
				@Override
				public Object getIngredientUnderMouse(BrewingStandScreen containerScreen, double mouseX, double mouseY) {
					if (mouseX < 10 && mouseY < 10) {
						return new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
					}
					return null;
				}
			});

			registration.addGhostIngredientHandler(BrewingStandScreen.class, new DebugGhostIngredientHandler<>());
		}
	}

	@Override
	public void registerFluidSubtypes(ISubtypeRegistration registration) {
		Fluid water = Fluids.WATER;
		FluidSubtypeHandlerTest subtype = new FluidSubtypeHandlerTest();

		registration.registerSubtypeInterpreter(water, subtype);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		if (ClientConfig.getInstance().isDebugModeEnabled()) {
			registration.addRecipeCatalyst(DebugIngredient.TYPE, new DebugIngredient(7), DebugRecipeCategory.TYPE);
			registration.addRecipeCatalyst(VanillaTypes.FLUID, new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME), DebugRecipeCategory.TYPE);
			registration.addRecipeCatalyst(VanillaTypes.ITEM, new ItemStack(Items.STICK), DebugRecipeCategory.TYPE);
			int i = 0;
			for (Item item : ForgeRegistries.ITEMS.getValues()) {
				ItemStack catalystIngredient = new ItemStack(item);
				if (!catalystIngredient.isEmpty()) {
					registration.addRecipeCatalyst(catalystIngredient, DebugRecipeCategory.TYPE);
				}
				i++;
				if (i > 30) {
					break;
				}
			}
		}
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
