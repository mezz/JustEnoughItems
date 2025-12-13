package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.transfer.RecipeTransferErrorInternal;
import mezz.jei.common.transfer.RecipeTransferUtil;
import mezz.jei.gui.elements.IIconButtonDescriptor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class RecipeTransferButton implements IIconButtonDescriptor {
	private final IDrawable icon;
	private final IRecipeLayoutDrawable<?> recipeLayout;
	private final RecipesGui recipesGui;
	private @Nullable IRecipeTransferError recipeTransferError;
	private boolean active = true;
	private boolean visible = true;

	public RecipeTransferButton(IRecipeLayoutDrawable<?> recipeLayout, RecipesGui recipesGui) {
		Textures textures = Internal.getTextures();
		this.icon = textures.getRecipeTransfer();
		this.recipeLayout = recipeLayout;
		this.recipesGui = recipesGui;

		tick();
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void tick() {
		Player player = Minecraft.getInstance().player;
		AbstractContainerMenu parentContainer = recipesGui.getParentContainerMenu();
		if (parentContainer != null && player != null) {
			IRecipeTransferManager recipeTransferManager = Internal.getJeiRuntime().getRecipeTransferManager();
			this.recipeTransferError = RecipeTransferUtil.getTransferRecipeError(recipeTransferManager, parentContainer, recipeLayout, player)
				.orElse(null);
		} else {
			this.recipeTransferError = RecipeTransferErrorInternal.INSTANCE;
		}

		if (recipeTransferError == null ||
			recipeTransferError.getType().allowsTransfer) {
			this.active = true;
			this.visible = true;
		} else {
			this.active = false;
			IRecipeTransferError.Type type = this.recipeTransferError.getType();
			this.visible = (type == IRecipeTransferError.Type.USER_FACING);
		}
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public boolean onPress(IJeiUserInput input) {
		if (!input.isSimulate()) {
			IRecipeTransferManager recipeTransferManager = Internal.getJeiRuntime().getRecipeTransferManager();
			boolean maxTransfer = Screen.hasShiftDown();
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			AbstractContainerMenu parentContainer = recipesGui.getParentContainerMenu();
			if (parentContainer != null && player != null && RecipeTransferUtil.transferRecipe(recipeTransferManager, parentContainer, recipeLayout, player, maxTransfer)) {
				recipesGui.onClose();
			}
		}
		return true;
	}

	@Override
	public void getTooltips(ITooltipBuilder tooltip) {
		if (recipeTransferError == null) {
			Component tooltipTransfer = Component.translatable("jei.tooltip.transfer");
			tooltip.add(tooltipTransfer);
		} else {
			recipeTransferError.getTooltip(tooltip);
		}
	}

	@Override
	public void drawExtras(GuiGraphics guiGraphics, Rect2i buttonArea, int mouseX, int mouseY, float partialTicks) {
		IRecipeTransferError recipeTransferError = this.recipeTransferError;
		if (recipeTransferError != null) {
			if (recipeTransferError.getType() == IRecipeTransferError.Type.COSMETIC) {
				guiGraphics.fill(
					RenderType.guiOverlay(),
					buttonArea.getX(),
					buttonArea.getY(),
					buttonArea.getX() + buttonArea.getWidth(),
					buttonArea.getY() + buttonArea.getHeight(),
					recipeTransferError.getButtonHighlightColor()
				);
			}
			if (buttonArea.contains(mouseX, mouseY)) {
				IRecipeSlotsView recipeSlotsView = recipeLayout.getRecipeSlotsView();
				Rect2i recipeRect = recipeLayout.getRect();
				recipeTransferError.showError(guiGraphics, mouseX, mouseY, recipeSlotsView, recipeRect.getX(), recipeRect.getY());
			}
		}
	}

	public int getMissingCountHint() {
		if (recipeTransferError == null) {
			return 0;
		}
		return recipeTransferError.getMissingCountHint();
	}
}
