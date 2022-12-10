package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class IngredientManager implements IIngredientManager {
	private static final Logger LOGGER = LogManager.getLogger();

	private final RegisteredIngredients registeredIngredients;
	private final Map<ResourceLocation, IIngredientListener> listeners = new HashMap<>();

	public IngredientManager(RegisteredIngredients registeredIngredients) {
		this.registeredIngredients = registeredIngredients;
	}

	@Override
	@Unmodifiable
	public <V> Collection<V> getAllIngredients(IIngredientType<V> ingredientType) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");

		return this.registeredIngredients
			.getIngredientInfo(ingredientType)
			.getAllIngredients();
	}

	@SuppressWarnings("removal")
	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(V ingredient) {
		return getIngredientTypeChecked(ingredient)
			.map(this::getIngredientHelper)
			.orElseThrow(() -> new IllegalArgumentException("Unknown ingredient class: " + ingredient.getClass()));
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(IIngredientType<V> ingredientType) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");

		return this.registeredIngredients
			.getIngredientInfo(ingredientType)
			.getIngredientHelper();
	}

	@SuppressWarnings("removal")
	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(V ingredient) {
		return getIngredientTypeChecked(ingredient)
			.map(this::getIngredientRenderer)
			.orElseThrow(() -> new IllegalArgumentException("Unknown ingredient class: " + ingredient.getClass()));
	}

	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> ingredientType) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");

		return this.registeredIngredients
			.getIngredientInfo(ingredientType)
			.getIngredientRenderer();
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

		if (!this.listeners.isEmpty()) {
			List<ITypedIngredient<V>> typedIngredients = ingredients.stream()
				.map(i -> TypedIngredient.createTyped(this, ingredientType, i))
				.map(Optional::orElseThrow)
				.toList();

			IIngredientHelper<V> ingredientHelper = ingredientInfo.getIngredientHelper();

			for (IIngredientListener listener : this.listeners.values()) {
				listener.onIngredientsAdded(ingredientHelper, typedIngredients);
			}
		}
	}

	@SuppressWarnings("removal")
	@Override
	public <V> IIngredientType<V> getIngredientType(V ingredient) {
		return getIngredientTypeChecked(ingredient)
			.orElseThrow(() -> new IllegalArgumentException("Unknown ingredient class: " + ingredient.getClass()));
	}

	@Override
	public <V> Optional<IIngredientType<V>> getIngredientTypeChecked(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		return this.registeredIngredients.getIngredientType(ingredient);
	}

	@SuppressWarnings("removal")
	@Override
	public <V> IIngredientType<V> getIngredientType(Class<? extends V> ingredientClass) {
		Optional<IIngredientType<V>> ingredientType = getIngredientTypeChecked(ingredientClass);
		return ingredientType
			.orElseThrow(() -> new IllegalArgumentException("Unknown ingredient class: " + ingredientClass));
	}

	@Override
	public <V> Optional<IIngredientType<V>> getIngredientTypeChecked(Class<? extends V> ingredientClass) {
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

		if (!this.listeners.isEmpty()) {
			List<ITypedIngredient<V>> typedIngredients = ingredients.stream()
				.map(i -> TypedIngredient.createTyped(this, ingredientType, i))
				.map(Optional::orElseThrow)
				.toList();

			IIngredientHelper<V> ingredientHelper = ingredientInfo.getIngredientHelper();

			for (IIngredientListener listener : this.listeners.values()) {
				listener.onIngredientsRemoved(ingredientHelper, typedIngredients);
			}
		}
	}

	@Override
	public void addIngredientListener(IIngredientListener listener) {
		ErrorUtil.checkNotNull(listener, "listener");
		this.listeners.put(listener.getUid(), listener);
	}

	@Override
	public void removeIngredientListener(IIngredientListener listener) {
		ErrorUtil.checkNotNull(listener, "listener");
		this.listeners.remove(listener.getUid(), listener);
	}

	@Override
	public <V> Optional<ITypedIngredient<V>> createTypedIngredient(IIngredientType<V> ingredientType, V ingredient) {
		return TypedIngredient.createTyped(this, ingredientType, ingredient);
	}

	@Override
	public <V> Optional<V> getIngredientByUid(IIngredientType<V> ingredientType, String ingredientUuid) {
		return registeredIngredients
			.getIngredientInfo(ingredientType)
			.getIngredientByUid(ingredientUuid);
	}
}
