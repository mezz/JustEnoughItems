package mezz.jei.ingredients;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;

public final class IngredientUtil {
	private IngredientUtil() {

	}

	public static <V> List<String> getUniqueIdsWithWildcard(IIngredientHelper<V> ingredientHelper, V ingredient) {
		String uid = ingredientHelper.getUniqueId(ingredient);
		String uidWild = ingredientHelper.getWildcardId(ingredient);

		if (uid.equals(uidWild)) {
			return Collections.singletonList(uid);
		} else {
			return Arrays.asList(uid, uidWild);
		}
	}

}
