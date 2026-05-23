package br.com.faculdade.felipeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AlunosCadastrados : AppCompatActivity() {

    data class Aluno(val nome: String, val email: String, val matricula: String)

    inner class AlunoAdapter(private val lista: List<Aluno>) :
        RecyclerView.Adapter<AlunoAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNome: TextView = view.findViewById(R.id.tv_nome_item)
            val tvMatricula: TextView = view.findViewById(R.id.tv_matricula_item)
            val tvEmail: TextView = view.findViewById(R.id.tv_email_item)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_aluno, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val aluno = lista[position]
            holder.tvNome.text = aluno.nome
            holder.tvMatricula.text = "Matrícula: ${aluno.matricula}"
            holder.tvEmail.text = aluno.email
        }

        override fun getItemCount() = lista.size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alunos_cadastrados)
        supportActionBar?.hide()

        val rv = findViewById<RecyclerView>(R.id.rv_alunos)
        rv.layoutManager = LinearLayoutManager(this)

        findViewById<TextView>(R.id.tv_voltar_alunos).setOnClickListener { finish() }

        FirebaseFirestore.getInstance().collection("Alunos")
            .orderBy("nome")
            .get()
            .addOnSuccessListener { docs ->
                val lista = docs.map {
                    Aluno(
                        nome = it.getString("nome") ?: "",
                        email = it.getString("email") ?: "",
                        matricula = it.getString("matricula") ?: ""
                    )
                }
                rv.adapter = AlunoAdapter(lista)
                findViewById<TextView>(R.id.tv_total_alunos).text = "${lista.size} aluno(s) cadastrado(s)"
            }
            .addOnFailureListener {
                findViewById<TextView>(R.id.tv_total_alunos).text = "Erro ao carregar alunos"
            }
    }
}
