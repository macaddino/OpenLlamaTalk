/*
 * ErroneousSentence.java
 *
 * Class for a sentence which contains a grammatical error.
 */

package com.openllamatalk.helloglass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import org.languagetool.rules.RuleMatch;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class ErroneousSentence {

  public String wrongSentence;
  public String correctedSentence;
  public String wordReplacement;  // In case we want word to be red in diagram.
  public String errorType;
  public List<TypedDependency> wordDeps;
  public LexicalizedParser _lp;
  public SentenceDiagram diagram;
  public Uri diagramFile;  // Stored in cacheDir
  public boolean isDiagramComplete;  // Has diagram been written yet?
  public Context context;  // Android context

  public ErroneousSentence(String sentence, RuleMatch error, LexicalizedParser lp,
		                   Context cntxt) {
    wrongSentence = sentence;
    errorType = error.getShortMessage();
    isDiagramComplete = false;
    _lp = lp;
    context = cntxt;

    String replaced_word = wrongSentence.substring(error.getFromPos(),
                                                   error.getToPos());
    wordReplacement = error.getSuggestedReplacements().get(0);
    correctedSentence = wrongSentence.replace(replaced_word,
                                              wordReplacement);
  }


  public void setDiagramComplete(Uri diagramF) {
    diagramFile = diagramF;
    isDiagramComplete = true;
  }


  // Get word dependencies in corrected sentence (such as nsubj, pred, dobj).
  public void getSentenceDependencies() {
    TokenizerFactory<CoreLabel> tokenizerFactory =
        PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
    Tokenizer<CoreLabel> tok =
        tokenizerFactory.getTokenizer(new StringReader(correctedSentence));
    List<CoreLabel> rawWords = tok.tokenize();

    if (_lp == null) {
      System.out.println("LP IS NULL");
    }
    Tree parse = _lp.apply(rawWords);
    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
    wordDeps = gs.typedDependencies(false);
  }
  

  public void makeDiagram() {
    diagram = new SentenceDiagram(wordDeps, wordReplacement);
    diagram.initDiagram();
    Bitmap bmp = diagram.bmp;

    // Create a File for saving an image or video.
    // LATER PUT THIS AS FXN IN SENTENCE DIAGRAMMER FILE.
    File mediaStorageDir = new File(context.getCacheDir()
        + context.getPackageName()
        + "/Files");
    System.out.println("TRYING TO WRITE FILE TO " + context.getCacheDir()
        + context.getPackageName() + "/Files");
    // Create the storage directory if it does not exist.
    if (!mediaStorageDir.exists()) {
      if (!mediaStorageDir.mkdirs()) {
        System.out.println("COULD NOT MAKE STORAGE DIR");
      }
    }
    // Create a media file name
    String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(
        new Date());
    File mediaFile;
    String mImageName = "MI_" + timeStamp + ".jpg";
    mediaFile = new File(mediaStorageDir.getPath() + File.separator
        + mImageName);
    if (mediaFile == null) {
      System.out.println(
          "ERROR CREATING MEDIA FILE, CHECK STORAGE PERMISSIONS");
    }
    try {
      FileOutputStream fos = new FileOutputStream(mediaFile);
      bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
      fos.close();
    } catch (FileNotFoundException e) {
      System.out.println("FILE NOT FOUND" + e.getMessage());
    } catch (IOException e) {
      System.out.println("ERROR ACCESSING FILE" + e.getMessage());
    }

    Uri img = Uri.fromFile(mediaFile);
    this.setDiagramComplete(img);
  }

}
