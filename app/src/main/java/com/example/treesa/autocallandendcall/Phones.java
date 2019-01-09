package com.example.treesa.autocallandendcall;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


@Entity(tableName = "user")
public class Phones {
    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "number")
    private String  number;

    @ColumnInfo(name = "withsms")
    private String withsms;


    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNumber() {
        return this.number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    public void setWithsms(String withsms) {this.withsms = withsms;}
    public String isWithsms() {return withsms;}

    @Override
    public String toString() {
        return number;
    }
    public String getWithsms() {
        return this.withsms;
    }
}
