package mezz.jei.test.lib;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TestModIdHelper implements IModIdHelper {
	@Override
	public String getModNameForModId(String modId) {
		return "ModName(" + modId + ")";
	}

	@Override
	@Deprecated(since = "15.47.0", forRemoval = true)
	@SuppressWarnings("removal")
	public String getFormattedModNameForModId(String modId) {
		return getModNameForModId(modId);
	}

	@Override
	public Set<String> getModAliases(String modId) {
		return Set.of();
	}

	@SuppressWarnings("removal")
	@Override
	public <T> List<Component> addModNameToIngredientTooltip(List<Component> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper) {
		return tooltip;
	}

	@SuppressWarnings("removal")
	@Override
	public <T> List<Component> addModNameToIngredientTooltip(List<Component> tooltip, ITypedIngredient<T> typedIngredient) {
		return tooltip;
	}

	@Override
	public Component getFormattedModNameComponentForModId(String modId) {
		return Component.literal(getModNameForModId(modId));
	}

	@Override
	public <T> Optional<Component> getModNameForTooltip(ITypedIngredient<T> typedIngredient) {
		return Optional.empty();
	}

	@Override
	public boolean isDisplayingModNameEnabled() {
		return false;
	}
}
