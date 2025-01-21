package hilox.crop.documentcropping

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val REQUEST_CAMERA = 1
    private val REQUEST_GALLERY = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCamera: Button = findViewById(R.id.btn_camera)
        val btnGallery: Button = findViewById(R.id.btn_gallery)

        btnCamera.setOnClickListener { openCamera() }
        btnGallery.setOnClickListener { openGallery() }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CAMERA)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    processImage(imageBitmap)
                }
                REQUEST_GALLERY -> {
                    val imageUri: Uri = data?.data ?: return
                    val inputStream = contentResolver.openInputStream(imageUri)
                    val selectedBitmap = BitmapFactory.decodeStream(inputStream)
                    processImage(selectedBitmap)
                }
            }
        }
    }

    private fun processImage(bitmap: Bitmap) {

    }
}
