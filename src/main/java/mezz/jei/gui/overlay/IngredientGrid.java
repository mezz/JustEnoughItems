package mezz.jei.gui.overlay;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import mezz.jei.Internal;
import mezz.jei.JustEnoughItems;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IPaged;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketJei;
import mezz.jei.render.GuiIngredientFast;
import mezz.jei.render.GuiIngredientFastList;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.startup.StackHelper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

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

	private static boolean intersects(Collection<Rectangle> areas, Rectangle comparisonArea) {
		for (Rectangle area : areas) {
			if (area.intersects(comparisonArea)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isMouseOverGuiArea(Collection<Rectangle> guiAreas, int mouseX, int mouseY) {
		for (Rectangle guiArea : guiAreas) {
			if (guiArea.contains(mouseX, mouseY)) {
				return true;
			}
		}
		return false;
	}

	private static Set<Rectangle> getGuiAreas() {
		final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		if (currentScreen instanceof GuiContainer) {
			final GuiContainer guiContainer = (GuiContainer) currentScreen;
			final JeiRuntime jeiRuntime = Internal.getRuntime();
			if (jeiRuntime != null) {
				final Set<Rectangle> allGuiExtraAreas = new HashSet<Rectangle>();
				final List<IAdvancedGuiHandler<GuiContainer>> activeAdvancedGuiHandlers = jeiRuntime.getActiveAdvancedGuiHandlers(guiContainer);
				for (IAdvancedGuiHandler<GuiContainer> advancedGuiHandler : activeAdvancedGuiHandlers) {
					final List<Rectangle> guiExtraAreas = advancedGuiHandler.getGuiExtraAreas(guiContainer);
					if (guiExtraAreas != null) {
						allGuiExtraAreas.addAll(guiExtraAreas);
					}
				}
				return allGuiExtraAreas;
			}
		}
		return Collections.emptySet();
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

		this.guiAreas = getGuiAreas();
		for (int row = 0; row < rows; row++) {
			int y1 = y + (row * INGREDIENT_HEIGHT);
			for (int column = 0; column < columns; column++) {
				int x1 = x + (column * INGREDIENT_WIDTH);
				GuiIngredientFast guiIngredientFast = new GuiIngredientFast(x1, y1, INGREDIENT_PADDING);
				Rectangle stackArea = guiIngredientFast.getArea();
				final boolean blocked = intersects(this.guiAreas, stackArea);
				guiIngredientFast.setBlocked(blocked);
				this.guiIngredientList.add(guiIngredientFast);
			}
		}
	}

	public void updateLayout() {
		for (GuiIngredientFast guiIngredientFast : this.guiIngredientList.getAllGuiIngredients()) {
			Rectangle stackArea = guiIngredientFast.getArea();
			final boolean blocked = intersects(this.guiAreas, stackArea);
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

		drawHighlightedStacks();

		if (hovered != null) {
			hovered.drawHovered(minecraft);
		}

		GlStateManager.enableAlpha();
	}

	private void drawHighlightedStacks() {
		JeiRuntime runtime = Internal.getRuntime();
		if (runtime == null) {
			return;
		}

		NonNullList<ItemStack> highlightedStacks = runtime.getItemListOverlay().getHighlightedStacks();
		if (highlightedStacks.isEmpty()) {
			return;
		}

		StackHelper helper = Internal.getHelpers().getStackHelper();
		for (GuiIngredientFast guiItemStack : guiIngredientList.getAllGuiIngredients()) {
			IIngredientListElement element = guiItemStack.getElement();
			if (element != null) {
				Object ingredient = element.getIngredient();
				if (ingredient instanceof ItemStack) {
					if (helper.containsStack(highlightedStacks, (ItemStack) ingredient) != null) {
						guiItemStack.drawHighlight();
					}
				}
			}
		}
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
				!isMouseOverGuiArea(this.guiAreas, mouseX, mouseY);
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
			IIngredientListElement element = hovered.getElement();
			if (element != null) {
				Object ingredient = element.getIngredient();
				if (ingredient instanceof ItemStack) {
					return (ItemStack) ingredient;
				}
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
		final Set<Rectangle> guiAreas = getGuiAreas();
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
