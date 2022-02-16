package com.editor.hiderx

import android.app.Application
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.editor.hiderx.StorageUtils.getAudiosHiderDirectory
import com.editor.hiderx.StorageUtils.getDocumentsHiderDirectory
import com.editor.hiderx.StorageUtils.getHiderDirectory
import com.editor.hiderx.StorageUtils.getOthersHiderDirectory
import com.editor.hiderx.StorageUtils.getPhotosHiderDirectory
import com.editor.hiderx.StorageUtils.getVideosHiderDirectory
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.database.HiddenFilesDatabase
import com.editor.hiderx.dataclass.FileDataClass
import com.editor.hiderx.dataclass.SimpleDataClass
import com.editor.hiderx.dataclass.TreeNode
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.*
import java.io.File

class DataViewModel(var app: Application) : AndroidViewModel(app), CoroutineScope by MainScope() {

    var hiddenPhotos: MutableLiveData<List<HiddenFiles>>? = MutableLiveData()
    var hiddenVideos: MutableLiveData<List<HiddenFiles>>? = MutableLiveData()
    var hiddenAudios: MutableLiveData<List<HiddenFiles>>? = MutableLiveData()
    var allPhotos: MutableLiveData<List<SimpleDataClass>?> = MutableLiveData()
    var allAudios: MutableLiveData<List<HiddenFiles>?> = MutableLiveData()
    var allVideos: MutableLiveData<List<SimpleDataClass>?> = MutableLiveData()
    var imageFolderData: MutableLiveData<HashMap<SimpleDataClass, ArrayList<SimpleDataClass>>>? = MutableLiveData()
    var audioFolderData: MutableLiveData<HashMap<SimpleDataClass, ArrayList<HiddenFiles>>>? = MutableLiveData()
    var videoFolderData: MutableLiveData<HashMap<SimpleDataClass, ArrayList<SimpleDataClass>>>? = MutableLiveData()
    val myPhotosFolders: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val myDocumentsFolders: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val myOthersFolders: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val myAudiosFolders: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val myVideosFolders: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val myCameraFolders: MutableLiveData<HashMap<FileDataClass, ArrayList<SimpleDataClass>>> = MutableLiveData()
    val myHiddenFiles: MutableLiveData<TreeNode<FileDataClass>> = MutableLiveData()
    val myFilesFolders: MutableLiveData<TreeNode<FileDataClass>> = MutableLiveData()
    val myHiderFolders: MutableLiveData<ArrayList<SimpleDataClass>> = MutableLiveData()

    var hashMapForPhotos : HashMap<SimpleDataClass, ArrayList<SimpleDataClass>> = HashMap()
    var hashMapForAudios : HashMap<SimpleDataClass, ArrayList<HiddenFiles>> = HashMap()
    var hashMapForVideos : HashMap<SimpleDataClass, ArrayList<SimpleDataClass>> = HashMap()
    var hashMapForCamera : HashMap<FileDataClass, ArrayList<SimpleDataClass>>? = HashMap()
    var tempListForPhotos: ArrayList<HiddenFiles> = ArrayList()
    var tempListForAudios: ArrayList<HiddenFiles> = ArrayList()
    var tempListForVideos: ArrayList<HiddenFiles> = ArrayList()
    var hiderFolders : ArrayList<SimpleDataClass> = ArrayList()

    fun getHiddenPhotos(context: Context) {
        fetchPhotos(context)
    }

    fun getMyHiderFolders()
    {
        launch {
            val operation = async(Dispatchers.IO)
            {
                val file : File = StorageUtils.getHiderDirectory()
                hiderFolders.clear()
                getChildFolders(file,true)
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                myHiderFolders.value = hiderFolders
            }
        }
    }

    private fun getChildFolders(file: File,isFirstLevel : Boolean) {
        val list = file.listFiles()
        if(list!=null)
        for(i in list)
        {
            if(i.isDirectory)
            {
                if(!isFirstLevel)
                hiderFolders.add(SimpleDataClass(i?.path!!, i.name, false))
                getChildFolders(i,false)
            }
        }
    }

