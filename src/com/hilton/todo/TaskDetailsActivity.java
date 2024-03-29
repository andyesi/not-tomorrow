package com.hilton.todo;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.hilton.todo.TaskStore.PomodoroIndex;
import com.hilton.todo.TaskStore.TaskColumns;

public class TaskDetailsActivity extends Activity {

    public static final String ACTION_VIEW_DETAILS = "com.hilton.todo.VIEW_TASK_DETAILS";
    public static final String EXTRA_TASK_CONTENT = "task_content";
    public static final String EXTRA_TASK_STATUS = "task_status";
    public static final String EXTRA_INTERRUPTS_COUNT = "interrupts_count";
    public static final String EXTRA_SPENT_POMODOROS = "spent_pomodoros";
    private static final String TAG = null;
    
    private RatingBar mExpected_1;
    private RatingBar mExpected_2;
    private RatingBar mSpent_1;
    private RatingBar mSpent_2;
    private int mSpentPomodoros;
    private Toast mTaskTooBigNoti;
    private Cursor mCursor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.task_details);
	getWindow().setBackgroundDrawableResource(R.drawable.pomodoro_background);
	final Uri uri = getIntent().getData();
	final boolean taskIsDone = getIntent().getBooleanExtra(EXTRA_TASK_STATUS, false);
	
	instantiateExpected(uri, taskIsDone);
	
	mSpent_1 = (RatingBar) findViewById(R.id.spent_1);
	mSpent_2 = (RatingBar) findViewById(R.id.spent_2);
	
	mCursor = TaskStore.getTaskDetails(uri, getContentResolver());
	mCursor.moveToFirst();
	
	mSpentPomodoros = mCursor.getInt(PomodoroIndex.SPENT);
	
	setInterrupts();
	
	final Button startPomodoro = (Button) findViewById(R.id.start_pomodoro);
	startPomodoro.setOnClickListener(new View.OnClickListener() {
	    private Toast mOverflowNoti;

	    @Override
	    public void onClick(View v) {
		final Intent si = new Intent(getApplication(), PomodoroClockService.class);
		si.putExtra(EXTRA_TASK_CONTENT, getIntent().getStringExtra(EXTRA_TASK_CONTENT));
		si.putExtra(EXTRA_INTERRUPTS_COUNT, mCursor.getInt(PomodoroIndex.INTERRUPTS));
		si.putExtra(EXTRA_SPENT_POMODOROS, mSpentPomodoros);
		si.setData(uri);
		startService(si);
		
		final Intent i = new Intent(getApplication(), PomodoroClockActivity.class);
		i.putExtra(EXTRA_TASK_CONTENT, getIntent().getStringExtra(EXTRA_TASK_CONTENT));
		i.putExtra(EXTRA_INTERRUPTS_COUNT, mCursor.getInt(PomodoroIndex.INTERRUPTS));
		i.putExtra(EXTRA_SPENT_POMODOROS, mSpentPomodoros);
		i.setData(uri);
		startActivity(i);
		overridePendingTransition(R.anim.activity_enter_in, R.anim.activity_enter_out);
	    }
	});
	startPomodoro.setEnabled(!taskIsDone);
	
	final TextView statusPanel = (TextView) findViewById(R.id.status_panel);
	final String taskContent = getIntent().getStringExtra(EXTRA_TASK_CONTENT);
	statusPanel.setText(taskContent);
	if (taskIsDone) {
	    final Spannable style = new SpannableString(taskContent);
	    style.setSpan(new StrikethroughSpan(), 0, taskContent.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
	    style.setSpan(new StyleSpan(Typeface.ITALIC) , 0, taskContent.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
	    statusPanel.setText(style);
	    statusPanel.setTextAppearance(this, R.style.done_task_item_text);
	} else {
	    statusPanel.setTextAppearance(this, R.style.task_item_text);
	}
    }

    private void setInterrupts() {
	mCursor.moveToFirst();
	final RatingBar interrupts_1 = (RatingBar) findViewById(R.id.interrupts_1);
	final RatingBar interrupts_2 = (RatingBar) findViewById(R.id.interrupts_2);
	final int interruptsCount = mCursor.getInt(PomodoroIndex.INTERRUPTS);
	if (interruptsCount <= 6) {
	    interrupts_1.setRating(interruptsCount);
	    interrupts_2.setVisibility(View.GONE);
	    interrupts_2.setRating(0);
	} else if (interruptsCount <= 12){
	    interrupts_1.setRating(6);
	    interrupts_2.setVisibility(View.VISIBLE);
	    interrupts_2.setRating(interruptsCount - 6);
	} else {
	    interrupts_1.setRating(6);
	    interrupts_2.setVisibility(View.VISIBLE);
	    interrupts_2.setRating(interruptsCount - 6);
	}
    }

    @Override
    protected void onStart() {
	mCursor.requery();
	mCursor.moveToFirst();
	mSpentPomodoros = mCursor.getInt(PomodoroIndex.SPENT);
	setExpectedRating(mCursor.getInt(PomodoroIndex.EXPECTED));
	setSpentRating();
	setInterrupts();
	
	super.onStart();
    }

    @Override
    protected void onStop() {
	super.onStop();
    }

    @Override
    protected void onDestroy() {
	mCursor.close();
	super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
	super.onBackPressed();
	overridePendingTransition(R.anim.activity_leave_in, R.anim.activity_leave_out);
    }
    
    private void setExpectedRating(int expected) {
	if (expected >= 6) {
	    mExpected_1.setRating(6);
	    mExpected_2.setRating(expected - 6);
	} else {
	    mExpected_1.setRating(expected);
	    mExpected_2.setRating(0);
	}
    }

    private void setSpentRating() {
	if (mSpentPomodoros >= 6) {
	    mSpent_1.setRating(6);
	    mSpent_2.setRating(mSpentPomodoros - 6);
	} else {
	    mSpent_1.setRating(mSpentPomodoros);
	    mSpent_2.setRating(0);
	}
    }

    private void instantiateExpected(final Uri uri, final boolean isIndicator) {
	mExpected_1 = (RatingBar) findViewById(R.id.expected_1);
	mExpected_1.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
	    @Override
	    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
		if (!fromUser) {
		    return;
		}
		updateExpected(uri, rating, mExpected_2);
	    }

	});
	mExpected_1.setIsIndicator(isIndicator);
	mExpected_2 = (RatingBar) findViewById(R.id.expected_2);
	mExpected_2.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
	    @Override
	    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
		if (!fromUser) {
		    return;
		}
		updateExpected(uri, rating, mExpected_1);
	    }
	});
	mExpected_2.setIsIndicator(isIndicator);
    }
    
    private void updateExpected(final Uri uri, float rating, RatingBar another) {
	final int expected = (int) rating + (int) another.getRating();
	if (expected > 6) {
	    warnTaskTooBig();
	}
	final ContentValues values = new ContentValues(1);
	values.put(TaskColumns.EXPECTED, expected);
	getContentResolver().update(uri, values, null, null);
    }

    private void warnTaskTooBig() {
	if (mTaskTooBigNoti == null) {
	    mTaskTooBigNoti = Toast.makeText(getApplication(), R.string.task_tip_complicated, Toast.LENGTH_SHORT);
	}
	mTaskTooBigNoti.show();
    }
}