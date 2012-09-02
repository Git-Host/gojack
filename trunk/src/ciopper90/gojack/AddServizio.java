package ciopper90.gojack;

import java.util.ArrayList;

import ciopper90.gojack.utility.DatabaseService;
import ciopper90.gojack.utility.Servizio;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddServizio extends Activity{
	private EditText primo,secondo,terzo,quarto,firma,name;
	@SuppressWarnings("unused")
	private TextView lb_primo,lb_secondo,lb_terzo,lb_quarto,lb_firma,lb_name;
	@SuppressWarnings("unused")
	private ArrayList<Servizio> service;
	@SuppressWarnings("unused")
	private Servizio s;
	@SuppressWarnings("unused")
	private int pos;
	private DatabaseService db;
	private String dati[];
	private String parametri[];
	private String config[];
	private String id;
	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addservizio);
		db=MainActivity.db;
		
		
		Bundle i=getIntent().getExtras();
		id=i.getString("id");
		dati=i.getStringArray("dati");
		parametri=i.getStringArray("parametri");
		config=i.getStringArray("config");
		
		
		name=(EditText)findViewById(R.id.name);
		name.setText(config[0]);
		
		primo=(EditText)findViewById(R.id.EditText1);
		secondo=(EditText)findViewById(R.id.EditText2);
		terzo=(EditText)findViewById(R.id.EditText3);
		quarto=(EditText)findViewById(R.id.EditText4);
		firma=(EditText)findViewById(R.id.EditText5);
		
		lb_primo=(TextView)findViewById(R.id.textView1);
		lb_secondo=(TextView)findViewById(R.id.textView2);
		lb_terzo=(TextView)findViewById(R.id.textView3);
		lb_quarto=(TextView)findViewById(R.id.textView4);
		lb_firma=(TextView)findViewById(R.id.textView5);
		int count=0;
		for(int k=1;k<(parametri.length-1);k++){
			if(parametri[k]!=null){
				count++;
			}
		}
		count++;
		switch(count){
		case 1:
		case 2:lb_secondo.setVisibility(View.GONE);
			   secondo.setVisibility(View.GONE);
		case 3:lb_terzo.setVisibility(View.GONE);
		       terzo.setVisibility(View.GONE);
		case 4:lb_quarto.setVisibility(View.GONE);
		       quarto.setVisibility(View.GONE);
		}
		
		switch((count-1)){
		case 4:if(dati[3].equals("2")){
			   		parametri[4]=parametri[4].concat("[OPZIONALE]");
			   }
		       lb_quarto.setText(parametri[4]);
		case 3:if(dati[2].equals("2")){
	   		   		parametri[3]=parametri[3].concat("[OPZIONALE]");
	           }
		       lb_terzo.setText(parametri[3]);
		case 2:if(dati[1].equals("2")){
					parametri[2]=parametri[2].concat("[OPZIONALE]");
	           }
		       lb_secondo.setText(parametri[2]);
		case 1:if(dati[0].equals("2")){
	   		   		parametri[1]=parametri[1].concat("[OPZIONALE]");
	           }
			   lb_firma.setText("Firma[OPZIONALE]");
		       lb_primo.setText(parametri[1]);
		}

		if(config.length>3){
			   primo.setText(config[2]);
			   secondo.setText(config[3]);
			   terzo.setText(config[4]);
			   quarto.setText(config[5]);
		       firma.setText(config[6]);
		}
		
		Button salva=(Button)findViewById(R.id.Salva);
		salva.setOnClickListener(new Button.OnClickListener(){

			public void onClick(View arg0) {
				if(controlla()){
					db.open();
					if(config.length<3)
					{
						save();					
					}else{
						modify();
				}
					db.close();
				}
			}

		});	
		}
		
	
	protected void modify() {
		db.UpdateService(id,name.getText().toString(), primo.getText().toString(), secondo.getText().toString(), terzo.getText().toString(), quarto.getText().toString(), config[1],firma.getText().toString());
		//service.add(new Servizio(name.getText().toString(), user.getText().toString(), pass.getText().toString(),url.getText().toString()));
		//salva(service);
		Toast.makeText(getBaseContext(), "Dati Modificati", Toast.LENGTH_LONG).show();
		this.finish();
		
	}


	protected void save() {
		// questo metodo dovrebbe salvare l'account o2
		db.insertService(config[0], primo.getText().toString(), secondo.getText().toString(), terzo.getText().toString(), quarto.getText().toString(), config[1],firma.getText().toString());
		Toast.makeText(getBaseContext(), "Dati Salvati", Toast.LENGTH_LONG).show();
		this.finish();
	}


	private boolean controlla() {
		if(name.getText().length()==0){
			Toast.makeText(getApplicationContext(), "Errore: Manca Nome Servizio", Toast.LENGTH_LONG).show();
			return false;
		}
			if(primo.getText().length()==0){
				Toast.makeText(getApplicationContext(), "Errore: Manca "+parametri[1], Toast.LENGTH_LONG).show();
				return false;
			}
			if((secondo.getVisibility()!=View.GONE)&&(secondo.getText().length()==0)&&(!dati[1].equals("2"))){
				Toast.makeText(getApplicationContext(), "Errore: Manca la "+parametri[2], Toast.LENGTH_LONG).show();
				return false;
			}
			if((terzo.getVisibility()!=View.GONE)&&(terzo.getText().length()==0)&&(!dati[2].equals("2"))){
				Toast.makeText(getApplicationContext(), "Errore: Manca la "+parametri[3], Toast.LENGTH_LONG).show();
				return false;
			}
			if((quarto.getVisibility()!=View.GONE)&&(quarto.getText().length()==0)&&(!dati[3].equals("2"))){
				Toast.makeText(getApplicationContext(), "Errore: Manca la "+parametri[4], Toast.LENGTH_LONG).show();
				return false;
			}
			return true;
		}


}
