package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class FurnaceSmeltingCategory extends AbstractCookingCategory<SmeltingRecipe> {
	public FurnaceSmeltingCategory(IGuiHelper guiHelper) {
		this(guiHelper, List.of());
	}

	public FurnaceSmeltingCategory(IGuiHelper guiHelper, List<ItemStack> furnaceFuels) {
		super(guiHelper, RecipeTypes.SMELTING, Blocks.FURNACE, "gui.jei.category.smelting", 200, furnaceFuels, 116, 54);
	}
}
