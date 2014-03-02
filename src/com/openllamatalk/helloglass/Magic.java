/*
 * Magic.java
 * 
 * This is the service which is started from HelloGlass.java, this is where the magic happens.
 */
package com.openllamatalk.helloglass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

/*
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
*/

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.glass.app.Card;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
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

public class Magic extends Activity {
	
  private static final int REQUEST_CODE = 1234;
  private Card card1;
  private List<ErroneousSentence> sentences;
  private GestureDetector mGestureDetector;
  /*
  private InputStream modelIn = null;
  private InputStream tokenModelIn = null;
  */
  private InputStream stanModelIn = null;
  private GZIPInputStream zipStanModelIn = null;
  private LexicalizedParser _lp = null;
  /*
  private Tokenizer _tokenizer = null;
  private POSTagger _posTagger = null;
  */
  private JLanguageTool _langTool = null;
  
  private class LoadModels extends AsyncTask<Context, Integer, Context> {
	// Load OpenNLP's sentence tokenizer and POS tagger models.
    protected Context doInBackground(Context... context) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          card1.setText("LOADING MODELS");
          View card1View = card1.toView();
          setContentView(card1View);
        }
      });

      try {
          // Loading LanguageTool grammatical correction tool.
      	Language lang = new AmericanEnglish();

      	_langTool = new JLanguageTool(context[0], lang);
      	
        _langTool.activateDefaultPatternRules();
      } catch (final IOException ioe) {
        ioe.printStackTrace();
      }
      
	    AssetManager assetManager = context[0].getAssets();

	    try {
	      // Loading Stanford Dependency parsing model.
	      stanModelIn = assetManager.open("englishPCFG");
	    
	      try {
	        zipStanModelIn = new GZIPInputStream(stanModelIn);
	      
	        try {
	          ObjectInputStream ios = new ObjectInputStream(zipStanModelIn);
	          _lp = LexicalizedParser.loadModel(ios);

	        } catch (IOException e) {
	          e.printStackTrace();
	        }
	      } catch (IOException e) {
	        e.printStackTrace();
	      }
	    } catch (final IOException e) {
        e.printStackTrace();
	    }
	  
	    /*
      try {
        // Loading tokenizer model.
        tokenModelIn = assetManager.open("en-token.bin");
        final TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
        tokenModelIn.close();
          
        _tokenizer = new TokenizerME(tokenModel);
      } catch (final IOException ioe) {
        ioe.printStackTrace();
      } finally {
        if (tokenModelIn != null) {
          try {
            tokenModelIn.close();
          } catch (final IOException e) {} // oh well!
        }
      }
      
      try {
        // Loading POS tagger model.
        modelIn = assetManager.open("en-pos-maxent.bin");
        final POSModel posModel = new POSModel(modelIn);
        modelIn.close();
        
        _posTagger = new POSTaggerME(posModel);
      } catch (final IOException ioe) {
        ioe.printStackTrace();
      } finally {
        if (modelIn != null) {
          try {
            modelIn.close();
          } catch (final IOException e) {} // oh well!
        }
      }
      */

    
      return context[0];
    }
      

    // After loading models, allow user to tap Glass,
    // thus triggering voice recognition.
    protected void onPostExecute(Context context) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          card1.setText("DONE LOADING MODELS, PLEASE TAP");
          View card1View = card1.toView();
          setContentView(card1View);
        }
      });
      mGestureDetector = createGestureDetector(context);
    }
    
  }
 
 
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    /*
     * We're creating a card for the interface.
     * 
     * More info here: http://developer.android.com/guide/topics/ui/themes.html
     */
    card1 = new Card(this);
    sentences = new ArrayList<ErroneousSentence>();
	
    // Alert user if no recognition service is present.
    PackageManager pm = getPackageManager();
    List<ResolveInfo> activities = pm.queryIntentActivities(
        new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
    if (activities.size() == 0) {
      card1.setText("RECOGNIZER NOT PRESENT");
      View card1View = card1.toView();
      setContentView(card1View);
    }
    
    new LoadModels().execute(this);
  }

  private GestureDetector createGestureDetector(Context context) {
    GestureDetector gestureDetector = new GestureDetector(context);
    // Create a base listener for generic gestures.
    gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
      @Override
      public boolean onGesture(Gesture gesture) {
        if (gesture == Gesture.TAP) {
          // do something on tap
      
          startVoiceRecognitionActivity();
          return true;
        }
        return false;
      }
    });

    return gestureDetector;
  }
  
  /**
   * Send generic motion events to the gesture detector.
   */
  public boolean onGenericMotionEvent(MotionEvent event) {
     if (mGestureDetector != null) {
    	 return mGestureDetector.onMotionEvent(event);
     }
     return false;
  }

  /**
   * Fire an intent to start the voice recognition activity.
   */
  private void startVoiceRecognitionActivity() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");
    startActivityForResult(intent, REQUEST_CODE);
  }

  /**
   * Handle the results from the voice recognition activity.
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode,
                                  Intent data) {
    if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
      // Populate the card with the String value of the highest confidence rating which the recognition engine thought it heard.
      ArrayList<String> matches = data.getStringArrayListExtra(
          RecognizerIntent.EXTRA_RESULTS);
      // Get first match (which has highest confidence value)

      String best_match = matches.get(0);

      // FIND PARTS OF SPEECH OF SPOKEN TEXT - ABSTRACT TO OTHER FUNCTION?
      
      //String[] all_tokens = _tokenizer.tokenize(best_match);
      //String[] all_pos = _posTagger.tag(all_tokens);
      
      // card1.setText(Arrays.toString(all_pos)); // Main text area
      // View card1View = card1.toView();
      
      // DETECT GRAMMATICAL ERRORS - ABSTRACT TO OTHER FUNCTION?
          
      List<RuleMatch> error_matches = new ArrayList<RuleMatch>();
      try {
        error_matches = _langTool.check(best_match);
      } catch (final IOException ioe) {
        ioe.printStackTrace();
      }
      if (error_matches.size() > 0) {
        // Only account for first error detected in sentence.
        ErroneousSentence sentence = new ErroneousSentence(
            best_match,
            error_matches.get(0));
        sentences.add(sentence);
        
        sentence.getSentenceDependencies(_lp);
        sentence.makeDiagram(this);
      }

      // For now, just displaying the diagram of the latest erroneous sentence.
      // Change later.
      if (!sentences.isEmpty()) {
        Uri img = sentences.get(sentences.size()-1).diagramFile;
        card1.setText("");
        card1.setImageLayout(Card.ImageLayout.FULL);
        card1.clearImages();
        card1.addImage(img);
      } else {
    	card1.clearImages();
        card1.setText(best_match);
      }
      View card1View = card1.toView();
      setContentView(card1View);
    }
	
    super.onActivityResult(requestCode, resultCode, data);
  }

}
