package br.lavid.pamin.com.pamin.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import br.lavid.pamin.com.pamin.R;
import br.lavid.pamin.com.pamin.models.CloudnaryPicture;
import br.lavid.pamin.com.pamin.models.CulturalRegister;
import br.lavid.pamin.com.pamin.models.User;
import br.lavid.pamin.com.pamin.utils.CloudnaryPamim;
import br.lavid.pamin.com.pamin.utils.InternetFeatures;
import br.lavid.pamin.com.pamin.utils.MaterialFeatures;
import br.lavid.pamin.com.pamin.utils.PaminAPI;

public class NewRegisterActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int SELECT_PHOTO = 100;
    public static final int REQUEST_PLACE = 200;
    private static int RESULT_IMAGE_CLICK = 1;

    private final Calendar c = Calendar.getInstance();
    private TextView addressView;
    private MapView mapView;
    private Button b;
    private EditText eTitle;
    private Button eWhere;
    private EditText eDesc;
    private GoogleMap googleMap;

    private ProgressDialog loading;

    private Uri cameraImageUri;
    private InputStream serialImage;
    private Bitmap bmpImage;

    private int aux = 0;
    private int aux2 = 0;

    private int startYear = c.get(Calendar.YEAR);
    private int startMonth = c.get(Calendar.MONTH);
    private int startDay = c.get(Calendar.DAY_OF_MONTH);
    private int endYear = c.get(Calendar.YEAR);
    private int endMonth = c.get(Calendar.MONTH);
    private int endDay = c.get(Calendar.DAY_OF_MONTH);
    private int startHour = c.get(Calendar.HOUR_OF_DAY);
    private int startMin = c.get(Calendar.MINUTE);
    private int endHour = c.get(Calendar.HOUR_OF_DAY);
    private int endMin = c.get(Calendar.MINUTE);

    private Date startDate;
    private Date endDate;

    private String category, where, picVidURL;
    private double latitude, longitude;

    private JSONObject jsonObject = new JSONObject();
    private NumberFormat formatter = new DecimalFormat("00");

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {

        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {

        // Check that the SDCard is mounted
        File mediaStorageDir = new File(
                Environment.getExternalStorageDirectory(), Environment.DIRECTORY_PICTURES);

        if (!mediaStorageDir.exists()) {

            if (!mediaStorageDir.mkdirs()) {

                Log.e("Item Attachment", "Failed to create directory MyCameraVideo.");

                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File mediaFile;

        if (type == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + ".jpg");

        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_new_register);

        loading = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);

        mapView = (MapView) findViewById(R.id.formRegister_map);
        aux2 = 0;
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);
        addressView = (TextView) findViewById(R.id.formRegisterr_textAdress);

        category = getIntent().getStringExtra("category");
        try {
            switch (category) {
                case "Celebrações":
                    getSupportActionBar().setTitle("Nova Celebração");
                    MaterialFeatures.actionBarColor(this, getResources().getColor(R.color.celeb_color));
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.celeb_color)));
                    break;
                case "Lugares":
                    getSupportActionBar().setTitle("Novo Lugar");
                    MaterialFeatures.actionBarColor(this, getResources().getColor(R.color.places_color));
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.places_color)));
                    break;
                case "Saberes":
                    getSupportActionBar().setTitle("Novo Saber");
                    MaterialFeatures.actionBarColor(this, getResources().getColor(R.color.know_color));
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.know_color)));
                    break;
                case "Pessoas":
                    getSupportActionBar().setTitle("Nova Pessoa");
                    MaterialFeatures.actionBarColor(this, getResources().getColor(R.color.people_color));
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.people_color)));
                    break;
                case "Formas de Expressão":
                    getSupportActionBar().setTitle("Nova Forma de Expressão");
                    MaterialFeatures.actionBarColor(this, getResources().getColor(R.color.expform_color));
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.expform_color)));
                    break;
                case "Objetos":
                    getSupportActionBar().setTitle("Novo Objeto");
                    MaterialFeatures.actionBarColor(this, getResources().getColor(R.color.object_color));
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.object_color)));
                    break;
            }
        } catch (NullPointerException error) {
            Log.e("Erro", "Impossible to get actionbar to set title and color");
        }


        SimpleDateFormat nameDate = new SimpleDateFormat(startDay + " 'de' MMM 'de' yyy");

        b = (Button) findViewById(R.id.startTimeBtn);
        b.setText("Hora inicial indefinida");
        b = (Button) findViewById(R.id.endTimeBtn);
        b.setText("Hora final indefinida");
        b = (Button) findViewById(R.id.startDateBtn);
        b.setText("Data inicial Indefinida");
        b = (Button) findViewById(R.id.endDateBtn);
        b.setText("Data final Indefinida");

        /*
        eWhere = (Button) findViewById(R.id.formRegister_WhereButton);
        eWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(),
                        LocationGetActivity.class), REQUEST_PLACE);
            }
        });
        */

    }

    public void showTimePickerDialog(View v) {
        switch (v.getId()) {
            case R.id.startTimeBtn:

                TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startHour = hourOfDay;
                        startMin = minute;

                        b = (Button) findViewById(R.id.startTimeBtn);
                        b.setText((formatter.format(startHour)) + ":" + formatter.format(startMin));
                    }
                }, startHour, startMin, DateFormat.is24HourFormat(this));
                timePicker.setTitle("Inicio");
                timePicker.setButton(DatePickerDialog.BUTTON_POSITIVE, "Ok", timePicker);
                timePicker.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                timePicker.show();
                break;

            case R.id.endTimeBtn:
                TimePickerDialog end = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endHour = hourOfDay;
                        endMin = minute;

                        b = (Button) findViewById(R.id.endTimeBtn);
                        b.setText((formatter.format(endHour)) + ":" + formatter.format(endMin));
                    }
                }, endHour, endMin, DateFormat.is24HourFormat(this));
                end.setTitle("fim");
                end.setButton(DatePickerDialog.BUTTON_POSITIVE, "Ok", end);
                end.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                end.show();
                break;
        }

    }

    public void showDatePickerDialog(View v) {
        switch (v.getId()) {
            case R.id.startDateBtn:
                DatePickerDialog startDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        startYear = year;
                        startMonth = monthOfYear;
                        startDay = dayOfMonth;

                        Date date = null;
                        Calendar cal = GregorianCalendar.getInstance();
                        cal.set(startYear, startMonth, startDay);
                        date = cal.getTime();
                        startDate = date;

                        SimpleDateFormat nameDate = new SimpleDateFormat(startDay + " 'de' MMM 'de' yyy");

                        b = (Button) findViewById(R.id.startDateBtn);
                        b.setText(nameDate.format(date));
                    }
                }, startYear, startMonth, startDay);
                startDatePicker.setTitle("Inicio");
                startDatePicker.setButton(DatePickerDialog.BUTTON_POSITIVE, "Ok", startDatePicker);
                startDatePicker.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                startDatePicker.show();
                break;

            case R.id.endDateBtn:
                DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        endYear = year;
                        endMonth = monthOfYear;
                        endDay = dayOfMonth;

                        Date date = null;
                        Calendar cal = GregorianCalendar.getInstance();
                        cal.set(endYear, endMonth, endDay);
                        date = cal.getTime();
                        endDate = date;

                        SimpleDateFormat nameDate = new SimpleDateFormat(endDay + " 'de' MMM 'de' yyy");

                        b = (Button) findViewById(R.id.endDateBtn);
                        b.setText(nameDate.format(date));
                    }
                }, endYear, endMonth, endDay);
                datePicker.setTitle("Fim");
                datePicker.setButton(DatePickerDialog.BUTTON_POSITIVE, "Ok", datePicker);
                datePicker.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                datePicker.show();
                break;
        }
    }

    public boolean checkInput() {

        eTitle = (EditText) findViewById(R.id.formRegister_titleField);
        eWhere = (Button) findViewById(R.id.formRegister_WhereButton);
        eDesc = (EditText) findViewById(R.id.formRegister_DescField);

        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.dialog);
        alert.setTitle("Faltou algo!");
        alert.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        if (eTitle.getText().toString().equals("")) {
            alert.setMessage("Digite um titulo!");
            alert.show();
            return false;
        }
        if (eDesc.getText().toString().equals("")) {
            alert.setMessage("Descreva o evento!");
            alert.show();
            return false;
        }

        return true;
    }

    public void sendRegister(View v) {
        String price, promot, contact;
        User user = User.getInstance(this);


        if (checkInput()) {
            if (!InternetFeatures.hasInternet(this)) {
                new AlertDialog.Builder(this)
                        .setTitle("Acesse a internet")
                        .setMessage("Acesse a internet para enviar o novo registro cultural para o site do Pamin")
                        .show();
                return;
            }

            eTitle = (EditText) findViewById(R.id.formRegister_titleField);
            eWhere = (Button) findViewById(R.id.formRegister_WhereButton);
            eDesc = (EditText) findViewById(R.id.formRegister_DescField);
            EditText ePrice = (EditText) findViewById(R.id.formRegister_Pricefield);
            EditText ePromot = (EditText) findViewById(R.id.formRegister_PromotField);
            EditText eContact = (EditText) findViewById(R.id.formRegister_ContactField);

            String title = eTitle.getText().toString();
            String desc = eDesc.getText().toString();

            if (ePrice.getText().toString().equals(""))
                price = null;
            else
                price = ePrice.getText().toString();

            if (ePromot.getText().toString().equals(""))
                promot = null;
            else
                promot = ePromot.getText().toString();

            if (eContact.getText().toString().equals(""))
                contact = null;
            else
                contact = eContact.getText().toString();

            Log.v("NewRegAct", "Inflating Json...");

            category = getIntent().getStringExtra("category");

            try {
                //headers
                jsonObject.put("what", title);
                jsonObject.put("where", where);
                if (startDate != null) {
                    jsonObject.put("start_date", PaminAPI.convertDateToJsonDate(startDate));
                }
                if (endDate != null) {
                    jsonObject.put("end_date", PaminAPI.convertDateToJsonDate(endDate));
                }
                jsonObject.put("price", price);
                jsonObject.put("promotor", promot);
                jsonObject.put("promotor_contact", contact);
                jsonObject.put("latitude", latitude);
                jsonObject.put("longitude", longitude);
                jsonObject.put("description", desc);
                jsonObject.put("category_id", PaminAPI.getCategoryID(category));
//                JSONArray picsVidsJson = new JSONArray();
//                if (picVidURL != null)
//                    picsVidsJson.put(picVidURL);
//                jsonObject.put("pictures_videos", picsVidsJson);

            } catch (JSONException e) {
                Log.e("JSON erro", "unexpected JSON exception", e);
            }
            Log.v("NewRegAct", "Json Complete!");
            Log.v("NewRegAct", "HTTP POST Request");

            Log.v("User", "USERNAME: " + User.getInstance(this).getName() +
                    "\nEMAIL: " + User.getInstance(this).getEmail() +
                    "\nTOKEN: " + User.getInstance(this).getToken());
            new PaminAPI().sendNewCultRegister(user, jsonObject, getApplicationContext(), new PaminAPI.SendRegisterCallback() {
                @Override
                public void registerCallback(boolean successful, CulturalRegister culturalRegister) {
                    Log.v("NewRegAct", "Sending Over");
                    Toast.makeText(getApplicationContext(), "Registro adicionado!", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        }
    }


    public void selectFromGalery(View v) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PHOTO);
    }

    public void captureImage(View v) {

        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraImageUri = getOutputMediaFileUri(1);

        camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        startActivityForResult(camera_intent, RESULT_IMAGE_CLICK);

    }

    public void addLocal(View v) {

        startActivityForResult(new Intent(getApplicationContext(), LocationGetActivity.class), REQUEST_PLACE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        ImageView imgView = (ImageView) findViewById(R.id.formRegister_picture);

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_IMAGE_CLICK && resultCode == RESULT_OK) {
            aux++;
            // Here you have the ImagePath which you can set to you image view
            Log.e("Image Name", cameraImageUri.getPath());
            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            bmpImage = BitmapFactory.decodeFile(cameraImageUri.getPath());

            int size[] = calculateBitmapSize(bmpImage);
            Bitmap.createScaledBitmap(bmpImage, size[0], size[1], false);
            bmpImage = cutImage(bmpImage);
            imgView.setImageBitmap(bmpImage);
            loading.show();

            new CloudnaryPicture(new CloudnaryPamim(this), bmpImage, User.getInstance(this), new CloudnaryPicture.SendPictureCallback() {
                @Override
                public void sentPicture(Bitmap downloadPicture, String url) {
                    picVidURL = url;
                    loading.dismiss();
                }
            });

            return;
        }

        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK) {

            Toast.makeText(this, "IS ALL RIGHT HERE", Toast.LENGTH_LONG).show();


            Log.v("NewRegAct", "REQUEST SELECT_PHOTO result OK");
            aux++;
            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Uri selectedImage = data.getData();
            cameraImageUri = selectedImage;
            try {
                bmpImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), cameraImageUri);
                int size[] = calculateBitmapSize(bmpImage);
                Bitmap.createScaledBitmap(bmpImage, size[0], size[1], false);
                bmpImage = cutImage(bmpImage);
                serialImage = convertBmpToInpStrm(bmpImage);
                imgView.setImageBitmap(bmpImage);
            } catch (IOException e) {
                Log.e("NewRegError", e.getMessage());
            }

            loading.show();
            new CloudnaryPicture(new CloudnaryPamim(this), bmpImage, User.getInstance(this), new CloudnaryPicture.SendPictureCallback() {
                @Override
                public void sentPicture(Bitmap downloadPicture, String url) {
                    picVidURL = url;
                    loading.dismiss();
                }
            });
            return;
        }
        if (requestCode == REQUEST_PLACE && resultCode == RESULT_OK) {
            aux2 = 1;
            Toast.makeText(this, "Request susseful made", Toast.LENGTH_LONG).show();
            this.latitude = data.getDoubleExtra("lat", 0);
            this.longitude = data.getDoubleExtra("lng", 0);

            //Verificar as permissões
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            //Atualizando mapView.getMap() para googleMap


            mapView.getMapAsync(this);
            googleMap.setMyLocationEnabled(true);
            //mapView.getMap().setMyLocationEnabled(true)

            googleMap.clear();
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title("Aqui!"));


            this.where = data.getStringExtra("where");

            addressView.setVisibility(View.VISIBLE);
            //mapView.setVisibility(View.VISIBLE);


            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(latitude, longitude), 14);
            googleMap.animateCamera(cameraUpdate);

            mapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(getApplicationContext(),
                            LocationGetActivity.class), REQUEST_PLACE);
                }
            });

            /*
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    startActivityForResult(new Intent(getApplicationContext(),
                            LocationGetActivity.class), REQUEST_PLACE);
                }
            });
            */

            addressView.setText(where);
            //mapView.getMapAsync(this);
        }
    }

    private InputStream convertBmpToInpStrm(Bitmap bpm) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bpm.compress(Bitmap.CompressFormat.JPEG, 85, bos);
        byte[] bitmapdata = bos.toByteArray();
        return new ByteArrayInputStream(bitmapdata);
    }

    private Bitmap cutImage(Bitmap srcBmp) {
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()) {

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        } else {

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }
        return dstBmp;
    }

    private int[] calculateBitmapSize(Bitmap bitmap) {
        final int WANTED_SIZE = 400;

        int size[] = new int[2];

        if (bitmap.getWidth() > WANTED_SIZE) {
            size[0] = bitmap.getWidth() / (bitmap.getWidth() / WANTED_SIZE);
        } else {
            size[0] = bitmap.getWidth();
        }

        if (bitmap.getHeight() > WANTED_SIZE) {
            size[1] = bitmap.getHeight() / (bitmap.getHeight() / WANTED_SIZE);
        } else
            size[1] = bitmap.getHeight();

        return size;
    }


    public void showFullscreen(View v) {
        if (aux != 0) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(cameraImageUri, "image/*");
            startActivity(intent);
        }
    }

    @Override
    public void onMapReady(GoogleMap gmap) {
        googleMap = gmap;
    }

    public void clearDateOrTime (View view) {

        Button b;

        switch (view.getId()) {

            case R.id.clearStartDate_btn:
                b = (Button) findViewById(R.id.startDateBtn);
                b.setText("Data inicial indefinida");
                this.startDate = null;
                break;

            case R.id.clearStartTime_btn:
                b = (Button) findViewById(R.id.startTimeBtn);
                b.setText("Hora inicial indefinida");
                this.startHour = 0;
                break;

            case R.id.clearEndDate_btn:
                b = (Button) findViewById(R.id.endDateBtn);
                b.setText("Data final indefinida");
                this.endDate = null;
                break;

            case R.id.clearEndTime_btn:
                b = (Button) findViewById(R.id.endTimeBtn);
                b.setText("Hora final indefinida");
                this.endHour = 0;
                break;

            default:
                break;
        }
    }


}
