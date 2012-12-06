/**
 * 
 */
package it.ciopper90.gojack2;


import it.ciopper90.gojack2.R;
import it.ciopper90.gojack2.utils.Servizio;

import java.io.FileNotFoundException;
import java.net.URLDecoder;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ub0r.android.lib.Log;
import de.ub0r.android.lib.apis.ContactsWrapper;

/**
 * Class sending messages via standard Messaging interface.
 * 
 * @author flx
 */
public final class SenderActivity extends SherlockActivity implements OnClickListener {
	/** Tag for output. */
	private static final String TAG = "send";

	/** {@link Uri} for saving messages. */
	private static final Uri URI_SMS = Uri.parse("content://sms");
	/** {@link Uri} for saving sent messages. */
	public static final Uri URI_SENT = Uri.parse("content://sms/sent");
	/** Projection for getting the id. */
	private static final String[] PROJECTION_ID = new String[] { BaseColumns._ID };
	/** SMS DB: address. */
	private static final String ADDRESS = "address";
	/** SMS DB: read. */
	private static final String READ = "read";
	/** SMS DB: type. */
	public static final String TYPE = "type";
	/** SMS DB: body. */
	private static final String BODY = "body";
	/** SMS DB: date. */
	private static final String DATE = "date";

	private ProgressDialog pd = null;

	/** Message set action. */
	public static final String MESSAGE_SENT_ACTION = "com.android.mms.transaction.MESSAGE_SENT";

	/** Hold recipient and text. */
	private String to, text;
	/** {@link ClipboardManager}. */
	@SuppressWarnings("deprecation")
	private ClipboardManager cbmgr;
	private String result;
	private Invio invio;
	private String prova;
	private WorkServizio ws;
	private static ArrayList<Servizio> service;

