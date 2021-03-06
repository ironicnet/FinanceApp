package com.databases.example.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.data.CategoryRecord;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.SubCategoryRecord;

import java.util.ArrayList;

public class CategoriesListViewAdapter extends BaseExpandableListAdapter {
    private Cursor category;
    private final ArrayList<Cursor> subcategory;
    private final Context context;

    public CategoriesListViewAdapter(Context context, int textViewResourceId, Cursor cats, ArrayList<Cursor> subcats) {
        this.context = context;
        this.subcategory = subcats;
    }

    //My method for getting a Category Record at a certain position
    public CategoryRecord getCategory(long id) {
        Cursor group = category;

        group.moveToPosition((int) id);
        int NameColumn = group.getColumnIndex(DatabaseHelper.CATEGORY_NAME);
        int NoteColumn = group.getColumnIndex(DatabaseHelper.CATEGORY_NOTE);

        String itemId = group.getString(0);
        String itemName = group.getString(NameColumn);
        String itemNote = group.getString(NoteColumn);

        return new CategoryRecord(itemId, itemName, itemNote);
    }

    //My method for getting a Category Record at a certain position
    public SubCategoryRecord getSubCategory(int groupId, int childId) {
        Cursor group = subcategory.get(groupId);

        group.moveToPosition(childId);
        int ToIDColumn = group.getColumnIndex(DatabaseHelper.SUBCATEGORY_CAT_ID);
        int NameColumn = group.getColumnIndex(DatabaseHelper.SUBCATEGORY_NAME);
        int NoteColumn = group.getColumnIndex(DatabaseHelper.SUBCATEGORY_NOTE);

        //Log.e("HERE", "columns " + IDColumn + " " + ToIDColumn + " " + NameColumn + " " + NoteColumn);
        String itemId = group.getString(0);
        String itemTo_id = group.getString(ToIDColumn);
        String itemSubname = group.getString(NameColumn);
        String itemNote = group.getString(NoteColumn);

        return new SubCategoryRecord(itemId, itemTo_id, itemSubname, itemNote);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v = convertView;
        Cursor user = category;

        CategoryViewHolder viewHolder;

        //For Custom View Properties
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_category", true);

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.category_item, parent);

            viewHolder = new CategoryViewHolder();
            viewHolder.tvName = (TextView) v.findViewById(R.id.category_name);
            viewHolder.tvNote = (TextView) v.findViewById(R.id.category_note);

