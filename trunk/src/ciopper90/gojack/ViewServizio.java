package ciopper90.gojack;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import ciopper90.gojack.utility.DatabaseService;
import ciopper90.gojack.utility.DatiServizio;
import ciopper90.gojack.utility.ParametriServizio;
import ciopper90.gojack.utility.Servizio;
import ciopper90.gojack.work.WorkServizio;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ViewServizio extends Activity{
	private ListView listView;
	private ArrayList<Servizio> ServiceList; 
	private ArrayList<DatiServizio> DataServiceList; 
	private ArrayList<ParametriServizio> ParameterServiceList; 
	public static DatabaseService db;
	private SharedPreferences prefs;
	private ProgressDialog pd;



	private String dati[];
	private String config[];
	private String parametri[];
	private String status;
	
	private String password="";


	@SuppressWarnings("static-access")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewservizio);
		this.db=MainActivity.db;
		aggiornalist();



		registerForContextMenu(listView);
		listView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{
				listView.setItemChecked(position, true);
				if(position==0){
					Intent add=new Intent(ViewServizio.this,AddScript.class);
					startActivity(add);
				}else{
					modify(position);
				}
			}
		});  
	}

	public void modify(int position){
		Intent mod=new Intent(ViewServizio.this,AddServizio.class);
		Servizio s=ServiceList.get(position-1);
		DatiServizio d=null;
		if (!s.getName().equalsIgnoreCase("GoJackMS")) {
		for(int i=0;i<DataServiceList.size();i++){
			if(DataServiceList.get(i).getUrl().equals(s.getUrl())){
				d=DataServiceList.get(i);				
			}
		}
		ParametriServizio p=null;
		for(int i=0;i<ParameterServiceList.size();i++){
			if(ParameterServiceList.get(i).getUrl().equals(s.getUrl())){
				p=ParameterServiceList.get(i);				
			}
		}

		dati=new String[6];
		dati[0]=Integer.toString(d.getDati()[0]);
		dati[1]=Integer.toString(d.getDati()[1]);
		dati[2]=Integer.toString(d.getDati()[2]);
		dati[3]=Integer.toString(d.getDati()[3]);
		dati[4]=Integer.toString(d.getDati()[4]);
		dati[5]=Integer.toString(d.getDati()[5]);

		config=new String[7];
		config[0]=s.getName();
		config[1]=s.getUrl();
		config[2]=s.getPrimo();
		config[3]=s.getSecondo();
		config[4]=s.getTerzo();
		config[5]=s.getQuarto();
		config[6]=s.getFirma();

		parametri=p.getParametri();

		mod.putExtra("id", s.getId());
		mod.putExtra("dati", dati);
		mod.putExtra("parametri", parametri);
		mod.putExtra("config", config);
		startActivity(mod);
		}
	}




	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		if (v.getId()==R.id.viewservizio) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			if(info.position!=0){
				String[] menuItems = {"Modifica","Elimina","Duplica"};
				for (int i = 0; i<menuItems.length; i++) {
					menu.add(Menu.NONE, i, i, menuItems[i]);
				}}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		int scelto=info.position;
		switch (item.getItemId()) {
		case 0:
			modify(scelto);
			return true;
		case 1:
			db.open();
			db.DeleteService(ServiceList.get(scelto-1).getId());
			aggiornalist();
			db.close();
			return true;
		case 2:
			duplica(scelto);
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void duplica(int scelto) {
		Servizio s=ServiceList.get(scelto-1);
		s.setName(s.getName());
		ServiceList.add(s);
		db.open();
		db.insertService(s.getName(),s.getServizio(), s.getPrimo(), s.getSecondo(), s.getTerzo(), s.getQuarto(), s.getUrl(), s.getFirma());
		db.close();
		aggiornalist();
	}

	@Override
	public void onResume() {
		super.onResume();
		aggiornalist();
	}

	private void aggiornalist() {
		ServiceList=new ArrayList<Servizio>();  
		WorkServizio w=new WorkServizio();
		ServiceList=w.caricaServizio();
		DataServiceList=w.caricaDataServizio();
		ParameterServiceList=w.caricaParametriServizio();
		if(ServiceList==null){
			ServiceList=new ArrayList<Servizio>();
		}


		//Questa è la lista che rappresenta la sorgente dei dati della listview
		//ogni elemento è una mappa(chiave->valore)
		ArrayList<HashMap<String, Object>> data=new ArrayList<HashMap<String,Object>>();

		HashMap<String,Object> ServiceMap=new HashMap<String, Object>();//creiamo una mappa di valori

		ServiceMap.put("name", "Aggiungi Servizio"); // per la chiave image, inseriamo la risorsa dell immagine
		ServiceMap.put("user", "");
		data.add(ServiceMap);  //aggiungiamo la mappa di valori alla sorgente dati

		for(int i=0;i<ServiceList.size();i++){
			Servizio s=ServiceList.get(i);// per ogni persona all'inteno della ditta

			ServiceMap=new HashMap<String, Object>();//creiamo una mappa di valori

			ServiceMap.put("name", s.getName()); // per la chiave image, inseriamo la risorsa dell immagine
			//ServiceMap.put("user", s.getUser());
			data.add(ServiceMap);  //aggiungiamo la mappa di valori alla sorgente dati
		}


		String[] from={"name","user"}; //dai valori contenuti in queste chiavi
		int[] to={R.id.Name,R.id.User};//agli id delle view

		//costruzione dell adapter
		SimpleAdapter adapter=new SimpleAdapter(
				getApplicationContext(),
				data,//sorgente dati
				R.layout.servizio, //layout contenente gli id di "to"
				from,
				to);

		//utilizzo dell'adapter
		((ListView)findViewById(R.id.viewservizio)).setAdapter(adapter);
		listView=(ListView)findViewById(R.id.viewservizio);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			prefs = getSharedPreferences("Setting", Context.MODE_PRIVATE);
			String textData = prefs.getString("url","");
			String result="";
			if(textData.equals(""))
				Toast.makeText(getApplicationContext(), "Inserire URL al file gojack.php come URL preferito", Toast.LENGTH_SHORT).show();
			else{
				if(textData.indexOf("?")==-1)
					result=Invio.aggiorna(textData+"?aggserver=1");
				else{
					textData=(String) textData.subSequence(0, textData.indexOf("?"));
					Log.d("url", textData);
					result=Invio.aggiorna(textData+"?aggserver=1");
				}
				Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
			}
			return true;
		case 2:byte[]a = null;
		try {
			pd = ProgressDialog.show(ViewServizio.this,"Import Servizi","Connecting...",true,false);
			// creo e avvio asynctask
			prefs = getSharedPreferences("Setting", Context.MODE_PRIVATE);
			String text = prefs.getString("url","");
			if(text.equals("")){
				Toast.makeText(getApplicationContext(), "Inserire URL al file gojack.php come URL preferito", Toast.LENGTH_SHORT).show();
				pd.dismiss();
			}else{
				if(text.indexOf("?")==-1)
					//a=Invio.scarica(text+"?sincweb=1");
					text=text+"?sincweb=1";
				//res=Invio.scarica(text+"?sincweb=1");

				else{
					text=(String) text.subSequence(0, text.indexOf("?"));
					Log.d("url", text);
					text=text+"?sincweb=1";
					//a=Invio.scarica(text+"?sincweb=1");
					//res=Invio.scarica(text+"?sincweb=1");
				}
				final String url=text;
				AlertDialog alertDialog;
				AlertDialog.Builder builder;
				LayoutInflater inflater = (LayoutInflater) ViewServizio.this.getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.dialog,(ViewGroup) findViewById(R.id.layout_root));
				final EditText text_1=(EditText) layout.findViewById(R.id.text);
				builder = new AlertDialog.Builder(ViewServizio.this);
				builder.setView(layout);
				builder.setTitle("Password Web");
				builder.setCancelable(false);
				builder.setPositiveButton("Invia!",new OnClickListener(){
					public void onClick(DialogInterface dialog, int id){
						String captcha=text_1.getText().toString();
						dialog.dismiss();
						DownTask task = new DownTask();
						task.execute(url+"&p="+captcha);
						password="&p="+captcha;
					}});
				alertDialog = builder.create();
				builder.show();	
				/*				//byte[] a=Base64.decode(res, Base64.DEFAULT);
				if(a!=null){
					InputStream is = new ByteArrayInputStream(a);	
					GZIPInputStream ginstream =new GZIPInputStream(is);
					String c="";
					int len;
					while ((len = ginstream.read()) != -1) 
					{
						c=c.concat(""+(char)len);
					}
					len=0;
					parseService(c);
					this.aggiornalist();
					Toast.makeText(getApplicationContext(), "Servizi Inseriti Correttamente", Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(getApplicationContext(), "Errore Rete", Toast.LENGTH_LONG).show();
				}*/
			}
		} catch (Exception e) {
			//Log.d("ciao", "Errore gzip");
			//Toast.makeText(getApplicationContext(), a.toString(), Toast.LENGTH_LONG).show();
			Toast.makeText(this.getApplicationContext(),"Aggiornare GoJack.php", Toast.LENGTH_LONG).show();
			//	e.printStackTrace();
		}
		return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		int order = Menu.FIRST;
		int GROUPB = 1;
		menu.add(GROUPB,order,order++,getResources().getString(R.string.serverpersonaleupdate)).setIcon(R.drawable.ic_menu_refresh);
		menu.add(GROUPB,order,order++,getResources().getString(R.string.download_service)).setIcon(R.drawable.ic_menu_download);
		return true;
	}

	public void not(String a){
		this.aggiornalist();
		Toast.makeText(getApplicationContext(), a, Toast.LENGTH_LONG).show();
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
					int res=parseService(c);
					//this.aggiornalist();
					if(res==0)
						return "Servizi Inseriti Correttamente";
					else
						return "Errore File GoJack.php";
					//Toast.makeText(getApplicationContext(), "Servizi Inseriti Correttamente", Toast.LENGTH_LONG).show();
				}else{
					return "Errore Rete";
					//Toast.makeText(getApplicationContext(), "Errore Rete", Toast.LENGTH_LONG).show();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
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

			//operazioni di chiusura
			not(result);

		}


		private int parseService(String c) {
			db.open();
			String[] s=c.split("<account ");
			/*for(int i=1;i<s.length;i++){
				s[i]=s[i].substring(0, s[i].indexOf("/account")+9);
			}*/
			for(int i=1;i<s.length;i++){
				int dist=s[i].indexOf("/data")-1-(s[i].indexOf("<data>")+6);
				if(dist<=10){
					Log.d("servi", "errore");
				}else{
					//Log.d("servi","ok");
					if(text.indexOf("?")==-1)
						text=text+"?servizio=";
					else{
						text=(String) text.subSequence(0, text.indexOf("?"));
						text=text+"?servizio=";
					}
				}
			}
			for(int n=1;n<s.length;n++){
				//richiesta parametri x service
				//HttpClient httpclient = new DefaultHttpClient();
				int primo=s[n].indexOf("service=\"")+9;
				int secondo=s[n].indexOf("\"", primo);
				String name=s[n].substring(primo,secondo);
				primo=s[n].indexOf("name=\"")+6;
				secondo=s[n].indexOf("\"", primo);
				String nameservice=s[n].substring(primo,secondo);
				String url=text+name+password;
				//response = httpclient.execute(httppost);
				String returnString=null;
				try{
					returnString=s[n].substring(s[n].indexOf("<res>"),s[n].indexOf("/res")+5);
				}catch(Exception e){
					return 1;
				}
				if(returnString.indexOf("<config>")!=-1){
					if(returnString.indexOf("<res>")!=-1){
						//elaborare returnString per poi salvare il dato nel database
						dati=new String[6];
						dati[0]=(String) returnString.subSequence(returnString.indexOf("<nu>")+4, returnString.indexOf("</nu>"));
						dati[1]=(String) returnString.subSequence(returnString.indexOf("<np>")+4, returnString.indexOf("</np>"));
						dati[2]=(String) returnString.subSequence(returnString.indexOf("<nn>")+4, returnString.indexOf("</nn>"));
						dati[3]="0";//returnString.subSequence(returnString.indexOf("<nu>"+4), returnString.indexOf("</nu>"));
						dati[4]=(String) returnString.subSequence(returnString.indexOf("<mc>")+4, returnString.indexOf("</mc>"));
						if(returnString.indexOf("<mm>")!=-1)
							dati[5]=(String) returnString.subSequence(returnString.indexOf("<mm>")+4, returnString.indexOf("</mm>"));
						else
							dati[5]=(String) returnString.subSequence(returnString.indexOf("<mmm>")+5, returnString.indexOf("</mmm>"));	
						db.insertDataService(nameservice, Integer.parseInt(dati[0]), Integer.parseInt(dati[1]), Integer.parseInt(dati[2]),Integer.parseInt(dati[3]), Integer.parseInt(dati[4]), Integer.parseInt(dati[5]), url);
						int count=0;
						for(int i=0;i<4;i++){
							if(dati[i].equals("1")||dati[i].equals("2"))
								count++;
						}
						parametri=new String[5];
						if(returnString.indexOf("<n1>")!=-1){
							switch(count){
							case 4:parametri[4]=(String) returnString.subSequence(returnString.indexOf("<n4>")+4, returnString.indexOf("</n4>"));
							case 3:parametri[3]=(String) returnString.subSequence(returnString.indexOf("<n3>")+4, returnString.indexOf("</n3>"));
							case 2:parametri[2]=(String) returnString.subSequence(returnString.indexOf("<n2>")+4, returnString.indexOf("</n2>"));
							case 1:parametri[1]=(String) returnString.subSequence(returnString.indexOf("<n1>")+4, returnString.indexOf("</n1>"));
							parametri[0]="Nome Servizio";
							}
						}else{
							parametri[0]="Nome Servizio";
							parametri[1]="Username:";
							parametri[2]="Password";
							parametri[3]="Nick";		
						}
						String[] conf=new String[4];
						for(int k=0;k<conf.length;k++)
							conf[k]="";
						switch(count){
						case 1:db.insertParameterService(nameservice,parametri[1],null,null,null,url);
						conf[0]=s[n].substring(s[n].indexOf("<"+parametri[1]+">")+2+parametri[1].length(),s[n].indexOf("/"+parametri[1])-1);					
						break;
						case 2:db.insertParameterService(nameservice,parametri[1],parametri[2],null,null,url);
						conf[0]=s[n].substring(s[n].indexOf("<"+parametri[1]+">")+2+parametri[1].length(),s[n].indexOf("/"+parametri[1])-1);					
						conf[1]=s[n].substring(s[n].indexOf("<"+parametri[2]+">")+2+parametri[2].length(),s[n].indexOf("/"+parametri[2])-1);					
						break;
						case 3:db.insertParameterService(nameservice,parametri[1],parametri[2],parametri[3],null,url);
						conf[0]=s[n].substring(s[n].indexOf("<"+parametri[1]+">")+2+parametri[1].length(),s[n].indexOf("/"+parametri[1])-1);					
						conf[1]=s[n].substring(s[n].indexOf("<"+parametri[2]+">")+2+parametri[2].length(),s[n].indexOf("/"+parametri[2])-1);
						conf[2]=s[n].substring(s[n].indexOf("<"+parametri[3]+">")+2+parametri[3].length(),s[n].indexOf("/"+parametri[3])-1);					
						break;
						case 4:db.insertParameterService(nameservice,parametri[1],parametri[2],parametri[3],parametri[4],url);
						conf[0]=s[n].substring(s[n].indexOf("<"+parametri[1]+">")+2+parametri[1].length(),s[n].indexOf("/"+parametri[1])-1);					
						conf[1]=s[n].substring(s[n].indexOf("<"+parametri[2]+">")+2+parametri[2].length(),s[n].indexOf("/"+parametri[2])-1);
						conf[2]=s[n].substring(s[n].indexOf("<"+parametri[3]+">")+2+parametri[3].length(),s[n].indexOf("/"+parametri[3])-1);	
						conf[3]=s[n].substring(s[n].indexOf("<"+parametri[4]+">")+2+parametri[4].length(),s[n].indexOf("/"+parametri[4])-1);	
						break;
						}
						//prima di inserire i dati bisogna controllare se ci sono gia nel db
						Cursor curs=db.fetchService(nameservice);
						String nome="";
						if(url.indexOf("&", url.indexOf("servizio"))!=-1){
							nome=(String) url.subSequence(url.indexOf("servizio=")+9, url.indexOf("&", url.indexOf("servizio=")));
						}else{
							nome=(String) url.toString().subSequence(url.indexOf("servizio=")+9, url.length());	
						}
						if(!(curs.moveToNext()))
							db.insertService(nameservice,nome, conf[0],conf[1],conf[2],conf[3],url,"");
						curs.close();
					}
				}
			}
			db.close();
			return 0;
		}

	}


}

