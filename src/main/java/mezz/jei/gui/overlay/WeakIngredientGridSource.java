package mezz.jei.gui.overlay;

import mezz.jei.api.ingredients.ITypedIngredient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public class WeakIngredientGridSource implements IIngredientGridSource {
	private static final Logger LOGGER = LogManager.getLogger();

	private final WeakReference<IIngredientGridSource> weakIngredientSource;

	public WeakIngredientGridSource(IIngredientGridSource source) {
		this.weakIngredientSource = new WeakReference<>(source);
	}

	@Override
	public List<ITypedIngredient<?>> getIngredientList(String filterText) {
		IIngredientGridSource ingredientGridSource = weakIngredientSource.get();
		if (ingredientGridSource == null) {
			LOGGER.error("no ingredientGridSource");
			return Collections.emptyList();
		}
		return ingredientGridSource.getIngredientList(filterText);
	}

	@Override
	public void addListener(Listener listener) {
		IIngredientGridSource ingredientGridSource = weakIngredientSource.get();
		if (ingredientGridSource == null) {
			LOGGER.error("no ingredientGridSource");
			return;
		}
		ingredientGridSource.addListener(listener);
	}
}
