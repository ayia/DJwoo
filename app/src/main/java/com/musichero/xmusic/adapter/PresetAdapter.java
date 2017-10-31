
package com.musichero.xmusic.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.musichero.xmusic.R;


public class PresetAdapter extends ArrayAdapter<String> {

    private LayoutInflater mInflater;
    private Context mContext;
    private String[] mListString;
    private Typeface mTypeFace;
    private IPresetListener presetListener;


    public PresetAdapter(Context context, int resource, String[] objects, Typeface mTypeFace) {
        super(context, resource, objects);
        this.mContext = context;
        this.mListString = objects;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mTypeFace = mTypeFace;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder mHolder;
        if (convertView == null) {
            mHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_preset_name, null);
            convertView.setTag(mHolder);

            mHolder.mTvName = (TextView) convertView.findViewById(R.id.tv_name);
            mHolder.mTvName.setTypeface(mTypeFace);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }
        mHolder.mTvName.setText(mListString[position]);
        return convertView;
    }

    @Override
    public View getDropDownView(final int position, View convertView, ViewGroup parent) {
        ViewDropHolder mViewDropHolder;
        if (convertView == null) {
            mViewDropHolder = new ViewDropHolder();
            convertView = mInflater.inflate(R.layout.item_preset_name, null);
            convertView.setTag(mViewDropHolder);

            mViewDropHolder.mTvName = (TextView) convertView.findViewById(R.id.tv_name);
            mViewDropHolder.mTvName.setTypeface(mTypeFace);

        } else {
            mViewDropHolder = (ViewDropHolder) convertView.getTag();
        }
        mViewDropHolder.mTvName.setText(mListString[position]);
        mViewDropHolder.mTvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View root = v.getRootView();
                root.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                root.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                if (presetListener != null) {
                    presetListener.onSelectItem(position);
                }
            }
        });
        return convertView;
    }

    public void setPresetListener(IPresetListener presetListener) {
        this.presetListener = presetListener;
    }


    public interface IPresetListener {
        void onSelectItem(int position);
    }

    private static class ViewDropHolder {
        public TextView mTvName;
    }

    private static class ViewHolder {
        public TextView mTvName;
    }


}