            v.setTag(viewHolder);
        } else {
            viewHolder = (CategoryViewHolder) v.getTag();
        }

        //Change Background Colors
        try {
            if (!useDefaults) {
                LinearLayout l;
                l = (LinearLayout) v.findViewById(R.id.category_layout);
                int startColor = prefs.getInt("key_category_startBackgroundColor", Color.parseColor("#FFFFFF"));
                int endColor = prefs.getInt("key_category_endBackgroundColor", Color.parseColor("#FFFFFF"));
                GradientDrawable defaultGradient = new GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{startColor, endColor});
                l.setBackgroundDrawable(defaultGradient);
            }


        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
        }

        //Change Size/Color of main field
        try {
            String DefaultSize = prefs.getString(context.getString(R.string.pref_key_category_nameSize), "24");

            if (useDefaults) {
                viewHolder.tvName.setTextSize(24);
            } else {
                viewHolder.tvName.setTextSize(Integer.parseInt(DefaultSize));
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        try {
            int DefaultColor = prefs.getInt("key_category_nameColor", Color.parseColor("#222222"));

            if (useDefaults) {
                viewHolder.tvName.setTextColor(Color.parseColor("#222222"));
            } else {
                viewHolder.tvName.setTextColor(DefaultColor);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        int NameColumn = user.getColumnIndex(DatabaseHelper.CATEGORY_NAME);
        int NoteColumn = user.getColumnIndex(DatabaseHelper.CATEGORY_NOTE);

        user.moveToPosition(groupPosition);
        String itemId = user.getString(0);
        String itemName = user.getString(NameColumn);
        String itemNote = user.getString(NoteColumn);
        //Log.d("getGroupView", "Found Category: " + itemName);

        //For User-Defined Field Visibility
        if (useDefaults || prefs.getBoolean("checkbox_category_nameField", true)) {
            viewHolder.tvName.setVisibility(View.VISIBLE);
            viewHolder.tvName.setText(itemName);
        } else {
            viewHolder.tvName.setVisibility(View.GONE);
        }

        if (prefs.getBoolean("checkbox_category_noteField", false) && !useDefaults && itemNote != null) {
            viewHolder.tvNote.setVisibility(View.VISIBLE);
            viewHolder.tvNote.setText(itemNote);
        } else {
            viewHolder.tvNote.setVisibility(View.GONE);
        }

        return v;
    }//end getView

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        Cursor temp = subcategory.get(groupPosition);
        temp.moveToPosition(childPosition);
        String itemId = temp.getString(0);

        //Log.d("getChildID", "returning " + Long.parseLong(itemId));

        return Long.parseLong(itemId);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        //Log.d("Cagtegories","groupPos: " + groupPosition + " childPos: " + childPosition + " isLastChild: " + isLastChild + " parent: " + parent);

        View v = convertView;
        Cursor user = subcategory.get(groupPosition);
        SubCategoryViewHolder viewHolder;

        //For Custom View Properties
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_subcategory", true);

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.subcategory_item, parent);

            viewHolder = new SubCategoryViewHolder();
            viewHolder.tvName = (TextView) v.findViewById(R.id.subcategory_name);
            viewHolder.tvNote = (TextView) v.findViewById(R.id.subcategory_note);
            viewHolder.tvCategory = (TextView) v.findViewById(R.id.subcategory_parent);
            v.setTag(viewHolder);
        } else {
            viewHolder = (SubCategoryViewHolder) v.getTag();
        }

        //Change Background Colors
        try {
            if (!useDefaults) {
                LinearLayout l;
                l = (LinearLayout) v.findViewById(R.id.subcategory_item_layout);
                int startColor = prefs.getInt("key_subcategory_startBackgroundColor", Color.parseColor("#FFFFFF"));
                int endColor = prefs.getInt("key_subcategory_endBackgroundColor", Color.parseColor("#FFFFFF"));
                GradientDrawable defaultGradient = new GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{startColor, endColor});
                l.setBackgroundDrawable(defaultGradient);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
        }

        //Change Size/color of main field
        try {
            String DefaultSize = prefs.getString(context.getString(R.string.pref_key_subcategory_nameSize), "24");

            if (useDefaults) {
                viewHolder.tvName.setTextSize(24);
            } else {
                viewHolder.tvName.setTextSize(Integer.parseInt(DefaultSize));
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        try {
            int DefaultColor = prefs.getInt("key_subcategory_nameColor", Color.parseColor("#000000"));

            if (useDefaults) {
                viewHolder.tvName.setTextColor(Color.parseColor("#000000"));
            } else {
                viewHolder.tvName.setTextColor(DefaultColor);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        int ToIDColumn = user.getColumnIndex(DatabaseHelper.SUBCATEGORY_CAT_ID);
        int NameColumn = user.getColumnIndex(DatabaseHelper.SUBCATEGORY_NAME);
        int NoteColumn = user.getColumnIndex(DatabaseHelper.SUBCATEGORY_NOTE);

        user.moveToPosition(childPosition);
        String itemId = user.getString(0);
        String itemTo_id = user.getString(ToIDColumn);
        String itemSubname = user.getString(NameColumn);
        String itemNote = user.getString(NoteColumn);
        //Log.d("getChildView", "Found SubCategory: " + itemSubname);

        if (useDefaults || prefs.getBoolean("checkbox_subcategory_nameField", true)) {
            viewHolder.tvName.setVisibility(View.VISIBLE);
            viewHolder.tvName.setText(itemSubname);
        } else {
            viewHolder.tvName.setVisibility(View.GONE);
        }
        if (prefs.getBoolean("checkbox_subcategory_noteField", false) && !useDefaults && itemNote != null) {
            viewHolder.tvNote.setVisibility(View.VISIBLE);
            viewHolder.tvNote.setText(itemNote);
        } else {
            viewHolder.tvNote.setVisibility(View.GONE);
        }

        viewHolder.tvCategory.setVisibility(View.GONE);

        return v;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return subcategory.get(groupPosition).getCount();
    }

    @Override
    public Object getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getGroupCount() {
        if (category == null) {
            return 0;
        }

        return category.getCount();
    }

    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void swapCategoryCursor(Cursor data) {
        category = data;
        notifyDataSetChanged();
    }

    public void swapSubCategoryCursor(Cursor data) {
        Log.e("Categories-swapSubCategoryCursor", "Cursor data=" + data + " data size=" + data.getCount());
        subcategory.add(data);
    }

}

//ViewHolder for Categories
class CategoryViewHolder {
    TextView tvName;
    TextView tvNote;
}

//ViewHolder for SubCategories
class SubCategoryViewHolder {
    TextView tvName;
    TextView tvCategory;
    TextView tvNote;
}
