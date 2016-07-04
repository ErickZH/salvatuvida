package itch.salvatuvida;

import android.content.DialogInterface;
import android.support.v4.view.KeyEventCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Ajustes extends AppCompatActivity
{
    ListView lvAjustes;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        // mapeamos xml con java
        lvAjustes = (ListView) findViewById(R.id.lvAjustes);
        actualiza_list_view();

        // Asignamos evento a cada item del ListView
        lvAjustes.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                switch (position)
                {

                    case 0:
                        // Enviar SMS
                        Log.e("EZH","OnCreate - Enviar SMS");
                        lanzar_alerta("Enviar SMS en caso de auxilio a",1);
                        break;
                    case 1:
                        // Llamar a
                        Log.e("EZH","OnCreate - Llamar a");
                        lanzar_alerta("Llamar en caso de auxilio a",2);
                        break;
                }
            }
        });

    }
    public void lanzar_alerta(String titulo, int codigo_mensaje)
    {
        Log.e("EZH","Lanzar alerta - Codigo de mensaje: "+codigo_mensaje);
        AlertDialog ad = alerta(codigo_mensaje);
        Log.e("EZH","Lanzar alerta - Despues del alert");
        ad.setTitle(titulo);
        ad.setCancelable(true);
        ad.show();
    }

    public AlertDialog alerta(final int codigo_mensaje)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(Ajustes.this);

        LayoutInflater inflater = getLayoutInflater();

        View v = inflater.inflate(R.layout.alerta, null);
        final EditText et = (EditText) v.findViewById(R.id.numTelefono);

        // leemos el archivo correspodiente, para ver si ya existe algun numero guardado
        if(codigo_mensaje == 1)// leer sms.txt
        {
            try
            {
                Log.e("EZH","Ajustes - Leer sms.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput("sms.txt")));
                String linea = br.readLine();
                String texto_string = "";
                while(linea != null)
                {
                    texto_string += linea;
                    linea = br.readLine();
                }
                br.close();
                et.setText(texto_string);

            }catch(Exception ex)
            {
                Log.e("EZH-Error: ",ex.getMessage());
                et.setText("");
            }
        }
        else
        {
            // leer llamar.txt
            try
            {
                Log.e("EZH","Ajustes - Leer llamar.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput("llamar.txt")));
                String linea = br.readLine();
                String texto_string = "";
                while(linea != null)
                {
                    texto_string += linea;
                    linea = br.readLine();
                }
                br.close();
                et.setText(texto_string);

            }catch(Exception ex)
            {
                Log.e("EZH-Error: ",ex.getMessage());
                et.setText("");
            }
        }


        // eventos
        builder.setPositiveButton("Guardar",
                new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

               Log.e("EZH","Ajustes - Guardar");
                // validamos que el editText no este vacio
                if (!et.getText().toString().equals(""))
                {
                    Log.e("EZH","Ajustes - Guardar - No esta vacio el EditText");
                    // Guardamos numero telefonico
                    String numero_telefono = et.getText().toString();
                    // validamos que numero se va a guardar (SMS o Llamada)
                    if (codigo_mensaje == 1)
                    {
                        // guardar numero en el archivo sms.txt
                        try
                        {
                            OutputStreamWriter osw = new OutputStreamWriter(openFileOutput("sms.txt",MODE_PRIVATE));
                            osw.write(numero_telefono);
                            osw.flush();
                            osw.close();
                            Log.e("EZH","Ajustes - Guardar - Se guardo el numero para SMS");
                        }catch (Exception e)
                        {
                            Log.e("EZH","Error al guardar el número SMS"+numero_telefono);
                        }
                    }
                    else
                    {
                        // guardar numero en el archivo llamar.txt
                        try
                        {
                            OutputStreamWriter osw = new OutputStreamWriter(openFileOutput("llamar.txt",MODE_PRIVATE));
                            osw.write(numero_telefono);
                            osw.flush();
                            osw.close();
                        }catch (Exception e)
                        {
                            Log.e("EZH","Error al guardar el número Llamada"+numero_telefono);
                        }
                    }
                }
            }
        });

        builder.setView(v);
        return builder.create();
    }

    public void actualiza_list_view()
    {
        ArrayList<String> arreglo =
                new ArrayList<String>(2);
        arreglo.add("Enviar SMS a");
        arreglo.add("Llamar por telefono a");
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_list_item_1,
                        arreglo);
        lvAjustes.setAdapter(adapter);
    }
}
