package com.example.myapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.myapp.databinding.ActivityClubRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Completable.merge
import io.reactivex.Observable


@SuppressLint("CheckResult")
class ClubRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClubRegisterBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClubRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

//  Auth
        auth = FirebaseAuth.getInstance()

//  Club_Fullname Validation
        val nameStream = RxTextView.textChanges(binding.etClubFullname)
            .skipInitialValue()
            .map {name ->
                name.isEmpty()
            }
        nameStream.subscribe {
            showNameExistAlert(it)
        }

//  Email Validation
        val emailStream = RxTextView.textChanges(binding.etClubEmail)
            .skipInitialValue()
            .map {email ->
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        emailStream.subscribe {
            showEmailValidAlert(it)
        }

//  Username Validation
        val usernameStream = RxTextView.textChanges(binding.etClubFaculty)
            .skipInitialValue()
            .map {username ->
                username.length < 6
            }
        usernameStream.subscribe {
            showTextMinimalAlert(it , "Username")
        }

//  Password Validation
        val passwordStream = RxTextView.textChanges(binding.etClubPassword)
            .skipInitialValue()
            .map {password ->
                password.length < 6
            }
        passwordStream.subscribe {
            showTextMinimalAlert(it , "Password")
        }

//  Confirm Password Validation
        val passwordConfirmStream = Observable.merge(
            RxTextView.textChanges(binding.etClubPassword)
                .skipInitialValue()
                .map {password ->
                    password.toString() != binding.etClubConfirmPassword.text.toString()

                },
            RxTextView.textChanges(binding.etClubConfirmPassword)
                .skipInitialValue()
                .map {confirmPassword ->
                    confirmPassword.toString() != binding.etClubPassword.text.toString()
                })
        passwordConfirmStream.subscribe {
            showPasswordConfirmAlert(it)
        }

//  Button enable true or false
        val invalidFieldsStream = Observable.combineLatest(
            nameStream,
            emailStream,
            usernameStream,
            passwordStream,
            passwordConfirmStream,
            { nameInvalid: Boolean , emailInvalid:Boolean , usernameInvalid: Boolean, passwordInvalid: Boolean, passwordConfirmInvalid: Boolean ->
                !nameInvalid && !emailInvalid && !usernameInvalid && !passwordInvalid && !passwordConfirmInvalid
            })
        invalidFieldsStream.subscribe { isValid ->
            if(isValid) {
                binding.btnClubRegister.isEnabled = true
                binding.btnClubRegister.backgroundTintList = ContextCompat.getColorStateList(this , R.color.primary_color)
            }else {
                binding.btnClubRegister.isEnabled = false
                binding.btnClubRegister.backgroundTintList = ContextCompat.getColorStateList(this , android.R.color.darker_gray)
            }
        }





//  Click
        binding.btnClubRegister.setOnClickListener{
            val email = binding.etClubEmail.text.toString().trim()
            val password = binding.etClubPassword.text.toString().trim()
            registerUser(email, password)
        }
        binding.tvClubHaveAccount.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun showNameExistAlert(isNotValid: Boolean){
        binding.etClubFullname.error = if (isNotValid) "Name cannot be empty!" else null
    }

    private fun showTextMinimalAlert(isNotValid: Boolean , text: String) {
        if(text == "Username")
            binding.etClubFaculty.error = if(isNotValid) "$text Must be more than 6 letters!" else null
        else if(text == "Password")
            binding.etClubPassword.error = if(isNotValid) "$text Must be more than 8 letters! " else null

    }

    private fun showEmailValidAlert(isNotValid: Boolean){
        binding.etClubEmail.error = if(isNotValid) "Email not valid!" else null

    }

    private fun showPasswordConfirmAlert(isNotValid: Boolean){
        binding.etClubConfirmPassword.error = if(isNotValid) "Password not same!" else null
    }

    private fun registerUser(email: String , password: String) {
        auth.createUserWithEmailAndPassword(email , password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    startActivity(Intent(this, LoginActivity::class.java))
                    Toast.makeText(this, "Register successful" , Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

}