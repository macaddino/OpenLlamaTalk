/*
 * FillerWord.java
 *
 * Class for filler words in a sentence diagram.
 */

package com.openllamatalk.helloglass;

public class FillerWord {

  public String word;
  public int count = 0;  // How many times user used this word.

  public FillerWord(String wrd) {
    word = wrd;
    count = 0;
  }

  public void addCount() {
    count++;
  }

}
