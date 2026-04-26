package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedRingBuffers;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IElementPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.functions.ILayerFunction;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.StagingBufferPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.*;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.IPolygonProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.ITransformShaderProgramOverride;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.IUploadingShaderProgramOverride;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.LongSupplier;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AcceleratedBufferBuilder implements IAcceleratedVertexConsumer, VertexConsumer, LongSupplier {

	public static								final	long											SHARING_SIZE		= 4L * 4L * 4L + 4L * 3L * 4L;
	public static								final	IMemoryInterface								SHARING_TRANSFORM	= new SimpleMemoryInterface			(0L,			SHARING_SIZE);
	public static								final	IMemoryInterface								SHARING_NORMAL		= new SimpleMemoryInterface			(4L * 4L * 4L,	SHARING_SIZE);

	@Getter public								final	IMemoryInterface								varyingOffset;
	@Getter public								final	IMemoryInterface								varyingSharing;
	@Getter public								final	IMemoryInterface								varyingMesh;
	@Getter public								final	IMemoryInterface								varyingShouldCull;

	@Getter private								final	Map<ServerMesh, MeshUploaderPool.MeshUploader>	meshUploaders;
	@Getter private								final	StagingBufferPool		.StagingBuffer			vertexBuffer;
	@Getter private								final	StagingBufferPool		.StagingBuffer			varyingBuffer;
	@Getter private								final	IElementPool			.IElementSegment		elementSegment;
	@Getter private								final	AcceleratedRingBuffers	.Buffers				buffer;
	@Getter private								final	ILayerFunction									function;


	@EqualsAndHashCode.Include private			final	RenderType										renderType;
	@Getter @EqualsAndHashCode.Include private	final	VertexLayout									layout;
	@Getter private								final	IPolygonProgramDispatcher						polygonProgramDispatcher;
	@Getter private								final	ICullingProgramDispatcher						cullingProgramDispatcher;
	@Getter private								final	ITransformShaderProgramOverride					transformOverride;
	@Getter private								final	IUploadingShaderProgramOverride					uploadingOverride;
	@Getter private								final	VertexFormat.Mode								mode;
	@Getter private								final	int												polygonSize;
	@Getter private								final	int												polygonElementCount;
	@Getter private								final	long											vertexSize;

	private										final	IMemoryInterface								posOffset;
	@Getter private								final	IMemoryInterface								colorOffset;
	private										final	IMemoryInterface								uv0Offset;
	@Getter private								final	IMemoryInterface								uv1Offset;
	@Getter private								final	IMemoryInterface								uv2Offset;
	private										final	IMemoryInterface								normalOffset;

	private 									final	Matrix4f										cachedTransformValue;
	private										final	Matrix3f										cachedNormalValue;

	private												int												elementCount;
	@Getter private										int												meshVertexCount;
	@Getter private										int												vertexCount;
	private												long											vertexAddress;
	private												long											sharingAddress;
	private												int												activeSharing;
	private												int												cachedSharing;
	@Getter private		 								boolean											outdated;

	private												Matrix4f										cachedTransform;
	private												Matrix3f										cachedNormal;

	public AcceleratedBufferBuilder(
			StagingBufferPool		.StagingBuffer		vertexBuffer,
			StagingBufferPool		.StagingBuffer		varyingBuffer,
			IElementPool			.IElementSegment	elementSegment,
			AcceleratedRingBuffers	.Buffers			buffer,
			ILayerFunction								layerFunction,
			RenderType									renderType
	) {
		var environment					= buffer.getEnvironment();

		this.varyingOffset				= new SimpleDynamicMemoryInterface						(0L * 4L, this);
		this.varyingSharing				= new SimpleDynamicMemoryInterface						(1L * 4L, this);
		this.varyingMesh				= new SimpleDynamicMemoryInterface						(2L * 4L, this);
		this.varyingShouldCull			= new SimpleDynamicMemoryInterface						(3L * 4L, this);

		this.meshUploaders				= new Reference2ObjectLinkedOpenHashMap<>				();
		this.vertexBuffer				= vertexBuffer;
		this.varyingBuffer				= varyingBuffer;
		this.elementSegment				= elementSegment;
		this.buffer						= buffer;
		this.function					= layerFunction;

		this.renderType					= renderType;
		this.layout						= environment		.getLayout							();
		this.polygonProgramDispatcher	= environment		.selectProcessingProgramDispatcher	(this.renderType.mode);
		this.cullingProgramDispatcher	= environment		.selectCullingProgramDispatcher		(this.renderType);
		this.transformOverride			= environment		.getTransformProgramOverride		(this.renderType);
		this.uploadingOverride			= environment		.getUploadingProgramOverride		(this.renderType);

		this.mode						= this.renderType	.mode;
		this.polygonSize				= this.mode			.primitiveLength;
		this.polygonElementCount		= this.mode			.indexCount							(this.polygonSize);
		this.vertexSize					= this.buffer		.getVertexSize						();

		this.posOffset					= this.layout		.getElement							(VertexFormatElement.POSITION);
		this.colorOffset				= this.layout		.getElement							(VertexFormatElement.COLOR);
		this.uv0Offset					= this.layout		.getElement							(VertexFormatElement.UV0);
		this.uv1Offset					= this.layout		.getElement							(VertexFormatElement.UV1);
		this.uv2Offset					= this.layout		.getElement							(VertexFormatElement.UV2);
		this.normalOffset				= this.layout		.getElement							(VertexFormatElement.NORMAL);

		this.cachedTransformValue		= new Matrix4f();
		this.cachedNormalValue			= new Matrix3f();

		this.elementCount				= 0;
		this.meshVertexCount			= 0;
		this.vertexCount				= 0;
		this.vertexAddress				= -1;
		this.sharingAddress				= -1;
		this.activeSharing				= -1;
		this.cachedSharing				= -1;

		this.cachedTransform			= null;
		this.cachedNormal				= null;
	}

	@Override
	public VertexConsumer addVertex(
			PoseStack.Pose	pPose,
			float			pX,
			float			pY,
			float			pZ
	) {
		beginTransform(
				pPose.pose	(),
				pPose.normal()
		);

		return addVertex(
				pX,
				pY,
				pZ
		);
	}

	@Override
	public VertexConsumer addVertex(
			float pX,
			float pY,
			float pZ
	) {
		var vertexAddress	= vertexBuffer	.reserve(getVertexSize	());
		var varyingAddress	= varyingBuffer	.reserve(getVaryingSize	());

		this.vertexAddress	= vertexAddress;

		posOffset			.putFloat		(vertexAddress + 0L,	pX);
		posOffset			.putFloat		(vertexAddress + 4L,	pY);
		posOffset			.putFloat		(vertexAddress + 8L,	pZ);

		varyingOffset		.putInt			(varyingAddress,		0);
		varyingSharing		.putInt			(varyingAddress,		activeSharing);
		varyingMesh			.putInt			(varyingAddress,		-1);
		varyingShouldCull	.putInt			(varyingAddress,		cullingProgramDispatcher.shouldCull() ? 1 : 0);
		transformOverride	.uploadVarying	(vertexAddress,			0);

		vertexCount		++;
		elementCount	++;

		if (elementCount >= polygonSize) {
			elementCount	= 0;
			activeSharing	= -1;

			elementSegment.count(polygonElementCount);
		}

		return this;
	}

	@Override
	public VertexConsumer setColor(
			int pRed,
			int pGreen,
			int pBlue,
			int pAlpha
	) {
		if (vertexAddress == -1) {
			throw new IllegalStateException("Vertex not building!");
		}

		colorOffset.putByte(vertexAddress + 0L, (byte) pRed);
		colorOffset.putByte(vertexAddress + 1L, (byte) pGreen);
		colorOffset.putByte(vertexAddress + 2L, (byte) pBlue);
		colorOffset.putByte(vertexAddress + 3L, (byte) pAlpha);

		return this;
	}

	@Override
	public VertexConsumer setUv(float pU, float pV) {
		if (vertexAddress == -1) {
			throw new IllegalStateException("Vertex not building!");
		}

		uv0Offset.putFloat(vertexAddress + 0L, pU);
		uv0Offset.putFloat(vertexAddress + 4L, pV);

		return this;
	}

	@Override
	public VertexConsumer setUv1(int pU, int pV) {
		if (vertexAddress == -1) {
			throw new IllegalStateException("Vertex not building!");
		}

		uv1Offset.putShort(vertexAddress + 0L, (short) pU);
		uv1Offset.putShort(vertexAddress + 2L, (short) pV);

		return this;
	}

	@Override
	public VertexConsumer setUv2(int pU, int pV) {
		if (vertexAddress == -1) {
			throw new IllegalStateException("Vertex not building!");
		}

		uv2Offset.putShort(vertexAddress + 0L, (short) pU);
		uv2Offset.putShort(vertexAddress + 2L, (short) pV);

		return this;
	}

	@Override
	public VertexConsumer setNormal(
			PoseStack.Pose	pPose,
			float			pNormalX,
			float			pNormalY,
			float			pNormalZ
	) {
		var normal = pPose.normal();

		if (activeSharing == -1) {
			return VertexConsumer.super.setNormal(
					pPose,
					pNormalX,
					pNormalY,
					pNormalZ
			);
		}

		if (!normal.equals(cachedNormal)) {
			SHARING_NORMAL.putMatrix3f(sharingAddress, normal);
		}

		return setNormal(
				pNormalX,
				pNormalY,
				pNormalZ
		);
	}

	@Override
	public VertexConsumer setNormal(
			float pNormalX,
			float pNormalY,
			float pNormalZ
	) {
		if (vertexAddress == -1) {
			throw new IllegalStateException("Vertex not building!");
		}

		normalOffset.putNormal(vertexAddress + 0L, pNormalX);
		normalOffset.putNormal(vertexAddress + 1L, pNormalY);
		normalOffset.putNormal(vertexAddress + 2L, pNormalZ);

		return this;
	}

	@Override
	public void addVertex(
			float	pX,
			float	pY,
			float	pZ,
			int		pColor,
			float	pU,
			float	pV,
			int		pPackedOverlay,
			int		pPackedLight,
			float	pNormalX,
			float	pNormalY,
			float	pNormalZ
	) {
		var vertexAddress	= vertexBuffer	.reserve(getVertexSize	());
		var varyingAddress	= varyingBuffer	.reserve(getVaryingSize	());

		posOffset			.putFloat		(vertexAddress + 0L,	pX);
		posOffset			.putFloat		(vertexAddress + 4L,	pY);
		posOffset			.putFloat		(vertexAddress + 8L,	pZ);
		colorOffset			.putInt			(vertexAddress,			FastColor.ABGR32.fromArgb32(pColor));
		uv0Offset			.putFloat		(vertexAddress + 0L,	pU);
		uv0Offset			.putFloat		(vertexAddress + 4L,	pV);
		uv1Offset			.putInt			(vertexAddress,			pPackedOverlay);
		uv2Offset			.putInt			(vertexAddress,			pPackedLight);
		normalOffset		.putNormal		(vertexAddress + 0L,	pNormalX);
		normalOffset		.putNormal		(vertexAddress + 1L,	pNormalY);
		normalOffset		.putNormal		(vertexAddress + 2L,	pNormalZ);

		varyingOffset		.putInt			(varyingAddress,		0);
		varyingSharing		.putInt			(varyingAddress,		activeSharing);
		varyingMesh			.putInt			(varyingAddress,		-1);
		varyingShouldCull	.putInt			(varyingAddress,		cullingProgramDispatcher.shouldCull() ? 1 : 0);
		transformOverride	.uploadVarying	(varyingAddress,		0);

		vertexCount		++;
		elementCount	++;

		if (elementCount >= polygonSize) {
			elementCount	= 0;
			activeSharing	= -1;

			elementSegment.count(polygonElementCount);
		}
	}

	@Override
	public void beginTransform(Matrix4f transform, Matrix3f normal) {
		if (		CoreFeature	.shouldCacheIdenticalPose	()
				&&	transform	.equals						(cachedTransform)
				&&	normal		.equals						(cachedNormal)
		) {
			activeSharing = cachedSharing;
			return;
		}

		cachedTransform	= cachedTransformValue	.set			(transform);
		cachedNormal	= cachedNormalValue		.set			(normal);

		sharingAddress	= buffer				.reserveSharing	();
		cachedSharing	= buffer				.getSharing		();
		activeSharing	= cachedSharing;

		SHARING_TRANSFORM	.putMatrix4f(sharingAddress, transform);
		SHARING_NORMAL		.putMatrix3f(sharingAddress, normal);
	}

	@Override
	public void endTransform() {
		cachedTransform	= null;
		cachedNormal	= null;
		activeSharing	= -1;
		cachedSharing	= -1;
	}

	@Override
	public void addClientMesh(
			ByteBuffer	meshBuffer,
			int			size,
			int			color,
			int			light,
			int			overlay
	) {
		var bufferSize		= vertexSize * size;
		var vertexAddress	= vertexBuffer	.reserve(bufferSize);
		var varyingAddress	= varyingBuffer	.reserve(getVaryingSize() * size);

		MemoryUtil.memCopy(
				MemoryUtil.memAddress0(meshBuffer),
				vertexAddress,
				bufferSize
		);

		colorOffset			.putInt			(vertexAddress,		FastColor.ABGR32.fromArgb32(color));
		uv1Offset			.putInt			(vertexAddress,		overlay);
		uv2Offset			.putInt			(vertexAddress,		light);

		varyingSharing		.putInt			(varyingAddress,	activeSharing);
		varyingMesh			.putInt			(varyingAddress,	-1);
		varyingShouldCull	.putInt			(varyingAddress,	cullingProgramDispatcher.shouldCull() ? 1 : 0);
		transformOverride	.uploadVarying	(varyingAddress,	0);

		for (var i = 0; i < size; i ++) {
			varyingOffset.at(i).putInt(varyingAddress, i);
		}

		elementSegment.count(mode.indexCount(size));
		vertexCount += size;
	}

	@Override
	public void addServerMesh(
			ServerMesh	serverMesh,
			int			color,
			int			light,
			int			overlay
	) {
		if (CoreFeature.shouldUploadMeshImmediately()) {
			var meshSize		= serverMesh	.size	();
			var vertexAddress	= vertexBuffer	.reserve(getVertexSize	() * meshSize);
			var varyingAddress	= varyingBuffer	.reserve(getVaryingSize	() * meshSize);

			colorOffset			.putInt			(vertexAddress,		FastColor.ABGR32.fromArgb32(color));
			uv1Offset			.putInt			(vertexAddress,		overlay);
			uv2Offset			.putInt			(vertexAddress,		light);

			varyingSharing		.putInt			(varyingAddress,	activeSharing);
			varyingMesh			.putInt			(varyingAddress,	serverMesh				.offset		());
			varyingShouldCull	.putInt			(varyingAddress,	cullingProgramDispatcher.shouldCull	() ? 1 : 0);
			transformOverride	.uploadVarying	(varyingAddress,	0);

			for (var i = 0; i < meshSize; i ++) {
				varyingOffset.at(i).putInt(varyingAddress, i);
			}

			elementSegment.count(mode.indexCount(meshSize));
			vertexCount += meshSize;

			return;
		}

		var meshSize		= serverMesh	.size	();
		var meshUploader	= meshUploaders	.get	(serverMesh);

		if (meshUploader == null) {
			meshUploader = buffer	.getMeshUploader		();
			meshUploader			.setBufferBuilder		(this);
			meshUploader			.setServerMesh			(serverMesh);
			meshUploader			.setUploadingOverride	(uploadingOverride);
			meshUploaders			.put					(serverMesh, meshUploader);
		}

		elementSegment.count(mode.indexCount(meshSize));
		meshVertexCount += meshSize;

		meshUploader.addUpload(
				color,
				light,
				overlay,
				activeSharing,
				cullingProgramDispatcher.shouldCull() ? 1 : 0
		);
	}

	@Override
	public <T> void doRender(
			IAcceleratedRenderer<T>	renderer,
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
	public VertexConsumer decorate(VertexConsumer buffer) {
		return buffer;
	}

	@Override
	public boolean isAccelerated() {
		return true;
	}

	@Override
	public RenderType getRenderType() {
		return renderType;
	}

	@Override
	public long getAsLong() {
		return getVaryingSize();
	}

	public long getVaryingSize() {
		return transformOverride.getVaryingSize();
	}

	public long getVertexCountOffset() {
		return vertexBuffer.getOffset() / getVertexSize();
	}

	public long getVaryingCountOffset() {
		return varyingBuffer.getOffset() / getVaryingSize();
	}

	public int getTotalVertexCount() {
		return vertexCount + meshVertexCount;
	}

	public boolean isEmpty() {
		return getTotalVertexCount() == 0;
	}

	public void setOutdated() {
		outdated = true;
	}
}
