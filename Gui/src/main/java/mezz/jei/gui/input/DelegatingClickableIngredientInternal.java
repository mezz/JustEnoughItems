package mezz.jei.gui.input;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DelegatingClickableIngredientInternal implements IClickableIngredientInternal {
	private final IClickableIngredientInternal delegate;

	public DelegatingClickableIngredientInternal(IClickableIngredientInternal delegate) {
		this.delegate = delegate;
	}

	@Override
	public ITypedIngredient<?> getTypedIngredient() {
		return delegate.getTypedIngredient();
	}

	@Override
	public IElement getElement() {
		return delegate.getElement();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return delegate.isMouseOver(mouseX, mouseY);
	}

	@Override
	public ItemStack getCheatItemStack(IIngredientManager ingredientManager) {
		return delegate.getCheatItemStack(ingredientManager);
	}

	@Override
	public boolean canClickToFocus() {
		return delegate.canClickToFocus();
	}

	@Override
	public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		delegate.show(recipesGui, focusUtil, roles);
	}
}
