package br.com.faculdade.felipeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TelaPerfil : AppCompatActivity() {

    private lateinit var mailUser: EditText
    private lateinit var usuarioUser: EditText
    private lateinit var bt_sair: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_perfil)
        supportActionBar?.hide()
        IniciarComponentes()
        fetchAllNames()
        db = FirebaseFirestore.getInstance()
        bt_sair.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@TelaPerfil, FormLogin::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<TextView>(R.id.tv_voltar_perfil).setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        mailUser.setText(userEmail)
        if (userEmail != null) {
            buscarNomeDoEmail(userEmail)
        }
    }

    fun buscarNomeDoEmail(email: String) {
        val usuariosRef = db.collection("Usuarios")
        val query = usuariosRef.whereEqualTo("email", email)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documento = querySnapshot.documents[0]
                    val nome = documento.getString("nome")
                    if (nome != null) {
                        usuarioUser.setText(nome)
                    } else {
                        println("Nome não encontrado para o e-mail $email")
                    }
                } else {
                    println("Nenhum documento encontrado para o e-mail $email")
                }
            }
            .addOnFailureListener { e ->
                println("Erro ao buscar documento: $e")
            }
    }

    fun IniciarComponentes() {
        mailUser = findViewById(R.id.textEmailUser)
        usuarioUser = findViewById(R.id.textNomeUser)
        bt_sair = findViewById(R.id.bt_sair)
    }

    fun fetchAllNames() {
        val db = FirebaseFirestore.getInstance()
        val usuariosRef = db.collection("Usuarios")

        usuariosRef.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val nome = document.getString("nome")
                println("Nome: $nome")
            }
        }.addOnFailureListener { exception ->
            println("Erro ao buscar os nomes: ${exception.message}")
        }
    }
}
