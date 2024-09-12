package mezz.jei.library.load.registration;

import com.google.common.base.Preconditions;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.ingredients.IngredientInfo;
import mezz.jei.library.ingredients.IngredientManager;
import mezz.jei.library.ingredients.RegisteredIngredients;

import java.util.Collection;
import java.util.LinkedHashMap;

public class IngredientManagerBuilder implements IModIngredientRegistration, IIngredientAliasRegistration, IExtraIngredientRegistration {
	private final LinkedHashMap<IIngredientType<?>, IngredientInfo<?>> ingredientInfos = new LinkedHashMap<>();
	private final ISubtypeManager subtypeManager;
	private final IColorHelper colorHelper;

	public IngredientManagerBuilder(ISubtypeManager subtypeManager, IColorHelper colorHelper) {
		this.subtypeManager = subtypeManager;
		this.colorHelper = colorHelper;
	}

	@Override
	public <V> void register(IIngredientType<V> ingredientType, Collection<V> allIngredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(allIngredients, "allIngredients");
		ErrorUtil.checkNotNull(ingredientHelper, "ingredientHelper");
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");
		Preconditions.checkArgument(ingredientRenderer.getWidth() == 16,
			"the default ingredient renderer registered here will be used for drawing " +
				"ingredients in the ingredient list, and it must have a width of 16"
		);
		Preconditions.checkArgument(ingredientRenderer.getHeight() == 16,
			"the default ingredient renderer registered here will be used for drawing " +
				"ingredients in the ingredient list, and it must have a height of 16"
		);
		if (ingredientInfos.containsKey(ingredientType)) {
			throw new IllegalArgumentException("Ingredient type has already been registered: " + ingredientType.getUid());
		}

		ingredientInfos.put(ingredientType, new IngredientInfo<>(ingredientType, allIngredients, ingredientHelper, ingredientRenderer));
	}

	@Override
	public <V> void addExtraIngredients(IIngredientType<V> ingredientType, Collection<V> extraIngredients) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(extraIngredients, "extraIngredients");

		IngredientInfo<?> ingredientInfo = ingredientInfos.get(ingredientType);
		if (ingredientInfo == null) {
			throw new IllegalArgumentException("Ingredient type has not been registered: " + ingredientType.getUid());
		}
		@SuppressWarnings("unchecked")
		IngredientInfo<V> castIngredientInfo = (IngredientInfo<V>) ingredientInfo;
		castIngredientInfo.addIngredients(extraIngredients);
	}

	@Override
	public <I> void addAlias(IIngredientType<I> type, I ingredient, String alias) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(alias, "alias");

		@SuppressWarnings("unchecked")
		IngredientInfo<I> ingredientInfo = (IngredientInfo<I>) ingredientInfos.get(type);
		ingredientInfo.addIngredientAlias(ingredient, alias);
	}

	@Override
	public <I> void addAlias(ITypedIngredient<I> typedIngredient, String alias) {
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");
		ErrorUtil.checkNotNull(alias, "alias");

		@SuppressWarnings("unchecked")
		IngredientInfo<I> ingredientInfo = (IngredientInfo<I>) ingredientInfos.get(typedIngredient.getType());
		ingredientInfo.addIngredientAlias(typedIngredient.getIngredient(), alias);
	}

	@Override
	public <I> void addAliases(IIngredientType<I> type, I ingredient, Collection<String> aliases) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(aliases, "aliases");

		@SuppressWarnings("unchecked")
		IngredientInfo<I> ingredientInfo = (IngredientInfo<I>) ingredientInfos.get(type);
		ingredientInfo.addIngredientAliases(ingredient, aliases);
	}

	@Override
	public <I> void addAliases(ITypedIngredient<I> typedIngredient, Collection<String> aliases) {
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");
		ErrorUtil.checkNotNull(aliases, "aliases");

		@SuppressWarnings("unchecked")
		IngredientInfo<I> ingredientInfo = (IngredientInfo<I>) ingredientInfos.get(typedIngredient.getType());
		ingredientInfo.addIngredientAliases(typedIngredient.getIngredient(), aliases);
	}

	@Override
	public <I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, String alias) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredients, "ingredients");
		ErrorUtil.checkNotNull(alias, "alias");

		@SuppressWarnings("unchecked")
		IngredientInfo<I> ingredientInfo = (IngredientInfo<I>) ingredientInfos.get(type);
		for (I ingredient : ingredients) {
			ingredientInfo.addIngredientAlias(ingredient, alias);
		}
	}

	@Override
	public <I> void addAliases(Collection<ITypedIngredient<I>> typedIngredients, String alias) {
		ErrorUtil.checkNotNull(typedIngredients, "typedIngredients");
		ErrorUtil.checkNotNull(alias, "alias");

		IngredientInfo<I> ingredientInfo = null;
		for (ITypedIngredient<I> typedIngredient : typedIngredients) {
			IIngredientType<I> ingredientType = typedIngredient.getType();
			if (ingredientInfo == null) {
				//noinspection unchecked
				ingredientInfo = (IngredientInfo<I>) ingredientInfos.get(ingredientType);
			}
			ingredientInfo.addIngredientAlias(typedIngredient.getIngredient(), alias);
		}
	}

	@Override
	public <I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, Collection<String> aliases) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredients, "ingredients");
		ErrorUtil.checkNotNull(aliases, "aliases");

		@SuppressWarnings("unchecked")
		IngredientInfo<I> ingredientInfo = (IngredientInfo<I>) ingredientInfos.get(type);
		for (I ingredient : ingredients) {
			ingredientInfo.addIngredientAliases(ingredient, aliases);
		}
	}

	@Override
	public <I> void addAliases(Collection<ITypedIngredient<I>> typedIngredients, Collection<String> aliases) {
		ErrorUtil.checkNotNull(typedIngredients, "typedIngredients");
		ErrorUtil.checkNotNull(aliases, "aliases");

		IngredientInfo<I> ingredientInfo = null;
		for (ITypedIngredient<I> typedIngredient : typedIngredients) {
			IIngredientType<I> ingredientType = typedIngredient.getType();
			if (ingredientInfo == null) {
				//noinspection unchecked
				ingredientInfo = (IngredientInfo<I>) ingredientInfos.get(ingredientType);
			}
			ingredientInfo.addIngredientAliases(typedIngredient.getIngredient(), aliases);
		}
	}

	@Override
	public ISubtypeManager getSubtypeManager() {
		return subtypeManager;
	}

	@Override
	public IColorHelper getColorHelper() {
		return colorHelper;
	}

	public IIngredientManager build() {
		RegisteredIngredients registeredIngredients = new RegisteredIngredients(ingredientInfos);
		return new IngredientManager(registeredIngredients);
	}
}
