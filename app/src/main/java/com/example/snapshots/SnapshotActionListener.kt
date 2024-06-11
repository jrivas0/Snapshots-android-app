package com.example.snapshots

interface SnapshotActionListener {
    fun onDeleteSnapshot(snapshot: Snapshot)
    fun onSetLike(snapshot: Snapshot,isChecked: Boolean)
    fun goToTop()
}
