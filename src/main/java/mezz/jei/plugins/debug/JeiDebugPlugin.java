package mezz.jei.plugins.debug;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.config.ClientConfig;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.inventory.BrewingStandScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

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
				new DebugRecipeCategory(guiHelper)
			);
		}
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		JeiDebugPlugin.ingredientManager = registration.getIngredientManager();

		if (ClientConfig.getInstance().isDebugModeEnabled()) {
			registration.addIngredientInfo(Arrays.asList(
				new ItemStack(Blocks.OAK_DOOR),
				new ItemStack(Blocks.SPRUCE_DOOR),
				new ItemStack(Blocks.BIRCH_DOOR),
				new ItemStack(Blocks.JUNGLE_DOOR),
				new ItemStack(Blocks.ACACIA_DOOR),
				new ItemStack(Blocks.DARK_OAK_DOOR)
				),
				VanillaTypes.ITEM,
				new TranslationTextComponent("description.jei.wooden.door.1"), // actually 2 lines
				new TranslationTextComponent("description.jei.wooden.door.2"),
				new TranslationTextComponent("description.jei.wooden.door.3")
			);

			registration.addIngredientInfo(new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME), VanillaTypes.FLUID, new StringTextComponent("water"));
			registration.addIngredientInfo(new DebugIngredient(1), DebugIngredient.TYPE, new StringTextComponent("debug"));
			registration.addIngredientInfo(new DebugIngredient(2), DebugIngredient.TYPE,
				new StringTextComponent("debug colored").withStyle(TextFormatting.AQUA),
				new StringTextComponent("debug\\nSplit and colored").withStyle(TextFormatting.LIGHT_PURPLE),
				new TranslationTextComponent("description.jei.debug.formatting.1", "various"),
				new TranslationTextComponent("description.jei.debug.formatting.1", "various\\nsplit"),
				new TranslationTextComponent("description.jei.debug.formatting.1", new StringTextComponent("various colored").withStyle(TextFormatting.RED)),
				new TranslationTextComponent("description.jei.debug.formatting.1",
					new StringTextComponent("various\\nsplit colored").withStyle(TextFormatting.DARK_AQUA)
				),
				new TranslationTextComponent("description.jei.debug.formatting.1", "\\nSplitting at the start"),
				new TranslationTextComponent("description.jei.debug.formatting.1", "various all colored").withStyle(TextFormatting.RED),
				new TranslationTextComponent("description.jei.debug.formatting.1",
					new TranslationTextComponent("description.jei.debug.formatting.3", "various").withStyle(TextFormatting.DARK_AQUA)
				),
				new TranslationTextComponent("description.jei.debug.formatting.2",
					new StringTextComponent("multiple").withStyle(TextFormatting.GOLD).withStyle(TextFormatting.ITALIC),
					new StringTextComponent("various").withStyle(TextFormatting.RED)
				).withStyle(TextFormatting.BLUE),
				new TranslationTextComponent("description.jei.debug.formatting.1",
					new TranslationTextComponent("description.jei.debug.formatting.3",
						new TranslationTextComponent("description.jei.debug.formatting.2",
							new StringTextComponent("multiple").withStyle(TextFormatting.GOLD).withStyle(TextFormatting.ITALIC),
							new StringTextComponent("various").withStyle(TextFormatting.RED)
						).withStyle(TextFormatting.DARK_AQUA)
					)
				)
			);

			registration.addRecipes(Arrays.asList(
				new DebugRecipe(),
				new DebugRecipe()
			), DebugRecipeCategory.UID);
		}
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		if (ClientConfig.getInstance().isDebugModeEnabled()) {
			registration.addGuiContainerHandler(BrewingStandScreen.class, new IGuiContainerHandler<BrewingStandScreen>() {
				@Override
				public List<Rectangle2d> getGuiExtraAreas(BrewingStandScreen containerScreen) {
					int widthMovement = (int) ((System.currentTimeMillis() / 100) % 100);
					int size = 25 + widthMovement;
					return Collections.singletonList(
						new Rectangle2d(containerScreen.getGuiLeft() + containerScreen.getXSize(), containerScreen.getGuiTop() + 40, size, size)
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
			registration.addRecipeCatalyst(new DebugIngredient(7), DebugRecipeCategory.UID);
			registration.addRecipeCatalyst(new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME), DebugRecipeCategory.UID);
			registration.addRecipeCatalyst(new ItemStack(Items.STICK), DebugRecipeCategory.UID);
			int i = 0;
			for (Item item : ForgeRegistries.ITEMS.getValues()) {
				ItemStack catalystIngredient = new ItemStack(item);
				if (!catalystIngredient.isEmpty()) {
					registration.addRecipeCatalyst(catalystIngredient, DebugRecipeCategory.UID);
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
