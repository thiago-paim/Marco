package com.example.marco;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import base.Trip;
import database.DataBaseMarco;

public class CreateTripActivity extends AppCompatActivity {
    protected EditText nome;
    protected DatePicker partida;
    protected TimePicker inicio, fim;
    protected EditText orcamento;
    protected int currentapiVersion;
    private FirebaseAuth mAuth;
    private DataBaseMarco banco;
    private DBOpenHelper dbhelper;
    Trip viagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        inicio = (TimePicker) findViewById(R.id.timeBeginPicker);
        fim = (TimePicker) findViewById(R.id.durationPicker);
        fim.setIs24HourView(true);
        inicio.setIs24HourView(true);
        currentapiVersion = android.os.Build.VERSION.SDK_INT;
        // O método setCurrentHour está obsoleto, mas o setHour é novo demais pro nosso minSDK
        // Precisa testar em outros aparelhos!!!
        if (currentapiVersion >= 23) {
            inicio.setHour(8);
            inicio.setMinute(0);
            fim.setHour(17);
            fim.setMinute(0);
        } else {
            inicio.setCurrentHour(8);
            inicio.setCurrentMinute(0);
            fim.setCurrentHour(17);
            fim.setCurrentMinute(0);
        }
        nome = (EditText) findViewById(R.id.tripNameField);
        partida = (DatePicker) findViewById(R.id.tripStartDateField);
        orcamento = (EditText) findViewById(R.id.dailyBudgetField);
    }

    protected void onDestroy(){
        dbhelper.getWritableDatabase().close();
        dbhelper.getReadableDatabase().close();
        super.onDestroy();
    }

    public void confirmTrip(View view) {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Date data = new Date(partida.getYear(), partida.getMonth(), partida.getDayOfMonth());
            banco = new DataBaseMarco();

            String timeStart;
            String timeEnd;
            if (currentapiVersion >= 23) {
                timeStart = inicio.getHour() + ":" + inicio.getMinute();
                timeEnd = fim.getHour() + ":" + fim.getMinute();
            } else {
                timeStart = inicio.getCurrentHour() + ":" + inicio.getCurrentMinute();
                timeEnd = fim.getCurrentHour() + ":" + fim.getCurrentMinute();
            }

            //criacao do objeto que cria a viagem.
            if (nome.getText().length() != 0 && orcamento.getText().length() != 0) {

                Double orcam = Double.parseDouble(orcamento.getText().toString());
                viagem = new Trip(nome.getText().toString(), orcam, "Recife", data, null, timeStart, timeEnd);
                dbhelper = new DBOpenHelper(getApplicationContext());
                ContentValues values = new ContentValues();
                values.put("Nome",nome.getText().toString());
                String formattedDate = new SimpleDateFormat("dd/MM/yyyy").format(data);
                values.put("DataInicio",formattedDate);
                values.put("Destino","Recife");
                values.put("HoraInicio",timeStart);
                values.put("HoraFinal",timeEnd);
                values.put("Orcamento",orcam);
                dbhelper.getWritableDatabase().insert(DBOpenHelper.TRIP,null,values);
                banco.createTrip(viagem);
                values.clear();
                Intent intent = new Intent(this, TravelCardsActivity.class);
                intent.putExtra("TRIP_TIME_START",timeStart);
                intent.putExtra("TRIP_TIME_END", timeEnd);
                intent.putExtra("TRIP_ORCAMENTO", orcam);
                startActivity(intent);
            } else {
                Toast.makeText(CreateTripActivity.this, "Preencha todos os campos obrigatórios para prosseguir", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(CreateTripActivity.this, "Realize Login para poder criar seu Roteiro personalizado", Toast.LENGTH_SHORT).show();
        }

    }
}
