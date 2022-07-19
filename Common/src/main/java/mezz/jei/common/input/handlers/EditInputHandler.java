package mezz.jei.common.input.handlers;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.config.IEditModeConfig;
import mezz.jei.common.ingredients.IngredientFilter;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.input.IClickedIngredient;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.UserInput;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.core.config.IngredientBlacklistType;
import mezz.jei.common.input.CombinedRecipeFocusSource;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public class EditInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final RegisteredIngredients registeredIngredients;
	private final IngredientFilter ingredientFilter;
	private final IWorldConfig worldConfig;
	private final IEditModeConfig editModeConfig;

	public EditInputHandler(CombinedRecipeFocusSource focusSource, RegisteredIngredients registeredIngredients, IngredientFilter ingredientFilter, IWorldConfig worldConfig, IEditModeConfig editModeConfig) {
		this.focusSource = focusSource;
		this.registeredIngredients = registeredIngredients;
		this.ingredientFilter = ingredientFilter;
		this.worldConfig = worldConfig;
		this.editModeConfig = editModeConfig;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		if (!worldConfig.isEditModeEnabled()) {
			return Optional.empty();
		}

		if (input.is(keyBindings.getToggleHideIngredient())) {
			return handle(input, keyBindings, IngredientBlacklistType.ITEM);
		}

		if (input.is(keyBindings.getToggleWildcardHideIngredient())) {
			return handle(input, keyBindings, IngredientBlacklistType.WILDCARD);
		}

		return Optional.empty();
	}

	private Optional<IUserInputHandler> handle(UserInput input, IInternalKeyMappings keyBindings, IngredientBlacklistType blacklistType) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.findFirst()
			.map(clicked -> {
				if (!input.isSimulate()) {
					execute(clicked, blacklistType);
				}
				return LimitedAreaInputHandler.create(this, clicked.getArea());
			});
	}

	private <V> void execute(IClickedIngredient<V> clicked, IngredientBlacklistType blacklistType) {
		ITypedIngredient<V> typedIngredient = clicked.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = registeredIngredients.getIngredientHelper(typedIngredient.getType());

		if (editModeConfig.isIngredientOnConfigBlacklist(typedIngredient, ingredientHelper)) {
			editModeConfig.removeIngredientFromConfigBlacklist(ingredientFilter, typedIngredient, blacklistType, ingredientHelper);
		} else {
			editModeConfig.addIngredientToConfigBlacklist(ingredientFilter, typedIngredient, blacklistType, ingredientHelper);
		}
	}
}
