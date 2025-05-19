package mezz.jei.library.plugins.vanilla.cooking.fuel;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.common.gui.textures.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class SmeltingFuelCategory extends AbstractFuelCategory {
	public SmeltingFuelCategory(IGuiHelper guiHelper, Textures textures) {
		super(
			textures,
			RecipeTypes.SMELTING_FUEL,
			Component.translatable("gui.jei.category.smelting_fuel"),
			guiHelper.createDrawableItemLike(Items.FURNACE),
			1
		);
	}
}
