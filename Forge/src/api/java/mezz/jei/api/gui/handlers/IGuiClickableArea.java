package mezz.jei.api.gui.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.network.chat.Component;

public interface IGuiClickableArea {
	/**
	 * The hover/click area for this {@link IGuiClickableArea}.
	 * When hovered, the message from {@link #getTooltipStrings()} will be displayed.
	 * When clicked, {@link #onClick(IFocusFactory, IRecipesGui)} will be called.
	 *
	 * Area is in gui-relative coordinates (not absolute Screen coordinates).
	 */
	Rect2i getArea();

	/**
	 * Returns the strings to be shown on the tooltip when this area is hovered over.
	 * Return an empty list to display the default "Show Recipes" message.
	 */
	default List<Component> getTooltipStrings() {
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
	 *
	 * @since 9.5.0
	 */
	static IGuiClickableArea createBasic(int xPos, int yPos, int width, int height, RecipeType<?>... recipeTypes) {
		Rect2i area = new Rect2i(xPos, yPos, width, height);
		List<RecipeType<?>> recipeTypesList = Arrays.asList(recipeTypes);
		return new IGuiClickableArea() {
			@Override
			public Rect2i getArea() {
				return area;
			}

			@Override
			public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
				recipesGui.showTypes(recipeTypesList);
			}
		};
	}

	/**
	 * Helper function to create the most basic type of {@link IGuiClickableArea},
	 * which displays a recipe category on click.
	 *
	 * @deprecated use {@link #createBasic(int, int, int, int, RecipeType[])} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	static IGuiClickableArea createBasic(int xPos, int yPos, int width, int height, ResourceLocation... recipeCategoryUids) {
		Rect2i area = new Rect2i(xPos, yPos, width, height);
		List<ResourceLocation> recipeCategoryUidList = new ArrayList<>();
		Collections.addAll(recipeCategoryUidList, recipeCategoryUids);
		return new IGuiClickableArea() {
			@Override
			public Rect2i getArea() {
				return area;
			}

			@SuppressWarnings("removal")
			@Override
			public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
				recipesGui.showCategories(recipeCategoryUidList);
			}
		};
	}
}
