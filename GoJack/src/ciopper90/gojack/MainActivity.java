package ciopper90.gojack;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import ciopper90.gojack.utility.DatabaseRubrica;
import ciopper90.gojack.utility.DatabaseService;
import ciopper90.gojack.utility.Servizio;
import ciopper90.gojack.work.WorkServizio;
import ciopper90.gojack.work.WorkSms;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;

@SuppressLint({ "HandlerLeak", "SimpleDateFormat" })
public class MainActivity extends Activity implements TextWatcher, OnItemSelectedListener, OnMenuItemClickListener{

	final static int RQS_PICK_CONTACT = 0;

	@SuppressWarnings("unused")
	private String contactName, contactNumber, contactId;
	private EditText testo;
	private AutoCompleteTextView number;
	private static ProgressDialog pd;
	private static String messag;
	private static String title;
	private static String alert;
	private String prova;
	private static String array[];
	private static Cursor cursor;
	private static String id;
	private static String numero;
	private static String[] num;
	private static String name;
	private static String filename="account.dat";
	private static ArrayList<Servizio> o2;
	private Spinner spinner;
	private Servizio s;
	private Context context;
	private WorkServizio ws;
	private WorkSms wsms;
	public static DatabaseService db;
	private static String captcha;
	private EditText text;
	private Invio invio;
	private int numchar;
	private TextView tw;
	private SharedPreferences prefs;
	private String password;





	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void onResume() {
		super.onResume();
		ArrayList<Servizio> o2temp=ws.caricaServizio();
		if(o2temp==null){
			Toast.makeText(getApplicationContext(), "Nessun Servizio Configurato", Toast.LENGTH_LONG).show();
		}else{
			if(!uguali(o2temp)){
				o2=o2temp;
				spinner = (Spinner) findViewById(R.id.listserv);
				ArrayList<String> lista=new ArrayList<String>();
				for(int i=0;i<o2.size();i++){
					lista.add(o2.get(i).getName());
				}
				ArrayAdapter spinneradapter = new ArrayAdapter  (this,android.R.layout.simple_spinner_item, lista);
				spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(spinneradapter);
				numchar=ws.caratteriServizio(o2.get(spinner.getSelectedItemPosition()).getUrl());
			}
		}
		if(testo!=null){
			tw.setText(testo.getText().length()+"/"+numchar);
		}
	}
	private boolean uguali(ArrayList<Servizio> o2temp) {
		int uguali=1;
		if(o2.size()==o2temp.size()){
			for(int i=0;i<o2.size()&&uguali==1;i++){
				if(!(o2.get(i).getName().equals(o2temp.get(i).getName())))
					uguali=0;
			}
		}else
			uguali=0;
		if(uguali==1)
			return true;
		else
			return false;
	}
	/** Called when the activity is first created. */
	@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);			

		numchar=0;

		String [] c=DatabaseRubrica.getFieldbyArr(getApplicationContext());
		number = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.rubrica,c);
		//Setto l'adapter
		number.setAdapter(adapter);	
		context=this.getApplicationContext();
		db=new DatabaseService(context);
		if(invio==null)
			invio=new Invio();
		wsms=new WorkSms();

		testo=(EditText)findViewById(R.id.testo);


		if (null == savedInstanceState) {
			processIntentData(getIntent());
		}


		if(alert==null)
			alert=new String("");
		if(messag==null)
			messag=new String("");
		if(title==null)
			title=new String("");
		if(name==null)
			name=new String("");
		pd=new ProgressDialog(MainActivity.this);
		if(!messag.equals("")){
			pd.setMessage(messag);
			pd.show();
		}
		if(alert.equals("")&&!alert.equals(captcha)){
			/*AlertDialog.Builder builder=new AlertDialog.Builder(this);
			builder.setTitle(title);
			builder.setMessage(alert);
			builder.setCancelable(false);
			builder.setPositiveButton("Chiudi",new OnClickListener(){
				public void onClick(DialogInterface dialog, int id){
					dialog.dismiss();
					alert="";
				}
			});
			builder.show();*/
		}else{
			AlertDialog alertDialog;
			AlertDialog.Builder builder;
			LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.dialog,(ViewGroup) findViewById(R.id.layout_root));
			ImageView image = (ImageView) layout.findViewById(R.id.image);
			CheckBox b=(CheckBox) layout.findViewById(R.id.checkBox1);
			b.setVisibility(View.GONE);
			Bitmap bMap = null;
			try {
				bMap = BitmapFactory.decodeStream(this.openFileInput("captcha.jpg"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			text=(EditText) layout.findViewById(R.id.text);
			bMap= Bitmap.createScaledBitmap(bMap, 300, 80, true);
			image.setImageBitmap(bMap);
			builder = new AlertDialog.Builder(MainActivity.this);
			builder.setView(layout);
			builder.setCancelable(false);
			builder.setPositiveButton("Invia!",new OnClickListener(){
				public void onClick(DialogInterface dialog, int id){
					captcha=text.getText().toString();
					InviaCaptcha();
					dialog.dismiss();
					alert="";
				}
			});
			builder.setNegativeButton("Annulla!",new OnClickListener(){
				public void onClick(DialogInterface dialog, int id){
					dialog.dismiss();
					//annullare realmente l'invio
					alert="";
				}
			});
			alertDialog = builder.create();
			builder.show();				
		}
		if(!name.equals("")){
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			builder1.setTitle(name+":");
			builder1.setItems(num, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item){
					concludi(1,item); 
				}
			});
			AlertDialog alert = builder1.create();
			alert.show();
		}
		ImageButton rubrica = (ImageButton)findViewById(R.id.rubrica);
		rubrica.setOnClickListener(new ImageButton.OnClickListener(){

			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, RQS_PICK_CONTACT);
			}});


		Button invia = (Button)findViewById(R.id.invia);
		invia.setOnClickListener(new Button.OnClickListener(){

			public void onClick(View arg0) {
				if(Controlla())
				{
					for(int i=0;i<o2.size();i++)
					{
						if(o2.get(i).getName()==spinner.getSelectedItem().toString()){
							s=o2.get(i);
						}
					}
					Invia();  		
				}}});

		ws=new WorkServizio();
		o2=ws.caricaServizio();
		if(o2.size()==0){
			Intent asd=new Intent(MainActivity.this,ViewServizio.class);
			startActivity(asd);
		}
		if(o2.size()>0){
			spinner = (Spinner) findViewById(R.id.listserv);
			ArrayList<String> lista=new ArrayList<String>();
			for(int i=0;i<o2.size();i++){
				lista.add(o2.get(i).getName());
			}
			ArrayAdapter spinneradapter = new ArrayAdapter  (this,android.R.layout.simple_spinner_item, lista);
			spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(spinneradapter);
			numchar=ws.caratteriServizio(o2.get(spinner.getSelectedItemPosition()).getUrl());
			testo.addTextChangedListener(this);
			spinner.setOnItemSelectedListener(this);
		}
		tw=(TextView)this.findViewById(R.id.textView4);
		tw.setText("0/"+numchar);
		registerForContextMenu(testo);
		registerForContextMenu(number);

	}

	protected boolean Controlla() {
		if(number.getText().length()==0){
			Toast.makeText(getApplicationContext(), "Errore: Manca il destinataio", Toast.LENGTH_LONG).show();
			return false;}
		if(testo.getText().length()==0){
			Toast.makeText(getApplicationContext(), "Errore: Manca il testo", Toast.LENGTH_LONG).show();
			return false;}
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == RQS_PICK_CONTACT){
			if(resultCode == RESULT_OK){
				Uri contactData = data.getData();
				cursor =  managedQuery(contactData, null, null, null, null);
				cursor.moveToFirst();
				ContentResolver cr = getContentResolver();
				id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
				numero = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));
				if (Integer.parseInt(cursor.getString(
						cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = cr.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "= ?",
							new String[]{id}, null);
					int NumNumeri=pCur.getCount();
					if(NumNumeri>1)
					{
						num=new String[NumNumeri];
						int i=0;
						while (i<NumNumeri) {
							// Do something with phones
							pCur.moveToNext();
							num[i] = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							i++;
						}
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
						builder.setTitle(name+":");
						builder.setItems(num, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item){
								concludi(1,item); 
							}
						});
						AlertDialog alert = builder.create();
						alert.show();

					}else{
						while (pCur.moveToNext()) {
							// Do something with phones
							numero = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						}
						name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
						concludi(0,0);
					}
					pCur.close();
				}


			}
		}
	}

	private void concludi(int i,int n) {
		if(i==0){
			contactNumber=numero;
		}else{
			contactNumber=num[n];
		}
		//name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
		contactId=id;
		contactName=name;
		name="";
		contactNumber=contactNumber.replaceAll("-", "");
		if(contactNumber.charAt(0)!='+')
			contactNumber="+39"+contactNumber;
		number.setText(contactName+" <"+contactNumber+">"); 


	}

	Handler effettuato = new Handler(){
		@Override
		public void handleMessage(Message msg){
			pd.cancel();
			invioeffettuato();
		}
	};

	private void Invia(){
		pd=new ProgressDialog(this);
		pd.setMessage("Invio in corso");
		pd.setCancelable(false);
		messag="Invio in corso";
		pd.show();
		new Thread() {
			public void run() {
				try{
					//	invio=new Invio();
					prova=invio.InvioSms(s,number, testo,context);
					if(!prova.contains("<res>")&&!prova.contains("<html>")&&!prova.contains("errore")){
						FileOutputStream out = openFileOutput("captcha.jpg", Context.MODE_PRIVATE);
						for(int i=0;i<prova.length();i++)
							out.write(prova.charAt(i));
						out.flush();
						out.close();
					}



				}catch(Exception e){
					e.printStackTrace();
				}
				Log.d("!", prova);

				effettuato.sendEmptyMessage(0);
			}
		}.start();
	}

	private void InviaCaptcha(){
		pd=new ProgressDialog(this);
		pd.setMessage("Invio Captcha in corso");
		pd.setCancelable(false);
		messag="Invio in corso";
		pd.show();
		new Thread() {
			public void run() {
				try{
					prova=invio.InvioCaptchaSms(s,captcha,number,testo);
					if(!prova.contains("<res>")&&!prova.contains("<html>")&&!prova.contains("errore")){
						FileOutputStream out = openFileOutput("captcha.jpg", Context.MODE_PRIVATE);
						for(int i=0;i<prova.length();i++)
							out.write(prova.charAt(i));
						out.flush();
						out.close();
					}

				}catch(Exception e){
					e.printStackTrace();
				}
				effettuato.sendEmptyMessage(0);
			}
		}.start();
	}


	@SuppressWarnings("unused")
	private void invioeffettuato() {
		pd.dismiss();
		messag="";
		Log.d("!", prova);

		if(!prova.contains("<res>")&&!prova.contains("<html>")&&!prova.contains("errore")){
			pd.dismiss();
			alert="captcha";
			AlertDialog alertDialog;
			AlertDialog.Builder builder;
			LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.dialog,(ViewGroup) findViewById(R.id.layout_root));
			ImageView image = (ImageView) layout.findViewById(R.id.image);
			CheckBox b=(CheckBox) layout.findViewById(R.id.checkBox1);
			b.setVisibility(View.GONE);
			Bitmap bMap = null;
			try {
				bMap = BitmapFactory.decodeStream(this.openFileInput("captcha.jpg"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			text=(EditText) layout.findViewById(R.id.text);
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics( metrics);
			Log.d("misure","image "+bMap.getHeight()+":"+bMap.getWidth()+" spazio "+image.getHeight()+" "+image.getWidth());
			double rapporto=bMap.getWidth()/(double)bMap.getHeight();
			bMap= Bitmap.createScaledBitmap(bMap,(int)(metrics.widthPixels*0.9), (int)(((int)(metrics.widthPixels*0.9))/ rapporto), true);
			image.setImageBitmap(bMap);
			builder = new AlertDialog.Builder(MainActivity.this);
			builder.setView(layout);
			builder.setCancelable(false);
			builder.setPositiveButton("Invia!",new OnClickListener(){
				public void onClick(DialogInterface dialog, int id){
					captcha=text.getText().toString();
					InviaCaptcha();
					dialog.dismiss();
					alert="";
				}
			});
			builder.setNegativeButton("Annulla!",new OnClickListener(){
				public void onClick(DialogInterface dialog, int id){
					dialog.dismiss();
					//annullare realmente l'invio
					alert="";
				}
			});
			alertDialog = builder.create();
			builder.show();
		}
		else{
			if(prova.contains("<res>")||prova.contains("500")){
				if(prova.equals("")){
					AlertDialog.Builder builder=new AlertDialog.Builder(this);
					builder.setTitle("Errore");
					title="errore";
					alert=prova;
					//prova=prova.substring(prova.indexOf("<txt>")+5, prova.indexOf("</txt>"));
					builder.setMessage("errore generico");
					builder.setCancelable(false);
					builder.setPositiveButton("Chiudi",new OnClickListener(){
						public void onClick(DialogInterface dialog, int id){
							dialog.dismiss();
							alert="";
						}
					});
					builder.show();}
				else{
					if(prova.contains("<num>")){

						array=prova.split("<num>");
						if((array.length>1)&&(array[1].charAt(0)=='0')){
							/*AlertDialog.Builder builder=new AlertDialog.Builder(this);
							builder.setTitle("Inviato");
							builder.setMessage("Sms Inviato:"+prova.substring(prova.indexOf("<txt>")+5, prova.indexOf("</txt>")));// a "+array[2]+"!\nSms residui:"+array[1]);
							//title="Inviato";
							//alert="Sms Inviato a "+array[2]+"!\nSms residui:"+array[1];
							builder.setCancelable(true);
							builder.show();*/
							Toast.makeText(getApplicationContext(), "Sms Inviato:"+prova.substring(prova.indexOf("<txt>")+5, prova.indexOf("</txt>")), Toast.LENGTH_LONG).show();
							addsms();
							svuota();
						}
						else{
							AlertDialog.Builder builder=new AlertDialog.Builder(this);
							builder.setTitle("Errore");
							builder.setMessage(prova.substring(prova.indexOf("<txt>")+5, prova.indexOf("</txt>")));// a "+array[2]+"!\nSms residui:"+array[1]);
							//title="Inviato";
							//alert="Sms Inviato a "+array[2]+"!\nSms residui:"+array[1];
							builder.setCancelable(true);
							builder.show();
						}
					}else{
						if(prova.contains("500")){
							AlertDialog.Builder builder=new AlertDialog.Builder(this);
							title="Errore";
							alert="Timeout Server";
							builder.setTitle("Errore");
							builder.setMessage("Timeout Server");
							builder.setCancelable(false);
							builder.setPositiveButton("Chiudi",new OnClickListener(){
								public void onClick(DialogInterface dialog, int id){
									dialog.dismiss();
									alert="";
								}
							});
							builder.show();	
						}
						if(prova.contains("<res></res>")){
							AlertDialog.Builder builder=new AlertDialog.Builder(this);
							title="Errore";
							alert="Nessuna risposta dal php";
							builder.setTitle("Errore");
							builder.setMessage("Nessuna risposta dal php");
							builder.setCancelable(false);
							builder.setPositiveButton("Chiudi",new OnClickListener(){
								public void onClick(DialogInterface dialog, int id){
									dialog.dismiss();
									alert="";
								}
							});
							builder.show();	

						}
					}
				}
			}
			if(prova.contains("errore")){
				AlertDialog.Builder builder=new AlertDialog.Builder(this);
				title="Errore";
				alert="Errore Generico";
				builder.setTitle("Errore");
				builder.setMessage("Errore Generico");
				builder.setCancelable(false);
				builder.setPositiveButton("Chiudi",new OnClickListener(){
					public void onClick(DialogInterface dialog, int id){
						dialog.dismiss();
						alert="";
					}
				});
				builder.show();	

			}

		}

	}

	private void addsms() {
		prefs = getSharedPreferences("SmsInviati", Context.MODE_PRIVATE);
		int sms = Integer.parseInt(prefs.getString("SmsSend","0"));
		sms++;
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("SmsSend", Integer.toString(sms));
		editor.commit();	

		addsmsphone();
		addsmsprog();
	}

	private void addsmsphone() {
		ContentValues values = new ContentValues();
		String to=number.getText().toString();
		int inizio=to.indexOf("<");
		if(to.charAt(to.length()-1)=='>'){
			to=to.substring(inizio+1, to.length()-1);
		}else{
			to=to.substring(inizio+1, to.length());
		}
		values.put("address",to);
		values.put("body", testo.getText().toString());
		values.put("type", 2);
		values.put("read", 1);
		values.put("seen", 1);

		getContentResolver().insert(Uri.parse("content://sms"), values);
	}


	@SuppressWarnings("unused")
	private void addsmsprog() {
		String to=number.getText().toString();

		int inizio=to.indexOf("<");
		if(to.charAt(to.length()-1)=='>'){
			to=to.substring(inizio+1, to.length()-1);
		}else{
			to=to.substring(inizio+1, to.length());
		}
		to=eliminapref(to);
		ContentResolver localContentResolver = context.getContentResolver();
		Cursor contactLookupCursor =  
				localContentResolver.query(
						Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, 
								Uri.encode(to)), 
								new String[] {PhoneLookup.DISPLAY_NAME, PhoneLookup._ID}, 
								null, 
								null, 
								null);
		String contactName=null;
		try {
			while(contactLookupCursor.moveToNext()){
				contactName= contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
				String contactId = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(PhoneLookup._ID));
				//Log.d("ciao", "contactMatch name: " + contactName);
				//Log.d("ciao", "contactMatch id: " + contactId);
			}
		}catch(Exception e){}

		contactLookupCursor.close();
		if(contactName==null){
			contactName="";
		}

		SharedPreferences pref=this.getSharedPreferences("Setting", Context.MODE_PRIVATE);
		int salva=pref.getInt("salvasmsinviati", 0);
		if(salva==1){
			db.open();
			db.insertSms(contactName, to, testo.getText().toString());
			db.close();
		}

	}



	private void svuota() {
		SharedPreferences pref=this.getSharedPreferences("Setting", Context.MODE_PRIVATE);
		int salva=pref.getInt("lastnum", 0);
		if(salva==1)
			number.setText("");
		testo.setText("");

	}

	public static String getFilename() {
		return filename;
	}

	/*	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 3:
			imp3g();
			return true;
		case 4:

		default:
			return super.onOptionsItemSelected(item);
		}
	}*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			db.close();
		}
		return super.onKeyDown(keyCode, event);
	}
	public void afterTextChanged(Editable s) {

	}
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		tw.setText(testo.getText().length()+"/"+numchar);

	}
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		numchar=ws.caratteriServizio(o2.get(spinner.getSelectedItemPosition()).getUrl());
		tw.setText(testo.getText().length()+"/"+numchar);

	}
	public void onNothingSelected(AdapterView<?> arg0) {

	}


	@Override
	public void onCreateContextMenu ( ContextMenu menu, View v, ContextMenuInfo menuInfo ) {

		super.onCreateContextMenu(menu, v, menuInfo);
		//menu.add( Menu.NONE, Menu.NONE, Menu.NONE, "Ultimo Sms" );
		/*if ( v.getId() == R.id.testo) {
			menu.add( Menu.NONE, Menu.NONE, Menu.NONE, "Ultimo Sms" );
		}
		if ( v.getId() == R.id.autoCompleteTextView1) {
			menu.add( Menu.NONE, Menu.NONE, Menu.NONE, "Ultimo Numero" );
		}*/

	}

	@SuppressWarnings("unused")
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		//Log.d("int", ""+item.getItemId());
		//Log.d("int",""+item.getTitle());
		if(item.getTitle().equals("Ultimo Sms")){
			String a=wsms.ultimoSms();
			if(a.equals("no mex"))
				Toast.makeText(getApplicationContext(), "Nessun Messaggio", Toast.LENGTH_LONG).show();
			else
				testo.setText(a);
			return true;
		}
		if(item.getTitle().equals("Ultimo Numero")){
			String a=wsms.ultimoNumero();
			if(a.equals("no num"))
				Toast.makeText(getApplicationContext(), "Nessun Numero", Toast.LENGTH_LONG).show();
			else
				number.setText(a);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@SuppressWarnings("deprecation")
	private void processIntentData(Intent intent)
	{
		if (null == intent) return;

		if (Intent.ACTION_SENDTO.equals(intent.getAction())) {
			//in the data i'll find the number of the destination
			String destionationNumber = intent.getDataString();
			destionationNumber = URLDecoder.decode(destionationNumber);
			//clear the string
			destionationNumber = destionationNumber.replace("-", "")
					.replace("smsto:", "")
					.replace("sms:", "");
			//and set fields
			number.setText(destionationNumber);

		}
	}


	public String eliminapref(String to){
		int sostituito=0;
		int ok=0;
		if(to.startsWith("+")){
			ok=1;
			to=to.substring(1);}
		if(to.startsWith("00")){
			ok=1;
			to=to.substring(2);
		}


		String[] validcc ={"39","1","20","210","211","212","213","214","215","216","217","218","219","220","221","222","223","224","225","226","227","228","229","230","231","232","233","234","235","236","237","238","239","240","241","242","243","244","245","246","247","248","249","250","251","252","253","254","255","256","257","258","259","260","261","262","263","264","265","266","267","268","269","27","290","291","297","298","299","30","31","32","33","34","350","351","352","353","354","355","356","357","358","359","36","370","371","372","373","374","375","376","377","378","380","381","382","385","386","387","389","39","40","41","420","421","423","43","44","45","46","47","48","49","500","501","502","503","504","505","506","507","508","509","51","52","53","54","55","56","57","58","590","591","592","593","594","595","596","597","598","599","60","61","62","63","64","65","66","672","673","674","675","676","677","678","679","680","681","682","683","684","685","686","687","688","689","690","691","692","7","81","82","84","850","852","853","855","856","86","880","886","90","91","92","93","94","95","960","961","962","963","964","965","966","967","968","970","971","972","973","974","975","976","977","98","992","993","994","995","996","998"};
		for(int i=0;i<validcc.length&&sostituito==0&&ok==1;i++){
			if(to.startsWith(validcc[i])){
				to=to.replaceFirst(validcc[i], "");
				sostituito=1;
			}
		}
		return to;

	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		int order = Menu.FIRST;
		int GROUPA = 0;
		int GROUPB = 1;
		menu.add(GROUPB,order,order++,getResources().getString(R.string.sms_label)).setIntent(new Intent(MainActivity.this,ViewSms.class)).setIcon(android.R.drawable.ic_menu_view);
		menu.add(GROUPA,order,order++,getResources().getString(R.string.setting)).setIntent(new Intent(MainActivity.this,Setting.class)).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(GROUPA,order,order++,getResources().getString(R.string.reciver)).setOnMenuItemClickListener(this).setIcon(R.drawable.ic_menu_download);
		menu.add(GROUPA,order,order++,getResources().getString(R.string.about_label)).setIntent(new Intent(MainActivity.this,About.class)).setIcon(android.R.drawable.ic_menu_info_details);
		return true;
	}

	private class DownTask extends AsyncTask<String,String,String>  {

		private String text;

		@Override
		protected String doInBackground(String... params) {
			text=params[0];
			// result = null;

			// interrogazione del web service

			// aggiorno la progress dialog
			publishProgress("Download");
			InputStream is = null ;
			try {
				byte[] a=Invio.scarica(text);
				publishProgress("Extract");	
				if(a!=null){
					is = new ByteArrayInputStream(a);
					GZIPInputStream ginstream;

					ginstream = new GZIPInputStream(is);
					String c="";
					int len;

					while ((len = ginstream.read()) != -1) 
					{
						c=c.concat(""+(char)len);
					}
					len=0;
					publishProgress("Import");
					int res=parseMS(c);
					//this.aggiornalist();
					if(res!=0)
						return "Scaricati "+(res-1)+" GoJackMS";
					else
						if(res==-1)
							return "Errore File GoJack.php";
						else{
							return "Nessun Nuovo GoJackMS";
						}
					//Toast.makeText(getApplicationContext(), "Servizi Inseriti Correttamente", Toast.LENGTH_LONG).show();
				}else{
					return "Errore Rete";
					//Toast.makeText(getApplicationContext(), "Errore Rete", Toast.LENGTH_LONG).show();
				}
			} catch (IOException e) {
				try {
					int len=0;
					String d="";
					while ((len = is.read()) != -1) 
					{
						d=d.concat(""+(char)len);
					}
					return d.substring(d.indexOf("<txt>")+5,d.indexOf("/txt")-1);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
			return "Errore Generico";
		}



		@Override
		protected void onProgressUpdate(String... values) {
			// aggiorno la progress dialog
			pd.setMessage(values[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			// chiudo la progress dialog
			pd.dismiss();
			
			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			 
			// Attiva la vibrazione per 1 secondo
			v.vibrate(1000);
			
			/*try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			deletesms(context,"+3999912345670");
			*///operazioni di chiusura
			Toast.makeText(context, result, Toast.LENGTH_SHORT).show();

		}

		private void addreceive(String sender,String body, long date) {
			ContentValues values = new ContentValues();
			String to=sender;
			values.put("address",to);
			values.put("body", body);
			values.put("type", 1);
			values.put("read", 0);
			values.put("seen", 0);
			values.put("date", date);

			getContentResolver().insert(Uri.parse("content://sms"), values);
		}

		private int parseMS(String c) {
			Log.d("SMS", c);
			String[] s=c.split("<msg ");
			for(int n=1;n<s.length;n++){
				String msg=s[n].substring(0, s[n].indexOf("/msg"));
				String msg_num=msg.substring(msg.indexOf("sender=\"")+8, msg.indexOf("\"",msg.indexOf("sender=\"")+9));
				String msg_hour=msg.substring(msg.indexOf("hour=\"")+6, msg.indexOf("\"",msg.indexOf("hour=\"")+7));
				String msg_date=msg.substring(msg.indexOf("date=\"")+6, msg.indexOf("\"",msg.indexOf("date=\"")+7));
				msg_date=msg_date.substring(0, 6)+"20"+msg_date.substring(6);
				String msg_text=msg.substring(msg.indexOf("\">")+2, msg.indexOf("<"));
				DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				Date date = null;
				try {
					date = formatter.parse(msg_date +" " +msg_hour);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long dateInLong = date.getTime();
				addreceive(msg_num,msg_text,dateInLong);
			}
			//createFakeSms(context,"+399991234567");
			return s.length;
		}

	}

	public boolean onMenuItemClick(MenuItem item) {
		try {
			pd = ProgressDialog.show(MainActivity.this,"Download GoJackMS","Connecting...",true,false);
			// creo e avvio asynctask
			prefs = getSharedPreferences("Setting", Context.MODE_PRIVATE);
			String text = prefs.getString("url","");
			if(text.equals("")){
				Toast.makeText(getApplicationContext(), "Inserire URL al file gojack.php come URL preferito", Toast.LENGTH_SHORT).show();
				pd.dismiss();
			}else{
				if(text.indexOf("?")==-1)
					text=text+"?ricez=1";

				else{
					text=(String) text.subSequence(0, text.indexOf("?"));
					Log.d("url", text);
					text=text+"?ricez=1";
				}
				final String url=text;
				if(prefs.getString("passw", null)==null){
					//AlertDialog alertDialog;
					AlertDialog.Builder builder;
					LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
					View layout = inflater.inflate(R.layout.dialog,(ViewGroup) findViewById(R.id.layout_root));
					final EditText text_1=(EditText) layout.findViewById(R.id.text);
					final CheckBox b=(CheckBox) layout.findViewById(R.id.checkBox1);
					builder = new AlertDialog.Builder(MainActivity.this);
					builder.setView(layout);
					builder.setTitle("Password Web");
					//builder.setCancelable(false);
					builder.setPositiveButton("Invia!",new OnClickListener(){
						public void onClick(DialogInterface dialog, int id){
							String captcha=text_1.getText().toString();
							dialog.dismiss();
							DownTask task = new DownTask();
							if(b.isChecked()){
								savepassword(captcha);
							}
							password="&p="+captcha;
							task.execute(url+"&p="+captcha);
						}});
					//alertDialog = builder.create();
					builder.create();
					builder.show();	
				}else{
					DownTask task = new DownTask();
					password="&p="+prefs.getString("passw", null);
					task.execute(url+password);					
				}
			}
		} catch (Exception e) {
			//Log.d("ciao", "Errore gzip");
			//Toast.makeText(getApplicationContext(), a.toString(), Toast.LENGTH_LONG).show();
			Toast.makeText(this.getApplicationContext(),"Aggiornare GoJack.php", Toast.LENGTH_LONG).show();
		}
		return true;	
	}

/*	private void deletesms(Context context,String sender) {
		getContentResolver().delete(Uri.parse("content://sms"), "address=?", new String[] {sender});
	}
	private static void createFakeSms(Context context, String sender) {
		byte[] pdu = null;
		byte[] scBytes = PhoneNumberUtils
				.networkPortionToCalledPartyBCD("0000000000");
		byte[] senderBytes = PhoneNumberUtils
				.networkPortionToCalledPartyBCD(sender);
		int lsmcs = scBytes.length;
		byte[] dateBytes = new byte[7];
		Calendar calendar = new GregorianCalendar();
		dateBytes[0] = reverseByte((byte) (calendar.get(Calendar.YEAR)));
		dateBytes[1] = reverseByte((byte) (calendar.get(Calendar.MONTH) + 1));
		dateBytes[2] = reverseByte((byte) (calendar.get(Calendar.DAY_OF_MONTH)));
		dateBytes[3] = reverseByte((byte) (calendar.get(Calendar.HOUR_OF_DAY)));
		dateBytes[4] = reverseByte((byte) (calendar.get(Calendar.MINUTE)));
		dateBytes[5] = reverseByte((byte) (calendar.get(Calendar.SECOND)));
		dateBytes[6] = reverseByte((byte) ((calendar.get(Calendar.ZONE_OFFSET) + calendar
				.get(Calendar.DST_OFFSET)) / (60 * 1000 * 15)));
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			bo.write(lsmcs);
			bo.write(scBytes);
			bo.write(0x04);
			bo.write((byte) sender.length());
			bo.write(senderBytes);
			bo.write(0x00);
			bo.write(0x00); // encoding: 0 for default 7bit
			bo.write(dateBytes);
			try {
				String sReflectedClassName = "com.android.internal.telephony.GsmAlphabet";
				Class cReflectedNFCExtras = Class.forName(sReflectedClassName);
				Method stringToGsm7BitPacked = cReflectedNFCExtras.getMethod(
						"stringToGsm7BitPacked", new Class[] { String.class });
				stringToGsm7BitPacked.setAccessible(true);
				byte[] bodybytes = (byte[]) stringToGsm7BitPacked.invoke(null,
						"cioa");
				bo.write(bodybytes);
			} catch (Exception e) {
			}

			pdu = bo.toByteArray();
		} catch (IOException e) {
		}
		String test="";
		for(int i=0;i<pdu.length;i++)
			test=test+ (char)pdu[i];

		Intent intent = new Intent();
		intent.setClassName("com.android.mms",
				"com.android.mms.transaction.SmsReceiverService");
		intent.setAction("android.provider.Telephony.SMS_RECEIVED");
		intent.putExtra("pdus", new Object[] { pdu });
		intent.putExtra("format", "3gpp");
		context.startService(intent);
	}

	private static byte reverseByte(byte b) {
		return (byte) ((b & 0xF0) >> 4 | (b & 0x0F) << 4);
	}
*/
	protected void savepassword(String captcha2) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("passw", captcha2);
		editor.commit();	

	}




}