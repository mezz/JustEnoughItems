package mezz.jei.gui.ingredients;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientTooltipCallback;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.Focus;
import mezz.jei.util.ErrorUtil;
import net.minecraft.client.renderer.Rect2i;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GuiIngredientGroup<T> implements IGuiIngredientGroup<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Map<Integer, GuiIngredient<T>> guiIngredients = new HashMap<>();
	private final EnumMap<RecipeIngredientRole, Set<Integer>> slots = new EnumMap<>(RecipeIngredientRole.class);
	private final IIngredientHelper<T> ingredientHelper;
	private final IIngredientRenderer<T> ingredientRenderer;
	private final IIngredientType<T> ingredientType;
	private final int cycleOffset;
	/**
	 * If focus is set and any of the guiIngredients contains focus
	 * they will only display focus instead of rotating through all their values.
	 */
	@Nullable
	private Focus<T> focus;

	private final List<IGuiIngredientTooltipCallback> tooltipCallbacks = new ArrayList<>();

	public GuiIngredientGroup(IIngredientManager ingredientManager, IIngredientType<T> ingredientType, int cycleOffset) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		this.ingredientType = ingredientType;
		this.ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		this.ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		this.cycleOffset = cycleOffset;
	}

	@Override
	public void init(int slotIndex, boolean input, int xPosition, int yPosition) {
		RecipeIngredientRole role = input ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
		init(slotIndex, role, ingredientRenderer, xPosition, yPosition, 16, 16, 0, 0);
	}

	@Override
	public void init(int slotIndex, boolean input, IIngredientRenderer<T> ingredientRenderer, int xPosition, int yPosition, int width, int height, int xInset, int yInset) {
		RecipeIngredientRole role = input ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
		init(slotIndex, role, ingredientRenderer, xPosition, yPosition, width, height, xInset, yInset);
	}

	@Override
	public void init(int slotIndex, RecipeIngredientRole role, IIngredientRenderer<T> ingredientRenderer, int xPosition, int yPosition, int width, int height, int xInset, int yInset) {
		Rect2i rect = new Rect2i(xPosition, yPosition, width, height);
		GuiIngredient<T> guiIngredient = new GuiIngredient<>(slotIndex, role, ingredientRenderer, ingredientHelper, rect, xInset, yInset, cycleOffset);
		guiIngredients.put(slotIndex, guiIngredient);
		slots.computeIfAbsent(role, key -> new IntOpenHashSet())
			.add(slotIndex);
	}

	@Override
	public void set(IIngredients ingredients) {
		List<List<T>> inputs = ingredients.getInputs(ingredientType);
		List<List<T>> outputs = ingredients.getOutputs(ingredientType);
		int inputIndex = 0;
		int outputIndex = 0;

		List<Integer> slots = new ArrayList<>(guiIngredients.keySet());
		Collections.sort(slots);
		Set<Integer> inputSlots = this.slots.get(RecipeIngredientRole.INPUT);
		for (Integer slot : slots) {
			if (inputSlots.contains(slot)) {
				if (inputIndex < inputs.size()) {
					List<T> input = inputs.get(inputIndex);
					inputIndex++;
					set(slot, input);
				}
			} else {
				if (outputIndex < outputs.size()) {
					List<T> output = outputs.get(outputIndex);
					outputIndex++;
					set(slot, output);
				}
			}
		}
	}

	@Override
	public void set(int slotIndex, @Nullable List<T> ingredients) {
		// Sanitize API input
		if (ingredients != null) {
			for (T ingredient : ingredients) {
				Class<? extends T> ingredientClass = ingredientType.getIngredientClass();
				if (!ingredientClass.isInstance(ingredient) && ingredient != null) {
					LOGGER.error("Received wrong type of ingredient. Expected {}, got {}", ingredientClass, ingredient.getClass(), new IllegalArgumentException());
					return;
				}
			}
		}
		GuiIngredient<T> guiIngredient = guiIngredients.get(slotIndex);

		if (focus == null || focus.getRole() == guiIngredient.getRole()) {
			guiIngredient.set(ingredients, focus);
		} else {
			guiIngredient.set(ingredients, null);
		}
	}

	@Override
	public void set(int slotIndex, @Nullable T value) {
		set(slotIndex, Collections.singletonList(value));
	}

	@Override
	public void setBackground(int slotIndex, IDrawable background) {
		GuiIngredient<T> guiIngredient = guiIngredients.get(slotIndex);
		guiIngredient.setBackground(background);
	}

	@Override
	@Deprecated
	public void addTooltipCallback(ITooltipCallback<T> tooltipCallback) {
		ErrorUtil.checkNotNull(tooltipCallback, "tooltipCallback");
		this.tooltipCallbacks.add(new LegacyTooltipAdapter<>(this.ingredientType, tooltipCallback));
	}

	@Override
	public void addTooltipCallback(IGuiIngredientTooltipCallback tooltipCallback) {
		ErrorUtil.checkNotNull(tooltipCallback, "tooltipCallback");
		this.tooltipCallbacks.add(tooltipCallback);
	}

	@Override
	public Map<Integer, GuiIngredient<T>> getGuiIngredients() {
		return guiIngredients;
	}

	public Optional<GuiIngredient<T>> getHoveredIngredient(int xOffset, int yOffset, double mouseX, double mouseY) {
		return guiIngredients.values().stream()
			.filter(ingredient -> ingredient.isMouseOver(xOffset, yOffset, mouseX, mouseY))
			.findFirst();
	}

	public void draw(PoseStack poseStack, int xOffset, int yOffset, int highlightColor, int mouseX, int mouseY) {
		for (GuiIngredient<T> ingredient : guiIngredients.values()) {
			ingredient.draw(poseStack, xOffset, yOffset);
			if (ingredient.isMouseOver(xOffset, yOffset, mouseX, mouseY)) {
				ingredient.setTooltipCallbacks(tooltipCallbacks);
				ingredient.drawHighlight(poseStack, highlightColor, xOffset, yOffset);
			}
		}
	}

	@Override
	public void setOverrideDisplayFocus(@Nullable IFocus<T> focus) {
		if (focus == null) {
			this.focus = null;
		} else {
			this.focus = Focus.checkOne(focus);
		}
	}

	public EnumMap<RecipeIngredientRole, Set<Integer>> getSlots() {
		return slots;
	}
}
