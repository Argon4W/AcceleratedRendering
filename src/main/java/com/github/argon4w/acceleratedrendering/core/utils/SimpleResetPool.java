package com.github.argon4w.acceleratedrendering.core.utils;

import lombok.Getter;

import java.util.Arrays;

public abstract class SimpleResetPool<T, C> {

	@Getter protected	final	C			context;

	@Getter protected			Object[]	pool;
	@Getter protected			int			cursor;
	protected 					int			size;

	public SimpleResetPool(int size, C context) {
		this.size		= size;
		this.context	= context;
		this.pool		= new Object[size];

		this.cursor		= 0;

		for (var i = 0; i < this.size; i++) {
			this.pool[i] = create(this.context, i);
		}
	}

	protected abstract T	create	(C context, int i);
	protected abstract void	reset	(T t);
	protected abstract void	delete	(T t);

	@SuppressWarnings("unchecked")
	public T get(boolean force) {
		if (cursor < size) {
			var t = (T) pool[cursor ++];

			if (test(t)) {
				init(t);
				return t;
			}
		}

		return fail(force);
	}

	@SuppressWarnings("unchecked")
	public void reset() {
		for (var i = 0; i < cursor; i++) {
			reset((T) pool[i]);
		}

		cursor = 0;
	}

	@SuppressWarnings("unchecked")
	public void delete() {
		for (var i = 0; i < size; i++) {
			delete((T) pool[i]);
		}
	}

	protected void expand() {
		var old	= size;

		size	= old * 2;
		pool	= Arrays.copyOf(pool, size);

		for (var i = old; i < size; i ++) {
			pool[i] = create(context, i);
		}
	}

	@SuppressWarnings("unchecked")
	public T at(int index) {
		return (T) pool[index];
	}

	public T get() {
		return get(false);
	}

	public void init(T t) {

	}

	protected T fail(boolean force) {
		return fail();
	}

	public T fail() {
		return null;
	}

	protected boolean test(T t) {
		return true;
	}
}