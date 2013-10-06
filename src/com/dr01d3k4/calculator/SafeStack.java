package com.dr01d3k4.calculator;

import java.util.EmptyStackException;
import java.util.Stack;


public class SafeStack<T> extends Stack<T> {
	private static final long serialVersionUID = 1L;
	
	
	@Override
	public T pop() {
		try {
			return super.pop();
		} catch (final EmptyStackException e) {
			return null;
		}
	}
	
	
	@Override
	public T peek() {
		try {
			return super.peek();
		} catch (final EmptyStackException e) {
			return null;
		}
	}
	
}
