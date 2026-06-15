package com.github.argon4w.acceleratedrendering.core.programs.dispatchers.meshes;

import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import lombok.Getter;

import java.util.Arrays;

@SuppressWarnings("unchecked")
public abstract class MeshSparseMap<T> {

	public static final int SPARSE_BLOCK_SIZE			= 256;
	public static final int DEFAULT_SPARSE_ARRAY_SIZE	= 1024;
	public static final int DEFAULT_DENSE_ARRAY_SIZE	= 8;

	@Getter private	int				layerSize;
	@Getter private	Object	[][]	denseObj;
	@Getter private	int		[]		denseHead;
	private			int		[][]	denseIdx;
	private			boolean	[][]	denseUsed;
	private			int		[]		denseSize;
	private			int		[][][]	sparseIdx;
	private			int		[][]	sparseNum;
	private			int		[]		sparseSize;

	public MeshSparseMap() {
		this.layerSize	= 16;
		this.denseObj	= new Object	[this.layerSize][];
		this.denseIdx	= new int		[this.layerSize][];
		this.denseUsed	= new boolean	[this.layerSize][];
		this.denseSize	= new int		[this.layerSize];
		this.denseHead	= new int		[this.layerSize];
		this.sparseIdx	= new int		[this.layerSize][][];
		this.sparseNum	= new int		[this.layerSize][];
		this.sparseSize	= new int		[this.layerSize];

		for (var meshLayer = 0; meshLayer < this.layerSize; meshLayer ++) {
			setupLayer(meshLayer);
		}
	}

	public abstract T		create	(ServerMesh	mesh);
	public abstract void	remove	(T			object);
	public abstract void	clear	(T			object);

	private void setupLayer(int meshLayer) {
		denseObj	[meshLayer] = new Object	[DEFAULT_DENSE_ARRAY_SIZE];
		denseIdx	[meshLayer] = new int		[DEFAULT_DENSE_ARRAY_SIZE];
		denseUsed	[meshLayer] = new boolean	[DEFAULT_DENSE_ARRAY_SIZE];
		sparseIdx	[meshLayer] = new int		[DEFAULT_SPARSE_ARRAY_SIZE][];
		sparseNum	[meshLayer] = new int		[DEFAULT_SPARSE_ARRAY_SIZE];

		sparseSize	[meshLayer] = DEFAULT_SPARSE_ARRAY_SIZE;
		denseSize	[meshLayer] = DEFAULT_DENSE_ARRAY_SIZE;
		denseHead	[meshLayer] = 0;
	}

	private void resizeLayer(int highestLayer) {
		var newLayerSize = size(highestLayer);

		denseObj	= Arrays.copyOf(denseObj,	newLayerSize);
		denseIdx	= Arrays.copyOf(denseIdx,	newLayerSize);
		denseUsed	= Arrays.copyOf(denseUsed,	newLayerSize);
		sparseIdx	= Arrays.copyOf(sparseIdx,	newLayerSize);
		sparseNum	= Arrays.copyOf(sparseNum,	newLayerSize);
		sparseSize	= Arrays.copyOf(sparseSize,	newLayerSize);
		denseSize	= Arrays.copyOf(denseSize,	newLayerSize);
		denseHead	= Arrays.copyOf(denseHead,	newLayerSize);

		for (var layer = layerSize; layer < newLayerSize; layer ++) {
			setupLayer(layer);
		}

		layerSize = newLayerSize;
	}

	private void resizeSparse(int meshLayer, int highestBlock) {
		var newSparseSize = size(highestBlock);

		sparseIdx	[meshLayer] = Arrays.copyOf(sparseIdx[meshLayer], newSparseSize);
		sparseNum	[meshLayer] = Arrays.copyOf(sparseNum[meshLayer], newSparseSize);
		sparseSize	[meshLayer] = newSparseSize;
	}

	private void resizeDense(int meshLayer, int highestDenseId) {
		var newDenseSize = size(highestDenseId);

		denseUsed	[meshLayer] = Arrays.copyOf(denseUsed	[meshLayer], newDenseSize);
		denseIdx	[meshLayer] = Arrays.copyOf(denseIdx	[meshLayer], newDenseSize);
		denseObj	[meshLayer] = Arrays.copyOf(denseObj	[meshLayer], newDenseSize);
		denseSize	[meshLayer] = newDenseSize;
	}

