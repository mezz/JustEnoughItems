package mezz.jei.library.plugins.debug.ingredients;

import com.mojang.serialization.Codec;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.common.codecs.EnumCodec;

public record ErrorIngredient(CrashType crashType) {
	public static final IIngredientType<ErrorIngredient> TYPE = () -> ErrorIngredient.class;

	public static final Codec<ErrorIngredient> CODEC = EnumCodec.create(CrashType.class)
		.xmap(ErrorIngredient::new, ErrorIngredient::crashType);

	public enum CrashType {
		RenderBreakVertexBufferCrash, TooltipCrash
	}
}
