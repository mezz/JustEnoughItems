package mezz.jei.gui.bookmarks;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientBookmarkElement;

import java.util.Objects;

public class IngredientBookmark<T> implements IBookmark {
	private final IElement<T> element;
	private final Object uid;
	private final ITypedIngredient<T> typedIngredient;
	private boolean visible = true;

	public static <T> IngredientBookmark<T> create(ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager) {
		IIngredientType<T> type = typedIngredient.getType();
		typedIngredient = ingredientManager.normalizeTypedIngredient(typedIngredient);
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(type);
		Object uniqueId = ingredientHelper.getUid(typedIngredient.getIngredient(), UidContext.Ingredient);
		return new IngredientBookmark<>(typedIngredient, uniqueId);
	}

	private IngredientBookmark(ITypedIngredient<T> typedIngredient, Object uid) {
		this.typedIngredient = typedIngredient;
		this.uid = uid;
		this.element = new IngredientBookmarkElement<>(this);
	}

	@Override
	public BookmarkType getType() {
		return BookmarkType.INGREDIENT;
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
		return Objects.hash(uid, typedIngredient.getType());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof IngredientBookmark<?> ingredientBookmark) {
			return ingredientBookmark.uid.equals(uid) &&
				ingredientBookmark.typedIngredient.getType().equals(typedIngredient.getType());
		}
		return false;
	}

	@Override
	public String toString() {
		return "IngredientBookmark{" +
			"uid=" + uid +
			", typedIngredient=" + typedIngredient +
			", visible=" + visible +
			'}';
	}
}
