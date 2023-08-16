package com.example.lam_project.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.lam_project.AcousticNoisePainter;
import com.example.lam_project.LTESignalPainter;
import com.example.lam_project.UpdatedSquarePainter;
import com.example.lam_project.WiFiSignalPainter;
import com.example.lam_project.model.Square;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

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
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_SIGNAL_VALUE = "signalValue";
    public static final String COLUMN_COUNT = "count";

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
                    COLUMN_TIMESTAMP + " REAL NOT NULL, " +
                    COLUMN_SIGNAL_VALUE + " REAL NOT NULL, " +
                    COLUMN_COUNT + " REAL NOT NULL, " +
                    COLUMN_COLOR + " INTEGER NOT NULL)";


    public List<Square> getAllSquares() {
        List<Square> squares = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                double latitudeStart = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE_START));
                double longitudeStart = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE_START));
                double latitudeEnd = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE_END));
                double longitudeEnd = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE_END));
                int color = cursor.getInt(cursor.getColumnIndex(COLUMN_COLOR));
                int type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
                double squareSize = cursor.getDouble(cursor.getColumnIndex(COLUMN_SIZE));
                long timestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP));
                double signalValue = cursor.getDouble(cursor.getColumnIndex(COLUMN_SIGNAL_VALUE));
                int count = cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT));

                squares.add(new Square(id, latitudeStart, longitudeStart, latitudeEnd, longitudeEnd,
                        color, type, squareSize, timestamp, signalValue, count));
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

    public static void saveSquareToDatabase(Polygon square, int color, int mode, MapView map,
                                            double squareSizeMeters, double signalValue) {
        // Get the latitude and longitude of the square
        long id = 0;
        double latitudeStart = square.getPoints().get(0).getLatitude();
        double longitudeStart = square.getPoints().get(0).getLongitude();
        double latitudeEnd = square.getPoints().get(2).getLatitude();
        double longitudeEnd = square.getPoints().get(2).getLongitude();
        long timestamp = 0;
        int count = 1;
        // Create a new Square object
        Square squareObject = new Square(id, latitudeStart, longitudeStart, latitudeEnd,
                longitudeEnd, color, mode, squareSizeMeters, timestamp, signalValue, count);

        long currentTimestamp = System.currentTimeMillis() / 1000; // Convert to seconds
        squareObject.setTimestamp(currentTimestamp);
        Log.d("TIMESTAMP SQUARE", "Timestamp: "+squareObject.getTimestamp());
        // Get a reference to the database helper
        Context context = map.getContext(); // Make sure you have access to the context where the map is displayed
        DatabaseManager dbHelper = new DatabaseManager(context);

        // Insert the square into the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(dbHelper.COLUMN_LATITUDE_START, squareObject.getLatitudeStart());
        values.put(dbHelper.COLUMN_LONGITUDE_START, squareObject.getLongitudeStart());
        values.put(dbHelper.COLUMN_LATITUDE_END, squareObject.getLatitudeEnd());
        values.put(dbHelper.COLUMN_LONGITUDE_END, squareObject.getLongitudeEnd());
        values.put(dbHelper.COLUMN_COLOR, squareObject.getColor());
        values.put(dbHelper.COLUMN_TYPE, squareObject.getType());
        values.put(dbHelper.COLUMN_SIZE, squareObject.getSquareSize());
        values.put(dbHelper.COLUMN_TIMESTAMP, squareObject.getTimestamp());
        values.put(dbHelper.COLUMN_SIGNAL_VALUE, squareObject.getSignalValue());
        values.put(dbHelper.COLUMN_COUNT, squareObject.getCount());
        id = db.insert(dbHelper.TABLE_NAME, null, values);
        Log.d("ID INSERT", "ID: "+id);
        // Set the ID of the square object after insertion, maybe removing this later?
        squareObject.setId(id);
        db.close();
        dbHelper.close();
    }

    public static void updateSquare(Square square, double signalValue, MapView map) {
        Square updatedSquare = new Square(square.getId(), square.getLatitudeStart(), square.getLongitudeStart(),
                square.getLatitudeEnd(), square.getLongitudeEnd(), square.getColor(),
                square.getType(), square.getSquareSize(), square.getTimestamp(), square.getSignalValue(),
                square.getCount());
        updatedSquare.setId(square.getId());
        updatedSquare.setSignalValue((updatedSquare.getSignalValue()+signalValue) /
                updatedSquare.getCount());
        int fillColor = UpdatedSquarePainter.paintSquare(updatedSquare.getType(),
                updatedSquare.getSignalValue());
        updatedSquare.setColor(fillColor);
        Log.d("ELIMINA QUESTO", "ID: "+updatedSquare.getId());
        deleteSquare(updatedSquare.getId(), map);
        long currentTimestamp = System.currentTimeMillis() / 1000; // Convert to seconds
        updatedSquare.setTimestamp(currentTimestamp);
        Context context = map.getContext(); // Make sure you have access to the context where the map is displayed
        DatabaseManager dbHelper = new DatabaseManager(context);

        // Insert the square into the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, updatedSquare.getId());
        values.put(dbHelper.COLUMN_LATITUDE_START, updatedSquare.getLatitudeStart());
        values.put(COLUMN_LONGITUDE_START, updatedSquare.getLongitudeStart());
        values.put(COLUMN_LATITUDE_END, updatedSquare.getLatitudeEnd());
        values.put(COLUMN_LONGITUDE_END, updatedSquare.getLongitudeEnd());
        values.put(COLUMN_COLOR, updatedSquare.getColor());
        values.put(COLUMN_TYPE, updatedSquare.getType());
        values.put(COLUMN_SIZE, updatedSquare.getSquareSize());
        values.put(COLUMN_TIMESTAMP, updatedSquare.getTimestamp());
        values.put(COLUMN_SIGNAL_VALUE, updatedSquare.getSignalValue());
        values.put(COLUMN_COUNT, updatedSquare.getCount()+1);
        db.insert(TABLE_NAME, null, values);
        Log.d("AGGIUNGI", "CON ID: " + updatedSquare.getId());
        dbHelper.close();
        db.close();

    }

    public static void deleteSquare(long id, MapView map){
        Context context = map.getContext(); // Make sure you have access to the context where the map is displayed
        DatabaseManager dbHelper = new DatabaseManager(context);

        // Insert the square into the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME,  "_id = ?", new String[]{String.valueOf(id)});
        Log.d("DELETE", "DELETE WITH ID: "+id);
        dbHelper.close();
        db.close();
    }
}

