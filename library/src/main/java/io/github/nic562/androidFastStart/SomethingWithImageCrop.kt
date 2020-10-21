package io.github.nic562.androidFastStart

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.yalantis.ucrop.UCrop
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.selector
import org.jetbrains.anko.warn
import java.io.*

/**
 * 集成从拍照、相册中获取图像、截图等api
 *
 * 占用 requestCode
 * - 9998
 * - 9997
 * - 9996
 * - 69 # UCrop
 * Created by Nic on 2020/9/24.
 */
interface SomethingWithImageCrop : SomethingWithContext, AnkoLogger {
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 9998
        private const val REQUEST_CODE_CAMERA = 9997
        private const val REQUEST_CODE_GALLERY = 9996

        val PERMISSION = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    class ImageOption {
        var crop: Boolean = false // 是否裁剪
        var compressQuality: Int = 100 // 压缩比例，1~100, 100则不压缩
        var xRatio: Float = -1f // 宽高比
        var yRatio: Float = -1f // 宽高比
        var maxWidth: Int = 1024 // 最大宽度
        var maxHeight: Int = 1024 // 最大高度
    }

    /**
     * 临时文件，实例化时直接等于 null 即可
     */
    var tmpCameraSavingFile: File?

    private interface CopyFileCallback {
        fun call(file: File?)
    }

    private fun initUCropOptions(): UCrop.Options {
        return UCrop.Options().apply {
            setStatusBarColor(getCropUIStatusBarColor())
            setToolbarColor(getCropUIToolbarColor())
            setActiveControlsWidgetColor(getCropUIActiveWidgetColor())
            setToolbarWidgetColor(getCropUIToolbarWidgetColor())
        }
    }

    fun getCropUIStatusBarColor(): Int {
        return ContextCompat.getColor(getOwnerContext(), R.color.colorPrimaryDark)
    }

    fun getCropUIToolbarColor(): Int {
        return ContextCompat.getColor(getOwnerContext(), R.color.colorPrimary)
    }

    fun getCropUIActiveWidgetColor(): Int {
        return ContextCompat.getColor(getOwnerContext(), R.color.colorAccent)
    }

    fun getCropUIToolbarWidgetColor(): Int {
        return ContextCompat.getColor(getOwnerContext(), android.R.color.white)
    }

    fun getTmpDir(): File
    fun getImageOption(): ImageOption
    fun startActivityForResult(intent: Intent, reqCode: Int)

    /**
     * @return 调用完拍照、相册、裁剪等流程后，最终返回临时存放的文件绝对路径。请手动保存到其他目录，否则在调用清空临时目录的方法后，文件会被删除
     * @see clearImageCroppingTmp
     */
    fun onImageReady(tmpFilePath: String)

