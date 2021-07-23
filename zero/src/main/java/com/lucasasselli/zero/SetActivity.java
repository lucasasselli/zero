package com.lucasasselli.zero;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.transition.TransitionManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lucasasselli.zero.data.CatalogItem;
import com.lucasasselli.zero.utils.StorageHelper;
import com.lucasasselli.zero.wallpaper.GLWallpaperPreview;

import java.io.File;

import static com.lucasasselli.zero.Constants.PREF_BACKGROUND;
import static com.lucasasselli.zero.Constants.SERVICE_NAME;
import static com.lucasasselli.zero.Utils.openLWSetter;

public class SetActivity extends AppCompatActivity {

    // Constants
    public static final String EXTRA_IS_CUSTOM = "custom";
    public static final String EXTRA_CATALOG_ITEM = "item";

    private Context context;

    // Layout
    private TextView titleText;
    private TextView authorText;
    private ConstraintLayout textBox;
    private GLWallpaperPreview wpPreview;

    private SharedPreferences sharedPreferences;
    private CatalogItem catalogItem;

    private boolean textBoxVisible = true;
    private boolean isCustom = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_set);

        context = this;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // First execution, grab intent extra
        Intent intent = getIntent();
        if (intent != null) {
            isCustom = intent.getBooleanExtra(EXTRA_IS_CUSTOM, false);
            catalogItem = intent.getParcelableExtra(EXTRA_CATALOG_ITEM);
        } else {
            finish();
            return;
        }

        textBox = (ConstraintLayout) findViewById(R.id.set_text_box);

        wpPreview = (GLWallpaperPreview) findViewById(R.id.set_wppreview);

        if (!isCustom) {
            // Show textbox
            textBox.setVisibility(View.VISIBLE);

            // Set wallpaper preview
            wpPreview.init(catalogItem.getId());

            // Set title text
            titleText = (TextView) findViewById(R.id.set_text_title);
            titleText.setText(catalogItem.getTitle());

            // Set author text
            authorText = (TextView) findViewById(R.id.set_text_author);
            authorText.setText(catalogItem.getAuthor());

            // Add magic button
            wpPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TransitionManager.beginDelayedTransition(textBox);
                    textBoxVisible = !textBoxVisible;
                    textBox.setVisibility(textBoxVisible ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // Hide text box
            textBox.setVisibility(View.GONE);

            // Set wallpaper preview
            wpPreview.init(Constants.BG_CUSTOM_ID);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        wpPreview.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        wpPreview.stop();
    }


    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.set_menu, menu);

        // Hide delete for custom wallpapers
        if (isCustom) {
            MenuItem deleteItem = menu.findItem(R.id.set_menu_delete);
            deleteItem.setVisible(false);
            MenuItem linkItem = menu.findItem(R.id.set_menu_link);
            linkItem.setVisible(false);
        } else {
            if (catalogItem.getSite() == null || catalogItem.getSite().equals("")) {
                MenuItem menuItem = menu.findItem(R.id.set_menu_link);
                menuItem.setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.set_menu_set:
                // Set wallpaper
                setWallpaper();
                return true;

            case R.id.set_menu_settings:
                // Start settings
                Intent intent = new Intent(context, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.set_menu_delete:
                // Delete local content
                deleteWallpaper();
                return true;

            case R.id.set_menu_link:
                // Open browser to author's website
                Utils.openBrowser(context, catalogItem.getSite());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Start wallpaper setting routine + interface
    private void setWallpaper() {
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        final WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();

        String wallpaperId;
        if (isCustom) {
            wallpaperId = Constants.BG_CUSTOM_ID;
        } else {
            wallpaperId = catalogItem.getId();
        }

        sharedPreferences.edit().putString(PREF_BACKGROUND, wallpaperId).apply();

        boolean zeroAsLive = true;

        // Check if Zero is the current live wallpaper
        if (wallpaperInfo == null) {
            zeroAsLive = false;
        } else {
            if (!wallpaperInfo.getServiceName().equals(SERVICE_NAME)) {
                zeroAsLive = false;
            }
        }

        if (!zeroAsLive) {
            // Show dialog
            new AlertDialog.Builder(this)
                    .setTitle(R.string.set_dialog_notzero_title)
                    .setMessage(R.string.set_dialog_notzero_message)
                    .setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Show live-wallpaper preview
                            openLWSetter(context);
                        }
                    })
                    .setNegativeButton(R.string.common_cancel, null)
                    .show();
        } else {
            Toast.makeText(context, R.string.set_alert_changed, Toast.LENGTH_SHORT).show();
        }
    }

    // Start wallpaper delete routine + interface
    private void deleteWallpaper() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.set_dialog_delete_title)
                .setMessage(R.string.set_dialog_delete_message)
                .setPositiveButton(R.string.common_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Show live-wallpaper preview
                        File file = StorageHelper.getBackgroundFolder(catalogItem.getId(), context);
                        StorageHelper.deleteFolder(file);
                        finish();
                    }
                })
                .setNegativeButton(R.string.common_cancel, null)
                .show();
    }
}
