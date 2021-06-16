package com.argostock.capstoneapp.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.argostock.capstoneapp.R
import com.argostock.capstoneapp.camera.remote.network.UploadRequest
import com.argostock.capstoneapp.camera.utils.getFileName
import com.argostock.capstoneapp.camera.utils.snackbar
import com.argostock.capstoneapp.databinding.FragmentCameraBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


@Suppress("DEPRECATION")
class CameraFragment : Fragment(), UploadRequest.UploadCallback {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding as FragmentCameraBinding
    private val homeViewModel by lazy {
        ViewModelProvider(this, FactoryViewModel.getInstance(requireActivity())).get(CameraViewModel::class.java)
    }
    private var selectedImageUri: Uri? = null
    private lateinit var photoFile: File
    private lateinit var file: File
    private lateinit var body: UploadRequest

    companion object {
        const val REQUEST_CODE_CAM = 100
        const val REQUEST_CODE_DIR = 101
        private const val FILE_NAME = "photo.jpg"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View { _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            buttonCapture.setOnClickListener {
                val directoryIntent = Intent(Intent.ACTION_PICK)
                directoryIntent.type = "image/"
                startActivityForResult(directoryIntent, REQUEST_CODE_DIR)
            }

            buttonPredict.setOnClickListener {
                buttonPredict.visibility = View.GONE
                uploadImage()
                context?.let { getDataUserFromApi(it) }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_CODE_CAM && resultCode == Activity.RESULT_OK) {
            //val imageData = data?.extras?.get("data") as Bitmap
            val imageData = BitmapFactory.decodeFile(photoFile.absolutePath)
            binding.apply {
                imgPreviewSample.visibility = View.GONE
                imagePreview.visibility = View.VISIBLE
                imagePreview.setImageBitmap(imageData)
                val goImage = getImageUriFromBitmap(imageData)
                selectedImageUri = goImage
                buttonPredict.visibility = View.VISIBLE
            }
        } else if (requestCode == REQUEST_CODE_DIR && resultCode == Activity.RESULT_OK) {
            binding.apply {
                imgPreviewSample.visibility = View.GONE
                imagePreview.visibility = View.VISIBLE
                selectedImageUri = data?.data
                imagePreview.setImageURI(selectedImageUri)
                buttonPredict.visibility = View.VISIBLE
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getDataUserFromApi(context: Context) {
        homeViewModel.getPredictionResult(context, file, body).observe(viewLifecycleOwner, { fruit ->
            binding.progBar.progress = 100
            binding.progBar.visibility = View.INVISIBLE
            val result = fruit.data

            val text1 = result?.result

            val text2: String = result?.result.toString()

            binding.tvResultPredict.text = resources.getString(R.string.result_predict, text1.toString(), text2)
        })
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun uploadImage() {
        if (selectedImageUri == null) {
            binding.layoutRoot.snackbar("Select an Image First")
            return
        }
        val resolver = requireActivity().contentResolver
        val cacheDir = activity?.externalCacheDir

        val parcelFileDescriptor =
            resolver.openFileDescriptor(selectedImageUri!!, "r", null) ?: return


        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        file = File(cacheDir, resolver.getFileName(selectedImageUri!!))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        binding.progBar.progress = 0

        body = UploadRequest(file, "image", this)
    }

    override fun onProgressUpdate(percentage: Int) {
        binding.progBar.progress = percentage
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes)
        val resolver = requireActivity().contentResolver
        val path = MediaStore.Images.Media.insertImage(resolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }

}