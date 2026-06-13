package br.com.faculdade.felipeapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class FormLogin : AppCompatActivity() {

    private lateinit var edit_email: EditText
    private lateinit var edit_senha: EditText
    private lateinit var btnEntrar: Button
    private lateinit var progressbar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_login)
        supportActionBar?.hide()

        edit_email = findViewById(R.id.edit_email_login)
        edit_senha = findViewById(R.id.edit_senha_login)
        btnEntrar = findViewById(R.id.bt_entrada)
        progressbar = findViewById(R.id.progressbar)

        configurarLinkCadastro()

        btnEntrar.setOnClickListener {
            val email = edit_email.text.toString().trim()
            val senha = edit_senha.text.toString().trim()
            if (email.isEmpty() || senha.isEmpty()) {
                Snackbar.make(it, "Campos não preenchidos, tente novamente", Snackbar.LENGTH_LONG).show()
            } else {
                logarUsuario(it)
            }
        }
    }

    private fun configurarLinkCadastro() {
        val linkFormCadastro = findViewById<TextView>(R.id.text_tela_cadastro)
        val texto = "Não tem conta? Clique aqui para se cadastrar"
        val spannable = SpannableString(texto)
        val inicio = texto.indexOf("Clique aqui")
        val fim = inicio + "Clique aqui".length

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@FormLogin, FormCadastro::class.java))
            }
            override fun updateDrawState(ds: TextPaint) {
                ds.color = Color.parseColor("#FFD700")
                ds.isUnderlineText = false
            }
        }, inicio, fim, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannable.setSpan(ForegroundColorSpan(Color.WHITE), 0, inicio, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(Color.WHITE), fim, texto.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        linkFormCadastro.text = spannable
        linkFormCadastro.movementMethod = LinkMovementMethod.getInstance()
        linkFormCadastro.highlightColor = Color.TRANSPARENT
    }

    private fun logarUsuario(it: View) {
        val email = edit_email.text.toString().trim()
        val senha = edit_senha.text.toString().trim()
        progressbar.visibility = View.VISIBLE

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                progressbar.visibility = View.INVISIBLE
                if (task.isSuccessful) {
                    startActivity(Intent(this, TelaPrincipal::class.java))
                    finish()
                } else {
                    Snackbar.make(it, "Erro ao realizar login", Snackbar.LENGTH_LONG).show()
                }
            }
    }
}
