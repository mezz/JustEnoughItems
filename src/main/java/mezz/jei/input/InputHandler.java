package mezz.jei.input;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.client.FMLClientHandler;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import mezz.jei.config.Config;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.RecipesGui;
import mezz.jei.util.Commands;
import mezz.jei.util.MouseHelper;
import mezz.jei.util.Permissions;

public class InputHandler {

	private final RecipesGui recipesGui;
	private final ItemListOverlay itemListOverlay;
	private final MouseHelper mouseHelper;

	private final List<IMouseHandler> mouseHandlers = new ArrayList<>();
	private final List<IKeyable> keyables = new ArrayList<>();
	private final List<IShowsItemStacks> showsItemStacks = new ArrayList<>();

	private boolean clickHandled = false;

	public InputHandler(RecipesGui recipesGui, ItemListOverlay itemListOverlay, GuiContainer guiContainer) {
		this.recipesGui = recipesGui;
		this.itemListOverlay = itemListOverlay;

		this.mouseHelper = new MouseHelper();

		List<ICloseable> objects = new ArrayList<>();
		objects.add(recipesGui);
		objects.add(itemListOverlay);
		objects.add(new GuiContainerWrapper(guiContainer, recipesGui));

		for (Object gui : objects) {
			if (gui instanceof IMouseHandler) {
				mouseHandlers.add((IMouseHandler) gui);
			}
			if (gui instanceof IKeyable) {
				keyables.add((IKeyable) gui);
			}
			if (gui instanceof IShowsItemStacks) {
				showsItemStacks.add((IShowsItemStacks) gui);
			}
		}
	}

	public boolean handleMouseEvent(int mouseX, int mouseY) {
		boolean cancelEvent = false;
		if (Mouse.getEventButton() > -1) {
			if (Mouse.getEventButtonState()) {
				if (!clickHandled) {
					cancelEvent = handleMouseClick(Mouse.getEventButton(), mouseX, mouseY);
					clickHandled = true;
				}
			} else {
				clickHandled = false;
			}
		} else if (Mouse.getEventDWheel() != 0) {
			cancelEvent = handleMouseScroll(Mouse.getEventDWheel(), mouseX, mouseY);
		}
		return cancelEvent;
	}

	private boolean handleMouseScroll(int dWheel, int mouseX, int mouseY) {
		for (IMouseHandler scrollable : mouseHandlers) {
			if (scrollable.handleMouseScrolled(mouseX, mouseY, dWheel)) {
				return true;
			}
		}
		return false;
	}

	private boolean handleMouseClick(int mouseButton, int mouseX, int mouseY) {
		ItemStack itemStack = getStackUnderMouseForClick(mouseX, mouseY);
		if (itemStack != null) {
			if (handleMouseClickedItemStack(mouseButton, itemStack)) {
				return true;
			}
		}

		for (IMouseHandler clickable : mouseHandlers) {
			if (clickable.handleMouseClicked(mouseX, mouseY, mouseButton)) {
				return true;
			}
		}

		return recipesGui.isOpen();
	}

	@Nullable
	private ItemStack getStackUnderMouseForClick(int mouseX, int mouseY) {
		for (IShowsItemStacks gui : showsItemStacks) {
			if (!(gui instanceof IMouseHandler)) {
				continue;
			}

			ItemStack itemStack = gui.getStackUnderMouse(mouseX, mouseY);
			if (itemStack != null) {
				return itemStack;
			}
		}
		return null;
	}

	@Nullable
	private ItemStack getStackUnderMouseForKey(int mouseX, int mouseY) {
		for (IShowsItemStacks gui : showsItemStacks) {
			if (!(gui instanceof IKeyable)) {
				continue;
			}

			ItemStack itemStack = gui.getStackUnderMouse(mouseX, mouseY);
			if (itemStack != null) {
				return itemStack;
			}
		}
		return null;
	}

	private boolean handleMouseClickedItemStack(int mouseButton, @Nonnull ItemStack itemStack) {
		EntityPlayerSP player = FMLClientHandler.instance().getClientPlayerEntity();
		if (Config.cheatItemsEnabled && Permissions.canPlayerSpawnItems(player)) {
			if (mouseButton == 0) {
				Commands.giveFullStack(itemStack);
				return true;
			} else if (mouseButton == 1) {
				Commands.giveOneFromStack(itemStack);
				return true;
			}
		} else {
			if (mouseButton == 0) {
				recipesGui.showRecipes(itemStack);
				return true;
			} else if (mouseButton == 1) {
				recipesGui.showUses(itemStack);
				return true;
			}
		}
		return false;
	}

	public boolean handleKeyEvent() {
		boolean cancelEvent = false;
		if (Keyboard.getEventKeyState()) {
			int eventKey = Keyboard.getEventKey();
			cancelEvent = handleKeyDown(eventKey);
		}
		return cancelEvent;
	}

	private boolean handleKeyDown(int eventKey) {
		for (IKeyable keyable : keyables) {
			if (keyable.isOpen() && keyable.hasKeyboardFocus()) {
				if (isInventoryCloseKey(eventKey)) {
					keyable.setKeyboardFocus(false);
					return true;
				} else if (keyable.onKeyPressed(eventKey)) {
					return true;
				}
			}
		}

		if (isInventoryCloseKey(eventKey) || isInventoryToggleKey(eventKey)) {
			if (recipesGui.isOpen()) {
				recipesGui.close();
				return true;
			} else if (itemListOverlay.isOpen()) {
				itemListOverlay.close();
				return false;
			}
		}

		if (eventKey == KeyBindings.showRecipe.getKeyCode()) {
			ItemStack itemStack = getStackUnderMouseForKey(mouseHelper.getX(), mouseHelper.getY());
			if (itemStack != null) {
				recipesGui.showRecipes(itemStack);
				return true;
			}
		} else if (eventKey == KeyBindings.showUses.getKeyCode()) {
			ItemStack itemStack = getStackUnderMouseForKey(mouseHelper.getX(), mouseHelper.getY());
			if (itemStack != null) {
				recipesGui.showUses(itemStack);
				return true;
			}
		} else if (eventKey == KeyBindings.toggleOverlay.getKeyCode()) {
			itemListOverlay.toggleEnabled();
			return false;
		}

		for (IKeyable keyable : keyables) {
			if (keyable.isOpen() && keyable.onKeyPressed(eventKey)) {
				return true;
			}
		}

		return false;
	}

	private boolean isInventoryToggleKey(int keyCode) {
		return keyCode == Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode();
	}

	private boolean isInventoryCloseKey(int keyCode) {
		return keyCode == Keyboard.KEY_ESCAPE;
	}

}
