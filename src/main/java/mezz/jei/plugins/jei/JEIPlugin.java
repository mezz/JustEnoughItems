package mezz.jei.plugins.jei;

import java.util.Arrays;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.config.Config;
import mezz.jei.plugins.jei.description.ItemDescriptionRecipeCategory;
import mezz.jei.plugins.jei.description.ItemDescriptionRecipeHandler;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin {

	@Override
	public boolean isModLoaded() {
		return true;
	}

	@Override
	public void register(IModRegistry registry) {
		registry.addRecipeCategories(
				new ItemDescriptionRecipeCategory()
		);

		registry.addRecipeHandlers(
				new ItemDescriptionRecipeHandler()
		);

		if (Config.debugModeEnabled) {
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
		}
	}
}
