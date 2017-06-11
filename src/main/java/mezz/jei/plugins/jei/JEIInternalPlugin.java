package mezz.jei.plugins.jei;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.config.Config;
import mezz.jei.plugins.jei.debug.DebugRecipe;
import mezz.jei.plugins.jei.debug.DebugRecipeCategory;
import mezz.jei.plugins.jei.info.IngredientInfoRecipeCategory;
import mezz.jei.plugins.jei.ingredients.DebugIngredient;
import mezz.jei.plugins.jei.ingredients.DebugIngredientHelper;
import mezz.jei.plugins.jei.ingredients.DebugIngredientListFactory;
import mezz.jei.plugins.jei.ingredients.DebugIngredientRenderer;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

@JEIPlugin
public class JEIInternalPlugin implements IModPlugin {
	@Nullable
	public static IIngredientRegistry ingredientRegistry;
	@Nullable
	public static IJeiRuntime jeiRuntime;

	@Override
	public void registerIngredients(IModIngredientRegistration ingredientRegistration) {
		if (Config.isDebugModeEnabled()) {
			DebugIngredientHelper ingredientHelper = new DebugIngredientHelper();
			DebugIngredientRenderer ingredientRenderer = new DebugIngredientRenderer(ingredientHelper);
			ingredientRegistration.register(DebugIngredient.class, Collections.emptyList(), ingredientHelper, ingredientRenderer);
		}
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		IJeiHelpers jeiHelpers = registry.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

		registry.addRecipeCategories(
				new IngredientInfoRecipeCategory(guiHelper)
		);

		if (Config.isDebugModeEnabled()) {
			registry.addRecipeCategories(
					new DebugRecipeCategory(guiHelper)
			);
		}
	}

	@Override
	public void register(IModRegistry registry) {
		ingredientRegistry = registry.getIngredientRegistry();

		if (Config.isDebugModeEnabled()) {
			registry.addIngredientInfo(Arrays.asList(
					new ItemStack(Items.OAK_DOOR),
					new ItemStack(Items.SPRUCE_DOOR),
					new ItemStack(Items.BIRCH_DOOR),
					new ItemStack(Items.JUNGLE_DOOR),
					new ItemStack(Items.ACACIA_DOOR),
					new ItemStack(Items.DARK_OAK_DOOR)
					),
					ItemStack.class,
					"description.jei.wooden.door.1", // actually 2 lines
					"description.jei.wooden.door.2",
					"description.jei.wooden.door.3"
			);

			registry.addRecipes(Arrays.asList(
					new DebugRecipe(),
					new DebugRecipe()
			), "debug");

			registry.addRecipeCatalyst(new DebugIngredient(7), "debug");
			registry.addRecipeCatalyst(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), "debug");
			registry.addRecipeCatalyst(new ItemStack(Items.STICK), "debug");

			registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler<GuiBrewingStand>() {
				@Override
				public Class<GuiBrewingStand> getGuiContainerClass() {
					return GuiBrewingStand.class;
				}

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
				public Object getIngredientUnderMouse(GuiBrewingStand guiContainer, int mouseX, int mouseY) {
					if (mouseX < 10 && mouseY < 10) {
						return new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
					}
					return null;
				}
			});
		}
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		JEIInternalPlugin.jeiRuntime = jeiRuntime;

		if (Config.isDebugModeEnabled()) {
			if (ingredientRegistry != null) {
				ingredientRegistry.addIngredientsAtRuntime(DebugIngredient.class, DebugIngredientListFactory.create());
			}
		}
	}
}
