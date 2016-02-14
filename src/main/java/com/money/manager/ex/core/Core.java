/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.money.manager.ex.core;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
//import net.sqlcipher.database.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.database.MmexOpenHelper;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.database.TableSubCategory;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.domainmodel.Subcategory;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.shamanland.fonticon.FontIconDrawable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Core {

    private static final String LOGCAT = Core.class.getSimpleName();
//    private static final String base64 = "A346EA3CFF9C3F679946C4294AAF737D1E2708694B66A13D1A615F7C0752F4770664F8407DDCE84AB1DE2770DA62760D2ACF6F45F9DCDDB7E5018D9F5FCD561544C6F4BDFF7801BC1DF65A97C40AC293A3C097C4E3F3CDD5366EAB72B29CA78D234B69C30047201323D7070BCC42EA02A9903955783CC701FB3B3CCB9855B16D70EAC0931668FD33E1C7634EAEECAC93D1980D3B81DB10A50905815A54FFEAE951DDA6862561B971D1F439FE49A146F33FF661E79533F574F187377DE7229433ABBFF7ACEC1C05BC38520BB537D1418239077A696AA00E415980922AD575B39E83B70B04C836D2E9BD9398884469A27E5826DAF1FF77D6DE687BE17ACA2243F25901168FD37E14717D38737A963175E4131B819B475A700A3532F10CA5A3028ACC4061217B7CD0EF070FB9B0354BD8D0F95719E5C1F27BB227CA62EBE1B6C04BFB31A5592701F263023A14A2D27231D44B60A5D0B5394154C786C87D0911C566640C1F67A223A302BF19E47137CEBF2DDFC3594E1AE0962A135BBEF9355AADB28AD494AEA033CB9C877254676844D0F246AAE95F7D95932929A01675E8AEC76097E46702514A60FF6A3A7C72C848D002CD1EEEA424FACE609126134C2E24A8D93B2777F3DE24CE8C557ABA1B293DEC5119A62DAE6249EAD0EB88A9B4E1D8729968A8098AF61A2BD67446F692F665F891F1909B1CA66AA6872524E7F7C6EB7EF79D87D6FD6665149F5FC94362533D9AB7CB51BCB6B4599BA217F82220C5217F6AC6D138A3B8E5D8DAC4DE6618B435F006E8426065593347411CD6BE9CECA39A23D693C7A072F75937F5BE454119D780E9623E1424F14631EF693ECAD21438473E5813DF6D7B5984693441AD8C15EA4543BC958AEE8F8C0AF587D18893AA3584268091B606A04B0B099B5AD3D97D5347BFB09F64B3CA7F3836D11EDF2C56C436877658A54C86ABEDE5BB86E0C3324E52CCEDD1DD1F41D347ECEE8E8566FCF481EA4FD5841F3102AB25228AD93A7A89C9728E18A1315EA46CCD94BA84A62EDFB65A992DDC8BC87983AE9F11AA606DCE191E209073B94B40760820C024EBD717D2D4D66F459EF7A7C577371CD88864B8E94E48D4883D623EB1EB1CDB8109157C6B2444F79869F2FE56C0A4CCA491DBE6A69F";
//    private static final String token = "1E1380731503B92242AB2A5D8A41F8AE761A9ECD8BC7BBC9C229C5B49F8A032C59467A21872756BF85B7CB781DDB1DE6C1D1D77C9D5E7EBCA07CD01E2525CE4B186128463FAC8C99956BF102DC28A56647F15248624DF792B7D437699024102D24B48B5FAFDE18E690F29224A6C233493D60696A1CF018F002C9CC4A057548665829B552B2F42F2CAACC5F47F5EC6A01B4F525AC2E0E07AEF2D18B871E1145066D5AF20F32D69B0CEDE9D0B0CEC313E34DF0B55F2A7D66A7CEBF481F877BDF04C9E7CEF1BC1F230342EF07C952D8C9A584B2D4D572D470075F2F2306D3F07785340BB49D6FED77E7A1F0CC30F79F21C774B0C298637E23C2A9FD4ECAB9E74A53B074A25B6566D3995DF2D25998A818B439273761E9A911EF73A118C3FE22CCBA4E5FA461501EE9E0E5A6E99942758BC9D9A9D12672A70E388437F2374C48314D13E9205973B7A452EA9FE97CFFCF3A966F02931EFBB5CE3AB70D5AEF2E9670A8B1C3CFF91889D4CAA4A176495954F0F95D4B71562527AB9E596D1F8A7147E221FD6E6C434740CED20D9422AC6ED96A48";

