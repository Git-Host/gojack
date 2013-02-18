package it.ciopper90.gojack2.added;

import it.ciopper90.gojack2.R;
import it.ciopper90.gojack2.utils.DatabaseService;
import it.ciopper90.gojack2.utils.DatiServizio;
import it.ciopper90.gojack2.utils.ParametriServizio;
import it.ciopper90.gojack2.utils.Servizio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ViewServizio extends Activity {
	private ListView listView;
	private ArrayList<Servizio> ServiceList;
	private ArrayList<DatiServizio> DataServiceList;
	private ArrayList<ParametriServizio> ParameterServiceList;
	public static DatabaseService db;
	private SharedPreferences prefs;
	private static ProgressDialog pd;

	private String dati[];
	private String config[];
	private String parametri[];
	private static String status;
	private static String url;
	private Context context;
	private String password;
	private EditText text_1;

	// private static AlertDialog alertDialog;

	@SuppressWarnings("static-access")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = this;
		this.setContentView(R.layout.viewservizio);
		this.db = new DatabaseService(this.getApplicationContext());
		this.aggiornalist();
		this.registerForContextMenu(this.listView);
		this.listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(final AdapterView<?> parent, final View v, final int position,
					final long id) {
				ViewServizio.this.listView.setItemChecked(position, true);
				if (position == 0) {
					Intent add = new Intent(ViewServizio.this, AddScript.class);
					ViewServizio.this.startActivity(add);
				} else {
					ViewServizio.this.modify(position);
				}
			}
		});

		this.pd = new ProgressDialog(this.context);
		if (this.status == null) {
			this.status = "";
		}
		if (this.status.equals("passwordservizi")) {
			this.pd.setTitle("Import Servizi");
			this.pd.setMessage("Connecting...");
			this.pd.setCancelable(false);
			pd.show();
			Builder builder = this.createAlertPassword();
			builder.setPositiveButton("Invia!", new OnClickListener() {
				public void onClick(final DialogInterface dialog, final int id) {
					String captcha = ViewServizio.this.text_1.getText().toString();
					dialog.dismiss();
					ViewServizio.this.password = "&p=" + captcha;
					new DownTask().execute(url + "&p=" + captcha);
				}
			});
			builder.show();
			ViewServizio.status = "passwordservizi";

		}
		if (this.status.equals("passwordupdate")) {
			ViewServizio.pd.setTitle("Update Xml");
			ViewServizio.pd.setMessage("Update...");
			ViewServizio.pd.setCancelable(false);
			pd.show();
			Builder builder = this.createAlertPassword();
			builder.setPositiveButton("Invia!", new OnClickListener() {
				public void onClick(final DialogInterface dialog, final int id) {
					String captcha = ViewServizio.this.text_1.getText().toString();
					dialog.dismiss();
					ViewServizio.this.password = "&p=" + captcha;
					new UpdateTask().execute(url + "&p=" + captcha);
				}
			});
			builder.show();
			ViewServizio.status = "passwordupdate";

		}
		if (this.status.equalsIgnoreCase("download") || this.status.equalsIgnoreCase("extract")
				|| this.status.equalsIgnoreCase("import")) {
			this.pd.setTitle("Import Servizi");
			this.pd.setMessage(status);
			this.pd.setCancelable(false);
			pd.show();
		}
		if (this.status.equalsIgnoreCase("update")) {
			this.pd.setTitle("Update Xml");
			this.pd.setMessage("Update...");
			this.pd.setCancelable(false);
			pd.show();
		}
	}

	public void modify(final int position) {
		Intent mod = new Intent(ViewServizio.this, AddServizio.class);
		Servizio s = this.ServiceList.get(position - 1);
		DatiServizio d = null;
		if (!s.getName().equalsIgnoreCase("GoJackMS")) {
			for (int i = 0; i < this.DataServiceList.size(); i++) {
				if (this.DataServiceList.get(i).getUrl().equals(s.getUrl())) {
					d = this.DataServiceList.get(i);
				}
			}
			ParametriServizio p = null;
			for (int i = 0; i < this.ParameterServiceList.size(); i++) {
				if (this.ParameterServiceList.get(i).getUrl().equals(s.getUrl())) {
					p = this.ParameterServiceList.get(i);
				}
			}

			this.dati = new String[6];
			this.dati[0] = Integer.toString(d.getDati()[0]);
			this.dati[1] = Integer.toString(d.getDati()[1]);
			this.dati[2] = Integer.toString(d.getDati()[2]);
			this.dati[3] = Integer.toString(d.getDati()[3]);
			this.dati[4] = Integer.toString(d.getDati()[4]);
			this.dati[5] = Integer.toString(d.getDati()[5]);

			this.config = new String[7];
			this.config[0] = s.getName();
			this.config[1] = s.getUrl();
			this.config[2] = s.getPrimo();
			this.config[3] = s.getSecondo();
			this.config[4] = s.getTerzo();
			this.config[5] = s.getQuarto();
			this.config[6] = s.getFirma();

			this.parametri = p.getParametri();

			mod.putExtra("id", s.getId());
			mod.putExtra("dati", this.dati);
			mod.putExtra("parametri", this.parametri);
			mod.putExtra("config", this.config);
			this.startActivity(mod);
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.viewservizio) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			if (info.position != 0) {
				String[] menuItems = { "Modifica", "Elimina", "Duplica" };
				for (int i = 0; i < menuItems.length; i++) {
					menu.add(Menu.NONE, i, i, menuItems[i]);
				}
			}
		}
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		int scelto = info.position;
		switch (item.getItemId()) {
		case 0:
			this.modify(scelto);
			return true;
		case 1:
			db.open();
			db.DeleteService(this.ServiceList.get(scelto - 1).getId());
			this.aggiornalist();
			db.close();
			return true;
		case 2:
			this.duplica(scelto);
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void duplica(final int scelto) {
		Servizio s = this.ServiceList.get(scelto - 1);
		s.setName(s.getName());
		this.ServiceList.add(s);
		db.open();
		db.insertService(s.getName(), s.getPrimo(), s.getSecondo(), s.getTerzo(), s.getQuarto(),
				s.getUrl(), s.getFirma());
		db.close();
		this.aggiornalist();
	}

	@Override
	public void onResume() {
		super.onResume();
		this.aggiornalist();
	}

	private void aggiornalist() {
		this.ServiceList = new ArrayList<Servizio>();
		WorkServizio w = new WorkServizio(this.getApplicationContext());
		this.ServiceList = w.caricaServizio();
		this.DataServiceList = w.caricaDataServizio();
		this.ParameterServiceList = w.caricaParametriServizio();
		if (this.ServiceList == null) {
			this.ServiceList = new ArrayList<Servizio>();
		}

		// Questa è la lista che rappresenta la sorgente dei dati della
		// listview
		// ogni elemento è una mappa(chiave->valore)
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

		HashMap<String, Object> ServiceMap = new HashMap<String, Object>();// creiamo
																			// una
																			// mappa
																			// di
																			// valori

		ServiceMap.put("name", "Aggiungi Servizio"); // per la chiave image,
														// inseriamo la risorsa
														// dell immagine
		ServiceMap.put("user", "");
		data.add(ServiceMap); // aggiungiamo la mappa di valori alla sorgente
								// dati

		for (int i = 0; i < this.ServiceList.size(); i++) {
			Servizio s = this.ServiceList.get(i);// per ogni persona all'inteno
													// della ditta

			ServiceMap = new HashMap<String, Object>();// creiamo una mappa di
														// valori

			ServiceMap.put("name", s.getName()); // per la chiave image,
													// inseriamo la risorsa dell
													// immagine
			// ServiceMap.put("user", s.getUser());
			data.add(ServiceMap); // aggiungiamo la mappa di valori alla
									// sorgente dati
		}

		String[] from = { "name", "user" }; // dai valori contenuti in queste
											// chiavi
		int[] to = { R.id.Name, R.id.User };// agli id delle view

		// costruzione dell adapter
		SimpleAdapter adapter = new SimpleAdapter(this.getApplicationContext(), data,// sorgente
																						// dati
				R.layout.servizio, // layout contenente gli id di "to"
				from, to);

		// utilizzo dell'adapter
		((ListView) this.findViewById(R.id.viewservizio)).setAdapter(adapter);
		this.listView = (ListView) this.findViewById(R.id.viewservizio);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			ViewServizio.status = "Import Servizi";
			this.prefs = this.getSharedPreferences("Setting", Context.MODE_PRIVATE);
			String textData = this.prefs.getString("url", "");
			String url_1 = "";
			if (textData.equals("")) {
				Toast.makeText(this.getApplicationContext(),
						"Inserire URL al file gojack.php come URL preferito", Toast.LENGTH_SHORT)
						.show();
			} else {
				if (textData.indexOf("?") == -1) {
					url_1 = textData + "?aggserver=1";// result =
														// Invio.aggiorna(textData
														// + "?aggserver=1");
				} else {
					textData = (String) textData.subSequence(0, textData.indexOf("?"));
					Log.d("url", textData);
					// result = Invio.aggiorna(textData + "?aggserver=1");
					url_1 = textData + "?aggserver=1";
				}
				// Toast.makeText(this.getApplicationContext(), result,
				// Toast.LENGTH_LONG).show();
				ViewServizio.url = url_1;
				ViewServizio.pd.setTitle("Update Xml");
				ViewServizio.pd.setMessage("Update...");
				ViewServizio.pd.setCancelable(false);

				Builder builder = this.createAlertPassword();
				builder.setPositiveButton("Invia!", new OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						String captcha = ViewServizio.this.text_1.getText().toString();
						dialog.dismiss();
						pd.show();
						ViewServizio.this.password = "&p=" + captcha;
						new UpdateTask().execute(url + "&p=" + captcha);
					}
				});
				builder.show();
				ViewServizio.status = "passwordupdate";

			}
			return true;
		case 2:
			// byte[] a = null;
			try {
				ViewServizio.status = "Import Servizi";
				// creo e avvio asynctask
				this.prefs = this.getSharedPreferences("Setting", Context.MODE_PRIVATE);
				String text = this.prefs.getString("url", "");
				if (text.equals("")) {
					Toast.makeText(this.getApplicationContext(),
							"Inserire URL al file gojack.php come URL preferito",
							Toast.LENGTH_SHORT).show();
					// ViewServizio.pd.dismiss();
				} else {
					if (text.indexOf("?") == -1) {
						// a=Invio.scarica(text+"?sincweb=1");
						text = text + "?sincweb=1";
						// res=Invio.scarica(text+"?sincweb=1");
					} else {
						text = (String) text.subSequence(0, text.indexOf("?"));
						Log.d("url", text);
						text = text + "?sincweb=1";
						// a=Invio.scarica(text+"?sincweb=1");
						// res=Invio.scarica(text+"?sincweb=1");
					}
					ViewServizio.url = text;
					ViewServizio.pd.setTitle("Import Servizi");
					ViewServizio.pd.setMessage("Connecting...");
					ViewServizio.pd.setCancelable(false);
					// pd.show();
					Builder builder = this.createAlertPassword();
					builder.setPositiveButton("Invia!", new OnClickListener() {
						public void onClick(final DialogInterface dialog, final int id) {
							String captcha = ViewServizio.this.text_1.getText().toString();
							dialog.dismiss();
							pd.show();
							ViewServizio.this.password = "&p=" + captcha;
							new DownTask().execute(url + "&p=" + captcha);
						}
					});
					builder.show();
					ViewServizio.status = "passwordservizi";
				}
			} catch (Exception e) {
				// Log.d("ciao", "Errore gzip");
				// Toast.makeText(getApplicationContext(), a.toString(),
				// Toast.LENGTH_LONG).show();
				Toast.makeText(this.getApplicationContext(), "Aggiornare GoJack.php",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private Builder createAlertPassword() {
		AlertDialog.Builder builder;
		LayoutInflater inflater = (LayoutInflater) ViewServizio.this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog,
				(ViewGroup) this.findViewById(R.id.layout_root));
		this.text_1 = (EditText) layout.findViewById(R.id.text);
		CheckBox cb = (CheckBox) layout.findViewById(R.id.checkBox1);
		cb.setVisibility(View.GONE);
		builder = new AlertDialog.Builder(ViewServizio.this);
		builder.setView(layout);
		builder.setTitle("Password Web");
		builder.setCancelable(true);
		builder.setNegativeButton("Annulla", new OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.dismiss();
				status = "";
			}
		});
		return builder;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		int order = Menu.FIRST;
		int GROUPB = 1;
		menu.add(GROUPB, order, order++,
				this.getResources().getString(R.string.serverpersonaleupdate));
		menu.add(GROUPB, order, order++, this.getResources().getString(R.string.downloadservice));
		return true;
	}

	public void not(final String a) {
		this.aggiornalist();
		ViewServizio.status = "";
		Toast.makeText(this.context, a, Toast.LENGTH_LONG).show();
	}

	private class DownTask extends AsyncTask<String, String, String> {

		private String text;

		@Override
		protected String doInBackground(final String... params) {
			this.text = params[0];
			// aggiorno la progress dialog
			this.publishProgress("Download");
			InputStream is = null;
			try {
				byte[] a = Invio.scarica(this.text);
				this.publishProgress("Extract");
				if (a != null) {
					is = new ByteArrayInputStream(a);
					GZIPInputStream ginstream;

					ginstream = new GZIPInputStream(is);
					String c = "";
					int len;

					while ((len = ginstream.read()) != -1) {
						c = c.concat("" + (char) len);
					}
					len = 0;
					this.publishProgress("Import");
					int res = this.parseService(c);
					// this.aggiornalist();
					if (res == 0) {
						return "Servizi Inseriti Correttamente";
					} else {
						return "Errore File GoJack.php";
						// Toast.makeText(getApplicationContext(),
						// "Servizi Inseriti Correttamente",
						// Toast.LENGTH_LONG).show();
					}
				} else {
					return "Errore Rete";
					// Toast.makeText(getApplicationContext(), "Errore Rete",
					// Toast.LENGTH_LONG).show();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				try {
					int len = 0;
					String d = "";
					while ((len = is.read()) != -1) {
						d = d.concat("" + (char) len);
					}
					if (d.contains("<txt>") && d.contains("</txt>")) {
						return d.substring(d.indexOf("<txt>") + 5, d.indexOf("/txt") - 1);
					} else {
						return "Errore URL";
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
				}

			}
			return "Errore Generico";
		}

		@Override
		protected void onProgressUpdate(final String... values) {
			// aggiorno la progress dialog
			ViewServizio.pd.setMessage(values[0]);
			status = values[0];
		}

		@Override
		protected void onPostExecute(final String result) {
			// chiudo la progress dialog
			ViewServizio.pd.dismiss();
			// operazioni di chiusura
			ViewServizio.this.not(result);

		}

		private int parseService(final String c) {
			db.open();
			String[] s = c.split("<account ");
			/*
			 * for(int i=1;i<s.length;i++){ s[i]=s[i].substring(0,
			 * s[i].indexOf("/account")+9); }
			 */
			for (int i = 1; i < s.length; i++) {
				int dist = s[i].indexOf("/data") - 1 - (s[i].indexOf("<data>") + 6);
				if (dist <= 10) {
					Log.d("servi", "errore");
				} else {
					// Log.d("servi","ok");
					if (this.text.indexOf("?") == -1) {
						this.text = this.text + "?servizio=";
					} else {
						this.text = (String) this.text.subSequence(0, this.text.indexOf("?"));
						this.text = this.text + "?servizio=";
					}
				}
			}
			for (int n = 1; n < s.length; n++) {
				// richiesta parametri x service
				// HttpClient httpclient = new DefaultHttpClient();
				int primo = s[n].indexOf("service=\"") + 9;
				int secondo = s[n].indexOf("\"", primo);
				String name = s[n].substring(primo, secondo);
				primo = s[n].indexOf("name=\"") + 6;
				secondo = s[n].indexOf("\"", primo);
				String nameservice = s[n].substring(primo, secondo);
				String url = this.text + name + ViewServizio.this.password;
				// response = httpclient.execute(httppost);
				String returnString = null;
				try {
					returnString = s[n].substring(s[n].indexOf("<res>"), s[n].indexOf("/res") + 5);
				} catch (Exception e) {
					return 1;
				}
				if (returnString.indexOf("<config>") != -1) {
					if (returnString.indexOf("<res>") != -1) {
						// elaborare returnString per poi salvare il dato nel
						// database
						ViewServizio.this.dati = new String[6];
						ViewServizio.this.dati[0] = (String) returnString.subSequence(
								returnString.indexOf("<nu>") + 4, returnString.indexOf("</nu>"));
						ViewServizio.this.dati[1] = (String) returnString.subSequence(
								returnString.indexOf("<np>") + 4, returnString.indexOf("</np>"));
						ViewServizio.this.dati[2] = (String) returnString.subSequence(
								returnString.indexOf("<nn>") + 4, returnString.indexOf("</nn>"));
						ViewServizio.this.dati[3] = "0";// returnString.subSequence(returnString.indexOf("<nu>"+4),
														// returnString.indexOf("</nu>"));
						ViewServizio.this.dati[4] = (String) returnString.subSequence(
								returnString.indexOf("<mc>") + 4, returnString.indexOf("</mc>"));
						if (returnString.indexOf("<mm>") != -1) {
							ViewServizio.this.dati[5] = (String) returnString
									.subSequence(returnString.indexOf("<mm>") + 4,
											returnString.indexOf("</mm>"));
						} else {
							ViewServizio.this.dati[5] = (String) returnString.subSequence(
									returnString.indexOf("<mmm>") + 5,
									returnString.indexOf("</mmm>"));
						}
						db.insertDataService(nameservice,
								Integer.parseInt(ViewServizio.this.dati[0]),
								Integer.parseInt(ViewServizio.this.dati[1]),
								Integer.parseInt(ViewServizio.this.dati[2]),
								Integer.parseInt(ViewServizio.this.dati[3]),
								Integer.parseInt(ViewServizio.this.dati[4]),
								Integer.parseInt(ViewServizio.this.dati[5]), url);
						int count = 0;
						for (int i = 0; i < 4; i++) {
							if (ViewServizio.this.dati[i].equals("1")
									|| ViewServizio.this.dati[i].equals("2")) {
								count++;
							}
						}
						ViewServizio.this.parametri = new String[5];
						if (returnString.indexOf("<n1>") != -1) {
							switch (count) {
							case 4:
								ViewServizio.this.parametri[4] = (String) returnString.subSequence(
										returnString.indexOf("<n4>") + 4,
										returnString.indexOf("</n4>"));
							case 3:
								ViewServizio.this.parametri[3] = (String) returnString.subSequence(
										returnString.indexOf("<n3>") + 4,
										returnString.indexOf("</n3>"));
							case 2:
								ViewServizio.this.parametri[2] = (String) returnString.subSequence(
										returnString.indexOf("<n2>") + 4,
										returnString.indexOf("</n2>"));
							case 1:
								ViewServizio.this.parametri[1] = (String) returnString.subSequence(
										returnString.indexOf("<n1>") + 4,
										returnString.indexOf("</n1>"));
								ViewServizio.this.parametri[0] = "Nome Servizio";
							}
						} else {
							ViewServizio.this.parametri[0] = "Nome Servizio";
							ViewServizio.this.parametri[1] = "Username:";
							ViewServizio.this.parametri[2] = "Password";
							ViewServizio.this.parametri[3] = "Nick";
						}
						String[] conf = new String[4];
						for (int k = 0; k < conf.length; k++) {
							conf[k] = "";
						}

						switch (count) {
						case 1:
							if (nameservice.equalsIgnoreCase("gojackms")
									&& (s[n].indexOf("<" + ViewServizio.this.parametri[1] + ">") == -1)) {
								db.insertParameterService(nameservice,
										ViewServizio.this.parametri[1], null, null, null, url);
							} else {
								db.insertParameterService(nameservice,
										ViewServizio.this.parametri[1], null, null, null, url);
								conf[0] = s[n].substring(
										s[n].indexOf("<" + ViewServizio.this.parametri[1] + ">")
												+ 2 + ViewServizio.this.parametri[1].length(),
										s[n].indexOf("/" + ViewServizio.this.parametri[1]) - 1);
							}
							break;
						case 2:
							db.insertParameterService(nameservice, ViewServizio.this.parametri[1],
									ViewServizio.this.parametri[2], null, null, url);
							conf[0] = s[n].substring(
									s[n].indexOf("<" + ViewServizio.this.parametri[1] + ">") + 2
											+ ViewServizio.this.parametri[1].length(),
									s[n].indexOf("/" + ViewServizio.this.parametri[1]) - 1);
							conf[1] = s[n].substring(
									s[n].indexOf("<" + ViewServizio.this.parametri[2] + ">") + 2
											+ ViewServizio.this.parametri[2].length(),
									s[n].indexOf("/" + ViewServizio.this.parametri[2]) - 1);
							break;
						case 3:
							db.insertParameterService(nameservice, ViewServizio.this.parametri[1],
									ViewServizio.this.parametri[2], ViewServizio.this.parametri[3],
									null, url);
							conf[0] = s[n].substring(
									s[n].indexOf("<" + ViewServizio.this.parametri[1] + ">") + 2
											+ ViewServizio.this.parametri[1].length(),
									s[n].indexOf("/" + ViewServizio.this.parametri[1]) - 1);
							conf[1] = s[n].substring(
									s[n].indexOf("<" + ViewServizio.this.parametri[2] + ">") + 2
											+ ViewServizio.this.parametri[2].length(),
									s[n].indexOf("/" + ViewServizio.this.parametri[2]) - 1);
							conf[2] = s[n].substring(
									s[n].indexOf("<" + ViewServizio.this.parametri[3] + ">") + 2
											+ ViewServizio.this.parametri[3].length(),
									s[n].indexOf("/" + ViewServizio.this.parametri[3]) - 1);
							break;
						case 4:
							db.insertParameterService(nameservice, ViewServizio.this.parametri[1],
									ViewServizio.this.parametri[2], ViewServizio.this.parametri[3],
									ViewServizio.this.parametri[4], url);
							conf[0] = s[n].substring(
									s[n].indexOf("<" + ViewServizio.this.parametri[1] + ">") + 2
											+ ViewServizio.this.parametri[1].length(),
									s[n].indexOf("/" + ViewServizio.this.parametri[1]) - 1);
							conf[1] = s[n].substring(
									s[n].indexOf("<" + ViewServizio.this.parametri[2] + ">") + 2
											+ ViewServizio.this.parametri[2].length(),
									s[n].indexOf("/" + ViewServizio.this.parametri[2]) - 1);
							conf[2] = s[n].substring(
									s[n].indexOf("<" + ViewServizio.this.parametri[3] + ">") + 2
											+ ViewServizio.this.parametri[3].length(),
									s[n].indexOf("/" + ViewServizio.this.parametri[3]) - 1);
							conf[3] = s[n].substring(
									s[n].indexOf("<" + ViewServizio.this.parametri[4] + ">") + 2
											+ ViewServizio.this.parametri[4].length(),
									s[n].indexOf("/" + ViewServizio.this.parametri[4]) - 1);
							break;
						}
						// prima di inserire i dati bisogna controllare se ci
						// sono gia nel db
						Cursor curs = db.fetchService(nameservice);
						if (!(curs.moveToNext())) {
							db.insertService(nameservice, conf[0], conf[1], conf[2], conf[3], url,
									"");
						}
						curs.close();
					}
				}
			}
			db.close();
			return 0;
		}

	}

	private class UpdateTask extends AsyncTask<String, String, String> {

		private String text;

		@Override
		protected String doInBackground(final String... params) {
			this.publishProgress("update");
			this.text = params[0];
			return Invio.aggiorna(this.text);
		}

		@Override
		protected void onProgressUpdate(final String... values) {
			status = values[0];
		}

		@Override
		protected void onPostExecute(final String result) {
			ViewServizio.pd.dismiss();
			// operazioni di chiusura
			ViewServizio.this.not(result);

		}

	}

}
