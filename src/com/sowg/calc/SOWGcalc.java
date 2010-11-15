/**
 *  SOWGware, Copyright 2010
 * 
 *  This file is part of SOWGcalc.

    SOWGcalc is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SOWGcalc is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SOWGcalc.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sowg.calc;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;

public class SOWGcalc extends Activity {
	private final char[] repChars = {'+', Constants.DIV_CHAR, Constants.MULT_CHAR, '^'};
	private final char[] nonClearChars = {'+', '-', Constants.DIV_CHAR, Constants.MULT_CHAR, '^', '!'};
	private final char[] doubles = {'.'};
	
	private final SOWGNumberKeyListener keyL = new SOWGNumberKeyListener();
	
	private boolean clearItFirst = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        EditText output = (EditText)findViewById(R.id.output);

        //set key override
        output.setKeyListener(keyL);
        
        //focus
        output.setSelected(true);
        
        //set font
        Typeface font = Typeface.createFromAsset(getAssets(), "outputfont.ttf");
        output.setTypeface(font);
    }
    
    //listener for text buttons
    public void buttonClickListener(View v)
    {
    	//new characters to be input
    	ImageButton b = (ImageButton)v;
    	String newChars = b.getTag().toString();
    	char lastNewChar = newChars.charAt(newChars.length() - 1);
    	char firstNewChar = newChars.charAt(0);
    	
    	//handle clearing it before, e.g. if equals was just clicked
    	if (clearItFirst && !Helper.charInList(firstNewChar, nonClearChars))
    	{
    		clearListener(v);
    	}
    	//make sure to reset this no matter what
    	clearItFirst = false;
    	
    	//get the output field
    	EditText output = (EditText) findViewById(R.id.output);
        //output.setTextSize((float)output.getMeasuredHeight() / 2.0f);
 
    	//current text in field
    	String currText = output.getText().toString();
    	
    	//indices for selection
    	int start = Math.min(output.getSelectionStart(), output.getSelectionEnd());
    	int end = Math.max(output.getSelectionStart(), output.getSelectionEnd());
    	
    	//check the last char for add/replace
    	if (currText.length() > 0)
    	{
    		char toCheck = output.getText().charAt(Math.max(0, end - 1));
    		if (!addOrReplace(toCheck, lastNewChar))
    		{
	    		//simply go 1 before in the index for replace
	    		start--;
    		}
    	}
    	
    	//temporarily allow all text
    	output.setKeyListener(null);
    	
    	//insert
    	try
    	{
    		output.getText().replace(start, end, newChars);
    	}
    	catch(Exception e)
    	{
    		
    	}
    	
    	//reinstate key restrictions
    	output.setKeyListener(keyL);
    }
    
    //equals button listener
    public void equalsListener(View v)
    {    	
    	setOutput(getCalcResult());
    }
    
    //C button
    public void clearListener(View v)
    {    	
    	EditText output = (EditText) findViewById(R.id.output);
    	output.setText("");
    }
    
    //backspace button
    public void backspaceListener(View v)
    {    	
    	EditText output = (EditText) findViewById(R.id.output);
    	keyL.backspace(output, output.getEditableText(), 0, new KeyEvent(0, 0));
    }
    
    //MC button
    public void MCListener(View v)
    {
    	Memory.clear();
    }
    
    //MR button
    public void MRListener(View v)
    {    	
    	setOutput(Helper.doubleFormatted(Memory.recall()));
    }
    
    //M+ button
    public void MPlusListener(View v)
    {
    	Memory.plus(Double.parseDouble(getCalcResult()));
    }
    
    //M- button
    public void MMinusListener(View v)
    {
    	Memory.minus(Double.parseDouble(getCalcResult()));
    }
    
    //set the output window with a specific string
    private void setOutput(String val)
    {
    	EditText output = (EditText) findViewById(R.id.output);

    	//temporarily allow all text
    	output.setKeyListener(null);
    	
    	try
    	{
	    	//feed into grammar parser
	    	output.setText(val);
	    	output.setSelection(output.getText().length());
		}
		catch(Exception e)
		{
			
		}
    	
    	//reinstate key restrictions
    	output.setKeyListener(keyL);
    	
    	//set the clear flag
    	clearItFirst = true;
    }
    
    //pass in a string expression, get a result
    private String getCalcResult()
    {
    	//get the calculator string
    	EditText output = (EditText) findViewById(R.id.output);
    	String calcText = output.getText().toString();
    	
    	//feed it through a parentheses checker
    	String correctedText = Helper.addParentheses(calcText);
    	
    	//return the evaluated result
    	return CalcGrammar.evalGrammar(correctedText);
    }
    
    //do we add to the string or replace the last char instead?
    private boolean addOrReplace(char lastChar, char toAdd)
    {
    	boolean add = true;
    	
    	//these characters should be replaced with each other    	
    	if (Helper.charInList(lastChar, repChars) && Helper.charInList(toAdd, repChars))
    	{
    		add = false;
    	}
    	
    	//these characters shouldn't produce doubles    	
    	for (char c : doubles)
    	{
    		if (c == lastChar && c == toAdd)
    		{
    			add = false;
    			break;
    		}
    	}
    	
    	return add;
    }
}