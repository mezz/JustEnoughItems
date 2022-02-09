package mezz.jei.gui.recipes;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.gui.ingredients.RecipeSlots;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.elements.GuiIconButtonSmall;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.transfer.RecipeTransferErrorInternal;
import mezz.jei.transfer.RecipeTransferUtil;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class RecipeTransferButton extends GuiIconButtonSmall {
	private static final int RECIPE_BUTTON_SIZE = 13;

	private final RecipeLayout<?> recipeLayout;
	@Nullable
	private IRecipeTransferError recipeTransferError;
	@Nullable
	private IOnClickHandler onClickHandler;

	public RecipeTransferButton(int xPos, int yPos, IDrawable icon, RecipeLayout<?> recipeLayout) {
		super(xPos, yPos, RECIPE_BUTTON_SIZE, RECIPE_BUTTON_SIZE, icon, b -> {
		});
		this.recipeLayout = recipeLayout;
	}

	public void init(RecipeTransferManager recipeTransferManager, @Nullable AbstractContainerMenu container, Player player) {
		if (container != null) {
			this.recipeTransferError = RecipeTransferUtil.getTransferRecipeError(recipeTransferManager, container, recipeLayout, player);
		} else {
			this.recipeTransferError = RecipeTransferErrorInternal.INSTANCE;
		}

		if (RecipeTransferUtil.allowsTransfer(recipeTransferError)) {
			this.active = true;
			this.visible = true;
		} else {
			this.active = false;
			IRecipeTransferError.Type type = this.recipeTransferError.getType();
			this.visible = (type == IRecipeTransferError.Type.USER_FACING);
		}
	}

	@SuppressWarnings("removal")
	public void drawToolTip(PoseStack poseStack, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			if (recipeTransferError == null) {
				TranslatableComponent tooltipTransfer = new TranslatableComponent("jei.tooltip.transfer");
				TooltipRenderer.drawHoveringText(poseStack, List.of(tooltipTransfer), mouseX, mouseY);
			} else {
				RecipeSlots recipeSlots = recipeLayout.getRecipeSlots();
				IRecipeSlotsView recipeSlotsView = recipeSlots.getView();
				recipeTransferError.showError(poseStack, mouseX, mouseY, recipeSlotsView, recipeLayout.getPosX(), recipeLayout.getPosY());
				recipeTransferError.showError(poseStack, mouseX, mouseY, recipeLayout.getLegacyAdapter(), recipeLayout.getPosX(), recipeLayout.getPosY());
			}
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.visible &&
			mouseX >= this.x &&
			mouseY >= this.y &&
			mouseX < this.x + this.width &&
			mouseY < this.y + this.height;
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		super.render(poseStack, mouseX, mouseY, partialTicks);
		if (this.visible && this.recipeTransferError != null && this.recipeTransferError.getType() == IRecipeTransferError.Type.COSMETIC) {
			fill(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0x80FFA500);
		}
	}

	public void setOnClickHandler(IOnClickHandler onClickHandler) {
		this.onClickHandler = onClickHandler;
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		if (!isMouseOver(mouseX, mouseY)) {
			return;
		}
		if (onClickHandler != null) {
			onClickHandler.onClick(mouseX, mouseY);
		}
	}
}
