package com.ekuater.labelchat.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.activity.base.TitleIconActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * custom overflow option menu, use popup window instead of
 * system action bar overflow menu
 *
 * @author LinYong
 */
public class OverflowMenuActivity extends TitleIconActivity {

    // private static final String TAG = OverflowMenuActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        boolean handled = super.onCreatePanelMenu(featureId, menu);

        if (featureId == Window.FEATURE_OPTIONS_PANEL
                && createOverflowMenu()) {
            getMenuInflater().inflate(R.menu.overflow_menu, menu);
            handled |= true;
        }

        return handled;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == Window.FEATURE_OPTIONS_PANEL
                && item.getItemId() == R.id.menu_more_overflow_menu) {
            handleOverflowMenuSelected();
            return true;
        } else {
            return super.onMenuItemSelected(featureId, item);
        }
    }

    private static final class MenuImpl implements Menu {

        private Context mContext;
        private ArrayList<MenuItemImpl> mItems;

        private static int findInsertIndex(ArrayList<MenuItemImpl> items, int order) {
            for (int i = items.size() - 1; i >= 0; i--) {
                MenuItemImpl item = items.get(i);
                if (item.getOrder() <= order) {
                    return i + 1;
                }
            }

            return 0;
        }

        public MenuImpl(Context context) {
            mContext = context;
            mItems = new ArrayList<MenuItemImpl>();
        }

        public Context getContext() {
            return mContext;
        }

        @Override
        public MenuItem add(CharSequence title) {
            return add(0, 0, 0, title);
        }

        @Override
        public MenuItem add(int titleRes) {
            return add(0, 0, 0, titleRes);
        }

        @Override
        public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
            MenuItemImpl item = new MenuItemImpl(mContext, groupId, itemId, order, title);
            mItems.add(findInsertIndex(mItems, order), item);
            return item;
        }

        @Override
        public MenuItem add(int groupId, int itemId, int order, int titleRes) {
            return add(groupId, itemId, order, mContext.getResources().getString(titleRes));
        }

        @Override
        public SubMenu addSubMenu(CharSequence title) {
            return null;
        }

        @Override
        public SubMenu addSubMenu(int titleRes) {
            return null;
        }

        @Override
        public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
            return null;
        }

        @Override
        public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
            return null;
        }

        @Override
        public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller,
                                    Intent[] specifics, Intent intent, int flags,
                                    MenuItem[] outSpecificItems) {
            return 0;
        }

        @Override
        public void removeItem(int id) {
            mItems.remove(findItemIndex(id));
        }

        @Override
        public void removeGroup(int groupId) {
            final ArrayList<MenuItemImpl> items = mItems;
            int itemCount = items.size();
            int i = 0;

            while (i < itemCount) {
                if (items.get(i).getGroupId() == groupId) {
                    items.remove(i);
                    itemCount--;
                } else {
                    i++;
                }
            }
        }

        @Override
        public void clear() {
            mItems.clear();
        }

        @Override
        public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
            for (MenuItemImpl item : mItems) {
                if (item.getGroupId() == group) {
                    item.setCheckable(checkable);
                    item.setExclusiveCheckable(exclusive);
                }
            }
        }

        @Override
        public void setGroupVisible(int group, boolean visible) {
            for (MenuItemImpl item : mItems) {
                if (item.getGroupId() == group) {
                    item.setVisible(visible);
                }
            }
        }

        @Override
        public void setGroupEnabled(int group, boolean enabled) {
            for (MenuItemImpl item : mItems) {
                if (item.getGroupId() == group) {
                    item.setEnabled(enabled);
                }
            }
        }

        @Override
        public boolean hasVisibleItems() {
            for (MenuItemImpl item : mItems) {
                if (item.isVisible()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public MenuItem findItem(int id) {
            return mItems.get(findItemIndex(id));
        }

        @Override
        public int size() {
            return mItems.size();
        }

        @Override
        public MenuItem getItem(int index) {
            return mItems.get(index);
        }

        @Override
        public void close() {
        }

        @Override
        public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
            return false;
        }

        @Override
        public boolean isShortcutKey(int keyCode, KeyEvent event) {
            return false;
        }

        @Override
        public boolean performIdentifierAction(int id, int flags) {
            return false;
        }

        @Override
        public void setQwertyMode(boolean isQwerty) {
        }

        private int findItemIndex(int id) {
            final ArrayList<MenuItemImpl> items = mItems;
            final int itemCount = items.size();

            for (int i = 0; i < itemCount; i++) {
                if (items.get(i).getItemId() == id) {
                    return i;
                }
            }

            return -1;
        }
    }

    private static final class MenuItemImpl implements MenuItem {

        private static final int CHECKABLE = 0x00000001;
        private static final int CHECKED = 0x00000002;
        private static final int EXCLUSIVE = 0x00000004;
        private static final int HIDDEN = 0x00000008;
        private static final int ENABLED = 0x00000010;

        private static final int NO_ICON = 0;

        private final int mId;
        private final int mGroup;
        private final int mOrder;

        private CharSequence mTitle;
        private CharSequence mTitleCondensed;
        private Intent mIntent;
        private char mShortcutNumericChar;
        private char mShortcutAlphabeticChar;
        private Drawable mIconDrawable;
        private int mIconResId = NO_ICON;
        private Context mContext;
        private int mFlags = ENABLED;

        public MenuItemImpl(Context context, int group, int id, int order,
                            CharSequence title) {
            mContext = context;
            mId = id;
            mGroup = group;
            mOrder = order;
            mTitle = title;
        }

        @Override
        public char getAlphabeticShortcut() {
            return mShortcutAlphabeticChar;
        }

        @Override
        public int getGroupId() {
            return mGroup;
        }

        @Override
        public Drawable getIcon() {
            if (mIconDrawable != null) {
                return mIconDrawable;
            } else if (mIconResId != NO_ICON) {
                Drawable icon = mContext.getResources().getDrawable(mIconResId);
                mIconResId = NO_ICON;
                mIconDrawable = icon;
                return icon;
            } else {
                return null;
            }
        }

        @Override
        public Intent getIntent() {
            return mIntent;
        }

        @Override
        public int getItemId() {
            return mId;
        }

        @Override
        public ContextMenuInfo getMenuInfo() {
            return null;
        }

        @Override
        public char getNumericShortcut() {
            return mShortcutNumericChar;
        }

        @Override
        public int getOrder() {
            return mOrder;
        }

        @Override
        public SubMenu getSubMenu() {
            return null;
        }

        @Override
        public CharSequence getTitle() {
            return mTitle;
        }

        @Override
        public CharSequence getTitleCondensed() {
            return mTitleCondensed != null ? mTitleCondensed : mTitle;
        }

        @Override
        public boolean hasSubMenu() {
            return false;
        }

        @Override
        public boolean isCheckable() {
            return (mFlags & CHECKABLE) != 0;
        }

        @Override
        public boolean isChecked() {
            return (mFlags & CHECKED) != 0;
        }

        @Override
        public boolean isEnabled() {
            return (mFlags & ENABLED) != 0;
        }

        @Override
        public boolean isVisible() {
            return (mFlags & HIDDEN) == 0;
        }

        @Override
        public MenuItem setAlphabeticShortcut(char alphaChar) {
            mShortcutAlphabeticChar = alphaChar;
            return this;
        }

        @Override
        public MenuItem setCheckable(boolean checkable) {
            mFlags = (mFlags & ~CHECKABLE) | (checkable ? CHECKABLE : 0);
            return this;
        }

        public void setExclusiveCheckable(boolean exclusive) {
            mFlags = (mFlags & ~EXCLUSIVE) | (exclusive ? EXCLUSIVE : 0);
        }

        public boolean isExclusiveCheckable() {
            return (mFlags & EXCLUSIVE) != 0;
        }

        @Override
        public MenuItem setChecked(boolean checked) {
            mFlags = (mFlags & ~CHECKED) | (checked ? CHECKED : 0);
            return this;
        }

        @Override
        public MenuItem setEnabled(boolean enabled) {
            mFlags = (mFlags & ~ENABLED) | (enabled ? ENABLED : 0);
            return this;
        }

        @Override
        public MenuItem setIcon(Drawable icon) {
            mIconDrawable = icon;
            mIconResId = NO_ICON;
            return this;
        }

        @Override
        public MenuItem setIcon(int iconRes) {
            mIconResId = iconRes;
            mIconDrawable = null;
            return this;
        }

        @Override
        public MenuItem setIntent(Intent intent) {
            mIntent = intent;
            return this;
        }

        @Override
        public MenuItem setNumericShortcut(char numericChar) {
            mShortcutNumericChar = numericChar;
            return this;
        }

        @Override
        public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
            return this;
        }

        @Override
        public MenuItem setShortcut(char numericChar, char alphaChar) {
            mShortcutNumericChar = numericChar;
            mShortcutAlphabeticChar = alphaChar;
            return this;
        }

        @Override
        public MenuItem setTitle(CharSequence title) {
            mTitle = title;
            return this;
        }

        @Override
        public MenuItem setTitle(int title) {
            mTitle = mContext.getResources().getString(title);
            return this;
        }

        @Override
        public MenuItem setTitleCondensed(CharSequence title) {
            mTitleCondensed = title;
            return this;
        }

        @Override
        public MenuItem setVisible(boolean visible) {
            mFlags = (mFlags & ~HIDDEN) | (visible ? 0 : HIDDEN);
            return this;
        }

        @Override
        public void setShowAsAction(int show) {
            // Do nothing. ActionMenuItems always show as action buttons.
        }

        @Override
        public MenuItem setActionView(View actionView) {
            // Do nothing. ActionMenuItems always show as action buttons.
            return this;
        }

        @Override
        public View getActionView() {
            return null;
        }

        @Override
        public MenuItem setActionView(int resId) {
            // Do nothing. ActionMenuItems always show as action buttons.
            return this;
        }

        @Override
        public ActionProvider getActionProvider() {
            return null;
        }

        @Override
        public MenuItem setActionProvider(ActionProvider actionProvider) {
            // Do nothing. ActionMenuItems always show as action buttons.
            return this;
        }

        @Override
        public MenuItem setShowAsActionFlags(int actionEnum) {
            setShowAsAction(actionEnum);
            return this;
        }

        @Override
        public boolean expandActionView() {
            return false;
        }

        @Override
        public boolean collapseActionView() {
            return false;
        }

        @Override
        public boolean isActionViewExpanded() {
            return false;
        }

        @Override
        public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
            // No need to save the listener; ActionMenuItem does not support collapsing items.
            return this;
        }
    }

    private static final class OverflowMenuAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;
        private List<MenuItem> mMenuItems;
        private boolean mShowIcon = false;
        private int mIconWidth;

        public OverflowMenuAdapter(Context context, List<MenuItem> items) {
            super();
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mMenuItems = items;
        }

        public void setShowIcon(boolean show, int width) {
            mShowIcon = show;
            mIconWidth = width;
        }

        @Override
        public int getCount() {
            return mMenuItems.size();
        }

        @Override
        public MenuItem getItem(int position) {
            return mMenuItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getItemId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.overflow_menu_item_layout,
                        parent, false);
            }
            bindView(getItem(position), convertView);
            return convertView;
        }

        private void bindView(MenuItem menuItem, View convertView) {
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            TextView title = (TextView) convertView.findViewById(R.id.title);

            title.setText(menuItem.getTitle());
            icon.setImageDrawable(menuItem.getIcon());

            if (mIconWidth > 0) {
                ViewGroup.LayoutParams iconLp = icon.getLayoutParams();
                iconLp.width = mIconWidth;
            }
            icon.setVisibility(mShowIcon ? View.VISIBLE : View.GONE);
        }
    }

    private MenuImpl mOverflowMenu;
    private PopupWindow mMenuPopup;
    private final PopupWindow.OnDismissListener mDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            mMenuPopup = null;
        }
    };
    private final AdapterView.OnItemClickListener mMenuItemClickListener
            = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object object = parent.getItemAtPosition(position);
            if (object instanceof MenuItem) {
                MenuItem item = (MenuItem) object;
                handleOverflowMenuItemSelected(item);
            }
            dismissOverflowMenu();
        }
    };

    protected boolean onCreateOverflowMenu(Menu menu) {
        return false;
    }

    protected boolean onOverflowMenuItemSelected(MenuItem item) {
        return false;
    }

    protected final MenuItem getOverflowMenuItem(int itemId) {
        MenuItem item = null;

        if (mOverflowMenu != null) {
            item = mOverflowMenu.findItem(itemId);
        }

        return item;
    }

    private boolean createOverflowMenu() {
        if (mOverflowMenu == null) {
            mOverflowMenu = new MenuImpl(this);
        }
        mOverflowMenu.clear();
        return onCreateOverflowMenu(mOverflowMenu);
    }

    private void handleOverflowMenuItemSelected(MenuItem item) {
        onOverflowMenuItemSelected(item);
    }

    private void handleOverflowMenuSelected() {
        if (!mOverflowMenu.hasVisibleItems()) {
            return;
        }

        if (mMenuPopup == null) {
            showOverflowMenu();
        } else {
            dismissOverflowMenu();
        }
    }

    private void showOverflowMenu() {
        List<MenuItem> menuItems = buildOverflowMenuItem(mOverflowMenu);
        if (menuItems.size() <= 0) {
            return;
        }

        OverflowMenuAdapter adapter = new OverflowMenuAdapter(this, menuItems);
        ListView menuList = (ListView) getLayoutInflater().inflate(
                R.layout.overflow_menu_list, null);

        menuList.setOnItemClickListener(mMenuItemClickListener);
        menuList.setAdapter(adapter);

        mMenuPopup = new PopupWindow(this, null, R.attr.overflowMenuStyle);
        mMenuPopup.setContentView(menuList);
        mMenuPopup.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mMenuPopup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setPopupContentWidth(mMenuPopup, measureMenuListWidth(menuList, adapter));
        mMenuPopup.setFocusable(true);
        mMenuPopup.setOnDismissListener(mDismissListener);
        mMenuPopup.showAsDropDown(findViewById(R.id.menu_more_overflow_menu));
    }

    private void dismissOverflowMenu() {
        if (mMenuPopup.isShowing()) {
            mMenuPopup.dismiss();
        }
    }

    private void setPopupContentWidth(PopupWindow popup, int contentWidth) {
        Drawable popupBackground = popup.getBackground();
        int width = contentWidth;

        if (popupBackground != null) {
            Rect rect = new Rect();
            popupBackground.getPadding(rect);
            width = rect.left + rect.right + contentWidth;
        }

        popup.setWidth(width);
    }

    private int measureMenuListWidth(ListView list, OverflowMenuAdapter adapter) {
        final FrameLayout measureParent = new FrameLayout(this);
        final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        final int count = adapter.getCount();

        int maxWidth = 0;
        int itemType = 0;
        int maxIconWidth = 0;
        boolean showIcon = false;
        View itemView = null;

        // measure icon view max width
        adapter.setShowIcon(true, -1);
        for (int i = 0; i < count; i++) {
            final int positionType = adapter.getItemViewType(i);
            final MenuItem item = adapter.getItem(i);

            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }

            itemView = adapter.getView(i, itemView, measureParent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);
            maxIconWidth = Math.max(itemView.findViewById(R.id.icon).getMeasuredWidth(),
                    maxIconWidth);
            if (item.getIcon() != null) {
                showIcon = true;
            }
        }

        // measure item view max width
        adapter.setShowIcon(showIcon, maxIconWidth);
        for (int i = 0; i < count; i++) {
            final int positionType = adapter.getItemViewType(i);

            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }

            itemView = adapter.getView(i, itemView, measureParent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);
            maxWidth = Math.max(itemView.getMeasuredWidth(), maxWidth);
        }

        Drawable listBg = list.getBackground();
        if (listBg != null) {
            Rect rect = new Rect();
            listBg.getPadding(rect);
            maxWidth += rect.left + rect.right;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            maxWidth = Math.max(list.getMeasuredWidth(), maxWidth);
        } else {
            maxWidth = Math.max(list.getMinimumWidth(), maxWidth);
        }
        //adapter.setShowIcon(showIcon, maxIconWidth);

        return maxWidth;
    }

    private List<MenuItem> buildOverflowMenuItem(Menu menu) {
        List<MenuItem> menuItemList = new ArrayList<MenuItem>();

        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item.isVisible()) {
                menuItemList.add(item);
            }
        }

        return menuItemList;
    }
}
