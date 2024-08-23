package mezz.jei.common.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TypedIngredientCodecs {
	private static final Map<IIngredientType<?>, Codec<ITypedIngredient<?>>> codecMapCache = new HashMap<>();
	private static @Nullable Codec<IIngredientType<?>> ingredientTypeCodec;
	private static @Nullable MapCodec<ITypedIngredient<?>> ingredientCodec;

	public static Codec<IIngredientType<?>> getIngredientTypeCodec(IIngredientManager ingredientManager) {
		if (ingredientTypeCodec == null) {
			ingredientTypeCodec = Codec.STRING.flatXmap(
				uid -> {
					return ingredientManager.getIngredientTypeForUid(uid)
						.map(DataResult::success)
						.orElseGet(() -> DataResult.error(() -> "Failed to find ingredient type for uid: " + uid));
				},
				ingredientType -> {
					String uid = ingredientType.getUid();
					return DataResult.success(uid);
				}
			);
		}
		return ingredientTypeCodec;
	}

	public static MapCodec<ITypedIngredient<?>> getIngredientCodec(IIngredientManager ingredientManager) {
		if (ingredientCodec == null) {
			Codec<IIngredientType<?>> ingredientTypeCodec = getIngredientTypeCodec(ingredientManager);
			ingredientCodec = ingredientTypeCodec.dispatchMap(
				ITypedIngredient::getType,
				type -> getIngredientCodec(type, ingredientManager).fieldOf("ingredient")
			);
		}
		return ingredientCodec;
	}

	@SuppressWarnings("unchecked")
	public static <T> Codec<ITypedIngredient<T>> getIngredientCodec(IIngredientType<T> ingredientType, IIngredientManager ingredientManager) {
		Codec<ITypedIngredient<T>> result = (Codec<ITypedIngredient<T>>) (Object) codecMapCache.get(ingredientType);
		if (result == null) {
			Codec<T> codec = ingredientManager.getIngredientCodec(ingredientType);
			result = create(codec, ingredientManager);
			codecMapCache.put(ingredientType, (Codec<ITypedIngredient<?>>) (Object) result);
		}
		return result;
	}

	private static <T> Codec<ITypedIngredient<T>> create(Codec<T> ingredientCodec, IIngredientManager ingredientManager) {
		return ingredientCodec.flatXmap(
			ingredient -> {
				Optional<IIngredientType<T>> type = ingredientManager.getIngredientTypeChecked(ingredient);
				return type.map(ingredientType ->
						ingredientManager.createTypedIngredient(ingredientType, ingredient)
						.map(DataResult::success)
						.orElseGet(() -> {
							return DataResult.error(() -> {
								IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
								String errorInfo = ingredientHelper.getErrorInfo(ingredient);
								return "Failed to create typed ingredient: " + errorInfo;
							});
						})
					)
					.orElseGet(() -> DataResult.error(() -> "Failed to find type for ingredient: " + ingredient.getClass()));
			},
			typedIngredient -> {
				T ingredient = typedIngredient.getIngredient();
				return DataResult.success(ingredient);
			}
		);
	}
}
