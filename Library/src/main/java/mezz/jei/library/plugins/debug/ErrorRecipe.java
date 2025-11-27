package mezz.jei.library.plugins.debug;

public class ErrorRecipe {
	private final CrashType type;

	public ErrorRecipe(CrashType type) {
		this.type = type;
	}

	public CrashType getType() {
		return type;
	}

	public enum CrashType {
		Draw, SetRecipe, CreateRecipeExtras, OnDisplayedIngredientsUpdate, GetTooltip
	}
}
