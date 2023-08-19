package com.example.lam_project.managers;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.example.lam_project.AcousticNoisePainter;
import com.example.lam_project.LTESignalPainter;
import com.example.lam_project.UpdatedSquarePainter;
import com.example.lam_project.WiFiSignalPainter;
import com.example.lam_project.model.Square;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    public static final String datasetDescription = "This data is retrieved from the app. Id is not relevant. longitude and latitude values represent the four angles that a square has. size represent the length of the square's lines expressed in METERS. Signal value is the aggregate of the signals values and the number of single values is counted in count. Type is 1 for LTE signal strength 2 for WiFi signal and 3 for Acoustic noise.";

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

        long currentTimestamp = System.currentTimeMillis(); // Convert to seconds
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
        SettingsManager settingsManager = new SettingsManager(map.getContext());
        if(updatedSquare.getCount() < settingsManager.getSelectedMeasurements()){
                    updatedSquare.setId(square.getId());
        updatedSquare.setSignalValue((updatedSquare.getSignalValue()+signalValue));
            int fillColor = UpdatedSquarePainter.paintSquare(updatedSquare.getType(),
                    updatedSquare.getSignalValue()/updatedSquare.getCount());
        updatedSquare.setColor(fillColor);
        Log.d("ELIMINA QUESTO","ID: "+updatedSquare.getId());

            deleteSquare(updatedSquare.getId(),map);
            long currentTimestamp = System.currentTimeMillis(); // Convert to seconds
        updatedSquare.setTimestamp(currentTimestamp);
            Context context = map.getContext();
            DatabaseManager dbHelper = new DatabaseManager(context);

            // Insert the square into the database
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
        values.put(COLUMN_ID,updatedSquare.getId());
        values.put(COLUMN_LATITUDE_START,updatedSquare.getLatitudeStart());
        values.put(COLUMN_LONGITUDE_START,updatedSquare.getLongitudeStart());
        values.put(COLUMN_LATITUDE_END,updatedSquare.getLatitudeEnd());
        values.put(COLUMN_LONGITUDE_END,updatedSquare.getLongitudeEnd());
        values.put(COLUMN_COLOR,updatedSquare.getColor());
        values.put(COLUMN_TYPE,updatedSquare.getType());
        values.put(COLUMN_SIZE,updatedSquare.getSquareSize());
        values.put(COLUMN_TIMESTAMP,updatedSquare.getTimestamp());
        values.put(COLUMN_SIGNAL_VALUE,updatedSquare.getSignalValue());
        values.put(COLUMN_COUNT,updatedSquare.getCount()+1);
        db.insert(TABLE_NAME,null,values);
        Log.d("AGGIUNGI","CON ID: "+updatedSquare.getId());
        dbHelper.close();
        db.close();
        }
    }

    public static void deleteSquare(long id, MapView map){
        Context context = map.getContext();
        DatabaseManager dbHelper = new DatabaseManager(context);

        // Insert the square into the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME,  "_id = ?", new String[]{String.valueOf(id)});
        Log.d("DELETE", "DELETE WITH ID: "+id);
        dbHelper.close();
        db.close();
    }

    public void eraseAllData() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }


    public void showEraseConfirmationDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Erase")
                .setMessage("Are you sure you want to erase all data?")
                .setPositiveButton("Delete anyway", (dialog, which) -> eraseAllData())
                .setNegativeButton("Cancel", null)
                .show();
    }
    public void showDumpDatabaseDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm dump")
                .setMessage("Are you sure you want to dump the .csv data?")
                .setPositiveButton("Dump it", (dialog, which) -> dumpDatabase(context, TABLE_NAME ))
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void dumpDatabase(Context context, String tableName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);

        try {
            File exportDir = new File(Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DOWNLOADS), "MyDatabaseExports");
            Log.d("DIRECTORY PATH", "DIR: "+ exportDir);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, tableName + ".csv");
            file.createNewFile();
            FileWriter fw = new FileWriter(file);
            fw.append("# Dataset: " + datasetDescription + "\n");
            int columnCount = cursor.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                if (i != 0) {
                    fw.append(",");
                }
                fw.append(cursor.getColumnName(i));
            }
            fw.append("\n");

            while (cursor.moveToNext()) {
                for (int i = 0; i < columnCount; i++) {
                    if (i != 0) {
                        fw.append(",");
                    }
                    fw.append(cursor.getString(i));
                }
                fw.append("\n");
            }

            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            db.close();
        }
    }
}

