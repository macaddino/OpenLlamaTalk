/*
 * SentenceDiagram.java
 *
 * This class contains information relating to the dimensions and drawing
 * of a Reed Kellogg sentence diagram.
 */

package com.openllamatalk.helloglass;

import java.util.ArrayList;
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
  public List<SDDependency> dependencies;  // Dependencies between words.
  public List<SDRow> rows;  // Sentence diagram rows.
  public String correctedWord;
  public Bitmap bmp;
  public Canvas canvas;
  public Paint mPaint;
  public int canvasHeight;
  public int canvasWidth;
  public int strokeWidth;
  public int textSize;


  SentenceDiagram(List<SDDependency> deps, String word) {
    dependencies = deps;
    correctedWord = word;
    canvasHeight = 360;
    canvasWidth = 640;
    strokeWidth = 5;
    vertDivisions = 2;
    rows = new ArrayList<SDRow>();
    
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
    SDRow diagramSkeleton = new SDRow(0, (float) canvasHeight);
    rows.add(diagramSkeleton);

    // Find verb, and dobj/pred adj/pred noun if present.
    for (SDDependency dep : dependencies) {
      System.out.println(dep.reln);
      System.out.println("SIZE OF DEPS: " + dependencies.size());
      if (dep.reln.equals("cop")) {
    	predAdjOrNoun = dep.gov;
        predAdjOrNounOrDobjWord = new SDWord(predAdjOrNoun,
                                             false, mPaint, correctedWord,
                                             diagramSkeleton.level);
        pred = dep.dep;
        predWord = new SDWord(pred, false, mPaint, correctedWord,
                              diagramSkeleton.level);
        vertDivisions = 3;
        diagramSkeleton.horizTextSize = 30;
        firstVertLineX = canvasWidth / 3;
      } else if (dep.reln.equals("root")) {
        if (!predAdjOrNoun.equals(dep.dep)) {
          pred = dep.dep;
          firstVertLineX = canvasWidth / 2;
          predWord = new SDWord(pred, false, mPaint, correctedWord,
                                diagramSkeleton.level);
          break;
        }
      }
    }

    // Find direct object.
    for (SDDependency dep : dependencies) {
      if (dep.reln.equals("dobj")) {
        dobj = dep.dep;
        predAdjOrNounOrDobjWord = new SDWord(dobj, false, mPaint,
        		                             correctedWord, diagramSkeleton.level);
        vertDivisions = 3;
        diagramSkeleton.horizTextSize = 30;
        firstVertLineX = canvasWidth / 3;
        break;
      }
    }

    // Find subject.
    for (SDDependency dep : dependencies) {
      if (dep.reln.equals("nsubj") &&
          (dep.gov.equals(pred) ||
           dep.gov.equals(predAdjOrNoun))) {
        subject = dep.dep;
        subjectWord = new SDWord(subject, false, mPaint,
                                 correctedWord, diagramSkeleton.level);
        break;
      }
    }

    // Handling command cases where subject "you" is implied.
    if (subject.equals("")) {
      subject = "(you)-0";
      subjectWord = new SDWord(subject, false, mPaint,
 	                             correctedWord, diagramSkeleton.level);
    }


    // Draw horizontal line.
    canvas.drawLine(0 + 20, diagramSkeleton.yCoord, canvasWidth - 20,
                    diagramSkeleton.yCoord, mPaint);

    // Draw vertical division lines.
    canvas.drawLine(firstVertLineX, diagramSkeleton.yCoord - 50,
                    firstVertLineX, diagramSkeleton.yCoord + 50, mPaint);

    if (vertDivisions == 3) {
      // Draw line following pred.
      canvas.drawLine(firstVertLineX * 2 - 30,
                      diagramSkeleton.yCoordTopOfDiagLine,
                      firstVertLineX * 2,
                      diagramSkeleton.yCoord, mPaint);
    }

    // Draw subject, predicate, and (if existing) dobj/pred adj/pred n words.
    mPaint.setTextSize(diagramSkeleton.horizTextSize);

    subjectWord.calculateDimensions();
    subjectWord.onPreDraw();
    canvas.drawText(subjectWord.word,
                    firstVertLineX - subjectWord.dimensions.width() - 20,
                    diagramSkeleton.yCoordToWriteHorizText,
                    mPaint);
    System.out.println("THIS IS TEXTDISTFROMHORIZLINE: " + textDistFromHorizLine);
    System.out.println("THIS IS FIRST VERTICAL X POS: " + firstVertLineX + ", WIDTH OF SUBJECT " + subjectWord.dimensions.width());
    subjectWord.placement.set(
        firstVertLineX - subjectWord.dimensions.width() - 20,
        (int) diagramSkeleton.yCoordToWriteHorizText -
            subjectWord.dimensions.height(),
        firstVertLineX - 20,
        (int) diagramSkeleton.yCoordToWriteHorizText + 20);
    subjectWord.onPostDraw();

    predWord.calculateDimensions();
    predWord.onPreDraw();
    canvas.drawText(predWord.word,
                    firstVertLineX + 20,
                    diagramSkeleton.yCoordToWriteHorizText,
                    mPaint);
    predWord.placement.set(
        firstVertLineX + 20,
        (int) diagramSkeleton.yCoordToWriteHorizText -
            predWord.dimensions.height(),
        firstVertLineX + predWord.dimensions.width() + 20,
        (int) diagramSkeleton.yCoordToWriteHorizText + 20);
    predWord.onPostDraw();

    if (vertDivisions == 3) {
      predAdjOrNounOrDobjWord.calculateDimensions();
      predAdjOrNounOrDobjWord.onPreDraw();
      canvas.drawText(predAdjOrNounOrDobjWord.word,
                      firstVertLineX * 2 - 30 + 50,
                      diagramSkeleton.yCoordToWriteHorizText,
                      mPaint);
      predAdjOrNounOrDobjWord.placement.set(
          firstVertLineX * 2 - 30 + 50,
          (int) diagramSkeleton.yCoordToWriteHorizText -
              predAdjOrNounOrDobjWord.dimensions.height(),
          firstVertLineX * 2 - 30 + predAdjOrNounOrDobjWord.dimensions.width() + 50,
          (int) diagramSkeleton.yCoordToWriteHorizText + 20);
      predAdjOrNounOrDobjWord.onPostDraw();
    }

    // Draw dependencies of subj, pred, and dobj/pred adj/pred n recursively.
    for (SDDependency dep : dependencies) {
      if (dep.gov.equals(subjectWord.word)) {
        dependencyParse(dep.dep, subjectWord, dep.reln,
                        subjectWord.row + 1);
      }
      else if (dep.gov.equals(predWord.word)) {
    	if (!dep.reln.equals("dobj") &&
    	    !dep.reln.equals("nsubj"))
          dependencyParse(dep.dep, predWord, dep.reln,
                          predWord.row + 1);
      }
      else if (vertDivisions == 3 && dep.gov.equals(predAdjOrNounOrDobjWord.word)) {
    	if (!dep.reln.equals("nsubj") &&
    	    !dep.reln.equals("cop"))
          dependencyParse(dep.dep, predAdjOrNounOrDobjWord,
                          dep.reln,
                          predAdjOrNounOrDobjWord.row + 1);
      }
    }
  }


  // Depending on dependency relationship between "dep" and "gov", place "dep"
  // on diagram in relation to "gov".
  public void dependencyParse(String dependent, SDWord governor,
                              String depType, int row) {
    System.out.println("CHILDPARSE CALLED ON " + dependent);
    System.out.println("THIS CHILD HAS A DEP WITH PARENT OF " + depType);
    // Make sure the corresponding sentence diagram row has already been created.
    SDRow r;
    if (rows.size() - 1 < row) {
      // Create row if it doesn't exist.
      r = new SDRow(row, canvasHeight);
      rows.add(r);
    } else {
      r = rows.get(row);
    }

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
          addDiagWord(dependent, governor, r);
          break;
        case IOBJ:
        case PARATAXIS:
        case POBJ:
          addHorizWord(dependent, governor, r);
          break;
        case APPOS:
        case POSSESSIVE:
        case PRT:
          appendWord(dependent, governor, true);
          break;
        case AUX:
        case TMOD:
          appendWord(dependent, governor, false);
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
  public void addDiagWord(String dependent, SDWord governor,
                          SDRow row) {
    // For now, don't handle cases where governor is also diagonal.
    if (governor.isDiagonal)
      return;

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

    canvas.drawLine(diagLineX, row.yCoordTopOfDiagLine,
                    diagLineX + row.diagLineWidth, row.yCoord, mPaint);
    
    canvas.save();
    canvas.rotate(65, diagLineX + 20, row.yCoordToWriteDiagText);
    mPaint.setTextSize(row.diagTextSize);
    canvas.drawText(dependent, diagLineX + 20,
                    row.yCoordToWriteDiagText, mPaint);
    canvas.restore();
    // Add dependent word to governing word's list of dependents.
    SDWord word = new SDWord(dependent, true, mPaint,
                             correctedWord, row.level);
    word.setDiagLineX(diagLineX);
    governor.dependents.add(word);
    
    // Diagram dependencies of the dependent.
    for (SDDependency dep : dependencies) {
      if (dep.gov.equals(word.word)) {
        dependencyParse(dep.dep, word, dep.reln,
                        word.row);
      }
    }
    
  }

  // Add word "dep" to diagram which will be horizontally placed under "gov".
  public void addHorizWord(String dependent, SDWord governor,
                           SDRow row) {
    float diagLineX;
    mPaint.setTextSize(row.horizTextSize);
    SDWord word = new SDWord(dependent, false, mPaint,
                             correctedWord, row.level);
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
      canvas.drawLine(diagLineX, row.yCoordTopOfDiagLine,
                      diagLineX + row.diagLineWidth, row.yCoord, mPaint);
    } else {
      diagLineX = governor.diagLineX;
    }
    // Draw horiz line.
    canvas.drawLine(diagLineX + 30, row.yCoord,
                    diagLineX + 30 + word.dimensions.width() + 70,
                    row.yCoord, mPaint);
    // Draw word on horizontal line.

    word.onPreDraw();
    canvas.drawText(word.word,
                    diagLineX + 30 + 30,
                    row.yCoordToWriteHorizText,
                    mPaint);
    word.placement.set(
        (int) diagLineX + 30 + 30,
        (int) row.yCoordToWriteHorizText - word.dimensions.height(),
        (int) diagLineX + 30 + 30 + word.dimensions.width(),
        (int) row.yCoordToWriteHorizText + 15);
    word.onPostDraw();
    // Save word as part of governor's dependents.
    governor.dependents.add(word);

    // Diagram dependencies of the dependent.
    for (SDDependency dep : dependencies) {
      if (dep.gov.equals(word.word)) {
        dependencyParse(dep.dep, word, dep.reln,
                        word.row + 1);
      }
    }
  }
  
  
  public void appendWord(String dependent, SDWord governor,
                         boolean depFollowsGov) {
	SDRow row = rows.get(governor.row);
    int yPlacement = (int) row.yCoordToWriteHorizText;
    int xLeftPlacement;
    int space = 15;
    mPaint.setTextSize(25);
    SDWord word = new SDWord(dependent, false, mPaint,
                             correctedWord, row.level);
    word.calculateDimensions();
    
    if (depFollowsGov) {
      xLeftPlacement = governor.placement.right + space;
    } else {
      xLeftPlacement = governor.placement.left;
      // Draw white rectangle in place of governor.
      mPaint.setColor(Color.WHITE);
      canvas.drawRect(governor.placement, mPaint);
      mPaint.setColor(Color.BLACK);
      // Redraw governor to follow dependent.
      mPaint.setTextSize(row.horizTextSize);
      governor.onPreDraw();
      canvas.drawText(governor.word,
    		          governor.placement.left + word.dimensions.width() + space,
    		          row.yCoordToWriteHorizText,
    		          mPaint);
      governor.placement.set(
          (int) governor.placement.left + word.dimensions.width() + space,
          (int) row.yCoordToWriteHorizText - governor.dimensions.height(),
          (int) governor.placement.left + word.dimensions.width() + space +
              governor.dimensions.width(),
          (int) row.yCoordToWriteHorizText + 15);
      governor.onPostDraw();
    }
    
    // Draw dependent word.
    word.onPreDraw();
    mPaint.setTextSize(25);
    canvas.drawText(word.word,
                    xLeftPlacement,
                    yPlacement,
                    mPaint);
    word.placement.set(
        (int) xLeftPlacement,
        (int) row.yCoordToWriteHorizText - word.dimensions.height(),
        (int) xLeftPlacement + word.dimensions.width(),
        (int) row.yCoordToWriteHorizText + 15);
    word.onPostDraw();
    // Save word as part of governor's dependents.
    governor.dependents.add(word);
    
    // Diagram dependencies of the dependent.
    for (SDDependency dep : dependencies) {
      if (dep.gov.equals(word.word)) {
        dependencyParse(dep.dep, word, dep.reln,
                        word.row + 1);
      }
    }
  }

}
