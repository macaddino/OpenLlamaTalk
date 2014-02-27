/*
 * SentenceDiagram.java
 *
 * This class contains information relating to the dimensions and drawing
 * of a Reed Kellogg sentence diagram.
 */

package com.openllamatalk.helloglass;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TypedDependency;

public class SentenceDiagram {

  public int vertDivisions;  // Number of vertical lines on horiz line.
  public int textDistFromHorizLine;  // Space between text and horiz line.
  public int textDistFromVertLine;  // Space between text and vertical line.
  public int diagSpacing; // Space between two diagonal lines.
  public List<TypedDependency> dependencies; // Dependencies between words.
  public Bitmap bmp;
  public Canvas canvas;
  public Paint mPaint;
  public int canvasHeight;
  public int canvasWidth;
  public int strokeWidth;
  public int textSize;


  SentenceDiagram(List<TypedDependency> deps) {
    dependencies = deps;
    canvasHeight = 360;
    canvasWidth = 640;
    strokeWidth = 5;
    vertDivisions = 2;
    textSize = 50;
    
    Bitmap.Config conf = Bitmap.Config.ARGB_8888;
    // Create mutable bitmap.
    bmp = Bitmap.createBitmap(canvasWidth, canvasHeight, conf);
    canvas = new Canvas(bmp);
    mPaint = new Paint();
    mPaint.setColor(Color.BLACK);

    // Ready to draw on bitmap through the canvas.
    canvas.drawColor(Color.WHITE);
    mPaint.setStrokeWidth(strokeWidth);
  }


  public void initDiagram() {
    String subject = "";
    SDWord subjectWord = null;
    String pred = "";
    SDWord predWord = null;
    String dobj = "";
    String predAdjOrNoun = "";
    SDWord predAdjOrNounOrDobjWord = null;
    int firstVertLineX = 0;
    int secondVertLineX = 0;

    // Find verb, and dobj/pred adj/pred noun if present.
    for (TypedDependency dep : dependencies) {
      if (dep.reln().toString().equals("cop")) {
        predAdjOrNoun = dep.gov().toString();
        predAdjOrNounOrDobjWord = new SDWord(predAdjOrNoun, dep.gov(),
                                             false, mPaint);
        pred = dep.dep().toString();
        predWord = new SDWord(pred, dep.dep(), false, mPaint);
        vertDivisions = 3;
        textSize = 30;
        firstVertLineX = canvasWidth / 3;
      } else if (dep.reln().toString().equals("root")) {
        if (!predAdjOrNoun.equals(dep.dep().toString())) {
          pred = dep.dep().toString();
          firstVertLineX = canvasWidth / 2;
          predWord = new SDWord(pred, dep.dep(), false, mPaint);
          break;
        }
      }
    }

    // Find direct object.
    for (TypedDependency dep : dependencies) {
      if (dep.reln().toString().equals("dobj")) {
        dobj = dep.dep().toString();
        predAdjOrNounOrDobjWord = new SDWord(dobj, dep.dep(), false, mPaint);
        vertDivisions = 3;
        textSize = 30;
        firstVertLineX = canvasWidth / 3;
        break;
      }
    }

    // Find subject.
    for (TypedDependency dep : dependencies) {
      if (dep.reln().toString().equals("nsubj") &&
          (dep.gov().toString().equals(pred) ||
           dep.gov().toString().equals(predAdjOrNoun))) {
        subject = dep.dep().toString();
        subjectWord = new SDWord(subject, dep.dep(), false, mPaint);
        break;
      }
    }

    // Handling command cases where subject "you" is implied.
    if (subject.equals("")) {
      subject = "(you)-0";
      subjectWord = new SDWord(subject, new TreeGraphNode(), false, mPaint);
    }


    // Draw horizontal line.
    canvas.drawLine(0 + 20, canvasHeight / 2, canvasWidth - 20,
                    canvasHeight / 2, mPaint);

    // Draw vertical division lines.
    canvas.drawLine(firstVertLineX, canvasHeight / 2 - 50,
                    firstVertLineX, canvasHeight / 2 + 50, mPaint);

    if (vertDivisions == 3) {
      // Draw line following pred.
      canvas.drawLine(firstVertLineX * 2 - 30, canvasHeight / 2 - 50,
                      firstVertLineX * 2, canvasHeight / 2, mPaint);
    }

    // Draw subject, predicate, and (if existing) dobj/pred adj/pred n words.
    mPaint.setTextSize(textSize);
    textDistFromHorizLine = canvasHeight / 2 - subjectWord.dimensions.height();


    canvas.drawText(subjectWord.word,
                    firstVertLineX - subjectWord.dimensions.width() - 100,
                    textDistFromHorizLine,
                    mPaint);
    subjectWord.placement.set(
        firstVertLineX - subjectWord.dimensions.width() - 100,
        textDistFromHorizLine - subjectWord.dimensions.height(),
        firstVertLineX - 100,
        textDistFromHorizLine);

    canvas.drawText(predWord.word,
                    firstVertLineX + 50,
                    textDistFromHorizLine,
                    mPaint);
    predWord.placement.set(
        firstVertLineX + 50,
        textDistFromHorizLine - predWord.dimensions.height(),
        firstVertLineX + predWord.dimensions.width() + 50,
        textDistFromHorizLine);

    if (vertDivisions == 3) {
      canvas.drawText(predAdjOrNounOrDobjWord.word,
                      firstVertLineX * 2 - 30 + 50,
                      textDistFromHorizLine,
                      mPaint);
      predAdjOrNounOrDobjWord.placement.set(
          firstVertLineX * 2 - 30 + 50,
          textDistFromHorizLine - predAdjOrNounOrDobjWord.dimensions.height(),
          firstVertLineX * 2 - 30 + predAdjOrNounOrDobjWord.dimensions.width() + 50,
          textDistFromHorizLine);
    }

    // Draw children of subj, pred, and dobj/pred adj/pred n recursively.
    for (TreeGraphNode child : subjectWord.node.children()) {
      childParse(child, subjectWord);
    }
    for (TreeGraphNode child : predWord.node.children()) {
      childParse(child, predWord);
    }
    if (vertDivisions == 3) {
      for (TreeGraphNode child : predAdjOrNounOrDobjWord.node.children()) {
        childParse(child, predAdjOrNounOrDobjWord);
      }
    }
  }


  public void childParse(TreeGraphNode child, SDWord parent) {
    // Switch 
  }
}
  


