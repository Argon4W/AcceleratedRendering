package com.github.argon4w.acceleratedrendering.core.meshes;

import com.github.argon4w.acceleratedrendering.core.backends.GLConstants;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MutableBuffer;
import lombok.Getter;

import static org.lwjgl.opengl.GL46.*;

@Getter
public class MeshBuffer extends MutableBuffer {

	protected long position;
	protected long current;

	public MeshBuffer(long initialSize) {
		super(initialSize,	GL_DYNAMIC_STORAGE_BIT);

		this.position	= 0L;
		this.current	= 0L;
	}

	public long append(
			long	dataAddress,
			long	dataBytes,
			boolean	occupied
	) {
		var oldPosition = this.position;
		var newPosition = oldPosition + dataBytes;

		if (occupied) {
			this.current	= oldPosition;
			this.position	= newPosition;
		}

		if (newPosition > size) {
			resize(newPosition);
		}

		data(
				oldPosition,
				dataBytes,
				dataAddress
		);

		return (int) oldPosition;
	}

	public long append(long dataAddress, long dataBytes) {
		return append(
				dataAddress,
				dataBytes,
				true
		);
	}

	public void reset() {
		position = 0;
	}

	public boolean overflow(long bytes) {
		return position + bytes >= GLConstants.MAX_SHADER_STORAGE_BLOCK_SIZE;
	}
}
