package com.toochi.job_swift.common.activities

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.squareup.picasso.Picasso
import com.toochi.job_swift.backend.PersonalDetailsManager.updateProfileImage
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.ActivityImagePreviewBinding
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME


class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImagePreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar.toolbar)

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        ViewCompat.setTransitionName(
            binding.profileImageView,
            ViewCompat.getTransitionName(binding.profileImageView) ?: ""
        )

        val sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val photoUrl = sharedPreferences.getString("photo_url", "")

        if (!photoUrl.isNullOrEmpty())
            Picasso.get().load(photoUrl).into(binding.profileImageView)

        val imageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                onPickedImage(uri, getFileName(uri))
            } else {
                onPickedImage(null, null)
            }
        }

        binding.editImage.setOnClickListener {
            imageLauncher.launch("image/*")
        }

    }

    private fun onPickedImage(uri: Uri?, fileName: String?) {
        val loadingDialog = LoadingDialog(this)

        try {
            if (uri != null) {
                loadingDialog.show()

                val profileId =
                    getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString("profile_id", "")

                profileId?.let {
                    updateProfileImage(
                        profileId = it,
                        imageUri = uri,
                        imageName = "$fileName"
                    ) { success, error ->
                        if (success) {
                            loadingDialog.dismiss()
                            Picasso.get().load(uri).into(binding.profileImageView)
                        } else {
                            loadingDialog.dismiss()
                            Toast.makeText(
                                this@ImagePreviewActivity,
                                error.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
            Toast.makeText(this@ImagePreviewActivity, "An error occurred.", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun getFileName(uri: Uri): String? {
        contentResolver.query(
            uri, null, null,
            null, null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            return cursor.getString(nameIndex)
        }
        return null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finishAfterTransition()
            true
        } else {
            false
        }
    }
}