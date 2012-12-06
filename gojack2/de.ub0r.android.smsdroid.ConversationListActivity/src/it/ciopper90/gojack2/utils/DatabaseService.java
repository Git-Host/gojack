package it.ciopper90.gojack2.utils;

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
	private static final String DB_NAME = "Servizi";// nome del db
	private static final int DB_VERSION = 5; // numero di versione del nostro db

	public DatabaseService(final Context ctx) {
		this.mContext = ctx;
		this.mDbHelper = new DbHelper(ctx, DB_NAME, null, DB_VERSION); // quando
		// istanziamo
		// questa
		// classe,
		// istanziamo
		// anche
		// l'helper
		// (vedi
		// sotto)
	}

	public void open() { // il database su cui agiamo è leggibile/scrivibile
		this.mDb = this.mDbHelper.getWritableDatabase();

	}

	public void close() { // chiudiamo il database su cui agiamo
		this.mDb.close();
	}

	// i seguenti 2 metodi servono per la lettura/scrittura del db. aggiungete e
	// modificate a discrezione
	// consiglio:si potrebbe creare una classe Prodotto, i quali oggetti
	// verrebbero passati come parametri dei seguenti metodi, rispettivamente
	// ritornati. Lacio a voi il divertimento

	public void insertDataService(final String name, final int primo, final int secondo,
			final int terzo, final int quarto, final int maxchar, final int maxsms, final String url) { // metodo
		// per
		// inserire
		// i
		// dati
		ContentValues cv = new ContentValues();
		cv.put("NomeServizio", name);
		cv.put("primo", primo);
		cv.put("secondo", secondo);
		cv.put("terzo", terzo);
		cv.put("quarto", quarto);
		cv.put("maxchar", maxchar);
		cv.put("maxsms", maxsms);
		cv.put("url", url);
		this.mDb.insert(ProductsMetaData.DATA_SERVICE, null, cv);
	}

	public void insertService(final String name, final String primo, final String secondo,
			final String terzo, final String quarto, final String url, final String firma) { // metodo
		// per
		// inserire
		// i
		// dati
		ContentValues cv = new ContentValues();
		cv.put("NomeServizio", name);
		cv.put("primo", primo);
		cv.put("secondo", secondo);
		cv.put("terzo", terzo);
		cv.put("quarto", quarto);
		cv.put("url", url);
		cv.put("firma", firma);
		this.mDb.insert(ProductsMetaData.SERVICE, null, cv);
	}

	public void insertParameterService(final String name, final String primo, final String secondo,
			final String terzo, final String quarto, final String url) { // metodo
		// per
		// inserire
		// i
		// dati
		ContentValues cv = new ContentValues();
		cv.put("NomeServizio", name);
		cv.put("primo", primo);
		cv.put("secondo", secondo);
		cv.put("terzo", terzo);
		cv.put("quarto", quarto);
		cv.put("url", url);
		this.mDb.insert(ProductsMetaData.PARAMETER_SERVICE, null, cv);
	}

	public void insertSms(final String name, final String numero, final String testo) { // metodo
		// per
		// inserire
		// i
		// dati
		ContentValues cv = new ContentValues();
		cv.put("NomeContatto", name);
		cv.put("numero", numero);
		cv.put("testo", testo);
		this.mDb.insert(ProductsMetaData.SMS, null, cv);
	}

	public Cursor fetchDataService() { // metodo per fare la query di tutti i
		// dati
		return this.mDb.query(ProductsMetaData.DATA_SERVICE, null, null, null, null, null, null);
	}

	public Cursor fetchDataService(final String where) { // metodo per fare la
		// query di tutti i
		// dati
		return this.mDb.query(ProductsMetaData.DATA_SERVICE, null, "url = ?",
				new String[] { where }, null, null, null);
	}

	public Cursor fetchService() { // metodo per fare la query di tutti i dati
		return this.mDb.query(ProductsMetaData.SERVICE, null, null, null, null, null,
				"NomeServizio");
	}

	public Cursor fetchService(final String nameservice) { // metodo per fare la
		// query di tutti i
		// dati
		return this.mDb.query(ProductsMetaData.SERVICE, null, "NomeServizio = '" + nameservice
				+ "'", null, null, null, null);
	}

	public Cursor fetchParameterService() { // metodo per fare la query di tutti
		// i dati
		return this.mDb.query(ProductsMetaData.PARAMETER_SERVICE, null, null, null, null, null,
				null);
	}

	public Cursor fetchSms() { // metodo per fare la query di tutti i dati
		return this.mDb.query(ProductsMetaData.SMS, null, null, null, null, null, "id desc");
	}

	public void UpdateService(final String id, final String config, final String string,
			final String string2, final String string3, final String string4, final String config2,
			final String string5) { // metodo per fare la query di tutti i dati
		// gestire update dei dati :DmDb.delete(SERVICE_TABLE_CREATE,
		// whereClause, whereArgs)
		this.DeleteService(id);
		this.insertService(config, string, string2, string3, string4, config2, string5);

	}

	public void DeleteService(final String id) {
		// gestire update dei dati :D
		this.mDb.delete(ProductsMetaData.SERVICE, "id like '" + id + "'", null);
		/*
		 * if(condizione!=0){ mDb.delete(ProductsMetaData.DATA_SERVICE,
		 * "url like '"+config+"'", null);
		 * mDb.delete(ProductsMetaData.PARAMETER_SERVICE,
		 * "url like '"+config+"'", null);
		 * 
		 * }
		 */

	}

	public void DeleteSms(final String id) {
		// gestire update dei dati :D
		this.mDb.delete(ProductsMetaData.SMS, "id like '" + id + "'", null);
	}

	static class ProductsMetaData { // i metadati della tabella, accessibili
		// ovunque
		static final String SERVICE = "servizi";
		static final String DATA_SERVICE = "dati_servizi";
		static final String SMS = "sms";
		static final String SERVICENUM = "servicenum";

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
	private static final String SERVICENUM_TABLE_CREATE = "create table if not exists servicenum (numero varchar(20) primary key ,service varchar(50))";

	private class DbHelper extends SQLiteOpenHelper { // classe che ci aiuta
		// nella creazione del
		// db

		public DbHelper(final Context context, final String name, final CursorFactory factory,
				final int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(final SQLiteDatabase _db) { // solo quando il db
			// viene creato,
			// creiamo la
			// tabella
			_db.execSQL(SERVICE_TABLE_CREATE);
			_db.execSQL(DATA_SERVICE_TABLE_CREATE);
			_db.execSQL(PARAMETER_SERVICE_TABLE_CREATE);
			_db.execSQL(RUBRICA_TABLE_CREATE);
			_db.execSQL(SMS_TABLE_CREATE);
			_db.execSQL(SERVICENUM_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(final SQLiteDatabase _db, final int oldVersion, final int newVersion) {

		}

	}

	public void saveService(final String to, final String service) {
		ContentValues cv = new ContentValues();
		cv.put("numero", to);
		cv.put("service", service);
		this.mDb.insert(ProductsMetaData.SERVICENUM, null, cv);
	}

	public void updateSerNum(final String to, final String service) {
		this.deleteSerNum(to);
		this.saveService(to, service);
	}

	public void deleteSerNum(final String id) {
		// gestire update dei dati :D
		this.mDb.delete(ProductsMetaData.SERVICENUM, "numero like '" + id + "'", null);
	}

	public Cursor fetchServiceNum(final String where) { // metodo per fare la
		// query di tutti i
		// dati
		return this.mDb.query(ProductsMetaData.SERVICENUM, null, "numero like '" + where + "'",
				null, null, null, null);
	}

}
