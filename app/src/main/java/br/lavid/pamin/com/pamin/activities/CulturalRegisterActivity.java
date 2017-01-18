package br.lavid.pamin.com.pamin.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pkmmte.view.CircularImageView;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.lavid.pamin.com.pamin.R;
import br.lavid.pamin.com.pamin.data.CulturalRegisterDAO;
import br.lavid.pamin.com.pamin.models.CloudnaryPicture;
import br.lavid.pamin.com.pamin.models.CulturalRegister;
import br.lavid.pamin.com.pamin.models.User;
import br.lavid.pamin.com.pamin.utils.CloudnaryPamim;
import br.lavid.pamin.com.pamin.utils.GoogleAPI;
import br.lavid.pamin.com.pamin.utils.MaterialFeatures;
import br.lavid.pamin.com.pamin.utils.PaminAPI;
import br.lavid.pamin.com.pamin.utils.PictureTransformation;
import br.lavid.pamin.com.pamin.utils.TabletFeatures;

public class CulturalRegisterActivity extends AppCompatActivity {

    private static final int SELECT_PHOTO = 51;
    private static final int RESULT_IMAGE_CLICK = 52;
    private CulturalRegister culturalRegister;
    private GoogleAPI gapi;
    private CloudnaryPamim cloudnaryInstance;

    private Uri cameraImageUri;

    private LayoutInflater inflater;
    private LinearLayout gallery;
    private ImageView[] galleryImage;

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
        setContentView(R.layout.activity_cultural_register);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gapi = new GoogleAPI(this);
        cloudnaryInstance = new CloudnaryPamim(this);

        long position = (long) getIntent().getSerializableExtra("culturalRegister");
        for (CulturalRegister cultReg : MainActivity.getActCulturalRegisters()) {
            if (cultReg.getIdCulturalRegister() == position) {
                culturalRegister = cultReg;
                break;
            }
        }

        activity();
        galleryCardView();
        cardView1();
        cardView2();
        cardView4();
        cardView5();
        mapCardView();
    }

    private void galleryCardView() {

        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        gallery = (LinearLayout) findViewById(R.id.culturalRegister_gallery);
        galleryImage = new ImageView[5];

        int iv = 0;

        for (CloudnaryPicture cp : culturalRegister.getPictures()) {
            Log.v("CultRegAct", "Picture " + cp.getCompleteUrl() + ", loading...");
            cp.loadPicture(getApplication(), cloudnaryInstance, new CloudnaryPicture.PictureDownloadCallback() {
                @Override
                public void pictureComplete(final Bitmap downloadPicture, String url) {
                    Log.v("CultRegAct", "Load complete");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageView newImage = (ImageView) inflater.inflate(R.layout.gallery_image, null);
                            newImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(600, 600);
                            newImage.setLayoutParams(parms);
                            newImage.setVisibility(View.VISIBLE);
                            gallery.addView(newImage);
                            newImage.setImageBitmap(downloadPicture);
                        }
                    });
                }
            });
            iv++;
        }

