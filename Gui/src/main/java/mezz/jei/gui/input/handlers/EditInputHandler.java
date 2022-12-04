package mezz.jei.gui.input.handlers;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickedIngredient;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.util.IImmutableRect2i;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.handlers.LimitedAreaInputHandler;
import mezz.jei.core.config.IWorldConfig;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public class EditInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final IWorldConfig worldConfig;
	private final IEditModeConfig editModeConfig;

	public EditInputHandler(CombinedRecipeFocusSource focusSource, IWorldConfig worldConfig, IEditModeConfig editModeConfig) {
		this.focusSource = focusSource;
		this.worldConfig = worldConfig;
		this.editModeConfig = editModeConfig;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		if (!worldConfig.isEditModeEnabled()) {
			return Optional.empty();
		}

		if (input.is(keyBindings.getToggleHideIngredient())) {
			return handle(input, keyBindings, IEditModeConfig.Mode.ITEM);
		}

		if (input.is(keyBindings.getToggleWildcardHideIngredient())) {
			return handle(input, keyBindings, IEditModeConfig.Mode.WILDCARD);
		}

		return Optional.empty();
	}

	private Optional<IUserInputHandler> handle(UserInput input, IInternalKeyMappings keyBindings, IEditModeConfig.Mode mode) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.findFirst()
			.map(clicked -> {
				if (!input.isSimulate()) {
					execute(clicked, mode);
				}
				IImmutableRect2i area = clicked.getArea().orElse(null);
				return LimitedAreaInputHandler.create(this, area);
			});
	}

	private <V> void execute(IClickedIngredient<V> clicked, IEditModeConfig.Mode mode) {
		ITypedIngredient<V> typedIngredient = clicked.getTypedIngredient();
		if (editModeConfig.isIngredientHiddenUsingConfigFile(typedIngredient)) {
			editModeConfig.showIngredientUsingConfigFile(typedIngredient, mode);
		} else {
			editModeConfig.hideIngredientUsingConfigFile(typedIngredient, mode);
		}
	}
}
