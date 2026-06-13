package br.com.faculdade.felipeapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Chamada : AppCompatActivity() {

    data class AlunoItem(val nome: String, val matricula: String, var falta: Boolean = false)

    private val listaAlunos = mutableListOf<AlunoItem>()
    private lateinit var adapter: ChamadaAdapter
    private var dataSelecionada = ""

    inner class ChamadaAdapter(private val lista: MutableList<AlunoItem>) :
        RecyclerView.Adapter<ChamadaAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNome: TextView = view.findViewById(R.id.tv_nome_chamada)
            val tvMatricula: TextView = view.findViewById(R.id.tv_matricula_chamada)
            val cbFalta: CheckBox = view.findViewById(R.id.cb_falta)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chamada_aluno, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val aluno = lista[position]
            holder.tvNome.text = aluno.nome
            holder.tvMatricula.text = "Mat: ${aluno.matricula}"
            holder.cbFalta.isChecked = aluno.falta
            holder.cbFalta.setOnCheckedChangeListener { _, isChecked ->
                lista[position].falta = isChecked
            }
        }

        override fun getItemCount() = lista.size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chamada)
        supportActionBar?.hide()

        val calendar = Calendar.getInstance()
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dataSelecionada = formato.format(calendar.time)
        findViewById<TextView>(R.id.tv_data_chamada).text = dataSelecionada

        adapter = ChamadaAdapter(listaAlunos)
        val rv = findViewById<RecyclerView>(R.id.rv_chamada)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        carregarAlunos()

        findViewById<TextView>(R.id.btn_alterar_data).setOnClickListener {
            abrirDatePicker()
        }

        findViewById<TextView>(R.id.tv_voltar_chamada).setOnClickListener { finish() }

        findViewById<Button>(R.id.btn_finalizar_chamada).setOnClickListener {
            finalizarChamada(it)
        }
    }

    private fun carregarAlunos() {
        FirebaseFirestore.getInstance().collection("Alunos")
            .orderBy("nome")
            .get()
            .addOnSuccessListener { docs ->
                listaAlunos.clear()
                for (doc in docs) {
                    listaAlunos.add(
                        AlunoItem(
                            nome = doc.getString("nome") ?: "",
                            matricula = doc.getString("matricula") ?: ""
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun abrirDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            dataSelecionada = String.format("%02d/%02d/%04d", day, month + 1, year)
            findViewById<TextView>(R.id.tv_data_chamada).text = dataSelecionada
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun finalizarChamada(view: View) {
        val faltas = listaAlunos.filter { it.falta }.map { it.matricula }
        val chamada = hashMapOf(
            "data" to dataSelecionada,
            "faltas" to faltas,
            "totalFaltas" to faltas.size
        )

        FirebaseFirestore.getInstance().collection("Chamadas").add(chamada)
            .addOnSuccessListener {
                Snackbar.make(view, "Chamada de $dataSelecionada registrada! ${faltas.size} falta(s).", Snackbar.LENGTH_LONG).show()
                listaAlunos.forEach { it.falta = false }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Snackbar.make(view, "Erro ao salvar chamada", Snackbar.LENGTH_LONG).show()
            }
    }
}
