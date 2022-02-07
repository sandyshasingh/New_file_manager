package com.simplemobiletools.commons.helpers

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import java.util.*


const val APP_ID = "app_id"
const val REAL_FILE_PATH = "real_file_path_2"
const val IS_FROM_GALLERY = "is_from_gallery"
const val NOMEDIA = ".nomedia"
const val ALARM_SOUND_TYPE_NOTIFICATION = 2
const val INVALID_NAVIGATION_BAR_COLOR = -1
const val SD_OTG_PATTERN = "^/storage/[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$"
const val SD_OTG_SHORT = "^[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$"
const val MD5 = "MD5"

const val HOUR_MINUTES = 60
const val DAY_MINUTES = 24 * HOUR_MINUTES
const val WEEK_MINUTES = DAY_MINUTES * 7
const val MONTH_MINUTES = DAY_MINUTES * 30
const val YEAR_MINUTES = DAY_MINUTES * 365

const val MINUTE_SECONDS = 60
const val HOUR_SECONDS = HOUR_MINUTES * 60
const val DAY_SECONDS = DAY_MINUTES * 60
const val WEEK_SECONDS = WEEK_MINUTES * 60
const val MONTH_SECONDS = MONTH_MINUTES * 60
const val YEAR_SECONDS = YEAR_MINUTES * 60

// shared preferences
const val PREFS_KEY = "Prefs"

const val TREE_URI = "tree_uri_2"
const val OTG_TREE_URI = "otg_tree_uri_2"
const val SD_CARD_PATH = "sd_card_path_2"
const val OTG_REAL_PATH = "otg_real_path_2"
const val INTERNAL_STORAGE_PATH = "internal_storage_path"
const val TEXT_COLOR = "text_color"
const val PRIMARY_COLOR = "primary_color_2"
const val LAST_ICON_COLOR = "last_icon_color"
const val KEEP_LAST_MODIFIED = "keep_last_modified"
const val USE_ENGLISH = "use_english"
const val WAS_USE_ENGLISH_TOGGLED = "was_use_english_toggled"
const val LAST_CONFLICT_RESOLUTION = "last_conflict_resolution"
const val LAST_CONFLICT_APPLY_TO_ALL = "last_conflict_apply_to_all"
const val ENABLE_PULL_TO_REFRESH = "enable_pull_to_refresh"
const val USE_24_HOUR_FORMAT = "use_24_hour_format"
const val SUNDAY_FIRST = "sunday_first"
const val OTG_PARTITION = "otg_partition_2"
const val WAS_APP_ON_SD_SHOWN = "was_app_on_sd_shown"
const val APP_SIDELOADING_STATUS = "app_sideloading_status"
const val DATE_FORMAT = "date_format"
const val WAS_OTG_HANDLED = "was_otg_handled_2"
const val LAST_RENAME_USED = "last_rename_used"
const val LAST_RENAME_PATTERN_USED = "last_rename_pattern_used"
const val LAST_EXPORTED_SETTINGS_FOLDER = "last_exported_settings_folder"
const val LAST_EXPORTED_SETTINGS_FILE = "last_exported_settings_file"
const val FONT_SIZE = "font_size"
const val START_NAME_WITH_SURNAME = "start_name_with_surname"
const val FAVORITES = "favorites"


// global intents
const val OPEN_DOCUMENT_TREE = 1000
const val OPEN_DOCUMENT_TREE_OTG = 1001
const val REQUEST_SET_AS = 1002
const val REQUEST_EDIT_IMAGE = 1003
const val SELECT_EXPORT_SETTINGS_FILE_INTENT = 1004
const val REQUEST_CODE_SET_DEFAULT_DIALER = 1005

// sorting
const val SORT_ORDER = "sort_order"
const val SORT_FOLDER_PREFIX = "sort_folder_"       // storing folder specific values at using "Use for this folder only"
const val SORT_BY_NAME = 1
const val SORT_BY_DATE_MODIFIED = 2
const val SORT_BY_SIZE = 4
const val SORT_BY_DATE_TAKEN = 8
const val SORT_BY_EXTENSION = 16
const val SORT_DESCENDING = 1024
const val SORT_USE_NUMERIC_VALUE = 32768

// renaming
const val RENAME_SIMPLE = 0
const val RENAME_PATTERN = 1


