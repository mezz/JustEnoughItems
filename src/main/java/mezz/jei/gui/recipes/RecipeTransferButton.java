package mezz.jei.gui.recipes;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.elements.GuiIconButtonSmall;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.transfer.RecipeTransferErrorInternal;
import mezz.jei.transfer.RecipeTransferUtil;
import net.minecraft.util.text.TranslationTextComponent;

public class RecipeTransferButton extends GuiIconButtonSmall {
	private final RecipeLayout<?> recipeLayout;
	@Nullable
	private IRecipeTransferError recipeTransferError;
	@Nullable
	private IOnClickHandler onClickHandler;

	public RecipeTransferButton(int xPos, int yPos, int width, int height, IDrawable icon, RecipeLayout<?> recipeLayout) {
		super(xPos, yPos, width, height, icon, b -> {});
		this.recipeLayout = recipeLayout;
	}

	public void init(RecipeTransferManager recipeTransferManager, @Nullable Container container, PlayerEntity player) {
		if (container != null) {
			this.recipeTransferError = RecipeTransferUtil.getTransferRecipeError(recipeTransferManager, container, recipeLayout, player);
		} else {
			this.recipeTransferError = RecipeTransferErrorInternal.INSTANCE;
		}

		if (RecipeTransferUtil.allowsTransfer(recipeTransferError)) {
			this.field_230693_o_ = true;
			this.field_230694_p_ = true;
		} else {
			this.field_230693_o_ = false;
			IRecipeTransferError.Type type = this.recipeTransferError.getType();
			this.field_230694_p_ = (type == IRecipeTransferError.Type.USER_FACING);
		}
	}

	public void drawToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
		if (func_231047_b_(mouseX, mouseY)) {
			if (recipeTransferError == null) {
				TranslationTextComponent tooltipTransfer = new TranslationTextComponent("jei.tooltip.transfer");
				TooltipRenderer.drawHoveringText(tooltipTransfer, mouseX, mouseY, matrixStack);
			} else {
				recipeTransferError.showError(matrixStack, mouseX, mouseY, recipeLayout, recipeLayout.getPosX(), recipeLayout.getPosY());
			}
		}
	}

	@Override
	public boolean func_231047_b_(double mouseX, double mouseY) {
		return this.field_230694_p_ &&
			mouseX >= this.field_230690_l_ &&
			mouseY >= this.field_230691_m_ &&
			mouseX < this.field_230690_l_ + this.field_230688_j_ &&
			mouseY < this.field_230691_m_ + this.field_230689_k_;
	}

	public void setOnClickHandler(IOnClickHandler onClickHandler) {
		this.onClickHandler = onClickHandler;
	}

	@Override
	public void func_230982_a_(double mouseX, double mouseY) {
		if (onClickHandler != null) {
			onClickHandler.onClick(mouseX, mouseY);
		}
	}
}
