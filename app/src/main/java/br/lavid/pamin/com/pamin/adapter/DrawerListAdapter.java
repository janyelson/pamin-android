package br.lavid.pamin.com.pamin.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;

import br.lavid.pamin.com.pamin.R;

/**
 * Created by Jordan on 14/07/2015.
 */
public class DrawerListAdapter extends ArrayAdapter<String> {

    public DrawerListAdapter(Context context) {
        super(context, R.layout.maindrawer_item);

        addAll(context.getResources().getStringArray(R.array.mainmenu_category_array));
    }

    public View getView(int position, View coverView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.maindrawer_item,
                parent, false);

        TextView tv = (TextView) rowView.findViewById(R.id.draweritem_text);
        tv.setText(getItem(position));
        CircularImageView img = (CircularImageView) rowView.findViewById(R.id.draweritem_icon);
        switch (getItem(position)) {
            case "Tudo": {
                img.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.all));
                break;
            }
            case "Linha do Tempo": {
                img.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.timeline_icon));
                break;
            }
            case "Pessoas": {
                img.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.peolarge));
                break;
            }
            case "Lugares": {
                img.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.placeslarge));
                break;
            }
            case "Celebrações": {
                img.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.celeblarge));
                break;
            }
            case "Saberes": {
                img.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.knowlarge));
                break;
            }
            case "Formas de Expressão": {
                img.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.expformlarge));
                break;
            }
            case "Objetos": {
                img.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.objlarge));
                break;
            }
        }

        return rowView;
    }

}
