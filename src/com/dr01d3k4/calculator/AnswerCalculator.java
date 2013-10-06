package com.dr01d3k4.calculator;

import java.util.ArrayList;


public final class AnswerCalculator {
	public static final String ERROR_CONSTANT = "[ERROR]";
	public static final String MISMATCHED_PARENTHESES = "Mismatched parentheses";
	
	
	/**
	 * Returns the precedence for an operator.
	 * Supports + - * / % ^
	 * 
	 * @param operator
	 *            the operator to find the precedence for
	 * @return integer precedence (or -1 if not an operator)
	 */
	private static int getPrecedence(final String operator) {
		int precedence = -1;
		if (operator.equals("+") || operator.equals("-")) {
			precedence = 0;
		} else if (operator.equals("*") || operator.equals("/") || operator.equals("%")) {
			precedence = 1;
		} else if (operator.equals("^")) {
			precedence = 2;
		}
		return precedence;
	}
	
	
	/**
	 * Takes an array list of tokens representing an equation in infix notation and rewrites it in RPN
	 * 
	 * @param tokens
	 *            the infix tokens
	 * @return array list of rpn tokens
	 */
	public static ArrayList<String> infixToRpn(final ArrayList<String> tokens) {
		final ArrayList<String> outputQueue = new ArrayList<String>();
		final SafeStack<String> stack = new SafeStack<String>();
		
		String errorMessage = "";
		
		for (final String token : tokens) {
			if (TypeChecker.isNumber(token)) {
				outputQueue.add(token);
				
			} else if (TypeChecker.isFunction(token)) {
				stack.push(token);
				
			} else if (TypeChecker.isOperator(token)) {
				String o2 = stack.peek();
				final int o1Precedence = getPrecedence(token);
				
				while ((o2 != null) && TypeChecker.isOperator(o2) && (o1Precedence <= getPrecedence(o2))) {
					outputQueue.add(stack.pop());
					o2 = stack.peek();
				}
				
				stack.push(token);
				
			} else if (token.equals("(")) {
				stack.push("(");
				
			} else if (token.equals(")")) {
				String topOfStack = stack.peek();
				while ((topOfStack != null) && (topOfStack != "(")) {
					outputQueue.add(stack.pop());
					topOfStack = stack.peek();
				}
				
				if ((topOfStack != null) && topOfStack.equals("(")) {
					stack.pop();
					
					topOfStack = stack.peek();
					if ((topOfStack != null) && TypeChecker.isFunction(topOfStack)) {
						outputQueue.add(stack.pop());
					}
				} else {
					errorMessage = AnswerCalculator.MISMATCHED_PARENTHESES;
					break;
				}
			}
		}
		
		String topOfStack = stack.peek();
		while (topOfStack != null) {
			if (TypeChecker.isBracket(topOfStack)) {
				errorMessage = AnswerCalculator.MISMATCHED_PARENTHESES;
				break;
			}
			outputQueue.add(stack.pop());
			topOfStack = stack.peek();
		}
		
		if (errorMessage.length() > 0) {
			final ArrayList<String> error = new ArrayList<String>();
			error.add(AnswerCalculator.ERROR_CONSTANT);
			error.add(errorMessage);
			return error;
		} else {
			return outputQueue;
		}
	}
	
	
	/**
	 * Builds the tokens for infixToRpn.
	 * 
	 * @param calculation
	 *            the infix tokens
	 * @return tokens
	 */
	public static ArrayList<String> buildTokensFromCalculation(final ArrayList<String> calculation) {
		final ArrayList<String> tokens = new ArrayList<String>();
		String current = "";
		int previousWasOperator = 0;
		
		for (int i = 0; i < calculation.size(); i++) {
			final String s = calculation.get(i);
			previousWasOperator--;
			
			if (TypeChecker.isNumber(s)) {
				current += s;
				
			} else if (TypeChecker.isOperator(s)) {
				if (current.length() > 0) {
					tokens.add(current);
				}
				current = "";
				if (s.equals("-") && (previousWasOperator > 0)) {
					if (((i + 1) < calculation.size()) && TypeChecker.isNumber(calculation.get(i + 1))) {
						current = "-";
					} else {
						tokens.add("-");
					}
				} else {
					tokens.add(s);
				}
				previousWasOperator = 2;
				
			} else if (TypeChecker.isBracket(s)) {
				if (current.length() > 0) {
					tokens.add(current);
				}
				current = "";
				tokens.add(s);
			} else {
				current += s;
			}
		}
		if (current.length() > 0) {
			tokens.add(current);
		}
		
		return tokens;
	}
	
	
	/**
	 * Calculates the numerical answer (or errors) from RPN tokens
	 * 
	 * @param rpnTokens
	 * @return
	 */
	public static String rpnToAnswer(final ArrayList<String> rpnTokens) {
		final SafeStack<Double> stack = new SafeStack<Double>();
		
		for (final String token : rpnTokens) {
			if (TypeChecker.isNumber(token)) {
				stack.push(Double.parseDouble(token));
				
			} else if (TypeChecker.isOperator(token)) {
				Double op2 = stack.pop();
				Double op1 = stack.pop();
				if ((op1 != null) && (op2 != null)) {
					op2 = (double) op2;
					op1 = (double) op1;
					
					double result = 0;
					
					if (token.equals("+")) {
						result = op1 + op2;
					} else if (token.equals("-")) {
						result = op1 - op2;
					} else if (token.equals("*")) {
						result = op1 * op2;
					} else if (token.equals("/")) {
						result = op1 / op2;
					} else if (token.equals("%")) {
						result = op1 % op2;
					} else if (token.equals("^")) {
						result = Math.pow(op1, op2);
					}
					
					stack.push(result);
				}
			}
		}
		
		if (stack.size() != 1) {
			return AnswerCalculator.ERROR_CONSTANT;
		} else {
			return "" + stack.get(0);
		}
	}
}