	private Spinner spinner;
	private SpinnerAdapter spinneradapter;
	private SendSMS send;
	private String alert;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.handleIntent(this.getIntent());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		this.handleIntent(intent);
	}

	/**
	 * Handle {@link Intent}.
	 * 
	 * @param intent
	 *            {@link Intent}
	 */
	@SuppressWarnings("deprecation")
	private void handleIntent(final Intent intent) {
		if (this.parseIntent(intent)) {
			this.setTheme(android.R.style.Theme_Translucent_NoTitleBar);
			this.send();
			this.finish();
		} else {
			this.setTheme(PreferencesActivity.getTheme(this));
			SMSdroid.fixActionBarBackground(this.getSupportActionBar(), this.getResources(),
					R.drawable.bg_striped, R.drawable.bg_striped_img);
			this.setContentView(R.layout.sender);
			this.findViewById(R.id.text_paste).setOnClickListener(this);
			final EditText et = (EditText) this.findViewById(R.id.text);
			et.addTextChangedListener(new MyTextWatcher(this, (TextView) this
					.findViewById(R.id.text_paste), (TextView) this.findViewById(R.id.text_)));
			et.setText(this.text);
			final MultiAutoCompleteTextView mtv = (MultiAutoCompleteTextView) this
					.findViewById(R.id.to);
			mtv.setAdapter(new MobilePhoneAdapter(this));
			mtv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
			mtv.setText(this.to);
			if (!TextUtils.isEmpty(this.to)) {
				this.to = this.to.trim();
				if (this.to.endsWith(",")) {
					this.to = this.to.substring(0, this.to.length() - 1).trim();
				}
				if (this.to.indexOf('<') < 0) {
					// try to fetch recipient's name from phone book
					String n = ContactsWrapper.getInstance().getNameForNumber(
							this.getContentResolver(), this.to);
					if (n != null) {
						this.to = n + " <" + this.to + ">, ";
					}
				}
				mtv.setText(this.to);
				et.requestFocus();
			} else {
				mtv.requestFocus();
			}
			this.cbmgr = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
		}

		this.getSupportActionBar().setDisplayShowHomeEnabled(false);

		// this.getSupportActionBar().setIcon(R.drawable.ic_menu_star);
		// this.getSupportActionBar().
		this.ws = new WorkServizio(this.getApplicationContext());

		service = this.ws.caricaServizio();
		this.spinner = new Spinner(this);
		ArrayList<String> lista = new ArrayList<String>();
		for (int i = 0; i < service.size(); i++) {
			lista.add(service.get(i).getName());
		}
		// lista.add("a");
		// lista.add("b");
		// lista.add("c");

		this.spinneradapter = new ArrayAdapter<String>(this, R.layout.textview, lista);
		this.spinner.setAdapter(this.spinneradapter);
		this.getSupportActionBar().setCustomView(this.spinner);
		this.getSupportActionBar().setDisplayShowCustomEnabled(true);

	}

	/**
	 * Parse data pushed by {@link Intent}.
	 * 
	 * @param intent
	 *            {@link Intent}
	 * @return true if message is ready to send
	 */
	private boolean parseIntent(final Intent intent) {
		Log.d(TAG, "parseIntent(" + intent + ")");
		if (intent == null) {
			return false;
		}
		Log.d(TAG, "got action: " + intent.getAction());

		this.to = null;
		String u = intent.getDataString();
		try {
			if (!TextUtils.isEmpty(u) && u.contains(":")) {
				String t = u.split(":")[1];
				if (t.startsWith("+")) {
					this.to = "+" + URLDecoder.decode(t.substring(1));
				} else {
					this.to = URLDecoder.decode(t);
				}
			}
		} catch (IndexOutOfBoundsException e) {
			Log.w(TAG, "could not split at :", e);
		}
		u = null;

		CharSequence cstext = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
		this.text = null;
		if (cstext != null) {
			this.text = cstext.toString();
			cstext = null;
		}
		if (TextUtils.isEmpty(this.text)) {
			Log.i(TAG, "text missing");
			return false;
		}
		if (TextUtils.isEmpty(this.to)) {
			Log.i(TAG, "recipient missing");
			return false;
		}

		return true;
	}

	/**
	 * Send a message.
	 * 
	 * @return true, if message was sent
	 */
	private boolean send() {
		if (TextUtils.isEmpty(this.to) || TextUtils.isEmpty(this.text)) {
			return false;
		}
		for (String r : this.to.split(",")) {
			r = MobilePhoneAdapter.cleanRecipient(r);
			if (TextUtils.isEmpty(r)) {
				Log.w(TAG, "skip empty recipipient: " + r);
				continue;
			}
			this.send(true, true);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.text_paste:
			final CharSequence s = this.cbmgr.getText();
			((EditText) this.findViewById(R.id.text)).setText(s);
			return;
		default:
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getSupportMenuInflater().inflate(R.menu.sender, menu);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in Action Bar clicked; go home
			Intent intent = new Intent(this, ConversationListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			return true;
		case R.id.item_send:
			EditText et = (EditText) this.findViewById(R.id.text);
			this.text = et.getText().toString();
			et = (MultiAutoCompleteTextView) this.findViewById(R.id.to);
			this.to = et.getText().toString();
			if (this.send()) {
				// this.finish();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	Handler effettuato = new Handler() {

		@Override
		public void handleMessage(final android.os.Message msg) {
			Bundle bundle = msg.getData();
			int cond = bundle.getInt("cond");
			switch (cond) {
			case 0:
				SenderActivity.this.ok();
				break;
			case 1:
				SenderActivity.this.captcha();
				break;
			case 2:
				SenderActivity.this.error(bundle.getString("error"));
				break;
			}
		}
	};

	/**
	 * Answer/send message.
	 * 
	 * @param autosend
	 *            enable autosend
	 * @param showChooser
	 *            show chooser
	 */
	private void send(final boolean autosend, final boolean showChooser) {
		Servizio s = null;
		this.pd = new ProgressDialog(SenderActivity.this);
		// String returnString = null;
		for (int i = 0; i < service.size(); i++) {
			if (service.get(i).getName() == this.spinner.getSelectedItem().toString()) {
				s = service.get(i);
			}
		}
		this.pd.setMessage("Invio in corso");
		this.pd.setCancelable(false);
		// messag="Invio in corso";
		// this.pd.show();
		EditText et = (EditText) this.findViewById(R.id.text);
		this.text = et.getText().toString();
		et = (MultiAutoCompleteTextView) this.findViewById(R.id.to);
		this.to = et.getText().toString();

		// Servizio s = new Servizio("free", "cADR8jqr80ku$Fw@fXMY", "", "", "",
		// "http://ciopper90.altervista.org/php5/gofree/gojack.php", "", "");

		this.send = new SendSMS(s, this.to, this.text, this.getApplicationContext(), this.pd);
		this.send.go(this.effettuato);
		// this.ws.fatto();
		// this.pd.cancel();
		// this.pd.dismiss();
		// Toast.makeText(this.getApplicationContext(), ret,
		// Toast.LENGTH_LONG).show();

		/*
		 * final Intent i = this.buildIntent(autosend, false);
		 * this.startActivity(i);
		 */
		// PreferenceManager
		// .getDefaultSharedPreferences(this)
		// .edit()
		// .putString(PreferencesActivity.PREFS_BACKUPLASTTEXT,
		// this.etText.getText().toString()).commit();
		// this.etText.setText("");

	}

	protected void error(final String string) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Errore");
		// title="errore";
		// alert=prova;
		// prova=prova.substring(prova.indexOf("<txt>")+5,
		// prova.indexOf("</txt>"));
		builder.setMessage(string);
		builder.setCancelable(false);
		builder.setNegativeButton("Chiudi", new AlertDialog.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.dismiss();
				// SenderActivity.this.alert = "";
			}
		});
		builder.show();

	}

	protected void captcha() {
		AlertDialog alertDialog;
		AlertDialog.Builder builder;
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog,
				(ViewGroup) this.findViewById(R.id.layout_root));
		ImageView image = (ImageView) layout.findViewById(R.id.image);
		Bitmap bMap = null;
		try {
			bMap = BitmapFactory.decodeStream(this.openFileInput("captcha.jpg"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final EditText text = (EditText) layout.findViewById(R.id.text);
		DisplayMetrics metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		Log.d("misure",
				"image " + bMap.getHeight() + ":" + bMap.getWidth() + " spazio "
						+ image.getHeight() + " " + image.getWidth());
		double rapporto = bMap.getWidth() / (double) bMap.getHeight();
		bMap = Bitmap.createScaledBitmap(bMap, (int) (metrics.widthPixels * 0.9),
				(int) (((int) (metrics.widthPixels * 0.9)) / rapporto), true);
		image.setImageBitmap(bMap);
		builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setCancelable(false);
		this.pd = new ProgressDialog(this);
		builder.setPositiveButton("Invia!", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				String captcha = text.getText().toString();
				SenderActivity.this.send.captcha(captcha, SenderActivity.this.pd);
				dialog.dismiss();
				SenderActivity.this.alert = "";
			}
		});
		builder.setNegativeButton("Annulla!", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.dismiss();
				// annullare realmente l'invio
				SenderActivity.this.alert = "";
			}
		});
		alertDialog = builder.create();
		builder.show();

	}

	public void ok() {
		int inizio = this.to.indexOf("<");
		if (this.to.charAt(this.to.length() - 1) == '>') {
			this.to = this.to.substring(inizio + 1, this.to.length() - 1);
		} else {
			this.to = this.to.substring(inizio + 1, this.to.length());
		}
		if (this.to.contains(" ")) {
			this.to = this.to.replace(" ", "");
		}
		if (this.to.contains(",")) {
			this.to = this.to.replace(",", "");
		}
		this.ws.saveService(this.to, (String) this.spinner.getSelectedItem());
		this.finish();
	}

}
