package it.ciopper90.gojack2;


import it.ciopper90.gojack2.utils.Servizio;

import java.io.FileOutputStream;


import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class SendSMS {
	private String to, body;
	private Context context;
	private Invio invio;
	private Servizio s;
	private String prova;
	private static ProgressDialog pd;
	private Handler listMessage;

	public SendSMS(final Servizio s, final String to, final String body, final Context context,
			final ProgressDialog pd) {
		super();
		this.to = to;
		this.body = body;
		this.context = context;
		this.s = s;
		this.invio = new Invio();
		SendSMS.pd = pd;
	}

	Handler effettuato = new Handler() {

		@Override
		public void handleMessage(final android.os.Message msg) {
			SendSMS.this.invioeffettuato(msg);
		}
	};

	private void invioeffettuato(final android.os.Message msg) {
		android.os.Message notifica = this.listMessage.obtainMessage();
		if (!this.prova.contains("<res>") && !this.prova.contains("<html>")
				&& !this.prova.contains("errore")) {
			Bundle bundle = new Bundle();
			bundle.putInt("cond", 1);
			notifica.setData(bundle);
			this.listMessage.sendMessage(notifica);
		} else {
			if (this.prova.contains("<res>") || this.prova.contains("500")) {
				if (this.prova.contains("<num>")) {
					// andato bene oppure errore
					String[] array = this.prova.split("<num>");
					if ((array.length > 1) && (array[1].charAt(0) == '0')) {
						// invio corretto
						ContentValues values = new ContentValues();
						int inizio = this.to.indexOf("<");
						if (this.to.charAt(this.to.length() - 1) == '>') {
							this.to = this.to.substring(inizio + 1, this.to.length() - 1);
						} else {
							this.to = this.to.substring(inizio + 1, this.to.length());
						}
						if (SendSMS.this.to.contains(" ")) {
							SendSMS.this.to = SendSMS.this.to.replace(" ", "");
						}
						if (SendSMS.this.to.contains(",")) {
							SendSMS.this.to = SendSMS.this.to.replace(",", "");
						}
						values.put("address", this.to);
						values.put("body", this.body);
						values.put("type", 2);
						values.put("read", 1);
						values.put("seen", 1);
						this.context.getContentResolver()
								.insert(Uri.parse("content://sms"), values);
						Toast.makeText(
								this.context,
								"Messaggio Inviato: "
										+ this.prova.substring(this.prova.indexOf("<txt>") + 5,
												this.prova.indexOf("/txt") - 1), Toast.LENGTH_LONG)
								.show();
						Bundle bundle = new Bundle();
						bundle.putInt("cond", 0);
						notifica.setData(bundle);
						this.listMessage.sendMessage(notifica);
					} else {
						// errore
						if (array.length > 1) {
							this.dialog("Errore", array[1].charAt(0),
									this.prova.substring(this.prova.indexOf("<txt>") + 5,
											this.prova.indexOf("/txt") - 1));
						}
					}
				} else {
					if (this.prova.contains("500")) {
						this.dialog("Errore", 2, "Errore 500 nel Server");
					}
					if (this.prova.contains("<res></res>")) {
						// risposta nulla
						this.dialog("Errore", 2, "Nessuna Risposta Dal Server");
					}
				}
			}

			if (this.prova.substring(this.prova.indexOf("<num>") + 5,
					this.prova.indexOf("/num") - 1).equals("0")) {
			}
		}

		SendSMS.pd.cancel();
		SendSMS.pd.dismiss();
	}

	private void dialog(final String string, final int i, final String c) {
		/*
		 * AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
		 * builder.setTitle(string); // title=string; // alert=prova; //
		 * prova=prova.substring(prova.indexOf("<txt>")+5, //
		 * prova.indexOf("</txt>")); builder.setMessage(c);
		 * builder.setCancelable(false); builder.setPositiveButton("Chiudi", new
		 * OnClickListener() { public void onClick(final DialogInterface dialog,
		 * final int id) { dialog.dismiss(); } }); builder.show();
		 */
		android.os.Message notifica = this.listMessage.obtainMessage();
		Bundle bundle = new Bundle();
		bundle.putInt("cond", 2);
		bundle.putString("error", c);
		notifica.setData(bundle);
		this.listMessage.sendMessage(notifica);
	}

	public void go(final Handler listMessage) {
		this.listMessage = listMessage;
		SendSMS.pd.show();
		new Thread() {
			@Override
			public void run() {
				String returnString = null;
				try {
					// invio=new Invio();
					if (SendSMS.this.to.contains(" ")) {
						SendSMS.this.to = SendSMS.this.to.replace(" ", "");
					}
					if (SendSMS.this.to.contains(",")) {
						SendSMS.this.to = SendSMS.this.to.replace(",", "");
					}
					SendSMS.this.prova = SendSMS.this.invio.InvioSms(SendSMS.this.s,
							SendSMS.this.to, SendSMS.this.body);
					if (!SendSMS.this.prova.contains("<res>")
							&& !SendSMS.this.prova.contains("<html>")
							&& !SendSMS.this.prova.contains("errore")) {
						FileOutputStream out = SendSMS.this.context.openFileOutput("captcha.jpg",
								Context.MODE_PRIVATE);
						for (int i = 0; i < SendSMS.this.prova.length(); i++) {
							out.write(SendSMS.this.prova.charAt(i));
						}
						out.flush();
						out.close();
					}

				} catch (Exception e) {
					SendSMS.this.prova = "<res><num>1</num><txt>Errore Rete</txt></res>";// e.printStackTrace();
				}
				android.os.Message msg = SendSMS.this.effettuato.obtainMessage();
				Bundle bundle = new Bundle();
				bundle.putString("to", SendSMS.this.to);
				bundle.putString("body", SendSMS.this.body);
				// bundle.putString();
				msg.setData(bundle);
				SendSMS.this.effettuato.sendMessage(msg);
			}
		}.start();
	}

	public void captcha(final String captcha, final ProgressDialog pd) {
		SendSMS.pd = pd;
		SendSMS.pd.setMessage("Invio Captcha in corso");
		SendSMS.pd.setCancelable(false);
		// messag="Invio in corso";
		SendSMS.pd.show();
		new Thread() {
			@Override
			public void run() {
				try {
					SendSMS.this.prova = SendSMS.this.invio.InvioCaptchaSms(SendSMS.this.s,
							captcha, SendSMS.this.to, SendSMS.this.body);
					if (!SendSMS.this.prova.contains("<res>")
							&& !SendSMS.this.prova.contains("<html>")
							&& !SendSMS.this.prova.contains("errore")) {
						FileOutputStream out = SendSMS.this.context.openFileOutput("captcha.jpg",
								Context.MODE_PRIVATE);
						for (int i = 0; i < SendSMS.this.prova.length(); i++) {
							out.write(SendSMS.this.prova.charAt(i));
						}
						out.flush();
						out.close();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				SendSMS.this.effettuato.sendEmptyMessage(0);
			}
		}.start();
	}

}
