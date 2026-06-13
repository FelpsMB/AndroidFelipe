package br.com.faculdade.felipeapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AlunosCadastrados : AppCompatActivity() {

    data class Aluno(
        val id: String,
        val nome: String,
        val email: String,
        val matricula: String
    )

    private val todosAlunos = mutableListOf<Aluno>()
    private val alunosFiltrados = mutableListOf<Aluno>()
    private val listaPagina = mutableListOf<Aluno>()
    private lateinit var adapter: AlunoAdapter
    private val POR_PAGINA = 5
    private var paginaAtual = 0

    inner class AlunoAdapter(private val lista: MutableList<Aluno>) :
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

            holder.itemView.setOnClickListener {
                val intent = Intent(this@AlunosCadastrados, DetalheAluno::class.java)
                intent.putExtra("documentId", aluno.id)
                intent.putExtra("nome", aluno.nome)
                intent.putExtra("email", aluno.email)
                intent.putExtra("matricula", aluno.matricula)
                startActivity(intent)
            }
        }

        override fun getItemCount() = lista.size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alunos_cadastrados)
        supportActionBar?.hide()

        val rv = findViewById<RecyclerView>(R.id.rv_alunos)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = AlunoAdapter(listaPagina)
        rv.adapter = adapter

        findViewById<TextView>(R.id.tv_voltar_alunos).setOnClickListener { finish() }

        findViewById<EditText>(R.id.edit_pesquisa_aluno).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarAlunos(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        carregarTodosAlunos()
    }

    override fun onResume() {
        super.onResume()
        carregarTodosAlunos()
    }

    private fun carregarTodosAlunos() {
        FirebaseFirestore.getInstance()
            .collection("Alunos")
            .orderBy("nome")
            .get()
            .addOnSuccessListener { docs ->
                todosAlunos.clear()
                for (doc in docs) {
                    todosAlunos.add(
                        Aluno(
                            id = doc.id,
                            nome = doc.getString("nome") ?: "",
                            email = doc.getString("email") ?: "",
                            matricula = doc.getString("matricula") ?: ""
                        )
                    )
                }
                alunosFiltrados.clear()
                alunosFiltrados.addAll(todosAlunos)
                paginaAtual = 0
                atualizarPagina()
                criarBotoesNumericos()
            }
            .addOnFailureListener {
                findViewById<TextView>(R.id.tv_total_alunos).text = "Erro ao carregar alunos"
            }
    }

    private fun filtrarAlunos(query: String) {
        alunosFiltrados.clear()
        if (query.isEmpty()) {
            alunosFiltrados.addAll(todosAlunos)
        } else {
            alunosFiltrados.addAll(todosAlunos.filter {
                it.nome.trim().equals(query, ignoreCase = true)
            })
        }
        paginaAtual = 0
        atualizarPagina()
        criarBotoesNumericos()
    }

    private fun atualizarPagina() {
        val inicio = paginaAtual * POR_PAGINA
        val fim = minOf(inicio + POR_PAGINA, alunosFiltrados.size)

        listaPagina.clear()
        if (alunosFiltrados.isNotEmpty()) {
            listaPagina.addAll(alunosFiltrados.subList(inicio, fim))
        }
        adapter.notifyDataSetChanged()

        val totalPaginas = totalPaginas()
        val total = alunosFiltrados.size
        findViewById<TextView>(R.id.tv_total_alunos).text =
            if (total == 0) "Nenhum aluno encontrado"
            else "$total aluno(s) — Página ${paginaAtual + 1} de $totalPaginas"
    }

    private fun totalPaginas(): Int {
        return if (alunosFiltrados.isEmpty()) 1
        else Math.ceil(alunosFiltrados.size.toDouble() / POR_PAGINA).toInt()
    }

    private fun criarBotoesNumericos() {
        val container = findViewById<LinearLayout>(R.id.ll_paginas)
        container.removeAllViews()

        val total = totalPaginas()
        if (total <= 1 || alunosFiltrados.isEmpty()) return

        for (i in 0 until total) {
            val tv = TextView(this)
            tv.text = "${i + 1}"
            tv.textSize = 16f
            tv.setPadding(24, 16, 24, 16)
            tv.setTypeface(null, if (i == paginaAtual) Typeface.BOLD else Typeface.NORMAL)
            tv.setTextColor(if (i == paginaAtual) Color.parseColor("#FFD700") else Color.WHITE)
            tv.background = if (i == paginaAtual) {
                val bg = android.graphics.drawable.GradientDrawable()
                bg.shape = android.graphics.drawable.GradientDrawable.OVAL
                bg.setColor(Color.parseColor("#44FFFFFF"))
                bg
            } else null

            tv.setOnClickListener {
                paginaAtual = i
                atualizarPagina()
                criarBotoesNumericos()
            }

            container.addView(tv)
        }
    }
}
