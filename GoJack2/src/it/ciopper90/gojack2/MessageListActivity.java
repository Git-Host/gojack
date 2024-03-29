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

import it.ciopper90.gojack2.added.SendSMS;
import it.ciopper90.gojack2.utils.Dialog;
import it.ciopper90.gojack2.utils.Servizio;
import it.ciopper90.gojack2.utils.WSInterface;

import java.util.ArrayList;
import java.util.HashSet;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.support.v4.app.FragmentActivity;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ub0r.android.lib.Log;
import de.ub0r.android.lib.Utils;
import de.ub0r.android.lib.apis.Contact;
import de.ub0r.android.lib.apis.ContactsWrapper;

/**
 * {@link FragmentActivity} showing a single conversation.
 * 
 * @author flx
 */
@SuppressLint("HandlerLeak")
@SuppressWarnings("deprecation")
public class MessageListActivity extends SherlockActivity implements OnItemClickListener,
		OnItemLongClickListener, OnClickListener, OnLongClickListener {
	/** Tag for output. */
	private static final String TAG = "ml";

	/** Ad's keywords. */
	public static final HashSet<String> AD_KEYWORDS = new HashSet<String>();
	static {
		AD_KEYWORDS.add("android");
		AD_KEYWORDS.add("mobile");
		AD_KEYWORDS.add("handy");
		AD_KEYWORDS.add("cellphone");
		AD_KEYWORDS.add("google");
		AD_KEYWORDS.add("htc");
		AD_KEYWORDS.add("samsung");
		AD_KEYWORDS.add("motorola");
		AD_KEYWORDS.add("market");
		AD_KEYWORDS.add("app");
		AD_KEYWORDS.add("message");
		AD_KEYWORDS.add("txt");
		AD_KEYWORDS.add("sms");
		AD_KEYWORDS.add("mms");
		AD_KEYWORDS.add("game");
		AD_KEYWORDS.add("websms");
		AD_KEYWORDS.add("amazon");
	}

	/** {@link ContactsWrapper}. */
	private static final ContactsWrapper WRAPPER = ContactsWrapper.getInstance();

	/** Number of items. */
	private static final int WHICH_N = 8;
	/** Index in dialog: mark view/add contact. */
	private static final int WHICH_VIEW_CONTACT = 0;
	/** Index in dialog: mark call contact. */
	private static final int WHICH_CALL = 1;
	/** Index in dialog: mark read/unread. */
	private static final int WHICH_MARK_UNREAD = 2;
	/** Index in dialog: reply. */
	private static final int WHICH_REPLY = 3;
	/** Index in dialog: forward. */
	private static final int WHICH_FORWARD = 4;
	/** Index in dialog: copy text. */
	private static final int WHICH_COPY_TEXT = 5;
	/** Index in dialog: view details. */
	private static final int WHICH_VIEW_DETAILS = 6;
	/** Index in dialog: delete. */
	private static final int WHICH_DELETE = 7;

	/** Package name for System's chooser. */
	// private static String chooserPackage = null;

	/** Used {@link Uri}. */
	private Uri uri;
	/** {@link Conversation} shown. */
	private Conversation conv = null;

	/** ORIG_URI to resolve. */
	static final String URI = "content://mms-sms/conversations/";

	/** Dialog items shown if an item was long clicked. */
	private final String[] longItemClickDialog = new String[WHICH_N];

	/** Marked a message unread? */
	private boolean markedUnread = false;

	/** {@link EditText} holding text. */
	private static EditText etText;
	/** {@link ClipboardManager}. */
	private ClipboardManager cbmgr;

	/** Enable autosend. */
	private boolean enableAutosend = true;
	/** Show textfield. */
	private boolean showTextField = true;
	/** Show {@link Contact}'s photo. */
	private boolean showPhoto = false;

	/** Default {@link Drawable} for {@link Contact}s. */
	private Drawable defaultContactAvatar = null;

	/** TextWatcher updating char count on writing. */
	private MyTextWatcher textWatcher;

	/** {@link MenuItem} holding {@link Contact}'s picture. */
	private MenuItem contactItem = null;
	/** Show {@link MenuItem} holding {@link Contact}'s picture . */
	private boolean showContactItem = false;
	/** True, to update {@link Contact}'s photo. */
	private boolean needContactUpdate = false;
	// private String prova;
	private static String usedservice;
	private static String errore;
	private static ProgressDialog pd;
	private Spinner spinner;
	// private SpinnerAdapter spinneradapter;
	private static SendSMS send;
	private static String alert;
	private static String number;

	/**
	 * Get {@link ListView}.
	 * 
	 * @return {@link ListView}
	 */
	private ListView getListView() {
		return (ListView) this.findViewById(android.R.id.list);
	}

	/**
	 * Set {@link ListAdapter} to {@link ListView}.
	 * 
	 * @param la
	 *            ListAdapter
	 */
	private void setListAdapter(final ListAdapter la) {
		this.getListView().setAdapter(la);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
		this.enableAutosend = p.getBoolean(PreferencesActivity.PREFS_ENABLE_AUTOSEND, true);
		this.showTextField = this.enableAutosend
				|| p.getBoolean(PreferencesActivity.PREFS_SHOWTEXTFIELD, true);
		this.showPhoto = false;
		final boolean hideSend = p.getBoolean(PreferencesActivity.PREFS_HIDE_SEND, false);
		this.setTheme(PreferencesActivity.getTheme(this));
		Utils.setLocale(this);
		this.setContentView(R.layout.messagelist);
		SMSdroid.fixActionBarBackground(this.getSupportActionBar(), this.getResources(),
				R.drawable.bg_striped, R.drawable.bg_striped_img);
		// this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		Log.d(TAG, "onCreate()");

		if (this.showPhoto) {
			this.defaultContactAvatar = this.getResources().getDrawable(
					R.drawable.ic_contact_picture);
		}
		if (hideSend) {
			this.findViewById(R.id.send_).setVisibility(View.GONE);
		}

		this.cbmgr = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
		MessageListActivity.etText = (EditText) this.findViewById(R.id.text);

		if (!this.showTextField) {
			this.findViewById(R.id.text_layout).setVisibility(View.GONE);
		}

		this.parseIntent(this.getIntent());

		final ListView list = this.getListView();
		list.setOnItemLongClickListener(this);
		list.setOnItemClickListener(this);
		View v = this.findViewById(R.id.send_);
		v.setOnClickListener(this);
		v.setOnLongClickListener(this);
		this.findViewById(R.id.text_paste).setOnClickListener(this);
		this.textWatcher = new MyTextWatcher(this, (TextView) this.findViewById(R.id.text_paste),
				(TextView) this.findViewById(R.id.text_));
		MessageListActivity.etText.addTextChangedListener(this.textWatcher);
		this.textWatcher.afterTextChanged(MessageListActivity.etText.getEditableText());

		this.longItemClickDialog[WHICH_MARK_UNREAD] = this.getString(R.string.mark_unread_);
		this.longItemClickDialog[WHICH_REPLY] = this.getString(R.string.reply);
		this.longItemClickDialog[WHICH_FORWARD] = this.getString(R.string.forward_);
		this.longItemClickDialog[WHICH_COPY_TEXT] = this.getString(R.string.copy_text_);
		this.longItemClickDialog[WHICH_VIEW_DETAILS] = this.getString(R.string.view_details_);
		this.longItemClickDialog[WHICH_DELETE] = this.getString(R.string.delete_message_);
		// this.longItemClickDialog[WHICH_SPEAK] =
		// this.getString(R.string.speak_);

		// this.ws = new WorkServizio(this.getApplicationContext());

		// service = this.ws.caricaServizio();
		// this.spinner = (Spinner) this.findViewById(R.id.planets_spinner);

		// if (service.size() == 0) {
		// Intent asd=new Intent(MainActivity.this,ViewServizio.class);
		// startActivity(asd);
		// }
		// if (service.size() > 0) {
		// this.spinner = (Spinner)
		// this.contactItem.getActionView().findViewById(
		// R.id.item_service);
		// ArrayList<String> lista = new ArrayList<String>();
		// for (int i = 0; i < service.size(); i++) {
		// lista.add(service.get(i).getName());
		// }
		// lista.add("a");
		// lista.add("b");
		// lista.add("c");
		// this.spinneradapter = new ArrayAdapter<String>(this,
		// R.layout.sherlock_spinner_dropdown_item, lista);
		/**
		 * Defining the ArrayAdapter to set items to Spinner Widget
		 * 
		 * @return
		 * @return
		 * @return
		 */
		// spinneradapter = new ArrayAdapter<String>(this,
		// android.R.layout.simple_spinner_item,
		// lista);
		// spinneradapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		// this.spinner.setAdapter(spinneradapter);

		// this.spinner.setAdapter(spinneradapter);
		// this.spinner.setVisibility(View.VISIBLE);
		// numchar=ws.caratteriServizio(service.get(spinner.getSelectedItemPosition()).getUrl());
		// testo.addTextChangedListener(this);
		// spinner.setOnItemSelectedListener(this);
		// }
		// tw=(TextView)this.findViewById(R.id.textView4);
		// tw.setText("0/"+numchar);
		// registerForContextMenu(testo);
		// registerForContextMenu(number);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		this.parseIntent(intent);
	}

	/**
	 * Parse data pushed by {@link Intent}.
	 * 
	 * @param intent
	 *            {@link Intent}
	 */
	private void parseIntent(final Intent intent) {
		Log.d(TAG, "parseIntent(" + intent + ")");
		if (intent == null) {
			return;
		}
		Log.d(TAG, "got action: " + intent.getAction());
		Log.d(TAG, "got uri: " + intent.getData());

		this.needContactUpdate = true;

		this.uri = intent.getData();
		if (this.uri != null) {
			if (!this.uri.toString().startsWith(URI)) {
				this.uri = Uri.parse(URI + this.uri.getLastPathSegment());
			}
		} else {
			final long tid = intent.getLongExtra("thread_id", -1L);
			this.uri = Uri.parse(URI + tid);
			if (tid < 0L) {
				try {
					// this.startActivity(ConversationListActivity.getComposeIntent(this,
					// null));
				} catch (ActivityNotFoundException e) {
					Log.e(TAG, "activity not found", e);
					Toast.makeText(this, R.string.error_conv_null, Toast.LENGTH_LONG).show();
				}
				this.finish();
				return;
			}
		}

		final int threadId = Integer.parseInt(this.uri.getLastPathSegment());
		final Conversation c = Conversation.getConversation(this, threadId, true);
		this.conv = c;

		if (c == null) {
			Toast.makeText(this, R.string.error_conv_null, Toast.LENGTH_LONG).show();
			this.finish();
			return;
		}

		final Contact contact = c.getContact();
		contact.update(this, false, true);

		Log.d(TAG, "address: " + contact.getNumber());
		Log.d(TAG, "name: " + contact.getName());
		Log.d(TAG, "displayName: " + contact.getDisplayName());

		final ListView lv = this.getListView();
		lv.setStackFromBottom(true);

		MessageAdapter adapter = new MessageAdapter(this, this.uri);
		this.setListAdapter(adapter);

		String displayName = contact.getDisplayName();
		this.setTitle(displayName);
		number = contact.getNumber();
		if (displayName.equals(number)) {
			this.getSupportActionBar().setSubtitle(null);
			number = contact.getDisplayName();
		} else {
			this.getSupportActionBar().setSubtitle(number);
		}
		// this.getSupportActionBar().setDisplayUseLogoEnabled(false);
		// this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		this.getSupportActionBar().setDisplayShowHomeEnabled(false);

		// this.spinner = WSInterface.setSpinner(this);
		if (MessageListActivity.alert == null) {
			MessageListActivity.alert = "";
		}
		// this.getSupportActionBar().setIcon(R.drawable.ic_menu_star);
		// this.getSupportActionBar().
		// this.ws = new WorkServizio(this.getApplicationContext());

		// service = this.ws.caricaServizio();
		// this.spinner = new Spinner(this);
		// ArrayList<String> lista = new ArrayList<String>();
		// for (int i = 0; i < service.size(); i++) {
		// lista.add(service.get(i).getName());
		// }
		// lista.add("a");
		// lista.add("b");
		// lista.add("c");

		// this.spinneradapter = new ArrayAdapter<String>(this,
		// R.layout.textview, lista);
		// this.spinner.setAdapter(this.spinneradapter);

		// String serv = WSInterface.ServizioNumero(number);
		// if (serv != null) {
		// int a = lista.indexOf(serv);
		// lista.indexOf(serv));
		// }
		// TODO attach to an adapter of some sort
		// this.getSupportActionBar().setCustomView(this.spinner);
		// this.getSupportActionBar().setDisplayShowCustomEnabled(true);
		// this.setContactIcon(contact);

		if (MessageListActivity.alert.equals("captcha")) {
			this.captcha();// Dialog.RestoreDialog(this, this.alert);
		} else {
			if (MessageListActivity.alert.equals("error")) {
				this.error(MessageListActivity.errore);
			} else {
				if (!MessageListActivity.alert.equals("")) {
					MessageListActivity.pd = Dialog.ProgDialog(this, MessageListActivity.alert);
					MessageListActivity.pd.show();
				}
			}
		}

		final String body = intent.getStringExtra(Intent.EXTRA_TEXT);
		if (!TextUtils.isEmpty(body)) {
			MessageListActivity.etText.setText(body);
		}

		this.setRead();
	}

	/**
	 * Show {@link Contact}'s photo.
	 * 
	 * @param contact
	 *            {@link Contact}
	 */
	private void setContactIcon(final Contact contact) {
		if (contact == null) {
			Log.w(TAG, "setContactIcon(null)");
			this.showContactItem = false;
			return;
		}

		final String name = contact.getName();
		this.showContactItem = this.showPhoto && name != null;

		if (this.contactItem == null) {
			Log.w(TAG, "setContactIcon: contactItem == null");
			return;
		}

		if (!this.needContactUpdate) {
			Log.i(TAG, "skip setContactIcon()");
			return;
		}

		if (this.showPhoto && name != null) {
			// photo
			ImageView ivPhoto = (ImageView) this.findViewById(R.id.photo);
			if (ivPhoto == null) {
				ivPhoto = (ImageView) this.contactItem.getActionView().findViewById(R.id.photo);
			}
			if (ivPhoto == null) {
				Log.w(TAG, "ivPhoto == null");
			} else {
				ivPhoto.setImageDrawable(contact.getAvatar(this, this.defaultContactAvatar));
				ivPhoto.setOnClickListener(WRAPPER.getQuickContact(this, ivPhoto,
						contact.getLookUpUri(this.getContentResolver()), 2, null));
			}

			// presence
			ImageView ivPresence = (ImageView) this.findViewById(R.id.presence);
			if (ivPresence == null) {
				ivPresence = (ImageView) this.contactItem.getActionView().findViewById(
						R.id.presence);
			}
			if (ivPresence == null) {
				Log.w(TAG, "ivPresence == null");
			} else {
				if (contact.getPresenceState() > 0) {
					ivPresence.setImageResource(Contact.getPresenceRes(contact.getPresenceState()));
					ivPresence.setVisibility(View.VISIBLE);
				} else {
					ivPresence.setVisibility(View.INVISIBLE);
				}
			}
		}

		this.contactItem.setVisible(this.showContactItem);
		this.needContactUpdate = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onResume() {
		super.onResume();
		// boolean noAds = DonationHelper.hideAds(this);
		// if (!noAds) {
		// Ads.loadAd(this, R.id.ad, ADMOB_PUBID, AD_KEYWORDS);
		// }

		final ListView lv = this.getListView();
		lv.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		lv.setAdapter(new MessageAdapter(this, this.uri));
		this.markedUnread = false;

		// final Button btn = (Button) this.findViewById(R.id.send_);
		// if (this.showTextField) {
		// final Intent i = this.buildIntent(this.enableAutosend, false);
		// final PackageManager pm = this.getPackageManager();
		// ActivityInfo ai = null;
		// if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
		// PreferencesActivity.PREFS_SHOWTARGETAPP, true)) {
		// ai = i.resolveActivityInfo(pm, 0);
		// }
		// if (ai == null) {
		// btn.setText(null);
		// this.etText.setMinLines(1);
		// } else {
		// if (chooserPackage == null) {
		// final ActivityInfo cai = this.buildIntent(this.enableAutosend, true)
		// .resolveActivityInfo(pm, 0);
		// if (cai != null) {
		// chooserPackage = cai.packageName;
		// }
		// }
		// if (ai.packageName.equals(chooserPackage)) {
		// btn.setText(R.string.chooser_);
		// } else {
		// Log.d(TAG, "ai.pn: " + ai.packageName);
		// btn.setText(ai.loadLabel(pm));
		// }
		// this.etText.setMinLines(3);
		// }
		// } else {
		// btn.setText(null);
		// }

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onPause() {
		super.onPause();
		if (!this.markedUnread) {
			this.setRead();
		}
	}

	/**
	 * Set all messages in a given thread as read.
	 */
	private void setRead() {
		if (this.conv != null) {
			ConversationListActivity.markRead(this, this.conv.getUri(), 1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		this.getSupportMenuInflater().inflate(R.menu.messagelist, menu);

		// this.contactItem = menu.findItem(R.id.item_service);
		// this.spinner = (Spinner)
		// this.contactItem.getActionView().findViewById(R.id.item_service);
		this.spinner = (Spinner) menu.findItem(R.id.menuSort).getActionView();
		this.spinner = WSInterface.setSpinner(this.spinner, this);
		if (number.contains(" ")) {
			number = number.replace(" ", "");
		}
		this.spinner.setSelection(WSInterface.PositionServizioNumero(number));

		if (this.conv != null) {
			this.setContactIcon(this.conv.getContact());
		}
		final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
		if (p.getBoolean(PreferencesActivity.PREFS_HIDE_RESTORE, false)) {
			menu.removeItem(R.id.item_restore);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in Action Bar clicked; go home
			Intent intent = new Intent(this, ConversationListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			return true;
		case R.id.item_delete_thread:
			ConversationListActivity.deleteMessages(this, this.uri, R.string.delete_thread_,
					R.string.delete_thread_question, this);
			return true;
		case R.id.item_answer:
			this.send(true, false);
			return true;
		case R.id.item_call:
			this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"
					+ this.conv.getContact().getNumber())));
			return true;
		case R.id.item_restore:
			MessageListActivity.etText.setText(PreferenceManager.getDefaultSharedPreferences(this)
					.getString(PreferencesActivity.PREFS_BACKUPLASTTEXT, null));
			return true;
			// case R.id.item_contact:
			// if (this.conv != null && this.contactItem != null) {
			// WRAPPER.showQuickContactFallBack(this,
			// this.contactItem.getActionView(), this.conv
			// .getContact().getLookUpUri(this.getContentResolver()), 2, null);
			// }
			// return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void onItemClick(final AdapterView<?> parent, final View view, final int position,
			final long id) {
		this.onItemLongClick(parent, view, position, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean onItemLongClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {
		final Context context = this;
		final Message m = Message.getMessage(this, (Cursor) parent.getItemAtPosition(position));
		final Uri target = m.getUri();
		final int read = m.getRead();
		final int type = m.getType();
		Builder builder = new Builder(context);
		builder.setTitle(R.string.message_options_);

		final Contact contact = this.conv.getContact();
		final String a = contact.getNumber();
		Log.d(TAG, "p: " + a);
		final String n = contact.getName();

		String[] items = this.longItemClickDialog;
		if (TextUtils.isEmpty(n)) {
			items[WHICH_VIEW_CONTACT] = this.getString(R.string.add_contact_);
		} else {
			items[WHICH_VIEW_CONTACT] = this.getString(R.string.view_contact_);
		}
		items[WHICH_CALL] = this.getString(R.string.call) + " " + contact.getDisplayName();
		if (read == 0) {
			items = items.clone();
			items[WHICH_MARK_UNREAD] = context.getString(R.string.mark_read_);
		}
		if (type == Message.SMS_DRAFT) {
			items = items.clone();
			items[WHICH_FORWARD] = context.getString(R.string.send_draft_);
		}
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				Intent i = null;
				switch (which) {
				case WHICH_VIEW_CONTACT:
					if (n == null) {
						i = ContactsWrapper.getInstance().getInsertPickIntent(a);
						Conversation.flushCache();
					} else {
						final Uri u = MessageListActivity.this.conv.getContact().getUri();
						i = new Intent(Intent.ACTION_VIEW, u);
					}
					try {
						MessageListActivity.this.startActivity(i);
					} catch (ActivityNotFoundException e) {
						Log.e(TAG, "activity not found: " + i.getAction(), e);
						Toast.makeText(MessageListActivity.this, "activity not found",
								Toast.LENGTH_LONG).show();
					}
					break;
				case WHICH_CALL:
					MessageListActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse("tel:" + a)));
					break;
				case WHICH_MARK_UNREAD:
					ConversationListActivity.markRead(context, target, 1 - read);
					MessageListActivity.this.markedUnread = true;
					break;
				case WHICH_REPLY:
					// MessageListActivity.this.startActivity(ConversationListActivity
					// .getComposeIntent(MessageListActivity.this, a));
					break;
				case WHICH_FORWARD:
					int resId;
					if (type == Message.SMS_DRAFT) {
						resId = R.string.send_draft_;
						// i =
						// ConversationListActivity.getComposeIntent(MessageListActivity.this,
						// MessageListActivity.this.conv.getContact().getNumber());
					} else {
						resId = R.string.forward_;
						i = new Intent(Intent.ACTION_SEND);
						i.setType("text/plain");
						i.putExtra("forwarded_message", true);
					}
					CharSequence text = null;
					if (PreferencesActivity.decodeDecimalNCR(context)) {
						text = Converter.convertDecNCR2Char(m.getBody());
					} else {
						text = m.getBody();
					}
					i.putExtra(Intent.EXTRA_TEXT, text);
					i.putExtra("sms_body", text);
					context.startActivity(Intent.createChooser(i, context.getString(resId)));
					break;
				case WHICH_COPY_TEXT:
					final ClipboardManager cm = (ClipboardManager) context
							.getSystemService(Context.CLIPBOARD_SERVICE);
					if (PreferencesActivity.decodeDecimalNCR(context)) {
						cm.setText(Converter.convertDecNCR2Char(m.getBody()));
					} else {
						cm.setText(m.getBody());
					}
					break;
				case WHICH_VIEW_DETAILS:
					final int t = m.getType();
					Builder b = new Builder(context);
					b.setTitle(R.string.view_details_);
					b.setCancelable(true);
					StringBuilder sb = new StringBuilder();
					final String a = m.getAddress(context);
					final long d = m.getDate();
					final String ds = DateFormat.format(
							context.getString(R.string.DATEFORMAT_details), d).toString();
					String sentReceived;
					String fromTo;
					if (t == Calls.INCOMING_TYPE) {
						sentReceived = context.getString(R.string.received_);
						fromTo = context.getString(R.string.from_);
					} else if (t == Calls.OUTGOING_TYPE) {
						sentReceived = context.getString(R.string.sent_);
						fromTo = context.getString(R.string.to_);
					} else {
						sentReceived = "ukwn:";
						fromTo = "ukwn:";
					}
					sb.append(sentReceived + " ");
					sb.append(ds);
					sb.append("\n");
					sb.append(fromTo + " ");
					sb.append(a);
					sb.append("\n");
					sb.append(context.getString(R.string.type_));
					if (m.isMMS()) {
						sb.append(" MMS");
					} else {
						sb.append(" SMS");
					}
					b.setMessage(sb.toString());
					b.setPositiveButton(android.R.string.ok, null);
					b.show();
					break;
				case WHICH_DELETE:
					ConversationListActivity.deleteMessages(context, target,
							R.string.delete_message_, R.string.delete_message_question, null);
					break;
				default:
					break;
				}
			}
		});
		builder.show();
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void onClick(final View v) {
		switch (v.getId()) {
		case R.id.send_:
			this.send(true, false);
			return;
		case R.id.text_paste:
			final CharSequence s = this.cbmgr.getText();
			MessageListActivity.etText.setText(s);
			return;
		default:
			return;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean onLongClick(final View v) {
		switch (v.getId()) {
		case R.id.send_:
			this.send(false, true);
			return true;
		default:
			return true;
		}
	}

	/**
	 * Build an {@link Intent} for sending it.
	 * 
	 * @param autosend
	 *            autosend
	 * @param showChooser
	 *            show chooser
	 * @return {@link Intent}
	 */
	// private Intent buildIntent(final boolean autosend, final boolean
	// showChooser) {
	// final String text = this.etText.getText().toString().trim();
	// final Intent i = ConversationListActivity.getComposeIntent(this,
	// this.conv.getContact()
	// .getNumber());
	// i.putExtra(Intent.EXTRA_TEXT, text);
	// i.putExtra("sms_body", text);
	// if (autosend && this.enableAutosend && text.length() > 0) {
	// i.putExtra("AUTOSEND", "1");
	// }
	// if (showChooser) {
	// return Intent.createChooser(i, this.getString(R.string.reply));
	// } else {
	// return i;
	// }
	// }

	Handler effettuato = new Handler() {

		@Override
		public void handleMessage(final android.os.Message msg) {
			Bundle bundle = msg.getData();
			int cond = bundle.getInt("cond");
			pd.cancel();
			pd.dismiss();
			switch (cond) {
			case 0:
				MessageListActivity.etText.setText("");
				MessageListActivity.this.ok();
				break;
			case 1:
				MessageListActivity.this.captcha();
				break;
			case 2:
				MessageListActivity.this.error(bundle.getString("error"));
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
		// String returnString = null;
		ArrayList<Servizio> service = WSInterface.getService();

		for (int i = 0; i < service.size(); i++) {
			if (service.get(i).getName() == this.spinner.getSelectedItem().toString()) {
				s = service.get(i);
			}
		}
		MessageListActivity.pd = Dialog.ProgDialog(this, "Invio in Corso..");
		// messag="Invio in corso";
		// this.pd.show();
		final String text = MessageListActivity.etText.getText().toString().trim();
		final String recipient = this.conv.getContact().getNumber();
		// Servizio s = new Servizio("free", "cADR8jqr80ku$Fw@fXMY", "", "", "",
		// "http://ciopper90.altervista.org/php5/gofree/gojack.php", "", "");

		MessageListActivity.send = new SendSMS(s, recipient, text, this, MessageListActivity.pd);
		MessageListActivity.send.go(this.effettuato);
		MessageListActivity.alert = "Invio in Corso..";
		MessageListActivity.usedservice = (String) this.spinner.getSelectedItem();
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

	private void error(final String string) {
		MessageListActivity.errore = string;
		AlertDialog.Builder builder = Dialog.ErrorDialog(this, string);
		MessageListActivity.alert = "error";
		builder.setNegativeButton("Chiudi", new AlertDialog.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.dismiss();
				MessageListActivity.alert = "";
			}
		});
		builder.show();

	}

	protected void captcha() {
		AlertDialog.Builder builder = Dialog.RestoreDialog(this, "captcha");
		MessageListActivity.alert = "captcha";
		MessageListActivity.pd = new ProgressDialog(this);
		builder.setPositiveButton("Invia!", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				String captcha = Dialog.getCaptcha();
				MessageListActivity.send.captcha(captcha, MessageListActivity.pd);
				dialog.dismiss();
				MessageListActivity.alert = "";
			}
		});
		builder.setNegativeButton("Annulla!", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.dismiss();
				// annullare realmente l'invio
				MessageListActivity.alert = "";
			}
		});
		builder.create();
		builder.show();

	}

	public void ok() {
		MessageListActivity.etText.setText("");
		String number = this.conv.getContact().getNumber();
		if (number.contains(" ")) {
			number = number.replace(" ", "");
		}
		WSInterface.saveService(number, MessageListActivity.usedservice);
		MessageListActivity.alert = "";
	}

	@Override
	protected void onDestroy() {
		/*
		 * if (this.pd != null) { this.pd.cancel(); this.pd.dismiss(); }
		 */
		super.onDestroy();
	}

}
