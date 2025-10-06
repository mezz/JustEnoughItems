package mezz.jei.gui.elements.config;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.runtime.config.IJeiConfigCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.config.file.ConfigValue;
import mezz.jei.common.config.file.IConfigSchema;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfigContextMenu {

	private static final int ENTRY_HEIGHT = 20;
	private static final int ENTRY_WIDTH = 200;

	public static ConfigContextMenu create() {
		var background = Internal.getTextures().getConfigWidgetBackground();
		IConfigSchema schema = Internal.getClientConfigSchema();
		int height = schema.getCategories().size() * ENTRY_HEIGHT;
		return new ConfigContextMenu(background, schema);
	}

	private final DrawableNineSliceTexture background;
	private final DrawableNineSliceTexture entryBackground;
	private ImmutableRect2i clickArea;
	private boolean display;
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;

	private final IConfigSchema schema;
	private final List<CategoryWidget> categories;

	private ConfigContextMenu(DrawableNineSliceTexture background, IConfigSchema schema) {
		this.background = background;
		this.entryBackground = Internal.getTextures().getRecipeBackground();
		this.schema = schema;
		categories = schema.getCategories()
			.stream()
			.limit(3)//TODO:just for debug
			.map(CategoryWidget::new)
			.toList();
	}

	public void updateBounds() {
		int x = clickArea.getX() - ENTRY_WIDTH - 5;
		int height = categories.stream()
			.mapToInt(category -> category.expanded ? category.entries.size() : 1)
			.sum() * ENTRY_HEIGHT;
		int y = clickArea.getY() - height;
		updateBounds(new ImmutableRect2i(x, y, ENTRY_WIDTH, height + 10));
	}

	public void updateBounds(ImmutableRect2i area) {
		this.area = area;
		int entryCount = 0;
		for (int i = 0, categoriesSize = categories.size(); i < categoriesSize; i++) {
			entryCount++;
			CategoryWidget category = categories.get(i);
			if (category.expanded) {
				entryCount += category.entries.size();
			}
			category.updateBounds(new ImmutableRect2i(area.getX() + 5, area.getY() + i * ENTRY_HEIGHT + 5, area.getWidth() - 10, ENTRY_HEIGHT));
		}
	}

	public void setClickArea(ImmutableRect2i area) {
		this.clickArea = area;
	}

	public boolean isDisplay() {
		return display;
	}

	public IUserInputHandler createInputHandler() {
		return new MenuInputHandler();
	}

	private class MenuInputHandler implements IUserInputHandler {
		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
			if (clickArea.contains(input.getMouseX(), input.getMouseY())) {
				if (input.is(keyBindings.getRightClick())) {
					if (!input.isSimulate()) {
						display = true;
						updateBounds();
					}
					return Optional.of(this);
				}
			} else if (area.contains(input.getMouseX(), input.getMouseY())) {
				for (CategoryWidget category : categories) {
					if (category.area.contains(input.getMouseX(), input.getMouseY())) {
						if (category.mouseClick(input)) {
							return Optional.of(this);
						}
					}
				}
			} else {
				display = false;
			}
			return Optional.empty();
		}

		@Override
		public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
			if (display && area.contains(mouseX, mouseY)) {
				return Optional.of(this);
			}
			return Optional.empty();
		}
	}


	public void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
		RenderSystem.disableDepthTest();
		if (!display) return;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, 0, 400F);
		background.draw(guiGraphics, area);
		for (CategoryWidget category : categories) {
			category.draw(guiGraphics, mouseX, mouseY);
		}
		guiGraphics.pose().popPose();
		RenderSystem.enableDepthTest();
	}

	class CategoryWidget {
		private final IJeiConfigCategory category;
		private final List<EntryWidget<?>> entries;

		private ImmutableRect2i area;
		private boolean expanded;

		@SuppressWarnings({"unchecked", "rawtypes"})
		<T> CategoryWidget(IJeiConfigCategory category) {
			this.category = category;
			entries = new ArrayList<>();
			for (var config : category.getConfigValues()) {
				switch (config.getDefaultValue()) {
					case Boolean ignored -> entries.add(new BooleanEntryWidget((ConfigValue<Boolean>) config));
					case Integer ignored -> entries.add(new IntegerEntryWidget((ConfigValue<Integer>) config));
					case Enum ignored -> entries.add(new EnumEntryWidget((ConfigValue<Double>) config));
					case List ignored -> entries.add(new ListEntryWidget((ConfigValue<List<T>>) config, null));
					default ->
						throw new IllegalArgumentException("Unknown type: " + config.getDefaultValue().getClass());
				}
			}
		}

		public void updateBounds(ImmutableRect2i area) {
			this.area = area;
			for (int i = 0; i < entries.size(); i++) {
				EntryWidget<?> entry = entries.get(i);
				ImmutableRect2i entryArea = new ImmutableRect2i(area.getX() + 3, area.getY() + i * ENTRY_HEIGHT, area.getWidth() - 6, ENTRY_HEIGHT);
				entry.updateBounds(entryArea);
			}
		}

		public void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
			entryBackground.draw(guiGraphics, area);
			guiGraphics.pose().pushPose();
			{
				guiGraphics.pose().translate(area.getX(), area.getY(), 0);
				String expandSymbol = expanded ? "-" : "+";
				guiGraphics.drawString(Minecraft.getInstance().font, expandSymbol, 5, 5, 0xFFFFFF);
				guiGraphics.drawString(Minecraft.getInstance().font, category.getName(), 13, 5, 0xFFFFFF);
			}
			guiGraphics.pose().popPose();
		}

		public boolean mouseClick(UserInput input) {
			if (area.contains(input.getMouseX(), input.getMouseY())) {
				if (!input.isSimulate()) {
					expanded = !expanded;
				}
				return true;
			}
			return false;
		}
	}


}
