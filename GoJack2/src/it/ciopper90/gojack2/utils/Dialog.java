package it.ciopper90.gojack2.utils;

import it.ciopper90.gojack2.R;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

public class Dialog {
	private static EditText captcha;

	public static ProgressDialog ProgDialog(final Context context, final String message) {
		ProgressDialog pd = new ProgressDialog(context);
		pd.setMessage(message);
		pd.setCancelable(false);
		return pd;
	}

	public static Builder ErrorDialog(final Context context, final String Message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Errore");
		builder.setMessage(Message);
		builder.setCancelable(false);
		return builder;
	}

	public static Builder RestoreDialog(final Activity context, final String alert) {
		if (alert.equals("captcha")) {
			AlertDialog.Builder builder;
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.dialog,
					(ViewGroup) context.findViewById(R.id.layout_root));
			ImageView image = (ImageView) layout.findViewById(R.id.image);
			Bitmap bMap = null;
			try {
				bMap = BitmapFactory.decodeStream(context.openFileInput("captcha.jpg"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			captcha = (EditText) layout.findViewById(R.id.text);
			DisplayMetrics metrics = new DisplayMetrics();
			context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			// Log.d("misure",
			// "image " + bMap.getHeight() + ":" + bMap.getWidth() + " spazio "
			// + image.getHeight() + " " + image.getWidth());
			double rapporto = bMap.getWidth() / (double) bMap.getHeight();
			bMap = Bitmap.createScaledBitmap(bMap, (int) (metrics.widthPixels * 0.9),
					(int) (((int) (metrics.widthPixels * 0.9)) / rapporto), true);
			image.setImageBitmap(bMap);
			builder = new AlertDialog.Builder(context);
			builder.setView(layout);
			builder.setCancelable(false);
			return builder;
		}
		return null;
	}

	public static String getCaptcha() {
		return captcha.getText().toString();
	}
}
