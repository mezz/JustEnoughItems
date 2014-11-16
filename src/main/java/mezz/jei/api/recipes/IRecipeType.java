package mezz.jei.api.recipes;

/* Defines a type of recipe, i.e. Crafting Table Recipe, Furnace Recipe, etc. */
public interface IRecipeType {

	/* Returns the localized name for this recipe type. */
	String getLocalizedName();

	/* Returns the dimensions of the drawn recipe type. */
	int displayWidth();
	int displayHeight();

}
