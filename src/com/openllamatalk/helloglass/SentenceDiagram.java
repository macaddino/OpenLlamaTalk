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

import edu.stanford.nlp.trees.GrammaticalRelation.GrammaticalRelationAnnotation;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TypedDependency;

public class SentenceDiagram {

  public int vertDivisions;  // Number of vertical lines on horiz line.
  public int textDistFromHorizLine;  // Space between text and horiz line.
  public int textDistFromVertLine;  // Space between text and vertical line.
  public int diagSpacing; // Space between two diagonal lines.
  public List<TypedDependency> dependencies; // Dependencies between words.
  public String correctedWord;
  public Bitmap bmp;
  public Canvas canvas;
  public Paint mPaint;
  public int canvasHeight;
  public int canvasWidth;
  public int strokeWidth;
  public int textSize;


  SentenceDiagram(List<TypedDependency> deps, String word) {
    dependencies = deps;
    correctedWord = word;
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


  // Draws the diagram.
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
                                             false, mPaint, correctedWord);
        pred = dep.dep().toString();
        predWord = new SDWord(pred, dep.dep(), false, mPaint, correctedWord);
        vertDivisions = 3;
        textSize = 30;
        firstVertLineX = canvasWidth / 3;
      } else if (dep.reln().toString().equals("root")) {
        if (!predAdjOrNoun.equals(dep.dep().toString())) {
          pred = dep.dep().toString();
          firstVertLineX = canvasWidth / 2;
          predWord = new SDWord(pred, dep.dep(), false, mPaint, correctedWord);
          break;
        }
      }
    }

    // Find direct object.
    for (TypedDependency dep : dependencies) {
      if (dep.reln().toString().equals("dobj")) {
        dobj = dep.dep().toString();
        predAdjOrNounOrDobjWord = new SDWord(dobj, dep.dep(), false, mPaint,
        		                             correctedWord);
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
        subjectWord = new SDWord(subject, dep.dep(), false, mPaint,
        		                                            correctedWord);
        break;
      }
    }

    // Handling command cases where subject "you" is implied.
    if (subject.equals("")) {
      subject = "(you)-0";
      subjectWord = new SDWord(subject, new TreeGraphNode(), false, mPaint,
    		                   correctedWord);
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
    subjectWord.calculateDimensions();
    textDistFromHorizLine = canvasHeight / 2 - subjectWord.dimensions.height();

    subjectWord.onPreDraw();
    canvas.drawText(subjectWord.word,
                    firstVertLineX - subjectWord.dimensions.width() - 50,
                    textDistFromHorizLine,
                    mPaint);
    System.out.println("THIS IS FIRST VERTICAL X POS: " + firstVertLineX + ", WIDTH OF SUBJECT " + subjectWord.dimensions.width());
    subjectWord.placement.set(
        firstVertLineX - subjectWord.dimensions.width() - 50,
        textDistFromHorizLine - subjectWord.dimensions.height(),
        firstVertLineX - 50,
        textDistFromHorizLine);
    subjectWord.onPostDraw();

    predWord.calculateDimensions();
    predWord.onPreDraw();
    canvas.drawText(predWord.word,
                    firstVertLineX + 50,
                    textDistFromHorizLine,
                    mPaint);
    predWord.placement.set(
        firstVertLineX + 50,
        textDistFromHorizLine - predWord.dimensions.height(),
        firstVertLineX + predWord.dimensions.width() + 50,
        textDistFromHorizLine);
    predWord.onPostDraw();

    if (vertDivisions == 3) {
      predAdjOrNounOrDobjWord.calculateDimensions();
      predAdjOrNounOrDobjWord.onPreDraw();
      canvas.drawText(predAdjOrNounOrDobjWord.word,
                      firstVertLineX * 2 - 30 + 50,
                      textDistFromHorizLine,
                      mPaint);
      predAdjOrNounOrDobjWord.placement.set(
          firstVertLineX * 2 - 30 + 50,
          textDistFromHorizLine - predAdjOrNounOrDobjWord.dimensions.height(),
          firstVertLineX * 2 - 30 + predAdjOrNounOrDobjWord.dimensions.width() + 50,
          textDistFromHorizLine);
      predAdjOrNounOrDobjWord.onPostDraw();
    }

    // Draw dependencies of subj, pred, and dobj/pred adj/pred n recursively.
    for (TypedDependency dep : dependencies) {
      if (dep.gov() == subjectWord.node) {
        dependencyParse(dep.dep(), subjectWord, dep.reln().toString());
      }
      else if (dep.gov() == predWord.node) {
    	if (!dep.reln().toString().equals("dobj") &&
    	    !dep.reln().toString().equals("nsubj"))
          dependencyParse(dep.dep(), predWord, dep.reln().toString());
      }
      else if (vertDivisions == 3 && dep.gov() == predAdjOrNounOrDobjWord.node) {
    	if (!dep.reln().toString().equals("nsubj") &&
    	    !dep.reln().toString().equals("cop"))
          dependencyParse(dep.dep(), predAdjOrNounOrDobjWord, dep.reln().toString());
      }
    }
  }


  // Depending on dependency relationship between "dep" and "gov", place "dep"
  // on diagram in relation to "gov".
  public void dependencyParse(TreeGraphNode dependent, SDWord governor,
                              String depType) {
	System.out.println("CHILDPARSE CALLED ON " + dependent.toString());
	System.out.println("THIS CHILD HAS A DEP WITH PARENT OF " + depType);
    DepEnum.Dep dep = DepEnum.fromString(depType);
    switch(dep) {
        case ABBREV:
        case ADVMOD:
        case AMOD:
        case DEP:
        case DET:
        case MEASURE:
        case NEG:
        case NN:
        case NUM:
        case NUMBER:
        case POSS:
        case PREDET:
        case PREP:
        case QUANTMOD:
        case REF:
          addDiagWord(dependent, governor);
          break;
        case IOBJ:
        case PARATAXIS:
        case POBJ:
          addHorizWord(dependent, governor);
          break;
        case APPOS:
        case POSSESSIVE:
        case PRT:
          // appendWord(true);
          break;
        case AUX:
        case TMOD:
          // appendWord(false);
          break;
        case ADVCL:
        case CSUBJ:
        case PCOMP:
        case RCMOD:
          // newDiagram();
          break;
        case COMPLM:
        case EXPL:
        case MARK:
          // addSegment();
          break;
    }
    // Call dependencyParse once more if there the dep is a gov of anything.
    // We might need to pass in the dependencytree to do this.
  }
  

  // Add word "dep" to diagram which will be diagonally placed under "gov".
  public void addDiagWord(TreeGraphNode dependent, SDWord governor) {
    // Determine leftmost x coords of diagonal line.
    // It should be directly under the parent word and to the right of 
    // all other diagonally placed children of parent.
    float diagLineX = governor.placement.left;
    if (!governor.dependents.isEmpty()) {
      for (SDWord chld : governor.dependents) {
        if (chld.isDiagonal && chld.diagLineX >= diagLineX)
          diagLineX = chld.diagLineX + 50;
      }
    }

    canvas.drawLine(diagLineX, (float) canvasHeight / 2,
                    diagLineX + 30, (float) canvasHeight / 2 + 70, mPaint);
    
    canvas.save();
    canvas.rotate(65, diagLineX + 20, (float) canvasHeight / 2 + 20);
    mPaint.setTextSize(25);
    canvas.drawText(dependent.toString(), diagLineX + 20,
                    (float) canvasHeight / 2 + 20, mPaint);
    canvas.restore();
    mPaint.setTextSize(textSize);
    // Add dependent word to governing word's list of dependents.
    SDWord word = new SDWord(dependent.toString(), dependent, true, mPaint,
    		                 correctedWord);
    word.setDiagLineX(diagLineX);
    governor.dependents.add(word);
    
    for (TypedDependency dep : dependencies) {
        if (dep.gov() == word.node) {
          dependencyParse(dep.dep(), word, dep.reln().toString());
        }
    }
    
  }

  // Add word "dep" to diagram which will be horizontally placed under "gov".
  public void addHorizWord(TreeGraphNode dependent, SDWord governor) {
    float diagLineX;
    mPaint.setTextSize(25);
    SDWord word = new SDWord(dependent.toString(), dependent, false, mPaint,
                             correctedWord);
    word.calculateDimensions();
    if (!governor.isDiagonal) {
      // If gov is horiz placed word, draw empty diag line under it.
      diagLineX = governor.placement.left;
      if (!governor.dependents.isEmpty()) {
        for (SDWord chld : governor.dependents) {
          if (chld.isDiagonal && chld.diagLineX >= diagLineX)
            diagLineX = chld.diagLineX + 50;
        }
      }
      canvas.drawLine(diagLineX, (float) canvasHeight / 2,
                      diagLineX + 30, (float) canvasHeight / 2 + 70, mPaint);
    } else {
      diagLineX = governor.diagLineX;
    }
    // Draw horiz line.
    canvas.drawLine(diagLineX + 30, canvasHeight / 2 + 70,
                    diagLineX + 30 + word.dimensions.width() + 70,
                    canvasHeight / 2 + 70, mPaint);
    // Draw word on horizontal line.


    word.onPreDraw();
    canvas.drawText(word.word,
                    diagLineX + 30 + 30,
                    canvasHeight / 2 + 70 - 20,
                    mPaint);
    // TODO: ADD PLACEMENT DATA FOR Word.
    word.onPostDraw();
    // Save word as part of governor's dependents.
    governor.dependents.add(word);
  }
}
  


