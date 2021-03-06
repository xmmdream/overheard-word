package com.b50.overheard;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.b50.gesticulate.SwipeDetector;
import com.b50.savvywords.Definition;
import com.b50.savvywords.Word;
import com.b50.savvywords.WordStudyEngine;

public class OverheardWord extends Activity {

	private static final String APP = "overheardword";
	private GestureDetector gestureDetector;
	private static WordStudyEngine engine;
	private static LinkedList<Word> wordsViewed;
	private static int viewPosition = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(APP, "onCreated Invoked");

		setContentView(R.layout.activity_overheard_word);

		initializeGestures();

		List<Word> words = buildWordList();

		if (engine == null) {
			engine = WordStudyEngine.getInstance(words);
		}

		if (wordsViewed == null) {
			wordsViewed = new LinkedList<Word>();
		}

		Word firstWord = engine.getWord();
		wordsViewed.add(firstWord);
		viewPosition++;
		displayWord(firstWord);
	}

	private void displayWord(Word aWord) {
		TextView wordView = (TextView) findViewById(R.id.word_study_word);
		wordView.setText(aWord.getSpelling());

		Definition firstDef = aWord.getDefinitions().get(0);
		TextView wordPartOfSpeechView = (TextView) findViewById(R.id.word_study_part_of_speech);
		wordPartOfSpeechView.setText(firstDef.getPartOfSpeech());

		TextView defView = (TextView) findViewById(R.id.word_study_definition);
		defView.setText(formatDefinition(aWord));
	}

	private String formatDefinition(final Word startingWord) {
		return formatDefinition(startingWord.getDefinitions().get(0).getDefinition());
	}

	private String formatDefinition(final String definition) {
		String firstChar = definition.substring(0, 1).toUpperCase(Locale.ENGLISH);
		StringBuffer buff = new StringBuffer(firstChar);
		buff.append(definition.substring(1, (definition.length() + 0)));
		if (!definition.endsWith(".")) {
			buff.append(".");
		}
		return buff.toString();
	}

	private List<Word> buildWordList() {
		InputStream resource = getApplicationContext().getResources().openRawResource(R.raw.words);
		List<Word> words = new ArrayList<Word>();
		try {
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(resource));
			String read = br.readLine();

			while (read != null) {
				sb.append(read);
				read = br.readLine();
			}

			JSONObject document = new JSONObject(sb.toString());
			JSONArray allWords = document.getJSONArray("words");
			for (int i = 0; i < allWords.length(); i++) {
				words.add(Word.manufacture(allWords.getJSONObject(i)));
			}

		} catch (Exception e) {
			Log.e(APP, "Exception in getInstance for WordEngine: " + e.getLocalizedMessage());
		}
		return words;
	}

	private void initializeGestures() {
		gestureDetector = initGestureDetector();

		View view = findViewById(R.id.LinearLayout1);

		view.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
			}
		});

		view.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
	}

	private GestureDetector initGestureDetector() {
		return new GestureDetector(new SimpleOnGestureListener() {

			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				try {
					final SwipeDetector detector = new SwipeDetector(e1, e2, velocityX, velocityY);
					if (detector.isDownSwipe()) {
						return false;
					} else if (detector.isUpSwipe()) {
						startActivity(new Intent(getApplicationContext(), OverheardQuiz.class));
					} else if (detector.isLeftSwipe()) {
						if (listSizeAndPositionEql()) {
							viewPosition++;
							Word wrd = engine.getWord();
							wordsViewed.add(wrd);
							displayWord(wrd);
						} else if (wordsViewed.size() > (viewPosition + 1)) {
							if (viewPosition == -1) {
								viewPosition++;
							}
							displayWord(wordsViewed.get(++viewPosition));
						} else {
							return false;
						}
					} else if (detector.isRightSwipe()) {
						if (wordsViewed.size() > 0 && (listSizeAndPositionEql() || (viewPosition >= 0))) {
							displayWord(wordsViewed.get(--viewPosition));
						} else {
							return false;
						}
					}
				} catch (Exception e) {
					// nothing
				}
				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.overheard_word, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.quit_item:
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(APP, "onResume Invoked");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(APP, "onStart Invoked");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(APP, "onPause Invoked");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(APP, "onRestart Invoked");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(APP, "onStop Invoked");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(APP, "onDestroy Invoked");
	}

	private boolean listSizeAndPositionEql() {
		return wordsViewed.size() == (viewPosition + 1);
	}
}
