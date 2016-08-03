package br.com.tairoroberto.tairoqrcodegeo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import br.com.tairoroberto.tairoqrcodegeo.domain.Registro;

/**
 * Created by tairo on 02/08/16.
 */
public class RegistroDAO {
    private static final String TAG = "Script";
    private SQLiteDatabase db;

    public RegistroDAO(Context context) {
        DBCore dbCore = new DBCore(context);
        db = dbCore.getWritableDatabase();
    }

    public void insert(Registro registro) {
        try {
            ContentValues values = new ContentValues();
            values.put("content", registro.getContent());
            values.put("type", registro.getType());
            values.put("latitude", registro.getLatitude());
            values.put("longitude", registro.getLongitude());

            db.insert("registros", null, values);
        } catch (Exception e) {
            Log.i(TAG, "insert: " + e.getMessage());
        }
    }


    public void update(Registro registro) {
        try {
            ContentValues values = new ContentValues();
            values.put("content", registro.getContent());
            values.put("type", registro.getType());
            values.put("latitude", registro.getLatitude());
            values.put("longitude", registro.getLongitude());

            db.update("registros", values, "_id = ?", new String[]{"" + registro.getId()});
        } catch (Exception e) {
            Log.i(TAG, "update: " + e.getMessage());
        }
    }


    public void delete(Registro registro) {
        try {
            db.delete("registros", "_id = ?", new String[]{"" + registro.getId()});
        } catch (Exception e) {
            Log.i(TAG, "delete: " + e.getMessage());
        }
    }


    public ArrayList<Registro> getAll() {
        ArrayList<Registro> list = new ArrayList<Registro>();
        String[] columns = {"_id", "content", "type", "latitude", "longitude"};
        Cursor cursor = db.query("registros", columns, null, null, null, null, "_id");

        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                do {
                    Registro registro = new Registro();

                    registro.setId(cursor.getLong(0));
                    registro.setContent(cursor.getString(1));
                    registro.setType(cursor.getString(2));
                    registro.setLatitude(cursor.getString(3));
                    registro.setLongitude(cursor.getString(4));


                    list.add(registro);
                } while (cursor.moveToNext());
            }
            return (list);

        } catch (Exception e) {
            Log.i(TAG, "getAll: " + e.getMessage());
            return (list);

        } finally {
            cursor.close();
        }
    }

    public Registro getById(long id) {
        Registro registro = new Registro();

        String[] columns = {"_id", "content", "type", "latitude", "longitude"};
        String where = "_id = ?";

        Cursor cursor = db.query("registros", columns, where, new String[]{"" + id}, null, null, null);
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                registro.setId(cursor.getLong(0));
                registro.setContent(cursor.getString(1));
                registro.setType(cursor.getString(2));
                registro.setLatitude(cursor.getString(3));
                registro.setLongitude(cursor.getString(4));

                return registro;
            } else {
                return registro;
            }
        } catch (Exception e) {
            Log.i(TAG, "getById: " + e.getMessage());
            return (registro);

        } finally {
            cursor.close();
        }
    }
}
