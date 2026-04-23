package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

@ExtensionMethod(VertexConsumerExtension.class)
public abstract class AcceleratedVertexConsumerWrapper implements IAcceleratedVertexConsumer, VertexConsumer {

	@Override
	public		abstract VertexConsumer decorate	(VertexConsumer buffer);
	protected	abstract VertexConsumer getDelegate	();

	@Override
	public void beginTransform(Matrix4f transform, Matrix3f normal) {
		getDelegate				()
				.getAccelerated	()
				.beginTransform	(transform, normal);
	}

	@Override
	public void endTransform() {
		getDelegate()
				.getAccelerated	()
				.endTransform	();
	}

	@Override
	public boolean isAccelerated() {
		return getDelegate		()
				.getAccelerated	()
				.isAccelerated	();
	}

	@Override
	public RenderType getRenderType() {
		return getDelegate		()
				.getAccelerated	()
				.getRenderType	();
	}

	@Override
	public VertexLayout getLayout() {
		return getDelegate		()
				.getAccelerated	()
				.getLayout		();
	}

	@Override
	public int getPolygonSize() {
		return getDelegate		()
				.getAccelerated	()
				.getPolygonSize	();
	}

	@Override
	public void addClientMesh(
			ByteBuffer meshBuffer,
			int			size,
			int			color,
			int			light,
			int			overlay
	) {
		getDelegate				()
				.getAccelerated	()
				.addClientMesh	(
						meshBuffer,
						size,
						color,
						light,
						overlay
				);
	}

	@Override
	public void addServerMesh(
			ServerMesh	serverMesh,
			int			color,
			int			light,
			int			overlay
	) {
		getDelegate				()
				.getAccelerated	()
				.addServerMesh	(
						serverMesh,
						color,
						light,
						overlay
				);
	}

	@Override
	public <T> void doRender(
			IAcceleratedRenderer<T> renderer,
			T						context,
			Matrix4f				transform,
			Matrix3f				normal,
			int						light,
			int						overlay,
			int						color
	) {
		renderer.render(
				this,
				context,
				transform,
				normal,
				light,
				overlay,
				color
		);
	}

	@Override
	public void endVertex() {
		getDelegate().endVertex();
	}

	@Override
	public void unsetDefaultColor() {
		getDelegate().unsetDefaultColor();
	}

	@Override
	public void defaultColor(
			int defaultR,
			int defaultG,
			int defaultB,
			int defaultA
	) {
		getDelegate().defaultColor(
				defaultR,
				defaultG,
				defaultB,
				defaultA
		);
	}

	@Override
	public VertexConsumer vertex(
			double x,
			double y,
			double z
	) {
		getDelegate().vertex(
				x,
				y,
				z
		);
		return this;
	}

	@Override
	public VertexConsumer color(
			int red,
			int green,
			int blue,
			int alpha
	) {
		getDelegate().color(
				red,
				green,
				blue,
				alpha
		);
		return this;
	}

	@Override
	public VertexConsumer uv(float u, float v) {
		getDelegate().uv(u, v);
		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		getDelegate().overlayCoords(u, v);
		return this;
	}

	@Override
	public VertexConsumer uv2(int u, int v) {
		getDelegate().uv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer normal(
			float normalX,
			float normalY,
			float normalZ
	) {
		getDelegate().normal(
				normalX,
				normalY,
				normalZ
		);
		return this;
	}

	@Override
	public void vertex(
			float	x,
			float	y,
			float	z,
			float	red,
			float	green,
			float	blue,
			float	alpha,
			float	u,
			float	v,
			int		packedOverlay,
			int		packedLight,
			float	normalX,
			float	normalY,
			float	normalZ
	) {
		getDelegate().vertex(
				x,
				y,
				z,
				red,
				green,
				blue,
				alpha,
				u,
				v,
				packedOverlay,
				packedLight,
				normalX,
				normalY,
				normalZ
		);
	}
}
