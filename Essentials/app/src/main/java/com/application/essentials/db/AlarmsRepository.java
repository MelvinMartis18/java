package com.application.essentials.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

public class AlarmsRepository {

    private AlarmsDAO alarmsDao;

    public AlarmsRepository(Application application) {
        AlarmsDB alarmsDB = AlarmsDB.getDatabase(application);
        alarmsDao = alarmsDB.alarmsDAO();
    }

    public LiveData<List<AlarmEntity>> getAlarm() {
        Log.e("Repository","Repository called");
        return alarmsDao.getAlarm();
    }


    public void insertAlarm(AlarmEntity alarmEntity) {
        new InsertAsyncTask(alarmsDao).execute(alarmEntity);
    }


    private static class InsertAsyncTask extends AsyncTask<AlarmEntity, Void, Void> {

        private AlarmsDAO asyncAlarmsDAO;

        InsertAsyncTask(AlarmsDAO alarmsDAO) {
            asyncAlarmsDAO = alarmsDAO;
        }

        @Override
        protected Void doInBackground(final AlarmEntity... params) {
            asyncAlarmsDAO.insertAlarm(params[0]);
           // Log.e("test", ""+params[0]);
            return null;
        }
    }

    public void deleteAlarm(AlarmEntity alarmEntity) {
        new DeleteAsyncTask(alarmsDao).execute(alarmEntity);
    }

    private static class DeleteAsyncTask extends AsyncTask<AlarmEntity, Void, Void> {

        private AlarmsDAO asyncAlarmsDAO;

        DeleteAsyncTask(AlarmsDAO alarmsDAO) {
            asyncAlarmsDAO = alarmsDAO;
        }

        @Override
        protected Void doInBackground(final AlarmEntity... params) {
            asyncAlarmsDAO.deleteAlarm(params[0]);
            return null;
        }
    }
}