//        for (int i = 0; i <= 4; i++) {
//            if (iv == i)
//                continue;
//            galleryImage[i] = (ImageView) inflater.inflate(R.layout.gallery_image, null);
//            galleryImage[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
//            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(600, 600);
//            galleryImage[i].setLayoutParams(parms);
//            galleryImage[i].setVisibility(View.GONE);
//            gallery.addView(galleryImage[i]);
//
//            if (culturalRegister.getDevicePictures(i, this) != null) {
//                if (galleryImage[i].getDrawable() == null) {
//                    galleryImage[i].setImageBitmap(culturalRegister.getDevicePictures(i, this));
//                    galleryImage[i].setVisibility(View.VISIBLE);
//                }
//            }
//
//        }
//
//        gapi.getCity(culturalRegister, new GoogleAPI.SearchCityCallback() {
//            @Override
//            public void cityComplete(CulturalRegister culturalRegister, String city) {
//                gapi.searchForImages(city, culturalRegister, new GoogleAPI.GoogleImageCallback() {
//                    @Override
//                    public void searchComplete(CulturalRegister culturalRegister, String[] urls) {
//                        for (int i = 0; i < galleryImage.length; i++) {
//                            try {
//                                if (galleryImage[i] == null)
//                                    continue;
//                                if (galleryImage[i].getDrawable() == null) {
//                                    Log.v("CRA", "Downloading " + urls[i]);
//                                    gapi.downloadImage(culturalRegister, new GoogleAPI.DownloadCallback() {
//                                        @Override
//                                        public void pictureComplete(CulturalRegister culturalRegister, Bitmap bitmap, int number) {
//                                            if (galleryImage[number] != null && culturalRegister.getDevicePictures(number, CulturalRegisterActivity.this) != null &&
//                                                    culturalRegister.getIdCulturalRegister() == culturalRegister.getIdCulturalRegister()) {
//                                                galleryImage[number].setImageBitmap(culturalRegister.getDevicePictures(number, CulturalRegisterActivity.this));
//                                                galleryImage[number].setVisibility(View.VISIBLE);
//                                            }
//                                        }
//                                    }, urls[i], urls, i);
//
//                                }
//                            } catch (Exception error) {
//
//                            }
//                        }
//                    }
//                });
//            }
//        });
    }

    private void mapCardView() {
        Log.v("ARAUJOJORDAN", "MAP LAT:" + culturalRegister.getLatitude());
        MapView map = (MapView) findViewById(R.id.culturalRegister_map);
        map.onCreate(null);
        map.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(culturalRegister.getLatitude(),
                                culturalRegister.getLongitude()), 18.0f));
                int resource = 0;
                switch (culturalRegister.getCategory()) {
                    case "Lugares": {
                        resource = R.drawable.places;
                        break;
                    }
                    case "Formas de Expressão": {
                        resource = R.drawable.expform;
                        break;
                    }
                    case "Celebrações": {
                        resource = R.drawable.celeb;
                        break;
                    }
                    case "Saberes": {
                        resource = R.drawable.know;
                        break;
                    }
                    case "Objetos": {
                        resource = R.drawable.obj;
                        break;
                    }
                    case "Pessoas": {
                        resource = R.drawable.peo;
                        break;
                    }
                }
                googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(resource))
                        .position(new LatLng(culturalRegister.getLatitude(), culturalRegister.getLongitude()))
                        .title(culturalRegister.getTitle()));
            }
        });
    }

    private void activity() {

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(culturalRegister.getTitle());
        else if (getActionBar() != null) getActionBar().setTitle(culturalRegister.getTitle());

        int color = 0;
        Bitmap backgroundPic;

        switch (culturalRegister.getCategory()) {
            case "Lugares": {
                color = getResources().getColor(R.color.places_color);
                break;
            }
            case "Celebrações": {
                color = getResources().getColor(R.color.celeb_color);

                break;
            }
            case "Saberes": {
                color = getResources().getColor(R.color.know_color);


                break;
            }
            case "Objetos": {
                color = getResources().getColor(R.color.object_color);

                break;
            }
            case "Pessoas": {
                color = getResources().getColor(R.color.people_color);
                break;
            }
            default:
                color = getResources().getColor(R.color.places_color);
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
        else if (getActionBar() != null)
            getActionBar().setBackgroundDrawable(new ColorDrawable(color));

        MaterialFeatures.actionBarColor(this, color);
        MaterialFeatures.navigationBarColor(this, color);

    }

    private void cardView1() {
        CircularImageView catType = (CircularImageView) findViewById(R.id.culturalRegister_thumbnail);
        Bitmap backgroundPic;

        switch (culturalRegister.getCategory()) {
            case "Lugares": {
                backgroundPic = BitmapFactory.decodeResource(getResources(), R.drawable.placeslarge);

                break;
            }
            case "Celebrações": {
                backgroundPic = BitmapFactory.decodeResource(getResources(), R.drawable.celeblarge);

                break;
            }
            case "Saberes": {
                backgroundPic = BitmapFactory.decodeResource(getResources(), R.drawable.knowlarge);

                break;
            }
            case "Objetos": {
                backgroundPic = BitmapFactory.decodeResource(getResources(), R.drawable.objlarge);

                break;
            }
            case "Pessoas": {
                backgroundPic = BitmapFactory.decodeResource(getResources(), R.drawable.peolarge);

                break;
            }
            default:
                backgroundPic = null;
        }

        if (backgroundPic != null)
            catType.setImageBitmap(backgroundPic);

        TextView where = (TextView) findViewById(R.id.culturalRegister_where);
        if (culturalRegister.getWhere() == null) where.setVisibility(View.GONE);
        else where.setText(culturalRegister.getWhere());

        TextView description = (TextView) findViewById(R.id.culturalRegister_description);
        if (culturalRegister.getDescription() == null || culturalRegister.getDescription().equals("null"))
            description.setVisibility(View.GONE);
        else
            description.setText(culturalRegister.getDescription());
    }

    private void cardView2() {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        TextView startDate = (TextView) findViewById(R.id.culturalRegister_startDate_field);
        if (culturalRegister.getStartDate() == null || culturalRegister.getStartDate().getYear() <= 1970) {
            startDate.setVisibility(View.GONE);
        } else startDate.setText(df.format(culturalRegister.getStartDate()));

        TextView endDate = (TextView) findViewById(R.id.culturalRegister_endDateField);
        if (culturalRegister.getEndDate() == null || culturalRegister.getEndDate().getYear() <= 1970) {
            endDate.setVisibility(View.GONE);
        } else endDate.setText(df.format(culturalRegister.getEndDate()));

        if (startDate.getVisibility() == View.GONE && endDate.getVisibility() == View.GONE) {
            (findViewById(R.id.culturalRegister_cardView2)).setVisibility(View.GONE);
        }
    }

    private void cardView4() {
        CardView cardView4 = (CardView) findViewById(R.id.culturalRegister_cardView4);
        if (culturalRegister.getPrice() == -1.0d) {
            cardView4.setVisibility(View.GONE);
            return;
        } else {
            TextView priceField = (TextView) findViewById(R.id.culturalRegister_priceField);
            if (culturalRegister.getPrice() == 0.0d) {
                priceField.setText(R.string.culturalRegister_priceFree);
                return;
            }
            priceField.setText(getString(R.string.brazilMoney) + culturalRegister.getPrice());
        }

    }

    private void cardView5() {
        CardView cardView5 = (CardView) findViewById(R.id.culturalRegister_cardView5);
        if (culturalRegister.getPromoter().equals("null") && culturalRegister.getPromoterContact().equals("null")) {
            cardView5.setVisibility(View.GONE);
            return;
        }

        TextView promotorTitle = (TextView) findViewById(R.id.culturalRegister_promotorTitle);
        TextView promotorField = (TextView) findViewById(R.id.culturalRegister_promotorField);

        if (culturalRegister.getPromoter().equals("null")) {
            promotorField.setVisibility(View.GONE);
            promotorTitle.setVisibility(View.GONE);
        } else promotorField.setText(culturalRegister.getPromoter());

        TextView promotorContactTitle = (TextView) findViewById(R.id.culturalRegister_promotorContactTitle);
        TextView promotorContactField = (TextView) findViewById(R.id.culturalRegister_promotorContactField);

        if (culturalRegister.getPromoterContact().equals("null")) {
            promotorContactTitle.setVisibility(View.GONE);
            promotorContactField.setVisibility(View.GONE);
        } else promotorContactField.setText(culturalRegister.getPromoterContact());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (new TabletFeatures().isTablet(this))
            overridePendingTransition(R.anim.tab_card_out, R.anim.tab_card_out2);
        else overridePendingTransition(R.anim.smart_card_out, R.anim.smart_card_out2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cultural_register, menu);
        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException npe) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_newPhoto) {
//            if (User.getInstance(getApplication()).hasUser())
//                selectFromGalery();
//            else {
//                startActivity(new Intent(CulturalRegisterActivity.this, LoginActivity.class));
//                Toast.makeText(CulturalRegisterActivity.this, "Faça login para adicionar imagens", Toast.LENGTH_LONG).show();
//            }
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK) {
            Log.v("NewRegAct", "REQUEST SELECT_PHOTO result OK");
            Uri selectedImage = data.getData();
            cameraImageUri = selectedImage;
            try {
                Bitmap bmpImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), cameraImageUri);
                bmpImage = PictureTransformation.scaleDown(PictureTransformation.getCroppedBitmap(bmpImage), 300);

                Log.v("NewRegAct", "Sending Pic");
                culturalRegister.addPicture(new CloudnaryPicture(cloudnaryInstance, bmpImage, User.getInstance(this), new CloudnaryPicture.SendPictureCallback() {
                    @Override
                    public void sentPicture(Bitmap downloadPicture, String url) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CulturalRegisterActivity.this, "Imagem enviada!", Toast.LENGTH_SHORT).show();
                                Log.v("NewRegAct", "Picture saved");
                                CulturalRegisterDAO cultDAO = new CulturalRegisterDAO(CulturalRegisterActivity.this);
                                cultDAO.update(culturalRegister);
                                new PaminAPI().updateRegister(User.getInstance(CulturalRegisterActivity.this),
                                        culturalRegister, new PaminAPI.SendRegisterCallback() {
                                            @Override
                                            public void registerCallback(boolean successful, CulturalRegister culturalRegister) {
                                                if (successful) {
                                                    Toast.makeText(CulturalRegisterActivity.this,
                                                            "Foto adicionada com sucesso", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(CulturalRegisterActivity.this,
                                                            "A foto não pode ser enviada", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                        });
                    }
                }));

            } catch (IOException e) {
                Log.e("NewRegError", e.getMessage());
            }

            return;
        }

    }

    public void selectFromGalery() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PHOTO);
    }

    public void captureImage() {

        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraImageUri = getOutputMediaFileUri(1);

        camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        startActivityForResult(camera_intent, RESULT_IMAGE_CLICK);

    }
}