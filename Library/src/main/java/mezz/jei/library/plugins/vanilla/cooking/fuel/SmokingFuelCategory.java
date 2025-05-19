package mezz.jei.library.plugins.vanilla.cooking.fuel;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.common.gui.textures.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class SmokingFuelCategory extends AbstractFuelCategory {
	public SmokingFuelCategory(IGuiHelper guiHelper, Textures textures) {
		super(
			textures,
			RecipeTypes.SMOKING_FUEL,
			Component.translatable("gui.jei.category.smoking_fuel"),
			guiHelper.createDrawableItemLike(Items.SMOKER),
			2
		);
	}
}
