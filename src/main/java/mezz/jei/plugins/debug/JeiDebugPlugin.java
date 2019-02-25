package mezz.jei.plugins.debug;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import mezz.jei.Internal;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ModIds;
import mezz.jei.api.gui.IGuiContainerHandler;
import mezz.jei.api.ingredients.IIngredientManager;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.recipe.IVanillaRecipeFactory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.config.ClientConfig;
import mezz.jei.gui.GuiHelper;
import mezz.jei.plugins.jei.ingredients.DebugIngredient;
import mezz.jei.plugins.jei.ingredients.DebugIngredientHelper;
import mezz.jei.plugins.jei.ingredients.DebugIngredientListFactory;
import mezz.jei.plugins.jei.ingredients.DebugIngredientRenderer;
import mezz.jei.runtime.JeiHelpers;

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
	public void registerIngredients(IModIngredientRegistration registration, ISubtypeManager subtypeManager) {
		if (ClientConfig.getInstance().isDebugModeEnabled()) {
			DebugIngredientHelper ingredientHelper = new DebugIngredientHelper();
			DebugIngredientRenderer ingredientRenderer = new DebugIngredientRenderer(ingredientHelper);
			registration.register(DebugIngredient.TYPE, Collections.emptyList(), ingredientHelper, ingredientRenderer);
		}
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration, IJeiHelpers jeiHelpers) {
		if (ClientConfig.getInstance().isDebugModeEnabled()) {
			JeiHelpers internalJeiHelpers = Internal.getHelpers();
			GuiHelper guiHelper = internalJeiHelpers.getGuiHelper();
			registration.addRecipeCategories(
				new DebugRecipeCategory(guiHelper)
			);
		}
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration, IJeiHelpers jeiHelpers, IIngredientManager ingredientManager, IVanillaRecipeFactory vanillaRecipeFactory) {
		JeiDebugPlugin.ingredientManager = ingredientManager;

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
				"description.jei.wooden.door.1", // actually 2 lines
				"description.jei.wooden.door.2",
				"description.jei.wooden.door.3"
			);

//			registry.addIngredientInfo(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), VanillaTypes.FLUID, "water");

			registration.addRecipes(Arrays.asList(
				new DebugRecipe(),
				new DebugRecipe()
			), DebugRecipeCategory.UID);
		}
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		if (ClientConfig.getInstance().isDebugModeEnabled()) {
			registration.addGuiContainerHandler(GuiBrewingStand.class, new IGuiContainerHandler<GuiBrewingStand>() {
				@Override
				public List<Rectangle> getGuiExtraAreas(GuiBrewingStand guiContainer) {
					int widthMovement = (int) ((System.currentTimeMillis() / 100) % 100);
					int size = 25 + widthMovement;
					return Collections.singletonList(
						new Rectangle(guiContainer.getGuiLeft() + guiContainer.getXSize(), guiContainer.getGuiTop() + 40, size, size)
					);
				}

				@Nullable
				@Override
				public Object getIngredientUnderMouse(GuiBrewingStand guiContainer, double mouseX, double mouseY) {
//					if (mouseX < 10 && mouseY < 10) {
//						return new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
//					}
					return null;
				}
			});

			registration.addGhostIngredientHandler(GuiBrewingStand.class, new DebugGhostIngredientHandler<>());
		}
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		if (ClientConfig.getInstance().isDebugModeEnabled()) {
			registration.addRecipeCatalyst(new DebugIngredient(7), DebugRecipeCategory.UID);
//			registry.addRecipeCatalyst(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), DebugRecipeCategory.UID);
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
