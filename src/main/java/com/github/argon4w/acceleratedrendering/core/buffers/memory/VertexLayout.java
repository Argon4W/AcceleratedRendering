package com.github.argon4w.acceleratedrendering.core.buffers.memory;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.objects.*;

public class VertexLayout implements IMemoryLayout<VertexFormatElement> {

	private final long															size;
	private final Reference2IntMap		<VertexFormatElement>					offsets;
	private final Reference2ObjectMap	<VertexFormatElement, IMemoryInterface>	interfaces;

	public VertexLayout(VertexFormat vertexFormat) {
		var elements		= vertexFormat	.getElements();
		var count			= elements		.size		();

		this.size		= vertexFormat.getVertexSize			();
		this.offsets	= new Reference2IntOpenHashMap		<>	();
		this.interfaces	= new Reference2ObjectOpenHashMap	<>	();

		for (var i = 0; i < count; i ++) {
			var element		= elements		.get		(i);
			var offset		= vertexFormat	.getOffset	(i);

			this.interfaces	.put(element, new SimpleMemoryInterface(offset, size));
			this.offsets	.put(element, offset);
		}
	}

	@Override
	public IMemoryInterface getElement(VertexFormatElement element) {
		return interfaces.getOrDefault(element, NullMemoryInterface.INSTANCE);
	}

	@Override
	public int getElementOffset(VertexFormatElement element) {
		return offsets.getOrDefault(element, -1);
	}

	@Override
	public boolean containsElement(VertexFormatElement element) {
		return offsets.containsKey(element);
	}

	@Override
	public long getSize() {
		return size;
	}
}
