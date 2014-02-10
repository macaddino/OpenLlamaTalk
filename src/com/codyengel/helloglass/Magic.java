/*
 * Magic.java
 * @author Cody Engel
 * http://codyengel.info
 * 
 * This is the service which is started from HelloGlass.java, this is where the magic happens.
 */
package com.codyengel.helloglass;

import com.google.android.glass.app.Card;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.List;

public class Magic extends Activity {

  private static final int REQUEST_CODE = 1234;
  private Card card1;
  private GestureDetector mGestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * We're creating a card for the interface.
		 * 
		 * More info here: http://developer.android.com/guide/topics/ui/themes.html
		 */
	card1 = new Card(this);
    mGestureDetector = createGestureDetector(this);


    card1.setText("TAP TO RECORD SPEECH"); // Main text area

    // Alert user if no recognition service is present.
    PackageManager pm = getPackageManager();
    List<ResolveInfo> activities = pm.queryIntentActivities(
        new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
    if (activities.size() == 0)
    {
      card1.setText("RECOGNIZER NOT PRESENT");
    }
		View card1View = card1.toView();
		
		// Display the card we just created
		setContentView(card1View);
	}

  private GestureDetector createGestureDetector(Context context)
  {
    GestureDetector gestureDetector = new GestureDetector(context);
    // Create a base listener for generic gestures.
    gestureDetector.setBaseListener(new GestureDetector.BaseListener()
    {
      @Override
      public boolean onGesture(Gesture gesture)
      {
        if (gesture == Gesture.TAP)
        {
          // do something on tap
          // Such as creating a new card and putting some info into it.
          card1.setText("TAPPED GLASS"); // Main text area
          View card1View = card1.toView();
      
          // Display the card we just created
          setContentView(card1View);
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
  public boolean onGenericMotionEvent(MotionEvent event)
  {
     if (mGestureDetector != null)
     {
    	 return mGestureDetector.onMotionEvent(event);
     }
     return false;
  }

  /**
   * Fire an intent to start the voice recognition activity.
   */
  private void startVoiceRecognitionActivity()
  {
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
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
    {
      // Populate the card with the String value of the highest confidence rating which the recognition engine thought it heard.
      ArrayList<String> matches = data.getStringArrayListExtra(
          RecognizerIntent.EXTRA_RESULTS);
      // Get first match (which has highest confidence value)

      card1.setText(matches.get(0)); // Main text area
      View card1View = card1.toView();
      
      // Display the card we just created
      setContentView(card1View);
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

}
