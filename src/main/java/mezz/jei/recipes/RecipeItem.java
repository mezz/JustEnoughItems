package mezz.jei.recipes;

import java.util.ArrayList;
import java.util.HashMap;

public class RecipeItem {
	/* Ordered list of recipe classes */
	private ArrayList<Class> recipeClasses = new ArrayList<Class>();
	/* List of recipes for each recipe class */
	private HashMap<Class, ArrayList> recipesMap = new HashMap<Class, ArrayList>();
}
