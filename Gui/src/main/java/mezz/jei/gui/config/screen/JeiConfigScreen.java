package mezz.jei.gui.config.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.config.IJeiConfigCategory;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.Internal;
import mezz.jei.common.config.file.IConfigSchema;
import mezz.jei.common.config.file.serializers.BooleanSerializer;
import mezz.jei.common.config.file.serializers.EnumSerializer;
import mezz.jei.common.config.file.serializers.IntegerSerializer;
import mezz.jei.common.config.file.serializers.ListSerializer;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.StringUtil;
import mezz.jei.gui.GuiProperties;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.InputType;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import mezz.jei.gui.input.handlers.SameElementInputHandler;
import mezz.jei.gui.input.handlers.UserInputRouter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JeiConfigScreen extends Screen {


	public static Screen create() {
		return new JeiConfigScreen(Internal.getClientConfigSchema());
	}

	private static final Component expandComponent = Component.literal("+");
	private static final Component unexpandComponent = Component.literal("-");

	private static final int minGuiWidth = 245;
	private static final int minHeight = 196;
	private static final int scrollFactor = 20;

	//config data
	private final IConfigSchema clientSchema;

	//ui
	private final DrawableNineSliceTexture background;
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;
	private ImmutableRect2i displayArea = ImmutableRect2i.EMPTY;

	private final UserInputRouter inputHandler;
	private final IInternalKeyMappings keyBindings;

	//scroll
	private ImmutableRect2i scrollBarArea = ImmutableRect2i.EMPTY;
	private final DrawableNineSliceTexture scrollbarMarker;
	private final DrawableNineSliceTexture scrollbarBackground;
	/**
	 * from 0 to 1
	 **/
	private int scrollOffset = 0;
	private int maxElementDisplay = 0;

	//config category
	private final List<CategoryWidget> categoryWidgets = new ArrayList<>();
	private int totalElements = 0;

	@Nullable
	private ValueSelector<?> valueSelector;

	private JeiConfigScreen(IConfigSchema clientSchema) {
		super(Component.literal("Jei Configs"));
		this.clientSchema = clientSchema;

		Textures textures = Internal.getTextures();
		this.background = textures.getConfigWidgetBackground();
		this.scrollbarMarker = textures.getConfigScrollBar();
		this.scrollbarBackground = textures.getScrollbarBackground();

		List<IUserInputHandler> inputHandlers = clientSchema.getCategories().stream()
			.map(category -> {
				CategoryWidget widget = new CategoryWidget(category);
				categoryWidgets.add(widget);
				return widget.createInputHandler();

			}).collect(Collectors.toList());
		inputHandlers.addFirst(new InputHandler());
		this.inputHandler = new UserInputRouter("JeiConfigScreen", inputHandlers);
		this.keyBindings = Internal.getKeyMappings();
	}

	@Nullable
	public IGuiProperties getGuiProperties() {
		if (width <= 0 || height <= 0) {
			return null;
		}
		return new GuiProperties(
			getClass(),
			area.getX(),
			area.getY(),
			area.getWidth(),
			area.getHeight(),
			width,
			height
		);
	}

	public static IGlobalGuiHandler createGuiHandler() {
		return new IGlobalGuiHandler() {
			@Override
			public Collection<Rect2i> getGuiExtraAreas() {
				Minecraft minecraft = Minecraft.getInstance();
				if (minecraft.screen instanceof JeiConfigScreen screen) {
					if (screen.valueSelector != null) {
						ImmutableRect2i area = screen.valueSelector.area;
						return Collections.singleton(new Rect2i(area.getX(), area.getY(), area.getWidth(), area.getHeight()));
					}
				}
				return Collections.emptyList();
			}
		};
	}

	@Override
	protected void init() {
		super.init();
		int guiLeft = (width - minGuiWidth) / 2;
		int guiTop = (height - minHeight) / 2;
		area = new ImmutableRect2i(guiLeft, guiTop, minGuiWidth, minHeight);
		displayArea = new ImmutableRect2i(area.getX() + 7, area.getY() + 8, area.getWidth() - 24, area.getHeight() - 16);
		scrollBarArea = new ImmutableRect2i(area.getX() + area.getWidth() - 9 - 4, area.getY() + 5, 8, area.getHeight() - 8);
		maxElementDisplay = (area.getHeight() - 8) / 20;
		updateLayout();
	}

	private void updateLayout() {
		totalElements = categoryWidgets.stream()
			.mapToInt(cat -> cat.expand ? cat.entryWidgets.size() + 1 : 1)
			.sum();
		scrollOffset = Mth.clamp(scrollOffset, 0, totalElements - maxElementDisplay);
		int currentY = area.getY() + 8 - scrollOffset * scrollFactor;
		for (CategoryWidget categoryWidget : categoryWidgets) {
			categoryWidget.updateBounds(new ImmutableRect2i(area.getX() + 7, currentY, area.getWidth() - 24, 20));
			currentY += 20;
			if (categoryWidget.expand) {
				for (EntryWidget<?> entryWidget : categoryWidget.entryWidgets) {
					entryWidget.updateBounds(new ImmutableRect2i(area.getX() + 13, currentY, area.getWidth() - 36, 20));
					entryWidget.afterBoundUpdated();
					currentY += entryWidget.getHeight();
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean ret = UserInput.fromVanilla(mouseX, mouseY, button, InputType.SIMULATE)
			.map(this::handleInput)
			.orElse(false);
		return ret || super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		boolean ret = UserInput.fromVanilla(mouseX, mouseY, button, InputType.EXECUTE)
			.map(this::handleInput)
			.orElse(false);
		if (ret) {
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		}
		return ret || super.mouseClicked(mouseX, mouseY, button);
	}

	private boolean handleInput(UserInput input) {
		return this.inputHandler.handleUserInput(this, input, keyBindings);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (mouseY >= area.getY() && mouseY <= area.getY() + area.getHeight()) {
			scrollOffset = Math.max(0, Math.min(totalElements - maxElementDisplay, scrollOffset - Mth.ceil(scrollY)));
			updateLayout();
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	private class InputHandler implements IUserInputHandler {

		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
			if (valueSelector != null) {
				if (valueSelector.isMouseOver(input.getMouseX(), input.getMouseY()) && input.is(keyBindings.getLeftClick())) {
					if (valueSelector.onMouseClicked(input)) {
						return Optional.of(this);
					}
				} else {
					valueSelector = null;
				}
			}
			return Optional.empty();
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (minecraft == null) {
			return;
		}
		renderTransparentBackground(guiGraphics);

		CategoryWidget hoveredCategoryWidget = null;
		EntryWidget<?> hoveredEntryWidget = null;
		guiGraphics.pose().pushPose();
		background.draw(guiGraphics, area);
		guiGraphics.enableScissor(displayArea.getX(), displayArea.getY(), displayArea.getX() + displayArea.getWidth(), displayArea.getY() + displayArea.getHeight());
		for (CategoryWidget categoryWidget : categoryWidgets) {
			categoryWidget.draw(guiGraphics, mouseX, mouseY);
			if (displayArea.contains(mouseX, mouseY) && categoryWidget.nameArea.contains(mouseX, mouseY)) {
				hoveredCategoryWidget = categoryWidget;
			}
			if (categoryWidget.expand) {
				for (EntryWidget<?> entryWidget : categoryWidget.entryWidgets) {
					entryWidget.draw(guiGraphics, mouseX, mouseY);
					if (displayArea.contains(mouseX, mouseY) && entryWidget.nameArea.contains(mouseX, mouseY)) {
						hoveredEntryWidget = entryWidget;
					}
				}
			}
		}
		guiGraphics.disableScissor();
		guiGraphics.pose().popPose();

		renderScrollBar(guiGraphics);

		if (valueSelector != null) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0, 0, 350);
			valueSelector.draw(guiGraphics, mouseX, mouseY);
			guiGraphics.pose().popPose();
		}

		if (hoveredCategoryWidget != null) {
			hoveredCategoryWidget.drawTooltip(guiGraphics, mouseX, mouseY);
		}
		if (hoveredEntryWidget != null) {
			hoveredEntryWidget.drawTooltip(guiGraphics, mouseX, mouseY);
		}
	}

	private void renderScrollBar(GuiGraphics guiGraphics) {
		scrollbarBackground.draw(guiGraphics, scrollBarArea);
		scrollbarMarker.draw(guiGraphics, new ImmutableRect2i(scrollBarArea.getX() + 1, scrollBarArea.getY() + scrollBarArea.getHeight() * scrollOffset / totalElements, scrollBarArea.getWidth() - 2, scrollBarArea.getHeight() * maxElementDisplay / totalElements));
	}

	private class CategoryWidget {
		final IJeiConfigCategory category;
		final List<EntryWidget<?>> entryWidgets = new ArrayList<>();
		boolean expand;
		ImmutableRect2i area = ImmutableRect2i.EMPTY;
		ImmutableRect2i clickArea;
		ImmutableRect2i nameArea;

		@SuppressWarnings({"rawtypes", "unchecked"})
		CategoryWidget(IJeiConfigCategory category) {
			this.category = category;
			for (IJeiConfigValue<?> value : category.getConfigValues()) {
				switch (value.getSerializer()) {
					case BooleanSerializer ignored ->
						entryWidgets.add(new BooleanEntryWidget((IJeiConfigValue<Boolean>) value));
					case IntegerSerializer ignored ->
						entryWidgets.add(new IntegerEntryWidget((IJeiConfigValue<Integer>) value));
					case EnumSerializer ignored -> entryWidgets.add(new EnumEntryWidget(value));
					case ListSerializer ignored -> entryWidgets.add(new ListEntryWidget(value));
					default ->
						throw new UnsupportedOperationException("Unsupported serializer: " + value.getSerializer());
				}
			}
		}

		void updateBounds(ImmutableRect2i area) {
			this.area = area;
			clickArea = new ImmutableRect2i(area.getX() + 6, area.getY() + 6, 16, 16);
			int nameWidth = font.width(category.getLocalizedName());
			nameWidth = Math.min(nameWidth, area.getWidth() - 20);
			nameArea = new ImmutableRect2i(area.getX() + 20, area.getY() + 7, nameWidth, 16);
		}

		boolean isMouseOver(double mouseX, double mouseY) {
			return area.contains(mouseX, mouseY);
		}

		IUserInputHandler createInputHandler() {
			List<IUserInputHandler> entryHandlers = entryWidgets.stream()
				.map(entry -> {
					final IUserInputHandler entryInputHandler = entry.createInputHandler();
					return new IUserInputHandler() {
						@Override
						public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
							if (expand && displayArea.contains(input.getMouseX(), input.getMouseY())) {
								return entryInputHandler.handleUserInput(screen, input, keyBindings);
							}
							return Optional.empty();
						}

						@Override
						public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
							if (expand) {
								return entryInputHandler.handleMouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
							}
							return Optional.empty();
						}
					};
				}).collect(Collectors.toList());

			entryHandlers.addFirst(new CategoryWidgetInputHandler());
			return new CombinedInputHandler("ConfigCategory:" + category.getName(), entryHandlers);
		}

		private class CategoryWidgetInputHandler implements IUserInputHandler {
			@Override
			public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
				if (displayArea.contains(input.getMouseX(), input.getMouseY()) && area.contains(input.getMouseX(), input.getMouseY())) {
					if (input.is(keyBindings.getLeftClick())) {
						if (!input.isSimulate()) {
							expand = !expand;
							JeiConfigScreen.this.updateLayout();
						}
						return Optional.of(this);
					}
				}
				return Optional.empty();
			}
		}

		void drawTooltip(GuiGraphics guiGraphics, double mouseX, double mouseY) {
			JeiTooltip tooltip = new JeiTooltip();
			tooltip.add(category.getLocalizedName().copy().withStyle(ChatFormatting.YELLOW));
			tooltip.add(category.getDescription().copy().withStyle(ChatFormatting.GREEN));
			tooltip.draw(guiGraphics, (int) mouseX, (int) mouseY);
		}

		void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
			Internal.getTextures().getRecipeBackground().draw(guiGraphics, area);
			guiGraphics.drawString(font, expand ? unexpandComponent : expandComponent, clickArea.getX(), clickArea.getY(), 0xFFFFFF);
			guiGraphics.drawString(font, category.getLocalizedName(), nameArea.getX(), nameArea.getY(), 0xFFFFFF);
		}
	}

	private abstract class EntryWidget<T> {

		static final int maxNameWidth = 145;

		final IJeiConfigValue<T> configValue;
		final Component fullName;

		FormattedCharSequence visibleString;

		ImmutableRect2i area = ImmutableRect2i.EMPTY;
		ImmutableRect2i nameArea = ImmutableRect2i.EMPTY;

		Undo undo;

		private EntryWidget(IJeiConfigValue<T> configValue) {
			this.configValue = configValue;
			this.fullName = StringUtil.stripStyling(configValue.getLocalizedName());

			this.undo = new Undo() {
				final T origin = configValue.getValue();

				@Override
				public void undo() {
					configValue.set(origin);
				}
			};
		}

		int getHeight() {
			return 20;
		}

		void updateBounds(ImmutableRect2i area) {
			this.area = area;
			this.nameArea = area.keepLeft(Math.min(maxNameWidth, font.width(fullName)))
				.addOffset(5, 6);
		}

		void afterBoundUpdated() {
			if (font.width(fullName) > nameArea.getWidth()) {
				FormattedText formattedText = StringUtil.truncateStringToWidth(fullName, nameArea.getWidth(), font);
				visibleString = Language.getInstance().getVisualOrder(formattedText);
			} else {
				visibleString = fullName.getVisualOrderText();
			}
		}

		boolean isMouseOver(double mouseX, double mouseY) {
			return area.contains(mouseX, mouseY);
		}

		IUserInputHandler createInputHandler() {
			return new EntryWidgetInputHandler();
		}

		private class EntryWidgetInputHandler implements IUserInputHandler {
			@Override
			public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
				if (onMouseClicked(input)) {
					return Optional.of(new SameElementInputHandler(this, area::contains));
				}
				return Optional.empty();
			}
		}

		boolean onMouseClicked(UserInput input) {
			return false;
		}

		void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
			Internal.getTextures().getRecipeBackground().draw(guiGraphics, area);
			drawContent(guiGraphics, mouseX, mouseY);
		}

		void drawTooltip(GuiGraphics guiGraphics, double mouseX, double mouseY) {
			JeiTooltip tooltip = new JeiTooltip();
			getTooltip(tooltip);
			if (!tooltip.isEmpty()) {
				tooltip.draw(guiGraphics, (int) mouseX, (int) mouseY);
			}
		}

		void getTooltip(JeiTooltip tooltip) {
			tooltip.add(configValue.getLocalizedName().copy().withStyle(ChatFormatting.YELLOW));
			tooltip.add(configValue.getLocalizedDescription().copy().withStyle(ChatFormatting.GREEN));
		}

		final void drawName(GuiGraphics guiGraphics) {
			guiGraphics.drawString(font, visibleString, nameArea.getX(), nameArea.getY(), 0xFFFFFFFF);
		}

		abstract void drawContent(GuiGraphics guiGraphics, double mouseX, double mouseY);
	}

	private class BooleanEntryWidget extends EntryWidget<Boolean> {

		ImmutableRect2i clickableValue = ImmutableRect2i.EMPTY;
		Component displayValue;

		BooleanEntryWidget(IJeiConfigValue<Boolean> value) {
			super(value);
			displayValue = getDisplayValue();
		}

		Component getDisplayValue() {
			return configValue.getValue() ? Component.translatable("jei.config.value.boolean.true") : Component.translatable("jei.config.value.boolean.false");
		}

		@Override
		void updateBounds(ImmutableRect2i area) {
			super.updateBounds(area);
			int valueWidth = font.width(displayValue);
			clickableValue = area.keepRight(valueWidth)
				.addOffset(-5, 5)
				.keepTop(font.lineHeight);
		}

		@Override
		void drawContent(GuiGraphics guiGraphics, double mouseX, double mouseY) {
			//name
			drawName(guiGraphics);
			//clickable
			Component toDisplay = clickableValue.contains(mouseX, mouseY) ?
				displayValue.copy().withStyle(ChatFormatting.UNDERLINE) :
				displayValue;
			guiGraphics.drawString(font, toDisplay, clickableValue.getX(), clickableValue.getY(), 0xFFFFFFFF);
		}

		@Override
		boolean onMouseClicked(UserInput input) {
			if (clickableValue.contains(input.getMouseX(), input.getMouseY())) {
				if (!input.isSimulate()) {
					configValue.set(!configValue.getValue());
					displayValue = getDisplayValue();
				}
				return true;
			}
			return false;
		}
	}

	private class IntegerEntryWidget extends EntryWidget<Integer> {

		final ValueButton upButton;
		final ValueButton downButton;
		final IntegerSerializer serializer;
		ImmutableRect2i valueArea = ImmutableRect2i.EMPTY;

		IntegerEntryWidget(IJeiConfigValue<Integer> value) {
			super(value);
			this.serializer = (IntegerSerializer) value.getSerializer();
			Textures textures = Internal.getTextures();
			this.upButton = new ValueButton(
				() -> {
					IntegerSerializer serializer = (IntegerSerializer) value.getSerializer();
					return configValue.getValue() < serializer.getMax();
				},
				textures.getArrowUp(),
				() -> configValue.set(Math.min(getMax(), configValue.getValue() + 1)));

			this.downButton = new ValueButton(
				() -> {
					IntegerSerializer serializer = (IntegerSerializer) value.getSerializer();
					return configValue.getValue() > serializer.getMin();
				},
				textures.getArrowDown(),
				() -> configValue.set(Math.max(getMin(), configValue.getValue() - 1)));
		}

		int getMax() {
			return serializer.getMax();
		}

		Integer getMin() {
			return serializer.getMin();
		}

		@Override
		void updateBounds(ImmutableRect2i area) {
			super.updateBounds(area);
			valueArea = area.keepRight(50)
				.addOffset(-15, 3)
				.cropBottom(5);
			ImmutableRect2i upArea = new ImmutableRect2i(
				valueArea.getX() + valueArea.getWidth() + 1,
				valueArea.getY() - 2,
				10,
				10
			);
			upButton.updateBounds(upArea);
			ImmutableRect2i downArea = new ImmutableRect2i(
				valueArea.getX() + valueArea.getWidth() + 1,
				valueArea.getY() + 8,
				10,
				9
			);
			downButton.updateBounds(downArea);
		}

		@Override
		void drawContent(GuiGraphics guiGraphics, double mouseX, double mouseY) {
			//name
			drawName(guiGraphics);
			//value
			Internal.getTextures().getBookmarkListSlotBackground().draw(guiGraphics, valueArea);
			guiGraphics.drawString(font, Component.literal(configValue.getValue().toString()), valueArea.getX() + 4, valueArea.getY() + 4, 0xFFFFFF);
			upButton.drawButton(guiGraphics, mouseX, mouseY);
			downButton.drawButton(guiGraphics, mouseX, mouseY);
		}

		@Override
		boolean onMouseClicked(UserInput input) {
			if (upButton.active.getAsBoolean() && upButton.area.contains(input.getMouseX(), input.getMouseY())) {
				if (!input.isSimulate()) {
					upButton.onClick.run();
				}
				return true;

			}
			if (downButton.active.getAsBoolean() && downButton.area.contains(input.getMouseX(), input.getMouseY())) {
				if (!input.isSimulate()) {
					downButton.onClick.run();
				}
				return true;
			}
			return false;
		}

		private static class ValueButton {
			final BooleanSupplier active;
			final IDrawableStatic buttonIcon;
			final Runnable onClick;
			ImmutableRect2i area = ImmutableRect2i.EMPTY;
			boolean pressed;

			private ValueButton(BooleanSupplier active, IDrawableStatic buttonIcon, Runnable onClick) {
				this.active = active;
				this.buttonIcon = buttonIcon;
				this.onClick = onClick;
			}

			void updateBounds(ImmutableRect2i area) {
				this.area = area;
			}

			void drawButton(GuiGraphics guiGraphics, double mouseX, double mouseY) {
				Textures textures = Internal.getTextures();
				DrawableNineSliceTexture button = textures.getButtonForState(pressed, active.getAsBoolean(), area.contains(mouseX, mouseY));
				button.draw(guiGraphics, area);
				buttonIcon.draw(guiGraphics, area.getX(), area.getY());
			}

		}
	}

	private class EnumEntryWidget<T extends Enum<T>> extends EntryWidget<T> {

		final List<T> validValues;
		ImmutableRect2i valueArea = ImmutableRect2i.EMPTY;

		EnumEntryWidget(IJeiConfigValue<T> value) {
			super(value);
			this.validValues = value.getSerializer()
				.getAllValidValues()
				.stream()
				.flatMap(Collection::stream)
				.toList();
		}

		@Override
		void updateBounds(ImmutableRect2i area) {
			super.updateBounds(area);
			int valueWidth = font.width(configValue.getValue().toString());
			valueArea = area.keepRight(valueWidth)
				.addOffset(-5, 2)
				.keepTop(font.lineHeight);
		}

		@Override
		void drawContent(GuiGraphics guiGraphics, double mouseX, double mouseY) {
			drawName(guiGraphics);
			String valueString = configValue.getValue().toString();
			MutableComponent displayValue = Component.literal(valueString);
			if (valueArea.contains(mouseX, mouseY)) {
				displayValue.withStyle(ChatFormatting.UNDERLINE);
			}
			guiGraphics.drawString(font, displayValue, valueArea.getX() + 4, valueArea.getY() + 4, 0xFFFFFF);
		}

		@Override
		boolean onMouseClicked(UserInput input) {
			if (valueArea.contains(input.getMouseX(), input.getMouseY())) {
				if (!input.isSimulate()) {
					valueSelector = new ValueSelector<>(validValues, configValue.getValue(), value -> {
						configValue.set(value);
						this.updateBounds(this.area);
					});
					valueSelector.updateBounds((int) input.getMouseX(), valueArea.getY() + valueArea.getHeight() + 2);
				}
				return true;
			}
			return false;
		}
	}

	private class ListEntryWidget<T> extends EntryWidget<List<T>> {

		private final List<ListValueEntry> listValueEntries = new ArrayList<>();

		ListEntryWidget(IJeiConfigValue<List<T>> listValue) {
			super(listValue);
			for (T value : listValue.getValue()) {
				ListValueEntry entry = new ListValueEntry(value);
				listValueEntries.add(entry);
			}
		}

		@Override
		int getHeight() {
			return (configValue.getValue().size() + 1) * 20;
		}

		@Override
		void updateBounds(ImmutableRect2i area) {
			super.updateBounds(area);
			for (int i = 0; i < listValueEntries.size(); i++) {
				ListValueEntry entry = listValueEntries.get(i);
				ImmutableRect2i entryArea = new ImmutableRect2i(
					area.getX() + 10,
					area.getY() + (i + 1) * 20,
					area.getWidth() - 30,
					20
				);
				entry.updateBounds(entryArea);
			}
		}

		@Override
		void drawContent(GuiGraphics guiGraphics, double mouseX, double mouseY) {
			drawName(guiGraphics);
			for (ListValueEntry entry : listValueEntries) {
				entry.draw(guiGraphics, mouseX, mouseY);
			}
		}

		private class DeleteButton {
			ImmutableRect2i area;
			boolean pressed;


			void updateBounds(ImmutableRect2i area) {
				this.area = area;
			}

			void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
				Textures textures = Internal.getTextures();
				boolean hovered = area.contains(mouseX, mouseY);
				var button = textures.getButtonForState(pressed, true, hovered);
				button.draw(guiGraphics, area);
				//TODO:button icon
			}
		}

		private class ListValueEntry {
			//null when unset
			@Nullable T value;

			ImmutableRect2i area;

			ListValueEntry(T value) {
				this.value = value;
			}

			void updateBounds(ImmutableRect2i area) {
				this.area = area;
			}

			void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
				Internal.getTextures().getRecipeBackground().draw(guiGraphics, area);
				if (value != null) {
					guiGraphics.drawString(font, Component.literal(value.toString()), area.getX() + 4, area.getY() + 4, 0xFFFFFF);
				}
			}

		}

	}

	private class ValueSelector<T> {
		final List<ValueEntry> valueEntries;
		final T currentValue;
		final Consumer<T> setter;

		ImmutableRect2i area = ImmutableRect2i.EMPTY;

		public ValueSelector(List<T> allValues, T currentValue, Consumer<T> setter) {
			this.valueEntries = allValues.stream()
				.map(ValueEntry::new)
				.toList();
			this.currentValue = currentValue;
			this.setter = setter;
		}

		boolean isMouseOver(double mouseX, double mouseY) {
			return area.contains(mouseX, mouseY);
		}

		void updateBounds(int x, int y) {
			int counts = valueEntries.size();
			int height = counts * 12;
			int width = valueEntries.stream()
				.mapToInt(entry -> font.width(entry.value.toString()))
				.max().orElse(50);
			area = new ImmutableRect2i(x, y, width + 14, height + 12);
			for (int i = 0; i < valueEntries.size(); i++) {
				ValueEntry entry = valueEntries.get(i);
				entry.area = new ImmutableRect2i(
					area.getX() + 5,
					area.getY() + 5 + i * 14,
					area.getWidth() - 10,
					14
				);
			}
		}

		void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
			Internal.getTextures().getRecipePreviewBackground().draw(guiGraphics, area);
			for (int i = 0, valueEntriesSize = valueEntries.size(); i < valueEntriesSize; i++) {
				ValueEntry entry = valueEntries.get(i);
				ImmutableRect2i valueArea = entry.area;
				guiGraphics.drawString(font, entry.value.toString(), valueArea.getX() + 2, valueArea.getY() + 2, 0xFFFFFFFF);
				if (i > 0) {
					drawLine(
						guiGraphics.pose(),
						area.getX() + 4,
						area.getX() + area.getWidth() - 4,
						valueArea.getY() - 2,
						0xFF959595
					);
				}
			}
		}

		boolean onMouseClicked(UserInput input) {
			for (ValueEntry entry : valueEntries) {
				if (entry.isMouseOver(input.getMouseX(), input.getMouseY())) {
					if (!input.isSimulate()) {
						setter.accept(entry.value);
						valueSelector = null;
					}
					return true;
				}
			}
			return false;
		}

		private void drawLine(PoseStack poseStack, int x1, int x2, int y, int argbColor) {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

			float a = (float) (argbColor >> 24 & 255) / 255.0F;
			float r = (float) (argbColor >> 16 & 255) / 255.0F;
			float g = (float) (argbColor >> 8 & 255) / 255.0F;
			float b = (float) (argbColor & 255) / 255.0F;
			Matrix4f pose = poseStack.last().pose();

			final int availableWidth = x2 - x1;
			if (availableWidth <= 0) {
				return;
			}
			final int dashWidth = 8;
			final int dashHeight = 1;
			final int spacing = 6;

			// space out the dashes so that we always start and end with whole dashes
			final int interval = dashWidth + spacing;
			final int dashCount = availableWidth / interval;
			final float floatInterval = (availableWidth - dashWidth) / (float) dashCount;

			for (float x = x1; x < x2; x += floatInterval) {
				builder.addVertex(pose, Mth.clamp(x + dashWidth, x1, x2), y, 0).setColor(r, g, b, a);
				builder.addVertex(pose, Mth.clamp(x, x1, x2), y, 0).setColor(r, g, b, a);
				builder.addVertex(pose, Mth.clamp(x, x1, x2), y + dashHeight, 0).setColor(r, g, b, a);
				builder.addVertex(pose, Mth.clamp(x + dashWidth, x1, x2), y + dashHeight, 0).setColor(r, g, b, a);
			}

			BufferUploader.drawWithShader(builder.buildOrThrow());
			RenderSystem.disableBlend();
		}


		class ValueEntry {
			final T value;
			ImmutableRect2i area = ImmutableRect2i.EMPTY;

			public ValueEntry(T value) {
				this.value = value;
			}

			boolean isMouseOver(double mouseX, double mouseY) {
				return area.contains(mouseX, mouseY);
			}

		}

	}

	interface Undo {
		void undo();
	}

}
