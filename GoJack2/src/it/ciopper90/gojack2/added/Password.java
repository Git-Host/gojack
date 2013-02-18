package it.ciopper90.gojack2.added;

import it.ciopper90.gojack2.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Password extends Activity {
	private SharedPreferences prefs;
	private EditText url;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.password);
		this.prefs = this.getSharedPreferences("Setting", Context.MODE_PRIVATE);
		String textData = this.prefs.getString("passw", "");

		this.url = (EditText) this.findViewById(R.id.password);
		this.url.setText(textData);

		Button b = (Button) this.findViewById(R.id.Salvasp);
		b.setOnClickListener(new Button.OnClickListener() {

			public void onClick(final View arg0) {
				SharedPreferences.Editor editor = Password.this.prefs.edit();
				editor.putString("passw", Password.this.url.getText().toString());
				editor.commit();
				Toast.makeText(Password.this.getApplicationContext(),
						"Salvata Password predefinita", Toast.LENGTH_SHORT).show();
				Password.this.finish();
			}
		});
	}

}
