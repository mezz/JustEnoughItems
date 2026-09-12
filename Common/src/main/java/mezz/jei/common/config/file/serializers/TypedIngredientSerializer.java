package mezz.jei.common.config.file.serializers;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import net.mezzdev.config.api.value.serializer.IDeserializeResult;

import java.util.Optional;

public class TypedIngredientSerializer {
	private static final String SEPARATOR = "&";
	private final IIngredientManager ingredientManager;

	public TypedIngredientSerializer(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	public String serialize(ITypedIngredient<?> value) {
		IIngredientType<?> type = value.getType();
		String typeUid = type.getUid();
		String uid = getUid(ingredientManager, value);
		return typeUid + SEPARATOR + uid;
	}

	public static <T> String getUid(IIngredientManager ingredientManager, ITypedIngredient<T> typedIngredient) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
		return ingredientHelper.getUniqueId(typedIngredient, UidContext.Recipe);
	}

	public IDeserializeResult<ITypedIngredient<?>> deserialize(String string) {
		String[] parts = string.split(SEPARATOR);
		if (parts.length != 2) {
			String error = "string must be two uids, separated by '" + SEPARATOR + "': " + string;
			return IDeserializeResult.failure(error);
		}
		String typeUid = parts[0];
		String uid = parts[1];
		Optional<IIngredientType<?>> ingredientTypeForUid = ingredientManager.getIngredientTypeForUid(typeUid);
		if (ingredientTypeForUid.isEmpty()) {
			String error = "no ingredient type was found for uid: " + typeUid;
			return IDeserializeResult.failure(error);
		}
		IIngredientType<?> ingredientType = ingredientTypeForUid.get();
		Optional<? extends ITypedIngredient<?>> ingredient = ingredientManager.getTypedIngredientByUid(ingredientType, uid);
		if (ingredient.isEmpty()) {
			String error = "no ingredient was found for uid: " + uid;
			return IDeserializeResult.failure(error);
		}
		return IDeserializeResult.success(ingredient.get());
	}

}
