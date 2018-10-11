package io.github.nic562.androidFastStart

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import com.yalantis.ucrop.UCrop
import org.jetbrains.anko.*
import pub.devrel.easypermissions.AfterPermissionGranted
import java.io.*

private const val REQUEST_CODE_PERMISSIONS = 200
private const val REQUEST_CODE_CAMERA = 300
private const val REQUEST_CODE_GALLERY = 301
/**
 * Created by Nic on 2018/10/10.
 */
abstract class ActivityBaseWithImageCrop : ActivityBase() {

    private val perms = arrayOf(
            Manifest.permission.CAMERA,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

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

    var msg_take_picture = "拍照"
    var msg_album = "相册"
    var msg_crop_which = "选择照片来源"
    var msg_crop_need_permission = "图片操作需要以下权限"
    var msg_crop_failed = "图片操作失败"
    var msg_data_error = "数据错误"

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

    override fun getInitPermissions(): Array<String>? {
        return null
    }

    override fun getInitPermissionsDescriptions(): String {
        return ""
    }

    override fun onInitPermissionsFinish(deniedPermissions: List<String>) {
    }

    protected abstract fun getImageOption(): ImageOption

    protected abstract fun onImageReady(tmpFilePath: String)

    @AfterPermissionGranted(REQUEST_CODE_PERMISSIONS)
    protected fun openImageChoice() {
        if (hasPermissions(*perms)) {
            selector(
                    msg_crop_which,
                    listOf(msg_take_picture, msg_album)
            ) { _, i ->
                when (i) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
        } else {
            requestPermissions(msg_crop_need_permission, REQUEST_CODE_PERMISSIONS, *perms)
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
        super.onPermissionsSettingFinish(requestCode, deniedPermissions)
        if (deniedPermissions.isNotEmpty()) {
            alert(
                    msg_crop_need_permission,
                    msg_crop_failed
            ) {
                yesButton { openImageChoice() }
                noButton { }
            }.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val option = getImageOption()
        when (requestCode) {
            REQUEST_CODE_CAMERA -> {
                if (resultCode != RESULT_OK) return
                if (tmpImageFile == null) {
                    toast(msg_data_error)
                    return
                }
                if (tmpImageFile?.isFile ?: return) {
                    if (option.crop) openCrop(option, tmpImageFile!!)
                    else finalCallbackImage(option, tmpImageFile!!.absolutePath)
                } else {
                    toast(msg_data_error)
                }
                return
            }
            REQUEST_CODE_GALLERY -> {
                if (resultCode != RESULT_OK) return
                saveUriToFile(data?.data ?: return, generateFile(), object : CopyFileCallback {
                    override fun call(file: File?) {
                        if (file == null || !file.isFile) {
                            toast(msg_data_error)
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
                finalCallbackImage(option, uri!!.path ?: throw RuntimeException("UCrop failed callback data on crop !"))
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
                        toast(msg_data_error)
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

        if (option.xRatio < 0 || option.yRatio < 0){

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
        copyFile(contentResolver.openInputStream(uri) ?: throw IOException("open [$uri] io error"), file, callback)
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
        for (file in dir.listFiles()) {
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