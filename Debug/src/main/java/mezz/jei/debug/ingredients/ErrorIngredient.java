package mezz.jei.debug.ingredients;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import mezz.jei.api.ingredients.IIngredientType;

public record ErrorIngredient(CrashType crashType) {
	public static final IIngredientType<ErrorIngredient> TYPE = () -> ErrorIngredient.class;

	public static final Codec<ErrorIngredient> CODEC = createCrashTypeCodec()
		.xmap(ErrorIngredient::new, ErrorIngredient::crashType);

	private static Codec<CrashType> createCrashTypeCodec() {
		return Codec.STRING.flatXmap(
			name -> {
				try {
					CrashType crashType = CrashType.valueOf(name);
					return DataResult.success(crashType);
				} catch (IllegalArgumentException ignored) {
					return DataResult.error(() -> "Unknown debug ingredient crash type: " + name);
				}
			},
			crashType -> DataResult.success(crashType.name())
		);
	}

	public enum CrashType {
		TooltipCrash
	}
}
