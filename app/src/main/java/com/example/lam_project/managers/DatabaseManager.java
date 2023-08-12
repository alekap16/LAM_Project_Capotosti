package com.example.lam_project.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.lam_project.model.Square;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LAM.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "squares";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE_START = "latitudeStart";
    public static final String COLUMN_LONGITUDE_START = "longitudeStart";
    public static final String COLUMN_LATITUDE_END = "latitudeEnd";
    public static final String COLUMN_LONGITUDE_END = "longitudeEnd";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_SIZE = "size";

    SQLiteDatabase db = getReadableDatabase();
    private static final String CREATE_SQUARE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LATITUDE_START + " REAL NOT NULL, " +
                    COLUMN_LONGITUDE_START + " REAL NOT NULL, " +
                    COLUMN_LATITUDE_END + " REAL NOT NULL, " +
                    COLUMN_LONGITUDE_END + " REAL NOT NULL, " +
                    COLUMN_TYPE + " INTEGER NOT NULL, " +
                    COLUMN_SIZE + " REAL NOT NULL, " +
                    COLUMN_COLOR + " INTEGER NOT NULL)";


    public List<Square> getAllSquares() {
        List<Square> squares = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                double latitudeStart = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE_START));
                double longitudeStart = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE_START));
                double latitudeEnd = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE_END));
                double longitudeEnd = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE_END));
                int color = cursor.getInt(cursor.getColumnIndex(COLUMN_COLOR));
                int type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
                double squareSize = cursor.getDouble(cursor.getColumnIndex(COLUMN_SIZE));

                squares.add(new Square(latitudeStart, longitudeStart, latitudeEnd, longitudeEnd,
                        color, type, squareSize));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return squares;
    }
    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SQUARE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}

