package it.ciopper90.gojack2.utils;

import it.ciopper90.gojack2.R;
import it.ciopper90.gojack2.added.WorkServizio;

import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class WSInterface {

	private static WorkServizio ws;
	private static Spinner spinner;
	private static SpinnerAdapter spinneradapter;
	private static ArrayList<Servizio> service;

	public static Spinner setSpinner(final Context context) {
		ws = new WorkServizio(context);
		service = ws.caricaServizio();
		spinner = new Spinner(context);
		ArrayList<String> lista = new ArrayList<String>();
		for (int i = 0; i < service.size(); i++) {
			lista.add(service.get(i).getName());
		}
		spinneradapter = new ArrayAdapter<String>(context, R.layout.textview, lista);
		spinner.setAdapter(spinneradapter);
		return spinner;
	}

	public static void saveService(final String to, final String selectedItem) {
		ws.saveService(to, selectedItem);
	}

	public static ArrayList<Servizio> getService() {
		return service;
	}

	public static int PositionServizioNumero(final String number) {
		ArrayList<String> lista = new ArrayList<String>();
		for (int i = 0; i < service.size(); i++) {
			lista.add(service.get(i).getName());
		}
		int pos = 0;
		String serv = ws.ServizioNumero(number);
		if (serv != null) {
			// int a = lista.indexOf(serv);
			pos = lista.indexOf(serv);
		}
		if (pos == -1) {
			return 0;
		} else {
			return pos;
		}
	}

}
