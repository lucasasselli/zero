package com.lucasasselli.zero.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.lucasasselli.zero.R;
import com.lucasasselli.zero.data.CatalogItem;
import com.lucasasselli.zero.utils.UrlFactory;

import java.util.ArrayList;
import java.util.List;

import static com.lucasasselli.zero.utils.StorageHelper.backgroundExist;

public class CatalogAdapter extends BaseAdapter {

    // Data
    private final List<CatalogItem> itemList;

    private final Context context;
    private final LayoutInflater inflater;

    public CatalogAdapter(Context context) {
        this.context = context;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        itemList = new ArrayList<>();
    }

    public void setContent(List<CatalogItem> itemList) {
        this.itemList.clear();

        this.itemList.addAll(itemList);

        notifyDataSetInvalidated();
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;

        if (vi == null) {
            vi = inflater.inflate(R.layout.catalog_cell, parent, false);
        }

        CatalogItem item = itemList.get(position);

        // Set title
        TextView title = (TextView) vi.findViewById(R.id.catalog_text_title);
        title.setText(item.getTitle());

        // Set author
        TextView author = (TextView) vi.findViewById(R.id.catalog_text_author);
        author.setText(item.getAuthor());

        // Set offline
        ImageView offline = (ImageView) vi.findViewById(R.id.catalog_img_offline);
        if (backgroundExist(item.getId(), context)) {
            offline.setVisibility(View.VISIBLE);
        } else {
            offline.setVisibility(View.INVISIBLE);
        }

        // Set pro
        TextView pro = (TextView) vi.findViewById(R.id.catalog_text_pro);
        int proVisibility = item.isPro() ? View.VISIBLE : View.INVISIBLE;
        pro.setVisibility(proVisibility);

        // Set thumbnail
        ImageView preview = (ImageView) vi.findViewById(R.id.catalog_preview);
        final ProgressBar progressBar = (ProgressBar) vi.findViewById(R.id.catalog_progress);
        String thumbnailUrl = UrlFactory.getThumbnailUrl(item);

        Glide.with(context)
                .load(thumbnailUrl)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .centerCrop()
                .placeholder(R.mipmap.empty)
                .crossFade()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(preview);

        return vi;
    }
}
