package pe.hikaru.stalker.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class UsuariosSQLiteHelper extends SQLiteOpenHelper {

    String sqlCreate = "CREATE TABLE drone_accion (idaccion INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, idacciontipo INTEGER, id_cliente INTEGER, idenvio INTEGER, horaweb TEXT(12), horaandroid TEXT(12), mensaje TEXT(10000), longitud INTEGER, desfase TEXT(7), enviado INTEGER NOT NULL);";

    public UsuariosSQLiteHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sqlCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int verant, int vernue) {
        db.execSQL("DROP TABLE IF EXISTS drone_accion");
        db.execSQL(sqlCreate);
    }

}
