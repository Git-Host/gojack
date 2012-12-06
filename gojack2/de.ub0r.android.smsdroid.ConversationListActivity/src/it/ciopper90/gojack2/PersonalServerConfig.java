package it.ciopper90.gojack2;

import it.ciopper90.gojack2.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PersonalServerConfig extends Activity {
	private SharedPreferences prefs;
	private EditText url;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.personalserverconfig);
		this.prefs = this.getSharedPreferences("Setting", Context.MODE_PRIVATE);
		String textData = this.prefs.getString("url", "http://");

		this.url = (EditText) this.findViewById(R.id.urlsp);
		this.url.setText(textData);

		Button b = (Button) this.findViewById(R.id.Salvasp);
		b.setOnClickListener(new Button.OnClickListener() {

			public void onClick(final View arg0) {
				SharedPreferences.Editor editor = PersonalServerConfig.this.prefs.edit();
				if (PersonalServerConfig.this.url.getText().toString().contains(" ")) {
					editor.putString("url", PersonalServerConfig.this.url.getText().toString()
							.replace(" ", ""));
				} else {
					editor.putString("url", PersonalServerConfig.this.url.getText().toString());
				}
				editor.commit();
				Toast.makeText(PersonalServerConfig.this.getApplicationContext(),
						"Salvato url predefinita", Toast.LENGTH_SHORT).show();
				PersonalServerConfig.this.finish();
			}
		});
	}

}
