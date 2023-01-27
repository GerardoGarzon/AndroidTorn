/**
 * Created by Gerardo Garzon on 04/01/23.
 */
package com.lebentech.lebentechtorniquetes.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.lebentech.lebentechtorniquetes.retrofit.reponses.SedeResponse

class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    /**
     * Create the new table for the sedes information, if the table already exists it will not be
     * modified
     */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + SEDES_TABLE + "(" +
                    "id TEXT PRIMARY KEY," +
                    "nombre TEXT," +
                    "dir_ip TEXT," +
                    "prioridad int )"
        )
    }

    /**
     * When the database version is changed it will call onUpgrade method and it will delete the
     * table to replace it with a new table
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE $SEDES_TABLE")
        onCreate(db)
    }

    /**
     * Return the sedes information where the priority is higher than teh actual priority, if there
     * is no more sedes with a higher priority it will return an empty array list
     */
    fun getSedes(priority: Int): ArrayList<SedeResponse> {
        val db = this.readableDatabase
        val cursorSedes = db.rawQuery(
            (" SELECT * " +
                    " FROM " + SEDES_TABLE +
                    " WHERE prioridad > " + priority +
                    " ORDER BY prioridad ASC "), null
        )
        val list = ArrayList<SedeResponse>()
        if (cursorSedes.moveToFirst()) {
            do {
                list.add(
                    SedeResponse(
                        cursorSedes.getString(1),
                        cursorSedes.getString(0),
                        cursorSedes.getString(2),
                        cursorSedes.getInt(3)
                    )
                )
            } while (cursorSedes.moveToNext())
        }
        cursorSedes.close()
        return list
    }

    /**
     * Insert the new sedes into the database
     */
    fun insertSedes(list: List<SedeResponse>) {
        val db = this.writableDatabase
        for (i in list.indices) {
            val contentValues = ContentValues()
            contentValues.put("id", list[i].idSede)
            contentValues.put("nombre", list[i].sedeName)
            contentValues.put("dir_ip", list[i].sedeIP)
            contentValues.put("prioridad", list[i].idPriority)
            db.insert(SEDES_TABLE, null, contentValues)
        }
    }

    fun deleteSedes() {
        val db = this.writableDatabase
        db.delete(SEDES_TABLE, null, null)
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "lebentech.db"
        private const val SEDES_TABLE = "t_sedes"
    }
}