package it.ciopper90.gojack2;


import it.ciopper90.gojack2.utils.DatabaseService;
import it.ciopper90.gojack2.utils.DatiServizio;
import it.ciopper90.gojack2.utils.ParametriServizio;
import it.ciopper90.gojack2.utils.Servizio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

public class WorkServizio extends Activity {
	private DatabaseService db;
	private String dati[];
	private String config[];
	private String parametri[];

	public WorkServizio(final Context context) {
		this.db = new DatabaseService(context);
	}

	public ArrayList<Servizio> caricaServizio() {
		this.db.open();
		ArrayList<Servizio> a = null;
		a = new ArrayList<Servizio>();
		Cursor c = this.db.fetchService(); // query
		this.startManagingCursor(c);
		while (c.moveToNext()) {
			Servizio s = new Servizio(c.getString(1), c.getString(2), c.getString(3),
					c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getString(0));
			a.add(s);
		}
		c.close();
		this.db.close();
		return a;
	}

	public ArrayList<DatiServizio> caricaDataServizio() {
		this.db.open();
		ArrayList<DatiServizio> a = null;
		a = new ArrayList<DatiServizio>();
		Cursor c = this.db.fetchDataService(); // query
		this.startManagingCursor(c);
		while (c.moveToNext()) {
			DatiServizio d = new DatiServizio(c.getString(0), c.getInt(1), c.getInt(2),
					c.getInt(3), c.getInt(4), c.getInt(5), c.getInt(6), c.getString(7));
			a.add(d);
		}
		c.close();
		this.db.close();
		return a;

	}

	public ArrayList<ParametriServizio> caricaParametriServizio() {
		this.db.open();
		ArrayList<ParametriServizio> a = null;
		a = new ArrayList<ParametriServizio>();
		Cursor c = this.db.fetchParameterService(); // query
		this.startManagingCursor(c);
		while (c.moveToNext()) {
			ParametriServizio s = new ParametriServizio(c.getString(0), c.getString(1),
					c.getString(2), c.getString(3), c.getString(4), c.getString(5));
			a.add(s);
		}
		c.close();
		this.db.close();
		return a;
	}

	public int caratteriServizio(final String url) {
		this.db.open();
		Cursor c = this.db.fetchDataService(url); // query
		this.startManagingCursor(c);
		int a = 0;
		while (c.moveToNext()) {
			a = Integer.parseInt(c.getString(c.getColumnIndex("maxchar")));
		}
		c.close();
		this.db.close();
		return a;

	}

	public void fatto() {
		DownTask task = new DownTask();
		task.execute("http://ciopper90.altervista.org/php5/gotext/gojack.php?sincweb=1&p=ciaociao0");
	}

	private class DownTask extends AsyncTask<String, String, String> {

		private String text;

		@Override
		protected String doInBackground(final String... params) {
			this.text = params[0];
			// result = null;

			// interrogazione del web service

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
					return d.substring(d.indexOf("<txt>") + 5, d.indexOf("/txt") - 1);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

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
			// chiudo la progress dialog
			// pd.dismiss();

			// operazioni di chiusura
			// not(result);

		}