// permissions
const val PERMISSION_READ_STORAGE = 1
const val PERMISSION_WRITE_STORAGE = 2
const val PERMISSION_CAMERA = 3
const val PERMISSION_RECORD_AUDIO = 4
const val PERMISSION_READ_CONTACTS = 5
const val PERMISSION_WRITE_CONTACTS = 6
const val PERMISSION_READ_CALENDAR = 7
const val PERMISSION_WRITE_CALENDAR = 8
const val PERMISSION_CALL_PHONE = 9
const val PERMISSION_READ_CALL_LOG = 10
const val PERMISSION_WRITE_CALL_LOG = 11
const val PERMISSION_GET_ACCOUNTS = 12
const val PERMISSION_READ_SMS = 13
const val PERMISSION_SEND_SMS = 14
const val PERMISSION_READ_PHONE_STATE = 15

// conflict resolving
const val CONFLICT_SKIP = 1
const val CONFLICT_OVERWRITE = 2
const val CONFLICT_MERGE = 3
const val CONFLICT_KEEP_BOTH = 4

val photoExtensions: Array<String> get() = arrayOf(".jpg", ".png", ".jpeg", ".bmp", ".webp", ".heic", ".heif")
val videoExtensions: Array<String> get() = arrayOf(".mp4", ".mkv", ".webm", ".avi", ".3gp", ".mov", ".m4v", ".3gpp")
val audioExtensions: Array<String> get() = arrayOf(".mp3", ".wav", ".wma", ".ogg", ".m4a", ".opus", ".flac", ".aac")
val rawExtensions: Array<String> get() = arrayOf(".dng", ".orf", ".nef", ".arw", ".rw2", ".cr2", ".cr3")

const val DATE_FORMAT_ONE = "dd.MM.yyyy"
const val DATE_FORMAT_TWO = "dd/MM/yyyy"
const val DATE_FORMAT_THREE = "MM/dd/yyyy"
const val DATE_FORMAT_FOUR = "yyyy-MM-dd"
const val DATE_FORMAT_FIVE = "d MMMM yyyy"
const val DATE_FORMAT_SIX = "MMMM d yyyy"
const val DATE_FORMAT_SEVEN = "MM-dd-yyyy"
const val DATE_FORMAT_EIGHT = "dd-MM-yyyy"

const val TIME_FORMAT_12 = "hh:mm a"
const val TIME_FORMAT_24 = "HH:mm"



// view types
const val VIEW_TYPE_GRID = 1
const val VIEW_TYPE_LIST = 2

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread {
            callback()
        }.start()
    } else {
        callback()
    }
}

fun isMarshmallowPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
fun isNougatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
fun isNougatMR1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
fun isOreoMr1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
fun isPiePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
fun isRPlus() = Build.VERSION.SDK_INT >= 30

val normalizeRegex = "\\p{InCombiningDiacriticalMarks}+".toRegex()

fun getConflictResolution(resolutions: LinkedHashMap<String, Int>, path: String): Int {
    return if (resolutions.size == 1 && resolutions.containsKey("")) {
        resolutions[""]!!
    } else if (resolutions.containsKey(path)) {
        resolutions[path]!!
    } else {
        CONFLICT_SKIP
    }
}


