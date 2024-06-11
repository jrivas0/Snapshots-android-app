package com.example.snapshots

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.snapshots.databinding.ItemSnapshotBinding
import com.google.firebase.auth.FirebaseAuth

class SnapshotsViewHolder(view: View, private val mContext: Context,private val listener: SnapshotActionListener) : RecyclerView.ViewHolder(view) {

    val binding = ItemSnapshotBinding.bind(view)

    fun setListener(snapshot: Snapshot) {
        binding.btnDelete.setOnClickListener { listener.onDeleteSnapshot(snapshot) }
        binding.cbLike.setOnCheckedChangeListener{ compoundButton, checked ->
            listener.onSetLike(snapshot,checked)
        }
    }

    fun render(snapshot: Snapshot){
        setListener(snapshot)
        binding.tvTitle.text = snapshot.title
        binding.cbLike.text = snapshot.likeList.keys.size.toString()
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            binding.cbLike.isChecked = snapshot.likeList.containsKey(it.uid)
        }

        Glide.with(mContext)
            .load(snapshot.photoUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(binding.ivPhoto)

        binding.btnDelete.visibility = if (snapshot.ownerUid == SnapshotsApplication.currentUser.uid){
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

}