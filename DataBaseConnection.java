package br.com.elede.mauro.weme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mauro on 28/06/2016.
 */
public class DataBaseConnection {
    private SQLiteDatabase dbWeme;

    public DataBaseConnection (Context context){
        DataBaseCreate auxBd = new DataBaseCreate(context);
        dbWeme = auxBd.getWritableDatabase();
    }

    public void inserir (Usuario usuario){

        try{
            ContentValues valores = new ContentValues();
            valores.put("nome_fb", usuario.getNome_fb());
            valores.put("imagem_fb", usuario.getImagem_fb());

            dbWeme.insert("usuario", null, valores);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void inserirImagem (ImageFeed imageFeed){

        try{
            ContentValues valores = new ContentValues();
            valores.put("nome_fb", imageFeed.getNome_fb());
            valores.put("imagem_feed", imageFeed.getImagem_feed());
            valores.put("curtidas", imageFeed.getCurtidas());

            dbWeme.insert("feed", null, valores);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void atualizar (Usuario usuario){
        ContentValues valores = new ContentValues();
        valores.put("nome_fb", usuario.getNome_fb());
        valores.put("imagem_fb", usuario.getImagem_fb());

        dbWeme.update("usuario", valores, "_id = ?", new String[]{"" + usuario.getId()});
    }

    public void atualizarCurtidas (ImageFeed imageFeed ){
        ContentValues valores = new ContentValues();
        valores.put("curtidas", imageFeed.getCurtidas());

        dbWeme.update("feed", valores, "_id = ?", new String[]{"" + imageFeed.getId()});
    }


    public void deletar (Usuario usuario){
        dbWeme.delete("usuario", "_id = ?" + usuario.getId(), null);
    }

    public List<Usuario> Buscar (){
        List<Usuario> list = new ArrayList<Usuario>();
        String[] colunas = new String[] {"_id", "nome_fb", "imagem_fb"};
        Cursor cursor = dbWeme.query("usuario", colunas, null, null, null, null, "nome_fb ASC");

        if (cursor.getCount() > 0){
            cursor.moveToFirst();

            do{
                Usuario u = new Usuario();
                u.setId((int) cursor.getLong(0));
                u.setNome_fb(cursor.getString(1));
                u.setImagem_fb(cursor.getString(2));

                list.add(u);
            }while (cursor.moveToNext());
        }

        return list;
    }

    public List<ImageFeed> BuscarFeed (){
        List<ImageFeed> list = new ArrayList<ImageFeed>();
        String[] colunas = new String[] {"_id", "nome_fb", "imagem_feed"};
        Cursor cursor = dbWeme.query("feed", colunas, null, null, null, null, "_id ASC");

        if (cursor.getCount() > 0){
            cursor.moveToFirst();

            do{
                ImageFeed feed = new ImageFeed();
                feed.setId((int) cursor.getLong(0));
                feed.setNome_fb(cursor.getString(1));
                feed.setImagem_feed(cursor.getString(2));

                list.add(feed);
            }while (cursor.moveToNext());
        }

        return list;
    }
}
