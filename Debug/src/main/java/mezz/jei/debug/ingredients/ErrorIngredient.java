package mezz.jei.debug.ingredients;

import mezz.jei.api.ingredients.IIngredientType;

public record ErrorIngredient(CrashType crashType) {
	public static final IIngredientType<ErrorIngredient> TYPE = () -> ErrorIngredient.class;

	public enum CrashType {
		RenderBreakVertexBufferCrash, TooltipCrash
	}
}
