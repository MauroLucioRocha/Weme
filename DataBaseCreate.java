package br.com.elede.mauro.weme;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Mauro on 28/06/2016.
 */
public class DataBaseCreate extends SQLiteOpenHelper {

    private static final String NOME_DB = "weme";
    private static final int VERSAO_DB = 1;


    public DataBaseCreate(Context context) {
        super(context, NOME_DB, null, VERSAO_DB);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table usuario(_id integer primary key autoincrement, nome_fb text not null, imagem_fb text not null);");
        db.execSQL("create table feed(_id integer primary key autoincrement, nome_fb text not null, imagem_feed text not null, curtidas integer);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table usuario;");
        db.execSQL("drop table feed;");
        onCreate(db);
    }
}
