package mezz.jei.library.load.registration;

import com.google.common.base.Preconditions;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.ingredients.IngredientInfo;
import mezz.jei.library.ingredients.IngredientManager;
import mezz.jei.library.ingredients.RegisteredIngredients;
import mezz.jei.library.ingredients.TypedIngredient;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class IngredientManagerBuilder implements IModIngredientRegistration, IIngredientAliasRegistration, IExtraIngredientRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

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

		List<ITypedIngredient<V>> typedIngredients = createTypedIngredients(
			ingredientType,
			allIngredients,
			ingredientHelper,
			"during ingredient registration"
		);
		ingredientInfos.put(ingredientType, new IngredientInfo<>(ingredientType, typedIngredients, ingredientHelper, ingredientRenderer));
	}

	@Override
	public <V> void addExtraIngredients(IIngredientType<V> ingredientType, Collection<V> extraIngredients) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(extraIngredients, "extraIngredients");

		IngredientInfo<V> ingredientInfo = getIngredientInfo(ingredientType);
		IIngredientHelper<V> ingredientHelper = ingredientInfo.getIngredientHelper();
		List<ITypedIngredient<V>> typedIngredients = createTypedIngredients(
			ingredientType,
			extraIngredients,
			ingredientHelper,
			"when adding extra ingredients"
		);
		ingredientInfo.addIngredients(typedIngredients);
	}

	@Override
	public <I> void addAlias(IIngredientType<I> type, I ingredient, String alias) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(alias, "alias");
		checkIngredientType(type, ingredient);

		IngredientInfo<I> ingredientInfo = getIngredientInfo(type);
		ingredientInfo.addIngredientAlias(ingredient, alias);
	}

	@Override
	public <B, I> void addAlias(IIngredientTypeWithSubtypes<B, I> type, B baseIngredient, String alias) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(baseIngredient, "baseIngredient");
		ErrorUtil.checkNotNull(alias, "alias");
		checkBaseIngredientType(type, baseIngredient);

		IngredientInfo<I> ingredientInfo = getIngredientInfo(type);
		ingredientInfo.addBaseIngredientAlias(baseIngredient, alias);
	}

	@Override
	public void addAlias(Fluid fluid, String alias) {
		ErrorUtil.checkNotNull(fluid, "fluid");
		ErrorUtil.checkNotNull(alias, "alias");

		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		IIngredientTypeWithSubtypes<Fluid, ?> fluidIngredientType = fluidHelper.getFluidIngredientType();
		addAlias(fluidIngredientType, fluid, alias);
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
		checkIngredientType(type, ingredient);

		IngredientInfo<I> ingredientInfo = getIngredientInfo(type);
		ingredientInfo.addIngredientAliases(ingredient, aliases);
	}

	@Override
	public <B, I> void addAliases(IIngredientTypeWithSubtypes<B, I> type, B baseIngredient, Collection<String> aliases) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(baseIngredient, "baseIngredient");
		ErrorUtil.checkNotNull(aliases, "aliases");
		checkBaseIngredientType(type, baseIngredient);

		IngredientInfo<I> ingredientInfo = getIngredientInfo(type);
		ingredientInfo.addBaseIngredientAliases(baseIngredient, aliases);
	}

	@Override
	public void addAliases(Fluid fluid, Collection<String> aliases) {
		ErrorUtil.checkNotNull(fluid, "fluid");
		ErrorUtil.checkNotNull(aliases, "aliases");

		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		IIngredientTypeWithSubtypes<Fluid, ?> fluidIngredientType = fluidHelper.getFluidIngredientType();
		addAliases(fluidIngredientType, fluid, aliases);
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
			checkIngredientType(type, ingredient);
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
			checkIngredientType(type, ingredient);
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

	private static <V> List<ITypedIngredient<V>> createTypedIngredients(
		IIngredientType<V> ingredientType,
		Collection<V> ingredients,
		IIngredientHelper<V> ingredientHelper,
		String context
	) {
		List<ITypedIngredient<V>> typedIngredients = new ArrayList<>(ingredients.size());
		for (V ingredient : ingredients) {
			if (!ingredientHelper.isIngredientOnServer(ingredient)) {
				String errorInfo = ingredientHelper.getErrorInfo(ingredient);
				LOGGER.warn("Attempted to add an Ingredient that is not on the server {}: {}", context, errorInfo);
				continue;
			}

			ITypedIngredient<V> typedIngredient = TypedIngredient.createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, false);
			if (typedIngredient == null) {
				LOGGER.warn("Detected an invalid ingredient {}: {}", context, ingredientHelper.getErrorInfo(ingredient));
				continue;
			}

			typedIngredients.add(typedIngredient);
		}
		return typedIngredients;
	}

	private static <I> void checkIngredientType(IIngredientType<I> type, I ingredient) {
		Class<? extends I> ingredientClass = type.getIngredientClass();
		if (!ingredientClass.isInstance(ingredient)) {
			throw new IllegalArgumentException(String.format("ingredient (%s) must be an instance of %s", ingredient.getClass(), ingredientClass));
		}
	}

	private static <B, I> void checkBaseIngredientType(IIngredientTypeWithSubtypes<B, I> type, B baseIngredient) {
		Class<? extends B> ingredientBaseClass = type.getIngredientBaseClass();
		if (!ingredientBaseClass.isInstance(baseIngredient)) {
			throw new IllegalArgumentException(String.format("baseIngredient (%s) must be an instance of %s", baseIngredient.getClass(), ingredientBaseClass));
		}
	}

	@SuppressWarnings("unchecked")
	private <T> IngredientInfo<T> getIngredientInfo(IIngredientType<T> ingredientType) {
		IngredientInfo<?> ingredientInfo = ingredientInfos.get(ingredientType);
		if (ingredientInfo == null) {
			throw new IllegalArgumentException("Ingredient type has not been registered: " + ingredientType.getUid());
		}
		return (IngredientInfo<T>) ingredientInfo;
	}
}
