package br.lavid.pamin.com.pamin.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Calendar;
import java.util.LinkedList;

/**
 * Created by araujojordan on 13/06/15.
 */
public class CulturalRegister implements Serializable {

    private long idCulturalRegister;
    private String title, description, where;

    private String category;
    private String promoter, promoterContact;
    private LinkedList<CloudnaryPicture> pictures;
    private LinkedList<DevicePicture> devPictures;
    private Calendar startDate;
    private Calendar endDate;
    private double price;
    private double latitude, longitude;

    public CulturalRegister(long idCulturalRegister, String promoter, String promoterContact,
                            String title, String description, LinkedList<CloudnaryPicture> pictures,
                            Calendar startDate, Calendar endDate, double price, double latitude,
                            double longitude, String category, String where) {

        setCategory(category);

        this.promoter = promoter;
        if (promoter == null)
            this.promoter = "";

        this.promoterContact = promoterContact;
        if (promoterContact == null)
            this.promoterContact = "";

        this.where = where;
        setLatitude(latitude);
        setLongitude(longitude);
        setPrice(price);
        setIdCulturalRegister(idCulturalRegister);
        setTitle(title);
        setDescription(description);

        this.pictures = pictures;
        setStartDate(startDate);
        setEndDate(endDate);

        this.pictures = new LinkedList<>();
    }

    public Uri getPictureUri(Context ctx) {
        try {
            return devPictures.get(0).getUri(ctx);
        } catch (Exception error) {
            Log.e("PicUri", error.getMessage());
            return null;
        }
    }

    public void addPicture(CloudnaryPicture cp) {
        if (pictures == null)
            pictures = new LinkedList<>();
        pictures.add(cp);
    }

    public long getIdCulturalRegister() {
        return idCulturalRegister;
    }

    public void setIdCulturalRegister(long idCulturalRegister) {
        this.idCulturalRegister = idCulturalRegister;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.isEmpty())
            this.title = "";
        else
            this.title = title;
    }

    public Bitmap getDevicePictures(int num, Context ctx) {
        Log.v("CultReg", "GetPic " + num);
        if (devPictures == null)
            devPictures = new LinkedList<>();

        Log.v("CultReg", "Filename:" + getTitle() + "_" + num);

        for (DevicePicture devpic : devPictures) {
            if (devpic.getFileName().equals(getTitle() + "_" + num))
                return devpic.getPicture(ctx);
        }

        DevicePicture tempDevPic = new DevicePicture(getTitle() + "_" + num, ctx);
        devPictures.add(tempDevPic);
        return tempDevPic.getPicture(ctx);

    }

    public void setDevicePictures(Bitmap devicePictures, int number, Context ctx) {
        if (devPictures == null)
            devPictures = new LinkedList<>();
        DevicePicture devPic = new DevicePicture(devicePictures, getTitle() + "_" + number, ctx);
        devPictures.add(devPic);
    }

    public String getWhere() {
        return where;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null)
            this.description = "";
        else
            this.description = description;
    }

    public String getPromoter() {
        return promoter;
    }

    public String getPromoterContact() {
        return promoterContact;
    }

    public synchronized LinkedList<CloudnaryPicture> getPictures() {
        return pictures;
    }

    public Calendar getStartDate() {
        if(startDate == null) {

            return null;
        }

        return (Calendar) startDate.clone();
    }

    public void setStartDate(Calendar startDate) {

        if(startDate == null) {
            this.startDate = null;
        }
        else {
            this.startDate = (Calendar) startDate.clone();
        }

    }

    public Calendar getEndDate() {
        if(endDate == null) {
            return null;
        }
        return (Calendar) endDate.clone();
    }

    public void setEndDate(Calendar endDate) {
        if(endDate == null) {

            this.endDate = null;

        }
        else {

            this.endDate = (Calendar) endDate.clone();

        }
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        if (latitude > 90 || latitude < -90)
            throw new IllegalArgumentException("Places latitude incorrect value");

        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        if (longitude > 180 || longitude < -180)
            throw new IllegalArgumentException("Places longitude incorrect value");

        this.longitude = longitude;
    }

    public boolean isTheSame(CulturalRegister other) {
        return other.getIdCulturalRegister() == this.getIdCulturalRegister();
    }

    public boolean checkPositionAndName(String name, LatLng position) {
        return position.latitude == latitude && position.longitude == longitude && title.endsWith(name);
    }
}
