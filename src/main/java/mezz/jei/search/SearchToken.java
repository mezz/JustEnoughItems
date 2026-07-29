package mezz.jei.search;

import java.util.Objects;

public final class SearchToken {
	private final String text;
	private final boolean exclusion;

	public SearchToken(String text, boolean exclusion) {
		this.text = text;
		this.exclusion = exclusion;
	}

	public String getText() {
		return text;
	}

	public boolean isExclusion() {
		return exclusion;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SearchToken)) {
			return false;
		}
		SearchToken other = (SearchToken) obj;
		return exclusion == other.exclusion && text.equals(other.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text, exclusion);
	}
}
