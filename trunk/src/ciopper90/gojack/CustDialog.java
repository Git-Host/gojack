package ciopper90.gojack;
/*package ciopper90.o2Ireland;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CustDialog {



	public class CustomDialog2 extends Activity {
	    static final int CUSTOM_DIALOG = 0;
	    Button button;
	    AlertDialog alertDialog;
	   
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);

	        // Creiamo un semplice bottone che faccia visualizzare la finestra di dialogo
	        button = (Button) findViewById(R.id.btn);
	        button.setOnClickListener(new OnClickListener(){
	            public void onClick(View v) {
	                showDialog(CUSTOM_DIALOG);
	            }
	        }); 
	    }
	    protected Dialog onCreateDialog(int id) {
	    	AlertDialog alertDialog;
	    	switch(id) {
	        case CUSTOM_DIALOG:
	        	AlertDialog.Builder builder;
	        	LayoutInflater inflater = (LayoutInflater) CustomDialog2.this.getSystemService(LAYOUT_INFLATER_SERVICE);
	        	View layout = inflater.inflate(R.layout.custom_dialog,(ViewGroup) findViewById(R.id.layout_root));
	        	TextView text = (TextView) layout.findViewById(R.id.text);
	        	text.setText("Ciao, questo è un custom dialog!");
	        	ImageView image = (ImageView) layout.findViewById(R.id.image);
	        	image.setImageResource(R.drawable.android);
	        	builder = new AlertDialog.Builder(CustomDialog2.this);
	        	builder.setView(layout);
	        	builder.setCancelable(true);
	        	alertDialog = builder.create();
	            break;
	        default:
	        	alertDialog = null;
	        }
	        return alertDialog;
	    }

}*/
