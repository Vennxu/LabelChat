package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.PickPhotoUser;
import com.ekuater.labelchat.datastruct.ThrowPhoto;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ThrowPhotoManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.labelstory.HorizontalListView;
import com.ekuater.labelchat.ui.fragment.throwphoto.MyThrowPhotoDetailFragment;
import com.ekuater.labelchat.ui.fragment.throwphoto.Utils;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.util.GeocodeSearcher;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Leo on 2015/2/3.
 *
 * @author LinYong
 */
public class ThrowPhotoAdapter extends BaseAdapter
        implements AdapterView.OnItemClickListener {

    private final Context mContext;
    private final ThrowPhotoManager mThrowPhotoManager;
    private final LayoutInflater mInflater;
    private final GeocodeSearcher mGeocodeSearcher;
    private ArrayList <ThrowPhoto> mThrowPhotos=new ArrayList<ThrowPhoto>();
    private SparseArrayCompat<GeocodeSearcher.SearchAddress> mAddressArray;
    private DeleteListener mDeleteListener;

    public ThrowPhotoAdapter(Context context,DeleteListener deleteListener) {
        mContext = context;
        mThrowPhotoManager = ThrowPhotoManager.getInstance(context);
        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mGeocodeSearcher = GeocodeSearcher.getInstance(context);
        mDeleteListener=deleteListener;
    }
    public ThrowPhotoAdapter(Context context) {
        mContext = context;
        mThrowPhotoManager = ThrowPhotoManager.getInstance(context);
        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mGeocodeSearcher = GeocodeSearcher.getInstance(context);
    }
    public void updateThrowPhotos(ThrowPhoto[] throwPhotos) {
        if (throwPhotos!=null) {
            mThrowPhotos.addAll(Arrays.asList(throwPhotos));
            mAddressArray = (mThrowPhotos != null)
                    ? new SparseArrayCompat<GeocodeSearcher.SearchAddress>(mThrowPhotos.size())
                    : null;
            notifyDataSetChanged();
        }
    }
    public void deleteThrowPhoto(int position){
         mThrowPhotos.remove(position);
         notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return (mThrowPhotos != null) ? mThrowPhotos.size() : 0;
    }

    @Override
    public ThrowPhoto getItem(int position) {
        return (mThrowPhotos != null) ? mThrowPhotos.get(position) : null;
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
        holder.checkList = (HorizontalListView) view.findViewById(R.id.pick_photo_user);
        holder.divisionLine = view.findViewById(R.id.division_lines);
        holder.read = (ImageView) view.findViewById(R.id.read);
        holder.userQuantity = (TextView) view.findViewById(R.id.quantity);
        holder.pickPhotoUserArea = view.findViewById(R.id.show_pick_photo_user_area);
        holder.delete=(ImageView) view.findViewById(R.id.throw_photo_delete);
        view.setTag(holder);
        return view;
    }

    private View bindView(final int position, View view) {
        PickPhotoAdapter adapter = new PickPhotoAdapter(mContext);
        final ThrowPhoto throwPhoto = getItem(position);
        adapter.updateList(throwPhoto.getPhotoChecks());
        ViewHolder holder = (ViewHolder) view.getTag();
        if (throwPhoto.getPhotoChecks() == null) {
            holder.checkList.setVisibility(View.GONE);
            holder.divisionLine.setVisibility(View.GONE);
            holder.read.setVisibility(View.GONE);
            holder.userQuantity.setVisibility(View.GONE);
        }else{
            holder.checkList.setVisibility(View.VISIBLE);
            holder.divisionLine.setVisibility(View.VISIBLE);
            holder.read.setVisibility(View.VISIBLE);
            holder.userQuantity.setVisibility(View.VISIBLE);
        }
        holder.throwPhotoId = throwPhoto.getId();
        displayPhoto(throwPhoto.getDisplayPhoto(), holder.displayPhoto);
        displayAddress(position, throwPhoto, holder);
        holder.photoCount.setText(mContext.getString(R.string.photo_count,
                throwPhoto.getPhotoArray().length));
        holder.photoCount.setVisibility(throwPhoto.getPhotoArray().length > 1
                ? View.VISIBLE : View.GONE);
        holder.throwTime.setText(DateTimeUtils.getTimeString(
                mContext, throwPhoto.getThrowDate()));
        if (throwPhoto.getUserId().equals(SettingHelper.getInstance(mContext).getAccountUserId())){
            holder.pickPhotoUserArea.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.VISIBLE);
            holder.checkList.setAdapter(adapter);

        }else{
            holder.pickPhotoUserArea.setVisibility(View.GONE);
            holder.delete.setVisibility(View.GONE);
        }


        holder.pickPhotoUserArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UILauncher.launchFragmentPickPhotoCrowd(mContext, throwPhoto.getPhotoChecks(),mContext.getString(R.string.pick_photo_user));
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mDeleteListener.onDelete(throwPhoto.getId(),position);
            }
        });
        if (throwPhoto.getPhotoChecks() != null) {
            holder.userQuantity.setText("" + throwPhoto.getPhotoChecks().length);
        }
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
        Object item = parent.getAdapter().getItem(position);
        if (item instanceof ThrowPhoto) {
            ThrowPhoto throwPhoto = (ThrowPhoto) item;
            Bundle arguments = new Bundle();
            arguments.putParcelable(MyThrowPhotoDetailFragment.EXTRA_MY_THROW_PHOTO,
                    throwPhoto);
            UILauncher.launchFragmentInNewActivity(mContext, MyThrowPhotoDetailFragment.class,
                    arguments);
        }
    }

    private static class ViewHolder {

        public ImageView displayPhoto;
        public TextView photoCount;
        public TextView throwTime;
        public TextView mainAddress;
        public TextView detailAddress;

        public String throwPhotoId;
        public HorizontalListView checkList;
        public View divisionLine;
        public ImageView read;
        public ImageView delete;
        public TextView userQuantity;
        public View pickPhotoUserArea;
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
                if (mAddressArray != null) {
                    mAddressArray.put(mPosition, address);
                }
                showAddress(holder, address);
            }
        }
    }

    private class PickPhotoAdapter extends BaseAdapter {
        private AvatarManager mAvatarManager;
        private LayoutInflater mInflater;
        private PickPhotoUser[] mPhotoChecks;

        public PickPhotoAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mAvatarManager = AvatarManager.getInstance(context);
        }

        public synchronized void updateList(PickPhotoUser[] photoChecks) {
            mPhotoChecks = photoChecks;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mPhotoChecks == null ? 0 : mPhotoChecks.length;
        }

        @Override
        public PickPhotoUser getItem(int position) {
            return mPhotoChecks == null ? null : mPhotoChecks[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.labelstory_praise_user_image, parent, false);
            }
            CircleImageView userAvatarImage = (CircleImageView) convertView.findViewById(R.id.labelstory_praise_iamge);
            MiscUtils.showAvatarThumb(mAvatarManager, getItem(position).getPickUserAvatarThumb(), userAvatarImage);
            return convertView;
        }

    }
    public interface DeleteListener{
        public void onDelete(String throwPhotoId,int position);
    }
}
