package mezz.jei.input.mouse.handlers;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.IngredientBlacklistType;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.input.CombinedRecipeFocusSource;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;

public class ClickEditHandler implements IUserInputHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private final CombinedRecipeFocusSource focusSource;
	private final IIngredientManager ingredientManager;
	private final WeakReference<IngredientFilter> weakIngredientFilter;
	private final IWorldConfig worldConfig;
	private final IEditModeConfig editModeConfig;

	public ClickEditHandler(CombinedRecipeFocusSource focusSource, IIngredientManager ingredientManager, IngredientFilter ingredientFilter, IWorldConfig worldConfig, IEditModeConfig editModeConfig) {
		this.focusSource = focusSource;
		this.ingredientManager = ingredientManager;
		this.weakIngredientFilter = new WeakReference<>(ingredientFilter);
		this.worldConfig = worldConfig;
		this.editModeConfig = editModeConfig;
	}

	@Override
	public IUserInputHandler handleUserInput(Screen screen, UserInput input) {
		if (!worldConfig.isEditModeEnabled()) {
			return null;
		}
		IClickedIngredient<?> clicked = focusSource.getIngredientUnderMouse(input);
		if (clicked == null) {
			return null;
		}
		if (!input.isSimulate()) {
			handle(clicked);
		}
		return LimitedAreaUserInputHandler.create(this, clicked.getArea());
	}

	private <V> void handle(IClickedIngredient<V> clicked) {
		IngredientFilter ingredientFilter = weakIngredientFilter.get();
		if (ingredientFilter == null) {
			LOGGER.error("Can't edit the config blacklist, the ingredient filter is null");
			return;
		}

		V ingredient = clicked.getValue();
		IngredientBlacklistType blacklistType = Screen.hasControlDown() ? IngredientBlacklistType.WILDCARD : IngredientBlacklistType.ITEM;
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);

		if (editModeConfig.isIngredientOnConfigBlacklist(ingredient, ingredientHelper)) {
			editModeConfig.removeIngredientFromConfigBlacklist(ingredientFilter, ingredientManager, ingredient, blacklistType, ingredientHelper);
		} else {
			editModeConfig.addIngredientToConfigBlacklist(ingredientFilter, ingredientManager, ingredient, blacklistType, ingredientHelper);
		}
	}
}
