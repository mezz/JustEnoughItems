package mezz.jei.gui.recipes;

import com.mojang.blaze3d.matrix.MatrixStack;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.input.CombinedMouseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rectangle2d;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IPaged;
import mezz.jei.util.MathUtil;
import net.minecraft.util.text.ITextComponent;

/**
 * The area drawn on top and bottom of the {@link RecipesGui} that show the recipe categories.
 */
public class RecipeGuiTabs implements IPaged {
	private final IRecipeGuiLogic recipeGuiLogic;
	private final List<RecipeGuiTab> tabs = new ArrayList<>();
	private final PageNavigation pageNavigation;
	private IMouseHandler mouseHandler;
	private Rectangle2d area = new Rectangle2d(0, 0, 0, 0);

	private int pageCount = 1;
	private int pageNumber = 0;
	private int categoriesPerPage = 1;

	public RecipeGuiTabs(IRecipeGuiLogic recipeGuiLogic) {
		this.recipeGuiLogic = recipeGuiLogic;
		this.pageNavigation = new PageNavigation(this, true);
		this.mouseHandler = this.pageNavigation.getMouseHandler();
	}

	public void initLayout(RecipesGui recipesGui) {
		ImmutableList<IRecipeCategory<?>> categories = recipeGuiLogic.getRecipeCategories();
		if (!categories.isEmpty()) {
			int totalWidth = 0;
			categoriesPerPage = 0;

			Rectangle2d recipeArea = recipesGui.getArea();
			for (int i = 0; i < categories.size(); i++) {
				if (totalWidth + RecipeGuiTab.TAB_WIDTH <= (recipeArea.getWidth() - 4)) {
					totalWidth += RecipeGuiTab.TAB_WIDTH;
					categoriesPerPage++;
				} else {
					break;
				}
			}

			this.area = new Rectangle2d(
				recipeArea.getX() + 2,
				recipeArea.getY() - RecipeGuiTab.TAB_HEIGHT + 3, // overlaps the recipe gui slightly
				totalWidth,
				RecipeGuiTab.TAB_HEIGHT
			);

			pageCount = MathUtil.divideCeil(categories.size(), categoriesPerPage);

			IRecipeCategory<?> currentCategory = recipeGuiLogic.getSelectedRecipeCategory();
			int categoryIndex = categories.indexOf(currentCategory);
			pageNumber = categoryIndex / categoriesPerPage;

			int navHeight = 20;
			Rectangle2d navigationArea = new Rectangle2d(
				this.area.getX(),
				this.area.getY() - (2 + navHeight),
				this.area.getWidth(),
				navHeight
			);
			pageNavigation.updateBounds(navigationArea);

			updateLayout();
		}
	}

	private void updateLayout() {
		tabs.clear();
		List<IMouseHandler> mouseHandlers = new ArrayList<>();

		ImmutableList<IRecipeCategory<?>> categories = recipeGuiLogic.getRecipeCategories();

		int tabX = area.getX();

		final int startIndex = pageNumber * categoriesPerPage;
		for (int i = 0; i < categoriesPerPage; i++) {
			int index = i + startIndex;
			if (index >= categories.size()) {
				break;
			}
			IRecipeCategory<?> category = categories.get(index);
			RecipeGuiTab tab = new RecipeCategoryTab(recipeGuiLogic, category, tabX, area.getY());
			this.tabs.add(tab);
			mouseHandlers.add(tab);
			tabX += RecipeGuiTab.TAB_WIDTH;
		}

		mouseHandlers.add(this.pageNavigation.getMouseHandler());
		this.mouseHandler = new CombinedMouseHandler(mouseHandlers);

		pageNavigation.updatePageState();
	}

	@SuppressWarnings("deprecation")
	public void draw(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY) {
		IRecipeCategory<?> selectedCategory = recipeGuiLogic.getSelectedRecipeCategory();

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		RecipeGuiTab hovered = null;

		RenderSystem.disableDepthTest();
		RenderSystem.enableAlphaTest();
		{
			for (RecipeGuiTab tab : tabs) {
				boolean selected = tab.isSelected(selectedCategory);
				tab.draw(selected, matrixStack, mouseX, mouseY);
				if (tab.isMouseOver(mouseX, mouseY)) {
					hovered = tab;
				}
			}
		}
		RenderSystem.disableAlphaTest();
		RenderSystem.enableDepthTest();

		pageNavigation.draw(minecraft, matrixStack, mouseX, mouseY, minecraft.getFrameTime());

		if (hovered != null) {
			List<ITextComponent> tooltip = hovered.getTooltip();
			TooltipRenderer.drawHoveringText(tooltip, mouseX, mouseY, matrixStack);
		}
	}

	public IMouseHandler getMouseHandler() {
		return this.mouseHandler;
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