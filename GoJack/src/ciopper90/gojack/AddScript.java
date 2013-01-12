package ciopper90.gojack;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import ciopper90.gojack.utility.DatabaseService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AddScript extends Activity{
	private EditText name,url;
	private DatabaseService db;

	//dati che servono all'activity successiva
	private String dati[];
	private String config[];
	private String parametri[];
	int count;
	private int errore;
	private SharedPreferences prefs;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addscript);
		//aggiungere shared preferences
		prefs = getSharedPreferences("Setting", Context.MODE_PRIVATE);
		String textData = prefs.getString("url","http://");

		db=MainActivity.db;
		db.open();
		errore=0;
		name=(EditText)findViewById(R.id.namesc);
		url=(EditText)findViewById(R.id.urlsc);
		url.setText(textData);
		config=new String[3];

		Button salva=(Button)findViewById(R.id.Salvasc);
		salva.setOnClickListener(new Button.OnClickListener(){

			public void onClick(View arg0) {
				//bisogna parsare il risultato del server
				//per capire come va configurato il servizio
				config[1]=url.getText().toString().replace(" ", "");
				config[0]=name.getText().toString();
				if(config[1].indexOf("&", config[1].indexOf("servizio"))!=-1){
					config[2]=(String) url.getText().toString().subSequence(config[1].indexOf("servizio=")+9, config[1].indexOf("&", config[1].indexOf("servizio=")));
				}else{
					config[2]=(String) url.getText().toString().subSequence(config[1].indexOf("servizio=")+9, config[1].length());	
				}
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost;
				if(config[1].contains("?servizio="))
					httppost = new HttpPost(config[1]+"&action=config");
				else
					httppost = new HttpPost(config[1]+"?action=config");
				HttpResponse response;
				try {
					response = httpclient.execute(httppost);
					String returnString=EntityUtils.toString(response.getEntity());
					if(returnString.indexOf("<config>")!=-1){
						if(returnString.indexOf("<res>")!=-1){
							//elaborare returnString per poi salvare il dato nel database
							dati=new String[6];
							dati[0]=(String) returnString.subSequence(returnString.indexOf("<nu>")+4, returnString.indexOf("</nu>"));
							dati[1]=(String) returnString.subSequence(returnString.indexOf("<np>")+4, returnString.indexOf("</np>"));
							dati[2]=(String) returnString.subSequence(returnString.indexOf("<nn>")+4, returnString.indexOf("</nn>"));
							dati[3]="0";//returnString.subSequence(returnString.indexOf("<nu>"+4), returnString.indexOf("</nu>"));
							dati[4]=(String) returnString.subSequence(returnString.indexOf("<mc>")+4, returnString.indexOf("</mc>"));
							if(returnString.indexOf("<mm>")!=-1)
								dati[5]=(String) returnString.subSequence(returnString.indexOf("<mm>")+4, returnString.indexOf("</mm>"));
							else
								dati[5]=(String) returnString.subSequence(returnString.indexOf("<mmm>")+5, returnString.indexOf("</mmm>"));	
							db.insertDataService(config[0], Integer.parseInt(dati[0]), Integer.parseInt(dati[1]), Integer.parseInt(dati[2]),Integer.parseInt(dati[3]), Integer.parseInt(dati[4]), Integer.parseInt(dati[5]), config[1]);
							//inserire elementi nel database
							//va ancora testata :D
							//rendere invisibili i 2 campi e aggiungere del testo ad un jlabel con su scritto i dati del servizio
							name.setVisibility(View.GONE);
							url.setVisibility(View.GONE);
							Button b=(Button)findViewById(R.id.Salvasc);
							b.setVisibility(View.GONE);
							TextView tw1=(TextView)findViewById(R.id.textView3sc);
							tw1.setVisibility(View.GONE);
							TextView tw=(TextView)findViewById(R.id.name1sc);
							String config=returnString.substring(returnString.indexOf("<t>")+3,returnString.indexOf("</t>"));
							tw.setText(config);
							count=0;
							for(int i=0;i<4;i++){
								if(dati[i].equals("1")||dati[i].equals("2"))
									count++;
							}
							parametri=new String[5];
							if(returnString.indexOf("<n1>")!=-1){
								switch(count){
								case 4:parametri[4]=(String) returnString.subSequence(returnString.indexOf("<n4>")+4, returnString.indexOf("</n4>"));
								case 3:parametri[3]=(String) returnString.subSequence(returnString.indexOf("<n3>")+4, returnString.indexOf("</n3>"));
								case 2:parametri[2]=(String) returnString.subSequence(returnString.indexOf("<n2>")+4, returnString.indexOf("</n2>"));
								case 1:parametri[1]=(String) returnString.subSequence(returnString.indexOf("<n1>")+4, returnString.indexOf("</n1>"));
								parametri[0]="Nome Servizio";
								}
							}else{
								parametri[0]="Nome Servizio";
								parametri[1]="Username:";
								parametri[2]="Password";
								parametri[3]="Nick";		
							}

							switch(count){
							case 1:db.insertParameterService(name.getText().toString(),parametri[1],null,null,null,url.getText().toString());
							break;
							case 2:db.insertParameterService(name.getText().toString(),parametri[1],parametri[2],null,null,url.getText().toString());
							break;
							case 3:db.insertParameterService(name.getText().toString(),parametri[1],parametri[2],parametri[3],null,url.getText().toString());
							break;
							case 4:db.insertParameterService(name.getText().toString(),parametri[1],parametri[2],parametri[3],parametri[4],url.getText().toString());
							break;
							}
							db.close();
						}else{
							errore=1;
						}
						
					}else{
						errore=1;
					}
					
					if(errore==1){
						TextView tw=(TextView)findViewById(R.id.textView00);
						tw.setText("Errore link");
						
					}


					//sulla pressione del tasto back deve essere caricata l'activity per l'aggiunta dei servizi impostando il servizio appena aggiunto :D


				} catch (ClientProtocolException e) {
					//primo caso
				} catch (IOException e) {
					System.out.println("Errore link inserito");
				}catch (IllegalArgumentException e) {
					TextView tw=(TextView)findViewById(R.id.textView00);
					tw.setText("Errore link inser");
				}
			}
		});
		

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK&&dati!=null) {
			Intent i=new Intent(this,AddServizio.class);
			i.putExtra("dati", dati);
			i.putExtra("parametri", parametri);
			i.putExtra("config", config);
			this.startActivity(i);
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
