package mezz.jei.gui.recipes;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IPaged;
import mezz.jei.util.MathUtil;

/**
 * The area drawn on top and bottom of the {@link RecipesGui} that show the recipe categories.
 */
public class RecipeGuiTabs implements IMouseHandler, IPaged {
	private final IRecipeGuiLogic recipeGuiLogic;
	private final List<RecipeGuiTab> tabs = new ArrayList<>();
	private final PageNavigation pageNavigation;
	private final Rectangle area = new Rectangle();

	private int pageCount = 1;
	private int pageNumber = 0;
	private int categoriesPerPage = 1;

	public RecipeGuiTabs(IRecipeGuiLogic recipeGuiLogic) {
		this.recipeGuiLogic = recipeGuiLogic;
		this.pageNavigation = new PageNavigation(this, true);
	}

	public void initLayout(RecipesGui recipesGui) {
		ImmutableList<IRecipeCategory> categories = recipeGuiLogic.getRecipeCategories();
		if (!categories.isEmpty()) {
			int totalWidth = 0;
			categoriesPerPage = 0;

			for (int i = 0; i < categories.size(); i++) {
				if (totalWidth + RecipeGuiTab.TAB_WIDTH <= (recipesGui.getXSize() - 4)) {
					totalWidth += RecipeGuiTab.TAB_WIDTH;
					categoriesPerPage++;
				} else {
					break;
				}
			}

			area.width = totalWidth;
			area.height = RecipeGuiTab.TAB_HEIGHT;
			area.x = recipesGui.getGuiLeft() + 2;
			area.y = recipesGui.getGuiTop() - RecipeGuiTab.TAB_HEIGHT + 3; // overlaps the recipe gui slightly

			pageCount = MathUtil.divideCeil(categories.size(), categoriesPerPage);

			IRecipeCategory currentCategory = recipeGuiLogic.getSelectedRecipeCategory();
			int categoryIndex = categories.indexOf(currentCategory);
			pageNumber = categoryIndex / categoriesPerPage;

			Rectangle navigationArea = new Rectangle(area);
			navigationArea.height = 20;
			navigationArea.translate(0, -(2 + navigationArea.height));
			pageNavigation.updateBounds(navigationArea);

			updateLayout();
		}
	}

	private void updateLayout() {
		tabs.clear();

		ImmutableList<IRecipeCategory> categories = recipeGuiLogic.getRecipeCategories();

		int tabX = area.x;

		final int startIndex = pageNumber * categoriesPerPage;
		for (int i = 0; i < categoriesPerPage; i++) {
			int index = i + startIndex;
			if (index >= categories.size()) {
				break;
			}
			IRecipeCategory category = categories.get(index);
			RecipeGuiTab tab = new RecipeCategoryTab(recipeGuiLogic, category, tabX, area.y);
			this.tabs.add(tab);
			tabX += RecipeGuiTab.TAB_WIDTH;
		}

		pageNavigation.updatePageState();
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

		pageNavigation.draw(minecraft, mouseX, mouseY, minecraft.getRenderPartialTicks());

		if (hovered != null) {
			List<String> tooltip = hovered.getTooltip();
			TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY);
		}
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return area.contains(mouseX, mouseY) ||
			pageNavigation.isMouseOver();
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
			if (pageNavigation.isMouseOver()) {
				return pageNavigation.handleMouseClickedButtons(mouseX, mouseY);
			}
		}
		return false;
	}

	@Override
	public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
		return false;
	}

	@Override
	public boolean nextPage() {
		if (hasNext()) {
			pageNumber++;
		} else {
			pageNumber = 0;
		}
		updateLayout();
		return true;
	}

	@Override
	public boolean hasNext() {
		return pageNumber + 1 < pageCount;
	}

	@Override
	public boolean previousPage() {
		if (hasPrevious()) {
			pageNumber--;
		} else {
			pageNumber = pageCount - 1;
		}
		updateLayout();
		return true;
	}

	@Override
	public boolean hasPrevious() {
		return pageNumber > 0;
	}

	@Override
	public int getPageCount() {
		return pageCount;
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}
}