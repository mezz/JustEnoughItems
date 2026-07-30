package mezz.jei.library.ingredients;

import com.mojang.serialization.Codec;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.common.input.ClickableIngredient;
import mezz.jei.common.input.ClickableIngredientFactory;
import mezz.jei.common.ingredients.TypedIngredientUtil;
import mezz.jei.common.ingredients.TypedIngredient;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.Translator;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IngredientManager implements IIngredientManagerInternal {
	private static final Logger LOGGER = LogManager.getLogger();

	private final RegisteredIngredients registeredIngredients;
	private final SlotDisplayInterpreterRegistry slotDisplayInterpreterRegistry;
	private final List<IIngredientListener> listeners = new ArrayList<>();

	public IngredientManager(
		RegisteredIngredients registeredIngredients,
		SlotDisplayInterpreterRegistry slotDisplayInterpreterRegistry
	) {
		this.registeredIngredients = registeredIngredients;
		this.slotDisplayInterpreterRegistry = slotDisplayInterpreterRegistry;
	}

	@Override
	@Unmodifiable
	public <V> Collection<V> getAllIngredients(IIngredientType<V> ingredientType) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");

		return this.registeredIngredients
			.getIngredientInfo(ingredientType)
			.getAllIngredients();
	}

	@Override
	@Unmodifiable
	public <V> Collection<ITypedIngredient<V>> getAllTypedIngredients(IIngredientType<V> ingredientType) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");

		return this.registeredIngredients
			.getIngredientInfo(ingredientType)
			.getAllTypedIngredients();
	}

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

	@Override
	public Stream<SlotIngredient<?>> resolveSlotDisplay(
		ContextMap contextMap,
		RecipeIngredientRole role,
		SlotDisplay slotDisplay
	) {
		return SlotDisplayIngredientResolver.resolve(this, slotDisplayInterpreterRegistry, contextMap, role, slotDisplay);
	}

	@Override
	public <T> Stream<SlotIngredient<T>> resolveSlotDisplay(
		IIngredientType<T> ingredientType,
		ContextMap contextMap,
		RecipeIngredientRole role,
		SlotDisplay slotDisplay
	) {
		return SlotDisplayIngredientResolver.resolve(this, slotDisplayInterpreterRegistry, ingredientType, contextMap, role, slotDisplay);
	}

	@Override
	public <T> List<ITypedIngredient<T>> getGroupedIngredients(ITypedIngredient<T> ingredient) {
		return registeredIngredients
			.getIngredientInfo(ingredient.getType())
			.getGroupedIngredients(ingredient);
	}

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
	public Optional<IIngredientType<?>> getIngredientTypeForUid(String ingredientTypeUid) {
		ErrorUtil.checkNotNull(ingredientTypeUid, "ingredientTypeUid");

		return this.registeredIngredients.getIngredientTypes()
			.stream()
			.filter(t -> ingredientTypeUid.equals(t.getUid()))
			.findFirst();
	}

	@Override
	public <V> void addIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		ErrorUtil.assertMainThread();
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");

		IngredientInfo<V> ingredientInfo = this.registeredIngredients.getIngredientInfo(ingredientType);
		IIngredientHelper<V> ingredientHelper = ingredientInfo.getIngredientHelper();

		LOGGER.info("Ingredients are being added at runtime: {} {}", ingredients.size(), ingredientType.getIngredientClass().getName());
		if (LOGGER.isDebugEnabled()) {
			String ingredientStrings = ingredients.stream()
				.map(ingredientHelper::getIdentifier)
				.map(Identifier::toString)
				.collect(Collectors.joining(", ", "[", "]"));
			LOGGER.debug("Ingredients added at runtime: {}", ingredientStrings);
		}

		List<ITypedIngredient<V>> validTypedIngredients = new ArrayList<>(ingredients.size());
		for (V ingredient : ingredients) {
			if (!ingredientHelper.isIngredientOnServer(ingredient)) {
				String errorInfo = ingredientHelper.getErrorInfo(ingredient);
				LOGGER.warn("Attempted to add an Ingredient that is not on the server: {}", errorInfo);
				continue;
			}
			ITypedIngredient<V> typedIngredient = TypedIngredient.createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, false);
			if (typedIngredient == null) {
				LOGGER.warn("Attempted to add an invalid ingredient at runtime: {}", ingredientHelper.getErrorInfo(ingredient));
				continue;
			}

			validTypedIngredients.add(typedIngredient);
		}

		ingredientInfo.addIngredients(validTypedIngredients);

		this.listeners.forEach(listener -> listener.onIngredientsAdded(ingredientHelper, validTypedIngredients));
	}

	@Override
	public @Nullable <V> IIngredientType<V> getIngredientType(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		return this.registeredIngredients.getIngredientType(ingredient);
	}

	@Override
	public <V> Optional<IIngredientType<V>> getIngredientTypeChecked(V ingredient) {
		IIngredientType<V> ingredientType = getIngredientType(ingredient);
		return Optional.ofNullable(ingredientType);
	}

	@Override
	public <B, I> Optional<IIngredientTypeWithSubtypes<B, I>> getIngredientTypeWithSubtypesFromBase(B baseIngredient) {
		ErrorUtil.checkNotNull(baseIngredient, "baseIngredient");
		return this.registeredIngredients.getIngredientTypeWithSubtypesFromBase(baseIngredient);
	}

	@Override
	public <V> Optional<IIngredientType<V>> getIngredientTypeChecked(Class<? extends V> ingredientClass) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");

		IIngredientType<V> ingredientType = this.registeredIngredients.getIngredientType(ingredientClass);
		return Optional.ofNullable(ingredientType);
	}

	@Override
	public <V> void removeIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		ErrorUtil.assertMainThread();
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");

		IngredientInfo<V> ingredientInfo = this.registeredIngredients.getIngredientInfo(ingredientType);
		IIngredientHelper<V> ingredientHelper = ingredientInfo.getIngredientHelper();

		LOGGER.info("Ingredients are being removed at runtime: {} {}", ingredients.size(), ingredientType.getIngredientClass().getName());
		if (LOGGER.isDebugEnabled()) {
			String ingredientStrings = ingredients.stream()
				.map(ingredientHelper::getIdentifier)
				.map(Identifier::toString)
				.collect(Collectors.joining(", ", "[", "]"));
			LOGGER.debug("Ingredients removed at runtime: {}", ingredientStrings);
		}

		List<ITypedIngredient<V>> typedIngredients = TypedIngredient.createAndFilterInvalidNonnullList(this, ingredientType, ingredients, false);

		ingredientInfo.removeIngredients(typedIngredients);

		this.listeners.forEach(listener -> listener.onIngredientsRemoved(ingredientHelper, typedIngredients));
	}

	@Override
	public void registerIngredientListener(IIngredientListener listener) {
		ErrorUtil.checkNotNull(listener, "listener");
		this.listeners.add(listener);
	}

	public void onRuntimeStopped() {
		this.listeners.clear();
	}

	@Override
	public IClickableIngredientFactory getClickableIngredientFactory() {
		return new ClickableIngredientFactory(this);
	}

	@Override
	public <V> Optional<ITypedIngredient<V>> createTypedIngredient(IIngredientType<V> ingredientType, V ingredient, boolean normalize) {
		ITypedIngredient<V> result = TypedIngredient.createAndFilterInvalid(this, ingredientType, ingredient, normalize);
		return Optional.ofNullable(result);
	}

	@Override
	public <V> ITypedIngredient<V> checkTypedIngredientFromApi(ITypedIngredient<V> typedIngredient) {
		return TypedIngredientUtil.checkTypedIngredientFromApi(this, typedIngredient);
	}

	@Override
	public <V> ITypedIngredient<V> normalizeTypedIngredient(ITypedIngredient<V> typedIngredient) {
		ITypedIngredient<V> checkedIngredient = checkTypedIngredientFromApi(typedIngredient);
		IIngredientType<V> type = checkedIngredient.getType();
		IIngredientHelper<V> ingredientHelper = getIngredientHelper(type);
		return checkedIngredient.normalize(ingredientHelper);
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated
	public <V> Optional<IClickableIngredient<V>> createClickableIngredient(IIngredientType<V> ingredientType, V ingredient, Rect2i area, boolean normalize) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(area, "area");
		ITypedIngredient<V> typedIngredient = TypedIngredient.createAndFilterInvalid(this, ingredientType, ingredient, normalize);
		if (typedIngredient == null) {
			return Optional.empty();
		}
		ImmutableRect2i slotArea = new ImmutableRect2i(area);
		ClickableIngredient<V> clickableIngredient = new ClickableIngredient<>(typedIngredient, slotArea);
		return Optional.of(clickableIngredient);
	}

	@Override
	public <V> Codec<V> getIngredientCodec(IIngredientType<V> ingredientType) {
		return registeredIngredients
			.getIngredientInfo(ingredientType)
			.getIngredientCodec();
	}

	@Override
	public Collection<String> getIngredientAliases(ITypedIngredient<?> ingredient) {
		return getIngredientAliasesChecked(ingredient);
	}

	private <T> Collection<String> getIngredientAliasesChecked(ITypedIngredient<T> typedIngredient) {
		ITypedIngredient<T> checkedIngredient = checkTypedIngredientFromApi(typedIngredient);
		return getIngredientAliasesInternal(checkedIngredient);
	}

	private <T> Collection<String> getIngredientAliasesInternal(ITypedIngredient<T> typedIngredient) {
		return registeredIngredients
			.getIngredientInfo(typedIngredient.getType())
			.getIngredientAliases(typedIngredient)
			.stream()
			.map(Translator::translateToLocal)
			.sorted(String::compareToIgnoreCase)
			.toList();
	}
}
