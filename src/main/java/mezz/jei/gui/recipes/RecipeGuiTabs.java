package mezz.jei.gui.recipes;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.input.mouse.handlers.CombinedInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.input.IPaged;
import mezz.jei.util.MathUtil;
import net.minecraft.network.chat.Component;

/**
 * The area drawn on top and bottom of the {@link RecipesGui} that show the recipe categories.
 */
public class RecipeGuiTabs implements IPaged {
	private final IRecipeGuiLogic recipeGuiLogic;
	private final List<RecipeGuiTab> tabs = new ArrayList<>();
	private final PageNavigation pageNavigation;
	private IUserInputHandler inputHandler;
	private Rect2i area = new Rect2i(0, 0, 0, 0);

	private int pageCount = 1;
	private int pageNumber = 0;
	private int categoriesPerPage = 1;

	public RecipeGuiTabs(IRecipeGuiLogic recipeGuiLogic) {
		this.recipeGuiLogic = recipeGuiLogic;
		this.pageNavigation = new PageNavigation(this, true);
		this.inputHandler = this.pageNavigation.createInputHandler();
	}

	public void initLayout(RecipesGui recipesGui) {
		ImmutableList<IRecipeCategory<?>> categories = recipeGuiLogic.getRecipeCategories();
		if (!categories.isEmpty()) {
			int totalWidth = 0;
			categoriesPerPage = 0;

			Rect2i recipeArea = recipesGui.getArea();
			for (int i = 0; i < categories.size(); i++) {
				if (totalWidth + RecipeGuiTab.TAB_WIDTH <= (recipeArea.getWidth() - 4)) {
					totalWidth += RecipeGuiTab.TAB_WIDTH;
					categoriesPerPage++;
				} else {
					break;
				}
			}

			this.area = new Rect2i(
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
			Rect2i navigationArea = new Rect2i(
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
		List<IUserInputHandler> inputHandlers = new ArrayList<>();

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
			inputHandlers.add(tab);
			tabX += RecipeGuiTab.TAB_WIDTH;
		}

		inputHandlers.add(this.pageNavigation.createInputHandler());
		this.inputHandler = new CombinedInputHandler(inputHandlers);

		pageNavigation.updatePageState();
	}

	public void draw(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		IRecipeCategory<?> selectedCategory = recipeGuiLogic.getSelectedRecipeCategory();

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		RecipeGuiTab hovered = null;

		RenderSystem.disableDepthTest();
		{
			for (RecipeGuiTab tab : tabs) {
				boolean selected = tab.isSelected(selectedCategory);
				tab.draw(selected, poseStack, mouseX, mouseY);
				if (tab.isMouseOver(mouseX, mouseY)) {
					hovered = tab;
				}
			}
		}
		RenderSystem.enableDepthTest();

		pageNavigation.draw(minecraft, poseStack, mouseX, mouseY, minecraft.getFrameTime());

		if (hovered != null) {
			List<Component> tooltip = hovered.getTooltip();
			TooltipRenderer.drawHoveringText(poseStack, tooltip, mouseX, mouseY);
		}
	}

	public IUserInputHandler getInputHandler() {
		return this.inputHandler;
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
