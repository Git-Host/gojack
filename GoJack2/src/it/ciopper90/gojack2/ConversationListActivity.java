/*
 * Copyright (C) 2010-2012 Felix Bechstein
 * 
 * This file is part of SMSdroid.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package it.ciopper90.gojack2;

import it.ciopper90.gojack2.added.Invio;
import it.ciopper90.gojack2.added.Setting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ub0r.android.lib.ChangelogHelper;
import de.ub0r.android.lib.Log;
import de.ub0r.android.lib.Utils;
import de.ub0r.android.lib.apis.Contact;
import de.ub0r.android.lib.apis.ContactsWrapper;

/**
 * Main {@link SherlockActivity} showing conversations.
 * 
 * @author flx
 */
public final class ConversationListActivity extends SherlockActivity implements
		OnItemClickListener, OnItemLongClickListener {
	/** Tag for output. */
	public static final String TAG = "main";
	/** mie variabili */
	private Context context;
	private ProgressDialog pd;
	private SharedPreferences prefs;
	private static String alert;
	private static String url;

	/** ORIG_URI to resolve. */
	static final Uri URI = Uri.parse("content://mms-sms/conversations/");

	/** Number of items. */
	private static final int WHICH_N = 6;
	/** Index in dialog: answer. */
	private static final int WHICH_ANSWER = 0;
	/** Index in dialog: answer. */
	private static final int WHICH_CALL = 1;
	/** Index in dialog: view/add contact. */
	private static final int WHICH_VIEW_CONTACT = 2;
	/** Index in dialog: view. */
	private static final int WHICH_VIEW = 3;
	/** Index in dialog: delete. */
	private static final int WHICH_DELETE = 4;
	/** Index in dialog: mark as spam. */
	private static final int WHICH_MARK_SPAM = 5;

	/** Minimum date. */
	public static final long MIN_DATE = 10000000000L;
	/** Miliseconds per seconds. */
	public static final long MILLIS = 1000L;

	/** Show contact's photo. */
	public static boolean showContactPhoto = false;
	/** Show emoticons in {@link MessageListActivity}. */
	public static boolean showEmoticons = false;

	/** Dialog items shown if an item was long clicked. */
	private String[] longItemClickDialog = null;

	/** Conversations. */
	private ConversationAdapter adapter = null;

	/** {@link Calendar} holding today 00:00. */
	private static final Calendar CAL_DAYAGO = Calendar.getInstance();
	static {
		// Get time for now - 24 hours
		CAL_DAYAGO.add(Calendar.DAY_OF_MONTH, -1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart() {
		super.onStart();
		AsyncHelper.setAdapter(this.adapter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStop() {
		super.onStop();
		AsyncHelper.setAdapter(null);
	}

	/**
	 * Get {@link AbsListView}.
	 * 
	 * @return {@link AbsListView}
	 */
	private AbsListView getListView() {
		return (AbsListView) this.findViewById(android.R.id.list);
	}

	/**
	 * Set {@link ListAdapter} to {@link ListView}.
	 * 
	 * @param la
	 *            ListAdapter
	 */
	private void setListAdapter(final ListAdapter la) {
		AbsListView v = this.getListView();
		if (v instanceof GridView) {
			((GridView) v).setAdapter(la);
		} else if (v instanceof ListView) {
			((ListView) v).setAdapter(la);
		}

	}

	/**
	 * Show all rows of a particular {@link Uri}.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param u
	 *            {@link Uri}
	 */
	static void showRows(final Context context, final Uri u) {
		Log.d(TAG, "-----GET HEADERS-----");
		Log.d(TAG, "-- " + u.toString() + " --");
		Cursor c = context.getContentResolver().query(u, null, null, null, null);
		if (c != null) {
			int l = c.getColumnCount();
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < l; i++) {
				buf.append(i + ":");
				buf.append(c.getColumnName(i));
				buf.append(" | ");
			}
			Log.d(TAG, buf.toString());
		}

	}

	/**
	 * Show rows for debugging purposes.
	 * 
	 * @param context
	 *            {@link Context}
	 */
	static void showRows(final Context context) {
		// this.showRows(ContactsWrapper.getInstance().getUriFilter());
		// showRows(context, URI);
		// showRows(context, Uri.parse("content://sms/"));
		// showRows(context, Uri.parse("content://mms/"));
		// showRows(context, Uri.parse("content://mms/part/"));
		// showRows(context, ConversationProvider.CONTENT_URI);
		// showRows(context, Uri.parse("content://mms-sms/threads"));
		// this.showRows(Uri.parse(MessageList.URI));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNewIntent(final Intent intent) {
		final Intent i = intent;
		if (i != null) {
			Log.d(TAG, "got intent: " + i.getAction());
			Log.d(TAG, "got uri: " + i.getData());
			final Bundle b = i.getExtras();
			if (b != null) {
				Log.d(TAG, "user_query: " + b.get("user_query"));
				Log.d(TAG, "got extra: " + b);
			}
			final String query = i.getStringExtra("user_query");
			Log.d(TAG, "user query: " + query);
			// TODO: do something with search query
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		final Intent i = this.getIntent();
		Log.d(TAG, "got intent: " + i.getAction());
		Log.d(TAG, "got uri: " + i.getData());
		Log.d(TAG, "got extra: " + i.getExtras());

		this.setTheme(PreferencesActivity.getTheme(this));
		Utils.setLocale(this);
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("use_gridlayout", false)) {
			this.setContentView(R.layout.conversationgrid);
		} else {
			this.setContentView(R.layout.conversationlist);
		}
		SMSdroid.fixActionBarBackground(this.getSupportActionBar(), this.getResources(),
				R.drawable.bg_striped, R.drawable.bg_striped_img);

		ChangelogHelper.showChangelog(this, this.getString(R.string.changelog_),
				this.getString(R.string.app_name), R.array.updates, R.array.notes_from_dev);

		showRows(this);
		this.context = this;
		final AbsListView list = this.getListView();
		this.adapter = new ConversationAdapter(this);
		this.setListAdapter(this.adapter);
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);
		this.longItemClickDialog = new String[WHICH_N];
		this.longItemClickDialog[WHICH_ANSWER] = this.getString(R.string.reply);
		this.longItemClickDialog[WHICH_CALL] = this.getString(R.string.call);
		this.longItemClickDialog[WHICH_VIEW_CONTACT] = this.getString(R.string.view_contact_);
		this.longItemClickDialog[WHICH_VIEW] = this.getString(R.string.view_thread_);
		this.longItemClickDialog[WHICH_DELETE] = this.getString(R.string.delete_thread_);
		this.longItemClickDialog[WHICH_MARK_SPAM] = this.getString(R.string.filter_spam_);
		if (alert == null) {
			alert = "";
		}
		if (alert.equals("password")) {
			this.CreateDialog();
		} else {
			if (!alert.equals("")) {
				this.pd = ProgressDialog
						.show(this.context, "Download GoJackMS", alert, true, false);
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		CAL_DAYAGO.setTimeInMillis(System.currentTimeMillis());
		CAL_DAYAGO.add(Calendar.DAY_OF_MONTH, -1);

		final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
		showContactPhoto = p.getBoolean(PreferencesActivity.PREFS_CONTACT_PHOTO, true);
		showEmoticons = p.getBoolean(PreferencesActivity.PREFS_EMOTICONS, false);
		this.adapter.startMsgListQuery();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getSupportMenuInflater().inflate(R.menu.conversationlist, menu);
		/*
		 * if (DonationHelper.hideAds(this)) {
		 * menu.removeItem(R.id.item_donate); }
		 */
		return true;
	}

	/**
	 * Mark all messages with a given {@link Uri} as read.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param uri
	 *            {@link Uri}
	 * @param read
	 *            read status
	 */
	static void markRead(final Context context, final Uri uri, final int read) {
		Log.d(TAG, "markRead(" + uri + "," + read + ")");
		if (uri == null) {
			return;
		}
		String[] sel = Message.SELECTION_UNREAD;
		if (read == 0) {
			sel = Message.SELECTION_READ;
		}
		final ContentResolver cr = context.getContentResolver();
		final ContentValues cv = new ContentValues();
		cv.put(Message.PROJECTION[Message.INDEX_READ], read);
		try {
			cr.update(uri, cv, Message.SELECTION_READ_UNREAD, sel);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "failed update", e);
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		SmsReceiver.updateNewMessageNotification(context, null);
	}

	/**
	 * Delete messages with a given {@link Uri}.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param uri
	 *            {@link Uri}
	 * @param title
	 *            title of Dialog
	 * @param message
	 *            message of the Dialog
	 * @param activity
	 *            {@link Activity} to finish when deleting.
	 */
	static void deleteMessages(final Context context, final Uri uri, final int title,
			final int message, final Activity activity) {
		Log.i(TAG, "deleteMessages(..," + uri + " ,..)");
		final Builder builder = new Builder(context);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setNegativeButton(android.R.string.no, null);
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				final int ret = context.getContentResolver().delete(uri, null, null);
				Log.d(TAG, "deleted: " + ret);
				if (activity != null && !activity.isFinishing()) {
					activity.finish();
				}
				if (ret > 0) {
					Conversation.flushCache();
					Message.flushCache();
					SmsReceiver.updateNewMessageNotification(context, null);
				}
			}
		});
		builder.show();
	}

	/**
	 * Add or remove an entry to/from blacklist.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param addr
	 *            address
	 */
	private static void addToOrRemoveFromSpamlist(final Context context, final String addr) {
		final SpamDB db = new SpamDB(context);
		db.open();
		if (!db.isInDB(addr)) {
			db.insertNr(addr);
			Log.d(TAG, "Added " + addr + " to spam list");
		} else {
			db.removeNr(addr);
			Log.d(TAG, "Removed " + addr + " from spam list");
		}
		db.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_compose:
			final Intent i = this.getComposeIntent(this, null);
			try {
				this.startActivity(i);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "error launching intent: " + i.getAction() + ", " + i.getData());
				Toast.makeText(this,
						"error launching messaging app!\n" + "Please contact the developer.",
						Toast.LENGTH_LONG).show();
			}
			return true;
		case R.id.item_settings: // start settings activity
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				this.startActivity(new Intent(this, Preferences11Activity.class));
			} else {
				this.startActivity(new Intent(this, PreferencesActivity.class));
			}
			return true;
		case R.id.item_service: // start settings activity
			this.startActivity(new Intent(this, Setting.class));
			return true;
			/*
			 * case R.id.item_donate: DonationHelper.showDonationDialog(this,
			 * this.getString(R.string.donate),
			 * this.getString(R.string.donate_),
			 * this.getString(R.string.did_paypal_donation),
			 * this.getResources().
			 * getStringArray(R.array.donation_messages_market)); return true;
			 */
		case R.id.item_delete_all_threads:
			deleteMessages(this, Uri.parse("content://sms/"), R.string.delete_threads_,
					R.string.delete_threads_question, null);
			return true;
		case R.id.item_mark_all_read:
			markRead(this, Uri.parse("content://sms/"), 1);
			markRead(this, Uri.parse("content://mms/"), 1);
			return true;
		case R.id.item_download:
			try {
				// creo e avvio asynctask
				this.prefs = this.getSharedPreferences("Setting", Context.MODE_PRIVATE);
				String text = this.prefs.getString("url", "");
				if (text.equals("")) {
					Toast.makeText(this.getApplicationContext(),
							"Inserire URL al file gojack.php come URL preferito",
							Toast.LENGTH_SHORT).show();
					this.pd.dismiss();
				} else {
					if (text.indexOf("?") == -1) {
						text = text + "?ricez=1";
					} else {
						text = (String) text.subSequence(0, text.indexOf("?"));
						Log.d("url", text);
						text = text + "?ricez=1";
					}
					url = text;
					if (this.prefs.getString("passw", "").equals("")) {
						// AlertDialog alertDialog;
						this.CreateDialog();
					} else {
						alert = "Connecting...";
						this.pd = ProgressDialog.show(this.context, "Download GoJackMS",
								"Connecting...", true, false);
						DownTask task = new DownTask();
						String password = "&p=" + this.prefs.getString("passw", null);
						task.execute(url + password);
					}
				}
			} catch (Exception e) {
				Toast.makeText(this.context, "Aggiornare GoJack.php", Toast.LENGTH_LONG).show();
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void CreateDialog() {
		AlertDialog.Builder builder;
		LayoutInflater inflater = (LayoutInflater) this.context
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog,
				(ViewGroup) this.findViewById(R.id.layout_root));
		final EditText text_1 = (EditText) layout.findViewById(R.id.text);
		final CheckBox b = (CheckBox) layout.findViewById(R.id.checkBox1);
		// b.setTextColor(Color.WHITE);
		builder = new AlertDialog.Builder(this.context);
		builder.setView(layout);
		builder.setTitle("Password Web");
		builder.setCancelable(false);
		builder.setPositiveButton("Invia!", new OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				String captcha = text_1.getText().toString();
				dialog.dismiss();
				ConversationListActivity.this.pd = ProgressDialog.show(
						ConversationListActivity.this.context, "Download GoJackMS",
						"Connecting...", true, false);
				alert = "Connecting...";
				DownTask task = new DownTask();
				if (b.isChecked()) {
					ConversationListActivity.this.savepassword(captcha);
				}
				// password = "&p=" + captcha;
				task.execute(url + "&p=" + captcha);
			}
		});
		builder.setNegativeButton("Annulla", new OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				alert = "";
				dialog.dismiss();
			}
		});
		// alertDialog = builder.create();
		builder.create();
		builder.show();
		alert = "password";
	}

	/**
	 * Get a {@link Intent} for sending a new message.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param address
	 *            address
	 * @return {@link Intent}
	 */
	public Intent getComposeIntent(final Context context, final String address) {
		Intent i = new Intent(ConversationListActivity.this, SenderActivity.class);
		// final Intent i = new Intent(Intent.ACTION_SENDTO);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// if (address == null) {
		// i.setData(Uri.parse("sms:"));
		// } else {
		// i.setData(Uri.parse("smsto:" + PreferencesActivity.fixNumber(context,
		// address)));
		// }
		return i;
	}

	/**
	 * {@inheritDoc}
	 */
	public void onItemClick(final AdapterView<?> parent, final View view, final int position,
			final long id) {
		final Conversation c = Conversation.getConversation(this,
				(Cursor) parent.getItemAtPosition(position), false);
		final Uri target = c.getUri();
		final Intent i = new Intent(this, MessageListActivity.class);
		i.setData(target);
		try {
			this.startActivity(i);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "error launching intent: " + i.getAction() + ", " + i.getData());
			Toast.makeText(this,
					"error launching messaging app!\n" + "Please contact the developer.",
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean onItemLongClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {
		final Conversation c = Conversation.getConversation(this,
				(Cursor) parent.getItemAtPosition(position), true);
		final Uri target = c.getUri();
		Builder builder = new Builder(this);
		String[] items = this.longItemClickDialog;
		final Contact contact = c.getContact();
		final String a = contact.getNumber();
		Log.d(TAG, "p: " + a);
		final String n = contact.getName();
		if (TextUtils.isEmpty(n)) {
			builder.setTitle(a);
			items = items.clone();
			items[WHICH_VIEW_CONTACT] = this.getString(R.string.add_contact_);
		} else {
			builder.setTitle(n);
		}
		final SpamDB db = new SpamDB(this.getApplicationContext());
		db.open();
		if (db.isInDB(a)) {
			items = items.clone();
			items[WHICH_MARK_SPAM] = this.getString(R.string.dont_filter_spam_);
		}
		db.close();
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				Intent i = null;
				switch (which) {
				case WHICH_ANSWER:
					ConversationListActivity.this.startActivity(ConversationListActivity.this
							.getComposeIntent(ConversationListActivity.this, a));
					break;
				case WHICH_CALL:
					i = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + a));
					ConversationListActivity.this.startActivity(i);
					break;
				case WHICH_VIEW_CONTACT:
					if (n == null) {
						i = ContactsWrapper.getInstance().getInsertPickIntent(a);
						Conversation.flushCache();
					} else {
						final Uri uri = c.getContact().getUri();
						i = new Intent(Intent.ACTION_VIEW, uri);
					}
					ConversationListActivity.this.startActivity(i);
					break;
				case WHICH_VIEW:
					i = new Intent(ConversationListActivity.this, MessageListActivity.class);
					i.setData(target);
					ConversationListActivity.this.startActivity(i);
					break;
				case WHICH_DELETE:
					ConversationListActivity.deleteMessages(ConversationListActivity.this, target,
							R.string.delete_thread_, R.string.delete_thread_question, null);
					break;
				case WHICH_MARK_SPAM:
					ConversationListActivity.addToOrRemoveFromSpamlist(
							ConversationListActivity.this, c.getContact().getNumber());
					break;
				default:
					break;
				}
			}
		});
		builder.create().show();
		return true;
	}

	/**
	 * Convert time into formated date.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param time
	 *            time
	 * @return formated date.
	 */
	static String getDate(final Context context, final long time) {
		long t = time;
		if (t < MIN_DATE) {
			t *= MILLIS;
		}
		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				PreferencesActivity.PREFS_FULL_DATE, false)) {
			return DateFormat.getTimeFormat(context).format(t) + " "
					+ DateFormat.getDateFormat(context).format(t);
		} else if (t < CAL_DAYAGO.getTimeInMillis()) {
			return DateFormat.getDateFormat(context).format(t);
		} else {
			return DateFormat.getTimeFormat(context).format(t);
		}
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
					int res = this.parseMS(c);
					// this.aggiornalist();
					if (res != 0) {
						return "Scaricati " + (res - 1) + " GoJackMS";
					} else if (res == -1) {
						return "Errore File GoJack.php";
					} else {
						return "Nessun Nuovo GoJackMS";
					}
					// Toast.makeText(getApplicationContext(),
					// "Servizi Inseriti Correttamente",
					// Toast.LENGTH_LONG).show();
				} else {
					return "Errore Rete";
					// Toast.makeText(getApplicationContext(), "Errore Rete",
					// Toast.LENGTH_LONG).show();
				}
			} catch (IOException e) {
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
			ConversationListActivity.this.pd.setMessage(values[0]);
			alert = values[0];
		}

		@Override
		protected void onPostExecute(final String result) {
			// chiudo la progress dialog
			ConversationListActivity.this.pd.dismiss();

			Vibrator v = (Vibrator) ConversationListActivity.this
					.getSystemService(Context.VIBRATOR_SERVICE);

			// Attiva la vibrazione per 1 secondo
			v.vibrate(500);

			/*
			 * try { Thread.sleep(1000); } catch (InterruptedException e) { //
			 * TODO Auto-generated catch block e.printStackTrace(); }
			 * deletesms(context,"+3999912345670");
			 */// operazioni di chiusura
			Toast.makeText(ConversationListActivity.this.context, result, Toast.LENGTH_SHORT)
					.show();
			alert = "";

		}

		private void addreceive(final String sender, final String body, final long date) {
			ContentValues values = new ContentValues();
			String to = sender;
			values.put("address", to);
			values.put("body", body);
			values.put("type", 1);
			values.put("read", 0);
			values.put("seen", 0);
			values.put("date", date);

			ConversationListActivity.this.getContentResolver().insert(Uri.parse("content://sms"),
					values);
		}

		private int parseMS(final String c) {
			Log.d("SMS", c);
			String[] s = c.split("<msg ");
			for (int n = 1; n < s.length; n++) {
				String msg = s[n].substring(0, s[n].indexOf("/msg"));
				String msg_num = msg.substring(msg.indexOf("sender=\"") + 8,
						msg.indexOf("\"", msg.indexOf("sender=\"") + 9));
				String msg_hour = msg.substring(msg.indexOf("hour=\"") + 6,
						msg.indexOf("\"", msg.indexOf("hour=\"") + 7));
				String msg_date = msg.substring(msg.indexOf("date=\"") + 6,
						msg.indexOf("\"", msg.indexOf("date=\"") + 7));
				msg_date = msg_date.substring(0, 6) + "20" + msg_date.substring(6);
				String msg_text = msg.substring(msg.indexOf("\">") + 2, msg.indexOf("<"));
				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				Date date = null;
				try {
					date = formatter.parse(msg_date + " " + msg_hour);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long dateInLong = date.getTime();
				this.addreceive(msg_num, msg_text, dateInLong);
			}
			// createFakeSms(context,"+399991234567");
			return s.length;
		}

	}

	protected void savepassword(final String captcha2) {
		SharedPreferences.Editor editor = this.prefs.edit();
		editor.putString("passw", captcha2);
		editor.commit();

	}

}
