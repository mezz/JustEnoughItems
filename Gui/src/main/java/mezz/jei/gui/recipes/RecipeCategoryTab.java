package mezz.jei.gui.recipes;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.Optional;

public class RecipeCategoryTab extends RecipeGuiTab {
	private final IRecipeGuiLogic logic;
	private final IRecipeCategory<?> category;
	private final IRecipeManager recipeManager;
	private final IGuiHelper guiHelper;

	public RecipeCategoryTab(
		IRecipeGuiLogic logic,
		IRecipeCategory<?> category,
		int x,
		int y,
		IRecipeManager recipeManager,
		IGuiHelper guiHelper
	) {
		super(x, y);
		this.logic = logic;
		this.category = category;
		this.recipeManager = recipeManager;
		this.guiHelper = guiHelper;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		if (!isMouseOver(input.getMouseX(), input.getMouseY())) {
			return Optional.empty();
		}
		if (input.is(keyBindings.getLeftClick())) {
			if (!input.isSimulate()) {
				logic.setRecipeCategory(category);
				SoundManager soundHandler = Minecraft.getInstance().getSoundManager();
				soundHandler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			}
			return Optional.of(this);
		}
		return Optional.empty();
	}

	@Override
	public void draw(boolean selected, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		super.draw(selected, guiGraphics, mouseX, mouseY);

		IDrawable icon = RecipeCategoryIconUtil.create(category, recipeManager, guiHelper);
		int iconX = area.x() + (TAB_WIDTH - icon.getWidth()) / 2;
		int iconY = area.y() + (TAB_HEIGHT - icon.getHeight()) / 2;
		icon.draw(guiGraphics, iconX, iconY);
	}

	@Override
	public boolean isSelected(IRecipeCategory<?> selectedCategory) {
		ResourceLocation categoryUid = category.getRecipeType().getUid();
		ResourceLocation selectedCategoryUid = selectedCategory.getRecipeType().getUid();
		return categoryUid.equals(selectedCategoryUid);
	}

	@Override
	public JeiTooltip getTooltip() {
		JeiTooltip tooltip = new JeiTooltip();
		Component title = category.getTitle();
		//noinspection ConstantConditions
		if (title != null) {
			tooltip.add(title);
		}

		ResourceLocation uid = category.getRecipeType().getUid();
		String modId = uid.getNamespace();
		IModIdHelper modIdHelper = Internal.getJeiRuntime().getJeiHelpers().getModIdHelper();
		if (modIdHelper.isDisplayingModNameEnabled()) {
			String modName = modIdHelper.getFormattedModNameForModId(modId);
			tooltip.add(Component.literal(modName));
		}
		return tooltip;
	}
}