	public T get(ServerMesh mesh) {
		var meshLayer	= mesh.meshLayer();
		var meshId		= mesh.meshId	();

		if (layerSize <= meshLayer) {
			resizeLayer(meshLayer);
		}

		var meshBlock = meshId / SPARSE_BLOCK_SIZE;
		var meshIndex = meshId % SPARSE_BLOCK_SIZE;

		var layerSize = sparseSize[meshLayer];

		if (layerSize <= meshBlock) {
			resizeSparse(meshLayer, meshBlock);
		}

		var blocks	= sparseIdx[meshLayer];
		var counts	= sparseNum[meshLayer];
		var used	= denseUsed[meshLayer];

		var block = blocks[meshBlock];

		if (block == null) {
			block = blocks[meshBlock] = new int[SPARSE_BLOCK_SIZE];
		}

		var denseId	= block[meshIndex];

		if (denseId == 0) {
			denseId	= denseHead[meshLayer] ++;

			var size	= denseSize	[meshLayer];
			var object	= create	(mesh);

			if (size <= denseId) {
				resizeDense(meshLayer, denseId);
			}

			denseIdx[meshLayer][denseId] = meshId;
			denseObj[meshLayer][denseId] = object;

			block	[meshIndex] = denseId + 1;
			counts	[meshBlock] ++;

			return object;
		}

		used[denseId - 1] = true;

		return (T) denseObj[meshLayer][denseId - 1];
	}

	public void clear() {
		for (var meshLayer = 0; meshLayer < layerSize; meshLayer ++) {
			for (int denseId = 0, denseSize = denseHead[meshLayer]; denseId < denseSize; denseId ++) {
				clear((T) denseObj[meshLayer][denseId]);
			}
		}
	}

	public void remove() {
		for (var meshLayer = 0; meshLayer < layerSize; meshLayer ++) {
			for (int denseSize = denseHead[meshLayer], removeDenseId = denseSize - 1; removeDenseId >= 0; removeDenseId --) {
				var tailDenseId		= denseHead	[meshLayer] - 1;
				var layerDenseIdx	= denseIdx	[meshLayer];
				var layerDenseObj	= denseObj	[meshLayer];
				var layerDenseUsed	= denseUsed	[meshLayer];
				var layerSparseIdx	= sparseIdx	[meshLayer];
				var layerSparseNum	= sparseNum	[meshLayer];

				if (layerDenseUsed[removeDenseId]) {
					layerDenseUsed[removeDenseId] = false;

					remove((T) layerDenseObj[removeDenseId]);
					continue;
				}

				var removeSparseId		= layerDenseIdx[removeDenseId];
				var removeSparseBlock	= removeSparseId / SPARSE_BLOCK_SIZE;
				var removeSparseIndex	= removeSparseId % SPARSE_BLOCK_SIZE;

				var tailSparseId	= layerDenseIdx[tailDenseId];
				var tailSparseBlock	= tailSparseId / SPARSE_BLOCK_SIZE;
				var tailSparseIndex	= tailSparseId % SPARSE_BLOCK_SIZE;

				layerDenseIdx[removeDenseId] = tailSparseId;
				layerDenseObj[removeDenseId] = layerDenseObj[tailDenseId];

				layerDenseIdx[tailDenseId] = 0;
				layerDenseObj[tailDenseId] = null;

				layerSparseIdx[tailSparseBlock]		[tailSparseIndex]	= 1 + removeDenseId;
				layerSparseIdx[removeSparseBlock]	[removeSparseIndex]	= 0;
				layerSparseNum[removeSparseBlock] --;

				denseHead[meshLayer] --;

				if (layerSparseNum[removeSparseBlock] == 0) {
					layerSparseIdx[removeSparseBlock] = null;
				}
			}
		}
	}

	private static int size(int highest) {
		return 1 << (32 - Integer.numberOfLeadingZeros(highest));
	}
}
