package com.hilton.todo;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class TaskStore {
    public static final Uri CONTENT_URI = Uri.parse("content://" + TaskProvider.AUTHORITY + "/" + TaskProvider.TABLE_NAME);
    public static final String PROJECTION[] = new String[] {
        TaskColumns._ID,
        TaskColumns.DONE,
        TaskColumns.TASK,
        TaskColumns.TYPE,
        TaskColumns.CREATED,
        TaskColumns.DAY,
        TaskColumns.DELETED,
        TaskColumns.MODIFIED,
        TaskColumns.GOOGLE_TASK_ID,
    };
    
    public static final class ProjectionIndex {
        public static final int ID = 0;
        public static final int DONE = 1;
        public static final int TASK = 2;
        public static final int TYPE = 3;
        public static final int CREATED = 4;
        public static final int DAY = 5;
        public static final int DELETED = 6;
        public static final int MODIFIED = 7;
        public static final int GOOGLE_TASK_ID = 8;
    }
    
    public static class TaskColumns {
        public static final String _ID = "_id";
        public static final String DONE = "done";
        public static final String TASK = "task";
        /** History task, today's task or tomorrow's task */
        public static final String TYPE = "type";
        public static final String CREATED = "created";
        /** the day of the year on which task is created */
        // TODO: there is a bug, if we are at end of a year, day-of-year is not reliable any more
        public static final String DAY = "day";
        public static final String MODIFIED = "modified";
        public static final String GOOGLE_TASK_ID = "google_task_id";
        public static final String DELETED = "deleted";
        
        /** Expected number of Pomodoros for this task */
        public static final String EXPECTED = "expected";
        /** Number of Pomodoro actually spent on this task */
        public static final String SPENT = "spent";
        /** Number of interrupts while executing this task */
        public static final String INTERRUPTS = "interrupts";
    }
    
    public static final int TYPE_TODAY = 1;
    public static final int TYPE_TOMORROW = 2;
    public static final int TYPE_HISTORY = 3;
    
    public static final String[] POMODORO_PROJECTION = new String[] {
	TaskColumns.EXPECTED,
	TaskColumns.SPENT,
	TaskColumns.INTERRUPTS,
    };
    
    public static final class PomodoroIndex {
        public static final int EXPECTED = 0;
        public static final int SPENT = 1;
        public static final int INTERRUPTS = 2;
    }
    
    public static Cursor getTomorrowTasks(ContentResolver cr) {
	final Cursor cursor = cr.query(TaskStore.CONTENT_URI, TaskStore.PROJECTION, 
        	TaskColumns.TYPE + " = " + TaskStore.TYPE_TOMORROW + " AND " + TaskColumns.DELETED + " = 0", null, null);
	return cursor;
    }
    

    public static Cursor getTodayTasks(ContentResolver cr) {
	final Cursor cursor = cr.query(TaskStore.CONTENT_URI, TaskStore.PROJECTION, 
        	TaskColumns.TYPE + " = " + TaskStore.TYPE_TODAY + " AND " + TaskColumns.DELETED + " = 0", null, null);
	return cursor;
    }


    public static Cursor getTaskDetails(Uri uri, ContentResolver cr) {
	final Cursor cursor = cr.query(uri, POMODORO_PROJECTION, null, null, null);
	cursor.moveToFirst();
	return cursor;
    }
}