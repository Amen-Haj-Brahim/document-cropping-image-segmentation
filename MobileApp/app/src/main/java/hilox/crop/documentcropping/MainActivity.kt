package hilox.crop.documentcropping

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private val REQUEST_CAMERA = 1
    private val REQUEST_GALLERY = 2
    private lateinit var processedImageView : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCamera: Button = findViewById(R.id.btn_camera)
        val btnGallery: Button = findViewById(R.id.btn_gallery)
        processedImageView = findViewById(R.id.processed_image)


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
            val bitmap: Bitmap? = when (requestCode) {
                REQUEST_CAMERA -> data?.extras?.get("data") as? Bitmap
                REQUEST_GALLERY -> {
                    val imageUri: Uri = data?.data ?: return
                    contentResolver.openInputStream(imageUri)?.use { BitmapFactory.decodeStream(it) }
                }
                else -> null
            }
            bitmap?.let { processAndDisplayImage(it) }
        }
    }

    private fun processAndDisplayImage(inputBitmap: Bitmap) {
        //resizing the bitmap to match model Input Size 640
        //val modelInputSize= 640
        //val resizedBitmap = Bitmap.createScaledBitmap(inputBitmap, modelInputSize,modelInputSize, false);

        //this part for loading the TFLite model and processing the img

        val interpreter = org.tensorflow.lite.Interpreter(FileUtil.loadMappedFile(this,"best_float16.tflite"))

        val inputShape = interpreter.getInputTensor(0).shape()
        val inputImageWidth = inputShape[1]
        val inputImageHeight = inputShape[2]
        val modelInputSize = inputImageWidth * inputImageHeight * 3


        println(inputShape)

        println(modelInputSize)

        val resizedBitmap = Bitmap.createScaledBitmap(inputBitmap, inputImageWidth, inputImageHeight, false)

        println("----------------------------------------------------------")
        println(inputShape)

        println(modelInputSize)

        val tensorImage = TensorImage(interpreter.getInputTensor(0).dataType())
        tensorImage.load(resizedBitmap)
        val inputBuffer = tensorImage.buffer

        val outputShape = interpreter.getOutputTensor(0).shape()
        println("##########################################################")
        println(outputShape)
        val outputBuffer = TensorBuffer.createFixedSize(outputShape, tensorImage.dataType)

        interpreter.run(inputBuffer, outputBuffer.buffer)

        println("#########---------------***********************")

        val outputBitmap = ByteBufferToBitmap(outputBuffer.buffer,inputImageWidth,inputImageHeight)

        processedImageView.setImageBitmap(outputBitmap)

    }
    private fun ByteBufferToBitmap(buffer: ByteBuffer, width:Int, height: Int):Bitmap{
        buffer.rewind()
        val pixels = IntArray(width * height)
        for(i in pixels.indices){
            val r =buffer.get().toInt() and 0xFF
            val g = buffer.get().toInt() and 0xFF
            val b= buffer.get().toInt() and 0xFF
            pixels[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
        }
        return Bitmap.createBitmap(pixels, width, height , Bitmap.Config.ARGB_8888)
    }
}

