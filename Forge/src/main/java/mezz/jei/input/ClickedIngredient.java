package mezz.jei.input;

import com.google.common.base.MoreObjects;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.ImmutableRect2i;
import org.jetbrains.annotations.Nullable;

public class ClickedIngredient<V> implements IClickedIngredient<V> {
	private final ITypedIngredient<V> value;
	@Nullable
	private final ImmutableRect2i area;
	private final boolean canOverrideVanillaClickHandler;
	private final boolean allowsCheating;

	public ClickedIngredient(ITypedIngredient<V> value, @Nullable ImmutableRect2i area, boolean allowsCheating, boolean canOverrideVanillaClickHandler) {
		ErrorUtil.checkNotNull(value, "value");
		this.value = value;
		this.area = area;
		this.allowsCheating = allowsCheating;
		this.canOverrideVanillaClickHandler = canOverrideVanillaClickHandler;
	}

	@Override
	public ITypedIngredient<V> getTypedIngredient() {
		return value;
	}

	@Nullable
	@Override
	public ImmutableRect2i getArea() {
		return area;
	}

	@Override
	public boolean canOverrideVanillaClickHandler() {
		return this.canOverrideVanillaClickHandler;
	}

	@Override
	public boolean allowsCheating() {
		return allowsCheating;
	}

	@Override
	public String toString() {
		RegisteredIngredients registeredIngredients = Internal.getRegisteredIngredients();
		IIngredientHelper<V> ingredientHelper = registeredIngredients.getIngredientHelper(value.getType());
		return MoreObjects.toStringHelper(ClickedIngredient.class)
			.add("value", ingredientHelper.getUniqueId(value.getIngredient(), UidContext.Ingredient))
			.add("area", area)
			.add("allowsCheating", allowsCheating)
			.add("canOverrideVanillaClickHandler", canOverrideVanillaClickHandler)
			.toString();
	}
}
