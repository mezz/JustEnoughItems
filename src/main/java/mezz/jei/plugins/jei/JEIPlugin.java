package mezz.jei.plugins.jei;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.config.Config;
import mezz.jei.plugins.jei.debug.DebugRecipe;
import mezz.jei.plugins.jei.debug.DebugRecipeCategory;
import mezz.jei.plugins.jei.debug.DebugRecipeHandler;
import mezz.jei.plugins.jei.description.ItemDescriptionRecipeCategory;
import mezz.jei.plugins.jei.description.ItemDescriptionRecipeHandler;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin {
	private IJeiHelpers jeiHelpers;

	@Override
	public void onJeiHelpersAvailable(IJeiHelpers jeiHelpers) {
		this.jeiHelpers = jeiHelpers;
	}

	@Override
	public void onItemRegistryAvailable(IItemRegistry itemRegistry) {

	}

	@Override
	public void register(IModRegistry registry) {
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

		registry.addRecipeCategories(
				new ItemDescriptionRecipeCategory(guiHelper)
		);

		registry.addRecipeHandlers(
				new ItemDescriptionRecipeHandler()
		);

		if (Config.isDebugModeEnabled()) {
			registry.addDescription(Arrays.asList(
					new ItemStack(Items.oak_door),
					new ItemStack(Items.spruce_door),
					new ItemStack(Items.birch_door),
					new ItemStack(Items.jungle_door),
					new ItemStack(Items.acacia_door),
					new ItemStack(Items.dark_oak_door)
					),
					"description.jei.wooden.door.1", // actually 2 lines
					"description.jei.wooden.door.2",
					"description.jei.wooden.door.3"
			);

			registry.addRecipeCategories(new DebugRecipeCategory(guiHelper));
			registry.addRecipeHandlers(new DebugRecipeHandler());
			registry.addRecipes(Arrays.asList(
					new DebugRecipe(),
					new DebugRecipe()
			));

			registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler<GuiBrewingStand>() {
				@Nonnull
				@Override
				public Class<GuiBrewingStand> getGuiContainerClass() {
					return GuiBrewingStand.class;
				}

				@Nullable
				@Override
				public List<Rectangle> getGuiExtraAreas(GuiBrewingStand guiContainer) {
					int widthMovement = (int) ((System.currentTimeMillis() / 100) % 100);
					int size = 25 + widthMovement;
					return Collections.singletonList(
							new Rectangle(guiContainer.guiLeft + guiContainer.xSize, guiContainer.guiTop + 40, size, size)
					);
				}
			});
		}
	}

	@Override
	public void onRecipeRegistryAvailable(IRecipeRegistry recipeRegistry) {

	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

	}
}
