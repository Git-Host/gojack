/*
 * Copyright (C) 2010-2012 Felix Bechstein
 * 
 * This file is part of WebSMS.
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

import it.ciopper90.gojack2.R;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import de.ub0r.android.lib.DbUtils;

/**
 * CursorAdapter getting Name, Phone from DB. This class requires android API5+
 * to work.
 * 
 * @author flx
 */
public class MobilePhoneAdapter extends ResourceCursorAdapter {
	/** Preferences: show mobile numbers only. */
	private static boolean prefsMobilesOnly;

	/** Global ContentResolver. */
	private ContentResolver mContentResolver;

	/** Projection for content. */
	private static final String[] PROJECTION = new String[] { BaseColumns._ID, // 0
			Data.DISPLAY_NAME, // 1
			Phone.NUMBER, // 2
			Phone.TYPE // 3
	};

	/** Index of id/lookup key. */
	public static final int INDEX_ID = 0;
	/** Index of name. */
	public static final int INDEX_NAME = 1;
	/** Index of number. */
	public static final int INDEX_NUMBER = 2;
	/** Index of type. */
	public static final int INDEX_TYPE = 3;

	/** Order for content. */
	private static final String SORT = Phone.STARRED + " DESC, " + Phone.TIMES_CONTACTED
			+ " DESC, " + Phone.DISPLAY_NAME + " ASC, " + Phone.TYPE;

	/** List of number types. */
	private final String[] types;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            context
	 */
	@SuppressWarnings("deprecation")
	public MobilePhoneAdapter(final Context context) {
		super(context, R.layout.recipient_dropdown_item, null);
		this.mContentResolver = context.getContentResolver();
		this.types = context.getResources().getStringArray(android.R.array.phoneTypes);
	}

	/** View holder. */
	private static class ViewHolder {
		/** {@link TextView}s. */
		TextView tv1, tv2, tv3;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void bindView(final View view, final Context context, final Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		if (holder == null) {
			holder = new ViewHolder();
			holder.tv1 = (TextView) view.findViewById(R.id.text1);
			holder.tv2 = (TextView) view.findViewById(R.id.text2);
			holder.tv3 = (TextView) view.findViewById(R.id.text3);
			view.setTag(holder);
		}
		holder.tv1.setText(cursor.getString(INDEX_NAME));
		holder.tv2.setText(cursor.getString(INDEX_NUMBER));
		final int i = cursor.getInt(INDEX_TYPE) - 1;
		if (i >= 0 && i < this.types.length) {
			holder.tv3.setText(this.types[i]);
		} else {
			holder.tv3.setText("");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String convertToString(final Cursor cursor) {
		final String name = cursor.getString(INDEX_NAME);
		final String number = cursor.getString(INDEX_NUMBER);
		if (TextUtils.isEmpty(name)) {
			return cleanRecipient(number);
		}
		return name + " <" + cleanRecipient(number) + '>';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Cursor runQueryOnBackgroundThread(final CharSequence constraint) {
		String where = null;
		if (!TextUtils.isEmpty(constraint)) {
			String f = DatabaseUtils.sqlEscapeString('%' + constraint.toString() + '%');
			where = "(" + ContactsContract.Data.DISPLAY_NAME + " LIKE " + f + ") OR ("
					+ Phone.DATA1 + " LIKE " + f + ")";
			if (prefsMobilesOnly) {
				where = DbUtils.sqlAnd(where, Phone.TYPE + " = " + Phone.TYPE_MOBILE);
			}
		}

		final Cursor cursor = this.mContentResolver.query(Phone.CONTENT_URI, PROJECTION, where,
				null, SORT);
		return cursor;
	}

	/**
	 * @param b
	 *            set to true, if only mobile numbers should be displayed.
	 */
	static final void setMoileNubersObly(final boolean b) {
		prefsMobilesOnly = b;
	}

	/**
	 * Clean recipient's phone number from [ -.()<>].
	 * 
	 * @param recipient
	 *            recipient's mobile number
	 * @return clean number
	 */
	public static String cleanRecipient(final String recipient) {
		if (TextUtils.isEmpty(recipient)) {
			return "";
		}
		String n;
		int i = recipient.indexOf("<");
		int j = recipient.indexOf(">");
		if (i != -1 && i < j) {
			n = recipient.substring(recipient.indexOf("<"), recipient.indexOf(">"));
		} else {
			n = recipient;
		}
		return n.replaceAll("[^*#+0-9]", "").replaceAll("^[*#][0-9]*#", "");
	}
}
