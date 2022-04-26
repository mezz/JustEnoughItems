package mezz.jei.test.lib;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TestModIdHelper implements IModIdHelper {
    @Override
	public String getModNameForModId(String modId) {
		return "ModName(" + modId + ")";
	}

	@Override
	public String getFormattedModNameForModId(String modId) {
		return getModNameForModId(modId);
	}

	@Override
	public <T> List<Component> addModNameToIngredientTooltip(List<Component> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper) {
		return tooltip;
	}

	@Override
	public boolean isDisplayingModNameEnabled() {
		return false;
	}
}
