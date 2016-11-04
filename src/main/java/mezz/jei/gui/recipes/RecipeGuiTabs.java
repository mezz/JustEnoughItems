package mezz.jei.gui.recipes;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.GuiProperties;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.input.IMouseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

/**
 * The area drawn on top of the {@link RecipesGui} that show the recipe categories.
 */
public class RecipeGuiTabs implements IMouseHandler {
	private final IRecipeGuiLogic recipeGuiLogic;
	private final List<RecipeGuiTab> tabs = new ArrayList<RecipeGuiTab>();

	private int pageCount = 1;
	private int pageNumber = 0;
	private int categoriesPerPage = 1;
	private int left;
	private int top;
	private int width;
	private int height;

	public RecipeGuiTabs(IRecipeGuiLogic recipeGuiLogic) {
		this.recipeGuiLogic = recipeGuiLogic;
	}

	public void updateLayout(GuiProperties guiProperties) {
		ImmutableList<IRecipeCategory> categories = recipeGuiLogic.getRecipeCategories();
		if (!categories.isEmpty()) {
			int totalWidth = 0;
			categoriesPerPage = 0;

			for (int i = 0; i < categories.size(); i++) {
				if (totalWidth + RecipeGuiTab.TAB_WIDTH <= (guiProperties.getGuiXSize() - 4)) {
					totalWidth += RecipeGuiTab.TAB_WIDTH;
					categoriesPerPage++;
				} else {
					break;
				}
			}

			width = totalWidth;
			height = RecipeGuiTab.TAB_HEIGHT;
			left = guiProperties.getGuiLeft() + 2;
			top = guiProperties.getGuiTop() - RecipeGuiTab.TAB_HEIGHT + 3; // overlaps the recipe gui slightly

			pageCount = getPageCount(categories.size(), categoriesPerPage);

			IRecipeCategory currentCategory = recipeGuiLogic.getSelectedRecipeCategory();
			int categoryIndex = categories.indexOf(currentCategory);
			pageNumber = getPageNumber(categoryIndex, pageCount, categoriesPerPage);

			createTabs();
		}
	}

	private static int getPageCount(int categoryCount, final int categoriesPerPage) {
		int pageCount = 0;
		while (categoryCount > 0) {
			int availableCategories = categoriesPerPage;
			if (pageCount > 0) {
				availableCategories--; // back button
			}

			if (categoryCount > availableCategories) {
				availableCategories--; // next button
			}
			categoryCount -= availableCategories;
			pageCount++;
		}

		return pageCount;
	}

	private static int getFirstCategoryIndex(final int pageNumber, final int pageCount, final int categoriesPerPage) {
		int categoryIndex = 0;

		for (int i = 0; i < pageNumber; i++) {
			int availableCategories = categoriesPerPage;
			if (i > 0) {
				availableCategories--; // back button
			}

			if (i + 1 < pageCount) {
				availableCategories--; // next button
			}

			categoryIndex += availableCategories;
		}

		return categoryIndex;
	}

	private static int getPageNumber(final int categoryIndex, final int pageCount, final int categoriesPerPage) {
		int currentCategoryIndex = 0;
		for (int pageNumber = 0; pageNumber < pageCount; pageNumber++) {
			int availableCategories = categoriesPerPage;
			if (pageNumber > 0) {
				availableCategories--; // back button
			}

			if (pageNumber + 1 < pageCount) {
				availableCategories--; // next button
			}

			currentCategoryIndex += availableCategories;
			if (currentCategoryIndex > categoryIndex) {
				return pageNumber;
			}
		}

		return 0;
	}

	private void createTabs() {
		tabs.clear();

		ImmutableList<IRecipeCategory> categories = recipeGuiLogic.getRecipeCategories();

		boolean nextButton = false;
		boolean prevButton = false;

		int categoryCount = categoriesPerPage;
		if (categories.size() > categoryCount) {
			if (pageNumber + 1 < pageCount) {
				nextButton = true;
				categoryCount--;
			}
			if (pageNumber > 0) {
				prevButton = true;
				categoryCount--;
			}
		}

		int tabX = this.left;

		if (prevButton) {
			RecipeArrowTab tab = new RecipeArrowTab(this, false, tabX, top);
			this.tabs.add(tab);
			tabX += RecipeGuiTab.TAB_WIDTH;
		}

		final int startIndex = getFirstCategoryIndex(pageNumber, pageCount, categoriesPerPage);
		for (int i = 0; i < categoryCount; i++) {
			int index = i + startIndex;
			if (index >= categories.size()) {
				break;
			}
			IRecipeCategory category = categories.get(index);
			RecipeGuiTab tab = new RecipeCategoryTab(recipeGuiLogic, category, tabX, top);
			this.tabs.add(tab);
			tabX += RecipeGuiTab.TAB_WIDTH;
		}

		if (nextButton) {
			RecipeArrowTab tab = new RecipeArrowTab(this, true, tabX, top);
			this.tabs.add(tab);
		}
	}

	public void draw(Minecraft minecraft, int mouseX, int mouseY) {
		IRecipeCategory selectedCategory = recipeGuiLogic.getSelectedRecipeCategory();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		RecipeGuiTab hovered = null;

		GlStateManager.disableDepth();
		GlStateManager.enableAlpha();
		{
			for (RecipeGuiTab tab : tabs) {
				boolean selected = tab.isSelected(selectedCategory);
				tab.draw(minecraft, selected, mouseX, mouseY);
				if (tab.isMouseOver(mouseX, mouseY)) {
					hovered = tab;
				}
			}
		}
		GlStateManager.disableAlpha();
		GlStateManager.enableDepth();

		if (hovered != null) {
			String tooltip = hovered.getTooltip();
			if (tooltip != null) {
				TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY);
			}
		}
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return (mouseX > left && mouseX < (left + width)) && (mouseY > top && mouseY < (top + height));
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			for (RecipeGuiTab tab : tabs) {
				if (tab.isMouseOver(mouseX, mouseY)) {
					tab.handleMouseClicked(mouseX, mouseY, mouseButton);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
		return false;
	}

	public void nextPage() {
		if (pageNumber + 1 < pageCount) {
			pageNumber++;
			createTabs();
		}
	}

	public void prevPage() {
		if (pageNumber > 0) {
			pageNumber--;
			createTabs();
		}
	}
}