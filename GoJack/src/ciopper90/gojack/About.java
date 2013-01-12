package ciopper90.gojack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
//import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class About extends Activity {
	private String string;
	AlertDialog.Builder builder;
	private SharedPreferences prefs;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.i(TAG,"onCreate()");
		setContentView(R.layout.about);
		TextView aboutText = (TextView) findViewById(R.id.about_content);
		string = getResources().getString(R.string.version);
		aboutText.setText(getResources().getString(R.string.about_text_pre) + string + getResources().getString(R.string.about_text_post));
		Linkify.addLinks(aboutText, Linkify.EMAIL_ADDRESSES);
		//controllo nuova versione
		builder=new AlertDialog.Builder(this);
		
		
		prefs = getSharedPreferences("SmsInviati", Context.MODE_PRIVATE);
		int sms = Integer.parseInt(prefs.getString("SmsSend","0"));
		TextView smsText = (TextView) findViewById(R.id.sms_send);
		smsText.setText("SMS Inviati: "+sms);
		
		
		
		Button chk=(Button)findViewById(R.id.button1);
		chk.setOnClickListener(new Button.OnClickListener(){

			public void onClick(View arg0) {

				DefaultHttpClient httpclient = new DefaultHttpClient();	
				HttpPost httppost = new HttpPost("http://ciopper90.altervista.org/php5/gofree/gojackcheck.php");
				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair("versione", string ));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse response = httpclient.execute(httppost);
					String returnString=EntityUtils.toString(response.getEntity());

					if(returnString.contains("nuova versione")){
						builder.setTitle("Nuova versione");
						builder.setMessage("Versione disponibile: "+returnString.substring(14));
						builder.setCancelable(false);
						builder.setPositiveButton("Scarica!",new OnClickListener(){
							public void onClick(DialogInterface dialog, int id){
								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://ciopper90.altervista.org/php5/gofree/gojackdown.php"));
								startActivity(intent);
							}
						});
						builder.setNegativeButton("Annulla!",new OnClickListener(){
							public void onClick(DialogInterface dialog, int id){
							}
						});
						builder.show();
					}else
						Toast.makeText(getApplicationContext(), "Nessuna Aggiornamento Disponibile", Toast.LENGTH_LONG).show();

				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
				} catch (IOException e) {

				}

			}});





	}

}