    fun onImageChoiceActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        val option = getImageOption()
        when (requestCode) {
            REQUEST_CODE_CAMERA -> {
                val tmpF = tmpCameraSavingFile
                if (resultCode != Activity.RESULT_OK || tmpF == null) return true
                if (tmpF.isFile) {
                    if (option.crop) openCrop(option, tmpF.absoluteFile)
                    else finalCallbackImage(option, tmpF.absolutePath)
                } else {
                    onImageSavingFileError("camera saving file failed: ${tmpF.absolutePath}")
                }
                return true
            }
            REQUEST_CODE_GALLERY -> {
                val iData = data?.data
                if (resultCode != Activity.RESULT_OK || iData == null) {
                    return true
                }
                saveUriToFile(iData, generateTmpFile(), object : CopyFileCallback {
                    override fun call(file: File?) {
                        if (file == null || !file.isFile) {
                            onImageSavingFileError("gallery saving file error: ${file?.absolutePath}")
                            return
                        }
                        if (option.crop) openCrop(option, file)
                        else finalCallbackImage(option, file.absolutePath)
                    }
                })
                return true
            }
            UCrop.REQUEST_CROP -> {
                if (resultCode != Activity.RESULT_OK) {
                    val thr = UCrop.getError(data ?: return true)
                    error("uCrop crop error:", thr)
                    onImageSavingFileError("uCrop crop error: ${thr?.message}")
                    return true
                }
                val uri = UCrop.getOutput(data ?: return true)
                finalCallbackImage(option, uri?.path
                        ?: throw RuntimeException("UCrop crop callback wrong image Uri error!"))
                return true
            }
        }
        return false
    }

    fun getFileProviderUri(f: File): Uri {
        return FileProvider.getUriForFile(getOwnerContext(), "${getOwnerContext().packageName}.provider", f)
    }

    fun onImageSavingFileError(msg: String) {
        error("saving image error: $msg")
    }

    fun onPermissionFailed(deniedPermissions: List<String>) {
        error("grant permission failed: ${deniedPermissions.joinToString(";")}")
    }

    fun getImageChoicePermissionsMsg(): String {
        return getOwnerContext().getString(R.string.picture_operation_need_permissions)
    }

    fun getImageChoiceTypeMsg(): String {
        return getOwnerContext().getString(R.string.picture_from)
    }

    fun getTakePictureMsg(): String {
        return getOwnerContext().getString(R.string.take_picture)
    }

    fun getAlbumMsg(): String {
        return getOwnerContext().getString(R.string.album)
    }

    fun getDataErrorMsg(): String {
        return getOwnerContext().getString(R.string.data_error)
    }

    private fun generateTmpFile(): File {
        return File(getTmpDir(), "${System.currentTimeMillis()}.jpg")
    }

    fun getImageChoiceRunnableWithPermissions(): SomethingWithPermissions.RunnableWithPermissions {
        return object : SomethingWithPermissions.RunnableWithPermissions {
            override val authFailedMsg: String = getImageChoicePermissionsMsg()
            override val requestCode: Int = REQUEST_CODE_PERMISSIONS
            override val permissions: Array<String> = PERMISSION

            override fun success() {
                getOwnerContext().selector(
                        getImageChoiceTypeMsg(),
                        listOf(getTakePictureMsg(), getAlbumMsg())
                ) { _, i ->
                    when (i) {
                        0 -> openCamera()
                        1 -> openGallery()
                    }
                }
            }

            override fun failed(deniedPermissions: List<String>) {
                onPermissionFailed(deniedPermissions)
            }
        }
    }

    private fun openCamera() {
        val tFile = generateTmpFile()
        val uri = getFileProviderUri(tFile)
        tmpCameraSavingFile = tFile
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            for (info in getOwnerContext().packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)) {
                getOwnerContext().grantUriPermission(info.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivityForResult(intent, REQUEST_CODE_CAMERA)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    private fun initCrop(opt: ImageOption, file: File, newFile: File? = null): UCrop {
        return UCrop.of(getFileProviderUri(file), Uri.fromFile(newFile ?: generateTmpFile()))
                .withMaxResultSize(opt.maxWidth, opt.maxHeight)
                .withOptions(initUCropOptions()).apply {
                    if (opt.xRatio >= 0 && opt.yRatio >= 0) {
                        withAspectRatio(opt.xRatio, opt.yRatio)
                    }
                }
    }

    private fun openCrop(activity: Activity, opt: ImageOption, file: File, newFile: File? = null) {
        initCrop(opt, file, newFile).start(activity)
    }

    private fun openCrop(fragment: Fragment, opt: ImageOption, file: File, newFile: File? = null) {
        initCrop(opt, file, newFile).start(getOwnerContext(), fragment)
    }

    private fun openCrop(opt: ImageOption, file: File, newFile: File? = null) {
        if (getOwnerContext() is Activity) {
            openCrop(getOwnerContext() as Activity, opt, file, newFile)
        } else {
            openCrop(getCurrentFragment(), opt, file, newFile)
        }
    }

    private fun saveUriToFile(uri: Uri, file: File, callback: CopyFileCallback) {
        val io = getOwnerContext().contentResolver.openInputStream(uri)
                ?: throw IOException("open [$uri] io error")
        copyFile(io, file, callback)
    }

    private fun copyFile(inputStream: InputStream, outFile: File, callback: CopyFileCallback) {
        val handler = Handler {
            when (it.what) {
                1 -> callback.call(outFile)
                2 -> callback.call(null)
            }

            return@Handler true
        }

        Thread {
            try {
                FileOutputStream(outFile).use { oit ->
                    inputStream.use { iit ->
                        copyFile(iit, oit)
                    }
                    handler.sendEmptyMessage(1)
                }
            } catch (e: IOException) {
                error("saving URI to image file error:", e)
                handler.sendEmptyMessage(2)
            }
        }.start()
    }

    private fun copyFile(inputStream: InputStream, outputStream: FileOutputStream) {
        var reader: BufferedInputStream? = null
        var writer: BufferedOutputStream? = null
        try {
            reader = BufferedInputStream(inputStream)
            writer = BufferedOutputStream(outputStream)

            val buffer = ByteArray(1024)
            var len: Int
            while (true) {
                len = reader.read(buffer)
                if (len == -1) {
                    break
                }
                writer.write(buffer, 0, len)
                writer.flush()
            }
        } finally {
            reader?.close()
            writer?.close()
        }
    }

    private fun compressImage(imageFilePath: String, outFile: File, quality: Int, callback: CopyFileCallback) {
        val handler = Handler {
            when (it.what) {
                1 -> callback.call(outFile)
                2 -> callback.call(null)
            }

            return@Handler true
        }
        Thread {
            val op = FileOutputStream(outFile)
            val bitmap = BitmapFactory.decodeFile(imageFilePath)
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, op)
                op.flush()
                op.close()
                handler.sendEmptyMessage(1)
            } catch (e: IOException) {
                error("compress image file error:", e)
                handler.sendEmptyMessage(2)
            } finally {
                bitmap.recycle()
            }
        }.start()
    }

    private fun finalCallbackImage(option: ImageOption, imagePath: String) {
        if (option.compressQuality in 1..99) {
            val newFile = generateTmpFile()
            compressImage(imagePath, newFile, option.compressQuality, object : CopyFileCallback {
                override fun call(file: File?) {
                    if (file == null) {
                        onImageSavingFileError(getDataErrorMsg())
                        return
                    }
                    onImageReady(file.absolutePath)
                }
            })
        } else {
            onImageReady(imagePath)
        }
    }

    /**
     * 若此接口在fragment中实现，请实现该方法
     */
    fun getCurrentFragment(): Fragment {
        throw NotImplementedError()
    }

    /**
     * 请在使用完本段api后，显式调用该方法以情况临时目录
     */
    fun clearImageCroppingTmp() {
        val dir = getTmpDir()
        if (!dir.isDirectory) {
            return
        }
        for (file in dir.listFiles() ?: return) {
            if (!file.delete()) {
                warn("Delete failed when clear Tmp direction: ${file.absolutePath}")
            }
        }
    }
}