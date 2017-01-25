package br.lavid.pamin.com.pamin.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.model.LatLng;
import com.pkmmte.view.CircularImageView;

import java.util.LinkedList;

import br.lavid.pamin.com.pamin.R;
import br.lavid.pamin.com.pamin.activities.CulturalRegisterActivity;
import br.lavid.pamin.com.pamin.activities.MainActivity;
import br.lavid.pamin.com.pamin.models.CulturalRegister;
import br.lavid.pamin.com.pamin.models.User;
import br.lavid.pamin.com.pamin.utils.GoogleAPI;
import br.lavid.pamin.com.pamin.utils.TabletFeatures;

/**
 * Created by araujojordan on 13/06/15.
 */
public class MainListAdapter extends ArrayAdapter<CulturalRegister> implements GoogleAPI.DownloadCallback {

    private Activity actReference;
    private GoogleAPI gapi;
    private LinkedList<CulturalRegister> backupTotal;

    public MainListAdapter(Activity act) {
        super(act, R.layout.mainlist_item, new LinkedList<CulturalRegister>());
        actReference = act;
        gapi = new GoogleAPI(act);
    }

    public void loadList(LinkedList<CulturalRegister> registersList) {
        if (registersList != null) {
            addAll(registersList);
            notifyDataSetChanged();
        }
    }

    private void removeAll() {
        int size = getCount();
        for (int i = 0; i < size; i++)
            remove(getItem(0));
        notifyDataSetChanged();
    }

    public void restoreAll() {
        if (backupTotal == null)
            return;

        removeAll();

        for (CulturalRegister culturalRegister : backupTotal)
            add(culturalRegister);

        notifyDataSetChanged();
    }

