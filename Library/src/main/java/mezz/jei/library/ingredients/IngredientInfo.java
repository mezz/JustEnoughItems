package mezz.jei.library.ingredients;

import com.google.common.collect.Collections2;
import com.mojang.serialization.Codec;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.collect.ListMultiMap;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;

public class IngredientInfo<T> {
	private final IIngredientType<T> ingredientType;
	private final IIngredientHelper<T> ingredientHelper;
	private final IIngredientRenderer<T> ingredientRenderer;
	private final Codec<T> ingredientCodec;
	private final RegisteredIngredientIndex<T> ingredientIndex;
	private final ListMultiMap<Object, String> aliases;
	private final ListMultiMap<Object, String> baseAliases;

	public IngredientInfo(
		IIngredientType<T> ingredientType,
		Collection<ITypedIngredient<T>> ingredients,
		IIngredientHelper<T> ingredientHelper,
		IIngredientRenderer<T> ingredientRenderer,
		Codec<T> ingredientCodec
	) {
		this.ingredientType = ingredientType;
		this.ingredientHelper = ingredientHelper;
		this.ingredientRenderer = ingredientRenderer;
		this.ingredientCodec = ingredientCodec;

		this.ingredientIndex = new RegisteredIngredientIndex<>(ingredientHelper);
		this.ingredientIndex.addAll(ingredients);

		this.aliases = new ListMultiMap<>();
		this.baseAliases = new ListMultiMap<>(new IdentityHashMap<>(), ArrayList::new);
	}

	public IIngredientType<T> getIngredientType() {
		return ingredientType;
	}

	public IIngredientHelper<T> getIngredientHelper() {
		return ingredientHelper;
	}

	public IIngredientRenderer<T> getIngredientRenderer() {
		return ingredientRenderer;
	}

	public Codec<T> getIngredientCodec() {
		return ingredientCodec;
	}

	@Unmodifiable
	public Collection<ITypedIngredient<T>> getAllTypedIngredients() {
		return ingredientIndex.getAllIngredients();
	}

	@Unmodifiable
	public Collection<T> getAllIngredients() {
		Collection<T> transform = Collections2.transform(ingredientIndex.getAllIngredients(), ITypedIngredient::getIngredient);
		return Collections.unmodifiableCollection(transform);
	}

	public void addIngredients(Collection<ITypedIngredient<T>> ingredients) {
		this.ingredientIndex.addAll(ingredients);
	}

	public void removeIngredients(Collection<ITypedIngredient<T>> ingredients) {
		this.ingredientIndex.removeAll(ingredients);
	}

	@Unmodifiable
	public List<ITypedIngredient<T>> getGroupedIngredients(ITypedIngredient<T> ingredient) {
		Object groupingUid = ingredientHelper.getGroupingUid(ingredient);
		return ingredientIndex.getIngredientsByGroupingUid(groupingUid);
	}

	@Unmodifiable
	public Collection<String> getIngredientAliases(ITypedIngredient<T> ingredient) {
		Object uid = ingredientHelper.getUid(ingredient, UidContext.Ingredient);
		Collection<String> ingredientAliases = aliases.get(uid);
		Collection<String> baseIngredientAliases = getBaseIngredientAliases(ingredient);
		if (ingredientAliases.isEmpty()) {
			return baseIngredientAliases;
		}
		if (baseIngredientAliases.isEmpty()) {
			return ingredientAliases;
		}
		List<String> combinedAliases = new ArrayList<>(ingredientAliases.size() + baseIngredientAliases.size());
		combinedAliases.addAll(ingredientAliases);
		combinedAliases.addAll(baseIngredientAliases);
		return Collections.unmodifiableList(combinedAliases);
	}

	public void addIngredientAlias(T ingredient, String alias) {
		Object uid = ingredientHelper.getUid(ingredient, UidContext.Ingredient);
		this.aliases.put(uid, alias);
	}

	public void addIngredientAlias(ITypedIngredient<T> ingredient, String alias) {
		Object uid = ingredientHelper.getUid(ingredient, UidContext.Ingredient);
		this.aliases.put(uid, alias);
	}

	public void addIngredientAliases(T ingredient, Collection<String> aliases) {
		Object uid = ingredientHelper.getUid(ingredient, UidContext.Ingredient);
		this.aliases.putAll(uid, aliases);
	}

	public void addIngredientAliases(ITypedIngredient<T> ingredient, Collection<String> aliases) {
		Object uid = ingredientHelper.getUid(ingredient, UidContext.Ingredient);
		this.aliases.putAll(uid, aliases);
	}

	public void addBaseIngredientAlias(Object baseIngredient, String alias) {
		this.baseAliases.put(baseIngredient, alias);
	}

	public void addBaseIngredientAliases(Object baseIngredient, Collection<String> aliases) {
		this.baseAliases.putAll(baseIngredient, aliases);
	}

	@Unmodifiable
	private Collection<String> getBaseIngredientAliases(ITypedIngredient<T> ingredient) {
		if (ingredientType instanceof IIngredientTypeWithSubtypes<?, T> ingredientTypeWithSubtypes) {
			Object baseIngredient = ingredient.getBaseIngredient(ingredientTypeWithSubtypes);
			return baseAliases.get(baseIngredient);
		}
		return Collections.emptyList();
	}
}
