package mezz.jei.gui.overlay;

import javax.annotation.Nullable;
import java.util.Collection;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import mezz.jei.Internal;
import mezz.jei.config.ClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.ingredients.GuiIngredientProperties;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.MouseUtil;
import mezz.jei.network.Network;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketJei;
import mezz.jei.render.IngredientListBatchRenderer;
import mezz.jei.render.IngredientListElementRenderer;
import mezz.jei.render.IngredientListSlot;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.GiveMode;
import mezz.jei.util.MathUtil;
import mezz.jei.util.Translator;

/**
 * An ingredient grid displays a rectangular area of clickable recipe ingredients.
 */
public class IngredientGrid implements IShowsRecipeFocuses {
	private static final int INGREDIENT_PADDING = 1;
	public static final int INGREDIENT_WIDTH = GuiIngredientProperties.getWidth(INGREDIENT_PADDING);
	public static final int INGREDIENT_HEIGHT = GuiIngredientProperties.getHeight(INGREDIENT_PADDING);
	private final GridAlignment alignment;

	private Rectangle2d area = new Rectangle2d(0, 0, 0, 0);
	protected final IngredientListBatchRenderer guiIngredientSlots;
	private final IIngredientFilterConfig ingredientFilterConfig;
	private final IWorldConfig worldConfig;

	public IngredientGrid(GridAlignment alignment, IEditModeConfig editModeConfig, IIngredientFilterConfig ingredientFilterConfig, IWorldConfig worldConfig) {
		this.alignment = alignment;
		this.guiIngredientSlots = new IngredientListBatchRenderer(editModeConfig, worldConfig);
		this.ingredientFilterConfig = ingredientFilterConfig;
		this.worldConfig = worldConfig;
	}

	public int size() {
		return this.guiIngredientSlots.size();
	}

	public boolean updateBounds(Rectangle2d availableArea, int minWidth, Collection<Rectangle2d> exclusionAreas) {
		final int columns = Math.min(availableArea.getWidth() / INGREDIENT_WIDTH, ClientConfig.getInstance().getMaxColumns());
		final int rows = availableArea.getHeight() / INGREDIENT_HEIGHT;

		final int ingredientsWidth = columns * INGREDIENT_WIDTH;
		final int width = Math.max(ingredientsWidth, minWidth);
		final int height = rows * INGREDIENT_HEIGHT;
		final int x;
		if (alignment == GridAlignment.LEFT) {
			x = availableArea.getX() + (availableArea.getWidth() - width);
		} else {
			x = availableArea.getX();
		}
		final int y = availableArea.getY() + (availableArea.getHeight() - height) / 2;
		final int xOffset = x + Math.max(0, (width - ingredientsWidth) / 2);

		this.area = new Rectangle2d(x, y, width, height);
		this.guiIngredientSlots.clear();

		if (rows == 0 || columns < ClientConfig.smallestNumColumns) {
			return false;
		}

		for (int row = 0; row < rows; row++) {
			int y1 = y + (row * INGREDIENT_HEIGHT);
			for (int column = 0; column < columns; column++) {
				int x1 = xOffset + (column * INGREDIENT_WIDTH);
				IngredientListSlot ingredientListSlot = new IngredientListSlot(x1, y1, INGREDIENT_PADDING);
				Rectangle2d stackArea = ingredientListSlot.getArea();
				final boolean blocked = MathUtil.intersects(exclusionAreas, stackArea);
				ingredientListSlot.setBlocked(blocked);
				this.guiIngredientSlots.add(ingredientListSlot);
			}
		}
		return true;
	}

	public Rectangle2d getArea() {
		return area;
	}

	public void draw(Minecraft minecraft, int mouseX, int mouseY) {
		RenderSystem.disableBlend();

		guiIngredientSlots.render(minecraft);

		if (!shouldDeleteItemOnClick(minecraft, mouseX, mouseY) && isMouseOver(mouseX, mouseY)) {
			IngredientListElementRenderer hovered = guiIngredientSlots.getHovered(mouseX, mouseY);
			if (hovered != null) {
				hovered.drawHighlight();
			}
		}

		RenderSystem.enableAlphaTest();
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			if (shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
				String deleteItem = Translator.translateToLocal("jei.tooltip.delete.item");
				TooltipRenderer.drawHoveringText(deleteItem, mouseX, mouseY);
			} else {
				IngredientListElementRenderer hovered = guiIngredientSlots.getHovered(mouseX, mouseY);
				if (hovered != null) {
					hovered.drawTooltip(mouseX, mouseY, ingredientFilterConfig, worldConfig);
				}
			}
		}
	}

	private boolean shouldDeleteItemOnClick(Minecraft minecraft, double mouseX, double mouseY) {
		if (worldConfig.isDeleteItemsInCheatModeActive()) {
			PlayerEntity player = minecraft.player;
			if (player != null) {
				ItemStack itemStack = player.inventory.getItemStack();
				if (!itemStack.isEmpty()) {
					// TODO find a better way to handle this without using Internal
					JeiRuntime runtime = Internal.getRuntime();
					if (runtime == null || !runtime.getRecipesGui().isOpen()) {
						GiveMode giveMode = ClientConfig.getInstance().getGiveMode();
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

	public boolean isMouseOver(double mouseX, double mouseY) {
		return MathUtil.contains(area, mouseX, mouseY);
	}

	public boolean handleMouseClicked(double mouseX, double mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			Minecraft minecraft = Minecraft.getInstance();
			if (shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
				ClientPlayerEntity player = minecraft.player;
				if (player != null) {
					ItemStack itemStack = player.inventory.getItemStack();
					if (!itemStack.isEmpty()) {
						player.inventory.setItemStack(ItemStack.EMPTY);
						PacketJei packet = new PacketDeletePlayerItem(itemStack);
						Network.sendPacketToServer(packet);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Nullable
	public IIngredientListElement getElementUnderMouse() {
		IngredientListElementRenderer hovered = guiIngredientSlots.getHovered(MouseUtil.getX(), MouseUtil.getY());
		if (hovered != null) {
			return hovered.getElement();
		}
		return null;
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(double mouseX, double mouseY) {
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
