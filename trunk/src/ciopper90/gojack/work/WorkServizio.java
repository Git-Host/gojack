package ciopper90.gojack.work;

import java.util.ArrayList;

import ciopper90.gojack.MainActivity;
import ciopper90.gojack.utility.DatabaseService;
import ciopper90.gojack.utility.DatiServizio;
import ciopper90.gojack.utility.ParametriServizio;
import ciopper90.gojack.utility.Servizio;

import android.app.Activity;
import android.database.Cursor;

public class WorkServizio extends Activity{
	private DatabaseService db;
	public WorkServizio(){
		db=MainActivity.db;
	}
	
	public ArrayList<Servizio> caricaServizio(){
		db.open();
		ArrayList<Servizio> a = null;
		a=new ArrayList<Servizio>();
		Cursor c=db.fetchService(); // query
       startManagingCursor(c);
        while(c.moveToNext()){
        	Servizio s=new Servizio(c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getString(6),c.getString(7),c.getString(8),c.getString(0));	
        	a.add(s);
        }
        c.close();
		db.close();
		return a;
	}
	
	 public ArrayList<DatiServizio> caricaDataServizio(){
 		db.open();
 		ArrayList<DatiServizio> a = null;
 		a=new ArrayList<DatiServizio>();
 		Cursor c=db.fetchDataService(); // query
        startManagingCursor(c);
         while(c.moveToNext()){
         	DatiServizio d=new DatiServizio(c.getString(0),c.getInt(1),c.getInt(2),c.getInt(3),c.getInt(4),c.getInt(5),c.getInt(6),c.getString(7));	
         	a.add(d);
         }
        c.close();
        db.close();
 		return a;
 		
 	}
	 
	 public ArrayList<ParametriServizio> caricaParametriServizio(){
		 db.open();
			ArrayList<ParametriServizio> a = null;
			a=new ArrayList<ParametriServizio>();
			Cursor c=db.fetchParameterService(); // query
	       startManagingCursor(c);
	        while(c.moveToNext()){
	        	ParametriServizio s=new ParametriServizio(c.getString(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5));	
	        	a.add(s);
	        }
	        c.close();
	        db.close();
	        return a;
	 }
	 
	 public int caratteriServizio(String url){
	 		db.open();
	 		Cursor c=db.fetchDataService(url); // query
	        startManagingCursor(c);
	        int a=0;
	         while(c.moveToNext()){
	         	a=Integer.parseInt(c.getString(c.getColumnIndex("maxchar")));
	         	}
	        c.close();
	        db.close();
	 		return a;
	 		
	 	}
	 

}
