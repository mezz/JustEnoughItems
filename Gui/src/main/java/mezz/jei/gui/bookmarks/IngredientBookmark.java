package mezz.jei.gui.bookmarks;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientBookmarkElement;

public class IngredientBookmark<T> implements IBookmark {
	private final IElement<T> element;
	private final String uid;
	private final ITypedIngredient<T> typedIngredient;
	private boolean visible = true;

	public static <T> IngredientBookmark<T> create(ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
		typedIngredient = ingredientManager.normalizeTypedIngredient(typedIngredient);
		String uniqueId = ingredientHelper.getUniqueId(typedIngredient.getIngredient(), UidContext.Ingredient);
		return new IngredientBookmark<>(typedIngredient, uniqueId);
	}

	private IngredientBookmark(ITypedIngredient<T> typedIngredient, String uid) {
		this.typedIngredient = typedIngredient;
		this.uid = uid;
		this.element = new IngredientBookmarkElement<>(this);
	}

	public ITypedIngredient<T> getIngredient() {
		return typedIngredient;
	}

	@Override
	public IElement<?> getElement() {
		return element;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public int hashCode() {
		return uid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof IngredientBookmark<?> ingredientBookmark) {
			return ingredientBookmark.uid.equals(uid);
		}
		return false;
	}
}
