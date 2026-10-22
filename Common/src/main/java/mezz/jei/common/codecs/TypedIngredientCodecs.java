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
	private final IIngredientManager ingredientManager;
	private final Map<IIngredientType<?>, Codec<ITypedIngredient<?>>> codecMapCache = new HashMap<>();
	private @Nullable Codec<IIngredientType<?>> ingredientTypeCodec;
	private @Nullable MapCodec<ITypedIngredient<?>> ingredientCodec;

	public TypedIngredientCodecs(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	public Codec<IIngredientType<?>> getIngredientTypeCodec() {
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

	public MapCodec<ITypedIngredient<?>> getIngredientCodec() {
		if (ingredientCodec == null) {
			Codec<IIngredientType<?>> ingredientTypeCodec = getIngredientTypeCodec();
			ingredientCodec = ingredientTypeCodec.dispatchMap(
				ITypedIngredient::getType,
				type -> getIngredientCodec(type).fieldOf("ingredient")
			);
		}
		return ingredientCodec;
	}

	@SuppressWarnings("unchecked")
	public <T> Codec<ITypedIngredient<T>> getIngredientCodec(IIngredientType<T> ingredientType) {
		Codec<ITypedIngredient<T>> result = (Codec<ITypedIngredient<T>>) (Object) codecMapCache.get(ingredientType);
		if (result == null) {
			Codec<T> codec = ingredientManager.getIngredientCodec(ingredientType);
			result = create(codec);
			codecMapCache.put(ingredientType, (Codec<ITypedIngredient<?>>) (Object) result);
		}
		return result;
	}

	private <T> Codec<ITypedIngredient<T>> create(Codec<T> ingredientCodec) {
		return ingredientCodec.flatXmap(
			ingredient -> {
				Optional<IIngredientType<T>> type = ingredientManager.getIngredientTypeChecked(ingredient);
				return type.map(ingredientType -> ingredientManager.createTypedIngredient(ingredientType, ingredient, false)
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
