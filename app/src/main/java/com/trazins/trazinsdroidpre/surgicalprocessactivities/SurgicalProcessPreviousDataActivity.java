package com.trazins.trazinsdroidpre.surgicalprocessactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.threepin.fireexit_wcf.Configurator;
import com.threepin.fireexit_wcf.FireExitClient;
import com.trazins.trazinsdroidpre.R;
import com.trazins.trazinsdroidpre.SurgicalProcessActivity;
import com.trazins.trazinsdroidpre.models.operationroom.OperationRoomInputModel;
import com.trazins.trazinsdroidpre.models.operationroom.OperationRoomOutputModel;
import com.trazins.trazinsdroidpre.models.sp_materialmodel.SP_MaterialOutputModel;
import com.trazins.trazinsdroidpre.models.surgicalprocessmodel.SurgicalProcessOutputModel;
import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;
import com.trazins.trazinsdroidpre.utils.ConnectionParameters;
import com.trazins.trazinsdroidpre.utils.ErrorLogWriter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SurgicalProcessPreviousDataActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 77;
    private EditText editTextRecordNumber, editTextInterventionCode, editTextDeliveryNote, editTextInterventionDate;
    private TextView textViewUserName;
    private Spinner spinnerOperationRoom;
    private BottomNavigationView bnv;

    private Switch switchUrgent;
    List<OperationRoomOutputModel> OperationRoomList;

    private boolean isUpdate = false;

    //Usuario logeado
    UserOutputModel userLogged;

    String activityName;

    //Proceso Quirúrgico
    SurgicalProcessOutputModel surgicalProcess = new SurgicalProcessOutputModel();

    Handler handler;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surgical_process_previous_data);

        handler = new Handler(Looper.getMainLooper());

        this.activityName= this.getClass().getSimpleName();

        //ajustar tamaño porque la fecha de intervención no se ve

        editTextInterventionCode = findViewById(R.id.editTextInterventionCode);
        editTextRecordNumber = findViewById(R.id.editTextRecordNumber);
//        editTextDeliveryNote = findViewById(R.id.editTextDeliveryNote);

        editTextInterventionDate = findViewById(R.id.editTextInterventionDate);
        editTextInterventionDate.setInputType(InputType.TYPE_NULL);
        spinnerOperationRoom = findViewById(R.id.spinnerOperationRoom);
        bnv = findViewById(R.id.bottomSPPDNavigationMenu);
        switchUrgent = findViewById(R.id.switchSPUrgent);

        LinearLayout layoutRegularization = findViewById(R.id.layoutRegularization);
        TextView textViewInterventionDate = findViewById(R.id.textViewInterventionDate);
        EditText editTextInterventionDate = findViewById(R.id.editTextInterventionDate);

        layoutRegularization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Muestra los campos
