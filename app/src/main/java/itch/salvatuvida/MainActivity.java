package itch.salvatuvida;


import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.telephony.SmsManager;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

public class MainActivity extends AppCompatActivity
{
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;
    ImageButton boton_voz;
    String frases_auxilio =  "(ayuda|auxilio|por favor ayuda|necesito ayuda|ayudenme|favor)";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //inicializamos el sdk de Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // mapeamos la parte grafica con la parte logica
        boton_voz = (ImageButton)findViewById(R.id.boton_voz);

        boton_voz.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // lanzar reconocimiento de voz
                startVoiceRecognitionActivity();
            }
        });
        // Asegurarnos que los archivos de ajustes existen
        // Archivo SMS
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput("sms.txt")));
        }catch (Exception e)
        {
            // no existe
            Toast.makeText(getApplicationContext(),"Necesitas tener configurado algun numero en Ajustes",Toast.LENGTH_SHORT).show();
            // pedir mediante un AlertDialog el numero y crear el archivo
            lanzar_alerta("Enviar SMS en caso de auxilio a",1);
        }
        // Archivo Llamar
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput("llamar.txt")));
        }catch (Exception e)
        {
            // no existe
            Toast.makeText(getApplicationContext(),"Necesitas tener configurado algun numero en Ajustes",Toast.LENGTH_SHORT).show();
            // pedir mediante un AlertDialog el numero y crear el archivo
            lanzar_alerta("Llamar en caso de auxilio a",2);
        }



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
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = getLayoutInflater();

        View v = inflater.inflate(R.layout.alerta, null);
        final EditText et = (EditText) v.findViewById(R.id.numTelefono);
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

    private void startVoiceRecognitionActivity()
    {
        // Definición del intent para realizar en análisis del mensaje
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Indicamos el modelo de lenguaje para el intent
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Definimos el mensaje que aparecerá
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Pida auxilio...");
        // Lanzamos la actividad esperando resultados
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    @Override
    //Recogemos los resultados del reconocimiento de voz
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 0 && resultCode == RESULT_OK)
        {
            Log.e("EZH","Metodo Respuesta 1");
            //Creamos un bitmap con la imagen recientemente
            //almacenada en la memoria
            Bitmap bMap = BitmapFactory.decodeFile(
                    Environment.getExternalStorageDirectory()+
                            "/SalvaTuVida/"+"salvatuvida.jpg");
            Log.e("EZH","Metodo Respuesta 2");
            // imagen que vamos a compartir en Facebook
            SharePhoto foto = new SharePhoto.Builder().setBitmap(bMap).build();
            Log.e("EZH","Metodo Respuesta 3");
            SharePhotoContent contenido = new SharePhotoContent.Builder().addPhoto(foto).build();
            Log.e("EZH","Metodo Respuesta 4");
            ShareDialog.show(MainActivity.this,contenido);
            Log.e("EZH","Metodo Respuesta 5");
        }
        else
        {

            //Si el reconocimiento a sido bueno
            if(requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK)
            {
                //El intent nos envia un ArrayList aunque en este caso solo
                //utilizaremos la pos.0
                ArrayList<String> matches = data.getStringArrayListExtra
                        (RecognizerIntent.EXTRA_RESULTS);
                //Separo el texto en palabras.
                String voz_usuario = matches.get(0);
                // Voz reconocida
                Toast.makeText(getApplicationContext(),voz_usuario,Toast.LENGTH_LONG).show();
                // Buscamos del texto reconocido las frases de auxilio
                Pattern p = Pattern.compile(frases_auxilio);
                Matcher m = p.matcher(voz_usuario);
                if(m.find())
                {
                    try
                    {
                        BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput("sms.txt")));
                        BufferedReader br2 = new BufferedReader(new InputStreamReader(openFileInput("llamar.txt")));
                    }catch (Exception e)
                    {
                        // no existe
                        Toast.makeText(getApplicationContext(),"Necesitas tener configurado algun numero en Ajustes",Toast.LENGTH_SHORT).show();
                    }
                    Log.e("EZH","MainActivity - Obteniendo ubicacion");
                    enviar_sms(mi_ubicacion());
                    llamar();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"No se pidio ayuda",Toast.LENGTH_LONG).show();
                }
            }
        }

    }
    public void llamar()
    {
        String num_telefono = "";
        // obtener le numero de telefono del archivo llamar.txt
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput("llamar.txt")));
            String linea = br.readLine();
            String numero = "";
            while(linea != null)
            {
                numero += linea;
                linea = br.readLine();
            }
            br.close();
            num_telefono = numero;
        }catch(Exception e)
        {
            Log.e("EZH - Error",e.getMessage());
        }
        // realizar llamada
        try
        {
            Log.e("EZH","Llamaar - 1: "+num_telefono+"*");
            Uri numero = Uri.parse("tel:"+num_telefono);
            Log.e("EZH","Llamaar - 2");
            Intent intent = new Intent(Intent.ACTION_CALL,numero);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
            startActivityForResult(intent,0);
        }catch (ActivityNotFoundException a)
        {
            Log.e("EZH","No se pudo realizar la llamada");
        }
    }


    public void enviar_sms(String ubicacion)
    {
        String mensaje = "";
        String num_telefono = "";
        // si ubicacion es Activar GPS - mandar msj (Necesito auxilio)
        if (ubicacion.equals("Activar GPS")) {
            mensaje = "Necesito ayuda por favor !!";
        } else
        {
            // mandar msj y ubicacion
            mensaje = "Hola, ven ayudarme!! "+ubicacion;
        }
        // obtener le numero de telefono del archivo sms.txt
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput("sms.txt")));
            String linea = br.readLine();
            String numero = "";
            while(linea != null)
            {
                numero += linea;
                linea = br.readLine();
            }
            br.close();
            num_telefono = numero;
        }catch(Exception e)
        {
            Log.e("EZH - Error",e.getMessage());
        }
        // mandar msj SMS
        Log.e("EZH",num_telefono+"*");
        Log.e("EZH",mensaje+"**");
        sendSMS(num_telefono,mensaje);

    }
    private void sendSMS(String numeroTelefono, String mensaje)
    {
        Log.e("EZH","sendSMS 1");
        SmsManager sms = SmsManager.getDefault();
        Log.e("EZH","sendSMS 2");
        sms.sendTextMessage(numeroTelefono,null,mensaje,null,null);
        Log.e("EZH","sendSMS 3");
    }
    public String mi_ubicacion()
    {

        Log.e("EZH","MainActivity - mi_ubicacion 1");
        // si esta desactivado el GPS del celular
            // instancia de la clase ServicioGPS
        ServicioGPS servicioGPS = new ServicioGPS(this);
        Log.e("EZH","MainActivity - mi_ubicacion 2");
        Log.e("EZH","UBICACION: "+servicioGPS.get_ubicacion());
        Log.e("EZH","MainActivity - mi_ubicacion 3");
        return servicioGPS.get_ubicacion();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Intent intent = new Intent(getApplicationContext(),Ajustes.class);
            startActivity(intent);
        }
        else if(id == R.id.action_foto)
        {
            Log.e("EZH","Capturar foto 1");
            capturar_foto();
            Log.e("EZH","Capturar foto 2");
        }

        return super.onOptionsItemSelected(item);
    }
    public void capturar_foto()
    {
        Log.e("EZH","Metodo capturar_foto 1");
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        //Creamos una carpeta en la memeria del terminal
        Log.e("EZH","Metodo capturar_foto 2");
        File imagesFolder = new File(
                Environment.getExternalStorageDirectory(), "SalvaTuVida");
        Log.e("EZH","Metodo capturar_foto 3");
        imagesFolder.mkdirs();
        Log.e("EZH","Metodo capturar_foto 4");
        //añadimos el nombre de la imagen
        File image_file = new File(imagesFolder, "salvatuvida.jpg");
        Log.e("EZH","Metodo capturar_foto 5");
        Uri uriSavedImage = Uri.fromFile(image_file);
        Log.e("EZH","Metodo capturar_foto 6");

        //Le decimos al Intent que queremos grabar la imagen
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
        Log.e("EZH","Metodo capturar_foto 7");
        //Lanzamos la aplicacion de la camara con retorno (forResult)
        startActivityForResult(intent, 0);
        Log.e("EZH","Metodo capturar_foto 8");
    }

}
