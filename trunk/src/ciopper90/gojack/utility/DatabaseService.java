package ciopper90.gojack.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseService {

        SQLiteDatabase mDb;
        DbHelper mDbHelper;
        Context mContext;
        private static final String DB_NAME="Servizi";//nome del db
        private static final int DB_VERSION=5; //numero di versione del nostro db
       
        public DatabaseService(Context ctx){
                mContext=ctx;
                mDbHelper=new DbHelper(ctx, DB_NAME, null, DB_VERSION);   //quando istanziamo questa classe, istanziamo anche l'helper (vedi sotto)    
        }
       
        public void open(){  //il database su cui agiamo è leggibile/scrivibile
                mDb=mDbHelper.getWritableDatabase();
               
        }
       
        public void close(){ //chiudiamo il database su cui agiamo
                mDb.close();
        }
       
       
        //i seguenti 2 metodi servono per la lettura/scrittura del db. aggiungete e modificate a discrezione
       // consiglio:si potrebbe creare una classe Prodotto, i quali oggetti verrebbero passati come parametri dei seguenti metodi, rispettivamente ritornati. Lacio a voi il divertimento

       
        public void insertDataService(String name,int primo,int secondo,int terzo,int quarto,int maxchar,int maxsms,String url){ //metodo per inserire i dati
                ContentValues cv=new ContentValues();
                cv.put("NomeServizio", name);
                cv.put("primo", primo);
                cv.put("secondo", secondo);
                cv.put("terzo", terzo);
                cv.put("quarto", quarto);
                cv.put("maxchar", maxchar);
                cv.put("maxsms", maxsms);
                cv.put("url", url);
                mDb.insert(ProductsMetaData.DATA_SERVICE, null, cv);
        }
        
        public void insertService(String name,String primo,String secondo,String terzo,String quarto,String url,String firma){ //metodo per inserire i dati
            ContentValues cv=new ContentValues();
            cv.put("NomeServizio", name);
            cv.put("primo", primo);
            cv.put("secondo", secondo);
            cv.put("terzo", terzo);
            cv.put("quarto", quarto);
            cv.put("url", url);
            cv.put("firma", firma);
            mDb.insert(ProductsMetaData.SERVICE, null, cv);
    }
        
        public void insertParameterService(String name,String primo,String secondo,String terzo,String quarto,String url){ //metodo per inserire i dati
            ContentValues cv=new ContentValues();
            cv.put("NomeServizio", name);
            cv.put("primo", primo);
            cv.put("secondo", secondo);
            cv.put("terzo", terzo);
            cv.put("quarto", quarto);
            cv.put("url", url);
            mDb.insert(ProductsMetaData.PARAMETER_SERVICE, null, cv);
    }
        
        public void insertSms(String name,String numero,String testo){ //metodo per inserire i dati
            ContentValues cv=new ContentValues();
            cv.put("NomeContatto", name);
            cv.put("numero", numero);
            cv.put("testo", testo);
            mDb.insert(ProductsMetaData.SMS, null, cv);
    }
        
       
        public Cursor fetchDataService(){ //metodo per fare la query di tutti i dati
                return mDb.query(ProductsMetaData.DATA_SERVICE, null,null,null,null,null,null);              
        }
        
        public Cursor fetchDataService(String where){ //metodo per fare la query di tutti i dati
            return mDb.query(ProductsMetaData.DATA_SERVICE, null,"url = ?",new String[] {where},null,null,null);              
        }
        
        public Cursor fetchService(){ //metodo per fare la query di tutti i dati
            return mDb.query(ProductsMetaData.SERVICE, null,null,null,null,null,"NomeServizio");              
        }
        
        public Cursor fetchService(String nameservice){ //metodo per fare la query di tutti i dati
            return mDb.query(ProductsMetaData.SERVICE, null,"NomeServizio = '"+nameservice+"'",null,null,null,null);              
        }
        
        public Cursor fetchParameterService(){ //metodo per fare la query di tutti i dati
            return mDb.query(ProductsMetaData.PARAMETER_SERVICE, null,null,null,null,null,null);              
        }
        
        public Cursor fetchSms(){ //metodo per fare la query di tutti i dati
            return mDb.query(ProductsMetaData.SMS, null,null,null,null,null,"id desc");              
        }
        
        public void UpdateService(String id,String config, String string, String string2, String string3, String string4, String config2, String string5){ //metodo per fare la query di tutti i dati
        	//gestire update dei dati :DmDb.delete(SERVICE_TABLE_CREATE, whereClause, whereArgs)
        	this.DeleteService(id);
        	this.insertService(config, string, string2, string3, string4, config2, string5);
        	
        	
        }
        
        public void DeleteService(String id){
        	//gestire update dei dati :D
        	mDb.delete(ProductsMetaData.SERVICE, "id like '"+id+"'", null);
        	/*if(condizione!=0){
        		mDb.delete(ProductsMetaData.DATA_SERVICE, "url like '"+config+"'", null);
        		mDb.delete(ProductsMetaData.PARAMETER_SERVICE, "url like '"+config+"'", null);
        		
        	}*/
        	
        	
        	
        }
        public void DeleteSms(String id){
        	//gestire update dei dati :D
        	mDb.delete(ProductsMetaData.SMS, "id like '"+id+"'", null); 		
        }
        

        static class ProductsMetaData {  // i metadati della tabella, accessibili ovunque
                static final String SERVICE = "servizi";
                static final String DATA_SERVICE = "dati_servizi";
                static final String SMS = "sms";
                
                static final String PARAMETER_SERVICE = "parametri_servizi";
                static final String PRIMO_PARAMETRO = "primo";
                static final String SECONDO_PARAMETRO = "secondo";
                static final String TERZO_PARAMETRO = "terzo";
                static final String QUARTO_PARAMETRO = "quarto";
                static final String MAXCHAR_PARAMETRO = "maxchar";
                static final String MAXSMS_PARAMETRO = "maxsms";
        }

        private static final String DATA_SERVICE_TABLE_CREATE = "create table if not exists dati_servizi (NomeServizio varchar(20) not null primary key,primo integer not null,secondo integer not null,terzo integer not null,quarto integer not null,maxsms integer not null,maxchar integer not null,url varchar(50) not null)";
        private static final String SERVICE_TABLE_CREATE = "create table if not exists servizi (id Integer primary key,NomeServizio varchar(20) not null ,primo varchar(20) not null,secondo varchar(20) ,terzo varchar(20),quarto varchar(20),url varchar(50) not null,firma varchar(40))";
        private static final String PARAMETER_SERVICE_TABLE_CREATE = "create table if not exists parametri_servizi (NomeServizio varchar(20) not null primary key,primo varchar(20) not null,secondo varchar(20) ,terzo varchar(20),quarto varchar(20),url varchar(50) not null)";
        private static final String RUBRICA_TABLE_CREATE = "create table if not exists rubrica (NomeContatto varchar(20) not null primary key,numero varchar(20) not null,servizio varchar(20))";
        private static final String SMS_TABLE_CREATE = "create table if not exists sms (id Integer primary key,NomeContatto varchar(20),numero varchar(20) ,testo varchar(1000))";

        
        private class DbHelper extends SQLiteOpenHelper { //classe che ci aiuta nella creazione del db

                public DbHelper(Context context, String name, CursorFactory factory,int version) {
                        super(context, name, factory, version);
                }

                @Override
                public void onCreate(SQLiteDatabase _db) { //solo quando il db viene creato, creiamo la tabella
                        _db.execSQL(SERVICE_TABLE_CREATE);
                        _db.execSQL(DATA_SERVICE_TABLE_CREATE);
                        _db.execSQL(PARAMETER_SERVICE_TABLE_CREATE);
                        _db.execSQL(RUBRICA_TABLE_CREATE);
                		_db.execSQL(SMS_TABLE_CREATE);
                }

                @Override
                public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
                	_db.execSQL("drop table dati_servizi");  
                	_db.execSQL("drop table servizi"); 
                	_db.execSQL("drop table parametri_servizi");  
                	_db.execSQL(SERVICE_TABLE_CREATE);
                    _db.execSQL(DATA_SERVICE_TABLE_CREATE);
                    _db.execSQL(PARAMETER_SERVICE_TABLE_CREATE);
                    _db.execSQL(RUBRICA_TABLE_CREATE);
            		_db.execSQL(SMS_TABLE_CREATE);
                    
                }

               

}

}