//                textViewInterventionDate.setVisibility(View.VISIBLE);
//                editTextInterventionDate.setVisibility(View.VISIBLE);

                // O, si quieres alternar (mostrar/ocultar), podrías usar:
                 int currentVisibility = textViewInterventionDate.getVisibility();
                 int newVisibility = (currentVisibility == View.VISIBLE) ? View.GONE : View.VISIBLE;
                 textViewInterventionDate.setVisibility(newVisibility);
                 editTextInterventionDate.setVisibility(newVisibility);
            }
        });

        //Usuario loggeado
        this.userLogged = (UserOutputModel)getIntent().getSerializableExtra("userLogged");

        this.surgicalProcess =(SurgicalProcessOutputModel)getIntent().getSerializableExtra("surgicalProcess");

        textViewUserName = findViewById(R.id.textViewSPUserName);
        textViewUserName.setText(getString(R.string.identified_user) + " " + userLogged.UserName);

        OperationRoomList = new ArrayList<OperationRoomOutputModel>();

        new OperationRoomAsyncClass().execute();

        //Así sabemos si el foco viene de uno nuevo o una modificación de uno ya creado
        if(this.surgicalProcess == null){
            this.surgicalProcess = new SurgicalProcessOutputModel();
        }else{
            setValuesOfRecoveredSP(this.surgicalProcess);
        }

        editTextInterventionDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimeDialog(editTextInterventionDate);
            }
        });

        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.add_material){
                    openSurgicalProcessActivity();
                }else{
                    onBackPressed();
                }
                return false;
            }
        });
    }

    private void openSurgicalProcessActivity() {

        OperationRoomOutputModel operationRoom = (OperationRoomOutputModel)spinnerOperationRoom.getSelectedItem();

        String interventionCode = editTextInterventionCode.getText().toString();
        String recordNumber = editTextRecordNumber.getText().toString();
//        String deliveryNote = editTextDeliveryNote.getText().toString();
        String interventionDate = editTextInterventionDate.getText().toString();

        //Comprobamos que los campos obligatorios tienen datos
        if(TextUtils.isEmpty(interventionCode)){
            editTextInterventionCode.setError(getText(R.string.empty_data));
            return;
        }
        if(TextUtils.isEmpty(recordNumber)){
            editTextRecordNumber.setError(getText(R.string.empty_data));
            return;
        }
//        if(TextUtils.isEmpty(deliveryNote)){
//            editTextDeliveryNote.setError(getText(R.string.empty_data));
//            return;
//        }
        if(TextUtils.isEmpty(interventionDate)){
            //Si el usuario no ha elegido fecha, se establece la fecha actual
            //y creamos también registroES (setShipmentData)
            Date currentDate = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            interventionDate = df.format(currentDate);
            this.surgicalProcess.RealTime = true;
        }

        this.surgicalProcess.HosId = this.userLogged.HosId;
        this.surgicalProcess.InterventionCode = interventionCode;
        this.surgicalProcess.RecordNumber = recordNumber;
        this.surgicalProcess.InterventionDate = interventionDate;
        this.surgicalProcess.OperationRoomId = operationRoom.OpId;
        this.surgicalProcess.Urgent = switchUrgent.isChecked();
//        this.surgicalProcess.DeliveryNote = deliveryNote;

        Intent i = new Intent(getApplicationContext(), SurgicalProcessActivity.class);
        i.putExtra("surgicalProcess", this.surgicalProcess);
        i.putExtra("userLogged", this.userLogged);
        i.putExtra("isUpdate", this.isUpdate);

        startActivityForResult(i,REQUEST_CODE);
    }

    //Crear controles de visualización del calendario.
    private void showDateTimeDialog(EditText editTextInterventioDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        SimpleDateFormat simpleDateFormat =
                                new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        editTextInterventioDate.setText(simpleDateFormat.format(calendar.getTime()));
                    }
                };
                new TimePickerDialog(SurgicalProcessPreviousDataActivity.this, timeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
            }
        };
        new DatePickerDialog(SurgicalProcessPreviousDataActivity.this, dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                cleanControls();
            }
        }
    }

    private void cleanControls(){
        editTextRecordNumber.setText("");
//        editTextDeliveryNote.setText("");
        editTextInterventionCode.setText("");
        editTextInterventionDate.setText("");
        spinnerOperationRoom.setSelection(0);
        switchUrgent.setChecked(false);
        this.isUpdate = false;
        surgicalProcess = new SurgicalProcessOutputModel();
    }

    //Añadimos la info a los controles de la vista
    private void setValuesOfRecoveredSP(SurgicalProcessOutputModel surgicalProcess) {
        editTextRecordNumber.setText(surgicalProcess.RecordNumber);
//        editTextDeliveryNote.setText(surgicalProcess.DeliveryNote);
        editTextInterventionCode.setText(surgicalProcess.InterventionCode);
        editTextInterventionDate.setText(surgicalProcess.InterventionDate);
        switchUrgent.setChecked(surgicalProcess.Urgent);

        this.isUpdate = true;
        //El control spinner hay que hacerlo después de recuperar los datos de bd
        //para que el valor del control se actualice.

    }

    private void selectValue(Spinner spinner, String oprName) {
       for(OperationRoomOutputModel op : OperationRoomList){
           if(op.OpName.equals(oprName)){
                int index = OperationRoomList.indexOf(op);
                spinner.setSelection(index);
           }
       }

    }

    //Solo realizamos la consulta para obtener los quirófanos de BD
    class OperationRoomAsyncClass extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            String methodName;
            String parameterName;

            Object resultModel = null;

            OperationRoomInputModel operationRoomInputModel = new OperationRoomInputModel();
            operationRoomInputModel.HosId = userLogged.HosId;

            FireExitClient client = new FireExitClient(
                    ConnectionParameters.SOAP_ADDRESS[ConnectionParameters.SET_URL_CONNECTION]);
            methodName = "GetOperationRoomList";
            parameterName = "hosId";

            client.configure(new Configurator(
                    ConnectionParameters.NAME_SPACE, ConnectionParameters.CONTRACT_NAME, methodName));
            client.addParameter(parameterName, operationRoomInputModel );
            resultModel = new OperationRoomOutputModel();
            try {
                resultModel = client.call(resultModel);
            } catch (Exception e) {
                e.printStackTrace();
                ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
            }
            return resultModel;
        }

        @Override
        protected void onPostExecute(Object modelResult) {
            super.onPostExecute(modelResult);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(modelResult!= null){
                        processData(modelResult);
                    }else{
                        Toast.makeText(getBaseContext(),R.string.error_operating_room,Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        private void processData(Object modelResult){
            //Si no es nulo cargamos el arrayadapter
            try {
                OperationRoomOutputModel o = (OperationRoomOutputModel)modelResult;
                OperationRoomList = o.OpList;
                adapter = new ArrayAdapter(
                        getBaseContext(), R.layout.spinner_list, OperationRoomList);
                spinnerOperationRoom.setAdapter(adapter);
                selectValue(spinnerOperationRoom, surgicalProcess.OperationRoomName);


            }catch(Exception e){
                ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
                Toast.makeText(getBaseContext(), e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }
}