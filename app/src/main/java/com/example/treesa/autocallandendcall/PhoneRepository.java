package com.example.treesa.autocallandendcall;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import java.util.List;

public class PhoneRepository {
    //Live Data of List of all notes
    private LiveData<List<Phones>> mAllPhones;
    //Define Phone Dao
    PhoneDao phoneDao;

    public PhoneRepository(@NonNull Application application) {
        AppDatabase phoneDatabase = AppDatabase.getDatabase(application);
        //init Phones Dao
        phoneDao = phoneDatabase.phoneDao();
        //get all Phones
        mAllPhones = phoneDao.getAllPhones();
    }
    //method to get all Phones
    public LiveData<List<Phones>> getAllPhones() {
        return mAllPhones;
    }

    //method to add Phone
    public void addPhone(Phones phones) {
        new AddPnone().execute(phones);
    }

    //Async task to add Phones
    public class AddPnone extends AsyncTask<Phones, Void, Void> {
        @Override
        protected Void doInBackground(Phones... notes) {
            phoneDao.insertAll(notes[0]);
            return null;
        }
    }

    //method to delete phone
    public void deletePhone(Phones phones) {
        new DeletePhone().execute(phones);
    }

    //Async task to delete phone
    public class DeletePhone extends AsyncTask<Phones, Void, Void> {
        @Override
        protected Void doInBackground(Phones... notes) {
            phoneDao.delete(notes[0]);
            return null;
        }
    }

    //method to update phone
    public void updatePhone(Phones note) {
        new UpdatePhone().execute(note);
    }

    //Async task to update phone
    public class UpdatePhone extends AsyncTask<Phones, Void, Void> {
        @Override
        protected Void doInBackground(Phones... notes) {
            phoneDao.update(notes[0]);
            return null;
        }
    }
}