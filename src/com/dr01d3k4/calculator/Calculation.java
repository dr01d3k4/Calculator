package com.dr01d3k4.calculator;

import java.util.ArrayList;


public final class Calculation {
	/**
	 * Each string in the array is just 1 character long, otherwise the rest breaks
	 * Should it be a LinkedList for faster random access with cursor?
	 * Or does this it make it slower when cursor is at end (most likely scenario)?
	 * Shouldn't have an affect on iterating performance
	 */
	private final ArrayList<String> calculation;
	
	/**
	 * Cursor is always last position in calculation list + 1
	 * calculation.get(0) is the first in array so smallest cursor hits is 1
	 */
	private int cursor = 1;
	
	
	public Calculation(String startText) {
		if (startText.length() == 0) {
			startText = "0";
		}
		
		calculation = new ArrayList<String>();
		setCalculationTo(startText);
	}
	
	
	/**
	 * Gets the current position of the cursor
	 */
	public int getCursor() {
		return cursor;
	}
	
	
	/**
	 * Gets the length of the calculation array
	 * 
	 * @return the length
	 */
	public int getCalculationSize() {
		return calculation.size();
	}
	
	
	/**
	 * Gets the character that is before the cursor
	 * 
	 * @return the character
	 */
	public String getBeforeCursor() {
		if (cursor == 0) {
			return "";
		} else {
			return calculation.get(cursor - 1);
		}
	}
	
	
	/**
	 * Gets the character that is after the cursor
	 * 
	 * @return the character if it exists, else puts the cursor to the end and returns a blank string
	 */
	public String getAfterCursor() {
		final int calcSize = getCalculationSize();
		if (calcSize > cursor) {
			return calculation.get(cursor);
		} else {
			putCursorAtEnd();
			return "";
		}
	}
	
	
	/**
	 * Calls calculation.get(index)
	 * 
	 * @param index
	 *            the index to get
	 * @return the string at this index
	 */
	public String get(final int index) {
		return calculation.get(index);
	}
	
	
	/**
	 * Moves the cursor 1 character to the left
	 * If the cursor moves too far to the left, it gets put back to the first character
	 */
	public void moveCursorLeft() {
		cursor--;
		if (cursor < 0) {
			cursor = 0;
		}
	}
	
	
	/**
	 * Moves the cursor 1 character to the right
	 * If the cursor moves too far to the left, it gets put to the end of the calculation
	 */
	public void moveCursorRight() {
		cursor++;
		final int calcSize = getCalculationSize();
		if (cursor > calcSize) {
			cursor = calcSize;
		}
	}
	
	
	/**
	 * Inserts the string at the current cursor location and advances the cursor one character to the right
	 * 
	 * @param add
	 *            the string to insert
	 */
	public void insertAtCursor(final String add) {
		calculation.add(cursor, add);
		moveCursorRight();
	}
	
	
	/**
	 * Puts the cursor to the end of the calculation
	 */
	public void putCursorAtEnd() {
		cursor = getCalculationSize();
	}
	
	
	/**
	 * Clears the current calculation, populates it with the new calculation and updates the cursor position
	 * 
	 * @param newCalculation
	 *            the new calculation to use
	 */
	public void setCalculationTo(final String newCalculation) {
		calculation.clear();
		for (int i = 0; i < newCalculation.length(); i++) {
			calculation.add("" + newCalculation.substring(i, i + 1));
		}
		putCursorAtEnd();
	}
	
	
	/**
	 * Returns true if a closing bracket is needed
	 * 
	 * @param startAt
	 *            where to start looking from (so whether cursor or whole calculation being checked)
	 * @return boolean whether a closing bracket can be placed without being syntactically incorrect
	 */
	public boolean canCloseBracket(final int startAt) {
		int bracketLevel = 0;
		for (int index = startAt - 1; index >= 0; index--) {
			final String s = calculation.get(index);
			if (s.equals("(")) {
				bracketLevel++;
			} else if (s.equals(")")) {
				bracketLevel--;
			}
		}
		return (bracketLevel > 0);
	}
	
	
	/**
	 * Casts the number to a string and puts it into the calculation, adding * if necessary
	 * 
	 * @param number
	 *            the number to add
	 */
	public void putNumber(final int number) {
		if ((number < 0) || (number > 9)) {
			return;
		}
		final String last = getBeforeCursor();
		
		if ((getCalculationSize() == 1) && last.equals("0")) {
			// If the only item in the calculation is a 0, no point keeping it
			setCalculationTo(Integer.toString(number));
		} else {
			// After a closing bracket, the number should be multiplied and so add a multiply sign
			if (last.equals(")")) {
				insertAtCursor("*");
			}
			insertAtCursor(Integer.toString(number));
		}
	}
	
	
	/**
	 * Puts an operator into the calculation.
	 * Checks it can be placed here and adds other operators like * as well.
	 * Handles + - * / ( ) .
	 * 
	 * @param operator
	 *            the operator to put into the calculation
	 */
	public void putOperator(final String operator) {
		if (operator.equals("")) {
			return;
		}
		
		final int calcSize = getCalculationSize();
		final String last = getBeforeCursor();
		
		if (operator.equals("(")) {
			// Can't put a bracket after a decimal point (number expected)
			if (!last.equals(".")) {
				if ((calcSize == 1) && last.equals("0")) {
					// Replace the 0 if that's all there is
					setCalculationTo("(");
				} else {
					// If the bracket is being placed after a number or a closing bracket
					// They should be multiplied together
					if (TypeChecker.isNumber(last) || last.equals(")")) {
						insertAtCursor("*");
					}
					insertAtCursor("(");
				}
				
				// If there isn't another character after this bracket, insert the closing bracket for the user
				// Like an IDE
				// TODO: Generated array index out of bounds exception here
				final String next = getAfterCursor();
				if (next.equals("")) {
					insertAtCursor(")");
					moveCursorLeft();
				}
			}
			
		} else if (operator.equals(")")) {
			// Can only put a closing bracket if there is a bracket to be closed
			// Prevent user writing syntax errors
			if (canCloseBracket(cursor) && (TypeChecker.isNumber(last) || TypeChecker.isBracket(last))) {
				final String next = getAfterCursor();
				// Like an IDE, just overwrite a closing bracket if there's already one there
				if (next.equals(")")) {
					moveCursorRight();
				} else {
					if (last.equals("(")) {
						// If this bracket is closing a just opened bracket, to prevent errors put a 1 inside
						insertAtCursor("1");
						insertAtCursor(")");
					} else if (TypeChecker.isNumber(last) || last.equals(")")) {
						insertAtCursor(")");
					}
				}
			}
			
		} else if (operator.equals(".")) {
			if (TypeChecker.isNumber(last)) {
				// Check that the number doesn't already have a decimal point
				// The algorithm works by going backwards until it finds the first non-numerical character
				// If this character is a ., then don't place a new .
				// Otherwise place .
				String c = "";
				boolean foundNotNumber = false;
				for (int index = cursor - 1; index >= 0; index--) {
					c = calculation.get(index);
					if (!TypeChecker.isNumber(c)) {
						foundNotNumber = true;
						break;
					}
				}
				if (foundNotNumber) {
					if (!c.equals(".")) {
						insertAtCursor(".");
					}
				} else {
					insertAtCursor(".");
				}
			} else if (TypeChecker.isOperator(last) || last.equals("(")) {
				// Automatically fill in the 0 for quicker typing
				insertAtCursor("0");
				insertAtCursor(".");
			} else if (last.equals(")")) {
				insertAtCursor("*");
				insertAtCursor("0");
				insertAtCursor(".");
			}
			
		} else {
			// If the calculation is just 0 and a - is being replaced, clear the 0
			if ((calcSize == 1) && last.equals("0") && operator.equals("-")) {
				setCalculationTo("-");
			} else if (!last.endsWith(".") && !(last.equals("(") && !operator.equals("-"))) {
				// Can't put an operator after a . or (
				if (operator.equals("-")) {
					// Don't allow -----etc, but still allow just --
					if (calcSize >= 2) {
						if (!(TypeChecker.isOperator(last) && TypeChecker.isOperator(calculation.get(cursor - 2)))) {
							insertAtCursor(operator);
						}
					} else {
						insertAtCursor(operator);
					}
				} else {
					// Can't put two operators next to each other other than -
					if (!TypeChecker.isOperator(last)) {
						insertAtCursor(operator);
					}
				}
			}
		}
	}
	
	
	/**
	 * Deletes the token just before the cursor.
	 * If this leaves a blank calculation, it sets it to 0
	 */
	public void deleteAtCursor() {
		if (cursor >= 1) {
			calculation.remove(cursor - 1);
			moveCursorLeft();
		}
		if (getCalculationSize() == 0) {
			// All the calculation was deleted so just set it back to a 0
			setCalculationTo("0");
		}
	}
	
	
	/**
	 * Calculates the answer for the current calculation
	 * If the calculation has a syntax error, this is detected and outputted instead
	 * 
	 * @return the answer as a string, or error message
	 */
	public String calculateAnswer() {
		final ArrayList<String> calculationTokens = AnswerCalculator.buildTokensFromCalculation(calculation);
		final ArrayList<String> answerTokens = AnswerCalculator.infixToRpn(calculationTokens);
		String answer = "";
		
		if ((answerTokens.size() == 2) && (answerTokens.get(0).equals(AnswerCalculator.ERROR_CONSTANT))) {
			answer = answerTokens.get(1);
		} else {
			answer = AnswerCalculator.rpnToAnswer(answerTokens);
		}
		
		return answer;
	}
	
	
	@Override
	public String toString() {
		String display = "";
		final StringBuilder displayBuilder = new StringBuilder();
		String previousToken = "";
		String previousToken2 = "";
		final String cursorChar = "|";
		
		for (int i = 0; i < calculation.size(); i++) {
			if (i == cursor) {
				displayBuilder.append(cursorChar);
			}
			
			final String token = calculation.get(i);
			
			if (TypeChecker.isOperator(token) && !token.equals("-")) {
				// Add a space before all operators apart from -
				displayBuilder.append(" ");
			} else if (token.equals("-") && !previousToken.equals("(")) {
				// Only add a space before - if it's not after a (
				displayBuilder.append(" ");
			}
			
			// Adding a space between operator and number/bracket, unless operator is a -
			if ((TypeChecker.isNumber(token) || TypeChecker.isBracket(token))
				&& (TypeChecker.isOperator(previousToken) || previousToken.equals("("))) {
				if (previousToken.equals("-")) {
					if (!previousToken2.equals("-") && !previousToken2.equals("") && !previousToken2.equals("(")) {
						displayBuilder.append(" ");
					}
				} else {
					if (!(TypeChecker.isNumber(token) && previousToken.equals("("))) {
						displayBuilder.append(" ");
					}
				}
			}
			
			// display += token;
			displayBuilder.append(token);
			previousToken2 = previousToken;
			previousToken = token;
		}
		
		// If the cursor hasn't been added yet because it's at the end, add now
		if (cursor >= calculation.size()) {
			// display += cursorChar;
			displayBuilder.append(cursorChar);
		}
		
		display = displayBuilder.toString();
		while (display.startsWith(" ")) {
			display = display.substring(1);
		}
		
		display = display.replace(") * (", ")(");
		display = display.replaceAll("(\\d) \\* \\(", "$1(");
		return display;
	}
}