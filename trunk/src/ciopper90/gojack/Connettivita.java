package ciopper90.gojack;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class Connettivita extends Activity{
	private ListView listView;
	String [] menu={"3G","WiFi"};
	private WifiManager wifi;
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.setting_2);
		wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE); // istanzio l'oggetto wifi(classe WifiManager)
		aggiornalist();
		
		listView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{
				listView.setItemChecked(position, true);
				switch(position){
				case 0:Intent intent=new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
				       ComponentName cName = new ComponentName("com.android.phone","com.android.phone.Settings");
				       intent.setComponent(cName); 
				       startActivity(intent);
				       break;
				case 1:if(wifi.isWifiEnabled()){
					wifi.setWifiEnabled(false);
				}
				else				
				{
					wifi.setWifiEnabled(true);
				}
					break;
				}
			}
		});  
	}

	private void aggiornalist() {
		ArrayList<HashMap<String, Object>> data=new ArrayList<HashMap<String,Object>>();

		HashMap<String, Object> ServiceMap=new HashMap<String, Object>();//creiamo una mappa di valori

		for(int i=0;i<menu.length;i++){
			String s=menu[i];// per ogni persona all'inteno della ditta

			ServiceMap=new HashMap<String, Object>();//creiamo una mappa di valori

			ServiceMap.put("name", s); // per la chiave image, inseriamo la risorsa dell immagine
			//ServiceMap.put("user", "");
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
