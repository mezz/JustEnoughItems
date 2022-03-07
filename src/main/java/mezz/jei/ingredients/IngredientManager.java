package mezz.jei.ingredients;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IClientConfig;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class IngredientManager implements IIngredientManager {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IModIdHelper modIdHelper;
	private final IngredientBlacklistInternal blacklist;
	private final IClientConfig clientConfig;
	private final RegisteredIngredients registeredIngredients;
	private final IngredientFilter ingredientFilter;

	public IngredientManager(
		IModIdHelper modIdHelper,
		IngredientBlacklistInternal blacklist,
		IClientConfig clientConfig,
		RegisteredIngredients registeredIngredients,
		IngredientFilter ingredientFilter
	) {
		this.modIdHelper = modIdHelper;
		this.blacklist = blacklist;
		this.clientConfig = clientConfig;
		this.registeredIngredients = registeredIngredients;
		this.ingredientFilter = ingredientFilter;
	}

	@Override
	@Unmodifiable
	public <V> Collection<V> getAllIngredients(IIngredientType<V> ingredientType) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");

		IngredientInfo<V> ingredientInfo = this.registeredIngredients.getIngredientInfo(ingredientType);
		return ingredientInfo.getAllIngredients();
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		IIngredientType<V> ingredientType = getIngredientType(ingredient);
		return getIngredientHelper(ingredientType);
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(IIngredientType<V> ingredientType) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");

		IngredientInfo<V> ingredientInfo = this.registeredIngredients.getIngredientInfo(ingredientType);
		return ingredientInfo.getIngredientHelper();
	}

	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		IIngredientType<V> ingredientType = getIngredientType(ingredient);
		return getIngredientRenderer(ingredientType);
	}

	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> ingredientType) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");

		IngredientInfo<V> ingredientInfo = this.registeredIngredients.getIngredientInfo(ingredientType);
		return ingredientInfo.getIngredientRenderer();
	}

	@Override
	public Collection<IIngredientType<?>> getRegisteredIngredientTypes() {
		return this.registeredIngredients.getIngredientTypes();
	}

	@Override
	public <V> void addIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		ErrorUtil.assertMainThread();
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");

		IngredientInfo<V> ingredientInfo = this.registeredIngredients.getIngredientInfo(ingredientType);

		LOGGER.info("Ingredients are being added at runtime: {} {}", ingredients.size(), ingredientType.getIngredientClass().getName());

		ingredientInfo.addIngredients(ingredients);

		List<ITypedIngredient<V>> typedIngredients = ingredients.stream()
			.map(i -> TypedIngredient.createTyped(this.registeredIngredients, ingredientType, i))
			.map(Optional::orElseThrow)
			.toList();

		IIngredientHelper<V> ingredientHelper = ingredientInfo.getIngredientHelper();

		for (ITypedIngredient<V> value : typedIngredients) {
			Optional<IListElementInfo<V>> matchingElementInfo = ingredientFilter.searchForMatchingElement(ingredientHelper, value);
			if (matchingElementInfo.isPresent()) {
				IListElement<V> matchingElement = matchingElementInfo.get().getElement();
				ITypedIngredient<V> typedIngredient = matchingElement.getTypedIngredient();
				blacklist.removeIngredientFromBlacklist(typedIngredient, ingredientHelper);
				ingredientFilter.updateHiddenState(matchingElement);
				if (clientConfig.isDebugModeEnabled()) {
					LOGGER.debug("Updated ingredient: {}", ingredientHelper.getErrorInfo(value.getIngredient()));
				}
			} else {
				IListElement<V> element = IngredientListElementFactory.createOrderedElement(value);
				IListElementInfo<V> info = ListElementInfo.create(element, this.registeredIngredients, modIdHelper);
				if (info != null) {
					blacklist.removeIngredientFromBlacklist(value, ingredientHelper);
					ingredientFilter.addIngredient(info);
					if (clientConfig.isDebugModeEnabled()) {
						LOGGER.debug("Added ingredient: {}", ingredientHelper.getErrorInfo(value.getIngredient()));
					}
				}
			}
		}
		ingredientFilter.invalidateCache();
	}

	@Override
	public <V> IIngredientType<V> getIngredientType(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		return this.registeredIngredients.getIngredientType(ingredient);
	}

	@Override
	public <V> IIngredientType<V> getIngredientType(Class<? extends V> ingredientClass) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");

		return this.registeredIngredients.getIngredientType(ingredientClass);
	}

	@Override
	public <V> void removeIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		ErrorUtil.assertMainThread();
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");

		IngredientInfo<V> ingredientInfo = this.registeredIngredients.getIngredientInfo(ingredientType);

		LOGGER.info("Ingredients are being removed at runtime: {} {}", ingredients.size(), ingredientType.getIngredientClass().getName());

		ingredientInfo.removeIngredients(ingredients);

		IIngredientHelper<V> ingredientHelper = getIngredientHelper(ingredientType);

		ingredients.stream()
			.map(i -> TypedIngredient.createTyped(this.registeredIngredients, ingredientType, i))
			.flatMap(Optional::stream)
			.forEach(typedIngredient -> {
				Optional<IListElementInfo<V>> matchingElementInfo = ingredientFilter.searchForMatchingElement(ingredientHelper, typedIngredient);
				if (matchingElementInfo.isEmpty()) {
					String errorInfo = ingredientHelper.getErrorInfo(typedIngredient.getIngredient());
					LOGGER.error("Could not find a matching ingredient to remove: {}", errorInfo);
				} else {
					if (clientConfig.isDebugModeEnabled()) {
						LOGGER.debug("Removed ingredient: {}", ingredientHelper.getErrorInfo(typedIngredient.getIngredient()));
					}
					IListElement<V> matchingElement = matchingElementInfo.get().getElement();
					blacklist.addIngredientToBlacklist(matchingElement.getTypedIngredient(), ingredientHelper);
					matchingElement.setVisible(false);
				}
			});

		ingredientFilter.invalidateCache();
	}

}
