package com.example.lam_project.logic;

import android.os.Handler;

import com.example.lam_project.managers.DatabaseManager;
import com.example.lam_project.managers.NotificationsManager;
import com.example.lam_project.managers.SettingsManager;
import com.example.lam_project.model.Square;
import com.example.lam_project.utils.Utils;

import org.osmdroid.views.MapView;

import java.util.List;

//This is a runnable thread that constantly checks and deletes the squares that expires.
//It also refresh the squares (that currently exists) each second.
public class RefreshExpiryThread {

    private static int MILLISECONDS_EXPIRY_REFRESH = 10;
    public static void expiredAndRefreshedSquares(MapView map) {
        SettingsManager settingsManager = new SettingsManager(map.getContext());
        NotificationsManager notificationsManager = new NotificationsManager(map.getContext());

        Handler handler = new Handler();
        Runnable timeCheckerRunnable = new Runnable() {
            @Override
            public void run() {
                DatabaseManager databaseManager = new DatabaseManager(map.getContext());
                List<Square> squares = databaseManager.getAllSquares();
                for (Square squareExpired : squares ) {
                    //If time expires, delete it
                    if (Utils.hasTimeExpired(squareExpired.getTimestamp(), settingsManager.getSelectedMinutes())) {
                        long id = squareExpired.getId();
                        DatabaseManager.deleteSquare(id, map);
                        if(notificationsManager.areNotificationsEnabled() &&
                                notificationsManager.isExpiryNotificationsEnabled()){
                            NotificationsManager.showExpiredNotification(map.getContext());
                        }
                    }
                }
                databaseManager.close();
                //Refresh the whole thing
                map.getOverlays().clear();
                map.invalidate();

                DatabaseManager databaseManager2 = new DatabaseManager(map.getContext());
                List<Square> printNonExpiredSquares;
                //Print the squares left
                printNonExpiredSquares = databaseManager2.retrieveSquares(map);
                for (Square square : printNonExpiredSquares) {
                    SquareCreator.createExistingSquares(map, square);
                }
                // Define MILLISECONDS_EXPIRY_REFRESH in milliseconds
                handler.postDelayed(this, MILLISECONDS_EXPIRY_REFRESH); }
        };

        handler.post(timeCheckerRunnable);

    }
}
