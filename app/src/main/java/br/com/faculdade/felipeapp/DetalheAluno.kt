package br.com.faculdade.felipeapp

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class DetalheAluno : AppCompatActivity() {

    private lateinit var editNome: EditText
    private lateinit var editEmail: EditText
    private lateinit var editMatricula: EditText
    private lateinit var documentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_aluno)
        supportActionBar?.hide()

        editNome = findViewById(R.id.edit_detalhe_nome)
        editEmail = findViewById(R.id.edit_detalhe_email)
        editMatricula = findViewById(R.id.edit_detalhe_matricula)

        documentId = intent.getStringExtra("documentId") ?: ""
        editNome.setText(intent.getStringExtra("nome") ?: "")
        editEmail.setText(intent.getStringExtra("email") ?: "")
        editMatricula.setText(intent.getStringExtra("matricula") ?: "")

        findViewById<TextView>(R.id.tv_voltar_detalhe).setOnClickListener { finish() }

        findViewById<AppCompatButton>(R.id.btn_salvar_detalhe).setOnClickListener { view ->
            val novoNome = editNome.text.toString().trim()
            val novoEmail = editEmail.text.toString().trim()
            val novaMatricula = editMatricula.text.toString().trim()

            if (novoNome.isEmpty() || novaMatricula.isEmpty()) {
                Snackbar.make(view, "Nome e matrícula são obrigatórios", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseFirestore.getInstance()
                .collection("Alunos")
                .document(documentId)
                .update(
                    mapOf(
                        "nome" to novoNome,
                        "email" to novoEmail,
                        "matricula" to novaMatricula
                    )
                )
                .addOnSuccessListener {
                    Snackbar.make(view, "✅ Dados atualizados com sucesso!", Snackbar.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Snackbar.make(view, "Erro ao atualizar dados", Snackbar.LENGTH_SHORT).show()
                }
        }
    }
}
