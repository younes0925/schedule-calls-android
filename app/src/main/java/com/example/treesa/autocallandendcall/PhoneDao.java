package com.example.treesa.autocallandendcall;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;
import android.provider.ContactsContract;

import java.util.List;

@Dao
public interface PhoneDao {

    @Query("SELECT * FROM user")
    LiveData<List<Phones>> getAllPhones();


    @Query("SELECT COUNT(*) from user")
    int countUsers();

    @Insert
    void insertAll(Phones... phones);

    @Delete
    void delete(Phones phones);

    @Update
    void update(Phones phones);


}