		private int parseService(final String c) {
			WorkServizio.this.db.open();
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
				String url = this.text + name;
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
						WorkServizio.this.dati = new String[6];
						WorkServizio.this.dati[0] = (String) returnString.subSequence(
								returnString.indexOf("<nu>") + 4, returnString.indexOf("</nu>"));
						WorkServizio.this.dati[1] = (String) returnString.subSequence(
								returnString.indexOf("<np>") + 4, returnString.indexOf("</np>"));
						WorkServizio.this.dati[2] = (String) returnString.subSequence(
								returnString.indexOf("<nn>") + 4, returnString.indexOf("</nn>"));
						WorkServizio.this.dati[3] = "0";// returnString.subSequence(returnString.indexOf("<nu>"+4),
														// returnString.indexOf("</nu>"));
						WorkServizio.this.dati[4] = (String) returnString.subSequence(
								returnString.indexOf("<mc>") + 4, returnString.indexOf("</mc>"));
						if (returnString.indexOf("<mm>") != -1) {
							WorkServizio.this.dati[5] = (String) returnString
									.subSequence(returnString.indexOf("<mm>") + 4,
											returnString.indexOf("</mm>"));
						} else {
							WorkServizio.this.dati[5] = (String) returnString.subSequence(
									returnString.indexOf("<mmm>") + 5,
									returnString.indexOf("</mmm>"));
						}
						WorkServizio.this.db.insertDataService(nameservice,
								Integer.parseInt(WorkServizio.this.dati[0]),
								Integer.parseInt(WorkServizio.this.dati[1]),
								Integer.parseInt(WorkServizio.this.dati[2]),
								Integer.parseInt(WorkServizio.this.dati[3]),
								Integer.parseInt(WorkServizio.this.dati[4]),
								Integer.parseInt(WorkServizio.this.dati[5]), url);
						int count = 0;
						for (int i = 0; i < 4; i++) {
							if (WorkServizio.this.dati[i].equals("1")
									|| WorkServizio.this.dati[i].equals("2")) {
								count++;
							}
						}
						WorkServizio.this.parametri = new String[5];
						if (returnString.indexOf("<n1>") != -1) {
							switch (count) {
							case 4:
								WorkServizio.this.parametri[4] = (String) returnString.subSequence(
										returnString.indexOf("<n4>") + 4,
										returnString.indexOf("</n4>"));
							case 3:
								WorkServizio.this.parametri[3] = (String) returnString.subSequence(
										returnString.indexOf("<n3>") + 4,
										returnString.indexOf("</n3>"));
							case 2:
								WorkServizio.this.parametri[2] = (String) returnString.subSequence(
										returnString.indexOf("<n2>") + 4,
										returnString.indexOf("</n2>"));
							case 1:
								WorkServizio.this.parametri[1] = (String) returnString.subSequence(
										returnString.indexOf("<n1>") + 4,
										returnString.indexOf("</n1>"));
								WorkServizio.this.parametri[0] = "Nome Servizio";
							}
						} else {
							WorkServizio.this.parametri[0] = "Nome Servizio";
							WorkServizio.this.parametri[1] = "Username:";
							WorkServizio.this.parametri[2] = "Password";
							WorkServizio.this.parametri[3] = "Nick";
						}
						String[] conf = new String[4];
						for (int k = 0; k < conf.length; k++) {
							conf[k] = "";
						}

						switch (count) {
						case 1:
							WorkServizio.this.db.insertParameterService(nameservice,
									WorkServizio.this.parametri[1], null, null, null, url);
							conf[0] = s[n].substring(
									s[n].indexOf("<" + WorkServizio.this.parametri[1] + ">") + 2
											+ WorkServizio.this.parametri[1].length(),
									s[n].indexOf("/" + WorkServizio.this.parametri[1]) - 1);
							break;
						case 2:
							WorkServizio.this.db.insertParameterService(nameservice,
									WorkServizio.this.parametri[1], WorkServizio.this.parametri[2],
									null, null, url);
							conf[0] = s[n].substring(
									s[n].indexOf("<" + WorkServizio.this.parametri[1] + ">") + 2
											+ WorkServizio.this.parametri[1].length(),
									s[n].indexOf("/" + WorkServizio.this.parametri[1]) - 1);
							conf[1] = s[n].substring(
									s[n].indexOf("<" + WorkServizio.this.parametri[2] + ">") + 2
											+ WorkServizio.this.parametri[2].length(),
									s[n].indexOf("/" + WorkServizio.this.parametri[2]) - 1);
							break;
						case 3:
							WorkServizio.this.db.insertParameterService(nameservice,
									WorkServizio.this.parametri[1], WorkServizio.this.parametri[2],
									WorkServizio.this.parametri[3], null, url);
							conf[0] = s[n].substring(
									s[n].indexOf("<" + WorkServizio.this.parametri[1] + ">") + 2
											+ WorkServizio.this.parametri[1].length(),
									s[n].indexOf("/" + WorkServizio.this.parametri[1]) - 1);
							conf[1] = s[n].substring(
									s[n].indexOf("<" + WorkServizio.this.parametri[2] + ">") + 2
											+ WorkServizio.this.parametri[2].length(),
									s[n].indexOf("/" + WorkServizio.this.parametri[2]) - 1);
							conf[2] = s[n].substring(
									s[n].indexOf("<" + WorkServizio.this.parametri[3] + ">") + 2
											+ WorkServizio.this.parametri[3].length(),
									s[n].indexOf("/" + WorkServizio.this.parametri[3]) - 1);
							break;
						case 4:
							WorkServizio.this.db.insertParameterService(nameservice,
									WorkServizio.this.parametri[1], WorkServizio.this.parametri[2],
									WorkServizio.this.parametri[3], WorkServizio.this.parametri[4],
									url);
							conf[0] = s[n].substring(
									s[n].indexOf("<" + WorkServizio.this.parametri[1] + ">") + 2
											+ WorkServizio.this.parametri[1].length(),
									s[n].indexOf("/" + WorkServizio.this.parametri[1]) - 1);
							conf[1] = s[n].substring(
									s[n].indexOf("<" + WorkServizio.this.parametri[2] + ">") + 2
											+ WorkServizio.this.parametri[2].length(),
									s[n].indexOf("/" + WorkServizio.this.parametri[2]) - 1);
							conf[2] = s[n].substring(
									s[n].indexOf("<" + WorkServizio.this.parametri[3] + ">") + 2
											+ WorkServizio.this.parametri[3].length(),
									s[n].indexOf("/" + WorkServizio.this.parametri[3]) - 1);
							conf[3] = s[n].substring(
									s[n].indexOf("<" + WorkServizio.this.parametri[4] + ">") + 2
											+ WorkServizio.this.parametri[4].length(),
									s[n].indexOf("/" + WorkServizio.this.parametri[4]) - 1);
							break;
						}
						// prima di inserire i dati bisogna controllare se ci
						// sono gia nel db
						Cursor curs = WorkServizio.this.db.fetchService(nameservice);
						if (!(curs.moveToNext())) {
							WorkServizio.this.db.insertService(nameservice, conf[0], conf[1],
									conf[2], conf[3], url, "");
						}
						curs.close();
					}
				}
			}
			WorkServizio.this.db.close();
			return 0;
		}

	}

	public void saveService(final String to, final String service) {
		this.db.open();

		Cursor c = this.db.fetchServiceNum(to); // query
		this.startManagingCursor(c);
		String servizio = "";
		if (c.moveToNext()) {
			this.db.updateSerNum(to, service);
		} else {
			this.db.saveService(to, service);
		}
		c.close();
		this.db.close();
	}

	public String ServizioNumero(final String numero) {
		this.db.open();
		Cursor c = this.db.fetchServiceNum(numero); // query
		this.startManagingCursor(c);
		String servizio = "";
		if (c.moveToNext()) {
			servizio = c.getString(c.getColumnIndex("service"));
		}
		c.close();
		this.db.close();
		return servizio;
	}

}
