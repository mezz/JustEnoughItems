package mezz.jei.test.lib;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.Set;

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
	public <T> Optional<Component> getModNameForTooltip(ITypedIngredient<T> typedIngredient) {
		return Optional.empty();
	}

	@Override
	public boolean isDisplayingModNameEnabled() {
		return false;
	}

	@Override
	public Set<String> getModAliases(String modId) {
		return Set.of();
	}
}
