package br.com.faculdade.felipeapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TelaPrincipal : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_principal)
        supportActionBar?.hide()

        carregarNomeUsuario()

        findViewById<MaterialCardView>(R.id.card_perfil).setOnClickListener {
            startActivity(Intent(this, TelaPerfil::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_cadastro_aluno).setOnClickListener {
            startActivity(Intent(this, CadastroAluno::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_alunos).setOnClickListener {
            startActivity(Intent(this, AlunosCadastrados::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_chamada).setOnClickListener {
            startActivity(Intent(this, Chamada::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_faltas).setOnClickListener {
            startActivity(Intent(this, FaltasAluno::class.java))
        }
    }

    private fun carregarNomeUsuario() {
        val tvNome = findViewById<TextView>(R.id.tv_nome_usuario)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Usuarios")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { docs ->
                for (doc in docs) {
                    tvNome.text = doc.getString("nome") ?: ""
                }
            }
    }
}
