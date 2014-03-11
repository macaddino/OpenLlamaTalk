/*
 * SDWord.java
 *
 * Class for a word in a sentence diagram.
 * Contains information relating to placement of word on sentence diagram
 * as well as which words are its children in the Stanford dependency
 * diagram.
 */

package com.openllamatalk.helloglass;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class SDWord {

  public String word;
  public Rect dimensions;    // Rectangular dimensions of word.
  public Rect placement;    // Axes of placement on diagram.
  public boolean isDiagonal;
  public float diagLineX;    // Leftmost x value of diagonal line.
  private Paint mPaint;
  public int color;
  public int row;  // Sentence Diagram row which word is placed on.
  public List<SDWord> dependents;

  public SDWord(String wrd, boolean diagonal, Paint mPnt,
                String correctedWord, int r) {
    dimensions = new Rect();
    placement = new Rect();
    word = wrd;
    isDiagonal = diagonal;
    row = r;
    if (wrd.length() > correctedWord.length() &&
        wrd.substring(0, correctedWord.length()).equals(correctedWord)) {
      color = Color.RED;
    } else {
      color = Color.BLACK;
    }
    mPaint = mPnt;
    dependents = new ArrayList<SDWord>();
  }

  
  public void calculateDimensions() {
    mPaint.getTextBounds(word, 0, word.length(), dimensions);
  }
  

  public void setDiagLineX(float xVal) {
    diagLineX = xVal;
  }


  public void onPreDraw() {
    mPaint.setColor(color);
  }


  public void onPostDraw() {
    mPaint.setColor(Color.BLACK);
  }
}
