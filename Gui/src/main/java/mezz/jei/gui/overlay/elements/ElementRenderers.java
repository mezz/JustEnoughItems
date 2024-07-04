package mezz.jei.gui.overlay.elements;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IClientToggleState;

import java.util.HashMap;
import java.util.Map;

public class ElementRenderers {
	private final Map<IIngredientType<?>, ElementRenderer<?>> map = new HashMap<>();
	private final IClientToggleState toggleState;
	private final IEditModeConfig editModeConfig;
	private final IIngredientManager ingredientManager;

	public ElementRenderers(IClientToggleState toggleState, IEditModeConfig editModeConfig, IIngredientManager ingredientManager) {
		this.toggleState = toggleState;
		this.editModeConfig = editModeConfig;
		this.ingredientManager = ingredientManager;
	}

	public <T> ElementRenderer<T> get(IIngredientType<T> ingredientType) {
		@SuppressWarnings("unchecked")
		ElementRenderer<T> result = (ElementRenderer<T>) this.map.get(ingredientType);
		if (result == null) {
			result = new ElementRenderer<>(ingredientType, toggleState, editModeConfig, ingredientManager);
			this.map.put(ingredientType, result);
		}
		return result;
	}
}
