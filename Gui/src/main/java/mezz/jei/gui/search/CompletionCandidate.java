package mezz.jei.gui.search;

import net.minecraft.network.chat.Component;

public record CompletionCandidate(
	String insertion,
	String displayName,
	Component description,
	CandidateCategory category
) {}