//    public static String getAppBase64() {
//        try {
//            return SimpleCrypto.decrypt(MoneyManagerApplication.KEY, getTokenApp());
//        } catch (Exception e) {
//            Log.e(LOGCAT, e.getMessage());
//        }
//        return null;
//    }

//    private static String getTokenApp() {
//        try {
//            return SimpleCrypto.decrypt(MoneyManagerApplication.DROPBOX_APP_KEY, base64);
//        } catch (Exception e) {
//            Log.e(LOGCAT, e.getMessage());
//        }
//        return null;
//    }

    /**
     * Shown alert dialog
     *
     * @param resId id of string
     * @return alert dialog
     */
    public static void alertDialog(Context ctx, int resId) {
        alertDialog(ctx, ctx.getString(resId));
    }

    /**
     * Shown alert dialog
     *
     * @param text to display
     * @return alert dialog
     */
    public static void alertDialog(Context context, String text) {
//        Core core = new Core(context);
//        int icon = core.usingDarkTheme()
//            ? R.drawable.ic_action_warning_dark
//            : R.drawable.ic_action_warning_light;

        new AlertDialogWrapper.Builder(context)
            // setting alert dialog
            .setIcon(FontIconDrawable.inflate(context, R.xml.ic_alert))
//            .setIcon(icon)
            .setTitle(R.string.attention)
            .setMessage(text)
//            .setNeutralButton()
            .setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    }
            })
            .show();
    }

    /**
     * Method, which allows you to change the language of the application
     *
     * @param context        Context
     * @param languageToLoad language to load for the locale
     * @return and indicator whether the operation was successful
     */
    public static boolean changeAppLocale(Context context, String languageToLoad) {
        Locale locale;
        try {
            if (!TextUtils.isEmpty(languageToLoad)) {
                locale = new Locale(languageToLoad);
                // Below method is not available in emulator 4.1.1 (?!).
//                locale = Locale.forLanguageTag(languageToLoad);
            } else {
                locale = Locale.getDefault();
            }
            // http://developer.android.com/reference/java/util/Locale.html#setDefault%28java.util.Locale%29
//            Locale.setDefault(locale);

            // change locale to configuration
            Resources resources = context.getResources();
//            Configuration config = new Configuration();
            Configuration config = new Configuration(resources.getConfiguration());
            config.locale = locale;
            // set new locale
            resources.updateConfiguration(config, resources.getDisplayMetrics());
//            getBaseContext().getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(context, null);
            handler.handle(e, "changing app locale");

            return false;
        }
        return true;
    }

    // Instance

    private Context mContext;

    public Core(Context context) {
        super();
        // todo: getApplicationContext?
        this.mContext = context;
        // .getApplicationContext() == null ? context.getApplicationContext() : context;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Method that allows you to make a copy of file
     *
     * @param src Source file
     * @param dst Destination file
     * @throws IOException
     */
    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * Backup current database
     *
     * @return new File database backup
     */
    public File backupDatabase() {
        File database = new File(MoneyManagerApplication.getDatabasePath(mContext));
        if (!database.exists()) return null;
        //create folder to copy database
        File folderOutput = getExternalStorageDirectoryApplication();
        //take a folder of database
        ArrayList<File> filesFromCopy = new ArrayList<>();
        //add current database
        filesFromCopy.add(database);
        //get file journal
        File folder = database.getParentFile();
        if (folder != null) {
            for (File file : folder.listFiles()) {
                if (file.getName().startsWith(database.getName()) && !database.getName().equals(file.getName())) {
                    filesFromCopy.add(file);
                }
            }
        }
        //copy all files
        for (int i = 0; i < filesFromCopy.size(); i++) {
            try {
                copy(filesFromCopy.get(i), new File(folderOutput + "/" + filesFromCopy.get(i).getName()));
            } catch (Exception e) {
                Log.e(LOGCAT, e.getMessage());
                return null;
            }
        }

        return new File(folderOutput + "/" + filesFromCopy.get(0).getName());
    }

    public String getAppVersionBuild() {
        return Integer.toString(getAppVersionCode());
    }

    /**
     * Get a versioncode of the application.
     *
     * @return application version name
     */
    public int getAppVersionCode() {
        try {
            PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(
                getContext().getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            ExceptionHandler handler = new ExceptionHandler(getContext().getApplicationContext(), this);
            handler.handle(e, "getting app version build number");
            return 0;
        }
    }

    public String getAppVersionName() {
        try {
            return getContext().getPackageManager().getPackageInfo(
                getContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
            handler.handle(e, "getting app version name");
        }
        return "n/a";
    }

    public String getFullAppVersion() {
        return getAppVersionName() + "." + getAppVersionBuild();
    }

    /**
     * Return application theme choice from user
     *
     * @return application theme id
     */
    public int getThemeApplication() {
        try {
            String darkTheme = Constants.THEME_DARK;
            String lightTheme = Constants.THEME_LIGHT;

            String currentTheme = PreferenceManager.getDefaultSharedPreferences(mContext)
                    .getString(mContext.getString(PreferenceConstants.PREF_THEME), lightTheme);

            if (currentTheme.endsWith(darkTheme)) {
                // Dark theme
                return R.style.Theme_Money_Manager_Dark;
            } else {
                // Light theme
                return R.style.Theme_Money_Manager_Light;
            }
        } catch (Exception e) {
            Log.e("", e.getMessage());
            return R.style.Theme_Money_Manager_Light;
        }
    }

    /**
     * Method, which returns the last payee used
     *
     * @return last payee used
     */
    public TablePayee getLastPayeeUsed() {
        MmexOpenHelper helper = MmexOpenHelper.getInstance(mContext);
        SQLiteDatabase database = helper.getReadableDatabase();
        TablePayee payee = null;

        String sql =
        "SELECT C.TransID, C.TransDate, C.PAYEEID, P.PAYEENAME, P.CATEGID, P.SUBCATEGID " +
        "FROM CHECKINGACCOUNT_V1 C " +
        "INNER JOIN PAYEE_V1 P ON C.PAYEEID = P.PAYEEID " +
        "WHERE C.TransCode <> 'Transfer' " +
        "ORDER BY C.TransDate DESC, C.TransId DESC " +
        "LIMIT 1";

        Cursor cursor = database.rawQuery(sql, null);

        // check if cursor can be open
        if (cursor != null && cursor.moveToFirst()) {
            payee = new TablePayee();
            payee.setPayeeId(cursor.getInt(cursor.getColumnIndex(Payee.PAYEEID)));
            payee.setPayeeName(cursor.getString(cursor.getColumnIndex(Payee.PAYEENAME)));
            payee.setCategId(cursor.getInt(cursor.getColumnIndex(Payee.CATEGID)));
            payee.setSubCategId(cursor.getInt(cursor.getColumnIndex(Payee.SUBCATEGID)));

            cursor.close();
        }
        //close database
        //helper.close();

        return payee;
    }

    /**
     * Get application directory on external storage. The directory is created if it does not exist.
     *
     * @return the default directory where to store the database
     */
    public File getExternalStorageDirectoryApplication() {
        //get external storage
        File externalStorage;
        externalStorage = Environment.getExternalStorageDirectory();

        if (externalStorage != null && externalStorage.exists() && externalStorage.isDirectory() && externalStorage.canWrite()) {
            //create folder to copy database
            File folderOutput = new File(externalStorage + File.separator + getContext().getPackageName());
            if (!folderOutput.exists()) {
                folderOutput = new File(externalStorage + "/MoneyManagerEx");
                if (!folderOutput.exists()) {
                    //make a directory
                    if (!folderOutput.mkdirs()) {
                        return mContext.getFilesDir();
                    }
                }
            }
            return folderOutput;
        } else {
            return getContext().getFilesDir();
        }
    }

    /**
     * Get dropbox application directory on external storage.
     *
     * @return directory created if not exists
     */
    public File getExternalStorageDirectoryDropboxApplication() {
        File folder = getExternalStorageDirectoryApplication();
        // manage folder
        if (folder != null && folder.exists() && folder.isDirectory() && folder.canWrite()) {
            // create a folder for dropbox
            File folderDropbox = new File(folder + "/dropbox");
            // check if folder exists otherwise create
            if (!folderDropbox.exists()) {
                if (!folderDropbox.mkdirs()) return mContext.getFilesDir();
            }
            return folderDropbox;
        } else {
            return mContext.getFilesDir();
        }
    }

    /**
     * Returns category and sub-category formatted
     *
     * @param categoryId
     * @param subCategoryId
     * @return category : sub-category
     */
    public String getCategSubName(int categoryId, int subCategoryId) {
        // validation
        if (categoryId == Constants.NOT_SET && subCategoryId == Constants.NOT_SET) return null;

        String categoryName = null;
        String subCategoryName, ret;
        TableCategory category = new TableCategory();
        TableSubCategory subCategory = new TableSubCategory();
        // category
        Cursor cursor = mContext.getContentResolver().query(category.getUri(),
                null,
                Category.CATEGID + "=?",
                new String[]{Integer.toString(categoryId)},
                null);

        if ((cursor != null) && (cursor.moveToFirst())) {
            // set category name and sub category name
            categoryName = cursor.getString(cursor.getColumnIndex(Category.CATEGNAME));
        }
        if (cursor != null) {
            cursor.close();
        }

        // sub-category
        cursor = mContext.getContentResolver().query(subCategory.getUri(),
                null,
                Subcategory.SUBCATEGID + "=?",
                new String[]{Integer.toString(subCategoryId)},
                null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            // set category name and sub category name
            subCategoryName = cursor.getString(cursor.getColumnIndex(Subcategory.SUBCATEGNAME));
        } else {
            subCategoryName = null;
        }
        if (cursor != null) {
            cursor.close();
        }

        ret = (!TextUtils.isEmpty(categoryName) ? categoryName : "") +
                (!TextUtils.isEmpty(subCategoryName) ? ":" + subCategoryName : "");

        return ret;
    }

    /**
     * Return arrays of month formatted and localizated
     *
     * @return arrays of months
     */
    public String[] getListMonths() {
        return new DateFormatSymbols().getMonths();
    }

    /**
     * This method allows to highlight in bold the content of a search string
     *
     * @param search       string
     * @param originalText string where to find
     * @return CharSequence modified
     */
    public CharSequence highlight(String search, String originalText) {
        if (TextUtils.isEmpty(search))
            return originalText;
        // ignore case and accents
        // the same thing should have been done for the search text
        String normalizedText = Normalizer.normalize(originalText, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();

        int start = normalizedText.indexOf(search.toLowerCase());
        if (start < 0) {
            // not found, nothing to to
            return originalText;
        } else {
            // highlight each appearance in the original text
            // while searching in normalized text
            Spannable highlighted = new SpannableString(originalText);
            while (start >= 0) {
                int spanStart = Math.min(start, originalText.length());
                int spanEnd = Math.min(start + search.length(), originalText.length());

                highlighted.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), spanStart,
                        spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                start = normalizedText.indexOf(search, spanEnd);
            }
            return highlighted;
        }
    }

    /**
     * Function that determines if the application is running on tablet
     *
     * @return true if running on the tablet, otherwise false
     */
    public boolean isTablet() {
        int layout = getContext().getResources().getConfiguration().screenLayout;
        return ((layout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) ||
                ((layout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
    }

    /**
     * Change the database used by the app.
     *
     * @param path new database
     * @return indicator whether the operation was successful
     */
    public boolean changeDatabase(String path, String password) {
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "switching database to: " + path);

        File file = new File(path);
        // check if database exists
        if (!file.exists()) {
            Toast.makeText(mContext, R.string.path_database_not_exists, Toast.LENGTH_LONG).show();
            return false;
        }
        // check if database can be open in write mode
        if (!file.canWrite()) {
            Toast.makeText(mContext, R.string.database_can_not_open_write, Toast.LENGTH_LONG).show();
            return false;
        }

        // close existing connection.
        MmexOpenHelper.closeDatabase();

        // change database
        new AppSettings(mContext).getDatabaseSettings().setDatabasePath(path);

        MmexOpenHelper.getInstance(getContext()).setPassword(password);

        return true;
    }

    /**
     * Resolves the id attribute in color
     *
     * @param attr id attribute
     * @return color
     */
    public int resolveColorAttribute(int attr) {
//        Resources.Theme currentTheme = mContext.getTheme();
//        return mContext.getResources().getColor(resolveIdAttribute(attr), currentTheme);
        //return mContext.getResources().getColor(resolveIdAttribute(attr));
        return ContextCompat.getColor(mContext, UIHelper.resolveIdAttribute(mContext, attr));
    }

    public boolean isToDisplayChangelog() {
        int currentVersionCode = getAppVersionCode();
        int lastVersionCode = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getInt(mContext.getString(PreferenceConstants.PREF_LAST_VERSION_KEY), -1);

        return lastVersionCode != currentVersionCode;
    }

    public boolean showChangelog() {
        int currentVersionCode = getAppVersionCode();
        PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                .putInt(mContext.getString(PreferenceConstants.PREF_LAST_VERSION_KEY), currentVersionCode)
                .commit();

        // create layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.changelog_layout, null);
        //create dialog
        AlertDialogWrapper.Builder showDialog = new AlertDialogWrapper.Builder(mContext)
            .setCancelable(false)
            .setTitle(R.string.changelog)
            .setView(view);
        showDialog.setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        // show dialog
        showDialog.create().show();
        return true;
    }

    public String getDefaultSystemDateFormat() {
        Locale loc = Locale.getDefault();
        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, loc);
        String pattern = sdf.toLocalizedPattern();
        // replace date
        if (pattern.contains("dd")) {
            pattern = pattern.replace("dd", "%d");
        } else {
            pattern = pattern.replace("d", "%d");
        }
        // replace month
        if (pattern.contains("MM")) {
            pattern = pattern.replace("MM", "%m");
        } else {
            pattern = pattern.replace("M", "%m");
        }
        // replace year
        pattern = pattern.replace("yyyy", "%Y");
        pattern = pattern.replace("yy", "%y");
        // check if exists in format definition
        boolean find = false;
        String[] availableDateFormats = mContext.getResources().getStringArray(R.array.date_format_mask);
        for (int i = 0; i < availableDateFormats.length; i++) {
            if (pattern.equals(availableDateFormats[i])) {
                find = true;
                break;
            }
        }

        String result = find
            ? pattern
            : null;
        return result;
    }

    public int getColourAttribute(int attribute) {
        TypedArray ta = getAttributeValue(attribute);
        int result = ta.getColor(0, Color.TRANSPARENT);

        ta.recycle();

        return result;
    }

    public boolean usingDarkTheme(){
        int currentTheme = this.getThemeApplication();
        return currentTheme == R.style.Theme_Money_Manager_Dark;
    }

    // private

    private TypedArray getAttributeValue(int attribute) {
//        TypedValue typedValue = new TypedValue();
//        context.getTheme().resolveAttribute(attribute, typedValue, true);
//        return typedValue;

//        int[] arrayAttributes = new int[] { attribute };
//        TypedArray typedArray = context.obtainStyledAttributes(arrayAttributes);
//        int value = typedArray.getColor(0, context.getResources().getColor(R.color.abBackground));
//        typedArray.recycle();

        // Create an array of the attributes we want to resolve
        // using values from a theme
        int[] attrs = new int[] { attribute /* index 0 */};
        // Obtain the styled attributes. 'themedContext' is a context with a
        // theme, typically the current Activity (i.e. 'this')
        TypedArray ta = mContext.obtainStyledAttributes(attrs);
        // To get the value of the 'listItemBackground' attribute that was
        // set in the theme used in 'themedContext'. The parameter is the index
        // of the attribute in the 'attrs' array. The returned Drawable
        // is what you are after
//        Drawable drawableFromTheme = ta.getDrawable(0 /* index */);

        // Finally, free the resources used by TypedArray
//        ta.recycle();

        return ta;
    }
}
