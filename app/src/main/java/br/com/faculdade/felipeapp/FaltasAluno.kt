package br.com.faculdade.felipeapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import com.google.firebase.firestore.FirebaseFirestore

class FaltasAluno : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faltas_aluno)
        supportActionBar?.hide()

        val editBusca = findViewById<EditText>(R.id.edit_busca_aluno)
        val btnBuscar = findViewById<AppCompatButton>(R.id.btn_buscar_faltas)
        val cardResultado = findViewById<CardView>(R.id.card_resultado)
        val tvNome = findViewById<TextView>(R.id.tv_nome_resultado)
        val tvMatricula = findViewById<TextView>(R.id.tv_matricula_resultado)
        val tvTotalFaltas = findViewById<TextView>(R.id.tv_total_faltas)
        val tvNaoEncontrado = findViewById<TextView>(R.id.tv_nao_encontrado)

        findViewById<TextView>(R.id.tv_voltar_faltas).setOnClickListener { finish() }

        btnBuscar.setOnClickListener {
            val busca = editBusca.text.toString().trim()
            if (busca.isEmpty()) return@setOnClickListener

            cardResultado.visibility = View.GONE
            tvNaoEncontrado.visibility = View.GONE

            val db = FirebaseFirestore.getInstance()

            db.collection("Alunos")
                .get()
                .addOnSuccessListener { alunosDocs ->
                    val aluno = alunosDocs.documents.firstOrNull { doc ->
                        val nome = doc.getString("nome") ?: ""
                        val matricula = doc.getString("matricula") ?: ""
                        nome.contains(busca, ignoreCase = true) || matricula == busca
                    }

                    if (aluno == null) {
                        tvNaoEncontrado.visibility = View.VISIBLE
                        return@addOnSuccessListener
                    }

                    val nomeAluno = aluno.getString("nome") ?: ""
                    val matriculaAluno = aluno.getString("matricula") ?: ""

                    db.collection("Chamadas")
                        .get()
                        .addOnSuccessListener { chamadaDocs ->
                            var totalFaltas = 0
                            for (doc in chamadaDocs) {
                                val faltas = doc.get("faltas") as? List<*> ?: emptyList<Any>()
                                if (faltas.contains(matriculaAluno)) {
                                    totalFaltas++
                                }
                            }

                            tvNome.text = nomeAluno
                            tvMatricula.text = "Matrícula: $matriculaAluno"
                            tvTotalFaltas.text = totalFaltas.toString()
                            cardResultado.visibility = View.VISIBLE
                        }
                        .addOnFailureListener {
                            tvNaoEncontrado.text = "Erro ao buscar chamadas"
                            tvNaoEncontrado.visibility = View.VISIBLE
                        }
                }
                .addOnFailureListener {
                    tvNaoEncontrado.text = "Erro ao buscar aluno"
                    tvNaoEncontrado.visibility = View.VISIBLE
                }
        }
    }
}
