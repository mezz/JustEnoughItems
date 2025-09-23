package mezz.jei.library.load.registration;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.ingredients.IngredientInfo;
import mezz.jei.library.ingredients.IngredientManager;
import mezz.jei.library.ingredients.RegisteredIngredients;
import mezz.jei.library.ingredients.TypedIngredient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

public class IngredientManagerBuilder implements IModIngredientRegistration, IIngredientAliasRegistration, IExtraIngredientRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	private final SequencedMap<IIngredientType<?>, IngredientInfo<?>> ingredientInfos = new LinkedHashMap<>();
	private final ISubtypeManager subtypeManager;
	private final IColorHelper colorHelper;

	public IngredientManagerBuilder(ISubtypeManager subtypeManager, IColorHelper colorHelper) {
		this.subtypeManager = subtypeManager;
		this.colorHelper = colorHelper;
	}

	@Override
	public <V> void register(
		IIngredientType<V> ingredientType,
		Collection<V> allIngredients,
		IIngredientHelper<V> ingredientHelper,
		IIngredientRenderer<V> ingredientRenderer,
		Codec<V> ingredientCodec
	) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(allIngredients, "allIngredients");
		ErrorUtil.checkNotNull(ingredientHelper, "ingredientHelper");
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");
		ErrorUtil.checkNotNull(ingredientCodec, "ingredientCodec");
		Preconditions.checkArgument(ingredientRenderer.getWidth() == 16,
			"the default ingredient renderer registered here will be used for drawing " +
				"ingredients in the ingredient list, and it must have a width of 16"
		);
		Preconditions.checkArgument(ingredientRenderer.getHeight() == 16,
			"the default ingredient renderer registered here will be used for drawing " +
				"ingredients in the ingredient list, and it must have a height of 16"
		);

		if (ingredientInfos.containsKey(ingredientType)) {
			throw new IllegalArgumentException("Ingredient type has already been registered: " + ingredientType.getIngredientClass());
		}

		List<ITypedIngredient<V>> allTypedIngredients = new ArrayList<>(allIngredients.size());
		for (V ingredient : allIngredients) {
			if (!ingredientHelper.isIngredientOnServer(ingredient)) {
				String errorInfo = ingredientHelper.getErrorInfo(ingredient);
				LOGGER.warn("Attempted to add an Ingredient that is not on the server: {}", errorInfo);
				continue;
			}
			ITypedIngredient<V> typedIngredient = TypedIngredient.createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, false);
			if (typedIngredient == null) {
				LOGGER.warn("Detected an invalid ingredient during ingredient registration: {}", ingredientHelper.getErrorInfo(ingredient));
				continue;
			}

			allTypedIngredients.add(typedIngredient);
		}

		ingredientInfos.put(ingredientType, new IngredientInfo<>(ingredientType, allTypedIngredients, ingredientHelper, ingredientRenderer, ingredientCodec));
	}

	@Override
	public <V> void addExtraIngredients(IIngredientType<V> ingredientType, Collection<V> extraIngredients) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(extraIngredients, "extraIngredients");

		IngredientInfo<V> castIngredientInfo = getIngredientInfo(ingredientType);
		IIngredientHelper<V> ingredientHelper = castIngredientInfo.getIngredientHelper();

		List<ITypedIngredient<V>> extraTypedIngredients = new ArrayList<>(extraIngredients.size());
		for (V ingredient : extraIngredients) {
			if (!ingredientHelper.isIngredientOnServer(ingredient)) {
				String errorInfo = ingredientHelper.getErrorInfo(ingredient);
				LOGGER.warn("Attempted to add an extra Ingredient that is not on the server: {}", errorInfo);
				continue;
			}

			ITypedIngredient<V> typedIngredient = TypedIngredient.createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, false);
			if (typedIngredient == null) {
				LOGGER.warn("Detected an invalid ingredient when adding extra ingredients: {}", ingredientHelper.getErrorInfo(ingredient));
				continue;
			}

			extraTypedIngredients.add(typedIngredient);
		}
		castIngredientInfo.addIngredients(extraTypedIngredients);
	}

	@Override
	public <I> void addAlias(IIngredientType<I> type, I ingredient, String alias) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(alias, "alias");

		IngredientInfo<I> ingredientInfo = getIngredientInfo(type);
		ingredientInfo.addIngredientAlias(ingredient, alias);
	}

	@Override
	public <I> void addAlias(ITypedIngredient<I> typedIngredient, String alias) {
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");
		ErrorUtil.checkNotNull(alias, "alias");

		IngredientInfo<I> ingredientInfo = getIngredientInfo(typedIngredient.getType());
		ingredientInfo.addIngredientAlias(typedIngredient, alias);
	}

	@Override
	public <I> void addAliases(IIngredientType<I> type, I ingredient, Collection<String> aliases) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(aliases, "aliases");

		IngredientInfo<I> ingredientInfo = getIngredientInfo(type);
		ingredientInfo.addIngredientAliases(ingredient, aliases);
	}

	@Override
	public <I> void addAliases(ITypedIngredient<I> typedIngredient, Collection<String> aliases) {
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");
		ErrorUtil.checkNotNull(aliases, "aliases");

		IngredientInfo<I> ingredientInfo = getIngredientInfo(typedIngredient.getType());
		ingredientInfo.addIngredientAliases(typedIngredient, aliases);
	}

	@Override
	public <I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, String alias) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredients, "ingredients");
		ErrorUtil.checkNotNull(alias, "alias");

		IngredientInfo<I> ingredientInfo = getIngredientInfo(type);
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
				ingredientInfo = getIngredientInfo(ingredientType);
			}
			ingredientInfo.addIngredientAlias(typedIngredient, alias);
		}
	}

	@Override
	public <I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, Collection<String> aliases) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredients, "ingredients");
		ErrorUtil.checkNotNull(aliases, "aliases");

		IngredientInfo<I> ingredientInfo = getIngredientInfo(type);
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
				ingredientInfo = getIngredientInfo(ingredientType);
			}
			ingredientInfo.addIngredientAliases(typedIngredient, aliases);
		}
	}

	private <T> IngredientInfo<T> getIngredientInfo(IIngredientType<T> ingredientType) {
		IngredientInfo<?> ingredientInfo = ingredientInfos.get(ingredientType);
		if (ingredientInfo == null) {
			throw new IllegalArgumentException("Ingredient type has not been registered: " + ingredientType.getUid());
		}
		@SuppressWarnings("unchecked")
		IngredientInfo<T> cast = (IngredientInfo<T>) ingredientInfo;
		return cast;
	}

	@Override
	public ISubtypeManager getSubtypeManager() {
		return subtypeManager;
	}

	@Override
	public IColorHelper getColorHelper() {
		return colorHelper;
	}

	public IngredientManager build() {
		RegisteredIngredients registeredIngredients = new RegisteredIngredients(ingredientInfos);
		return new IngredientManager(registeredIngredients);
	}
}
