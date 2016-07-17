package mezz.jei.plugins.jei;

import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.config.Config;
import mezz.jei.plugins.jei.debug.DebugRecipe;
import mezz.jei.plugins.jei.debug.DebugRecipeCategory;
import mezz.jei.plugins.jei.debug.DebugRecipeHandler;
import mezz.jei.plugins.jei.description.ItemDescriptionRecipeCategory;
import mezz.jei.plugins.jei.description.ItemDescriptionRecipeHandler;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@JEIPlugin
public class JEIInternalPlugin extends BlankModPlugin {
	@Override
	public void register(@Nonnull IModRegistry registry) {
		IJeiHelpers jeiHelpers = registry.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

		registry.addRecipeCategories(
				new ItemDescriptionRecipeCategory(guiHelper)
		);

		registry.addRecipeHandlers(
				new ItemDescriptionRecipeHandler()
		);

		if (Config.isDebugModeEnabled()) {
			registry.addDescription(Arrays.asList(
					new ItemStack(Items.OAK_DOOR),
					new ItemStack(Items.SPRUCE_DOOR),
					new ItemStack(Items.BIRCH_DOOR),
					new ItemStack(Items.JUNGLE_DOOR),
					new ItemStack(Items.ACACIA_DOOR),
					new ItemStack(Items.DARK_OAK_DOOR)
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
	public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
		super.onRuntimeAvailable(jeiRuntime);

		if (Config.isDebugModeEnabled()) {
			jeiRuntime.getItemListOverlay().highlightStacks(Collections.singleton(new ItemStack(Items.STICK)));
		}
	}
}
