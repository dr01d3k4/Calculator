package com.dr01d3k4.calculator;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class Main extends Activity {
	/** Each string in the array is just 1 character long, otherwise the rest breaks */
	private Calculation calculation;
	
	private TextView calculationDisplay;
	private TextView answerDisplay;
	private Button openBracketButton, closeBracketButton;
	private Button divideButton, multiplyButton, plusButton, minusButton;
	
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		calculationDisplay = (TextView) findViewById(R.id.tvCalculationDisplay);
		String startText = calculationDisplay.getText().toString();
		if (startText.equals("")) {
			startText = "0";
		}
		
		calculation = new Calculation(startText);
		
		answerDisplay = (TextView) findViewById(R.id.tvAnswerDisplay);
		
		openBracketButton = (Button) findViewById(R.id.btOpenBracketButton);
		closeBracketButton = (Button) findViewById(R.id.btCloseBracketButton);
		divideButton = (Button) findViewById(R.id.btDivideButton);
		multiplyButton = (Button) findViewById(R.id.btMultiplyButton);
		plusButton = (Button) findViewById(R.id.btPlusButton);
		minusButton = (Button) findViewById(R.id.btMinusButton);
		
		updateEnabledButtons();
		updateCalculationDisplay();
	}
	
	
	public void onNumberClick(final View view) {
		// Find which button it was
		int n = -1;
		switch (view.getId()) {
			case (R.id.btNumber0Button):
				n = 0;
				break;
			case (R.id.btNumber1Button):
				n = 1;
				break;
			case (R.id.btNumber2Button):
				n = 2;
				break;
			case (R.id.btNumber3Button):
				n = 3;
				break;
			case (R.id.btNumber4Button):
				n = 4;
				break;
			case (R.id.btNumber5Button):
				n = 5;
				break;
			case (R.id.btNumber6Button):
				n = 6;
				break;
			case (R.id.btNumber7Button):
				n = 7;
				break;
			case (R.id.btNumber8Button):
				n = 8;
				break;
			case (R.id.btNumber9Button):
				n = 9;
				break;
			default:
				n = -1;
				break;
		}
		// This shouldn't happen
		if ((n < 0) || (n > 9)) {
			Log.d("wat", "n out of bounds");
			return;
		}
		calculation.putNumber(n);
		updateAll();
	}
	
	
	public void onOperatorClick(final View view) {
		// Find which operator it is
		String operator = "";
		switch (view.getId()) {
			case R.id.btDivideButton:
				operator = "/";
				break;
			case R.id.btMultiplyButton:
				operator = "*";
				break;
			case R.id.btMinusButton:
				operator = "-";
				break;
			case R.id.btPlusButton:
				operator = "+";
				break;
			case R.id.btOpenBracketButton:
				operator = "(";
				break;
			case R.id.btCloseBracketButton:
				operator = ")";
				break;
			case R.id.btPointButton:
				operator = ".";
				break;
			default:
				operator = "";
		}
		calculation.putOperator(operator);
		updateAll();
	}
	
	
	public void onCursorClick(final View view) {
		switch (view.getId()) {
			case R.id.btCursorLeft:
				calculation.moveCursorLeft();
				break;
			case R.id.btCursorRight:
				calculation.moveCursorRight();
				break;
		}
		updateAll();
	}
	
	
	public void onClearClick(final View view) {
		calculation.setCalculationTo("0");
		updateAll();
	}
	
	
	public void onDeleteClick(final View view) {
		calculation.deleteAtCursor();
		updateAll();
	}
	
	
	/**
	 * Updates all the UI components
	 */
	private void updateAll() {
		updateEnabledButtons();
		updateCalculationDisplay();
		updateAnswerDisplay();
	}
	
	
	/**
	 * Concatenates all the parts of the calculation, adds the cursor and reformats in a more mathematical way
	 * Then displays to the user
	 */
	private void updateCalculationDisplay() {
		// Should change to use a StringBuilder probably
		final String display = calculation.toString();
		calculationDisplay.setText(display);
	}
	
	
	/**
	 * Changes whether an operator's button is enabled or not depending on whether or not
	 * pressing it would be syntactically correct
	 */
	private void updateEnabledButtons() {
		final int calcSize = calculation.getCalculationSize();
		final String last = calculation.getBeforeCursor();
		
		openBracketButton.setEnabled(true);
		closeBracketButton.setEnabled(true);
		divideButton.setEnabled(true);
		multiplyButton.setEnabled(true);
		plusButton.setEnabled(true);
		minusButton.setEnabled(true);
		
		if (last.equals("(")) {
			divideButton.setEnabled(false);
			multiplyButton.setEnabled(false);
			plusButton.setEnabled(false);
			closeBracketButton.setEnabled(false);
			
		} else if (last.equals(".")) {
			openBracketButton.setEnabled(false);
			closeBracketButton.setEnabled(false);
			divideButton.setEnabled(false);
			multiplyButton.setEnabled(false);
			plusButton.setEnabled(false);
			minusButton.setEnabled(false);
			
		} else if (TypeChecker.isOperator(last)) {
			closeBracketButton.setEnabled(false);
			divideButton.setEnabled(false);
			multiplyButton.setEnabled(false);
			plusButton.setEnabled(false);
			
			if (last.equals("-") && (calcSize >= 2)) {
				final String penultimate = calculation.get(calculation.getCursor() - 2);
				if (TypeChecker.isOperator(penultimate)) {
					minusButton.setEnabled(false);
				}
			}
			
		} else if ((calcSize == 1) && (last.equals("0"))) {
			closeBracketButton.setEnabled(false);
			divideButton.setEnabled(false);
			multiplyButton.setEnabled(false);
			plusButton.setEnabled(false);
		}
		
		if (closeBracketButton.isEnabled()) {
			closeBracketButton.setEnabled(calculation.canCloseBracket(calculation.getCursor()));
		}
	}
	
	
	private void updateAnswerDisplay() {
		answerDisplay.setText(calculation.calculateAnswer());
	}
}