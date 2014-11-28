package mezz.jei.input;

import cpw.mods.fml.client.FMLClientHandler;
import mezz.jei.config.Config;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.RecipesGui;
import mezz.jei.util.Commands;
import mezz.jei.util.MouseHelper;
import mezz.jei.util.Permissions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class InputHandler {

	private final RecipesGui recipesGui;
	private final ItemListOverlay itemListOverlay;
	private final MouseHelper mouseHelper;

	private final List<IClickable> clickables = new ArrayList<IClickable>();
	private final List<IKeyable> keyables = new ArrayList<IKeyable>();
	private final List<IShowsItemStacks> showsItemStacks = new ArrayList<IShowsItemStacks>();

	private boolean clickHandled = false;
	private int keyHandled = -1;

	public InputHandler(Minecraft minecraft, RecipesGui recipesGui, ItemListOverlay itemListOverlay, GuiContainer guiContainer) {
		this.recipesGui = recipesGui;
		this.itemListOverlay = itemListOverlay;

		this.mouseHelper = new MouseHelper(minecraft);

		List<ICloseable> objects = new ArrayList<ICloseable>();
		objects.add(recipesGui);
		objects.add(itemListOverlay);
		objects.add(new GuiContainerWrapper(guiContainer, recipesGui));

		for (Object gui : objects) {
			if (gui instanceof IClickable) {
				clickables.add((IClickable) gui);
			}
			if (gui instanceof IKeyable) {
				keyables.add((IKeyable) gui);
			}
			if (gui instanceof IShowsItemStacks) {
				showsItemStacks.add((IShowsItemStacks) gui);
			}
		}
	}

	public void handleMouseEvent(Minecraft minecraft, int mouseX, int mouseY) {
		if (Mouse.getEventButton() > -1) {
			if (Mouse.getEventButtonState()) {
				if (!clickHandled) {
					handleMouseClick(minecraft, Mouse.getEventButton(), mouseX, mouseY);
					clickHandled = true;
				}
			} else {
				clickHandled = false;
			}
		}
	}

	private void handleMouseClick(Minecraft minecraft, int mouseButton, int mouseX, int mouseY) {

		ItemStack itemStack = getStackUnderMouseForClick(mouseX, mouseY);
		if (itemStack != null) {
			handleMouseClickedItemStack(mouseButton, itemStack);
			return;
		}

		for (IClickable clickable : clickables) {
			clickable.handleMouseClicked(minecraft, mouseX, mouseY, mouseButton);
		}
	}

	@Nullable
	private ItemStack getStackUnderMouseForClick(int mouseX, int mouseY) {
		for (IShowsItemStacks gui : showsItemStacks) {
			if (!(gui instanceof IClickable))
				continue;

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
			if (!(gui instanceof IKeyable))
				continue;

			ItemStack itemStack = gui.getStackUnderMouse(mouseX, mouseY);
			if (itemStack != null) {
				return itemStack;
			}
		}
		return null;
	}

	private void handleMouseClickedItemStack(int mouseButton, @Nonnull ItemStack itemStack) {
		EntityClientPlayerMP player = FMLClientHandler.instance().getClientPlayerEntity();
		if (Config.cheatItemsEnabled && Permissions.canPlayerSpawnItems(player) && player.inventory.getFirstEmptyStack() != -1) {
			if (mouseButton == 0) {
				Commands.giveFullStack(itemStack);
			} else if (mouseButton == 1) {
				Commands.giveOneFromStack(itemStack);
			}
		} else {
			if (mouseButton == 0) {
				recipesGui.showRecipes(itemStack);
			} else if (mouseButton == 1) {
				recipesGui.showUses(itemStack);
			}
		}
	}

	public void handleKeyEvent() {
		if (Keyboard.getEventKeyState()) {
			int eventKey = Keyboard.getEventKey();
			if (keyHandled != eventKey) {
				handleKeyDown(eventKey);
				keyHandled = eventKey;
			}
		} else {
			keyHandled = -1;
		}
	}

	private void handleKeyDown(int eventKey) {
		for (IKeyable keyable : keyables) {
			if (keyable.isOpen() && keyable.hasKeyboardFocus()) {
				if (keyable.onKeyPressed(eventKey)) {
					return;
				} else if (isInventoryCloseKey(eventKey)) {
					keyable.setKeyboardFocus(false);
					return;
				}
			}
		}

		if (isInventoryCloseKey(eventKey) || isInventoryToggleKey(eventKey)) {
			if (recipesGui.isOpen()) {
				recipesGui.close();
				return;
			} else if (itemListOverlay.isOpen()) {
				itemListOverlay.close();
				return;
			} else if (isInventoryToggleKey(eventKey)) {
				itemListOverlay.open();
				return;
			}
		}

		if (eventKey == KeyBindings.showRecipe.getKeyCode()) {
			ItemStack itemStack = getStackUnderMouseForKey(mouseHelper.getX(), mouseHelper.getY());
			if (itemStack != null) {
				recipesGui.showRecipes(itemStack);
				return;
			}
		} else if (eventKey == KeyBindings.showUses.getKeyCode()) {
			ItemStack itemStack = getStackUnderMouseForKey(mouseHelper.getX(), mouseHelper.getY());
			if (itemStack != null) {
				recipesGui.showUses(itemStack);
				return;
			}
		}

		for (IKeyable keyable : keyables) {
			if (keyable.isOpen() && keyable.onKeyPressed(eventKey)) {
				return;
			}
		}
	}

	private boolean isInventoryToggleKey(int keyCode) {
		return keyCode == Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode();
	}

	private boolean isInventoryCloseKey(int keyCode) {
		return keyCode == Keyboard.KEY_ESCAPE;
	}

}
