package com.example.treesa.autocallandendcall;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import java.util.List;

public class PhoneListViewModel extends AndroidViewModel {

    private LiveData<List<Phones>> mAllPhone;
    PhoneRepository mPhoneRepository;

    public PhoneListViewModel(@NonNull Application application) {
        super(application);

        mPhoneRepository = new PhoneRepository(application);
        mAllPhone = mPhoneRepository.getAllPhones();
    }

    public LiveData<List<Phones>> getAllNotes() {
        return mAllPhone;
    }

    public void addNote(Phones phones) {
        mPhoneRepository.addPhone(phones);
    }

    public void deleteNote(Phones phones) {
        mPhoneRepository.deletePhone(phones);
    }

    public void updateNote(Phones phones) {
        mPhoneRepository.updatePhone(phones);
    }


}
