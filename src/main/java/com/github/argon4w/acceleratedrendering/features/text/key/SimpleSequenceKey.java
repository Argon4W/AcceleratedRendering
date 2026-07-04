package com.github.argon4w.acceleratedrendering.features.text.key;

import com.github.argon4w.acceleratedrendering.core.utils.FastUtils;
import com.github.argon4w.acceleratedrendering.features.text.extensions.StyleExtension;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.experimental.ExtensionMethod;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

// Inspired by Modern UI's FormattedLayoutKey: https://github.com/BloCamLimb/ModernUI-MC/blob/1.21.1/common/src/main/java/icyllis/modernui/mc/text/FormattedLayoutKey.java
// Special thanks to BloCamLimb.
@ExtensionMethod(StyleExtension.class)
public class SimpleSequenceKey implements ISequenceKey {

	public static final int HIDDEN_BIT = 1 << 21;

	private final	IntArrayList		texts;
	private final	ResourceLocation	font;
	private final	int					flag;
	private final	float				advance;

	private			int					hashCode;
	private			boolean				cached;

	public SimpleSequenceKey(
			IntArrayList		texts,
			ResourceLocation	font,
			int					flag,
			float				advance
	) {
		this.texts		= texts;
		this.font		= font;
		this.flag		= flag;
		this.advance	= advance;

		this.hashCode	= -1;
		this.cached		= false;
	}

	@Override
	public IntArrayList getTexts() {
		return texts;
	}

	@Override
	public ResourceLocation getFont() {
		return font;
	}

	@Override
	public float getAdvance() {
		return advance;
	}

	@Override
	public int getColor() {
		return (flag & 0xFFFFFF);
	}

	@Override
	public boolean hasColor() {
		return (flag & StyleExtension.COLOR_BIT) != 0;
	}

	@Override
	public boolean isBold() {
		return (flag & StyleExtension.BOLD_BIT) != 0;
	}

	@Override
	public boolean isItalic() {
		return (flag & StyleExtension.ITALIC_BIT) != 0;
	}

	@Override
	public boolean isStrikethrough() {
		return (flag & StyleExtension.STRIKETHROUGH_BIT) != 0;
	}

	@Override
	public boolean isUnderlined() {
		return (flag & StyleExtension.UNDERLINE_BIT) != 0;
	}

	@Override
	public boolean isShadow() {
		return (flag & StyleExtension.SHADOW_BIT) != 0;
	}

	@Override
	public boolean isOutline() {
		return (flag & StyleExtension.OUTLINE_BIT) != 0;
	}

	@Override
	public ISequenceKey bake() {
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o instanceof Mutable that
				&&					this.flag ==	that.flag
				&& Objects	.equals(this.font,		that.font)
				&& FastUtils.equals(this.texts,		that.texts)
		) {
			return true;
		}

		return o instanceof SimpleSequenceKey that
				&&					this.flag ==	that.flag
				&& Objects	.equals(this.font,		that.font)
				&& FastUtils.equals(this.texts,		that.texts);
	}

	@Override
	public int hashCode() {
		if (cached) {
			return hashCode;
		}

		cached = true;

		hashCode = FastUtils.hashCode(texts);

		hashCode = 31 * hashCode + Objects.hashCode(font);
		hashCode = 31 * hashCode + Integer.hashCode(flag);

		return hashCode;
	}

	public static boolean isHidden(int data) {
		return (data & HIDDEN_BIT) != 0;
	}

	public static class Mutable implements ISequenceKey {

		private final	IntArrayList		texts;
		private			ResourceLocation	font;
		private			int					flag;
		private			int					hashCode;
		private			float				advance;
		private			boolean				cached;

		public Mutable() {
			this.texts		= new IntArrayList();
			this.font		= null;
			this.flag		= 0;
			this.hashCode	= 0;
			this.advance	= 0.0f;
			this.cached		= false;
		}

		public void reset() {
			this.texts.clear();

			this.flag		= 0;
			this.hashCode	= 0;
			this.advance	= 0.0f;
			this.cached		= false;
		}

		public void setStyle(
				Style	style,
				boolean	shadow,
				boolean	outline
		) {
			this.font = style.getFont();
			this.flag = style.getFlag(shadow, outline);
		}

		public void addCodePoint(int codePoint) {
			this.texts.add(codePoint);
		}

		public void addHidden(int codePoint) {
			this.texts.add(codePoint | HIDDEN_BIT);
		}

		public void addAdvance(float advance) {
			this.advance += advance;
		}

		public boolean isEmpty() {
			return this.texts.isEmpty();
		}

		@Override
		public IntArrayList getTexts() {
			return texts;
		}

		@Override
		public ResourceLocation getFont() {
			return font;
		}

		@Override
		public float getAdvance() {
			return advance;
		}

		@Override
		public int getColor() {
			return (flag & 0xFFFFFF);
		}

		@Override
		public boolean hasColor() {
			return (flag & StyleExtension.COLOR_BIT) != 0;
		}

		@Override
		public boolean isBold() {
			return (flag & StyleExtension.BOLD_BIT) != 0;
		}

		@Override
		public boolean isItalic() {
			return (flag & StyleExtension.ITALIC_BIT) != 0;
		}

		@Override
		public boolean isStrikethrough() {
			return (flag & StyleExtension.STRIKETHROUGH_BIT) != 0;
		}

		@Override
		public boolean isUnderlined() {
			return (flag & StyleExtension.UNDERLINE_BIT) != 0;
		}

		@Override
		public boolean isShadow() {
			return (flag & StyleExtension.SHADOW_BIT) != 0;
		}

		@Override
		public boolean isOutline() {
			return (flag & StyleExtension.OUTLINE_BIT) != 0;
		}

		@Override
		public SimpleSequenceKey bake() {
			return new SimpleSequenceKey(
					this.texts.clone(),
					this.font,
					this.flag,
					this.advance
			);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}

			if (o instanceof Mutable that
					&&					this.flag ==	that.flag
					&& Objects	.equals(this.font,		that.font)
					&& FastUtils.equals(this.texts,		that.texts)
			) {
				return true;
			}

			return o instanceof SimpleSequenceKey that
					&&					this.flag ==	that.flag
					&& Objects	.equals(this.font,		that.font)
					&& FastUtils.equals(this.texts,		that.texts);
		}

		@Override
		public int hashCode() {
			if (cached) {
				return hashCode;
			}

			cached = true;

			hashCode = FastUtils.hashCode(texts);

			hashCode = 31 * hashCode + Objects.hashCode(font);
			hashCode = 31 * hashCode + Integer.hashCode(flag);

			return hashCode;
		}
	}
}
