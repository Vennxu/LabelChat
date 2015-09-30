package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.widget.LabelImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2015/2/4.
 *
 * @author Fan Chong
 */
public class StrangerLabelAdapter extends BaseAdapter
        implements AdapterView.OnItemClickListener {

    private LayoutInflater mInflater;
    private Context mContext;
    private List<UserLabel> mUserLabelList;
    private UserLabelManager mLabelManager;

    private final Comparator<UserLabel> mComparator = new Comparator<UserLabel>() {
        @Override
        public int compare(UserLabel lhs, UserLabel rhs) {
            long diff = rhs.getIntegral() - lhs.getIntegral();
            diff = (diff != 0) ? diff : (rhs.getTime() - lhs.getTime());
            return (diff > 0) ? 1 : (diff < 0) ? -1 : 0;
        }
    };

    public StrangerLabelAdapter(Context context, Stranger stranger) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mUserLabelList = new ArrayList<UserLabel>();
        if (stranger.getLabels() != null) {
            Collections.addAll(mUserLabelList, stranger.getLabels());
        }
        sortUserLabels(mUserLabelList);
        mLabelManager = UserLabelManager.getInstance(context);
    }

    private void sortUserLabels(List<UserLabel> labelList) {
        Collections.sort(labelList, mComparator);
    }

    @Override
    public int getCount() {
        return mUserLabelList == null ? 0 : mUserLabelList.size();
    }

    @Override
    public UserLabel getItem(int position) {
        return mUserLabelList == null ? null : mUserLabelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView(parent);
        }
        bindView(position, convertView);
        return convertView;
    }

    private View newView(ViewGroup parent) {
        View view = mInflater.inflate(R.layout.fragment_show_label_item, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.labelImageView = (LabelImageView) view.findViewById(R.id.label_avatar_image);
        holder.labelName = (TextView) view.findViewById(R.id.label_name);
        holder.labelDynamic = (TextView) view.findViewById(R.id.label_dynamic);
        holder.labelLevel = (ImageView) view.findViewById(R.id.show_level);
        holder.labelIntegral = (TextView) view.findViewById(R.id.label_integral);
        holder.gradeBar = (RatingBar) view.findViewById(R.id.grade_bar);
        holder.gradeText = (TextView) view.findViewById(R.id.grade_text);
        view.setTag(holder);
        return view;
    }

    private void bindView(int position, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        UserLabel userLabel = getItem(position);
        String labelImage = userLabel.getImage();
        holder.labelName.setText(userLabel.getName());
        holder.labelIntegral.setText(String.valueOf(userLabel.getIntegral()));
        holder.labelLevel.getDrawable().setLevel(userLabel.getIntegral());
        if (TextUtils.isEmpty(labelImage)) {
            holder.labelImageView.setImageResource(R.drawable.label_ic);
        } else {
            mLabelManager.displayLabelImage(labelImage, holder.labelImageView,
                    R.drawable.label_ic);
        }

        float grade = userLabel.getAverageGrade();
        holder.gradeBar.setRating(getRatingGrade(grade));
        holder.gradeText.setText(String.valueOf(grade));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object item = parent.getAdapter().getItem(position);
        if (item instanceof UserLabel) {
            UserLabel label = (UserLabel) item;
            UILauncher.launchFragmentLabelStoryUI(mContext, label.toBaseLabel(), null);
        }
    }

    private float getRatingGrade(float grade) {
        float offset = grade - (int) grade;
        return (int) grade + ((offset > 0) ? 0.5F : 0.0F);
    }

    private class ViewHolder {

        LabelImageView labelImageView;
        TextView labelName, labelDynamic, labelIntegral;
        ImageView labelLevel;
        RatingBar gradeBar;
        TextView gradeText;
    }
}
