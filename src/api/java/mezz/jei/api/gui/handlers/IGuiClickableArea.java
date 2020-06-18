package mezz.jei.api.gui.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.runtime.IRecipesGui;

public interface IGuiClickableArea {
	/**
	 * The hover/click area for this {@link IGuiClickableArea}.
	 * When hovered, the message from {@link #getTooltipStrings()} will be displayed.
	 * When clicked, {@link #onClick(IFocusFactory, IRecipesGui)} will be called.
	 */
	Rectangle2d getArea();

	/**
	 * Returns the strings to be shown on the tooltip when this area is hovered over.
	 * Return an empty list to display the default "Show Recipes" message.
	 */
	default List<String> getTooltipStrings() {
		return Collections.emptyList();
	}

	/**
	 * Called when the area is clicked.
	 * This method is passed some parameters to allow plugins to conveniently
	 * show recipes or recipe categories when their recipe area is clicked.
	 */
	void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui);

	/**
	 * Helper function to create the most basic type of {@link IGuiClickableArea},
	 * which displays a recipe category on click.
	 */
	static IGuiClickableArea createBasic(int xPos, int yPos, int width, int height, ResourceLocation... recipeCategoryUids) {
		Rectangle2d area = new Rectangle2d(xPos, yPos, width, height);
		List<ResourceLocation> recipeCategoryUidList = new ArrayList<>();
		Collections.addAll(recipeCategoryUidList, recipeCategoryUids);
		return new IGuiClickableArea() {
			@Override
			public Rectangle2d getArea() {
				return area;
			}

			@Override
			public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
				recipesGui.showCategories(recipeCategoryUidList);
			}
		};
	}
}
