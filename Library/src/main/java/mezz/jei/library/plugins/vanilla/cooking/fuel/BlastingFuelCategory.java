package mezz.jei.library.plugins.vanilla.cooking.fuel;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.common.gui.textures.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class BlastingFuelCategory extends AbstractFuelCategory {
	public BlastingFuelCategory(IGuiHelper guiHelper, Textures textures) {
		super(
			textures,
			RecipeTypes.BLASTING_FUEL,
			Component.translatable("gui.jei.category.blasting_fuel"),
			guiHelper.createDrawableItemLike(Items.BLAST_FURNACE),
			2
		);
	}
}