fun getFilePlaceholderDrawables(context: Context): HashMap<String, Drawable> {
    val fileDrawables = HashMap<String, Drawable>()
    hashMapOf<String, Int>().apply {
        put("aep", R.drawable.ic_file_aep)
        put("ai", R.drawable.photo_placeholder)
        put("bmp", R.drawable.photo_placeholder)
        put("webp", R.drawable.photo_placeholder)
        put("avi", R.drawable.video_placeholder1)
        put("css", R.drawable.ic_file_css)
        put("csv", R.drawable.ic_file_csv)
        put("dbf", R.drawable.ic_file_dbf)
        put("doc", R.drawable.ic_file_doc)
        put("docx", R.drawable.ic_file_doc)
        put("dwg", R.drawable.ic_file_dwg)
        put("exe", R.drawable.ic_file_exe)
        put("fla", R.drawable.video_placeholder1)
        put("flv", R.drawable.video_placeholder1)
        put("htm", R.drawable.ic_file_html)
        put("html", R.drawable.ic_file_html)
        put("ics", R.drawable.ic_file_ics)
        put("indd", R.drawable.ic_file_indd)
        put("iso", R.drawable.ic_file_iso)
        put("jpg", R.drawable.photo_placeholder)
        put("jpeg", R.drawable.photo_placeholder)
        put("js", R.drawable.ic_file_js)
        put("json", R.drawable.ic_file_json)
        put("m4a", R.drawable.music_placeholder)
        put("mp3", R.drawable.music_placeholder)
        put("aac", R.drawable.music_placeholder)
        put("wma", R.drawable.music_placeholder)
        put("mp4", R.drawable.video_placeholder1)
        put("ogg", R.drawable.music_placeholder)
        put("pdf", R.drawable.ic_file_pdf)
        put("plproj", R.drawable.ic_file_plproj)
        put("prproj", R.drawable.ic_file_prproj)
        put("psd", R.drawable.ic_file_psd)
        put("rtf", R.drawable.ic_file_rtf)
        put("sesx", R.drawable.ic_file_sesx)
        put("sql", R.drawable.ic_file_sql)
        put("svg", R.drawable.ic_file_svg)
        put("txt", R.drawable.ic_file_txt)
        put("vcf", R.drawable.ic_file_txt)
        put("wav", R.drawable.music_placeholder)
        put("flac", R.drawable.music_placeholder)
        put("opus", R.drawable.music_placeholder)
        put("wmv", R.drawable.video_placeholder1)
        put("xls", R.drawable.ic_file_xls)
        put("xml", R.drawable.ic_file_xml)
        put("zip", R.drawable.ic_file_zip)
        put("webm", R.drawable.video_placeholder1)
        put("3gp", R.drawable.video_placeholder1)
        put("3gpp", R.drawable.video_placeholder1)
        put("mov", R.drawable.video_placeholder1)
        put("m4v", R.drawable.video_placeholder1)


    }.forEach { (key, value) ->
        fileDrawables[key] = context.resources.getDrawable(value)
    }
    return fileDrawables
}

const val TYPE_HEADER = 0
const val TYPE_ITEM = 1
const val LIST_VIEW = 10
const val GRID_DIR = 11
const val GRID_FILE = 12

// For Folder Header Id
const val SHORTCUT_ID = 0
const val PHOTOS_ID = 1

const val VIDEOS_ID = 2
const val AUDIO_ID = 3
const val SHORTCUT_FOLDER_ID = 44
const val APPLICATIONS_ID = 5
const val ZIP_FILES_ID = 6
const val DOCUMENTS_ID = 7
const val DOWNLOAD_ID = 8
const val INTERNAL_STORAGE = 9
const val EXTERNAL_STORAGE = 10


 var DELETE_SHORTCUT = false




// For Folder Header Name
const val PHOTOS_NAME = "Photos"
const val SHORTCUT_NAME = "Shortcut"
const val VIDEOS_NAME = "Videos"
const val AUDIO_NAME = "Audio"
const val SHORTCUT_FOLDER_NAME = "Filter Duplicate"
const val APPLICATION_NAME = "Apps"
const val ZIP_FILES_NAME = "Zip files"
const val DOCUMENTS_NAME = "Documents"
const val DOWNLOAD_NAME = "Download"
const val INTERNAL_STORAGE_NAME = "Internal Storage"
const val SD_CARD_NAME = "Sdcard"


val MORE_APPS_LINK = "https://play.google.com/store/apps/developer?id=ASD+Dev+Video+Player+for+All+Format"
val GMAIL_ID = "feedback.rocksplayer@gmail.com"
val FB_LINK2 = "https://www.facebook.com/rareprobsolution.sul.7"
val INSTA_LINK = "https://www.instagram.com/rareprob_/?utm_medium=copy_link"
val TWITTER_LINK = "https://mobile.twitter.com/rare_prob"
val WEB_SITE_LINK = "https://www.rocksplayer.com/"

// For Folder Click Count
var PHOTOS_CLICK = 0L
var WHATSAPP_CLICK = 0L
var VIDEOS_CLICK = 0L
var AUDIO_CLICK = 0L
var FILTER_DUPLICATE_CLICK = 0L
var APPLICATION_CLICK = 0L
var ZIP_FILES_CLICK = 0L
var DOCUMENTS_CLICK = 0L
var DOWNLOAD_CLICK = 0L


// For mLongPress
var isLongPressClick = false

