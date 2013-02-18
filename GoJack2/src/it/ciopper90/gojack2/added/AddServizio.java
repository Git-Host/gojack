package it.ciopper90.gojack2.added;

import it.ciopper90.gojack2.R;
import it.ciopper90.gojack2.utils.DatabaseService;
import it.ciopper90.gojack2.utils.Servizio;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddServizio extends Activity {
	private EditText primo, secondo, terzo, quarto, firma, name;
	@SuppressWarnings("unused")
	private TextView lb_primo, lb_secondo, lb_terzo, lb_quarto, lb_firma, lb_name;
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
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.addservizio);
		this.db = new DatabaseService(this.getApplicationContext());

		Bundle i = this.getIntent().getExtras();
		this.id = i.getString("id");
		this.dati = i.getStringArray("dati");
		this.parametri = i.getStringArray("parametri");
		this.config = i.getStringArray("config");

		this.name = (EditText) this.findViewById(R.id.name);
		this.name.setText(this.config[0]);

		this.primo = (EditText) this.findViewById(R.id.EditText1);
		this.secondo = (EditText) this.findViewById(R.id.EditText2);
		this.terzo = (EditText) this.findViewById(R.id.EditText3);
		this.quarto = (EditText) this.findViewById(R.id.EditText4);
		this.firma = (EditText) this.findViewById(R.id.EditText5);

		this.lb_primo = (TextView) this.findViewById(R.id.textView1);
		this.lb_secondo = (TextView) this.findViewById(R.id.textView2);
		this.lb_terzo = (TextView) this.findViewById(R.id.textView3);
		this.lb_quarto = (TextView) this.findViewById(R.id.textView4);
		this.lb_firma = (TextView) this.findViewById(R.id.textView5);
		int count = 0;
		for (int k = 1; k < (this.parametri.length - 1); k++) {
			if (this.parametri[k] != null) {
				count++;
			}
		}
		count++;
		switch (count) {
		case 1:
		case 2:
			this.lb_secondo.setVisibility(View.GONE);
			this.secondo.setVisibility(View.GONE);
		case 3:
			this.lb_terzo.setVisibility(View.GONE);
			this.terzo.setVisibility(View.GONE);
		case 4:
			this.lb_quarto.setVisibility(View.GONE);
			this.quarto.setVisibility(View.GONE);
		}

		switch ((count - 1)) {
		case 4:
			if (this.dati[3].equals("2")) {
				this.parametri[4] = this.parametri[4].concat("[OPZIONALE]");
			}
			this.lb_quarto.setText(this.parametri[4]);
		case 3:
			if (this.dati[2].equals("2")) {
				this.parametri[3] = this.parametri[3].concat("[OPZIONALE]");
			}
			this.lb_terzo.setText(this.parametri[3]);
		case 2:
			if (this.dati[1].equals("2")) {
				this.parametri[2] = this.parametri[2].concat("[OPZIONALE]");
			}
			this.lb_secondo.setText(this.parametri[2]);
		case 1:
			if (this.dati[0].equals("2")) {
				this.parametri[1] = this.parametri[1].concat("[OPZIONALE]");
			}
			this.lb_firma.setText("Firma[OPZIONALE]");
			this.lb_primo.setText(this.parametri[1]);
		}

		if (this.config.length > 3) {
			this.primo.setText(this.config[2]);
			this.secondo.setText(this.config[3]);
			this.terzo.setText(this.config[4]);
			this.quarto.setText(this.config[5]);
			this.firma.setText(this.config[6]);
		}

		Button salva = (Button) this.findViewById(R.id.Salva);
		salva.setOnClickListener(new Button.OnClickListener() {

			public void onClick(final View arg0) {
				if (AddServizio.this.controlla()) {
					AddServizio.this.db.open();
					if (AddServizio.this.config.length < 3) {
						AddServizio.this.save();
					} else {
						AddServizio.this.modify();
					}
					AddServizio.this.db.close();
				}
			}

		});
	}

	protected void modify() {
		this.db.UpdateService(this.id, this.name.getText().toString(), this.primo.getText()
				.toString(), this.secondo.getText().toString(), this.terzo.getText().toString(),
				this.quarto.getText().toString(), this.config[1], this.firma.getText().toString());
		// service.add(new Servizio(name.getText().toString(),
		// user.getText().toString(),
		// pass.getText().toString(),url.getText().toString()));
		// salva(service);
		Toast.makeText(this.getBaseContext(), "Dati Modificati", Toast.LENGTH_LONG).show();
		this.finish();

	}

	protected void save() {
		// questo metodo dovrebbe salvare l'account o2
		this.db.insertService(this.config[0], this.primo.getText().toString(), this.secondo
				.getText().toString(), this.terzo.getText().toString(), this.quarto.getText()
				.toString(), this.config[1], this.firma.getText().toString());
		Toast.makeText(this.getBaseContext(), "Dati Salvati", Toast.LENGTH_LONG).show();
		this.finish();
	}

	private boolean controlla() {
		if (this.name.getText().length() == 0) {
			Toast.makeText(this.getApplicationContext(), "Errore: Manca Nome Servizio",
					Toast.LENGTH_LONG).show();
			return false;
		}
		if (this.primo.getText().length() == 0) {
			Toast.makeText(this.getApplicationContext(), "Errore: Manca " + this.parametri[1],
					Toast.LENGTH_LONG).show();
			return false;
		}
		if ((this.secondo.getVisibility() != View.GONE) && (this.secondo.getText().length() == 0)
				&& (!this.dati[1].equals("2"))) {
			Toast.makeText(this.getApplicationContext(), "Errore: Manca la " + this.parametri[2],
					Toast.LENGTH_LONG).show();
			return false;
		}
		if ((this.terzo.getVisibility() != View.GONE) && (this.terzo.getText().length() == 0)
				&& (!this.dati[2].equals("2"))) {
			Toast.makeText(this.getApplicationContext(), "Errore: Manca la " + this.parametri[3],
					Toast.LENGTH_LONG).show();
			return false;
		}
		if ((this.quarto.getVisibility() != View.GONE) && (this.quarto.getText().length() == 0)
				&& (!this.dati[3].equals("2"))) {
			Toast.makeText(this.getApplicationContext(), "Errore: Manca la " + this.parametri[4],
					Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

}
