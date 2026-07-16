package com.github.argon4w.acceleratedrendering.core.utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4i;

@Getter
@EqualsAndHashCode
public class Vertex {

	public static final int NORMAL_X_OFFSET = 0;
	public static final int NORMAL_Y_OFFSET = 8;
	public static final int NORMAL_Z_OFFSET = 16;

	private final Vector3f position;
	private final Vector2f texCoord;
	private final Vector4i color;
	private final Vector2i light;
	private final Vector2i overlay;
	private final Vector3f normal;

	public Vertex() {
		this.position	= new Vector3f();
		this.texCoord	= new Vector2f();
		this.color		= new Vector4i();
		this.light		= new Vector2i();
		this.overlay	= new Vector2i();
		this.normal		= new Vector3f();
	}

	public Vertex(Vertex vertex) {
		this.position	= new Vector3f(vertex.getPosition	());
		this.texCoord	= new Vector2f(vertex.getTexCoord	());
		this.color		= new Vector4i(vertex.getColor		());
		this.light		= new Vector2i(vertex.getLight		());
		this.overlay	= new Vector2i(vertex.getOverlay	());
		this.normal		= new Vector3f(vertex.getNormal		());
	}

	public int getPackedLight() {
		return light.x() | light.y() << 16;
	}

	public int getPackedOverlay() {
		return overlay.x() | overlay.y() << 16;
	}

	public int getPackedColor() {
		return FastColor.ARGB32.color(
				color.w(),
				color.x(),
				color.y(),
				color.z()
		);
	}

	public int getPackedNormal() {
		return		((byte) ((int) (Mth.clamp(normal.x(), -1.0f, 1.0f) * 127.0f) & 0xFF) << NORMAL_X_OFFSET)
				|	((byte) ((int) (Mth.clamp(normal.y(), -1.0f, 1.0f) * 127.0f) & 0xFF) << NORMAL_Y_OFFSET)
				|	((byte) ((int) (Mth.clamp(normal.z(), -1.0f, 1.0f) * 127.0f) & 0xFF) << NORMAL_Z_OFFSET);
	}
}
