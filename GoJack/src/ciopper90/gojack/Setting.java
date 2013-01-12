package ciopper90.gojack;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;


public class Setting extends Activity{
	private SharedPreferences pref;
	private ListView listView;
	String [] menu={"Connettivita","Generali","Servizi","Url Server"};
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.setting_2);
		pref=getSharedPreferences("Setting", Context.MODE_PRIVATE);
		aggiornalist();
		
		listView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{
				listView.setItemChecked(position, true);
				Intent intent=null;
				switch(position){
				case 0:intent=new Intent(Setting.this,Connettivita.class);
					   //Toast.makeText(getApplicationContext(), menu[position], Toast.LENGTH_SHORT).show();
					   break;
				case 1:intent=new Intent(Setting.this,Generali.class);
				       //Toast.makeText(getApplicationContext(), menu[position], Toast.LENGTH_SHORT).show();
				       break;
				case 2:intent=new Intent(Setting.this,ViewServizio.class);
				       //Toast.makeText(getApplicationContext(), menu[position], Toast.LENGTH_SHORT).show();
					   break;
				case 3:intent=new Intent(Setting.this,PersonalServerConfig.class);
			           //Toast.makeText(getApplicationContext(), menu[position], Toast.LENGTH_SHORT).show();
			           break;
				}
				if(intent!=null)
					startActivity(intent);
			}
		});  
	}
	
	
	
	
	@Override
	protected void onResume() {
		aggiornalist();
		super.onResume();
	}




	private void aggiornalist() {
		ArrayList<String> test=new ArrayList<String>();
		//Questa è la lista che rappresenta la sorgente dei dati della listview
		//ogni elemento è una mappa(chiave->valore)
		ArrayList<HashMap<String, Object>> data=new ArrayList<HashMap<String,Object>>();

		HashMap<String, Object> ServiceMap=new HashMap<String, Object>();//creiamo una mappa di valori

		for(int i=0;i<menu.length;i++){
			String s=menu[i];// per ogni persona all'inteno della ditta

			ServiceMap=new HashMap<String, Object>();//creiamo una mappa di valori

			ServiceMap.put("name", s); // per la chiave image, inseriamo la risorsa dell immagine
			String c=pref.getString("url", "Nessun Server");
			if(s.equals(menu[3])){
				ServiceMap.put("user", pref.getString("url", "Nessun Server"));
			}
			else
				ServiceMap.put("user", "");
			data.add(ServiceMap);  //aggiungiamo la mappa di valori alla sorgente dati
		}


		String[] from={"name","user"}; //dai valori contenuti in queste chiavi
		int[] to={R.id.setting_name,R.id.setting_text};//agli id delle view

		//costruzione dell adapter
		SimpleAdapter adapter=new SimpleAdapter(
				getApplicationContext(),
				data,//sorgente dati
				R.layout.setting_1, //layout contenente gli id di "to"
				from,
				to);

		//utilizzo dell'adapter
		((ListView)findViewById(R.id.setting_1)).setAdapter(adapter);
		listView=(ListView)findViewById(R.id.setting_1);
	}
		
}