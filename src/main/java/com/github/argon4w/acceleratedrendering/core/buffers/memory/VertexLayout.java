package com.github.argon4w.acceleratedrendering.core.buffers.memory;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class VertexLayout implements IMemoryLayout<VertexFormatElement> {

	private final long					size;
	private final int					mask;
	private final int				[]	offsets;
	private final IMemoryInterface	[]	interfaces;

	public VertexLayout(VertexFormat vertexFormat) {
		var offsets	= vertexFormat	.getOffsetsByElement();
		var count	= offsets		.length;

		this.size = vertexFormat.getVertexSize	();
		this.mask = vertexFormat.getElementsMask();

		this.offsets	= new int				[count];
		this.interfaces	= new IMemoryInterface	[count];

		for (var i = 0; i < count; i ++) {
			var offset = offsets[i];

			this.interfaces	[i]	= offset == -1 ? NullMemoryInterface.INSTANCE : new SimpleMemoryInterface(offset, size);
			this.offsets	[i]	= offset;
		}
	}

	@Override
	public IMemoryInterface getElement(VertexFormatElement element) {
		return interfaces[element.id()];
	}

	@Override
	public int getElementOffset(VertexFormatElement element) {
		return offsets[element.id()];
	}

	@Override
	public boolean containsElement(VertexFormatElement element) {
		return (mask & element.mask()) != 0;
	}

	@Override
	public long getSize() {
		return size;
	}
}
