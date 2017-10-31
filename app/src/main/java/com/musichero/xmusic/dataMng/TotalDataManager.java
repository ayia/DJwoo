package com.musichero.xmusic.dataMng;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.musichero.xmusic.R;
import com.musichero.xmusic.YPYFragmentActivity;
import com.musichero.xmusic.constants.IXMusicConstants;
import com.musichero.xmusic.executor.DBExecutorSupplier;
import com.musichero.xmusic.model.ConfigureModel;
import com.musichero.xmusic.model.GenreModel;
import com.musichero.xmusic.model.PlaylistModel;
import com.musichero.xmusic.model.TrackModel;
import com.musichero.xmusic.model.UserModel;
import com.musichero.xmusic.setting.IYPYSettingConstants;
import com.musichero.xmusic.setting.YPYSettingManager;
import com.musichero.xmusic.task.IYPYCallback;
import com.musichero.xmusic.utils.ApplicationUtils;
import com.musichero.xmusic.utils.DBLog;
import com.musichero.xmusic.utils.IOUtils;
import com.musichero.xmusic.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class TotalDataManager implements IXMusicConstants, IYPYSettingConstants {

    public static final String TAG = TotalDataManager.class.getSimpleName();

    private static TotalDataManager totalDataManager;
    private ArrayList<GenreModel> listGenreObjects;

    private ArrayList<TrackModel> listSavedTrackObjects;

    private ArrayList<PlaylistModel> listPlaylistObjects;

    private ArrayList<TrackModel> listTopMusicObjects;

    private PlaylistModel playlistObject;
    private GenreModel genreObject;
    private ArrayList<TrackModel> listLibraryTrackObjects;
    private ConfigureModel configureModel;

    private TotalDataManager() {

    }

    public static TotalDataManager getInstance() {
        if (totalDataManager == null) {
            totalDataManager = new TotalDataManager();
        }
        return totalDataManager;
    }

    public void onDestroy() {
        if (listGenreObjects != null) {
            listGenreObjects.clear();
            listGenreObjects = null;
        }
        if (listPlaylistObjects != null) {
            listPlaylistObjects.clear();
            listPlaylistObjects = null;
        }
        if (listLibraryTrackObjects != null) {
            listLibraryTrackObjects.clear();
            listLibraryTrackObjects = null;
        }
        if (listTopMusicObjects != null) {
            listTopMusicObjects.clear();
            listTopMusicObjects = null;
        }
        totalDataManager = null;
    }

    public ArrayList<GenreModel> getListGenreObjects() {
        return listGenreObjects;
    }


    public void setListGenreObjects(ArrayList<GenreModel> listGenreObjects) {
        this.listGenreObjects = listGenreObjects;
    }


    public ArrayList<PlaylistModel> getListPlaylistObjects() {
        return listPlaylistObjects;
    }

    public void setListPlaylistObjects(ArrayList<PlaylistModel> listPlaylistObjects) {
        this.listPlaylistObjects = listPlaylistObjects;
    }

    public void addPlaylistObject(PlaylistModel mPlaylistObject) {
        if (listPlaylistObjects != null && mPlaylistObject != null) {
            synchronized (listPlaylistObjects) {
                listPlaylistObjects.add(mPlaylistObject);
            }
            DBExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    savePlaylistObjects();
                }
            });
        }
    }

    public boolean isPlaylistNameExisted(String name) {
        if (!StringUtils.isEmptyString(name)) {
            if (listPlaylistObjects != null && listPlaylistObjects.size() > 0) {
                for (PlaylistModel mPlaylistObject : listPlaylistObjects) {
                    if (mPlaylistObject.getName().equals(name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void editPlaylistObject(PlaylistModel mPlaylistObject, String newName) {
        if (listPlaylistObjects != null && mPlaylistObject != null && !StringUtils.isEmptyString(newName)) {
            mPlaylistObject.setName(newName);
            DBExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    savePlaylistObjects();
                }
            });
        }
    }

    public void removePlaylistObject(PlaylistModel mPlaylistObject) {
        if (listPlaylistObjects != null && listPlaylistObjects.size() > 0) {
            listPlaylistObjects.remove(mPlaylistObject);
            ArrayList<TrackModel> mListTrack = mPlaylistObject.getListTrackObjects();
            boolean isNeedToSaveTrack = false;
            if (mListTrack != null && mListTrack.size() > 0) {
                for (TrackModel mTrackObject : mListTrack) {
                    boolean isAllowRemoveToList = true;
                    for (PlaylistModel mPlaylist : listPlaylistObjects) {
                        if (mPlaylist.isSongAlreadyExited(mTrackObject.getId())) {
                            isAllowRemoveToList = false;
                            break;
                        }
                    }
                    if (isAllowRemoveToList) {
                        listSavedTrackObjects.remove(mTrackObject);
                        isNeedToSaveTrack = true;
                    }
                }
                mListTrack.clear();
            }
            final boolean isGlobal = isNeedToSaveTrack;
            DBExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    savePlaylistObjects();
                    if (isGlobal) {
                        saveDataInCached(TYPE_FILTER_SAVED);
                    }
                }
            });
        }
    }

    public synchronized void savePlaylistObjects() {
        File mFile = getDirectoryTemp();
        if (mFile != null && listPlaylistObjects != null) {
            Gson mGson = new GsonBuilder().create();
            Type listType = new TypeToken<ArrayList<PlaylistModel>>() {
            }.getType();
            String data = mGson.toJson(listPlaylistObjects, listType);
            DBLog.d(TAG, "=============>savePlaylistObjects=" + data + "==>path=" + mFile.getAbsolutePath());
            IOUtils.writeString(mFile.getAbsolutePath(), FILE_PLAYLIST, data);
        }

    }

    public ArrayList<TrackModel> getListTopMusicObjects() {
        return listTopMusicObjects;
    }

    public void setListTopMusicObjects(ArrayList<TrackModel> listTopMusicObjects) {
        if (this.listTopMusicObjects != null) {
            this.listTopMusicObjects.clear();
            this.listTopMusicObjects = null;
        }
        this.listTopMusicObjects = listTopMusicObjects;
    }


    public TrackModel getTrack(int type, long id) {
        ArrayList<TrackModel> mLisTracks = getListTracks(type);
        if (mLisTracks != null && mLisTracks.size() > 0) {
            for (TrackModel mTrackObject : mLisTracks) {
                if (mTrackObject.getId() == id) {
                    return mTrackObject;
                }
            }
        }
        return null;
    }


    public void deleteSong(final TrackModel mTrackObject,
                           final IYPYCallback mCallback) {
        try {
            File mFile = null;
            String path = mTrackObject.getPath();
            if (!StringUtils.isEmptyString(path)) {
                mFile = new File(path);
            }
            if (mFile != null && mFile.exists() && mFile.isFile()) {
                try {
                    boolean b = mFile.delete();
                    if (b) {
                        removeSongFromList(MusicDataMng.getInstance().getListPlayingTrackObjects(), mTrackObject.getId());
                        removeSongFromList(listLibraryTrackObjects, mTrackObject.getId());
                        if (listPlaylistObjects != null && listPlaylistObjects.size() > 0) {
                            for (PlaylistModel mPlaylistObject : listPlaylistObjects) {
                                boolean remove = removeTrackToPlaylistNoThread(mTrackObject, mPlaylistObject, null, true);
                                if (remove) {
                                    break;
                                }
                            }
                        }
                        if (mCallback != null) {
                            mCallback.onAction();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<TrackModel> startSearchSong(String keyword) {
        ArrayList<TrackModel> mListTracks = listLibraryTrackObjects;
        if (!TextUtils.isEmpty(keyword)) {
            if (mListTracks != null && mListTracks.size() > 0) {
                ArrayList<TrackModel> mListTrackObjects = new ArrayList<TrackModel>();
                synchronized (mListTracks) {
                    int size = mListTracks.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            TrackModel mTrackObject = mListTracks.get(i);
                            if (mTrackObject.getTitle().toLowerCase(Locale.US).contains(keyword)) {
                                mListTrackObjects.add(mTrackObject.clone());
                            }
                        }
                    }
                }
                return mListTrackObjects;
            }
        } else {
            if (mListTracks != null && mListTracks.size() > 0) {
                return (ArrayList<TrackModel>) mListTracks.clone();
            }
        }
        return null;
    }


    public boolean isLibraryTracks(TrackModel mTrackObject) {
        if (listLibraryTrackObjects != null && listLibraryTrackObjects.size() > 0) {
            for (TrackModel mTrackObject1 : listLibraryTrackObjects) {
                if (mTrackObject1.getId() == mTrackObject.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removeSongFromList(ArrayList<TrackModel> listTrackObjects, long id) {
        if (listTrackObjects != null && listTrackObjects.size() > 0) {
            synchronized (listTrackObjects) {
                Iterator<TrackModel> mIterator = listTrackObjects.iterator();
                while (mIterator.hasNext()) {
                    TrackModel mTrackObject = mIterator.next();
                    if (mTrackObject.getId() == id) {
                        mIterator.remove();
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public void readPlaylistCached() {
        File mFile = getDirectoryTemp();
        if (mFile != null) {
            String data = IOUtils.readString(mFile.getAbsolutePath(), FILE_PLAYLIST);
            ArrayList<PlaylistModel> mListPlaylist = JsonParsingUtils.parsingPlaylistObject(data);
            if (mListPlaylist != null && mListPlaylist.size() > 0) {
                setListPlaylistObjects(mListPlaylist);
            } else {
                mListPlaylist = new ArrayList<>();
                setListPlaylistObjects(mListPlaylist);
            }
            if (mListPlaylist.size() > 0) {
                for (PlaylistModel mPlaylistObject : mListPlaylist) {
                    filterSongOfPlaylistId(mPlaylistObject);
                }
            }
        }
    }

    public File getDirectoryCached() {
        if (!ApplicationUtils.hasSDcard()) {
            return null;
        }
        try {
            final File mFile = new File(Environment.getExternalStorageDirectory(), DIR_CACHE);
            if (!mFile.exists()) {
                mFile.mkdirs();
            }
            return mFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private File getDirectoryTemp() {
        File mRoot = getDirectoryCached();
        if (mRoot != null) {
            final File mFile = new File(mRoot, DIR_TEMP);
            if (!mFile.exists()) {
                mFile.mkdirs();
            }
            return mFile;
        }
        return null;

    }


    public ArrayList<TrackModel> getListTracks(int type) {
        if (type == TYPE_FILTER_SAVED) {
            return listSavedTrackObjects;
        }
        return null;
    }

    public String getFileNameSaved(int type) {
        if (type == TYPE_FILTER_SAVED) {
            return FILE_SAVED_TRACK;
        }
        return null;
    }

    public void saveListTrack(int type, ArrayList<TrackModel> listTrack) {
        if (type == TYPE_FILTER_SAVED) {
            listSavedTrackObjects = listTrack;
        }
    }

    public void readCached(int type) {
        final ArrayList<TrackModel> mListTrackObject = getListTracks(type);
        if (mListTrackObject != null && mListTrackObject.size() > 0) {
            return;
        }
        File mFileCache = getDirectoryTemp();
        if (mFileCache != null) {
            File mFileData = new File(mFileCache, getFileNameSaved(type));
            if (mFileData.exists() && mFileData.isFile()) {
                try {
                    FileInputStream mFileInputStream = new FileInputStream(mFileData);
                    ArrayList<TrackModel> mListTracks = JsonParsingUtils.parsingListTrackObjects(mFileInputStream);
                    DBLog.d(TAG, "=========>readCached=" + (mListTracks != null ? mListTracks.size() : 0));
                    if (mListTracks != null && mListTracks.size() > 0) {
                        saveListTrack(type, mListTracks);
                        return;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            saveListTrack(type, new ArrayList<TrackModel>());
        }

    }

    private void filterSongOfPlaylistId(PlaylistModel mPlaylistObject) {
        if (listSavedTrackObjects != null && listSavedTrackObjects.size() > 0) {
            ArrayList<Long> mListId = mPlaylistObject.getListTrackIds();
            if (mListId != null && mListId.size() > 0) {
                for (Long mId : mListId) {
                    for (TrackModel mTrackObject : listSavedTrackObjects) {
                        if (mTrackObject.getId() == mId) {
                            mPlaylistObject.addTrackObject(mTrackObject, false);
                            break;
                        }
                    }
                }
            }
        }
    }

    public synchronized void addTrackToPlaylist(final YPYFragmentActivity mContext, final TrackModel mParentTrackObject,
                                                final PlaylistModel mPlaylistObject, boolean isShowMsg, final IYPYCallback mCallback) {
        if (mParentTrackObject != null && mPlaylistObject != null) {
            if (!mPlaylistObject.isSongAlreadyExited(mParentTrackObject.getId())) {
                TrackModel mTrackObject = mParentTrackObject.clone();
                mPlaylistObject.addTrackObject(mTrackObject, true);
                boolean isAllowAddToList = true;
                for (TrackModel mTrackObject1 : listSavedTrackObjects) {
                    if (mTrackObject1.getId() == mTrackObject.getId()) {
                        isAllowAddToList = false;
                        break;
                    }
                }
                if (isAllowAddToList) {
                    listSavedTrackObjects.add(mTrackObject);
                }
                if (mCallback != null) {
                    mCallback.onAction();
                }
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mContext.showToast(String.format(mContext.getString(R.string.info_add_playlist), mParentTrackObject.getTitle()
                                , mPlaylistObject.getName()));
                    }
                });
                DBExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        savePlaylistObjects();
                        saveDataInCached(TYPE_FILTER_SAVED);
                    }
                });
            } else {
                if (isShowMsg) {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mContext.showToast(R.string.info_song_already_playlist);
                        }
                    });
                }

            }
        }
    }

    public synchronized boolean removeTrackToPlaylistNoThread(TrackModel mTrackObject
            , PlaylistModel mPlaylistObject, IYPYCallback mCallback, boolean isNeedSave) {
        if (mTrackObject != null && mPlaylistObject != null) {
            mPlaylistObject.removeTrackObject(mTrackObject);
            boolean isAllowRemoveToList = true;
            for (PlaylistModel mPlaylist : listPlaylistObjects) {
                if (mPlaylist.isSongAlreadyExited(mTrackObject.getId())) {
                    isAllowRemoveToList = false;
                    break;
                }
            }
            if (mCallback != null) {
                mCallback.onAction();
            }
            DBLog.d(TAG, "============>removeTrackToPlaylist=" + isAllowRemoveToList);
            if (isAllowRemoveToList) {
                listSavedTrackObjects.remove(mTrackObject);
                if (isNeedSave) {
                    savePlaylistObjects();
                    saveDataInCached(TYPE_FILTER_SAVED);
                }
            }
            return isAllowRemoveToList;

        }
        return false;
    }

    public synchronized void removeTrackToPlaylist(final TrackModel mTrackObject
            , final PlaylistModel mPlaylistObject, final IYPYCallback mCallback) {
        DBExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                removeTrackToPlaylistNoThread(mTrackObject, mPlaylistObject, mCallback, true);
            }
        });
    }

    public void readGenreData(Context mContext) {
        String data = IOUtils.readStringFromAssets(mContext, FILE_GENRE);
        ArrayList<GenreModel> mListGenres = JsonParsingUtils.parsingGenreObject(data);
        DBLog.d(TAG, "==========>size genres=" + (mListGenres != null ? mListGenres.size() : 0));
        if (mListGenres != null) {
            setListGenreObjects(mListGenres);
        }
    }

    public void readConfigure(Context mContext) {
        String data = IOUtils.readStringFromAssets(mContext, FILE_CONFIGURE);
        configureModel = JsonParsingUtils.parsingConfigureModel(data);
        if (configureModel != null) {
            YPYSettingManager.setBackground(mContext, configureModel.getBg());
        }
    }

    public synchronized void saveDataInCached(int type) {
        File mFile = getDirectoryTemp();
        if (mFile != null) {
            ArrayList<TrackModel> mListTracks = getListTracks(type);
            String data = "[]";
            if (mListTracks != null && mListTracks.size() > 0) {
                Gson mGson = new GsonBuilder().create();
                ArrayList<TrackModel> mListSave = new ArrayList<>();
                try {
                    for (TrackModel mTrackObject : mListTracks) {
                        if (!mTrackObject.isNativeAds()) {
                            mListSave.add(mTrackObject);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Type listType = new TypeToken<ArrayList<TrackModel>>() {
                }.getType();
                data = mGson.toJson(mListSave, listType);
            }
            DBLog.d(TAG, "===============>saveTrackDataInCached=" + data);
            IOUtils.writeString(mFile.getAbsolutePath(), getFileNameSaved(type), data);
        }

    }

    public PlaylistModel getPlaylistObject() {
        return playlistObject;
    }

    public void setPlaylistObject(PlaylistModel playlistObject) {
        this.playlistObject = playlistObject;
    }

    public GenreModel getGenreObject() {
        return genreObject;
    }

    public void setGenreObject(GenreModel genreObject) {
        this.genreObject = genreObject;
    }


    public void readLibraryTrack(Context mContext) {
        if (listLibraryTrackObjects != null && listLibraryTrackObjects.size() > 0) {
            return;
        }
        this.listLibraryTrackObjects = getListMusicsFromLibrary(mContext);
        sortListSongs(listLibraryTrackObjects);
    }

    private ArrayList<TrackModel> getListMusicsFromLibrary(Context mContext) {
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = mContext.getContentResolver().query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);
        DBLog.d(TAG, "Query finished. " + (cur == null ? "Returned NULL." : "Returned a cursor."));
        if (cur == null) {
            DBLog.d(TAG, "Failed to retrieve music: cursor is null :-(");
            return null;
        }
        if (!cur.moveToFirst()) {
            DBLog.d(TAG, "Failed to move cursor to first row (no query results).");
            return null;
        }
        int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
        int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);
        int dataColumn = cur.getColumnIndex(MediaStore.Audio.Media.DATA);
        int dateColumn = cur.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);

        ArrayList<TrackModel> listTrackObjects = new ArrayList<TrackModel>();
        do {
            long id = cur.getLong(idColumn);
            String singer = cur.getString(artistColumn);
            String title = cur.getString(titleColumn);
            long duration = cur.getLong(durationColumn);
            String path = cur.getString(dataColumn);
            Date mDate = new Date(cur.getLong(dateColumn) * 1000);
            if (!StringUtils.isEmptyString(path)) {
                File mFile = new File(path);
                if (mFile.exists() && mFile.isFile()) {
                    UserModel userObject = new UserModel(singer);
                    TrackModel mTrackObject = new TrackModel(path, title);
                    mTrackObject.setId(id);
                    mTrackObject.setUserObject(userObject);
                    mTrackObject.setDateCreated(mDate);
                    mTrackObject.setDuration(duration);
                    listTrackObjects.add(mTrackObject);
                }
            }

        }
        while (cur.moveToNext());
        return listTrackObjects;

    }

    public boolean sortListSongs(ArrayList<TrackModel> mListLibrary) {
        if (mListLibrary != null && mListLibrary.size() > 0) {
            try {
                Collections.sort(mListLibrary, new Comparator<TrackModel>() {
                    @Override
                    public int compare(TrackModel lhs, TrackModel rhs) {
                        Date date1 = lhs.getDateCreated();
                        Date date2 = rhs.getDateCreated();
                        return date2.compareTo(date1);

                    }
                });
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    public ArrayList<TrackModel> getListLibraryTrackObjects() {
        return listLibraryTrackObjects;
    }

    public ArrayList<TrackModel> getAllTrackWithAdmob(Context mContext, ArrayList<TrackModel> mListTracks, int typeUI) {
        if (mListTracks != null) {
            ArrayList<TrackModel> mListObjects = (ArrayList<TrackModel>) mListTracks.clone();
            int size = mListObjects.size();
            boolean b = SHOW_ADS && SHOW_NATIVE_ADS && typeUI == TYPE_UI_LIST;
            if (b && size > 0 && ApplicationUtils.isOnline(mContext)) {
                int len = ADS_FREQ.length;
                for (int i = 0; i < len; i++) {
                    if (size >= ADS_FREQ[i] + 1) {
                        TrackModel mObject = new TrackModel();
                        mObject.setNativeAds(true);
                        try {
                            mListObjects.add(ADS_FREQ[i], mObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
            return mListObjects;

        }
        return null;
    }

    public ArrayList<PlaylistModel> getAllPlaylistObjectWithAdmob(Context mContext, ArrayList<PlaylistModel> mListPlaylist, int typeUI) {
        if (mListPlaylist != null) {
            ArrayList<PlaylistModel> mListObjects = (ArrayList<PlaylistModel>) mListPlaylist.clone();
            boolean b = SHOW_ADS && SHOW_NATIVE_ADS && typeUI == TYPE_UI_LIST;
            int size = mListObjects.size();
            if (b && size > 0 && ApplicationUtils.isOnline(mContext)) {
                int len = ADS_FREQ.length;
                for (int i = 0; i < len; i++) {
                    if (size >= ADS_FREQ[i] + 1) {
                        PlaylistModel mObject = new PlaylistModel();
                        mObject.setNativeAds(true);
                        try {
                            mListObjects.add(ADS_FREQ[i], mObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
            return mListObjects;

        }
        return null;
    }

    public ConfigureModel getConfigureModel() {
        return configureModel;
    }

}
