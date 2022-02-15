package mezz.jei.api.recipe;

/**
 * The relationship between an ingredient and a recipe.
 *
 * @since 9.3.0
 */
public enum RecipeIngredientRole {
	/**
	 * Input ingredients are consumed when a recipe is crafted.
	 */
	INPUT,
	/**
	 * Output ingredients are the result of a crafted recipe.
	 */
	OUTPUT,
	/**
	 * Catalysts are ingredients that are necessary for crafting, but are not consumed.
	 * These are treated similarly to {@link #INPUT}.
	 * Examples may be a crafting table, a furnace, or an ingredient that sits in the crafting grid but is not used up.
	 */
	CATALYST,
	/**
	 * Render-only ingredients should be drawn, and can be navigated on,
	 * but are ignored when looking up the recipe.
	 */
	RENDER_ONLY
}
