package ciopper90.gojack.work;

import java.util.ArrayList;

import ciopper90.gojack.MainActivity;
import ciopper90.gojack.utility.DatabaseService;
import ciopper90.gojack.utility.SMS;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;

public class WorkSms extends Activity{
	private DatabaseService db;
	public WorkSms(){
		db=MainActivity.db;
	}

	public ArrayList<SMS> CaricaSms(){
		db.open();
		Cursor c=db.fetchSms(); // query
		startManagingCursor(c);
		ArrayList<SMS> a = new ArrayList<SMS>();
		int i=0;
		while(c.moveToNext()&&i<20){
			i++;
			a.add(new SMS(Integer.parseInt(c.getString(0)),c.getString(1),c.getString(2),c.getString(3)));
		}
		c.close();
		db.close();
		return a;

	}


	public String ultimoSms(){
		db.open();
		Cursor c=db.fetchSms(); // query
		startManagingCursor(c);
		String a = null;
		boolean cond=c.moveToNext();
		if(cond==true){
			a=c.getString(c.getColumnIndex("testo"));
		}else{
			a="no mex";
		}
		c.close();
		db.close();
		return a;

	}
	public String ultimoNumero(){
		db.open();
		Cursor c=db.fetchSms(); // query
		startManagingCursor(c);
		String a = null;
		boolean cond=c.moveToNext();
		if(cond==true){
			a=c.getString(c.getColumnIndex("numero"));
			Log.d("number", a);
		}else{
			a="no num";
		}
		c.close();
		db.close();
		return a;

	}

}
