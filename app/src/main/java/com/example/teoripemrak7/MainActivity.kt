package com.example.teoripemrak7
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var listView: ListView
    private lateinit var btnRetry: Button
    private lateinit var btnHapus: Button
    private lateinit var adapter: ArrayAdapter<String>
    private var listUsersGlobal: List<User> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        listView = findViewById(R.id.listUser)
        btnRetry = findViewById(R.id.btnRetry)
        btnHapus = findViewById(R.id.btnHapus)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter

        muatUsers()

        btnRetry.setOnClickListener {
            muatUsers()
        }

        btnHapus.setOnClickListener {
            hapusDataPost(1) // Contoh menghapus postingan dengan ID 1
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            if (listUsersGlobal.isNotEmpty()) {
                val clickedUser = listUsersGlobal[position]
                muatPostsUser(clickedUser.id, clickedUser.name)
            }
        }
    }

    private fun muatUsers() {
        tvStatus.text = "Memuat data..."
        btnRetry.visibility = View.GONE
        listView.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val daftar = RetrofitClient.api.getUsers()
                listUsersGlobal = daftar

                adapter.clear()
                adapter.addAll(daftar.map { "${it.name} (@${it.username})" })
                tvStatus.text = "${daftar.size} pengguna dimuat (Klik salah satu untuk lihat Post)"
                btnRetry.visibility = View.GONE

            } catch (e: HttpException) {
                tvStatus.text = "Server error: HTTP ${e.code()}"
                btnRetry.visibility = View.VISIBLE
            } catch (e: IOException) {
                tvStatus.text = "Tidak ada koneksi internet"
                btnRetry.visibility = View.VISIBLE
            }
        }
    }

    // Fungsi untuk mengambil postingan berdasarkan ID pengguna yang dipilih (Soal 5)
    private fun muatPostsUser(userId: Int, userName: String) {
        tvStatus.text = "Memuat postingan $userName..."
        btnRetry.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val daftarPosts = RetrofitClient.api.getPostsByUser(userId)

                adapter.clear()
                adapter.addAll(daftarPosts.map { "• ${it.title}" })
                tvStatus.text = "${daftarPosts.size} postingan dari $userName"

            } catch (e: HttpException) {
                tvStatus.text = "Server error: HTTP ${e.code()}"
                btnRetry.visibility = View.VISIBLE
            } catch (e: IOException) {
                tvStatus.text = "Tidak ada koneksi internet"
                btnRetry.visibility = View.VISIBLE
            }
        }
    }
    private fun hapusDataPost(postId: Int) {
        tvStatus.text = "Menghapus postingan ID $postId..."

        lifecycleScope.launch {
            try {
                // Memanggil fungsi hapus dari interface ApiService menggunakan coroutine
                val response = RetrofitClient.api.hapusPost(postId)

                // Cek apakah kode HTTP sukses (200-299)
                if (response.isSuccessful) {
                    tvStatus.text = "Berhasil menghapus postingan ID $postId (HTTP ${response.code()})"
                } else {
                    tvStatus.text = "Gagal menghapus: HTTP ${response.code()}"
                }
            } catch (e: HttpException) {
                tvStatus.text = "Server error: HTTP ${e.code()}"
            } catch (e: IOException) {
                tvStatus.text = "Tidak ada koneksi internet"
            }
        }
    }
}