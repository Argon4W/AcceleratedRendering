package com.github.argon4w.acceleratedrendering.features.text.cache;

import com.github.argon4w.acceleratedrendering.core.utils.FastUtils;
import com.github.argon4w.acceleratedrendering.features.text.key.SimpleSequenceKey;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import net.minecraft.client.gui.Font.DisplayMode;

import java.util.Objects;

public sealed interface IFormattedTextKey permits IFormattedTextKey.Immutable, IFormattedTextKey.Mutable {

	IFormattedTextKey bake();

	final class Immutable implements IFormattedTextKey {

		private final	ISeekableFormattedText	text;
		private final	DisplayMode				mode;
		private final	boolean					shadow;
		private			boolean					cached;
		private			int						hashCode;

		public Immutable(
				ISeekableFormattedText	text,
				DisplayMode				mode,
				boolean					shadow
		) {
			this.text		= text;
			this.mode		= mode;
			this.shadow		= shadow;
			this.cached		= false;
			this.hashCode	= 0;
		}

		@Override
		public IFormattedTextKey bake() {
			return this;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}

			if (o instanceof IFormattedTextKey.Mutable that
					&&					this.shadow	==	that.shadow
					&&					this.mode	==	that.mode
					&& Objects.equals(	this.text,		that.text)
			) {
				return true;
			}

			return o instanceof IFormattedTextKey.Immutable that
					&&					this.shadow	==	that.shadow
					&&					this.mode	==	that.mode
					&& Objects.equals(	this.text,		that.text);
		}

		@Override
		public int hashCode() {
			if (cached) {
				return hashCode;
			}

			cached = true;

			hashCode =					Objects.hashCode(text);
			hashCode = 31 * hashCode +	Objects.hashCode(mode);
			hashCode = 31 * hashCode +	Boolean.hashCode(shadow);

			return hashCode;
		}
	}

	@Setter
	final class Mutable implements IFormattedTextKey {

		private ISeekableFormattedText	text;
		private DisplayMode				mode;
		private boolean					shadow;

		@Override
		public IFormattedTextKey bake() {
			return new Immutable(
					text,
					mode,
					shadow
			);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}

			if (o instanceof IFormattedTextKey.Mutable that
					&&					this.shadow	==	that.shadow
					&&					this.mode	==	that.mode
					&& Objects.equals(	this.text,		that.text)
			) {
				return true;
			}

			return o instanceof IFormattedTextKey.Immutable that
					&&					this.shadow	==	that.shadow
					&&					this.mode	==	that.mode
					&& Objects.equals(	this.text,		that.text);
		}

		@Override
		public int hashCode() {

			var result = Objects.hashCode(text);

			result = 31 * result + Objects.hashCode(mode);
			result = 31 * result + Boolean.hashCode(shadow);

			return result;
		}

		public void reset() {
			text	= null;
			mode	= null;
			shadow	= false;
		}
	}
}
