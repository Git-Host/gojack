package ciopper90.gojack;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PersonalServerConfig extends Activity{
	private SharedPreferences prefs;
	private EditText url;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personalserverconfig);
		prefs = getSharedPreferences("Setting", Context.MODE_PRIVATE);
		String textData = prefs.getString("url","http://");

		
		url=(EditText)findViewById(R.id.urlsp);
		url.setText(textData);
		
		Button b=(Button)findViewById(R.id.Salvasp);
		b.setOnClickListener(new Button.OnClickListener(){

			public void onClick(View arg0) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("url", url.getText().toString());
                editor.commit();	
                Toast.makeText(getApplicationContext(), "Salvato url predefinita", Toast.LENGTH_SHORT).show();
                finish();
			}
			});
	}
	
}
