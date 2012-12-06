package it.ciopper90.gojack2;


import it.ciopper90.gojack2.R;
import it.ciopper90.gojack2.utils.DatabaseService;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AddScript extends Activity {
	private EditText name, url;
	private DatabaseService db;

	// dati che servono all'activity successiva
	private String dati[];
	private String config[];
	private String parametri[];
	int count;
	private int errore;
	private SharedPreferences prefs;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.addscript);
		// aggiungere shared preferences
		this.prefs = this.getSharedPreferences("Setting", Context.MODE_PRIVATE);
		String textData = this.prefs.getString("url", "http://");

		this.db = new DatabaseService(this.getApplicationContext());
		this.db.open();
		this.errore = 0;
		this.name = (EditText) this.findViewById(R.id.namesc);
		this.url = (EditText) this.findViewById(R.id.urlsc);
		this.url.setText(textData);
		this.config = new String[2];

		Button salva = (Button) this.findViewById(R.id.Salvasc);
		salva.setOnClickListener(new Button.OnClickListener() {

			public void onClick(final View arg0) {
				AddTask task = new AddTask();
				AddScript.this.config[1] = AddScript.this.url.getText().toString().replace(" ", "");
				AddScript.this.config[0] = AddScript.this.name.getText().toString();
				task.execute(AddScript.this.config[1]);

			}
		});

	}

	Handler effettuato = new Handler() {

		@Override
		public void handleMessage(final android.os.Message msg) {
			Bundle bundle = msg.getData();
			int cond = bundle.getInt("cond");
			String text = bundle.getString("text");
			AddScript.this.setText(text, cond);
		}
	};

	public void setText(final String text, final int cond) {
		if (cond == 0) {
			this.name.setVisibility(View.GONE);
			this.url.setVisibility(View.GONE);
			Button b = (Button) AddScript.this.findViewById(R.id.Salvasc);
			b.setVisibility(View.GONE);
			TextView tw1 = (TextView) AddScript.this.findViewById(R.id.textView3sc);
			tw1.setVisibility(View.GONE);
			TextView tw = (TextView) AddScript.this.findViewById(R.id.name1sc);
			tw.setText(text);
		}
		if (cond == 1) {
			TextView tw = (TextView) AddScript.this.findViewById(R.id.textView00);
			tw.setText("Errore link");
		}

	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && this.dati != null) {
			Intent i = new Intent(this, AddServizio.class);
			i.putExtra("dati", this.dati);
			i.putExtra("parametri", this.parametri);
			i.putExtra("config", this.config);
			this.startActivity(i);
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private class AddTask extends AsyncTask<String, String, String> {

		private String text;

		@Override
		protected String doInBackground(final String... params) {
			this.text = params[0];

			// bisogna parsare il risultato del server
			// per capire come va configurato il servizio
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost;
			if (AddScript.this.config[1].contains("?servizio=")) {
				httppost = new HttpPost(AddScript.this.config[1] + "&action=config");
			} else {
				httppost = new HttpPost(AddScript.this.config[1] + "?action=config");
			}
			HttpResponse response;
			try {
				response = httpclient.execute(httppost);
				String returnString = EntityUtils.toString(response.getEntity());
				if (returnString.indexOf("<config>") != -1) {
					if (returnString.indexOf("<res>") != -1) {
						// elaborare returnString per poi salvare il dato
						// nel database
						AddScript.this.dati = new String[6];
						AddScript.this.dati[0] = (String) returnString.subSequence(
								returnString.indexOf("<nu>") + 4, returnString.indexOf("</nu>"));
						AddScript.this.dati[1] = (String) returnString.subSequence(
								returnString.indexOf("<np>") + 4, returnString.indexOf("</np>"));
						AddScript.this.dati[2] = (String) returnString.subSequence(
								returnString.indexOf("<nn>") + 4, returnString.indexOf("</nn>"));
						AddScript.this.dati[3] = "0";// returnString.subSequence(returnString.indexOf("<nu>"+4),
						// returnString.indexOf("</nu>"));
						AddScript.this.dati[4] = (String) returnString.subSequence(
								returnString.indexOf("<mc>") + 4, returnString.indexOf("</mc>"));
						if (returnString.indexOf("<mm>") != -1) {
							AddScript.this.dati[5] = (String) returnString
									.subSequence(returnString.indexOf("<mm>") + 4,
											returnString.indexOf("</mm>"));
						} else {
							AddScript.this.dati[5] = (String) returnString.subSequence(
									returnString.indexOf("<mmm>") + 5,
									returnString.indexOf("</mmm>"));
						}
						AddScript.this.db.insertDataService(AddScript.this.config[0],
								Integer.parseInt(AddScript.this.dati[0]),
								Integer.parseInt(AddScript.this.dati[1]),
								Integer.parseInt(AddScript.this.dati[2]),
								Integer.parseInt(AddScript.this.dati[3]),
								Integer.parseInt(AddScript.this.dati[4]),
								Integer.parseInt(AddScript.this.dati[5]), AddScript.this.config[1]);
						// inserire elementi nel database
						// va ancora testata :D
						// rendere invisibili i 2 campi e aggiungere del
						// testo ad un jlabel con su scritto i dati del
						// servizio
						String config = returnString.substring(returnString.indexOf("<t>") + 3,
								returnString.indexOf("</t>"));
						this.text = config;

						AddScript.this.count = 0;
						for (int i = 0; i < 4; i++) {
							if (AddScript.this.dati[i].equals("1")
									|| AddScript.this.dati[i].equals("2")) {
								AddScript.this.count++;
							}
						}
						AddScript.this.parametri = new String[5];
						if (returnString.indexOf("<n1>") != -1) {
							switch (AddScript.this.count) {
							case 4:
								AddScript.this.parametri[4] = (String) returnString.subSequence(
										returnString.indexOf("<n4>") + 4,
										returnString.indexOf("</n4>"));
							case 3:
								AddScript.this.parametri[3] = (String) returnString.subSequence(
										returnString.indexOf("<n3>") + 4,
										returnString.indexOf("</n3>"));
							case 2:
								AddScript.this.parametri[2] = (String) returnString.subSequence(
										returnString.indexOf("<n2>") + 4,
										returnString.indexOf("</n2>"));
							case 1:
								AddScript.this.parametri[1] = (String) returnString.subSequence(
										returnString.indexOf("<n1>") + 4,
										returnString.indexOf("</n1>"));
								AddScript.this.parametri[0] = "Nome Servizio";
							}
						} else {
							AddScript.this.parametri[0] = "Nome Servizio";
							AddScript.this.parametri[1] = "Username:";
							AddScript.this.parametri[2] = "Password";
							AddScript.this.parametri[3] = "Nick";
						}

						switch (AddScript.this.count) {
						case 1:
							AddScript.this.db.insertParameterService(AddScript.this.name.getText()
									.toString(), AddScript.this.parametri[1], null, null, null,
									AddScript.this.url.getText().toString());
							break;
						case 2:
							AddScript.this.db.insertParameterService(AddScript.this.name.getText()
									.toString(), AddScript.this.parametri[1],
									AddScript.this.parametri[2], null, null, AddScript.this.url
											.getText().toString());
							break;
						case 3:
							AddScript.this.db.insertParameterService(AddScript.this.name.getText()
									.toString(), AddScript.this.parametri[1],
									AddScript.this.parametri[2], AddScript.this.parametri[3], null,
									AddScript.this.url.getText().toString());
							break;
						case 4:
							AddScript.this.db.insertParameterService(AddScript.this.name.getText()
									.toString(), AddScript.this.parametri[1],
									AddScript.this.parametri[2], AddScript.this.parametri[3],
									AddScript.this.parametri[4], AddScript.this.url.getText()
											.toString());
							break;
						}
						AddScript.this.db.close();
					} else {
						AddScript.this.errore = 1;
					}

				} else {
					AddScript.this.errore = 1;
				}

				if (AddScript.this.errore == 1) {
					this.text = "Errore Link";
				}

				// sulla pressione del tasto back deve essere caricata
				// l'activity per l'aggiunta dei servizi impostando il
				// servizio appena aggiunto :D

			} catch (ClientProtocolException e) {
				// primo caso
			} catch (IOException e) {
				System.out.println("Errore link inserito");
			} catch (IllegalArgumentException e) {
				this.text = "Errore Link";

			}

			return "Errore Generico";
		}

		@Override
		protected void onProgressUpdate(final String... values) {
			// aggiorno la progress dialog
			// pd.setMessage(values[0]);
		}

		@Override
		protected void onPostExecute(final String result) {
			android.os.Message msg = AddScript.this.effettuato.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putInt("cond", 0);
			bundle.putString("text", this.text);
			// bundle.putString();
			msg.setData(bundle);
			AddScript.this.effettuato.handleMessage(msg);

		}
	}

}
