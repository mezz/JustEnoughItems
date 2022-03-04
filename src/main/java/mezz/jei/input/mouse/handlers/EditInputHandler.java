package mezz.jei.input.mouse.handlers;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.IngredientBlacklistType;
import mezz.jei.config.KeyBindings;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.input.CombinedRecipeFocusSource;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
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
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input) {
		if (!worldConfig.isEditModeEnabled()) {
			return Optional.empty();
		}

		if (input.is(KeyBindings.toggleHideIngredient)) {
			return handle(input, IngredientBlacklistType.ITEM);
		}

		if (input.is(KeyBindings.toggleWildcardHideIngredient)) {
			return handle(input, IngredientBlacklistType.WILDCARD);
		}

		return Optional.empty();
	}

	private Optional<IUserInputHandler> handle(UserInput input, IngredientBlacklistType blacklistType) {
		return focusSource.getIngredientUnderMouse(input)
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
