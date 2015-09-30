package com.ekuater.labelchat.ui.fragment.throwphoto;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ThrowPhoto;
import com.ekuater.labelchat.delegate.ThrowPhotoManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.util.GeocodeSearcher;

import java.lang.ref.WeakReference;

/**
 * Created by Leo on 2015/1/8.
 *
 * @author LinYong
 */
public class MyThrowPhotosFragment extends Fragment {

    private static final int MSG_GET_MY_THROW_PHOTOS = 101;
    private static final int MSG_HANDLE_GET_MY_THROW_PHOTOS = 102;

    private ThrowPhotoManager mThrowPhotoManager;
    private ThrowPhotoAdapter mThrowPhotoAdapter;
    private ImageView mLoadingImage;
    private View mNoDataView;
    private boolean mNowLoading;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_MY_THROW_PHOTOS:
                    getMyThrowPhotos();
                    break;
                case MSG_HANDLE_GET_MY_THROW_PHOTOS:
                    handleGetMyThrowPhotos((ThrowPhoto[]) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        mThrowPhotoManager = ThrowPhotoManager.getInstance(activity);
        mThrowPhotoAdapter = new ThrowPhotoAdapter(activity, mThrowPhotoManager);

        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.my_throw_photos);
        }

        // To get my throw photo
        mNowLoading = false;
        mHandler.sendEmptyMessage(MSG_GET_MY_THROW_PHOTOS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_throw_photos, container, false);
        mLoadingImage = (ImageView) view.findViewById(R.id.loading);
        mNoDataView = view.findViewById(R.id.no_data);
        ListView throwListView = (ListView) view.findViewById(R.id.list);
        throwListView.setAdapter(mThrowPhotoAdapter);
        throwListView.setOnItemClickListener(mThrowPhotoAdapter);
        updateLoadingViewVisibility();
        return view;
    }

    private void getMyThrowPhotos() {
        mNowLoading = true;
        updateLoadingViewVisibility();
        mThrowPhotoManager.getMyThrowPhotos(new ThrowPhotoManager.ThrowPhotoQueryObserver() {
            @Override
            public void onQueryResult(ThrowPhotoManager.ResultCode result,
                                      ThrowPhoto[] throwPhotos) {
                Message msg = mHandler.obtainMessage(MSG_HANDLE_GET_MY_THROW_PHOTOS, throwPhotos);
                mHandler.sendMessage(msg);
            }
        });
    }

    private void handleGetMyThrowPhotos(ThrowPhoto[] throwPhotos) {
        mThrowPhotoAdapter.updateThrowPhotos(throwPhotos);
        mNowLoading = false;
        updateLoadingViewVisibility();
    }

    private void updateLoadingViewVisibility() {
        if (mNowLoading) {
            mLoadingImage.setVisibility(View.VISIBLE);
            mNoDataView.setVisibility(View.GONE);
            startLoadAnimation();
        } else {
            mLoadingImage.setVisibility(View.GONE);
            mNoDataView.setVisibility(mThrowPhotoAdapter.getCount() <= 0
                    ? View.VISIBLE : View.GONE);
            stopLoadAnimation();
        }
    }

    private void startLoadAnimation() {
        Drawable drawable = mLoadingImage.getDrawable();
        if (drawable instanceof AnimationDrawable) {
            AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
            if (!animationDrawable.isRunning()) {
                animationDrawable.start();
            }
        }
    }

    private void stopLoadAnimation() {
        Drawable drawable = mLoadingImage.getDrawable();
        if (drawable instanceof AnimationDrawable) {
            AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
            if (animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
        }
    }

    private static class ThrowPhotoAdapter extends BaseAdapter
            implements AdapterView.OnItemClickListener {

        private final Context mContext;
        private final ThrowPhotoManager mThrowPhotoManager;
        private final LayoutInflater mInflater;
        private final GeocodeSearcher mGeocodeSearcher;
        private ThrowPhoto[] mThrowPhotos;
        private SparseArrayCompat<GeocodeSearcher.SearchAddress> mAddressArray;

        public ThrowPhotoAdapter(Context context, ThrowPhotoManager throwPhotoManager) {
            mContext = context;
            mThrowPhotoManager = throwPhotoManager;
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mGeocodeSearcher = GeocodeSearcher.getInstance(context);
        }

        public void updateThrowPhotos(ThrowPhoto[] throwPhotos) {
            mThrowPhotos = throwPhotos;
            mAddressArray = (throwPhotos != null)
                    ? new SparseArrayCompat<GeocodeSearcher.SearchAddress>(throwPhotos.length)
                    : null;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return (mThrowPhotos != null) ? mThrowPhotos.length : 0;
        }

        @Override
        public ThrowPhoto getItem(int position) {
            return (mThrowPhotos != null) ? mThrowPhotos[position] : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = (convertView == null) ? newView(parent) : convertView;
            return bindView(position, convertView);
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        private View newView(ViewGroup parent) {
            View view = mInflater.inflate(R.layout.my_throw_photo_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.displayPhoto = (ImageView) view.findViewById(R.id.display_photo);
            holder.photoCount = (TextView) view.findViewById(R.id.photo_count);
            holder.throwTime = (TextView) view.findViewById(R.id.throw_time);
            holder.mainAddress = (TextView) view.findViewById(R.id.throw_address_main);
            holder.detailAddress = (TextView) view.findViewById(R.id.throw_address_detail);
            view.setTag(holder);
            return view;
        }

        private View bindView(int position, View view) {
            ThrowPhoto throwPhoto = getItem(position);
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.throwPhotoId = throwPhoto.getId();
            displayPhoto(throwPhoto.getDisplayPhoto(), holder.displayPhoto);
            displayAddress(position, throwPhoto, holder);
            holder.photoCount.setText(mContext.getString(R.string.photo_count,
                    throwPhoto.getPhotoArray().length));
            holder.photoCount.setVisibility(throwPhoto.getPhotoArray().length > 1
                    ? View.VISIBLE : View.GONE);
            holder.throwTime.setText(DateTimeUtils.getTimeString(
                    mContext, throwPhoto.getThrowDate()));
            return view;
        }

        private void displayPhoto(String url, ImageView imageView) {
            Utils.showDisplayPhoto(mThrowPhotoManager, url, imageView);
        }

        private void displayAddress(int position, ThrowPhoto throwPhoto, ViewHolder holder) {
            GeocodeSearcher.SearchAddress address = mAddressArray.get(position);
            showAddress(holder, address);

            if (address == null) {
                mGeocodeSearcher.searchAddress(throwPhoto.getLocation(),
                        new ShowAddressObserver(position, holder));
            }
        }

        private void showAddress(ViewHolder holder, GeocodeSearcher.SearchAddress address) {
            String mainAddress = "";
            String detailAddress = "";

            if (address != null) {
                mainAddress = address.province + address.city;
                detailAddress = address.district + address.township + address.street
                        + address.neighborhood + address.building;
                detailAddress = TextUtils.isEmpty(detailAddress)
                        ? "" : detailAddress + mContext.getString(R.string.near_by);
            }

            holder.mainAddress.setText(mainAddress);
            holder.detailAddress.setText(detailAddress);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ThrowPhoto throwPhoto = getItem(position);
            Bundle arguments = new Bundle();
            arguments.putParcelable(MyThrowPhotoDetailFragment.EXTRA_MY_THROW_PHOTO,
                    throwPhoto);
            UILauncher.launchFragmentInNewActivity(mContext, MyThrowPhotoDetailFragment.class,
                    arguments);
        }

        private static class ViewHolder {

            public ImageView displayPhoto;
            public TextView photoCount;
            public TextView throwTime;
            public TextView mainAddress;
            public TextView detailAddress;

            public String throwPhotoId;
        }

        private class ShowAddressObserver implements GeocodeSearcher.AddressObserver {

            private final int mPosition;
            private final WeakReference<ViewHolder> mHolderRef;
            private final String mThrowPhotoId;

            public ShowAddressObserver(int position, ViewHolder holder) {
                mPosition = position;
                mHolderRef = new WeakReference<ViewHolder>(holder);
                mThrowPhotoId = holder.throwPhotoId;
            }

            @Override
            public void onSearch(boolean success, GeocodeSearcher.SearchAddress address) {
                final ViewHolder holder = mHolderRef.get();

                if (holder != null && success && address != null
                        && mThrowPhotoId.equals(holder.throwPhotoId)) {
                    mAddressArray.put(mPosition, address);
                    showAddress(holder, address);
                }
            }
        }
    }
}
