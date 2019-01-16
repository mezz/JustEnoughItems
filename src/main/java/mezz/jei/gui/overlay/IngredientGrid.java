package mezz.jei.gui.overlay;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Collection;

import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import mezz.jei.Internal;
import mezz.jei.JustEnoughItems;
import mezz.jei.config.Config;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.MouseHelper;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketJei;
import mezz.jei.render.IngredientListBatchRenderer;
import mezz.jei.render.IngredientListSlot;
import mezz.jei.render.IngredientRenderer;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.GiveMode;
import mezz.jei.util.MathUtil;
import mezz.jei.util.Translator;

/**
 * An ingredient grid displays a rectangular area of clickable recipe ingredients.
 */
public class IngredientGrid implements IShowsRecipeFocuses {
	private static final int INGREDIENT_PADDING = 1;
	public static final int INGREDIENT_WIDTH = GuiItemStackGroup.getWidth(INGREDIENT_PADDING);
	public static final int INGREDIENT_HEIGHT = GuiItemStackGroup.getHeight(INGREDIENT_PADDING);
	private final GridAlignment alignment;

	private Rectangle area = new Rectangle();
	protected final IngredientListBatchRenderer guiIngredientSlots;

	public IngredientGrid(GridAlignment alignment) {
		this.alignment = alignment;
		this.guiIngredientSlots = new IngredientListBatchRenderer();
	}

	public int size() {
		return this.guiIngredientSlots.size();
	}

	public boolean updateBounds(Rectangle availableArea, int minWidth, Collection<Rectangle> exclusionAreas) {
		final int columns = Math.min(availableArea.width / INGREDIENT_WIDTH, Config.getMaxColumns());
		final int rows = availableArea.height / INGREDIENT_HEIGHT;

		final int ingredientsWidth = columns * INGREDIENT_WIDTH;
		final int width = Math.max(ingredientsWidth, minWidth);
		final int height = rows * INGREDIENT_HEIGHT;
		final int x;
		if (alignment == GridAlignment.LEFT) {
			x = availableArea.x + (availableArea.width - width);
		} else {
			x = availableArea.x;
		}
		final int y = availableArea.y + (availableArea.height - height) / 2;
		final int xOffset = x + Math.max(0, (width - ingredientsWidth) / 2);

		this.area = new Rectangle(x, y, width, height);
		this.guiIngredientSlots.clear();

		if (rows == 0 || columns < Config.smallestNumColumns) {
			return false;
		}

		for (int row = 0; row < rows; row++) {
			int y1 = y + (row * INGREDIENT_HEIGHT);
			for (int column = 0; column < columns; column++) {
				int x1 = xOffset + (column * INGREDIENT_WIDTH);
				IngredientListSlot ingredientListSlot = new IngredientListSlot(x1, y1, INGREDIENT_PADDING);
				Rectangle stackArea = ingredientListSlot.getArea();
				final boolean blocked = MathUtil.intersects(exclusionAreas, stackArea);
				ingredientListSlot.setBlocked(blocked);
				this.guiIngredientSlots.add(ingredientListSlot);
			}
		}
		return true;
	}

	public Rectangle getArea() {
		return area;
	}

	public void draw(Minecraft minecraft, int mouseX, int mouseY) {
		GlStateManager.disableBlend();

		guiIngredientSlots.render(minecraft);

		if (!shouldDeleteItemOnClick(minecraft, mouseX, mouseY) && isMouseOver(mouseX, mouseY)) {
			IngredientRenderer hovered = guiIngredientSlots.getHovered(mouseX, mouseY);
			if (hovered != null) {
				hovered.drawHighlight();
			}
		}

		GlStateManager.enableAlpha();
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			if (shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
				String deleteItem = Translator.translateToLocal("jei.tooltip.delete.item");
				TooltipRenderer.drawHoveringText(minecraft, deleteItem, mouseX, mouseY);
			} else {
				IngredientRenderer hovered = guiIngredientSlots.getHovered(mouseX, mouseY);
				if (hovered != null) {
					hovered.drawTooltip(minecraft, mouseX, mouseY);
				}
			}
		}
	}

	private boolean shouldDeleteItemOnClick(Minecraft minecraft, int mouseX, int mouseY) {
		if (Config.isDeleteItemsInCheatModeActive()) {
			EntityPlayer player = minecraft.player;
			if (player != null) {
				ItemStack itemStack = player.inventory.getItemStack();
				if (!itemStack.isEmpty()) {
					JeiRuntime runtime = Internal.getRuntime();
					if (runtime == null || !runtime.getRecipesGui().isOpen()) {
						GiveMode giveMode = Config.getGiveMode();
						if (giveMode == GiveMode.MOUSE_PICKUP) {
							IClickedIngredient<?> ingredientUnderMouse = getIngredientUnderMouse(mouseX, mouseY);
							if (ingredientUnderMouse != null && ingredientUnderMouse.getValue() instanceof ItemStack) {
								ItemStack value = (ItemStack) ingredientUnderMouse.getValue();
								if (ItemHandlerHelper.canItemStacksStack(itemStack, value)) {
									return false;
								}
							}
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return area.contains(mouseX, mouseY);
	}

	public boolean handleMouseClicked(int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			Minecraft minecraft = Minecraft.getMinecraft();
			if (shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
				EntityPlayerSP player = minecraft.player;
				if (player != null) {
					ItemStack itemStack = player.inventory.getItemStack();
					if (!itemStack.isEmpty()) {
						player.inventory.setItemStack(ItemStack.EMPTY);
						PacketJei packet = new PacketDeletePlayerItem(itemStack);
						JustEnoughItems.getProxy().sendPacketToServer(packet);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Nullable
	public IIngredientListElement getElementUnderMouse() {
		IngredientRenderer hovered = guiIngredientSlots.getHovered(MouseHelper.getX(), MouseHelper.getY());
		if (hovered != null) {
			return hovered.getElement();
		}
		return null;
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			ClickedIngredient<?> clicked = guiIngredientSlots.getIngredientUnderMouse(mouseX, mouseY);
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
}
