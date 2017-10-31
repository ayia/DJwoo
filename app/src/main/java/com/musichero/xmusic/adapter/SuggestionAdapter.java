package com.musichero.xmusic.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.musichero.xmusic.R;
import com.musichero.xmusic.constants.IXMusicConstants;

import java.util.ArrayList;

/**
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.musichero.com
 * @Project:AndroidCloundMusicPlayer
 * @Date:Dec 14, 2014
 */
public class SuggestionAdapter extends CursorAdapter implements IXMusicConstants {
    public static final String TAG = SuggestionAdapter.class.getSimpleName();

    private ArrayList<String> mListItems;


    public SuggestionAdapter(Context context, Cursor c, ArrayList<String> items) {
        super(context, c, false);
        this.mListItems = items;

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView mTextView = (TextView) view.findViewById(R.id.tv_name_options);
        mTextView.setText(mListItems.get(cursor.getPosition()));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (cursor.getPosition() >= 0 && cursor.getPosition() < mListItems.size()) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_suggestion, parent, false);
            TextView mTextView = (TextView) view.findViewById(R.id.tv_name_options);
            mTextView.setText(mListItems.get(cursor.getPosition()));
            return view;
        }
        return null;
    }


}
