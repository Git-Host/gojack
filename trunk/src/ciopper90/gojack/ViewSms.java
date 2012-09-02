package ciopper90.gojack;

import java.util.ArrayList;
import java.util.HashMap;

import ciopper90.gojack.utility.DatabaseService;
import ciopper90.gojack.utility.SMS;
import ciopper90.gojack.work.WorkSms;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ViewSms extends Activity{
	private ListView listView;
	private ArrayList<SMS> SmsList; 
	public static DatabaseService db;


	@SuppressWarnings("static-access")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewsms);
		this.db=MainActivity.db;
		aggiornalist();
		registerForContextMenu(listView);
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		if (v.getId()==R.id.viewsms) {
			@SuppressWarnings("unused")
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			String[] menuItems = {"Inoltra","Elimina"};
			for (int i = 0; i<menuItems.length; i++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		int scelto=info.position;
		switch (item.getItemId()) {
		case 0:
			Toast.makeText(getApplicationContext(),SmsList.get(scelto).getNumero(),Toast.LENGTH_LONG).show();
			return true;
		case 1:db.open();
		db.DeleteSms(Integer.toString(SmsList.get(scelto).getId()));
		db.close();
		this.aggiornalist();
		Toast.makeText(getApplicationContext(),"Sms Cancellato",Toast.LENGTH_LONG).show();
		return true;
		default:
			return super.onContextItemSelected(item);
		}
	}


	@Override
	public void onResume() {
		super.onResume();
		aggiornalist();
	}

	private void aggiornalist() {
		WorkSms w=new WorkSms();
		SmsList=w.CaricaSms();
		if(SmsList==null){
			SmsList=new ArrayList<SMS>();
		}


		//Questa è la lista che rappresenta la sorgente dei dati della listview
		//ogni elemento è una mappa(chiave->valore)
		ArrayList<HashMap<String, Object>> data=new ArrayList<HashMap<String,Object>>();

		HashMap<String,Object> ServiceMap=new HashMap<String, Object>();//creiamo una mappa di valori

		for(int i=0;i<SmsList.size();i++){
			SMS s=SmsList.get(i);// per ogni persona all'inteno della ditta

			ServiceMap=new HashMap<String, Object>();//creiamo una mappa di valori

			if(s.getNome().equals("")){
				ServiceMap.put("sms_nome", s.getNumero()); // per la chiave image, inseriamo la risorsa dell immagine
			}else{
				ServiceMap.put("sms_nome", s.getNome()); // per la chiave image, inseriamo la risorsa dell immagine
			}
			ServiceMap.put("sms_text", s.getTesto());
			ServiceMap.put("sms_id", s.getId());

			data.add(ServiceMap);  //aggiungiamo la mappa di valori alla sorgente dati
		}


		String[] from={"sms_nome","sms_text","sms_id"}; //dai valori contenuti in queste chiavi
		int[] to={R.id.sms_name,R.id.sms_text,R.id.sms_id};//agli id delle view

		//costruzione dell adapter
		SimpleAdapter adapter=new SimpleAdapter(
				getApplicationContext(),
				data,//sorgente dati
				R.layout.sms, //layout contenente gli id di "to"
				from,
				to);

		//utilizzo dell'adapter
		((ListView)findViewById(R.id.viewsms)).setAdapter(adapter);
		listView=(ListView)findViewById(R.id.viewsms);
	}



	/*@Override
public boolean onCreateOptionsMenu(Menu menu){
	super.onCreateOptionsMenu(menu);
	int order = Menu.FIRST;
	int GROUPA = 0;
	int GROUPB = 1;
	menu.add(GROUPA,order,order++,getResources().getString(R.string.cancellatutti)).setIcon(android.R.drawable.ic_menu_preferences);
	//menu.add(GROUPB,order,order++,getResources().getString(R.string.about_label)).setIntent(new Intent(MainActivity.this,About.class)).setIcon(android.R.drawable.ic_menu_info_details);
	// menu.add(GROUPB,order,order++,getResources().getString(R.string.about_label)).setIntent(new Intent(info3.this,About.class)).setIcon(android.R.drawable.ic_menu_info_details);
	return true;
}*/
}
