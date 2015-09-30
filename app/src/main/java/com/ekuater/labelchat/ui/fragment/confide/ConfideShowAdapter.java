package com.ekuater.labelchat.ui.fragment.confide;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ConfideManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.fragment.labelstory.CustomListView;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.ClickEventInterceptLinear;
import com.ekuater.labelchat.util.TextUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Administrator on 2015/4/7.
 */
public class ConfideShowAdapter extends BaseAdapter{

    private LayoutInflater inflater;
    private Context context;
    private ArrayList<Confide> mConfides;
    private AvatarManager mAvatarManager;
    private View.OnClickListener onClickListener;

    public ConfideShowAdapter(Context context, View.OnClickListener onClickListener,boolean flag){
        this.context = context;
        this.onClickListener = onClickListener;
        mAvatarManager = AvatarManager.getInstance(context);
    }


    public ConfideShowAdapter(Context context, View.OnClickListener onClickListener){
        this.context = context;
        inflater = LayoutInflater.from(context);
        mConfides = new ArrayList<Confide>();
        mAvatarManager = AvatarManager.getInstance(context);
        this.onClickListener = onClickListener;
    }

    public void notifyAdapterList(Confide[] confides, CustomListView listView, int flags){
            List<Confide> list = Arrays.asList(confides);
            if (list != null) {
                listView.setCanLoadMore(list.size() < 20 ? false : true);
                switch (flags) {
                    case ConfideUtils.REFRESH:
                        listView.onRefreshComplete();
                        mConfides.clear();
                        mConfides.addAll(list);
                        break;

                    case ConfideUtils.LOADING:
                        listView.onLoadMoreComplete();
                        mConfides.addAll(list);
                        break;
                }
                notifyDataSetChanged();
            }
    }

    public void addAdapterList(Confide confide, int index){
        mConfides.remove(index);
        mConfides.add(index,confide);
        notifyDataSetChanged();
    }

    public void addSendConfide(Confide confide){
        mConfides.add(0,confide);
        notifyDataSetChanged();
    }

    public void removeConfide(int position){
        mConfides.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mConfides == null ? 0:mConfides.size();
    }

    @Override
    public Confide getItem(int position) {
        return mConfides.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.confide_show_item, parent, false);
            bindView(holder, convertView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        final Confide confide = getItem(position);
        bindDate(holder, confide, position);
        return convertView;
    }

    public void bindView(ViewHolder holder, View parent){
        holder.linearLayout = (RelativeLayout) parent.findViewById(R.id.confide_show_item_parent);
        holder.tx = (CircleImageView) parent.findViewById(R.id.confide_show_item_tx);
        holder.role = (TextView) parent.findViewById(R.id.confide_show_item_role);
        holder.time = (TextView) parent.findViewById(R.id.confide_show_item_time);
        holder.area = (TextView) parent.findViewById(R.id.confide_show_item_area);
        holder.commentNum = (TextView) parent.findViewById(R.id.operation_bar_comment_num);
        holder.praiseNum = (TextView) parent.findViewById(R.id.operation_bar_praise_num);
        holder.praise = (ImageView) parent.findViewById(R.id.operation_bar_praise);
        holder.operationBar = (LinearLayout) parent.findViewById(R.id.operation_bar);
        holder.praiseLinear = (LinearLayout) parent.findViewById(R.id.operation_bar_comment_linear);
        holder.commentNum = (TextView) parent.findViewById(R.id.operation_bar_comment_num);
        holder.praiseNum = (TextView) parent.findViewById(R.id.operation_bar_praise_num);
        holder.praise = (ImageView) parent.findViewById(R.id.operation_bar_praise);
        holder.commentLinear = (ClickEventInterceptLinear) parent.findViewById(R.id.operation_bar_comment_parent);
        holder.comment = (ImageView) parent.findViewById(R.id.operation_bar_comment);
        holder.letter = (ImageView) parent.findViewById(R.id.operation_bar_letter);
        holder.letterNum = (TextView) parent.findViewById(R.id.operation_bar_letter_num);
        holder.more = (ImageView) parent.findViewById(R.id.operation_bar_more);
        holder.content = (TextView) parent.findViewById(R.id.confide_show_item_content);
        holder.praise.setImageResource(R.drawable.ic_praise_white);
        holder.more.setImageResource(R.drawable.ic_more_white);
        holder.comment.setImageResource(R.drawable.ic_translation_white);
        holder.commentNum.setTextColor(Color.WHITE);
        holder.praiseNum.setTextColor(Color.WHITE);
        holder.letterNum.setTextColor(Color.WHITE);
        holder.letter.setVisibility(View.GONE);
        holder.operationBar.setBackgroundResource(0);
        holder.more.setOnClickListener(onClickListener);
        holder.praise.setOnClickListener(onClickListener);
        holder.commentLinear.setOnClickListener(onClickListener);

    }

    public void bindDate(ViewHolder holder, Confide confide, int position){
        if (TextUtil.isEmpty(confide.getConfideBgImg())){
            holder.linearLayout.setBackgroundColor(confide.parseBgColor());
        }else{
            holder.linearLayout.setBackgroundResource(ConfideManager.getInstance(context).getConfideBs().get(confide.getConfideBgImg()));
        }
        holder.role.setText(String.format(context.getString(R.string.confide_role), confide.getConfideRole()));
        holder.time.setText(getDateTime(confide.getConfideCreateDate()));
        holder.commentNum.setText(confide.getConfideCommentNum()+"");
        holder.commentNum.setVisibility(confide.getConfideCommentNum() == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.praiseNum.setVisibility(confide.getConfidePraiseNum() == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.praiseNum.setText(confide.getConfidePraiseNum() + "");
        holder.letterNum.setText(TextUtils.isEmpty(confide.getConfidePosition())
                ? context.getString(R.string.confide_unknown) : confide.getConfidePosition());
        holder.praise.setImageResource(confide.getConfideIsPraise().equals("Y") ? R.drawable.ic_praise_pressed : R.drawable.ic_praise_white);
        holder.content.setText(confide.getConfideContent());
        holder.area.setText(TextUtils.isEmpty(confide.getConfidePosition()) ? context.getString(R.string.confide_unknown) : confide.getConfidePosition());
        if (SettingHelper.getInstance(context).getAccountUserId().equals(confide.getConfideUserId())){
            MiscUtils.showAvatarThumb(mAvatarManager, SettingHelper.getInstance(context).getAccountAvatarThumb(), holder.tx,R.drawable.contact_single);
        }else{
            holder.tx.setImageResource(getUserTx(confide.getConfideSex()));
        }
        holder.praise.setTag(position);
    }

    private int getUserTx(String sex){
        return sex.equals("2") ? R.drawable.confide_female : R.drawable.confide_male;
    }

    private String getDateTime(long time){
        return DateTimeUtils.getDescriptionTimeFromTimestamp(context, time);
    }

    public static class ViewHolder{
        public RelativeLayout linearLayout;
        public CircleImageView tx;
        public TextView time;
        public TextView role;
        public TextView area;
        public ImageView praise;
        public ImageView letter;
        public ImageView more;
        public ImageView comment;
        public LinearLayout operationBar;
        public ClickEventInterceptLinear commentLinear;
        public LinearLayout praiseLinear;
        public TextView praiseNum;
        public TextView commentNum;
        public TextView letterNum;
        public TextView content;
    }
}
