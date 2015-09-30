package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.ui.UILauncher;

/**
 * Created by Administrator on 2015/2/4.
 *
 * @author FanChong
 */
public class StrangerLabelPage extends BasePage {
    private View mBackgroundView;
    private Stranger mStranger;

    private StrangerLabelAdapter adapter;

    public StrangerLabelPage(Fragment fragment, Stranger stranger) {
        super(fragment);
        mStranger = stranger;
        adapter = new StrangerLabelAdapter(mContext, stranger);
    }

    @Override
    public ListAdapter getContentAdapter() {
        return adapter;
    }

    @Override
    public AdapterView.OnItemClickListener getContentItemClickListener() {
        return adapter;
    }

    @Override
    public void onAddToContentForeground(ViewGroup container) {
        if (mBackgroundView == null) {
            mBackgroundView = LayoutInflater.from(mContext).inflate(
                    R.layout.other_no_label_layout, container, false);
        }
        ViewGroup parent = (ViewGroup) mBackgroundView.getParent();
        if (parent != null) {
            parent.removeView(mBackgroundView);
        }
        container.addView(mBackgroundView);
        mBackgroundView.setVisibility(mStranger.getLabels() != null
                ? View.GONE : View.VISIBLE);
    }
}
