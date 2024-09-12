package mezz.jei.common.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.common.Internal;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.SafeIngredientUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JeiTooltip implements ITooltipBuilder {
	private final List<Component> lines = new ArrayList<>();
	private final List<Either<FormattedText, TooltipComponent>> elements = new ArrayList<>();
	private @Nullable ITypedIngredient<?> typedIngredient;

	@Override
	public void add(@Nullable Component formattedText) {
		if (formattedText == null) {
			if (Services.PLATFORM.getModHelper().isInDev()) {
				throw new NullPointerException("Tried to add null tooltip text");
			}
			return;
		}
		lines.add(formattedText);
		elements.add(Either.left(formattedText));
	}

	@Override
	public void add(@Nullable FormattedText formattedText) {
		if (formattedText == null) {
			if (Services.PLATFORM.getModHelper().isInDev()) {
				throw new NullPointerException("Tried to add null tooltip text");
			}
			return;
		}
		if (formattedText instanceof Component component) {
			lines.add(component);
		}
		elements.add(Either.left(formattedText));
	}

	@Override
	public void add(@Nullable TooltipComponent component) {
		if (component == null) {
			if (Services.PLATFORM.getModHelper().isInDev()) {
				throw new NullPointerException("Tried to add null tooltip component");
			}
			return;
		}
		elements.add(Either.right(component));
	}

	public void addClientTooltipComponent(@Nullable ClientTooltipComponent component) {
		if (component == null) {
			if (Services.PLATFORM.getModHelper().isInDev()) {
				throw new NullPointerException("Tried to add null tooltip component");
			}
			return;
		}
		if (component instanceof TooltipComponent tooltipComponent) {
			add(tooltipComponent);
			return;
		}
		throw new IllegalArgumentException("ClientTooltipComponent must also implement TooltipComponent");
	}

	@Override
	public void setIngredient(ITypedIngredient<?> typedIngredient) {
		this.typedIngredient = typedIngredient;
	}

	public void addKeyUsageComponent(String translationKey, IJeiKeyMapping keyMapping) {
		MutableComponent translatedKeyMessage = keyMapping.getTranslatedKeyMessage().copy();
		addKeyUsageComponent(translationKey, translatedKeyMessage);
	}

	public void addKeyUsageComponent(String translationKey, MutableComponent keyMapping) {
		Component boldKeyMapping = keyMapping.withStyle(ChatFormatting.BOLD);
		MutableComponent component = Component.translatable(translationKey, boldKeyMapping)
			.withStyle(ChatFormatting.ITALIC)
			.withStyle(ChatFormatting.GRAY);

		add(component);
	}

	@Override
	public void addAll(Collection<? extends Component> components) {
		for (Component component : components) {
			add(component);
		}
	}

	@Override
	public void clear() {
		this.lines.clear();
		this.elements.clear();
		this.typedIngredient = null;
	}

	public void addAll(JeiTooltip tooltip) {
		lines.addAll(tooltip.lines);
		elements.addAll(tooltip.elements);
	}

	public boolean isEmpty() {
		return elements.isEmpty() && typedIngredient == null;
	}

	public List<Either<FormattedText, TooltipComponent>> build() {
		return elements;
	}

	@SuppressWarnings("removal")
	@Override
	public List<Component> toLegacyToComponents() {
		return new ArrayList<>(lines);
	}

	@SuppressWarnings("removal")
	@Override
	public void removeAll(List<Component> components) {
		for (Component component : components) {
			lines.remove(component);
			elements.remove(Either.left(component));
		}
	}

	@Override
	public String toString() {
		return elements.stream()
			.map(e -> e.map(
				FormattedText::getString,
				Object::toString
			))
			.collect(Collectors.joining("\n", "[\n", "\n]"));
	}

	public void draw(PoseStack poseStack, int x, int y) {
		if (typedIngredient != null) {
			draw(poseStack, x, y, typedIngredient);
			return;
		}
		if (isEmpty()) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (screen == null) {
			return;
		}
		Font font = minecraft.font;
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		try {
			renderHelper.renderTooltip(screen, poseStack, elements, x, y, font, ItemStack.EMPTY);
		} catch (RuntimeException e) {
			throw new RuntimeException("Crashed when rendering tooltip:\n" + this, e);
		}
	}

	public <T> void draw(PoseStack poseStack, int x, int y, ITypedIngredient<T> typedIngredient) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		draw(poseStack, x, y, typedIngredient, ingredientRenderer, ingredientManager);
	}

	public <T> void draw(
		PoseStack poseStack,
		int x,
		int y,
		ITypedIngredient<T> typedIngredient,
		IIngredientRenderer<T> ingredientRenderer,
		IIngredientManager ingredientManager
	) {
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (screen == null) {
			return;
		}
		T ingredient = typedIngredient.getIngredient();
		Font font = ingredientRenderer.getFontRenderer(minecraft, ingredient);
		ItemStack itemStack = typedIngredient.getItemStack().orElse(ItemStack.EMPTY);

		itemStack.getTooltipImage()
			.ifPresent(c -> elements.add(Math.min(1, elements.size()), Either.right(c)));

		addDebugInfo(ingredientManager, typedIngredient);

		IJeiHelpers jeiHelpers = Internal.getJeiRuntime().getJeiHelpers();
		IModIdHelper modIdHelper = jeiHelpers.getModIdHelper();
		modIdHelper.getModNameForTooltip(typedIngredient)
			.ifPresent(this::add);

		if (isEmpty()) {
			return;
		}

		SafeIngredientUtil.renderTooltip(
			screen,
			poseStack,
			this,
			x,
			y,
			font,
			itemStack,
			typedIngredient,
			ingredientManager
		);
	}

	private <T> void addDebugInfo(IIngredientManager ingredientManager, ITypedIngredient<T> typedIngredient) {
		if (!DebugConfig.isDebugInfoTooltipsEnabled() || !Minecraft.getInstance().options.advancedItemTooltips) {
			return;
		}
		T ingredient = typedIngredient.getIngredient();
		IIngredientType<T> type = typedIngredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(type);

		add(Component.empty());
		add(
			Component.literal("JEI Debug:")
				.withStyle(ChatFormatting.DARK_GRAY)
		);
		add(
			Component.literal("• type: " + ingredientHelper.getIngredientType().getIngredientClass())
				.withStyle(ChatFormatting.DARK_GRAY)
		);
		add(
			Component.literal("• uid: " + ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient))
				.withStyle(ChatFormatting.DARK_GRAY)
		);
		add(
			Component.literal("• extra info: " + ingredientHelper.getErrorInfo(ingredient))
				.withStyle(ChatFormatting.DARK_GRAY)
		);
		add(Component.empty());
	}

	@Override
	public List<Component> getLegacyComponents() {
		return lines;
	}

	public List<Either<FormattedText, TooltipComponent>> getElements() {
		return Collections.unmodifiableList(elements);
	}
}
