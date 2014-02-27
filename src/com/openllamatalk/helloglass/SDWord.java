/*
 * SDWord.java
 *
 * Class for a word in a sentence diagram.
   Contains information relating to placement of word on sentence diagram
   as well as which words are its children in the Stanford dependency
   diagram.
 */

package com.openllamatalk.helloglass;

import java.util.List;

import android.graphics.Paint;
import android.graphics.Rect;

import edu.stanford.nlp.trees.TreeGraphNode;

public class SDWord {

  public String word;
  public TreeGraphNode node;    // Node in dependency tree.
  public Rect dimensions;    // Rectangular dimensions of word.
  public Rect placement;    // Axes of placement on diagram.
  public List<SDWord> children;
  public boolean isDiagonal;
  public float diagLineX;    // Leftmost x value of diagonal line.


  public SDWord(String wrd, TreeGraphNode nd, boolean diagonal, Paint mPaint) {
    node = nd;
    dimensions = new Rect();
    placement = new Rect();
    word = wrd;
    isDiagonal = diagonal;


    mPaint.getTextBounds(word, 0, word.length(), dimensions);
  }


  public void setDiagLineX(float xVal) {
    diagLineX = xVal;
  }

}
