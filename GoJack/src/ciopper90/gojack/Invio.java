package ciopper90.gojack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import ciopper90.gojack.utility.Servizio;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.EditText;

public class Invio {
	private static DefaultHttpClient httpclient;
	private HttpPost httppost;
	public Invio(){
		HttpParams httpParameters = new BasicHttpParams();
		int timeoutConnection = 200000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		int timeoutSocket = 205000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		httpclient = new DefaultHttpClient(httpParameters);	
	}


	public static String aggiorna(String url){
		try {
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 200000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 205000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			DefaultHttpClient httpclient1 = new DefaultHttpClient(httpParameters);
			HttpGet httpget=new HttpGet(url);
			HttpResponse response;
			response = httpclient1.execute(httpget);
			String returnString=EntityUtils.toString(response.getEntity());	
			Log.d("return", returnString);
			return returnString.substring(returnString.indexOf("<txt>")+5, returnString.indexOf("/txt")-1);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return "Errore Rete";
		}

		return "Aggiornamento non riuscito";
	}
	public static byte[] scarica(String url){
		try {
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 200000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 205000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			DefaultHttpClient httpclient1 = new DefaultHttpClient(httpParameters);
			HttpGet httpget=new HttpGet(url);
			HttpResponse response;
			response = httpclient1.execute(httpget);
			//String returnString=EntityUtils.toString(response.getEntity());
			byte[] returnString=EntityUtils.toByteArray(response.getEntity());	
			//Log.d("return", returnString);
			return returnString;//.substring(returnString.indexOf("<txt>")+5, returnString.indexOf("/txt")-1);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return null;
		}
		return null;
		//return "Download non riuscito";
	}
	public String InvioSms(Servizio s,EditText number,EditText testo,Context context) {
		// Create a new HttpClient and Post Header
		String url=s.getUrl();
		//url.replaceAll(, substitute)


		httppost = new HttpPost(url);

		try {

			//recuperare servizio di invio
			String primo=new String(s.getPrimo());
			String secondo=new String(s.getSecondo());
			String terzo=new String(s.getTerzo());
			@SuppressWarnings("unused")
			String quarto=new String(s.getQuarto());
			String servizio=new String(s.getServizio());

			String body=testo.getText().toString();
			String to;
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

			//estraggo numero :D
			to=number.getText().toString();
			int inizio=to.indexOf("<");
			if(inizio!=-1){
				to=to.substring(inizio+1, to.length()-1);
			}
			else{
				if(!to.contains("@")){
					if(to.charAt(0)!='+'&&(to.charAt(0)!='0'&&to.charAt(1)!='0')){
						to="+39"+to;
					}
				}

			}
			nameValuePairs.add(new BasicNameValuePair("user", primo));
			nameValuePairs.add(new BasicNameValuePair("pass", secondo));
			nameValuePairs.add(new BasicNameValuePair("nick", terzo));//+"$$"+quarto));
			nameValuePairs.add(new BasicNameValuePair("rcpt", to));
			nameValuePairs.add(new BasicNameValuePair("lang", "it"));
			nameValuePairs.add(new BasicNameValuePair("text", body));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = httpclient.execute(httppost);
			String returnString=EntityUtils.toString(response.getEntity());
			returnString.concat("");
			return returnString;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			return "errore rete";
		}
		return "errore";
	}

	public String InvioCaptchaSms(Servizio s,String captcha,EditText number,EditText testo) {
		// Create a new HttpClient and Post Header
		@SuppressWarnings("unused")
		String url=s.getUrl();
		try {


			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("ic", captcha));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = httpclient.execute(httppost);

			String returnString=EntityUtils.toString(response.getEntity());
			return returnString;

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			return "errore rete";
		}
		return "errore";
	}


}
