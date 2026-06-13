package br.com.faculdade.felipeapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
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
        val containerResultados = findViewById<LinearLayout>(R.id.container_resultados)
        val tvNaoEncontrado = findViewById<TextView>(R.id.tv_nao_encontrado)

        findViewById<TextView>(R.id.tv_voltar_faltas).setOnClickListener { finish() }

        btnBuscar.setOnClickListener {
            val busca = editBusca.text.toString().trim()
            if (busca.isEmpty()) return@setOnClickListener

            containerResultados.removeAllViews()
            tvNaoEncontrado.visibility = View.GONE

            val db = FirebaseFirestore.getInstance()

            db.collection("Alunos").get()
                .addOnSuccessListener { alunosDocs ->
                    val alunosEncontrados = alunosDocs.documents.filter { doc ->
                        val nome = doc.getString("nome")?.trim() ?: ""
                        val matricula = doc.getString("matricula")?.trim() ?: ""
                        nome.equals(busca, ignoreCase = true) || matricula == busca
                    }

                    if (alunosEncontrados.isEmpty()) {
                        tvNaoEncontrado.visibility = View.VISIBLE
                        return@addOnSuccessListener
                    }

                    db.collection("Chamadas").get()
                        .addOnSuccessListener { chamadaDocs ->
                            for (aluno in alunosEncontrados) {
                                val nomeAluno = aluno.getString("nome") ?: ""
                                val matriculaAluno = aluno.getString("matricula") ?: ""

                                var totalFaltas = 0
                                for (chamada in chamadaDocs) {
                                    val faltas = chamada.get("faltas") as? List<*> ?: emptyList<Any>()
                                    if (faltas.contains(matriculaAluno)) totalFaltas++
                                }

                                adicionarCard(containerResultados, nomeAluno, matriculaAluno, totalFaltas)
                            }
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

    private fun adicionarCard(container: LinearLayout, nome: String, matricula: String, faltas: Int) {
        val card = CardView(this).apply {
            radius = 40f
            cardElevation = 8f
            setCardBackgroundColor(android.graphics.Color.WHITE)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = 24
            layoutParams = params
        }

        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 40, 48, 40)
        }

        val tvNome = TextView(this).apply {
            text = nome
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#1A1A2E"))
        }

        val tvMatricula = TextView(this).apply {
            text = "Matrícula: $matricula"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#555555"))
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.bottomMargin = 24
            layoutParams = p
        }

        val divisor = View(this).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#EEEEEE"))
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
            p.bottomMargin = 24
            layoutParams = p
        }

        val tvFaltasNum = TextView(this).apply {
            text = "$faltas"
            textSize = 36f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#E53935"))
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        val tvFaltasLabel = TextView(this).apply {
            text = "falta(s) registrada(s)"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#888888"))
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        inner.addView(tvNome)
        inner.addView(tvMatricula)
        inner.addView(divisor)
        inner.addView(tvFaltasNum)
        inner.addView(tvFaltasLabel)
        card.addView(inner)
        container.addView(card)
    }
}
