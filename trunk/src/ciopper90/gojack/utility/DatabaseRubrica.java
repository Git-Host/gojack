package ciopper90.gojack.utility;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class DatabaseRubrica {
	
	public static String[] getFieldbyArr(Context context){
		ContentResolver localContentResolver = context.getContentResolver();
		
		Cursor cursor = localContentResolver.query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},null,null,null);
		
		if(cursor.getCount() >0){
	        String[] str = new String[cursor.getCount()];
	        int i = 0;
	        while (cursor.moveToNext()){
	           str[i] = new Contatto(cursor.getString(0),cursor.getString(1)).toString();
	           i++;
	        }
	        return str;
	    }else{
	     return new String[] {};
	    }
	}
}
