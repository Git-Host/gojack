package it.ciopper90.gojack2;

import it.ciopper90.gojack2.R;

import java.util.ArrayList;
import java.util.HashMap;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Connettivita extends Activity {
	private ListView listView;
	String[] menu = { "3G", "WiFi" };
	private WifiManager wifi;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.setting_2);
		this.wifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE); // istanzio
																				// l'oggetto
																				// wifi(classe
																				// WifiManager)
		this.aggiornalist();

		this.listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(final AdapterView<?> parent, final View v, final int position,
					final long id) {
				Connettivita.this.listView.setItemChecked(position, true);
				switch (position) {
				case 0:
					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
						final ComponentName cn = new ComponentName("com.android.phone",
								"com.android.phone.MobileNetworkSettings");
						final Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
						intent.addCategory(Intent.ACTION_MAIN);
						intent.setComponent(cn);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Connettivita.this.startActivity(intent);

					} else {
						Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
						ComponentName cName = new ComponentName("com.android.phone",
								"com.android.phone.Settings");
						intent.setComponent(cName);
						Connettivita.this.startActivity(intent);
					}

					break;
				case 1:
					if (Connettivita.this.wifi.isWifiEnabled()) {
						Connettivita.this.wifi.setWifiEnabled(false);
					} else {
						Connettivita.this.wifi.setWifiEnabled(true);
					}
					break;
				}
			}
		});
	}

	private void aggiornalist() {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

		HashMap<String, Object> ServiceMap = new HashMap<String, Object>();// creiamo
																			// una
																			// mappa
																			// di
																			// valori

		for (int i = 0; i < this.menu.length; i++) {
			String s = this.menu[i];// per ogni persona all'inteno della ditta

			ServiceMap = new HashMap<String, Object>();// creiamo una mappa di
														// valori

			ServiceMap.put("name", s); // per la chiave image, inseriamo la
										// risorsa dell immagine
			// ServiceMap.put("user", "");
			data.add(ServiceMap); // aggiungiamo la mappa di valori alla
									// sorgente dati
		}

		String[] from = { "name", "user" }; // dai valori contenuti in queste
											// chiavi
		int[] to = { R.id.setting_name, R.id.setting_text };// agli id delle
															// view

		// costruzione dell adapter
		SimpleAdapter adapter = new SimpleAdapter(this.getApplicationContext(), data,// sorgente
																						// dati
				R.layout.setting_1, // layout contenente gli id di "to"
				from, to);

		// utilizzo dell'adapter
		((ListView) this.findViewById(R.id.setting_1)).setAdapter(adapter);
		this.listView = (ListView) this.findViewById(R.id.setting_1);
	}
}
