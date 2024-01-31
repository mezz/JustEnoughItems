package mezz.jei.library.plugins.debug.ingredients;

import mezz.jei.api.ingredients.IIngredientType;

public class ErrorIngredient {
    public static final IIngredientType<ErrorIngredient> TYPE = () -> ErrorIngredient.class;
    private final CrashType crashType;

    public ErrorIngredient(CrashType crashType) {
        this.crashType = crashType;
    }

    public CrashType getCrashType() {
        return crashType;
    }

    public enum CrashType {
        RenderBreakVertexBufferCrash, TooltipCrash
    }
}