    public void setCategory(String name) {
        if (name.equals("Tudo")) {
            restoreAll();
            return;
        }
        if (backupTotal == null) {
            backupTotal = new LinkedList<>();
            try {
                for (int i = 0; getItem(i) != null; i++)
                    backupTotal.add(getItem(i));
            } catch (IndexOutOfBoundsException out) {

            }
        }

        removeAll();

        for (CulturalRegister culturalRegister : backupTotal)
            if (culturalRegister.getCategory().equals(name))
                add(culturalRegister);

        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View rowView, ViewGroup parent) {

        final CulturalRegister culturalRegister = getItem(position);

        View.OnClickListener openCulturalRegister = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCulturalRegister(position);
            }
        };

        if (rowView == null) {
            rowView = LayoutInflater.from(getContext()).
                    inflate(R.layout.mainlist_item, parent, false);
        }

        final Bitmap backgroundPic;

        final ImageView backgroundImage;
        TextView title, distance, description;
        final CircularImageView image;
        ImageButton locBtn;
        Button openBtn, shareBtn;

        backgroundImage = (ImageView) rowView.findViewById(R.id.mainList_background);
        openBtn = (Button) rowView.findViewById(R.id.mainList_openBtn);
        title = (TextView) rowView.findViewById(R.id.mainList_title);
        description = (TextView) rowView.findViewById(R.id.mainList_description);
        distance = (TextView) rowView.findViewById(R.id.mainList_distance);
        image = (CircularImageView) rowView.findViewById(R.id.mainList_thumbnail);
        locBtn = (ImageButton) rowView.findViewById(R.id.mainList_locBtn);
        shareBtn = (Button) rowView.findViewById(R.id.mainList_shareBtn);

        title.setText(culturalRegister.getTitle());
        if (culturalRegister.getDescription().equals("null"))
            description.setText(culturalRegister.getCategory());
        else
            description.setText(culturalRegister.getDescription());
        locBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() instanceof MainActivity) {
                    ((MainActivity) getContext()).goToPosition(
                            new LatLng(culturalRegister.getLatitude(),
                                    culturalRegister.getLongitude())
                    );
                }
            }
        });
        openBtn.setOnClickListener(openCulturalRegister);
        backgroundImage.setOnClickListener(openCulturalRegister);
        title.setOnClickListener(openCulturalRegister);
        description.setOnClickListener(openCulturalRegister);

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share(culturalRegister);
            }
        });

        if (User.getInstance(getContext()).getUserLocation() != null)
            distance.setText(getDistanceBetween2Points(
                    new LatLng(User.getInstance(getContext()).getUserLocation().getLatitude(),
                            User.getInstance(getContext()).getUserLocation().getLongitude()),
                    new LatLng(culturalRegister.getLatitude(), culturalRegister.getLongitude())
            ));
        else
            distance.setText("");


        switch (culturalRegister.getCategory()) {
            case "Lugares": {
                backgroundPic = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.placeslarge);
                if(Build.VERSION.SDK_INT >= 23) {
                    backgroundImage.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.places_color));
                }
                else {
                    backgroundImage.setBackgroundColor(getContext().getResources().getColor(R.color.places_color));

                }
                break;
            }
            case "Celebrações": {
                backgroundPic = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.celeblarge);

                if(Build.VERSION.SDK_INT >= 23) {
                    backgroundImage.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.celeb_color));
                }
                else {
                    backgroundImage.setBackgroundColor(getContext().getResources().getColor(R.color.celeb_color));

                }

                break;
            }
            case "Saberes": {
                backgroundPic = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.knowlarge);

                if(Build.VERSION.SDK_INT >= 23) {
                    backgroundImage.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.know_color));
                }
                else {
                    backgroundImage.setBackgroundColor(getContext().getResources().getColor(R.color.know_color));

                }

                break;
            }
            case "Objetos": {
                backgroundPic = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.objlarge);

                if(Build.VERSION.SDK_INT >= 23) {
                    backgroundImage.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.object_color));
                }
                else {
                    backgroundImage.setBackgroundColor(getContext().getResources().getColor(R.color.object_color));
                }

                break;
            }
            case "Pessoas": {
                backgroundPic = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.peolarge);

                if(Build.VERSION.SDK_INT >= 23) {
                    backgroundImage.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.people_color));
                }
                else {
                    backgroundImage.setBackgroundColor(getContext().getResources().getColor(R.color.people_color));
                }
                break;
            }
            case "Formas de Expressão": {
                backgroundPic = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.expformlarge);

                if(Build.VERSION.SDK_INT >= 23) {
                    backgroundImage.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.expform_color));
                }
                else {
                    backgroundImage.setBackgroundColor(getContext().getResources().getColor(R.color.expform_color));

                }
                break;
            }
            default:
                backgroundPic = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.celeblarge);


        }

        backgroundImage.setImageBitmap(backgroundPic);
        if (!culturalRegister.getPictures().isEmpty() && culturalRegister.getPictures().get(0) != null && culturalRegister.getDevicePictures((int) culturalRegister.getIdCulturalRegister(), getContext()) == null) {
            ImageRequest ir = new ImageRequest(culturalRegister.getPictures().getLast().getCompleteUrl(), new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap bitmap) {
                    culturalRegister.setDevicePictures(bitmap,(int) culturalRegister.getIdCulturalRegister(), getContext());
                }
            }, 300, 300, null,
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            devicePic(image, backgroundImage, culturalRegister, backgroundPic);
                        }
                    });
            Volley.newRequestQueue(getContext()).add(ir);
        } else devicePic(image, backgroundImage, culturalRegister, backgroundPic);

        image.setImageBitmap(backgroundPic);

        return rowView;
    }

    public void devicePic(ImageView image, ImageView backgroundImage, CulturalRegister culturalRegister, Bitmap backgroundPic) {
        if (culturalRegister.getDevicePictures((int) culturalRegister.getIdCulturalRegister(), getContext()) != null) {
            Log.v("MainAdapter", "Picture " + culturalRegister.getTitle() + " found");
            backgroundImage.setBackground(
                    new BitmapDrawable(getContext().getResources(),
                            culturalRegister.getDevicePictures((int) culturalRegister.getIdCulturalRegister(), getContext())));
            backgroundImage.setImageBitmap(null);
        }
//        else {
//            Log.d("MainAdapter", "Picture " + culturalRegister.getTitle() + " NOT found");
//            Log.d("MainAdapter", "Getting city...");
//            gapi.getCity(culturalRegister, new GoogleAPI.SearchCityCallback() {
//                @Override
//                public void cityComplete(CulturalRegister culturalRegister, String city) {
//                    Log.d("MainAdapter", "City: " + city + ". Getting picture metadata...");
//                    gapi.searchForImages(city, culturalRegister, new GoogleAPI.GoogleImageCallback() {
//                        @Override
//                        public void searchComplete(CulturalRegister culturalRegister, String[] urls) {
//                            Log.d("MainAdapter", "Downloading picture...");
//                            try {
//                                gapi.downloadImage(culturalRegister, MainListAdapter.this, urls[0], urls, 0);
//                            } catch (ArrayIndexOutOfBoundsException aioobe) {
//                                return;
//                            }
//                        }
//                    });
//                }
//            });
//        }

        image.setImageBitmap(backgroundPic);
    }

    public void share(CulturalRegister culturalRegister) {
        Log.v("MainAdapter", "Share");

        ShareDialog shareDialog;
        shareDialog = new ShareDialog(actReference);
        //Trocando porta :80 (padrão) para :3333
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            Log.v("MainAdapter", "Can share...");
            ShareLinkContent content = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse("http://www.pamin.lavid.ufpb.br:3333/registro/" + culturalRegister.getIdCulturalRegister()))
                    .setContentTitle(culturalRegister.getTitle())
                    .setContentDescription(culturalRegister.getDescription())
                    //.setImageUrl(culturalRegister.getPictureUri(getContext()))
                    .build();

            MainActivity.callbackManager = CallbackManager.Factory.create();
            shareDialog.registerCallback(MainActivity.callbackManager, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {
                    Log.v("Face", "Result: " + result.toString());
                    LoginManager.getInstance().logOut();
                }

                @Override
                public void onCancel() {
                    Log.e("Face", "Cancel");
                    LoginManager.getInstance().logOut();
                }

                @Override
                public void onError(FacebookException e) {
                    Log.e("Face", e.getMessage());
                }
            });
            shareDialog.show(content);
        } else {
            Log.e("Face", "Cant show");
        }


    }

    /**
     * Distance between 2 points on latitude and longitude.
     *
     * @return an text indicating the distance in m or in km, if not initialized, it will disapear. If
     * its < 5m it will show and message telling that is too close
     * @author Jordan Junior
     * @version 1.0
     */
    private String getDistanceBetween2Points(LatLng p1, LatLng p2) {
        String kil = getContext().getString(R.string.kilometer);
        String met = getContext().getString(R.string.meter);
        String toClose = getContext().getString(R.string.distanceToClose);

        if ((p1.latitude == 0 && p1.longitude == 0) || (p2.latitude == 0 && p2.longitude
                == 0))
            return "";

        final int R = 6371;
        final double dLat = deg2rad(p2.latitude - p1.latitude);
        final double dLon = deg2rad(p2.longitude - p1.longitude);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(deg2rad(p1.latitude)) * Math.cos(deg2rad(p2.latitude)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        final double d = R * c; //km

        if (d < 1) {
            int distance = (int) (d * 1000);
            if (distance < 5)
                return toClose;

            return distance + " " + met;
        } else {
            return ((int) (d)) + " " + kil; //in km
        }
    }

    /**
     * Simple conversion from degree to radian
     *
     * @author Jordan Junior
     * @version 1.0
     */
    private double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }


    private void openCulturalRegister(int position) {
        Intent culturalRegisterIntent = new Intent(getContext(), CulturalRegisterActivity.class);
        culturalRegisterIntent.putExtra("culturalRegister", getItem(position).getIdCulturalRegister());
        getContext().startActivity(culturalRegisterIntent);
        if (getContext() instanceof MainActivity) {
            if (new TabletFeatures().isTablet(getContext()))
                ((MainActivity) getContext()).overridePendingTransition(R.anim.tab_card_in, R.anim.tab_card_in2);
            else
                ((MainActivity) getContext()).overridePendingTransition(R.anim.smart_card_in, R.anim.smart_card_in2);
        }
    }

    public void updateList(LinkedList<CulturalRegister> list) {
        removeAll();
        if (backupTotal != null)
            backupTotal.clear();
        for (CulturalRegister culturalRegister : list)
            add(culturalRegister);
    }

    @Override
    public void pictureComplete(CulturalRegister culturalRegister, Bitmap bitmap, int number) {
        Log.d("MainAdapter", "Saving Picture...");
        culturalRegister.setDevicePictures(bitmap, (int) culturalRegister.getIdCulturalRegister(), getContext());
        notifyDataSetChanged();
    }
}