package com.example.snapshots

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.snapshots.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var mActiveFragment: Fragment
    private lateinit var mFragmentManager: FragmentManager

    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    private var mFirebaseAuth:FirebaseAuth? = null

    private val authResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK){
            Toast.makeText(this,"Bienvenido",Toast.LENGTH_SHORT).show()
        } else{
            if(IdpResponse.fromResultIntent(it.data) == null){
                finish()
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(mBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setUpAuth()
        setUpBottomNav()
    }

    private fun setUpAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser
            if (user == null){
                authResult.launch(
                    AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(
                            listOf(AuthUI.IdpConfig.EmailBuilder().build(),
                                AuthUI.IdpConfig.GoogleBuilder().build())
                        )
                        .build()
                )
            }
        }

    }

    private fun setUpBottomNav(){
        mFragmentManager = supportFragmentManager

        val homeFragment = HomeFragment()
        val addFragment = AddFragment()
        val profileFragment = ProfileFragment()

        mActiveFragment = homeFragment

        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment,profileFragment,ProfileFragment::class.java.name)
            .hide(profileFragment)
            .commit()
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment,addFragment,AddFragment::class.java.name)
            .hide(addFragment)
            .commit()
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment,homeFragment,HomeFragment::class.java.name)
            .commit()

        mBinding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.action_home -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(homeFragment).commit()
                    mActiveFragment = homeFragment
                    true
                }
                R.id.action_add -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(addFragment).commit()
                    mActiveFragment = addFragment
                    true
                }
                R.id.action_profile -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(profileFragment).commit()
                    mActiveFragment = profileFragment
                    true
                }

                else -> false
            }
        }

        mBinding.bottomNav.setOnItemReselectedListener {
            when(it.itemId){
                R.id.action_home -> (homeFragment as SnapshotActionListener).goToTop()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth?.addAuthStateListener(mAuthListener)
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth?.removeAuthStateListener(mAuthListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}