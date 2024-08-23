package mezz.jei.library.ingredients;

import com.mojang.serialization.Codec;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.core.collect.ListMultiMap;
import mezz.jei.library.load.registration.LegacyUidCodec;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class IngredientInfo<T> {
	private final IIngredientType<T> ingredientType;
	private final IIngredientHelper<T> ingredientHelper;
	private final IIngredientRenderer<T> ingredientRenderer;
	private final Codec<T> ingredientCodec;
	private final IngredientSet<T> ingredientSet;
	private final ListMultiMap<Object, String> aliases;

	public IngredientInfo(
		IIngredientType<T> ingredientType,
		Collection<T> ingredients,
		IIngredientHelper<T> ingredientHelper,
		IIngredientRenderer<T> ingredientRenderer,
		@Nullable Codec<T> ingredientCodec
	) {
		if (ingredientCodec == null) {
			//noinspection deprecation
			ingredientCodec = LegacyUidCodec.create(this);
		}

		this.ingredientType = ingredientType;
		this.ingredientHelper = ingredientHelper;
		this.ingredientRenderer = ingredientRenderer;
		this.ingredientCodec = ingredientCodec;

		this.ingredientSet = new IngredientSet<>(ingredientHelper, UidContext.Ingredient);
		this.ingredientSet.addAll(ingredients);

		this.aliases = new ListMultiMap<>();
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
	public Collection<T> getAllIngredients() {
		return Collections.unmodifiableCollection(ingredientSet);
	}

	public void addIngredients(Collection<T> ingredients) {
		this.ingredientSet.addAll(ingredients);
	}

	public void removeIngredients(Collection<T> ingredients) {
		this.ingredientSet.removeAll(ingredients);
	}

	@SuppressWarnings({"removal"})
	@Deprecated(forRemoval = true)
	public Optional<T> getIngredientByLegacyUid(String uid) {
		return ingredientSet.getByLegacyUid(uid);
	}

	@Unmodifiable
	public Collection<String> getIngredientAliases(T ingredient) {
		Object uid = ingredientHelper.getUid(ingredient, UidContext.Ingredient);
		return aliases.get(uid);
	}

	public void addIngredientAlias(T ingredient, String alias) {
		Object uid = ingredientHelper.getUid(ingredient, UidContext.Ingredient);
		this.aliases.put(uid, alias);
	}

	public void addIngredientAliases(T ingredient, Collection<String> aliases) {
		Object uid = ingredientHelper.getUid(ingredient, UidContext.Ingredient);
		this.aliases.putAll(uid, aliases);
	}
}
