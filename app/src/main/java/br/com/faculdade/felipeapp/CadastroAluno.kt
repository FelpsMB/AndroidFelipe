package br.com.faculdade.felipeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class CadastroAluno : AppCompatActivity() {

    private lateinit var editNome: EditText
    private lateinit var editEmail: EditText
    private lateinit var editMatricula: EditText
    private lateinit var btnCadastrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_aluno)
        supportActionBar?.hide()

        editNome = findViewById(R.id.edit_nome_aluno)
        editEmail = findViewById(R.id.edit_email_aluno)
        editMatricula = findViewById(R.id.edit_matricula)
        btnCadastrar = findViewById(R.id.bt_cadastrar_aluno)

        findViewById<TextView>(R.id.text_voltar_principal).setOnClickListener {
            finish()
        }

        btnCadastrar.setOnClickListener {
            val nome = editNome.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val matricula = editMatricula.text.toString().trim()

            if (nome.isEmpty() || matricula.isEmpty()) {
                Snackbar.make(it, "Nome e matrícula são obrigatórios", Snackbar.LENGTH_LONG).show()
            } else {
                cadastrarAluno(it, nome, email, matricula)
            }
        }
    }

    private fun cadastrarAluno(view: android.view.View, nome: String, email: String, matricula: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Alunos").whereEqualTo("matricula", matricula).get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    Snackbar.make(view, "Já existe um aluno com essa matrícula", Snackbar.LENGTH_LONG).show()
                } else {
                    val aluno = hashMapOf("nome" to nome, "email" to email, "matricula" to matricula)
                    db.collection("Alunos").add(aluno)
                        .addOnSuccessListener {
                            Snackbar.make(view, "Aluno cadastrado com sucesso!", Snackbar.LENGTH_SHORT).show()
                            editNome.text.clear()
                            editEmail.text.clear()
                            editMatricula.text.clear()
                        }
                        .addOnFailureListener {
                            Snackbar.make(view, "Erro ao cadastrar aluno", Snackbar.LENGTH_LONG).show()
                        }
                }
            }
    }
}
