package io.github.nic562.androidFastStart

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.yalantis.ucrop.UCrop
import org.jetbrains.anko.*
import pub.devrel.easypermissions.AfterPermissionGranted
import java.io.*

/**
 * 集成从拍照、相册中获取图像、截图等api
 *
 * 占用 requestCode
 * - 9998
 * - 9997
 * - 9996
 * - 69
 *
 * Created by Nic on 2018/10/10.
 */
abstract class ActivityBaseWithImageCrop : ActivityBase() {

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 9998
        private const val REQUEST_CODE_CAMERA = 9997
        private const val REQUEST_CODE_GALLERY = 9996
    }

    private val perms = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private var tmpImageFile: File? = null

    private val uCropOption by lazy {
        val x = UCrop.Options()
        x.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        x.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        x.setActiveWidgetColor(ContextCompat.getColor(this, R.color.colorAccent))
        x.setToolbarWidgetColor(android.R.attr.textColorPrimary)
        x
    }

    private val handlerThread: HandlerThread by lazy {
        HandlerThread("ImageCropThread")
    }

    private val threadHandler: Handler by lazy {
        if (handlerThread.state == Thread.State.NEW) {
            handlerThread.start()
        }
        Handler(handlerThread.looper)
    }

    var msgTakePicture = ""
    var msgAlbum = ""
    var msgCropWhich = ""
    var msgCropNeedPermission = ""
    var msgCropFailed = ""
    var msgDataError = ""

    protected class ImageOption {
        var crop: Boolean = false // 是否裁剪
        var compressQuality: Int = 100 // 压缩比例，1~100, 100则不压缩
        var xRatio: Float = -1f // 宽高比
        var yRatio: Float = -1f // 宽高比
        var maxWidth: Int = 1024 // 最大宽度
        var maxHeight: Int = 1024 // 最大高度
    }

    protected interface CopyFileCallback {
        fun call(file: File?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        msgTakePicture = getString(R.string.take_picture)
        msgAlbum = getString(R.string.album)
        msgCropWhich = getString(R.string.picture_from)
        msgCropNeedPermission = getString(R.string.picture_operation_need_permissions)
        msgCropFailed = getString(R.string.picture_operation_failed)
        msgDataError = getString(R.string.data_error)
    }

    private fun getDefaultTmpDir(): File {
        val dir = File(getExternalStoragePath("cropTmp"))
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun generateFile(dir: File? = null): File {
        return File(dir ?: getDefaultTmpDir(), "${System.currentTimeMillis()}.jpg")
    }

    protected abstract fun getImageOption(): ImageOption

    protected abstract fun onImageReady(tmpFilePath: String)

    @AfterPermissionGranted(REQUEST_CODE_PERMISSIONS)
    protected fun openImageChoice() {
        if (hasPermissions(*perms)) {
            selector(
                    msgCropWhich,
                    listOf(msgTakePicture, msgAlbum)
            ) { _, i ->
                when (i) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
        } else {
            requestPermissions(msgCropNeedPermission, REQUEST_CODE_PERMISSIONS, *perms)
        }
    }

    private fun openCamera() {
        tmpImageFile = generateFile()
        val uri = getUriForFile(tmpImageFile!!)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            for (info in packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)) {
                grantUriPermission(info.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
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

    override fun onPermissionsSettingFinish(requestCode: Int, deniedPermissions: List<String>) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (deniedPermissions.isNotEmpty()) {
                alert(
                        msgCropNeedPermission,
                        msgCropFailed
                ) {
                    yesButton { openImageChoice() }
                    noButton { }
                }.show()
            }
            return
        }
        super.onPermissionsSettingFinish(requestCode, deniedPermissions)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val option = getImageOption()
        when (requestCode) {
            REQUEST_CODE_CAMERA -> {
                if (resultCode != RESULT_OK) return
                if (tmpImageFile == null) {
                    toast(msgDataError)
                    return
                }
                if (tmpImageFile?.isFile ?: return) {
                    if (option.crop) openCrop(option, tmpImageFile!!)
                    else finalCallbackImage(option, tmpImageFile!!.absolutePath)
                } else {
                    toast(msgDataError)
                }
                return
            }
            REQUEST_CODE_GALLERY -> {
                if (resultCode != RESULT_OK) return
                saveUriToFile(data?.data ?: return, generateFile(), object : CopyFileCallback {
                    override fun call(file: File?) {
                        if (file == null || !file.isFile) {
                            toast(msgDataError)
                            return
                        }
                        if (option.crop) openCrop(option, file)
                        else finalCallbackImage(option, file.absolutePath)
                    }
                })
                return
            }
            UCrop.REQUEST_CROP -> {
                if (resultCode != RESULT_OK) {
                    val throwable = UCrop.getError(data ?: return)
                    toast(throwable?.message.toString())
                    error("uCrop error:", throwable)
                    return
                }
                val uri = UCrop.getOutput(data ?: return)
                finalCallbackImage(option, uri!!.path
                        ?: throw RuntimeException("UCrop failed callback data on crop !"))
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun finalCallbackImage(option: ImageOption, imagePath: String) {
        if (option.compressQuality in 1..99) {
            val newFile = generateFile()
            compressImage(imagePath, newFile, option.compressQuality, object : CopyFileCallback {
                override fun call(file: File?) {
                    if (file == null) {
                        toast(msgDataError)
                        return
                    }
                    onImageReady(file.absolutePath)
                }
            })
        } else {
            onImageReady(imagePath)
        }
    }

    private fun openCrop(option: ImageOption, file: File, newFile: File? = null) {
        val oo = UCrop.of(getUriForFile(file), Uri.fromFile(newFile ?: generateFile()))
                .withMaxResultSize(option.maxWidth, option.maxHeight)
                .withOptions(uCropOption)

        if (option.xRatio < 0 || option.yRatio < 0) {

        } else {
            oo.withAspectRatio(option.xRatio, option.yRatio)
        }

        oo.start(this)
    }

    private fun getUriForFile(file: File): Uri {
        val auth = "$packageName.provider"
        return FileProvider.getUriForFile(this, auth, file)
    }

    private fun saveUriToFile(uri: Uri, file: File, callback: CopyFileCallback) {
        copyFile(contentResolver.openInputStream(uri)
                ?: throw IOException("open [$uri] io error"), file, callback)
    }

    private fun copyFile(inputStream: InputStream, outFile: File, callback: CopyFileCallback) {
        val handler = Handler {
            when (it.what) {
                1 -> callback.call(outFile)
                2 -> callback.call(null)
            }

            return@Handler true
        }
        val runnable = Runnable {
            try {
                copyFile(inputStream, FileOutputStream(outFile))
                handler.sendEmptyMessage(1)
            } catch (e: IOException) {
                error("make URI to image file error:", e)
                handler.sendEmptyMessage(2)
            }
        }
        threadHandler.post(runnable)
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
        val runnable = Runnable {
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
        }
        threadHandler.post(runnable)
    }

    private fun clearTmp() {
        val dir = getDefaultTmpDir()
        if (!dir.exists() || !dir.isDirectory) {
            return
        }
        for (file in dir.listFiles() ?: return) {
            if (file.delete())
                debug("Delete success: ${file.absolutePath}")
            else
                warn("Delete failed: ${file.absolutePath}")
        }
    }

    override fun onDestroy() {
        handlerThread.quit()
        clearTmp()
        super.onDestroy()
    }
}