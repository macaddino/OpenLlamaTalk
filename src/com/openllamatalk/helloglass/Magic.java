/*
 * Magic.java
 * 
 * This is the service which is started from HelloGlass.java, this is where the magic happens.
 */
package com.openllamatalk.helloglass;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import junit.framework.Assert;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class Magic extends Activity {
	
  private static final int REQUEST_CODE = 1234;
  private ArrayList<Card> mainMenuCards = new ArrayList<Card>();
  private Card dashboardCard;
  private ArrayList<Card> dashOptionsCards = new ArrayList<Card>();
  private ArrayList<Card> fillerCards = new ArrayList<Card>();
  private ArrayList<Card> senCards = new ArrayList<Card>();
  
  private ArrayList<String> mainMenuText = new ArrayList<String>(
      Arrays.asList("Say a sentence", "Dashboard"));
  private ArrayList<String> dashOptionsText = new ArrayList<String>(
      Arrays.asList("Grammatical errors", "Filler words",
    		        "Return to main menu"));
  
  private CardScrollView mainMenuCardsView = null;
  private CardScrollView dashboardView = null;
  private CardScrollView dashOptionsView = null;
  private CardScrollView fillerView = null;
  private CardScrollView senCardsView = null;
  private boolean dashboardLock = false;
  private Card card1;

  private int total_sentences = 0;
  private List<ErroneousSentence> err_sentences;
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
          mainMenuCardsView.activate();
          setContentView(mainMenuCardsView);
        }
      });
    }
    
  }

 
  private class mainMenuAdapter extends CardScrollAdapter
      implements OnItemClickListener {

    @Override
    public int findIdPosition(Object id) {
      return -1;
    }

    @Override
    public int findItemPosition(Object item) {
      return mainMenuCards.indexOf(item);
    }

    @Override
    public int getCount() {
      return mainMenuCards.size();
    }

    @Override
    public Object getItem(int position) {
      return mainMenuCards.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      return mainMenuCards.get(position).toView();
    }

    public void onItemClick(AdapterView<?> parent, View v, int position,
                            long id) {
      if (this.getItem(position) == mainMenuCards.get(0)) {
        // Record sentence.
        startVoiceRecognitionActivity();
      } else if (this.getItem(position) == mainMenuCards.get(1)) {
        // Display dashboard if erroneous sentences have been made.
        while (dashboardLock) {
          // Sleep 2 seconds if dashboard is being modified.
          SystemClock.sleep(2000);
        }
        dashboardLock = true;
        mainMenuCardsView.deactivate();
        
        String total_sentences_plural = "s have";
        String err_sentences_plural = "s have";
        if (total_sentences == 1) {
          total_sentences_plural = " has";
        }
        if (err_sentences.size() == 1) {
          err_sentences_plural = " has";
        }

        dashboardCard.setText(total_sentences + " sentence" +
                              total_sentences_plural + " been spoken.\n" +
                              err_sentences.size() + " grammar error" +
                              err_sentences_plural + " been made.");

        dashboardView.updateViews(true);
        dashboardView.activate();
        setContentView(dashboardView);
      }
    }
  }
  

  private class dashboardAdapter extends CardScrollAdapter
      implements OnItemClickListener {
    
    @Override
	public int findIdPosition(Object id) {
	  return -1;
	}
    
    @Override
    public int findItemPosition(Object item) {
      return 0;
    }
    
    @Override
    public int getCount() {
      return 1;
    }
    
    @Override
    public Object getItem(int position) {
      return dashboardCard;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      return dashboardCard.toView();
    }
    
    public void onItemClick(AdapterView<?> parent, View v, int position,
            long id) {
      Assert.assertEquals(dashboardLock, true);
      dashboardView.deactivate();
      dashOptionsView.activate();
      setContentView(dashOptionsView);
    }
  }

  
  private class dashOptionsAdapter extends CardScrollAdapter
      implements OnItemClickListener {
    
    @Override
	public int findIdPosition(Object id) {
	  return -1;
	}
    
    @Override
    public int findItemPosition(Object item) {
      return dashOptionsCards.indexOf(item);
    }
    
    @Override
    public int getCount() {
      return dashOptionsCards.size();
    }
    
    @Override
    public Object getItem(int position) {
      return dashOptionsCards.get(position);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      return dashOptionsCards.get(position).toView();
    }
    
    public void onItemClick(AdapterView<?> parent, View v, int position,
            long id) {
      Assert.assertEquals(dashboardLock, true);
      // Look at grammatical errors.
      if (position == 0 && !senCards.isEmpty()) {
        dashOptionsView.deactivate();
        senCardsView.activate();
        setContentView(senCardsView);
      } else if (position == 1 && !fillerCards.isEmpty()) {
      // Look at fillers.
        dashOptionsView.deactivate();
        fillerView.activate();
        setContentView(fillerView);
      } else if (position == 2) {
        // Return to main menu.
        dashboardLock = false;
        dashOptionsView.deactivate();
        mainMenuCardsView.activate();
        setContentView(mainMenuCardsView);
      }
    }
  }
  
  
  private class fillerWordsAdapter extends CardScrollAdapter
      implements OnItemClickListener {
    
    @Override
    public int findIdPosition(Object id) {
        return -1;
    }
    
    @Override
    public int findItemPosition(Object item) {
      return fillerCards.indexOf(item);
    }
    
    @Override
    public int getCount() {
      return fillerCards.size();
    }
    
    @Override
    public Object getItem(int position) {
      return fillerCards.get(position);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      return fillerCards.get(position).toView();
    }
    
    public void onItemClick(AdapterView<?> parent, View v, int position,
            long id) {
      // Return to dashboard.
      Assert.assertEquals(dashboardLock, true);
      fillerView.deactivate();
      dashboardView.activate();
      setContentView(dashboardView);
    }
  }
  
  
  private class sentenceAdapter extends CardScrollAdapter 
      implements OnItemClickListener {

    @Override
    public int findIdPosition(Object id) {
      return -1;
    }

    @Override
    public int findItemPosition(Object item) {
      return senCards.indexOf(item);
    }

    @Override
    public int getCount() {
      return senCards.size();
    }

    @Override
    public Object getItem(int position) {
      return senCards.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      return senCards.get(position).toView();
    }

    public void onItemClick(AdapterView<?> parent, View v, int position,
                            long id) {
      // Return to dashboard.
      Assert.assertEquals(dashboardLock, true);
      senCardsView.deactivate();
      dashboardView.activate();
      setContentView(dashboardView);
    }

  }


  private class AddErrorSentence extends AsyncTask<
      ErroneousSentence, Integer, ErroneousSentence> {
  // Load an erroneous sentence's information/diagrams into respective cards.
    protected ErroneousSentence doInBackground(ErroneousSentence... sen) {
      ErroneousSentence sentence = sen[0];
      sentence.getSentenceDependencies();
      sentence.makeDiagram();

      return sentence;
    }

    protected void onPostExecute(ErroneousSentence sentence) {
      err_sentences.add(sentence);
      
      Card newCard = new Card(sentence.context);
      newCard.setImageLayout(Card.ImageLayout.FULL);
      newCard.addImage(sentence.diagramFile);
      newCard.setFootnote(sentence.errorType);
      // Should there be a lock around senCards here?
      while (dashboardLock) {
        // Sleep 2 seconds if dashboard is being viewed.
        SystemClock.sleep(2000);
      }
      dashboardLock = true;
      senCards.add(newCard);
      dashboardLock = false;
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
    err_sentences = new ArrayList<ErroneousSentence>();
    
    for (int i = 0; i < mainMenuText.size(); ++i) {
      Card newCard = new Card(this);
      newCard.setImageLayout(Card.ImageLayout.FULL);
      newCard.setText(mainMenuText.get(i));
      mainMenuCards.add(newCard);
    }

    mainMenuCardsView = new CardScrollView(this);
    mainMenuAdapter mMenuAdapter = new mainMenuAdapter();
    mainMenuCardsView.setAdapter(mMenuAdapter);
    mainMenuCardsView.setOnItemClickListener(mMenuAdapter);
    
    dashboardCard = new Card(this);
    dashboardCard.setImageLayout(Card.ImageLayout.FULL);
    dashboardView = new CardScrollView(this);
    dashboardAdapter dashAdapter = new dashboardAdapter();
    dashboardView.setAdapter(dashAdapter);
    dashboardView.setOnItemClickListener(dashAdapter);
    
    for (int i = 0; i < dashOptionsText.size(); ++i) {
      Card newCard = new Card(this);
      newCard.setImageLayout(Card.ImageLayout.FULL);
      newCard.setText(dashOptionsText.get(i));
      dashOptionsCards.add(newCard);
    }
    
    dashOptionsView = new CardScrollView(this);
    dashOptionsAdapter dashOptAdapter = new dashOptionsAdapter();
    dashOptionsView.setAdapter(dashOptAdapter);
    dashOptionsView.setOnItemClickListener(dashOptAdapter);
    
    fillerView = new CardScrollView(this);
    fillerWordsAdapter fillerAdapter = new fillerWordsAdapter();
    fillerView.setAdapter(fillerAdapter);
    fillerView.setOnItemClickListener(fillerAdapter);

    senCardsView = new CardScrollView(this);
    sentenceAdapter senAdapter = new sentenceAdapter();
    senCardsView.setAdapter(senAdapter);
    senCardsView.setOnItemClickListener(senAdapter);
	
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
      total_sentences++;

      // Get first speech to text match (which has highest confidence value)
      ArrayList<String> matches = data.getStringArrayListExtra(
          RecognizerIntent.EXTRA_RESULTS);
      String best_match = matches.get(0);

      // FIND PARTS OF SPEECH OF SPOKEN TEXT - ABSTRACT TO OTHER FUNCTION?
      
      //String[] all_tokens = _tokenizer.tokenize(best_match);
      //String[] all_pos = _posTagger.tag(all_tokens);
      
      // card1.setText(Arrays.toString(all_pos)); // Main text area
      // View card1View = card1.toView();
      
      // Detect grammatical errors and create dashboard entry for all sentences
      // with errors.
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
            error_matches.get(0),
            _lp,
            this);
        
        // Create sentence visualization in background.
        new AddErrorSentence().execute(sentence);
      }

      // Display options menu again.
      setContentView(mainMenuCardsView);
    }
	
    super.onActivityResult(requestCode, resultCode, data);
  }

}