    fun getFilemanagerData()
    {
        launch {
            var data : TreeNode<FileDataClass>? = null
            val operation = async(Dispatchers.IO)
            {
                data = getFilesData(getHiderDirectory())
            }
            operation.await()
            postHiddenFilesData(data)
        }
    }

    private fun postHiddenFilesData(data: TreeNode<FileDataClass>?) {
            myHiddenFiles.value = data
    }

 /*   private fun postFileManagerData(data: TreeNode<FileDataClass>?) {
            myFilesFolders.value = data
    }
*/

    fun getFilesData(file: File) : TreeNode<FileDataClass>
    {
        var itemCount : Int=0
        var mimeType  : String? = ""
        var filename : String? = ""
        var size = 0L
        if(file.isFile)
        {
            filename = StorageUtils.decode(file.name, StorageUtils.offset)
            val ext =   filename?.substringAfterLast(".")
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext?.toLowerCase())!!
        }
        else
        {
            val list = file.listFiles()
            if(list !=null && list.isNotEmpty())
            itemCount = list.size
        }

        val tree = TreeNode(
            FileDataClass(
                    file.path, file.name, "", file.isFile, itemCount, mimeType,
                    isSelected = false,
                    updateTimeStamp = 0
            )
        )

        if(file.isDirectory)
        {
            val childFiles : Array<File>? = file.listFiles()

            if(childFiles!=null && childFiles.isNotEmpty())
            {
                for(i in childFiles)
                {
                    if(i.path!= getHiderDirectory().path+"/$PASSWORD_FILE_NAME")
                    {
                        if(i.isDirectory)
                        {
                            tree.add(getFilesData(i))
                        }
                        else
                        {
                            var name = ""
                            name = StorageUtils.decode(i.name, StorageUtils.offset)!!
                            val ext =   name.substringAfterLast(".")
                            Log.d("data", name)
                            size += i.length()
                            val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext?.toLowerCase())
                            tree.add(
                                TreeNode(
                                    FileDataClass(
                                            i.path, name, StorageUtils.format(
                                                i.length().toDouble(), 1
                                            ), true, 0, type,
                                            isSelected = false,
                                            updateTimeStamp = 0
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
        tree.value.size = StorageUtils.format(size.toDouble(), 1)
        return tree
    }


    fun getHiddenVideos(context : Context) {
        fetchVideos(context)
    }

    fun getHiddenAudios() {
        fetchAudios()
    }

    fun getCameraFolders() {
        launch {
            hashMapForCamera = HashMap()
            val operation = async(Dispatchers.IO)
            {
                getFolderData(File(getPhotosHiderDirectory()), false)
                getFolderData(File(getVideosHiderDirectory()), true)
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                myCameraFolders.value = hashMapForCamera
                hashMapForCamera = null
            }
        }
    }

    private fun getFolderData(file: File, isVideo: Boolean) {
        val rootFiles: ArrayList<SimpleDataClass> = ArrayList()
        val filesList = file.listFiles()
        var noOfFiles = 0
        var size = 0L
        if (filesList != null && filesList.isNotEmpty()) {
            for (i in file.listFiles()!!) {
                if (i.isDirectory) {
                    getFolderData(i, isVideo)
                } else {
                    if(i.length()>0)
                    {
                        rootFiles.add(SimpleDataClass(i.path, i.name, false))
                        noOfFiles++
                        size += i.length()
                    }
                }
            }
            hashMapForCamera?.set(
                FileDataClass(
                        file.path, file.name, StorageUtils.format(
                            size.toDouble(),
                            1
                        ), false, noOfFiles, "", false, 0
                ), rootFiles
            )
        }
    }

    private fun fetchAudios() {

        launch {
            var audiosList: List<HiddenFiles>? = null
            val operation = async(Dispatchers.IO)
            {
                audiosList =
                        HiddenFilesDatabase.getInstance(getApplication()).hiddenFilesDao.getAllFiles(
                            "audio%"
                        )
                audiosList = filterAudios(audiosList)
            }
            operation.await()
            if (audiosList != null && audiosList?.isNotEmpty()!!) {
                hiddenAudios?.value = audiosList
            } else {
                val rootDir = getAudiosHiderDirectory()
                tempListForAudios.clear()
                val operation2 = async(Dispatchers.IO)
                {
                    getHiddenAudiosFromStorage(rootDir)
                }
                operation2.await()
                if (tempListForVideos.isNotEmpty()) {
                    tempListForVideos = filterVideos(tempListForVideos)
                    postHiddenVideos(tempListForVideos)
                    withContext(Dispatchers.IO)
                    {
                        for (i in tempListForVideos)
                            HiddenFilesDatabase.getInstance(getApplication()).hiddenFilesDao.insertFile(
                                i
                            )
                    }
                }
            }
        }
    }

    private fun fetchVideos(context: Context) {

        launch {
                val rootDir = getVideosHiderDirectory()
                tempListForVideos.clear()
                val operation2 = async(Dispatchers.IO)
                {
                    getHiddenVideosFromStorage(rootDir,context)
                    if (tempListForVideos.isNotEmpty()) {
                        tempListForVideos = filterVideos(tempListForVideos)
                        tempListForVideos?.sortedBy { hiddenVideo-> hiddenVideo.updateTime }.reversed()?.let {
                            if(it.isNotEmpty())
                            {
                                tempListForVideos = ArrayList()
                                tempListForVideos.addAll(it)
                            }
                        }
                    }
                }
                operation2.await()
                if (tempListForVideos.isNotEmpty()) {
                    postHiddenVideos(tempListForVideos)
                }
            }
    }


    private fun fetchPhotos(context: Context) {
        launch {
                val rootDir = getPhotosHiderDirectory()
                tempListForPhotos.clear()
                val operation1 = async(Dispatchers.IO)
                {
                    getHiddenPhotosFromStorage(rootDir,context)
                    if (tempListForPhotos.isNotEmpty()) {
                        tempListForPhotos = filterPhotos(tempListForPhotos)
                        tempListForPhotos.sortedBy { hiddenPhoto -> hiddenPhoto.updateTime }
                            .reversed().let {
                            if (it.isNotEmpty())
                            {
                                tempListForPhotos = ArrayList()
                                tempListForPhotos.addAll(it)
                            }
                        }
                    }
                }
                operation1.await()
                if (tempListForPhotos.isNotEmpty()) {
                    postHiddenPhotos(tempListForPhotos)
            }
        }
    }

    private fun filterPhotos(photosList: List<HiddenFiles>?): ArrayList<HiddenFiles> {
        val filteredList: ArrayList<HiddenFiles> = ArrayList()
        if (photosList != null && photosList.isNotEmpty()) {
            for (i in photosList) {
                if (File(i.path).exists())
                    filteredList.add(i)
            }
        }
        return filteredList
    }

    private fun filterVideos(videosList: List<HiddenFiles>?): ArrayList<HiddenFiles> {
        val filteredList: ArrayList<HiddenFiles> = ArrayList()
        if (videosList != null && videosList.isNotEmpty()) {
            for (i in videosList) {
                if (File(i.path).exists())
                    filteredList.add(i)
            }
        }
        return filteredList
    }

    private fun filterAudios(audiosList: List<HiddenFiles>?): ArrayList<HiddenFiles> {
        val filteredList: ArrayList<HiddenFiles> = ArrayList()
        if (audiosList != null && audiosList.isNotEmpty()) {
            for (i in audiosList) {
                if (File(i.path).exists())
                    filteredList.add(i)
            }
        }
        return filteredList
    }

    private fun postHiddenPhotos(tempList: java.util.ArrayList<HiddenFiles>) {
        hiddenPhotos?.value = tempList
    }

    private fun postHiddenAudios(tempList: java.util.ArrayList<HiddenFiles>) {
        hiddenAudios?.value = tempList
    }

    private fun postHiddenVideos(tempList: java.util.ArrayList<HiddenFiles>) {
        hiddenVideos?.value = tempList
    }

    private fun getHiddenPhotosFromStorage(rootDir: String,context: Context) {
        val list = File(rootDir).listFiles()
        if (list != null && list.isNotEmpty()) {
            val databaseDao = HiddenFilesDatabase.getInstance(context).hiddenFilesDao
            for (file in list) {
                if (file?.isDirectory!!) {
                    Log.d("fetch", "directory " + file.name)
                    getHiddenPhotosFromStorage(file.path,context)
                } else {
                    Log.d("fetch", "file " + file.path)
                    val data = HiddenFiles(
                        file.path, StorageUtils.decode(file.name,StorageUtils.offset)!!, "", StorageUtils.format(
                            file.length().toDouble(),
                            2
                        ), "image/*", System.currentTimeMillis(), false,true,0
                    )
                    data.originalPath = databaseDao.getOriginalPathForFile(file.path)?:""
                    data.updateTime = databaseDao.getUpdateTimeForFile(file.path)?:0
                    tempListForPhotos.add(data)
                }
            }
        }

    }

    private fun getHiddenAudiosFromStorage(rootDir: String) {
        val list = File(rootDir).listFiles()
        if (list != null && list.isNotEmpty()) {
            for (file in list) {
                if (file?.isDirectory!!) {
                    getHiddenAudiosFromStorage(file.path)
                } else {
                    tempListForAudios.add(
                        HiddenFiles(
                                file.path, StorageUtils.decode(file.name,StorageUtils.offset)!!, "", StorageUtils.format(
                                    file.length().toDouble(),
                                    2
                                ), "audio/*", file.lastModified(), false,true,0
                        )
                    )
                }
            }
        }

    }

    private suspend fun getHiddenVideosFromStorage(rootDir: String, context: Context) {
        val list = File(rootDir).listFiles()
        if (list != null && list.isNotEmpty()) {
            for (file in list) {
                val dataBaseDao = HiddenFilesDatabase.getInstance(context).hiddenFilesDao
                if (file?.isDirectory!!) {
                    getHiddenVideosFromStorage(file.path, context)
                } else {
                    if(file.length()>0 && file.exists())
                    {
                        val data = HiddenFiles(
                            file.path, StorageUtils.decode(file.name,StorageUtils.offset)!!, "", StorageUtils.format(
                                file.length().toDouble(),
                                2
                            ), "video/*", System.currentTimeMillis(), false,true,0
                        )
                        data.updateTime = dataBaseDao.getUpdateTimeForFile(file.path)?:0
                        tempListForVideos.add(data)
                    }
                }
            }
        }

    }

    fun getAllVideos() {
        launch {
            var videosList: ArrayList<SimpleDataClass>? = null
            val operation = async(Dispatchers.IO)
            {
                videosList = fetchAllVideos(app.applicationContext!!)
            }
            operation.await()
            postAllVideos(videosList)
            withContext(Dispatchers.IO)
            {
                val parentFile = Environment.getExternalStorageDirectory()
                val operation2 = async(Dispatchers.IO)
                {
                    fetchVideoFolders(parentFile)
                }
                operation2.await()
                postVideoFolderData()
            }
        }

    }

    fun getAllPhotos() {
        launch {
            var photosList: ArrayList<SimpleDataClass>? = null
            val operation = async(Dispatchers.IO)
            {
                photosList = fetchAllPhotos(app.applicationContext!!)
            }
            operation.await()
            postAllPhotos(photosList)
            withContext(Dispatchers.IO)
            {
                val parentFile = Environment.getExternalStorageDirectory()
                val operation2 = async(Dispatchers.IO)
                {
                    fetchImageFolders(parentFile)
                }
                operation2.await()
                postImageFolderData()
            }
        }

    }

    fun getAllAudios() {
        launch {
            var audiosList: ArrayList<HiddenFiles>? = null
            val operation = async(Dispatchers.IO)
            {
                audiosList = fetchAllAudios(app.applicationContext!!)
            }
            operation.await()
            postAllAudios(audiosList)
            withContext(Dispatchers.IO)
            {
                val parentFile = Environment.getExternalStorageDirectory()
                val operation2 = async(Dispatchers.IO)
                {
                    fetchAudioFolders(parentFile)
                }
                operation2.await()
                postAudioFolderData()
            }
        }

    }

    private fun postAllAudios(audiosList: ArrayList<HiddenFiles>?) {
        launch {
            withContext(Dispatchers.Main)
            {
                allAudios.value = audiosList
            }
        }
    }


    private fun postAllPhotos(photosList: ArrayList<SimpleDataClass>?) {
        launch {
            withContext(Dispatchers.Main)
            {
                allPhotos.value = photosList
            }
        }
    }

    private fun postAllVideos(videosList: ArrayList<SimpleDataClass>?) {
        launch {
            withContext(Dispatchers.Main)
            {
                allVideos.value = videosList
            }
        }
    }

    private fun postImageFolderData() {
        launch {
            withContext(Dispatchers.Main)
            {
                imageFolderData?.value = hashMapForPhotos
                hashMapForPhotos = HashMap()
            }
        }
    }

    private fun postAudioFolderData() {
        launch {
            withContext(Dispatchers.Main)
            {
                audioFolderData?.value = hashMapForAudios
                hashMapForAudios = HashMap()
            }
        }
    }

    private fun postVideoFolderData() {
        launch {
            withContext(Dispatchers.Main)
            {
                videoFolderData?.value = hashMapForVideos
                hashMapForVideos = HashMap()
            }
        }
    }

    private fun fetchAllVideos(context: Context): ArrayList<SimpleDataClass>? {
        try {
            val videoDataClassList: ArrayList<SimpleDataClass>? = ArrayList()
            val projection = arrayOf(
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DURATION
            )
            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
            val query = context.contentResolver?.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            query.use { cursor ->

                val nameColumn = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dataColumn: Int = cursor?.getColumnIndex(MediaStore.Video.Media.DATA)!!
                val durationColumn : Int = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)

                while (cursor?.moveToNext()) {
                    // Get values of columns for a given video
                    val name = cursor.getString(nameColumn!!)
                    val data: String = cursor.getString(dataColumn)
                    val duration: Long? = cursor.getLong(durationColumn)
                    val file = File(data)
                    if (file.exists() && file.length() > 0) {
                        if(duration == null || (duration>0))
                        {
                            videoDataClassList?.plusAssign(
                                SimpleDataClass(
                                    data, name, isSelected = false
                                )
                            )
                            Log.d("fetch_videos", data)
                        }
                    }
                }
            }
            return videoDataClassList
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().log(e.toString())
            FirebaseCrashlytics.getInstance().recordException(e)
            return null
        }
    }

    private fun fetchAllPhotos(context: Context): ArrayList<SimpleDataClass>? {
        try {
            val imageDataClassList: ArrayList<SimpleDataClass>? = ArrayList()
            val projection = arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
            )
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
            val query = context.contentResolver?.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            query.use { cursor ->
                Log.d("reached", "cursor")

                val nameColumn = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dataColumn: Int = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)!!


                while (cursor?.moveToNext()) {
                    // Get values of columns for a given video
                    val name = cursor.getString(nameColumn!!)
                    Log.d("fetch", name)
                    val data: String = cursor.getString(dataColumn)
                    val file = File(data)
                    if (file.exists() && file.length() > 0) {
                        imageDataClassList?.plusAssign(
                            SimpleDataClass(
                                data, name, isSelected = false
                            )
                        )
                    }
                }
            }
            return imageDataClassList
        } catch (e: Exception) {
            Log.d("fetch", e.toString())
            return null
        }
    }

    private fun fetchAllAudios(context: Context): ArrayList<HiddenFiles>? {
        try {
            val audioDataClassList: ArrayList<HiddenFiles>? = ArrayList()
            val projection = arrayOf(
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.SIZE,
            )

            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
            val query = context.contentResolver?.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            query.use { cursor ->

                val nameColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val dataColumn: Int = cursor?.getColumnIndex(MediaStore.Audio.Media.DATA)!!
                val sizeColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)

                while (cursor.moveToNext()) {
                    // Get values of columns for a given video
                    val name = cursor.getString(nameColumn!!)
                    val data: String = cursor.getString(dataColumn)
                    val size: String = StorageUtils.format(cursor.getDouble(sizeColumn), 2)
                    val file = File(data)
                    if (file.exists() && file.length() > 0) {
                        audioDataClassList?.plusAssign(
                            HiddenFiles(
                                    data, name, "", size, "audio/*", System.currentTimeMillis(), false,true,0
                            )
                        )
                    }
                }
            }
            return audioDataClassList
        } catch (e: Exception) {
            return null
        }
    }

    fun fetchImageFolders(dir: File) {
        val listFile: Array<File>? = dir.listFiles()
        if (listFile != null && listFile.isNotEmpty()) {
            for (file in listFile) {
                if (file.isDirectory) {
                    fetchImageFolders(file)
                } else {
                    if (file.name.endsWith(".png")
                            || file.name.endsWith(".jpg")
                            || file.name.endsWith(".jpeg")
                            || file.name.endsWith(".gif")
                            || file.name.endsWith(".bmp")
                            || file.name.endsWith(".webp")
                    ) {
                        val parentFilePath: String =
                                file.path.substring(0, file.path.lastIndexOf('/'))
                        val temp = parentFilePath?.split("/")
                        val name = temp[temp.size - 1]
                        val folder = SimpleDataClass(parentFilePath, name, false)
                        val files = hashMapForPhotos[folder]
                        if (files == null) {
                            hashMapForPhotos[folder] = ArrayList<SimpleDataClass>().apply {
                                add(
                                    SimpleDataClass(
                                        file.path,
                                        file.name,
                                        isSelected = false
                                    )
                                )
                            }
                        } else {
                            files.add(SimpleDataClass(file.path, file.name, isSelected = false))
                            hashMapForPhotos[folder] = files
                        }
                    }
                }
            }
        }
    }

    fun fetchVideoFolders(dir: File) {
        val listFile: Array<File>? = dir.listFiles()
        if (listFile != null && listFile.isNotEmpty()) {
            for (file in listFile) {
                if (file.isDirectory) {
                    fetchVideoFolders(file)
                } else {
                    if (file.name.endsWith(".mp4")
                            || file.name.endsWith(".mov")
                            || file.name.endsWith(".wmv")
                            || file.name.endsWith(".avi")
                            || file.name.endsWith(".mkv")
                    ) {
                        val parentFilePath: String =
                                file.path.substring(0, file.path.lastIndexOf('/'))
                        val temp = parentFilePath?.split("/")
                        val name = temp[temp.size - 1]
                        val folder = SimpleDataClass(parentFilePath, name, false)
                        val files = hashMapForVideos[folder]
                        if (files == null) {
                            hashMapForVideos[folder] = ArrayList<SimpleDataClass>().apply {
                                add(
                                    SimpleDataClass(
                                        file.path,
                                        file.name,
                                        isSelected = false
                                    )
                                )
                            }
                        } else {
                            files.add(SimpleDataClass(file.path, file.name, isSelected = false))
                            hashMapForVideos[folder] = files
                        }
                    }
                }
            }
        }
    }

    fun fetchAudioFolders(dir: File) {
        val listFile: Array<File>? = dir.listFiles()
        if (listFile != null && listFile.isNotEmpty()) {
            for (file in listFile) {
                if (file.isDirectory) {
                    fetchAudioFolders(file)
                } else {
                    if (file.name.endsWith(".pcm")
                            || file.name.endsWith(".wav")
                            || file.name.endsWith(".aiff")
                            || file.name.endsWith(".mp3")
                            || file.name.endsWith(".aac")
                            || file.name.endsWith(".ogg")
                            || file.name.endsWith(".wma")
                            || file.name.endsWith(".flac")
                            || file.name.endsWith(".alac")
                            || file.name.endsWith(".m4a")
                    ) {
                        val parentFilePath: String =
                                file.path.substring(0, file.path.lastIndexOf('/'))
                        val temp = parentFilePath?.split("/")
                        val name = temp[temp.size - 1]
                        val folder = SimpleDataClass(parentFilePath, name, false)
                        val files = hashMapForAudios[folder]
                        if (files == null) {
                            hashMapForAudios[folder] = ArrayList<HiddenFiles>().apply {
                                add(
                                    HiddenFiles(
                                            file.path,
                                            file.name, "", StorageUtils.format(
                                                file.length().toDouble(), 2
                                            ), "audio/*", System.currentTimeMillis(), false,true,0
                                    )
                                )
                            }
                        } else {
                            files.add(
                                HiddenFiles(
                                        file.path,
                                        file.name,
                                        "",
                                        StorageUtils.format(file.length().toDouble(), 2),
                                        "audio/*",
                                        System.currentTimeMillis(),
                                    false,true,0
                                )
                            )
                            hashMapForAudios[folder] = files
                        }
                    }
                }
            }
        }
    }


    fun getMyPhotosFolders() {
        launch {
            val folders = ArrayList<String>()
            val operation = async(Dispatchers.IO)
            {
                val file: File = File(getPhotosHiderDirectory())
                val filesList = file.listFiles()
                if (filesList != null && filesList.isNotEmpty()) {
                    for (i in file.listFiles()!!) {
                        if (i.isDirectory)
                            folders.add(i.path)
                    }
                }
            }
            operation.await()
            postMyPhotosFolders(folders)
        }

    }

    fun getMyDocumentsFolders() {
        launch {
            val folders = ArrayList<String>()
            val operation = async(Dispatchers.IO)
            {
                val file: File = File(getDocumentsHiderDirectory())
                val filesList = file.listFiles()
                if (filesList != null && filesList.isNotEmpty()) {
                    for (i in file.listFiles()!!) {
                        if (i.isDirectory)
                            folders.add(i.path)
                    }
                }
            }
            operation.await()
            postMyDocumentsFolders(folders)
        }

    }

    fun getMyOthersFolders() {
        launch {
            val folders = ArrayList<String>()
            val operation = async(Dispatchers.IO)
            {
                val file: File = File(getOthersHiderDirectory())
                val filesList = file.listFiles()
                if (filesList != null && filesList.isNotEmpty()) {
                    for (i in file.listFiles()!!) {
                        if (i.isDirectory)
                            folders.add(i.path)
                    }
                }
            }
            operation.await()
            postMyOthersFolders(folders)
        }

    }

    private fun postMyOthersFolders(folders: java.util.ArrayList<String>) {
        launch {
            withContext(Dispatchers.Main)
            {
                myOthersFolders.value = folders
            }
        }
    }

    private fun postMyDocumentsFolders(folders: java.util.ArrayList<String>) {
        launch {
            withContext(Dispatchers.Main)
            {
                myDocumentsFolders.value = folders
            }
        }
    }

    fun getMyVideosFolders() {
        launch {
            val folders = ArrayList<String>()
            val operation = async(Dispatchers.IO)
            {
                val file: File = File(getVideosHiderDirectory())
                val filesList = file.listFiles()
                if (filesList != null && filesList.isNotEmpty()) {
                    for (i in file.listFiles()!!) {
                        if (i.isDirectory)
                            folders.add(i.path)
                    }
                }
            }
            operation.await()
            postMyVideosFolders(folders)
        }

    }

    fun getMyAudiosFolders() {
        launch {
            val folders = ArrayList<String>()
            val operation = async(Dispatchers.IO)
            {
                val file: File = File(getAudiosHiderDirectory())
                val filesList = file.listFiles()
                if (filesList != null && filesList.isNotEmpty()) {
                    for (i in file.listFiles()!!) {
                        if (i.isDirectory)
                            folders.add(i.path)
                    }
                }
            }
            operation.await()
            postMyAudiosFolders(folders)
        }

    }

    private fun postMyPhotosFolders(folders: ArrayList<String>) {
        launch {
            withContext(Dispatchers.Main)
            {
                myPhotosFolders.value = folders
            }
        }
    }

    private fun postMyVideosFolders(folders: ArrayList<String>) {
        launch {
            withContext(Dispatchers.Main)
            {
                myVideosFolders.value = folders
            }
        }
    }

    private fun postMyAudiosFolders(folders: ArrayList<String>) {
        launch {
            withContext(Dispatchers.Main)
            {
                myAudiosFolders.value = folders
            }
        }
    }

}
