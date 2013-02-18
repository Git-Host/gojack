package it.ciopper90.gojack2.added;

import it.ciopper90.gojack2.R;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Setting extends Activity {
	private SharedPreferences pref;
	private ListView listView;
	String[] menu = { "Connettivita", "Servizi", "Url Server", "Password GoJackWeb" };

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.setting_2);
		this.pref = this.getSharedPreferences("Setting", Context.MODE_PRIVATE);
		this.aggiornalist();

		this.listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(final AdapterView<?> parent, final View v, final int position,
					final long id) {
				Setting.this.listView.setItemChecked(position, true);
				Intent intent = null;
				switch (position) {
				case 0:
					intent = new Intent(Setting.this, Connettivita.class);
					// Toast.makeText(getApplicationContext(), menu[position],
					// Toast.LENGTH_SHORT).show();
					break;
				case 1:
					intent = new Intent(Setting.this, ViewServizio.class);
					// Toast.makeText(getApplicationContext(), menu[position],
					// Toast.LENGTH_SHORT).show();
					break;
				case 2:
					intent = new Intent(Setting.this, PersonalServerConfig.class);
					// Toast.makeText(getApplicationContext(), menu[position],
					// Toast.LENGTH_SHORT).show();
					break;
				case 3:
					intent = new Intent(Setting.this, Password.class);
					// Toast.makeText(getApplicationContext(), menu[position],
					// Toast.LENGTH_SHORT).show();
					break;
				}
				if (intent != null) {
					Setting.this.startActivity(intent);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		this.aggiornalist();
		super.onResume();
	}

	private void aggiornalist() {
		// Questa è la lista che rappresenta la sorgente dei dati della
		// listview
		// ogni elemento è una mappa(chiave->valore)
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
			if (s.equals(this.menu[2])) {
				ServiceMap.put("user", this.pref.getString("url", "Nessun Server"));
			} else {
				ServiceMap.put("user", "");
			}
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