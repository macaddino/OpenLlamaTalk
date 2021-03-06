/*
 * Magic.java
 * 
 * This is the service which is started from HelloGlass.java, this is where the magic happens.
 */
package com.openllamatalk.helloglass;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import junit.framework.Assert;

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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;


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
  private Context _con;

  private int total_sentences = 0;
  private int total_fillers = 0;
  private List<ErroneousSentence> err_sentences;
  private TextView tot_sen_view = null;
  private TextView err_sen_view = null;
  private TextView filler_view = null;
  private TextView grade_view = null;
  
  private InputStream stanModelIn = null;
  private GZIPInputStream zipStanModelIn = null;
  private JLanguageTool _langTool = null;

  private List<FillerWord> fillerWords = Arrays.asList(
      new FillerWord("um"), new FillerWord("uh"), new FillerWord("er"));

  
  private class LoadModels extends AsyncTask<Context, Integer, Context> {
	// Load OpenNLP's sentence tokenizer and POS tagger models.
    protected Context doInBackground(Context... context) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          card1.setImageLayout(Card.ImageLayout.FULL);
          card1.addImage(R.drawable.load);
          // card1.setText("LOADING MODELS");
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
       
        String grade =  getReportCardGrade(total_sentences,
                                           err_sentences.size(),
                                           total_fillers);
        
        //tot_sen_view.setText(total_sentences);
        //err_sen_view.setText(err_sentences.size());
		//filler_view.setText(total_fillers);
		//grade_view.setText(grade);
        
        dashboardCard.setText("Report Card\nTotal Sentences: " +
                              total_sentences + "\nGrammar Errors: " +
                              err_sentences.size() + "\nFiller Word Count: " +
                              total_fillers + "\nGrade: " + grade);

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
      //LayoutInflater li = LayoutInflater.from(_con);
      //return li.inflate(R.layout.l2, null, true);
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
      if (position == 0 && err_sentences.size() > 0) {
        dashOptionsView.deactivate();
        senCardsView.activate();
        setContentView(senCardsView);
      } else if (position == 1 && total_fillers > 0) {
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
      Socket sock;
      List<SDDependency> dep = null;
      try {
        // FOR UNITVERSTIY OF CHICAGO NETWORKS
        // sock = new Socket("10.150.32.49", 1149);
        // FOR HOME APARTMENT NETWORKS
        sock = new Socket("2602:306:37ef:240:61e8:a9b8:abb:bee5", 1149);
        System.out.println("CONNECTING...");

        OutputStreamWriter osw;
        osw = new OutputStreamWriter(sock.getOutputStream(), "UTF-8");
        osw.write(sentence.correctedSentence, 0, sentence.correctedSentence.length());
        osw.flush();
        sock.shutdownOutput();

        InputStream socketStream = sock.getInputStream();
        ObjectInputStream objectInput = new ObjectInputStream(socketStream);
        dep = (List<SDDependency>) objectInput.readObject();
        if (dep != null)
          System.out.println("DEPS IS NOT NULL");
        sock.close();
 
      } catch (UnknownHostException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
		    e.printStackTrace();
	    }
	
      sentence.getSentenceDependencies(dep);
      sentence.makeDiagram();

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
      err_sentences.add(sentence);
      senCardsView.activate();
      senCards.add(newCard);
      senCardsView.updateViews(true);
      senCardsView.deactivate();
      dashboardLock = false;
      
      return sentence;
    }

    protected void onPostExecute(ErroneousSentence sentence) {
      
      
      
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
    _con = this;
    
    //setContentView(R.layout.l2);
    //tot_sen_view = (TextView) this.findViewById(R.id.sentenceCount);
    //err_sen_view = (TextView) this.findViewById(R.id.grammarErrorCount);
    //filler_view = (TextView) this.findViewById(R.id.fillerCount);
	//grade_view = (TextView) this.findViewById(R.id.grade);
    
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


      // Find filler words - abstract to other function?
      boolean set_card = false;
      String[] sen_words = best_match.split(" ");
      for (String word : sen_words) {
        for (FillerWord filler : fillerWords) {
          if (word.equals(filler.word)) {
            filler.addCount();
            total_fillers++;
            for (Card fillerCard : fillerCards) {
              // Upon filler match, increment filler card count.
              if (fillerCard.getText().equals(word)) {
                fillerCard.setFootnote(String.valueOf(filler.count));
                set_card = true;
                break;
              }
            }
            if (!set_card) {
              // If filler card not already created, create it.
              Card fillerCard = new Card(this);
              fillerCard.setText(filler.word);
              fillerCard.setFootnote(String.valueOf(filler.count));
              fillerCards.add(fillerCard);
              fillerView.updateViews(true);
            }
          }
        }
      }

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
            null,
            this);
        
        // Create sentence visualization in background.
        new AddErrorSentence().execute(sentence);
      }

      // Display options menu again.
      setContentView(mainMenuCardsView);
    }
	
    super.onActivityResult(requestCode, resultCode, data);
  }

  
  /**
   * Output report card grade.
   */
  private String getReportCardGrade(int total_sen, int grammar_errors,
                                    int fillers) {
    double errorDeduction;
    double gradePercentage;
    double prevalenceRate = 0.3;

    // Gain a rough estimate of the word count
    // This also has the positive side effect of rewarding sentence complexity.
    int totalWords = (int) ((double) total_sen / prevalenceRate);

    // Count grammar errors as 2 errors because they arise as problems
    // between at least two words.
    errorDeduction = 2.0 * (double) grammar_errors;
    // Vocal fillers are a single error each.
    errorDeduction += (double) fillers;
    // But fillers also reduce relative sentence length.
    totalWords -= fillers;

    // Compute grade as a percentage.
    gradePercentage = ((double) totalWords - errorDeduction) / totalWords;
    // Convert to a letter grade using social conventions.
    if (gradePercentage > .9) {
      return "A";
    } else if (gradePercentage > .8) {
      return "B";
    } else if (gradePercentage > .7) {
      return "C";
    } else if (gradePercentage > .6) {
      return "D";
    } else {
      return "F";
    }
  }

}
