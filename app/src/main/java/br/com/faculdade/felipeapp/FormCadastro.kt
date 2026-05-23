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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FormCadastro : AppCompatActivity() {

    private lateinit var edit_nome: EditText
    private lateinit var edit_email: EditText
    private lateinit var edit_senha: EditText
    private lateinit var btnCadastrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_cadastro)
        supportActionBar?.hide()

        edit_nome = findViewById(R.id.edit_nome)
        edit_email = findViewById(R.id.edit_email)
        edit_senha = findViewById(R.id.edit_senha)
        btnCadastrar = findViewById(R.id.bt_cadastrar)

        configurarLinkLogin()

        btnCadastrar.setOnClickListener {
            val nome = edit_nome.text.toString().trim()
            val email = edit_email.text.toString().trim()
            val senha = edit_senha.text.toString().trim()

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Snackbar.make(it, "Campos não preenchidos, tente novamente", Snackbar.LENGTH_LONG).show()
            } else if (senha.length < 6) {
                Snackbar.make(it, "A senha deve ter pelo menos 6 caracteres", Snackbar.LENGTH_LONG).show()
            } else {
                cadastrarUsuario(it)
            }
        }
    }

    private fun configurarLinkLogin() {
        val linkLogin = findViewById<TextView>(R.id.text_tela_login)
        val texto = "Já tem conta? Clique aqui para entrar"
        val spannable = SpannableString(texto)
        val inicio = texto.indexOf("Clique aqui")
        val fim = inicio + "Clique aqui".length

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@FormCadastro, FormLogin::class.java))
                finish()
            }
            override fun updateDrawState(ds: TextPaint) {
                ds.color = Color.parseColor("#FFD700")
                ds.isUnderlineText = false
            }
        }, inicio, fim, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannable.setSpan(ForegroundColorSpan(Color.WHITE), 0, inicio, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(Color.WHITE), fim, texto.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        linkLogin.text = spannable
        linkLogin.movementMethod = LinkMovementMethod.getInstance()
        linkLogin.highlightColor = Color.TRANSPARENT
    }

    private fun cadastrarUsuario(it: View) {
        val email = edit_email.text.toString().trim()
        val senha = edit_senha.text.toString().trim()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    salvarDadosUsuario()
                    startActivity(Intent(this, TelaPrincipal::class.java))
                    finish()
                } else {
                    Snackbar.make(it, "Erro ao cadastrar: ${task.exception?.localizedMessage}", Snackbar.LENGTH_LONG).show()
                }
            }
    }

    private fun salvarDadosUsuario() {
        val db = FirebaseFirestore.getInstance()
        val nome = edit_nome.text.toString().trim()
        val usuarioID = FirebaseAuth.getInstance().currentUser?.uid
        val email = FirebaseAuth.getInstance().currentUser?.email
        if (usuarioID != null && email != null) {
            val usuarios = hashMapOf("nome" to nome, "email" to email, "uid" to usuarioID)
            db.collection("Usuarios").add(usuarios)
        }
    }
}
