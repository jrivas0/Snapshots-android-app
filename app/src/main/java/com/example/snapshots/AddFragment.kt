package com.example.snapshots

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.example.snapshots.databinding.FragmentAddBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddFragment : Fragment() {

    private lateinit var mStorageReference: StorageReference
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mBinding: FragmentAddBinding
    private var mPhotoSelectedUri: Uri? = null

    private val galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK){
            mPhotoSelectedUri = it.data?.data
            with(mBinding){
                ivPhoto.setImageURI(mPhotoSelectedUri)
                tilTitle.visibility = View.VISIBLE
                tvMessage.text = getString(R.string.post_message_title_valid)
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAddBinding.inflate(inflater,container,false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.btnPost.setOnClickListener { postSnapshot() }
        mBinding.btnSelect.setOnClickListener { openGallery() }
        mStorageReference = FirebaseStorage.getInstance().reference
        mDatabaseReference = FirebaseDatabase.getInstance().reference.child(SnapshotsApplication.PATH_SNAPSHOTS)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResult.launch(intent)
    }

    @SuppressLint("SetTextI18n")
    private fun postSnapshot() {
        mBinding.progressBarAdd.visibility = View.VISIBLE
        val key = mDatabaseReference.push().key!!
        val storageReference = mStorageReference.child(SnapshotsApplication.currentUser.uid).child(key)

        if(mPhotoSelectedUri != null){
            storageReference.putFile(mPhotoSelectedUri!!)
                .addOnProgressListener {
                    val progress = (100 * it.bytesTransferred/it.totalByteCount).toDouble()
                    mBinding.progressBarAdd.progress = progress.toInt()
                    mBinding.tvMessage.text = "$progress%"

                }
                .addOnCompleteListener{
                    mBinding.progressBarAdd.visibility = View.INVISIBLE
                }
                .addOnSuccessListener {
                    Snackbar.make(mBinding.root,"Publicada",Snackbar.LENGTH_SHORT).show()
                    it.storage.downloadUrl.addOnSuccessListener {
                        saveSnapshot(key,it.toString(),mBinding.etTitle.text.toString().trim())
                        mBinding.tilTitle.visibility = View.GONE
                        mBinding.tvMessage.text = getString(R.string.post_message_title)
                    }
                }
                .addOnFailureListener{
                    Snackbar.make(mBinding.root,"Fallo",Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveSnapshot(key:String,url:String,title:String){
        val snapshot = Snapshot(title=title, photoUrl = url,
            ownerUid = SnapshotsApplication.currentUser.uid)
        mDatabaseReference.child(key).setValue(snapshot)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }


}