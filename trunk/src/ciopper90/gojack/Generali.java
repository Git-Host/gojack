package ciopper90.gojack;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;


public class Generali extends Activity{
	private SharedPreferences prefs;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.setting_2);
		setContentView(R.layout.setting);
		prefs=this.getSharedPreferences("Setting", Context.MODE_PRIVATE);
		int stat = prefs.getInt("lastnum",0);
		final CheckBox c=(CheckBox)findViewById(R.id.checkBox1);
		if(stat==1)
			c.setChecked(true);
		else
			c.setChecked(false);

		c.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View arg0) {
				SharedPreferences.Editor editor = prefs.edit();
				if(c.isChecked())
					editor.putInt("lastnum", 1);
				else
					editor.putInt("lastnum", 0);
				editor.commit();	
				//Toast.makeText(getApplicationContext(), "Salvate Impostazioni", Toast.LENGTH_SHORT).show();
				//finish();
			}
		});
		stat = prefs.getInt("salvasmsinviati",0);
		final CheckBox d=(CheckBox)findViewById(R.id.checkBox2);
		if(stat==1)
			d.setChecked(true);
		else
			d.setChecked(false);

		c.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View arg0) {
				SharedPreferences.Editor editor = prefs.edit();
				if(d.isChecked())
					editor.putInt("salvasmsinviati", 1);
				else
					editor.putInt("salvasmsinviati", 0);
				editor.commit();	
				//Toast.makeText(getApplicationContext(), "Salvate Impostazioni", Toast.LENGTH_SHORT).show();
				//finish();
			}
		});
	}	
}
