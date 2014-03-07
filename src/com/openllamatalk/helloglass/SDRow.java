/*
 * SDRow.java
 *
 * Class containing information about a row in a sentence diagram.
 * A row is constituted as a horizontal line and any diagonal lines
 * which are appended above it. For example, this is a row:
 *                __\______\______\__
 */

package com.openllamatalk.helloglass;

public class SDRow {

  public float yCoord;  // y coordinate of horiz line.
  public float yCoordToWriteHorizText;
  public float yCoordTopOfDiagLine;
  public float yCoordToWriteDiagText;
  public float diagLineWidth;  // diagLineRightXCoord - diagLineLeftXCoord
  public int horizTextSize;  // Text size of text above horiz line.
  public int diagTextSize;
  public int level;  // Level of row, where row zero represents
                     // diagram skeleton.

  public SDRow(int rowLevel, float canvasHeight) {
    // Set row variables in respect to the level.
    // For now, rows 1 - x will have identical characteristics, while
    // row 0 will have a slightly larger text size.
    level = rowLevel;
    if (level == 0) {
      // Skeleton row does not have text written on it's appended diag lines.
      yCoordToWriteDiagText = 0;
      diagTextSize = 0;

      yCoord = canvasHeight / 2;
      yCoordToWriteHorizText = yCoord - 40;
      yCoordTopOfDiagLine = yCoord - 50;
      diagLineWidth = 30;
      horizTextSize = 50;
    } else {
      yCoord = canvasHeight / 2 + (level * 70);
      yCoordToWriteHorizText = yCoord - 20;
      yCoordTopOfDiagLine = yCoord - 70;
      yCoordToWriteDiagText = yCoordTopOfDiagLine + 20;
      diagLineWidth = 30;  
      horizTextSize = 25;
      diagTextSize = 25;  
    }
  }

}
