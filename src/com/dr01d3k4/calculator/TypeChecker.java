package com.dr01d3k4.calculator;

public final class TypeChecker {
	/**
	 * Checks whether a string is a number
	 * 
	 * @param input
	 *            the input to check
	 * @return boolean whether the string can be casted to a double
	 */
	public static boolean isNumber(final String input) {
		try {
			Double.parseDouble(input);
			return true;
		} catch (final Exception e) {
			return false;
		}
	}
	
	
	/**
	 * Checks whether a string is an operator (+ - * \ ^ %)
	 * 
	 * @param input
	 *            the input to check
	 * @return boolean whether the string is a a number
	 */
	public static boolean isOperator(final String input) {
		return (input.equals("+") || input.equals("-") || input.equals("*") || input.equals("/") || input.equals("^") || input
			.equals("%"));
	}
	
	
	/**
	 * Checks whether a string is a bracket
	 * 
	 * @param input
	 *            the input to check
	 * @return boolean whether the string is a bracket
	 */
	public static boolean isBracket(final String input) {
		return (input.equals("(") || input.equals(")"));
	}
	
	
	/**
	 * Checks whether a string is a function identifier
	 * i.e. not a number, bracket or operator
	 * 
	 * @param input
	 *            the input to check
	 * @return boolean whether the string is an identifier
	 */
	public static boolean isFunction(final String input) {
		return (!isNumber(input) && !isBracket(input) && !isOperator(input));
	}
}
