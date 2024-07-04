package mezz.jei.common.config.file.serializers;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;

import java.util.Collection;
import java.util.Optional;

public class TypedIngredientSerializer implements IJeiConfigValueSerializer<ITypedIngredient<?>>  {
	private static final String SEPARATOR = "&";
	private final IIngredientManager ingredientManager;

	public TypedIngredientSerializer(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@Override
	public String serialize(ITypedIngredient<?> value) {
		IIngredientType<?> type = value.getType();
		String typeUid = type.getUid();
		String uid = getUid(ingredientManager, value);
		return typeUid + SEPARATOR + uid;
	}

	public static <T> String getUid(IIngredientManager ingredientManager, ITypedIngredient<T> typedIngredient) {
		T ingredient = typedIngredient.getIngredient();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		return ingredientHelper.getUniqueId(ingredient, UidContext.Recipe);
	}

	@Override
	public IDeserializeResult<ITypedIngredient<?>> deserialize(String string) {
		String[] parts = string.split(SEPARATOR);
		if (parts.length != 2) {
			String error = "string must be two uids, separated by '" + SEPARATOR + "': " + string;
			return new DeserializeResult<>(null, error);
		}
		String typeUid = parts[0];
		String uid = parts[1];
		Optional<IIngredientType<?>> ingredientTypeForUid = ingredientManager.getIngredientTypeForUid(typeUid);
		if (ingredientTypeForUid.isEmpty()) {
			String error = "no ingredient type was found for uid: " + typeUid;
			return new DeserializeResult<>(null, error);
		}
		IIngredientType<?> ingredientType = ingredientTypeForUid.get();
		Optional<? extends ITypedIngredient<?>> ingredient = ingredientManager.getTypedIngredientByUid(ingredientType, uid);
		if (ingredient.isEmpty()) {
			String error = "no ingredient was found for uid: " + uid;
			return new DeserializeResult<>(null, error);
		}
		return new DeserializeResult<>(ingredient.get());
	}

	@Override
	public boolean isValid(ITypedIngredient<?> value) {
		return true;
	}

	@Override
	public Optional<Collection<ITypedIngredient<?>>> getAllValidValues() {
		return Optional.empty();
	}

	@Override
	public String getValidValuesDescription() {
		return "";
	}
}
