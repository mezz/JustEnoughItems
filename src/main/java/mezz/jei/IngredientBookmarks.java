package mezz.jei;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientBookmarks;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.IngredientListElement;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public class IngredientBookmarks implements IIngredientBookmarks {
	private final IIngredientRegistry ingredientRegistry;

	// Using both cause linked list will retain order of insertion
	private List<String> bookmarkIds = new LinkedList<String>();
	private HashMap<String, Object> bookmarkList = new HashMap<String, Object>();

	public IngredientBookmarks(IIngredientRegistry ingredientRegistry) {
		this.ingredientRegistry = ingredientRegistry;

		String[] bookmarks = Config.getBookmarks();

		for (String uniqueId : bookmarks) {
			bookmarkIds.add(uniqueId);
			bookmarkList.put(uniqueId, findIngredientFromUniqueId(uniqueId));
		}
	}

	@Override
	public <V> void toggleIngredientBookmark(V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		String uniqueId = ingredientHelper.getUniqueId(ingredient);

		// System.out.println(ingredientHelper.getUniqueId(ingredient));
		if (bookmarkIds.contains(uniqueId)) {
			bookmarkIds.remove(uniqueId);
			bookmarkList.remove(uniqueId);
		} else {
			bookmarkIds.add(uniqueId);
			bookmarkList.put(uniqueId, findIngredientFromUniqueId(uniqueId));
		}
		Config.updateBookmarks(bookmarkIds.toArray(new String[bookmarkIds.size()]));
	}

	private <V> V findIngredientFromUniqueId(String uniqueId) {
		ImmutableCollection<Class> classes = ingredientRegistry.getRegisteredIngredientClasses();
		for (Class<V> clazz : classes) {
			ImmutableList<V> iList = ingredientRegistry.getIngredients(clazz);
			IIngredientHelper<V> iHelper = ingredientRegistry.getIngredientHelper(clazz);
			for (V i : iList) {
				if (iHelper.getUniqueId(i).equals(uniqueId)) {
					return i;
				}
			}
		}
		return null;
	}

	@Override
	public List<IIngredientListElement> getIngredientList() {
		List<IIngredientListElement> ingredientListElements = new LinkedList<IIngredientListElement>();

		for (String uniqueId : bookmarkIds) {
			Object ingredient = bookmarkList.get(uniqueId);

			IIngredientHelper<Object> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
			IIngredientRenderer<Object> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredient);
			IngredientListElement<Object> ingredientListElement = IngredientListElement.create(ingredient, ingredientHelper,
					ingredientRenderer);
			if (ingredientListElement != null) {
				ingredientListElements.add(ingredientListElement);
			}
		}
		return ingredientListElements;
	}

	@Override
	public void clear() {
		bookmarkIds.clear();
		bookmarkList.clear();
	}
}
