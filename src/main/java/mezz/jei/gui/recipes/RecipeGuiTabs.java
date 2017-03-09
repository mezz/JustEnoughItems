package mezz.jei.gui.recipes;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.GuiProperties;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IPaged;
import mezz.jei.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

/**
 * The area drawn on top and bottom of the {@link RecipesGui} that show the recipe categories.
 */
public class RecipeGuiTabs implements IMouseHandler, IPaged {
	private final IRecipeGuiLogic recipeGuiLogic;
	private final List<RecipeGuiTab> tabs = new ArrayList<RecipeGuiTab>();
	private final PageNavigation pageNavigation;

	private int pageCount = 1;
	private int pageNumber = 0;
	private int categoriesPerPage = 1;
	private Rectangle area = new Rectangle();

	public RecipeGuiTabs(IRecipeGuiLogic recipeGuiLogic) {
		this.recipeGuiLogic = recipeGuiLogic;
		this.pageNavigation = new PageNavigation(this, true, new Rectangle());
	}

	public void initLayout(GuiProperties guiProperties) {
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

			area.width = totalWidth;
			area.height = RecipeGuiTab.TAB_HEIGHT;
			area.x = guiProperties.getGuiLeft() + 2;
			area.y = guiProperties.getGuiTop() - RecipeGuiTab.TAB_HEIGHT + 3; // overlaps the recipe gui slightly

			pageCount = MathUtil.divideCeil(categories.size(), categoriesPerPage);

			IRecipeCategory currentCategory = recipeGuiLogic.getSelectedRecipeCategory();
			int categoryIndex = categories.indexOf(currentCategory);
			pageNumber = categoryIndex / categoriesPerPage;

			Rectangle navigationArea = new Rectangle(area);
			navigationArea.height = 20;
			navigationArea.translate(0, -(2 + navigationArea.height));
			pageNavigation.setArea(navigationArea);

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

		pageNavigation.updatePageState(pageNumber, pageCount);
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

		pageNavigation.draw(minecraft, mouseX, mouseY);

		if (hovered != null) {
			String tooltip = hovered.getTooltip();
			if (tooltip != null) {
				TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY);
			}
		}
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return area.contains(mouseX, mouseY) ||
				pageNavigation.isMouseOver(mouseX, mouseY);
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
			if (pageNavigation.isMouseOver(mouseX, mouseY)) {
				return pageNavigation.handleMouseClickedButtons(Minecraft.getMinecraft(), mouseX, mouseY);
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
			updateLayout();
			return true;
		}
		return false;
	}

	@Override
	public boolean hasNext() {
		return pageNumber + 1 < pageCount;
	}

	@Override
	public boolean previousPage() {
		if (hasPrevious()) {
			pageNumber--;
			updateLayout();
			return true;
		}
		return false;
	}

	@Override
	public boolean hasPrevious() {
		return pageNumber > 0;
	}
}