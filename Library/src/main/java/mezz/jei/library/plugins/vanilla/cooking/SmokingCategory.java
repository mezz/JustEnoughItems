package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class SmokingCategory extends AbstractCookingCategory<SmokingRecipe> {
	public SmokingCategory(IGuiHelper guiHelper) {
		this(guiHelper, List.of());
	}

	public SmokingCategory(IGuiHelper guiHelper, List<ItemStack> furnaceFuels) {
		super(guiHelper, RecipeTypes.SMOKING, Blocks.SMOKER, "gui.jei.category.smoking", 100, furnaceFuels);
	}
}
