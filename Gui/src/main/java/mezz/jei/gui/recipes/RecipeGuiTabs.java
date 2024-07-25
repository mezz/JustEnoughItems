package mezz.jei.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.input.IPaged;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import mezz.jei.gui.input.handlers.ProxyInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * The area drawn on top and bottom of the {@link RecipesGui} that show the recipe categories.
 */
public class RecipeGuiTabs implements IPaged {
	private static final int TAB_GUI_OVERLAP = 3;
	private static final int TAB_HORIZONTAL_INSET = 2;
	private static final int NAVIGATION_HEIGHT = 20;

	private final IRecipeGuiLogic recipeGuiLogic;
	private final List<RecipeGuiTab> tabs = new ArrayList<>();
	private final PageNavigation pageNavigation;
	private final IRecipeManager recipeManager;
	private final IGuiHelper guiHelper;
	private IUserInputHandler inputHandler;
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;

	private int pageCount = 1;
	private int pageNumber = 0;
	private int categoriesPerPage = 1;

	public RecipeGuiTabs(IRecipeGuiLogic recipeGuiLogic, IRecipeManager recipeManager, IGuiHelper guiHelper) {
		this.recipeGuiLogic = recipeGuiLogic;
		this.pageNavigation = new PageNavigation(this, true);
		this.recipeManager = recipeManager;
		this.guiHelper = guiHelper;
		this.inputHandler = this.pageNavigation.createInputHandler();
	}

	public void initLayout(ImmutableRect2i recipeGuiArea) {
		List<IRecipeCategory<?>> categories = this.recipeGuiLogic.getRecipeCategories();
		if (categories.isEmpty()) {
			return;
		}

		final ImmutableRect2i tabsArea = recipeGuiArea
			.keepTop(RecipeGuiTab.TAB_HEIGHT)
			// move up above the recipe area and overlap the recipe gui
			.moveUp(RecipeGuiTab.TAB_HEIGHT - TAB_GUI_OVERLAP)
			// inset to avoid the recipe gui corners
			.cropLeft(TAB_HORIZONTAL_INSET)
			.cropRight(TAB_HORIZONTAL_INSET);

		categoriesPerPage = Math.min(tabsArea.getWidth() / RecipeGuiTab.TAB_WIDTH, categories.size());
		final int tabsWidth = categoriesPerPage * RecipeGuiTab.TAB_WIDTH;

		this.area = tabsArea.keepLeft(tabsWidth);

		pageCount = MathUtil.divideCeil(categories.size(), categoriesPerPage);

		IRecipeCategory<?> currentCategory = recipeGuiLogic.getSelectedRecipeCategory();
		int categoryIndex = categories.indexOf(currentCategory);
		pageNumber = categoryIndex / categoriesPerPage;

		ImmutableRect2i navigationArea = tabsArea
			.keepTop(NAVIGATION_HEIGHT)
			.moveUp(2 + NAVIGATION_HEIGHT); // move up and add a little padding

		pageNavigation.updateBounds(navigationArea);

		updateLayout();
	}

	private void updateLayout() {
		tabs.clear();
		List<IUserInputHandler> inputHandlers = new ArrayList<>();

		List<IRecipeCategory<?>> categories = recipeGuiLogic.getRecipeCategories();

		int tabX = area.getX();

		final int startIndex = pageNumber * categoriesPerPage;
		for (int i = 0; i < categoriesPerPage; i++) {
			int index = i + startIndex;
			if (index >= categories.size()) {
				break;
			}
			IRecipeCategory<?> category = categories.get(index);
			RecipeGuiTab tab = new RecipeCategoryTab(
				recipeGuiLogic,
				category,
				tabX,
				area.getY(),
				recipeManager,
				guiHelper
			);
			this.tabs.add(tab);
			inputHandlers.add(tab);
			tabX += RecipeGuiTab.TAB_WIDTH;
		}

		inputHandlers.add(this.pageNavigation.createInputHandler());
		this.inputHandler = new CombinedInputHandler(inputHandlers);

		pageNavigation.updatePageNumber();
	}

	public void draw(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		IRecipeCategory<?> selectedCategory = recipeGuiLogic.getSelectedRecipeCategory();

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		RecipeGuiTab hovered = null;

		RenderSystem.disableDepthTest();
		{
			for (RecipeGuiTab tab : tabs) {
				boolean selected = tab.isSelected(selectedCategory);
				tab.draw(selected, guiGraphics, mouseX, mouseY);
				if (tab.isMouseOver(mouseX, mouseY)) {
					hovered = tab;
				}
			}
		}
		RenderSystem.enableDepthTest();

		pageNavigation.draw(minecraft, guiGraphics, mouseX, mouseY, partialTicks);

		if (hovered != null) {
			JeiTooltip tooltip = hovered.getTooltip();
			tooltip.draw(guiGraphics, mouseX, mouseY);
		}
	}

	public IUserInputHandler createInputHandler() {
		return new ProxyInputHandler(() -> inputHandler);
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
