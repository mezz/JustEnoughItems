package mezz.jei.gui;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import mezz.jei.Internal;
import mezz.jei.JeiRuntime;
import mezz.jei.JustEnoughItems;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.GuiIngredientFast;
import mezz.jei.gui.ingredients.GuiIngredientFastList;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IPaged;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketJei;
import mezz.jei.util.GuiAreaHelper;
import mezz.jei.util.StackHelper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * An ingredient grid displays a rectangular area of clickable recipe ingredients.
 */
public abstract class IngredientGrid implements IShowsRecipeFocuses, IPaged {
	private static final int INGREDIENT_PADDING = 1;
	private static final int INGREDIENT_WIDTH = GuiItemStackGroup.getWidth(INGREDIENT_PADDING);
	private static final int INGREDIENT_HEIGHT = GuiItemStackGroup.getHeight(INGREDIENT_PADDING);

	private Set<Rectangle> guiAreas = Collections.emptySet();
	private Rectangle area = new Rectangle();
	protected final GuiIngredientFastList guiIngredientList;

	@Nullable
	private GuiIngredientFast hovered;

	public IngredientGrid(IIngredientRegistry ingredientRegistry) {
		this.guiIngredientList = new GuiIngredientFastList(ingredientRegistry);
	}

	public void updateBounds(Rectangle area) {
		final int columns = area.width / INGREDIENT_WIDTH;
		final int rows = area.height / INGREDIENT_HEIGHT;

		final int width = columns * INGREDIENT_WIDTH;
		final int height = rows * INGREDIENT_HEIGHT;
		final int x = area.x + (area.width - width) / 2;
		final int y = area.y + (area.height - height) / 2;

		this.area = new Rectangle(x, y, width, height);
		this.guiIngredientList.clear();

		this.guiAreas = GuiAreaHelper.getGuiAreas();
		for (int row = 0; row < rows; row++) {
			int y1 = y + (row * INGREDIENT_HEIGHT);
			for (int column = 0; column < columns; column++) {
				int x1 = x + (column * INGREDIENT_WIDTH);
				GuiIngredientFast guiIngredientFast = new GuiIngredientFast(x1, y1, INGREDIENT_PADDING);
				Rectangle stackArea = guiIngredientFast.getArea();
				final boolean blocked = GuiAreaHelper.intersects(this.guiAreas, stackArea);
				guiIngredientFast.setBlocked(blocked);
				this.guiIngredientList.add(guiIngredientFast);
			}
		}
	}

	public void updateLayout() {
		for (GuiIngredientFast guiIngredientFast : this.guiIngredientList.getAllGuiIngredients()) {
			Rectangle stackArea = guiIngredientFast.getArea();
			final boolean blocked = GuiAreaHelper.intersects(this.guiAreas, stackArea);
			guiIngredientFast.setBlocked(blocked);
		}
	}

	public Rectangle getArea() {
		return area;
	}

	public void draw(Minecraft minecraft, int mouseX, int mouseY) {
		GlStateManager.disableBlend();

		if (shouldShowDeleteItemTooltip(minecraft)) {
			hovered = guiIngredientList.render(minecraft, false, mouseX, mouseY);
		} else {
			boolean mouseOver = isMouseOver(mouseX, mouseY);
			hovered = guiIngredientList.render(minecraft, mouseOver, mouseX, mouseY);
		}

		JeiRuntime runtime = Internal.getRuntime();
		if (runtime != null) {
			Set<ItemStack> highlightedStacks = runtime.getItemListOverlay().getHighlightedStacks();
			if (!highlightedStacks.isEmpty()) {
				StackHelper helper = Internal.getHelpers().getStackHelper();
				for (GuiIngredientFast guiItemStack : guiIngredientList.getAllGuiIngredients()) {
					Object ingredient = guiItemStack.getIngredient();
					if (ingredient instanceof ItemStack) {
						if (helper.containsStack(highlightedStacks, (ItemStack) ingredient) != null) {
							guiItemStack.drawHighlight();
						}
					}
				}
			}
		}

		if (hovered != null) {
			hovered.drawHovered(minecraft);
		}

		GlStateManager.enableAlpha();
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		boolean mouseOver = isMouseOver(mouseX, mouseY);
		if (mouseOver && shouldShowDeleteItemTooltip(minecraft)) {
			String deleteItem = Translator.translateToLocal("jei.tooltip.delete.item");
			TooltipRenderer.drawHoveringText(minecraft, deleteItem, mouseX, mouseY);
		}

		if (hovered != null) {
			hovered.drawTooltip(minecraft, mouseX, mouseY);
		}
	}

	private static boolean shouldShowDeleteItemTooltip(Minecraft minecraft) {
		if (Config.isDeleteItemsInCheatModeActive()) {
			EntityPlayer player = minecraft.player;
			if (!player.inventory.getItemStack().isEmpty()) {
				JeiRuntime runtime = Internal.getRuntime();
				return runtime == null || !runtime.getRecipesGui().isOpen();
			}
		}
		return false;
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return area.contains(mouseX, mouseY) &&
				!GuiAreaHelper.isMouseOverGuiArea(this.guiAreas, mouseX, mouseY);
	}

	public boolean handleMouseClicked(int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			Minecraft minecraft = Minecraft.getMinecraft();

			JeiRuntime runtime = Internal.getRuntime();
			if (Config.isDeleteItemsInCheatModeActive() && (runtime == null || !runtime.getRecipesGui().isOpen())) {

				EntityPlayerSP player = minecraft.player;
				ItemStack itemStack = player.inventory.getItemStack();
				if (!itemStack.isEmpty()) {
					player.inventory.setItemStack(ItemStack.EMPTY);
					PacketJei packet = new PacketDeletePlayerItem(itemStack);
					JustEnoughItems.getProxy().sendPacketToServer(packet);
					return true;
				}
			}
		}
		return false;
	}

	@Nullable
	public ItemStack getStackUnderMouse() {
		if (hovered != null) {
			Object ingredient = hovered.getIngredient();
			if (ingredient instanceof ItemStack) {
				return (ItemStack) ingredient;
			}
		}
		return null;
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			ClickedIngredient<?> clicked = guiIngredientList.getIngredientUnderMouse(mouseX, mouseY);
			if (clicked != null) {
				clicked.setAllowsCheating();
			}
			return clicked;
		}
		return null;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return true;
	}

	public boolean updateGuiAreas() {
		final Set<Rectangle> guiAreas = GuiAreaHelper.getGuiAreas();
		if (!guiAreas.equals(this.guiAreas)) {
			this.guiAreas = guiAreas;
			return true;
		}
		return false;
	}

	public abstract int getPageCount();

	public abstract int getPageNum();

	public abstract ImmutableList<ItemStack> getVisibleStacks();
}
