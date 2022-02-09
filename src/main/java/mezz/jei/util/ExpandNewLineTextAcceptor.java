package mezz.jei.util;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExpandNewLineTextAcceptor implements FormattedText.StyledContentConsumer<Void> {
	private final List<FormattedText> lines = new ArrayList<>();
	@Nullable
	private MutableComponent lastComponent;

	@Override
	public Optional<Void> accept(Style style, String line) {
		String[] descriptionLineExpanded = line.split("\\\\n");
		for (int i = 0; i < descriptionLineExpanded.length; i++) {
			String s = descriptionLineExpanded[i];
			if (s.isEmpty()) {
				//If the string is empty
				if (i == 0 && lastComponent != null) {
					// and we are the first string (for example from a string \nTest)
					// and we had a last component (we are a variable in a translation string)
					// add our last component as is and reset it
					lines.add(lastComponent);
					lastComponent = null;
				} else {
					//Otherwise, just add the empty line
					lines.add(TextComponent.EMPTY);
				}
				continue;
			}
			TextComponent textComponent = new TextComponent(s);
			textComponent.setStyle(style);
			if (lastComponent != null) {
				//If we already have a component that we want to continue with
				if (i == 0) {
					// and we are the first line, add ourselves to the last component
					if (!lastComponent.getStyle().isEmpty() && !lastComponent.getStyle().equals(style)) {
						//If it has a style and the style is different from the style the text component
						// we are adding has add the last component as a sibling to an empty unstyled
						// component so that we don't cause the styling to leak into the component we are adding
						lastComponent = new TextComponent("").append(lastComponent);
					}
					lastComponent.append(textComponent);
					continue;
				} else {
					// otherwise if we aren't the first line, add the old component to our list of lines
					lines.add(lastComponent);
					lastComponent = null;
				}
			}
			if (i == descriptionLineExpanded.length - 1) {
				//If we are the last line we are adding, persist the text component
				lastComponent = textComponent;
			} else {
				//Otherwise, add it to our list of lines
				lines.add(textComponent);
			}
		}
		return Optional.empty();
	}

	public void addLinesTo(List<FormattedText> descriptionLinesExpanded) {
		descriptionLinesExpanded.addAll(lines);
		if (lastComponent != null) {
			descriptionLinesExpanded.add(lastComponent);
		}
	}
}
