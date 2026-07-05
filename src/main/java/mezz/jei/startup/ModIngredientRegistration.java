package mezz.jei.startup;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.IngredientSet;

public class ModIngredientRegistration implements IModIngredientRegistration, IIngredientAliasRegistration {
	private final Map<IIngredientType, Collection> allIngredientsMap = new IdentityHashMap<>();
	private final Map<IIngredientType, IIngredientHelper> ingredientHelperMap = new IdentityHashMap<>();
	private final Map<IIngredientType, IIngredientRenderer> ingredientRendererMap = new IdentityHashMap<>();
	private final Map<IIngredientType, ListMultimap<String, String>> ingredientAliasesMap = new IdentityHashMap<>();
	private final Map<IIngredientType, ListMultimap<String, String>> ingredientSubtypeAliasesMap = new IdentityHashMap<>();

	@Override
	public <V> void register(IIngredientType<V> ingredientType, Collection<V> allIngredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(allIngredients, "allIngredients");
		ErrorUtil.checkNotNull(ingredientHelper, "ingredientHelper");
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");

		allIngredientsMap.put(ingredientType, allIngredients);
		ingredientHelperMap.put(ingredientType, ingredientHelper);
		ingredientRendererMap.put(ingredientType, ingredientRenderer);
		ingredientAliasesMap.put(ingredientType, ArrayListMultimap.create());
		ingredientSubtypeAliasesMap.put(ingredientType, ArrayListMultimap.create());
	}

	@Override
	@Deprecated
	public <V> void register(Class<V> ingredientClass, Collection<V> allIngredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");
		register(() -> ingredientClass, allIngredients, ingredientHelper, ingredientRenderer);
	}

	@Override
	public <I> void addAlias(IIngredientType<I> type, I ingredient, String alias) {
		addAliases(type, ingredient, Collections.singleton(alias));
	}

	@Override
	public <I> void addAliasToAllSubtypes(IIngredientType<I> type, I ingredient, String alias) {
		addAliasesToAllSubtypes(type, ingredient, Collections.singleton(alias));
	}

	@Override
	public <I> void addAliasesToAllSubtypes(IIngredientType<I> type, I ingredient, Collection<String> aliases) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(aliases, "aliases");

		IIngredientHelper<I> ingredientHelper = getIngredientHelper(type);
		String wildcardId = ingredientHelper.getWildcardId(ingredient);
		ingredientSubtypeAliasesMap.get(type).putAll(wildcardId, aliases);
	}

	@Override
	public <I> void addAliases(IIngredientType<I> type, I ingredient, Collection<String> aliases) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(aliases, "aliases");

		IIngredientHelper<I> ingredientHelper = getIngredientHelper(type);
		String uid = ingredientHelper.getUniqueId(ingredient);
		ingredientAliasesMap.get(type).putAll(uid, aliases);
	}

	@Override
	public <I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, String alias) {
		addAliases(type, ingredients, Collections.singleton(alias));
	}

	@Override
	public <I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, Collection<String> aliases) {
		ErrorUtil.checkNotNull(ingredients, "ingredients");
		for (I ingredient : ingredients) {
			addAliases(type, ingredient, aliases);
		}
	}

	public IngredientRegistry createIngredientRegistry(IModIdHelper modIdHelper, IngredientBlacklistInternal blacklist) {
		Map<IIngredientType, IngredientSet> ingredientsMap = new IdentityHashMap<>();
		for (Map.Entry<IIngredientType, Collection> entry : allIngredientsMap.entrySet()) {
			IIngredientType ingredientType = entry.getKey();
			@SuppressWarnings("unchecked")
			IngredientSet ingredientSet = createIngredientSet(ingredientType, entry.getValue());
			ingredientsMap.put(ingredientType, ingredientSet);
		}

		return new IngredientRegistry(
			modIdHelper,
			blacklist,
			ingredientsMap,
			ImmutableMap.copyOf(ingredientHelperMap),
			ImmutableMap.copyOf(ingredientRendererMap),
			ImmutableMap.copyOf(ingredientAliasesMap),
			ImmutableMap.copyOf(ingredientSubtypeAliasesMap)
		);
	}

	private <T> IIngredientHelper<T> getIngredientHelper(IIngredientType<T> ingredientType) {
		@SuppressWarnings("unchecked")
		IIngredientHelper<T> ingredientHelper = ingredientHelperMap.get(ingredientType);
		if (ingredientHelper == null) {
			throw new IllegalArgumentException("Unknown ingredient type: " + ingredientType.getIngredientClass());
		}
		return ingredientHelper;
	}

	private <T> IngredientSet<T> createIngredientSet(IIngredientType<T> ingredientType, Collection<T> ingredients) {
		@SuppressWarnings("unchecked")
		IIngredientHelper<T> ingredientHelper = ingredientHelperMap.get(ingredientType);
		IngredientSet<T> ingredientSet = IngredientSet.create(ingredientType, ingredientHelper);
		ingredientSet.addAll(ingredients);
		return ingredientSet;
	}
}
